#!/usr/bin/env python3
"""Generate ignored runtime configuration without displaying credentials."""
import os
import re
from pathlib import Path


def main():
    root = Path(__file__).resolve().parent.parent
    source = root / "tokens.dat"
    if not source.is_file():
        raise SystemExit("Missing local tokens.dat. See tools/mapbox-setup.en.md.")
    tokens = re.findall(r"(?m)^\s*(pk\.[A-Za-z0-9._-]+)\s*$", source.read_text())
    if len(tokens) != 1:
        raise SystemExit("Expected exactly one public token on its own line in tokens.dat.")
    destination = root / "iosApp" / "Mapbox.local.xcconfig"
    os.umask(0o077)
    destination.write_text("// Generated locally; do not commit.\nMAPBOX_PUBLIC_TOKEN = " + tokens[0] + "\n")
    destination.chmod(0o600)
    print("Local Mapbox iOS configuration generated (credential values suppressed).")


if __name__ == "__main__":
    main()
