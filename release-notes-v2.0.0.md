## Mobile Harness v2.0.0

### Changes in this build
- **Foundation Hardening & Safe Workspace Management:**
  - **Workspace Checkpoint Manager:** Integrated automatic snapshot checkpoints before high-risk operations and agent tasks, supporting one-tap rollback to previous file tree states and atomic disk writes.
  - **Agent Session Crash Recovery:** Added disk persistence for agent session metadata, live transcripts, and background execution states in `AgentSessionManager`.
  - **Interrupted Task Auto-Restore Banner:** Added top-level recovery alert UI on the Main IDE screen when an interrupted task or abnormal app exit is detected, offering one-tap session resumption.
  - **Process & PTY Cleanup Lifecycle:** Guaranteed clean process teardown, signal delivery, and PTY descriptor deallocation upon app lifecycle events or crash recovery.

- **Real Mobile IDE (Multi-Tab Code Editor & Advanced Terminal):**
  - **Multi-Tab Code Editor Strip:** Added visual horizontal tab bar supporting open files, active tab indicator, dirty state indicators (unsaved change dot), and one-tap tab close buttons.
  - **Tab State Persistence:** Editor automatically preserves open files and active selection across app backgrounding, warm-starts, and device rotations.
  - **Multi-Session Terminal:** Support for running multiple parallel terminal sessions with a sleek tab selector, status badges, and session creation/deletion.
  - **Terminal Search & Filter:** Integrated live search overlay in the terminal supporting real-time text matching, case-sensitivity toggle, and regex filtering across scrollback history.

- **Autonomous Agent Loop Engine (Self-Healing Build & Error Repair):**
  - **Autonomous Execution Loop:** Implemented multi-turn build & test repair engine with configurable iteration budgets and stop guardrails.
  - **Automatic Toolchain Detection:** Automatically detects Gradle (`gradlew`), Node/npm (`package.json`), Python/pip (`setup.py`, `requirements.txt`), and Rust/Cargo (`Cargo.toml`).
  - **Compiler Error Extraction & Auto-Prompting:** Parses compiler diagnostics, stack traces, and test failures to generate contextual corrective repair prompts for the AI agent.

- **Production Firebase Authentication & Account Foundation:**
  - **Google Credential Manager & Email/Password:** Integrated Android 14+ Credential Manager API with legacy Google Sign-In fallback and Email/Password authentication.
  - **Seamless Account Linking:** Supports anonymous/guest account upgrading with automatic conversation and project migration upon authentication.
  - **Token Management & Interceptors:** Secure encrypted token storage with automatic token refresh interceptor for cloud API requests.

- **Toolchain, Dependencies & Architecture Updates:**
  - Upgraded target SDK to Android 36 with Jetpack Compose 2026.02 BOM and Kotlin 2.2 compiler support.
  - Added native build prebuilt fallback support for host architecture compatibility.
  - Comprehensive unit test coverage for `AutonomousLoopEngine` and `WorkspaceCheckpointManager`.

### Release files
- **Online Release:** app-online-release.apk
- **Offline Release:** app-offline-release.apk
- **Update Manifest:** mobile-harness-update.json
