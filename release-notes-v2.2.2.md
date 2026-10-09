## Mobile Harness Extended v2.2.2

### Highlights in this build
- **Antigravity Multi-Account 401 Session Failover**:
  - Sanitized Google OAuth token expiration and `401 UNAUTHENTICATED` errors with friendly diagnostic messaging in `AntigravityRuntimeBridge`.
  - Expanded failover logic (`isAntigravityFailoverEligible`) in `MainViewModel` to seamlessly rotate to the next connected Google account upon 401 token expiry without interrupting active coding tasks or agent loops.
  - Multi-account state controller tracks `isAuthExpired` and `authExpiredAt` timestamps to prevent repeated auth failures on expired accounts.
  - Added dedicated "Session Expired" badge and "Reconnect" action button in `SettingsScreenModern` for intuitive one-tap re-authentication.
- **Developer Tools Settings Redesign (Equal Gaps)**:
  - Eliminated compounding vertical spacers (`Spacer(8.dp)`, `Spacer(6.dp)`) and manual row paddings that created 32–58dp gaps.
  - Unified spacing under `SettingsAccordion`'s consistent 10dp vertical arrangement for all elements, switches, and dividers.
  - Restored balanced two-line layout grouping tool label, version badge, and installation summary on the left, with action buttons ("Add", "Update", "Remove", "Included", progress %) vertically centered on the right.
- **Expanded Automated Unit Test Coverage**:
  - Added test coverage in `AntigravityMultiAccountTest` for 401 unauthenticated failover, expiration state transitions, and skip-expired round-robin rotation.

### Release files
- **Online Release:** `app-online-release.apk`
- **Offline Release:** `app-offline-release.apk`
