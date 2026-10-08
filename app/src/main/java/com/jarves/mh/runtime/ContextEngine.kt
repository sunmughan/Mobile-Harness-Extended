package com.jarves.mh.runtime

import java.io.File

data class ContextReference(
    val path: String,
    val language: String,
    val symbols: List<String>,
    val imports: List<String>,
    val reason: String,
)

/**
 * AI Context Engine 2.0.
 * Dynamically classifies user intent, ranks files by semantic and dependency relevance,
 * incorporates project memory (architecture rules, error patterns), and manages token budgets.
 */
class ContextEngine(
    private val projectIndex: ProjectIndex,
    val memoryStore: ProjectMemoryStore? = null,
) {
    companion object {
        private val CONVERSATIONAL_KEYWORDS = setOf(
            "yes", "no", "ok", "okay", "continue", "proceed", "go ahead",
            "approve", "approved", "done", "next", "thanks", "thank you",
            "start", "run", "build", "stop", "cancel", "help",
        )
    }

    private fun isConversationalOrBrief(request: String): Boolean {
        val trimmed = request.trim().lowercase()
        if (trimmed.length < 3) return true
        if (trimmed in CONVERSATIONAL_KEYWORDS) return true
        val words = trimmed.split("\\s+".toRegex())
        return words.size <= 2 && words.all { it in CONVERSATIONAL_KEYWORDS }
    }

    fun buildPromptContext(
        projectId: String,
        workspace: File,
        request: String,
        mentionedPaths: List<String> = emptyList(),
        maxReferences: Int = 12,
        maxTokenBudget: Int = 3000,
    ): String {
        if (isConversationalOrBrief(request) && mentionedPaths.isEmpty()) return ""

        val snapshot = projectIndex.ensureFresh(projectId, workspace)
        val intent = ContextRankingEngine.detectIntent(request)
        val memories = memoryStore?.getMemories(projectId).orEmpty()

        val ranked = ContextRankingEngine.rankFiles(
            files = snapshot.files,
            request = request,
            mentionedPaths = mentionedPaths,
            limit = maxReferences,
        )

        return ContextRankingEngine.compressContext(
            intent = intent,
            ranked = ranked,
            memories = memories,
            maxTokenBudget = maxTokenBudget,
        )
    }
}
