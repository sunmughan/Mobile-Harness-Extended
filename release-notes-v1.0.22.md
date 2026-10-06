## Mobile Harness v1.0.22

### Changes in this build
- **Continuous Task Attachments:**
  - Resolved the limitation where the paperclip (`AttachFile`) icon was disabled while an agent task was actively running.
  - Users can now attach up to 5 media or file items dynamically to the chat box while tasks are running, queuing context for follow-up prompts without waiting for task termination.
- **Collapsible Workspace Controls:**
  - Added an animated `Tune` toggle button directly beside the attachment button in the chat pill.
  - Tapping `Tune` collapses the top `Scratchpad` banner and bottom `ModeSelectorBar` on demand, freeing significant vertical screen real estate for active conversations.
  - Includes rotation animation and active scratchpad indicator badge.
- **In-App Offline Privacy Policy:**
  - Added an in-app Privacy Policy screen rendering the bundled `PRIVACY.md` offline without requiring internet access or opening external browsers.
- **CI & Release Pipeline Hardening:**
  - Fixed release asset retrieval in private repository workflows using authenticated `gh release download`.
  - Added automatic runner disk space cleanup freeing 50+ GB before Android SDK and emulator setup.
  - Fixed multiline shell execution syntax in Android emulator test steps via dedicated script wrapper.
  - Added quota resilience to artifact uploads (`continue-on-error: true`) and purged obsolete build artifacts.
- **Release Verification & Signing:**
  - Bumped version to `v1.0.22` (`versionCode = 23`).
  - Production release APKs signed with permanent Codeair Software Solutions key, verified with SHA-256 certificate checks, and tested with in-place Android 34 emulator upgrades.

### Release files
- **Online Release:** app-online-release.apk (87.2 MiB)
- **Offline Release:** app-offline-release.apk (847.3 MiB)
- **Update Manifest:** mobile-harness-update.json
