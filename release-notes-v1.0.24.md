## Mobile Harness v1.0.24

### Changes in this build
- **Instant Warm-Start App Resume & Splash Delay Removal:**
  - Pre-hydrated active project, chats, and messages in `MainViewModel` initial state on warm app launches so the workspace renders immediately on the 1st frame.
  - Eliminated the artificial 3-second delay (`MINIMUM_INITIALIZATION_SCREEN_MS`) on warm launches.
  - Kept runtime verification on silent background I/O threads so UI never flips back to `INITIALIZING` or `CHECKING` when returning to the app.
- **Samsung One UI Process & Memory Retention:**
  - Added `android:largeHeap="true"` to `<application>` in `AndroidManifest.xml` to prevent aggressive OS Low Memory Killer (LMK) eviction on high-performance devices like Samsung Galaxy S26 Ultra.
  - Added `android:alwaysRetainTaskState="true"` to `MainActivity` to maintain the exact task hierarchy and state when backgrounded.
- **Full Chromium Multi-Tab Browser & In-Preview Controls:**
  - Added native multi-tab strip in the Preview tab with new tab button, tab closing, and tab switching.
  - Implemented 3-dots Chromium action menu featuring New Tab, Find in Page, Request Desktop Site toggle, Bookmarks, History, Downloads, and Extensions.
  - Unified local dev server and web browsing seamlessly in a single modern browser surface.
- **Chat Header Refinements:**
  - Updated title to display "IDE" with mobile device icon and moved back button to right side.
  - Cleaned subtitle to display only the active conversation title.
  - Aligned the 4 header action icons directly with the "IDE" title row.
- **Token Optimization Suite:**
  - Implemented client-side context window optimizations and streamlined prompt formatting.

### Release files
- **Online Release:** app-online-release.apk
- **Offline Release:** app-offline-release.apk
- **Update Manifest:** mobile-harness-update.json
