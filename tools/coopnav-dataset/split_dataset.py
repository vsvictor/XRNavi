#!/usr/bin/env python3
import argparse
import hashlib
import json
import random
import shutil
from pathlib import Path

from validate_dataset import CLASSES, IMAGE_SUFFIXES, validate_pair


def split(source: Path, output: Path, manifest: Path, seed: int, train: float, val: float) -> dict:
    source = source.resolve()
    output = output.resolve()
    if not source.is_dir() or output == source or source in output.parents or output in source.parents:
        raise ValueError("source must exist and output must be separate from source")
    if output.exists():
        raise ValueError(f"output already exists: {output}; choose a new versioned directory")
    if not 0 < train < 1 or not 0 < val < 1 or train + val >= 1:
        raise ValueError("invalid split proportions")
    data = json.loads(manifest.read_text(encoding="utf-8"))
    if data.get("schemaVersion") != 1 or not isinstance(data.get("images"), list):
        raise ValueError("invalid review manifest")
    rows = data["images"]
    paths = {}
    hashes = {}
    groups = {}
    for row in rows:
        rel = row.get("image")
        if not isinstance(rel, str) or not rel:
            raise ValueError("image path missing")
        image = (source / rel).resolve()
        if source not in image.parents or image.suffix.lower() not in IMAGE_SUFFIXES or not image.is_file():
            raise ValueError(f"invalid image path: {rel}")
        if image in paths:
            raise ValueError(f"duplicate manifest image: {rel}")
        if any(row.get(field) != "approved" for field in ("licenseReview", "privacyReview", "annotationReview", "sceneReview")):
            raise ValueError(f"reviews incomplete: {rel}")
        group = row.get("sceneGroup")
        if not isinstance(group, str) or not group.strip():
            raise ValueError(f"scene group missing: {rel}")
        if not isinstance(row.get("sourceUrl"), str) or not row["sourceUrl"].startswith("https://"):
            raise ValueError(f"source URL missing: {rel}")
        if not isinstance(row.get("license"), str) or not row["license"].strip():
            raise ValueError(f"license missing: {rel}")
        if not isinstance(row.get("licenseUrl"), str) or not row["licenseUrl"].startswith(("https://", "http://")):
            raise ValueError(f"license URL missing: {rel}")
        if not isinstance(row.get("author"), str) or not row["author"].strip():
            raise ValueError(f"author missing: {rel}")
        digest = hashlib.sha256(image.read_bytes()).hexdigest()
        if row.get("sha256") != digest:
            raise ValueError(f"image checksum mismatch: {rel}")
        issues = validate_pair(image, image.with_suffix(".txt"))
        if issues:
            raise ValueError("\n".join(issues))
        if digest in hashes and hashes[digest] != group:
            raise ValueError(f"identical images assigned to different scene groups: {rel}")
        hashes[digest] = group
        paths[image] = row
        groups.setdefault(group, []).append(image)
    actual = {p.resolve() for p in source.rglob("*") if p.is_file() and p.suffix.lower() in IMAGE_SUFFIXES}
    if not actual or actual != paths.keys():
        raise ValueError("manifest must cover every image in the source directory exactly once")
    names = sorted(groups)
    if len(names) < 3:
        raise ValueError("at least three independent scene groups are required")
    random.Random(seed).shuffle(names)
    train_end = max(1, min(len(names) - 2, int(len(names) * train)))
    val_end = max(train_end + 1, min(len(names) - 1, int(len(names) * (train + val))))
    counts = dict.fromkeys(("train", "val", "test"), 0)
    assignments = []
    for part in counts:
        (output / "images" / part).mkdir(parents=True, exist_ok=True)
        (output / "labels" / part).mkdir(parents=True, exist_ok=True)
    for index, group in enumerate(names):
        part = "train" if index < train_end else "val" if index < val_end else "test"
        for image in sorted(groups[group]):
            row = paths[image]
            name = hashlib.sha256(str(image.relative_to(source)).encode()).hexdigest()[:20]
            shutil.copy2(image, output / "images" / part / (name + image.suffix.lower()))
            shutil.copy2(image.with_suffix(".txt"), output / "labels" / part / (name + ".txt"))
            assignments.append({"image": str(image.relative_to(source)), "outputName": name + image.suffix.lower(), "split": part, "sceneGroup": group, "sha256": row["sha256"], "sourceUrl": row["sourceUrl"], "license": row["license"], "licenseUrl": row["licenseUrl"], "author": row["author"]})
            counts[part] += 1
    (output / "assignments.json").write_text(json.dumps({"seed": seed, "images": assignments}, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    yaml = "path: " + json.dumps(str(output), ensure_ascii=False) + "\ntrain: images/train\nval: images/val\ntest: images/test\nnames:\n"
    yaml += "".join(f"  {index}: {name}\n" for index, name in enumerate(CLASSES))
    (output / "dataset.yaml").write_text(yaml, encoding="utf-8")
    return counts


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--source", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--seed", type=int, default=20261121)
    parser.add_argument("--train", type=float, default=0.8)
    parser.add_argument("--val", type=float, default=0.15)
    args = parser.parse_args()
    try:
        print(split(args.source, args.output, args.manifest, args.seed, args.train, args.val))
    except (OSError, ValueError, KeyError, TypeError) as error:
        parser.exit(1, f"Split failed: {error}\n")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
