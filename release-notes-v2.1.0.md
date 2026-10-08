## Mobile Harness Extended v2.1.0

### Changes in this build

- **Real Mobile IDE (v1.2):**
  - **Multi-Tab Code Editor:** Horizontal tab bar with dirty indicators, active tab tracking, and state persistence across app backgrounding and device rotations.
  - **Touch-Friendly Mobile Coding Toolbar:** Quick-access symbol bar with bracket auto-pairing (`{}`, `()`, `[]`), tab indentation/unindent, comment toggles, arrows, and operator shortcuts.
  - **Universal Command Palette:** `Ctrl/Cmd+P` dialog for fuzzy workspace file navigation and categorized action triggers (Editor, Navigation, Files, Build).
  - **Project-Wide Search & Replace:** `GlobalSearchEngine` featuring regex, case-sensitivity, whole-word matching, file pattern filters, and direct line jumping.
  - **Contextual File Tree:** Full interactive file management (`FileOperationsController` & `FileTreeComponent`) supporting Create File/Folder, Rename, Delete, Duplicate, and Move with directory traversal security.

- **Multi-Language Semantic Code Intelligence (v1.3):**
  - **9-Language AST Symbol Extraction:** High-speed symbol parsing across Kotlin, Java, TypeScript, JavaScript, Python, Go, Rust, C/C++, and Shell.
  - **Call Graphs & Dependencies:** Forward and inverse dependency mapping (`imports` and `dependents`) with cross-file symbol call reference resolution.
  - **Enriched Project Index:** Symbol-aware indexing and structured code outlines integrated directly into `ProjectIndex`.

- **AI Context Engine 2.0 & Persistent Project Memory:**
  - **Automatic Intent Classification:** Intelligently categorizes requests into `BUG_FIX`, `FEATURE_IMPLEMENTATION`, `REFACTORING`, `BUILD_OR_TEST`, and `QUESTION`.
  - **Multi-Tier Semantic Relevance Ranking:** Scores files using explicit `@mentions`, direct connected dependencies, AST symbol matches, and matching imports.
  - **Token Budget Management:** Context compression maintaining essential pointers within configurable token budgets.
  - **Persistent Project Memory Store:** `ProjectMemoryStore` preserves architectural decisions, previous error patterns, coding conventions, and preferences across agent sessions.

- **Autonomous Agent Orchestrator & Self-Healing Loop:**
  - **14-State Software Engineering Lifecycle:** State machine driving `UNDERSTAND` → `PLAN` → `IMPLEMENT` → `BUILD` → `TEST` → `ANALYZE_FAILURE` → `FIX` → `RETEST` → `REVIEW` → `APPROVE`.
  - **Automated Compiler Diagnostics Analysis:** `SelfHealingLoop` parses compiler outputs, classifies 6 error classes, extracts affected source files, and suggests targeted corrective actions.
  - **Self-Healing Retries & Checkpoint Rollback:** Configurable repair retry budgets with automated fallback rollback to baseline checkpoints upon unrecoverable errors.

- **Granular Permission & Safety Engine:**
  - **Dangerous Command Blocker:** `DangerousCommandDetector` halts destructive commands (`rm -rf /`, `mkfs`, raw `dd` disk writes, fork bombs, and untrusted remote script piping to root shells).
  - **Sensitive File Shielding:** Blocks unauthorized access to `.env` files, SSH private keys, Android/Java keystores (`*.keystore`, `*.jks`), and cloud tokens.
  - **Emergency Kill Switch:** Immediate one-tap cutoff halting all agent tool executions and background operations.
  - **Granular Policy Scopes:** 11 permission categories governed by 5 evaluation scopes (`ONCE`, `SESSION`, `PROJECT`, `ALWAYS`, `DENY`).
  - **Persistent Security Audit Trail:** `SecurityAuditLogger` records all security events, evaluations, and blocks with crash-safe atomic disk logging.

- **Foundation Hardening & Multi-Account Rotation:**
  - **Crash-Safe File Ops:** `SafeFileOps` atomic writes with staging sync (`fd.sync()`) preventing file corruption during unexpected Android process kills.
  - **Storage Quota & Health Monitors:** `RuntimeHealthController` guards against low-disk failures before running builds and cleans orphaned temporary artifacts.
  - **Session Crash Recovery:** `AgentSessionManager` detects abnormal terminations and restores session state upon app relaunch.
  - **Multi-Account OAuth Rotation:** `AntigravityAuthController` handles round-robin Google OAuth failover across 60-minute, daily, and 429 rate limits.
  - **Multi-Tab Terminal Session Manager:** `TerminalSessionController` manages parallel Linux terminal instances with search and scrollback history.

### Release files
- **Online Release:** app-online-release.apk
- **Offline Release:** app-offline-release.apk
- **Update Manifest:** mobile-harness-update.json
