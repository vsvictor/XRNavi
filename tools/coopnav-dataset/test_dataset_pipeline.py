import hashlib
import json
import tempfile
import unittest
from pathlib import Path

from PIL import Image

from split_dataset import split
from validate_dataset import validate_pair


class DatasetPipelineTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)
        self.source = self.root / "source"
        self.source.mkdir()
        self.rows = []
        for i in range(4):
            image = self.source / f"{i}.jpg"
            Image.new("RGB", (10, 10), (i * 50, 0, 0)).save(image)
            image.with_suffix(".txt").write_text("0 0.5 0.5 0.4 0.4\n")
            self.rows.append({
                "image": image.name, "sha256": hashlib.sha256(image.read_bytes()).hexdigest(),
                "sourceUrl": "https://example.test/image", "sceneGroup": f"scene-{i}",
                "license": "CC0", "licenseUrl": "https://creativecommons.org/publicdomain/zero/1.0/",
                "author": "Example",
                "licenseReview": "approved", "privacyReview": "approved",
                "annotationReview": "approved", "sceneReview": "approved",
            })
        self.manifest = self.root / "manifest.json"

    def run_split(self):
        self.manifest.write_text(json.dumps({"schemaVersion": 1, "images": self.rows}))
        return split(self.source, self.root / "out", self.manifest, 1, 0.8, 0.15)

    def test_grouped_split_preserves_images_and_provenance(self):
        self.rows[1]["sceneGroup"] = self.rows[0]["sceneGroup"]
        counts = self.run_split()
        self.assertEqual(sum(counts.values()), 4)
        assignments = json.loads((self.root / "out" / "assignments.json").read_text())["images"]
        self.assertEqual(len({x["split"] for x in assignments if x["sceneGroup"] == "scene-0"}), 1)
        self.assertEqual(len({x["split"] for x in assignments}), 3)

    def test_pending_review_prevents_output(self):
        self.rows[0]["privacyReview"] = "pending"
        with self.assertRaisesRegex(ValueError, "reviews incomplete"):
            self.run_split()
        self.assertFalse((self.root / "out").exists())

    def test_missing_manifest_entry_prevents_output(self):
        self.rows.pop()
        with self.assertRaisesRegex(ValueError, "cover every image"):
            self.run_split()

    def test_duplicate_content_cannot_cross_groups(self):
        original = self.source / "0.jpg"
        duplicate = self.source / "1.jpg"
        duplicate.write_bytes(original.read_bytes())
        self.rows[1]["sha256"] = self.rows[0]["sha256"]
        with self.assertRaisesRegex(ValueError, "different scene groups"):
            self.run_split()

    def test_rejects_outside_and_zero_sized_boxes(self):
        label = self.source / "0.txt"
        label.write_text("0 0.99 0.5 0.5 0.5\n1 0.5 0.5 0 0.5\n")
        self.assertEqual(len(validate_pair(self.source / "0.jpg", label)), 2)

    def test_existing_output_refused(self):
        (self.root / "out").mkdir()
        with self.assertRaisesRegex(ValueError, "already exists"):
            self.run_split()


if __name__ == "__main__":
    unittest.main()
