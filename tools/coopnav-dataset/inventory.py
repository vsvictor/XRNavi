#!/usr/bin/env python3
import csv
import hashlib
import json
from pathlib import Path

from PIL import Image, ImageOps

ROOT = Path(__file__).resolve().parent
RAW = ROOT / "data" / "raw" / "commons-v2"
REVIEW = ROOT / "data" / "review"


def dhash(path: Path) -> int:
    with Image.open(path) as image:
        image = ImageOps.exif_transpose(image).convert("L").resize((9, 8), Image.Resampling.LANCZOS)
        pixels = list(image.getdata())
    return sum((pixels[y * 9 + x] > pixels[y * 9 + x + 1]) << (y * 8 + x)
               for y in range(8) for x in range(8))


def main() -> None:
    initial = json.loads((ROOT / "initial-review.json").read_text(encoding="utf-8"))
    rejected = set(initial["rejectedFromInitialVehicleSet"]["pageIds"])
    records = [json.loads(p.read_text(encoding="utf-8")) for p in sorted(RAW.glob("*.json"))]
    if not records:
        raise SystemExit("No candidate metadata found")
    REVIEW.mkdir(parents=True, exist_ok=True)
    review_path = REVIEW / "candidates.csv"
    if review_path.exists():
        raise SystemExit(f"Review ledger already exists; refusing to overwrite: {review_path}")
    fingerprints = []
    rows = []
    for record in records:
        path = RAW / record["file"]
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        if digest != record["sha256"]:
            raise SystemExit(f"Checksum mismatch: {path}")
        fingerprint = dhash(path)
        fingerprints.append((record["pageId"], digest, fingerprint))
        rows.append({
            "page_id": record["pageId"], "image": record["file"],
            "search_class": record["searchClass"], "title": record["title"],
            "source_url": record["sourceUrl"], "license": record["license"],
            "license_url": record["licenseUrl"], "sha256": digest,
            "visual_triage": "preliminary_reject" if record["pageId"] in rejected else "pending",
            "license_review": "pending", "privacy_review": "pending",
            "annotation_review": "pending", "scene_review": "pending", "scene_group": "",
        })
    with review_path.open("x", encoding="utf-8", newline="") as file:
        writer = csv.DictWriter(file, fieldnames=rows[0].keys())
        writer.writeheader()
        writer.writerows(rows)
    pairs = []
    for index, (first, hash1, dhash1) in enumerate(fingerprints):
        for second, hash2, dhash2 in fingerprints[index + 1:]:
            distance = (dhash1 ^ dhash2).bit_count()
            if hash1 == hash2 or distance <= 5:
                pairs.append({"first": first, "second": second, "exact": hash1 == hash2, "dhashDistance": distance})
    (REVIEW / "possible-duplicates.json").write_text(
        json.dumps({"method": "dHash 9x8, distance <= 5; suggestions only", "pairs": pairs}, indent=2) + "\n",
        encoding="utf-8",
    )
    print(f"Candidates: {len(rows)}; preliminary rejects: {sum(r['visual_triage'] == 'preliminary_reject' for r in rows)}; duplicate suggestions: {len(pairs)}")


if __name__ == "__main__":
    main()
