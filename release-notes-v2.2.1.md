## Mobile Harness Extended v2.2.1

### Highlights in this build
- **Modernized RTL-Safe AutoMirrored Compose Icons**:
  - Replaced deprecated vector icons across UI navigation and editors (`List`, `WrapText`, `NoteAdd`, `Chat`, `OpenInNew`, `Comment`, `ArrowBack`, `ArrowForward`) with RTL-compliant `Icons.AutoMirrored.Filled.*`.
- **Low-Latency Background Execution & Clean Diagnostics**:
  - `RuntimeExecutionService` upgraded to utilize `WIFI_MODE_FULL_LOW_LATENCY` on Android 10+ (API 29+) with backward-compatible fallback and compiler deprecation suppression.
  - Suppressed deprecated member override diagnostic in `MobileHarnessFirebaseMessagingService`.
- **Stealth Deal Sniper & Anti-Sponsored Card Filtering**:
  - Injected client-side DOM filtering in `PriceScraperEngine` for Flipkart and Amazon India to ignore sponsored carousel cards.
  - Added deal countdown badge and flash sale timer parsing (`dealBadge`).
  - Strengthened price number sanitization supporting range syntax (`₹12,499 - ₹14,999`), currency tags (`Rs 1,299`), and non-breaking space characters.
- **Expanded Automated Unit Test Coverage**:
  - Added comprehensive test suites in `PriceScraperEngineTest` covering sponsored card filtering, price ranges, and deal badge extraction.

### Release files
- **Online Release:** `app-online-release.apk`
- **Offline Release:** `app-offline-release.apk`
