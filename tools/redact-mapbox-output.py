#!/usr/bin/env python3
"""Filter build/SDK output before logging; never run credential builds with shell tracing."""
import re
import sys

for line in sys.stdin:
    sys.stdout.write(re.sub(r"\b[ps]k\.[A-Za-z0-9._-]+", "[REDACTED_MAPBOX_TOKEN]", line))
    sys.stdout.flush()
