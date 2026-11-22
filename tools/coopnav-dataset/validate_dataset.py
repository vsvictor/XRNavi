#!/usr/bin/env python3
import argparse
import math
from pathlib import Path

from PIL import Image, UnidentifiedImageError

CLASSES = ("car", "truck", "bus", "trolleybus")
IMAGE_SUFFIXES = {".jpg", ".jpeg", ".png", ".webp"}


def validate_pair(image: Path, label: Path) -> list[str]:
    issues = []
    try:
        with Image.open(image) as opened:
            opened.verify()
    except (OSError, ValueError, UnidentifiedImageError) as error:
        issues.append(f"{image}: invalid image: {error}")
    if not label.is_file():
        return issues + [f"{image}: missing label"]
    try:
        lines = label.read_text(encoding="utf-8").splitlines()
    except (OSError, UnicodeError) as error:
        return issues + [f"{label}: cannot read: {error}"]
    for number, line in enumerate(lines, 1):
        fields = line.split()
        try:
            if len(fields) != 5:
                raise ValueError("expected class x y width height")
            cls = int(fields[0])
            x, y, w, h = map(float, fields[1:])
            if cls not in range(len(CLASSES)):
                raise ValueError("invalid class")
            if not all(math.isfinite(v) for v in (x, y, w, h)):
                raise ValueError("non-finite coordinate")
            if w <= 0 or h <= 0 or x - w / 2 < -1e-7 or y - h / 2 < -1e-7 or x + w / 2 > 1 + 1e-7 or y + h / 2 > 1 + 1e-7:
                raise ValueError("box is empty or outside image")
        except ValueError as error:
            issues.append(f"{label}:{number}: {error}")
    return issues


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--source", type=Path, default=Path(__file__).resolve().parent / "data" / "yolo")
    source = parser.parse_args().source
    if not source.is_dir():
        parser.error(f"source directory not found: {source}")
    images = sorted(p for p in source.rglob("*") if p.is_file() and p.suffix.lower() in IMAGE_SUFFIXES)
    if not images:
        parser.error("no images found")
    issues = [issue for image in images for issue in validate_pair(image, image.with_suffix(".txt"))]
    for issue in issues:
        print(issue)
    print(f"Images: {len(images)}, issues: {len(issues)}")
    return int(bool(issues))


if __name__ == "__main__":
    raise SystemExit(main())
