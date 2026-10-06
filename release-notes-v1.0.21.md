## Mobile Harness v1.0.21

### Changes in this build
- **Interactive Clickable Links in Chat & Summaries:**
  - Resolved the issue where links in assistant summaries and chat messages were rendered as static, unclickable text.
  - Markdown links (`[label](url)`) and autolinks (`<http://...>`) now generate interactive `LinkAnnotation.Url` spans that open directly in the user's default web browser when clicked.
  - Auto-detection for raw HTTP and HTTPS URLs (`https://...`, `http://...`) appearing anywhere in text, correctly stripping trailing punctuation (`.`, `,`, `!`).
  - Added fallback to `Intent.ACTION_VIEW` with `FLAG_ACTIVITY_NEW_TASK` to guarantee safe browser launches.
  - User messages in chat now also render markdown links and raw URLs interactively.
- **Markdown Data Table Rendering:**
  - Added full support for GitHub-flavored Markdown tables (`MarkdownBlock.Table`).
  - Renders a clean, horizontally scrollable data card on mobile screens with styled header rows, dividers, and cell margins.
  - All cells support inline formatting, code spans, and clickable links.
- **Execution Modes (Plan, Build, Unified):**
  - Ultra-compact Mode Selector Bar on top of chat.
  - Plan Mode: analyzes requirements and outputs structured architecture and roadmaps.
  - Build Mode: executes changes end-to-end autonomously.
  - Unified Mode: presents interactive roadmap with step checklists, status badges, and user comments for approval before building.
- **Interactive RoadmapCard & Scratchpad:**
  - Live roadmap status checklist with affected file tags.
  - User notes & guidance injection before or during build execution.
  - Antigravity / Cursor-style ultra-compact scratchpad banner with expandable status pad.
- **Runtime Resilience & Toolchain Auto-Update:**
  - Fixed false-positive network error cards on completed tasks.
  - Universal skills and agent CLIs auto-update support.
  - Added Ubuntu Focal universe fallback and PHP 8.4 repository handling for PHP installations.
  - Version-aware DevStack updates for Python, Android, PHP, and C/C++.

### Release files
- **Online Release:** app-online-release.apk
- **Offline Release:** app-offline-release.apk
- **Update Manifest:** mobile-harness-update.json

Both online and offline release APKs are signed with the permanent release key, aligned, and verified for in-place upgrades.
