## Mobile Harness v1.0.22

### Changes in this build
- **In-App Privacy Policy Screen:**
  - Added a dedicated, offline-bundled In-App Privacy Policy screen accessible directly from Settings.
  - Guarantees complete offline availability of privacy policies even when repository visibility changes.
  - Supports full markdown rendering, back navigation, and system gesture back handlers.
- **Chat Pill Send Button Vertical Centering:**
  - Vertically centered the send button in default / single-line chat pill state.
  - Dynamically preserves bottom alignment when multi-line text expands the chat pill.
- **New Contributor Cards:**
  - Added Contributor cards for 0xMassi (Valerio) and Tech Jarves with profile pictures, bios, and links to GitHub / LinkedIn.
- **Local Device Folder Import:**
  - Integrated `ActivityResultContracts.OpenDocumentTree` to import existing project folders directly from the device file manager into `/workspace/`.
- **Multi-Project & Multi-Chat Memorization:**
  - Memorizes active project and active conversation across app restarts.
  - Added Chat Switcher dialog supporting fast switching, inline renaming, and conversation deletion.

### Release files
- **Online Release:** app-online-release.apk
- **Offline Release:** app-offline-release.apk
- **Update Manifest:** mobile-harness-update.json

Both online and offline release APKs are signed with the permanent release key, aligned, and verified for in-place upgrades.
