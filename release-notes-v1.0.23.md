## Mobile Harness v1.0.23

### Changes in this build
- **Flexible Project Directory Binding & Custom Root Directory:**
  - Added native device folder picker directly in the "New Project" dialog so users can bind an existing device directory upon project creation.
  - Added "Open / Bind folder from device" action and quick-action empty state card in the Files tab.
  - Added custom working directory configuration (`Tune` button in Files tab) allowing users to select any subfolder as the active project root.
  - Added `BIND_FOLDER` command in the workspace Command Palette (`Ctrl/Cmd+Shift+P`).
- **Automated Browser DevStack (Chromium & Puppeteer):**
  - Integrated `DevStack.BROWSER` toolchain featuring headless Chromium and Puppeteer CLI.
  - Enhanced Preview tab with dual-mode support: Live Web DevServer mode and Automated Chromium CDP View with real-time inspection, screenshot capture, and navigation controls.
  - Integrated `browser-automation` agent skill automatically provisioned to projects with browser stack enabled.
- **Instant Task Completion & Progress Indicator Fixes:**
  - Fixed issue where the top-right header progress spinner (`CircularProgressIndicator`) remained spinning after task completion by emitting `SessionCompleted` immediately upon terminal `Result` event in `AntigravityRuntimeBridge` without waiting on PRoot process exit.
  - Fixed `deriveScratchpadItems` to guarantee no item remains in `TaskStatus.RUNNING` when the session is inactive (`isRunning == false`).
  - Added `isRunning` synchronization to `ScratchpadPill` so running badges and orange indicators halt immediately upon task completion.
- **Connection Lost & Network Recovery Resume Fix:**
  - Fixed issue where clicking the "Resume" button during network connection loss did not restart or resume the task.
  - Preserved `lastInterruptedRequest` across recovery states and added fallback request reconstruction so clicking "Resume" immediately initiates session reconnect.
  - Extended network resilience and session recovery across all supported agent runtimes.
- **Comprehensive Unit Testing & Release Verification:**
  - Added test suites for `SkillManager` and `DevStack.BROWSER`.
  - Bumped version to `v1.0.23` (`versionCode = 24`).
  - Verified tests pass across both online and offline test configurations.

### Release files
- **Online Release:** app-online-release.apk
- **Offline Release:** app-offline-release.apk
- **Update Manifest:** mobile-harness-update.json
