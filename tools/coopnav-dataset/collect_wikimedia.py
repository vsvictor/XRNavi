#!/usr/bin/env python3

import hashlib
import io
import json
import sys
import time
from datetime import datetime, timezone
from email.utils import parsedate_to_datetime
from pathlib import Path

import requests
from PIL import Image, UnidentifiedImageError

ROOT = Path(__file__).resolve().parent
MANIFEST = ROOT / "source_manifest.json"
CACHE = ROOT / ".cache" / "commons-search"
OUTPUT = ROOT / "data" / "raw" / "commons-v2"
API = "https://commons.wikimedia.org/w/api.php"
USER_AGENT = "XRNavi-CoopNav-Dataset/1.0 (https://github.com/vsvictor/XRNavi)"
REQUEST_INTERVAL = 5.0
MAX_ATTEMPTS = 4
MAX_IMAGE_BYTES = 20 * 1024 * 1024


def retry_after_seconds(value: str | None, now: datetime | None = None) -> float:
    if not value:
        return 0.0
    try:
        return max(0.0, float(value))
    except ValueError:
        try:
            target = parsedate_to_datetime(value)
            if target.tzinfo is None:
                target = target.replace(tzinfo=timezone.utc)
            return max(0.0, (target - (now or datetime.now(timezone.utc))).total_seconds())
        except (TypeError, ValueError, OverflowError):
            return 0.0


def allowed_license(name: str, url: str) -> bool:
    # Candidates only: a human must still verify copyright and license metadata.
    from urllib.parse import urlparse

    parsed = urlparse(url)
    if parsed.scheme not in ("http", "https") or parsed.hostname != "creativecommons.org":
        return False
    path = parsed.path.lower().strip("/").split("/")
    if len(path) < 3 or path[2] not in ("1.0", "2.0", "2.5", "3.0", "4.0"):
        return False
    if path[:2] in (["licenses", "by"], ["licenses", "by-sa"], ["publicdomain", "zero"]):
        return bool(name.strip())
    return False


def atomic_json(path: Path, value: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_suffix(path.suffix + ".tmp")
    temporary.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    temporary.replace(path)


class CommonsClient:
    def __init__(self) -> None:
        self.session = requests.Session()
        self.session.headers.update({"User-Agent": USER_AGENT})
        self.last_request = 0.0

    def get(self, url: str, **kwargs) -> requests.Response:
        for attempt in range(MAX_ATTEMPTS):
            delay = REQUEST_INTERVAL - (time.monotonic() - self.last_request)
            if delay > 0:
                time.sleep(delay)
            self.last_request = time.monotonic()
            response = self.session.get(url, timeout=(15, 60), **kwargs)
            if response.status_code != 429:
                response.raise_for_status()
                return response
            if attempt == MAX_ATTEMPTS - 1:
                response.raise_for_status()
            wait = max(30.0 * 2**attempt, retry_after_seconds(response.headers.get("Retry-After")))
            response.close()
            print(f"HTTP 429: waiting {wait:.0f} seconds before retry", flush=True)
            time.sleep(wait)
        raise RuntimeError("Unreachable retry state")

    def search(self, query: str, limit: int) -> list[dict]:
        params = {
            "action": "query", "generator": "search", "gsrnamespace": 6,
            "gsrsearch": f"{query} filetype:bitmap", "gsrlimit": limit,
            "prop": "imageinfo", "iiprop": "url|extmetadata|mime",
            "iiurlwidth": 1024, "format": "json", "maxlag": 5,
        }
        key = hashlib.sha256(json.dumps(params, sort_keys=True).encode()).hexdigest()
        cache_path = CACHE / f"{key}.json"
        if cache_path.exists():
            return json.loads(cache_path.read_text(encoding="utf-8"))
        with self.get(API, params=params) as response:
            data = response.json()
        if "error" in data:
            raise RuntimeError(f"Wikimedia API error: {data['error']}")
        pages = list(data.get("query", {}).get("pages", {}).values())
        atomic_json(cache_path, pages)
        return pages

    def download(self, url: str) -> bytes:
        with self.get(url, stream=True) as response:
            chunks = []
            size = 0
            for chunk in response.iter_content(chunk_size=65536):
                size += len(chunk)
                if size > MAX_IMAGE_BYTES:
                    raise ValueError(f"Image exceeds {MAX_IMAGE_BYTES} bytes")
                chunks.append(chunk)
        return b"".join(chunks)


def main() -> int:
    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    OUTPUT.mkdir(parents=True, exist_ok=True)
    client = CommonsClient()
    added = 0
    reused = 0
    skipped = 0
    for search in manifest["searches"]:
        for query in search["queries"]:
            print(f"[{search['class']}] {query}", flush=True)
            for page in client.search(query, search["maxResultsPerQuery"]):
                info = (page.get("imageinfo") or [{}])[0]
                metadata = info.get("extmetadata", {})
                license_name = metadata.get("LicenseShortName", {}).get("value", "")
                license_url = metadata.get("LicenseUrl", {}).get("value", "")
                url = info.get("thumburl") or info.get("url")
                if not url or not allowed_license(license_name, license_url):
                    skipped += 1
                    continue
                page_id = str(page["pageid"])
                record_path = OUTPUT / f"{page_id}.json"
                if record_path.exists():
                    record = json.loads(record_path.read_text(encoding="utf-8"))
                    if (OUTPUT / record["file"]).is_file():
                        reused += 1
                        continue
                content = client.download(url)
                try:
                    with Image.open(io.BytesIO(content)) as image:
                        image.verify()
                        extension = {"JPEG": ".jpg", "PNG": ".png"}.get(image.format)
                    if extension is None:
                        raise ValueError("Unsupported image format")
                except (UnidentifiedImageError, OSError, ValueError) as error:
                    print(f"  skipped {page_id}: {error}", file=sys.stderr, flush=True)
                    skipped += 1
                    continue
                image_path = OUTPUT / f"{page_id}{extension}"
                temporary = image_path.with_suffix(image_path.suffix + ".tmp")
                temporary.write_bytes(content)
                temporary.replace(image_path)
                record = {
                    "pageId": page_id, "file": image_path.name,
                    "sha256": hashlib.sha256(content).hexdigest(),
                    "searchClass": search["class"], "searchQuery": query,
                    "title": page.get("title"), "sourceUrl": info.get("descriptionurl"),
                    "originalUrl": info.get("url"), "downloadUrl": url,
                    "license": license_name, "licenseUrl": license_url,
                    "artist": metadata.get("Artist", {}).get("value"),
                    "credit": metadata.get("Credit", {}).get("value"),
                    "description": metadata.get("ImageDescription", {}).get("value"),
                    "reviewStatus": "pending",
                }
                atomic_json(record_path, record)
                added += 1
                print(f"  saved {page_id} ({added} new)", flush=True)
    print(f"Finished: {added} new, {reused} already present, {skipped} skipped", flush=True)
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (requests.RequestException, RuntimeError) as error:
        print(f"Collection stopped: {error}", file=sys.stderr, flush=True)
        raise SystemExit(1)
