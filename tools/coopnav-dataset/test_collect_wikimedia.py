import unittest
from datetime import datetime, timezone
from unittest.mock import Mock, patch

import requests

import collect_wikimedia as collector


class CollectorTests(unittest.TestCase):
    def test_allowed_licenses_require_recognized_commercial_license_url(self):
        self.assertTrue(collector.allowed_license("CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/"))
        self.assertTrue(collector.allowed_license("CC0", "https://creativecommons.org/publicdomain/zero/1.0/"))
        self.assertFalse(collector.allowed_license("CC BY-NC 4.0", "https://creativecommons.org/licenses/by-nc/4.0/"))
        self.assertFalse(collector.allowed_license("CC BY-ND 4.0", "https://creativecommons.org/licenses/by-nd/4.0/"))
        self.assertFalse(collector.allowed_license("Public domain", ""))
        self.assertFalse(collector.allowed_license("CC BY 4.0", "https://creativecommons.org.example.com/licenses/by/4.0/"))

    def test_retry_after_accepts_seconds_and_http_dates(self):
        now = datetime(2026, 11, 21, 12, 0, tzinfo=timezone.utc)
        self.assertEqual(120, collector.retry_after_seconds("120", now))
        self.assertEqual(60, collector.retry_after_seconds("Sat, 21 Nov 2026 12:01:00 GMT", now))
        self.assertEqual(0, collector.retry_after_seconds("invalid", now))

    @patch.object(collector.time, "sleep")
    def test_retry_waits_for_server_header_without_truncating(self, sleep):
        client = collector.CommonsClient()
        limited = Mock(status_code=429, headers={"Retry-After": "600"})
        success = Mock(status_code=200)
        client.session.get = Mock(side_effect=[limited, success])
        self.assertIs(success, client.get("https://example.test/"))
        self.assertTrue(any(call.args[0] >= 600 for call in sleep.call_args_list))
        limited.close.assert_called_once()

    @patch.object(collector.time, "sleep")
    def test_persistent_rate_limit_fails(self, _sleep):
        client = collector.CommonsClient()
        limited = Mock(status_code=429, headers={})
        limited.raise_for_status.side_effect = requests.HTTPError("429")
        client.session.get = Mock(return_value=limited)
        with self.assertRaises(requests.HTTPError):
            client.get("https://example.test/")
        self.assertEqual(collector.MAX_ATTEMPTS, client.session.get.call_count)


if __name__ == "__main__":
    unittest.main()
