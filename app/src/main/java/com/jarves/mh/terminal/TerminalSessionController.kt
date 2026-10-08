package com.jarves.mh.terminal

import com.jarves.mh.ui.TerminalOutputLine
import com.jarves.mh.ui.TerminalSessionTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TerminalDomainState(
    val sessions: List<TerminalSessionTab> = listOf(TerminalSessionTab(id = "term-1", title = "Terminal 1")),
    val activeSessionId: String = "term-1",
    val activeLines: List<TerminalOutputLine> = emptyList(),
    val activeCwd: String = "/workspace",
    val isRunning: Boolean = false,
    val liveOutput: String = "",
)

/**
 * Domain controller for managing multi-tab terminal sessions and command state.
 */
class TerminalSessionController {

    private val _state = MutableStateFlow(TerminalDomainState())
    val state: StateFlow<TerminalDomainState> = _state.asStateFlow()

    fun addSession(title: String? = null, cwd: String = "/workspace"): String {
        var newId = ""
        _state.update { current ->
            val nextNum = current.sessions.size + 1
            val id = "term-$nextNum"
            newId = id
            val newTab = TerminalSessionTab(
                id = id,
                title = title ?: "Terminal $nextNum",
                cwd = cwd,
            )
            current.copy(
                sessions = current.sessions + newTab,
                activeSessionId = id,
                activeLines = emptyList(),
                activeCwd = cwd,
                isRunning = false,
                liveOutput = "",
            )
        }
        return newId
    }

    fun selectSession(sessionId: String) {
        _state.update { current ->
            if (current.activeSessionId == sessionId) return@update current
            // Save active tab state before switching
            val updatedSessions = current.sessions.map { tab ->
                if (tab.id == current.activeSessionId) {
                    tab.copy(
                        lines = current.activeLines,
                        cwd = current.activeCwd,
                        isRunning = current.isRunning,
                    )
                } else tab
            }
            val target = updatedSessions.firstOrNull { it.id == sessionId } ?: return@update current
            current.copy(
                sessions = updatedSessions,
                activeSessionId = target.id,
                activeLines = target.lines,
                activeCwd = target.cwd,
                isRunning = target.isRunning,
                liveOutput = "",
            )
        }
    }

    fun closeSession(sessionId: String) {
        _state.update { current ->
            if (current.sessions.size <= 1) {
                // Clear the single remaining session instead of removing
                val resetTab = TerminalSessionTab(id = "term-1", title = "Terminal 1")
                return@update current.copy(
                    sessions = listOf(resetTab),
                    activeSessionId = "term-1",
                    activeLines = emptyList(),
                    activeCwd = "/workspace",
                    isRunning = false,
                    liveOutput = "",
                )
            }
            val remaining = current.sessions.filter { it.id != sessionId }
            val newActive = if (current.activeSessionId == sessionId) {
                remaining.last()
            } else {
                remaining.firstOrNull { it.id == current.activeSessionId } ?: remaining.first()
            }
            current.copy(
                sessions = remaining,
                activeSessionId = newActive.id,
                activeLines = newActive.lines,
                activeCwd = newActive.cwd,
                isRunning = newActive.isRunning,
                liveOutput = "",
            )
        }
    }

    fun appendLine(line: TerminalOutputLine) {
        _state.update { current ->
            current.copy(activeLines = current.activeLines + line)
        }
    }

    fun updateCwd(cwd: String) {
        _state.update { it.copy(activeCwd = cwd) }
    }

    fun updateRunning(running: Boolean) {
        _state.update { it.copy(isRunning = running) }
    }

    fun updateLiveOutput(output: String) {
        _state.update { it.copy(liveOutput = output) }
    }

    fun clearActive() {
        _state.update { current ->
            current.copy(
                activeLines = emptyList(),
                liveOutput = "",
            )
        }
    }
}
