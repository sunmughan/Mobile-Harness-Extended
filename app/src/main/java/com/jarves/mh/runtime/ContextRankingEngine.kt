package com.jarves.mh.runtime

enum class UserIntent {
    BUG_FIX,
    FEATURE_IMPLEMENTATION,
    REFACTORING,
    BUILD_OR_TEST,
    EXPLANATION_QUESTION,
    GENERAL,
}

data class RankedContextFile(
    val file: IndexedFile,
    val score: Int,
    val matchedSymbols: List<String>,
    val reason: String,
)

/**
 * Intelligent context ranking, intent classification, and token budget management engine.
 */
object ContextRankingEngine {

    fun detectIntent(request: String): UserIntent {
        val lower = request.trim().lowercase()
        val errorKeywords = setOf("error", "crash", "exception", "fail", "failed", "bug", "fix", "broken", "trace", "nullpointer")
        val buildKeywords = setOf("build", "compile", "test", "verify", "gradle", "make", "unit test")
        val featureKeywords = setOf("add", "create", "implement", "new", "integrate", "support", "feature")
        val refactorKeywords = setOf("refactor", "cleanup", "clean up", "optimize", "restructure", "simplify")
        val questionKeywords = setOf("why", "how", "what is", "explain", "where", "tell me", "does")

        return when {
            errorKeywords.any { lower.contains(it) } || lower.contains("at com.") || lower.contains("caused by:") -> UserIntent.BUG_FIX
            buildKeywords.any { lower.contains(it) } -> UserIntent.BUILD_OR_TEST
            featureKeywords.any { lower.contains(it) } -> UserIntent.FEATURE_IMPLEMENTATION
            refactorKeywords.any { lower.contains(it) } -> UserIntent.REFACTORING
            questionKeywords.any { lower.contains(it) } -> UserIntent.EXPLANATION_QUESTION
            else -> UserIntent.GENERAL
        }
    }

    fun rankFiles(
        files: List<IndexedFile>,
        request: String,
        mentionedPaths: List<String> = emptyList(),
        dependencyGraph: DependencyGraph? = null,
        limit: Int = 15,
    ): List<RankedContextFile> {
        val queryTokens = request.lowercase()
            .split(Regex("[^a-zA-Z0-9_]"))
            .filter { it.length > 2 }
            .toSet()

        val mentionedSet = mentionedPaths.toSet()
        val directConnectedFiles = mutableSetOf<String>()

        if (dependencyGraph != null) {
            mentionedPaths.forEach { path ->
                dependencyGraph.imports[path]?.let { directConnectedFiles.addAll(it) }
                dependencyGraph.dependents[path]?.let { directConnectedFiles.addAll(it) }
            }
        }

        return files.map { file ->
            var score = 0
            val reasons = mutableListOf<String>()
            val matchedSymbols = mutableListOf<String>()

            // Tier 1: Explicit mentions
            if (file.path in mentionedSet) {
                score += 1000
                reasons.add("Explicit @mention")
            }

            // Tier 2: Direct dependency of mentioned file
            if (file.path in directConnectedFiles) {
                score += 500
                reasons.add("Direct dependency of mentioned file")
            }

            // Path & filename matching
            val fileName = file.path.substringAfterLast('/').lowercase()
            if (queryTokens.any { fileName.contains(it) }) {
                score += 120
                reasons.add("File name match")
            }

            // Symbol matching
            file.symbols.forEach { sym ->
                val symLower = sym.lowercase()
                if (queryTokens.any { symLower.contains(it) || it.contains(symLower) }) {
                    score += 45
                    matchedSymbols.add(sym)
                }
            }

            // Import matching
            val matchingImports = file.imports.count { imp ->
                queryTokens.any { imp.lowercase().contains(it) }
            }
            if (matchingImports > 0) {
                score += matchingImports * 15
            }

            RankedContextFile(
                file = file,
                score = score,
                matchedSymbols = matchedSymbols.distinct().take(6),
                reason = if (file.path in mentionedSet) "Explicit @mention" else if (reasons.isEmpty()) "Keyword relevance" else reasons.joinToString(", "),
            )
        }
            .filter { it.score > 0 }
            .sortedByDescending { it.score }
            .take(limit)
    }

    /**
     * Formats ranked context into a compressed, budget-controlled XML block.
     */
    fun compressContext(
        intent: UserIntent,
        ranked: List<RankedContextFile>,
        memories: List<ProjectMemoryItem> = emptyList(),
        maxTokenBudget: Int = 3000,
    ): String {
        if (ranked.isEmpty() && memories.isEmpty()) return ""

        val approxCharsPerToken = 4
        val maxChars = maxTokenBudget * approxCharsPerToken
        val sb = StringBuilder()

        sb.appendLine("<context_engine_v2 intent=\"${intent.name}\">")

        if (memories.isNotEmpty()) {
            sb.appendLine("  <project_memory>")
            memories.take(6).forEach { mem ->
                sb.appendLine("    - [${mem.category.name}] ${mem.key}: ${mem.content}")
            }
            sb.appendLine("  </project_memory>")
        }

        if (ranked.isNotEmpty()) {
            sb.appendLine("  <relevant_files count=\"${ranked.size}\">")
            var currentLength = sb.length

            for ((idx, item) in ranked.withIndex()) {
                val header = "- ${item.file.path} [${item.file.language}] — ${item.reason}"
                val line = buildString {
                    appendLine("    $header")
                    if (item.matchedSymbols.isNotEmpty()) {
                        appendLine("      symbols: ${item.matchedSymbols.joinToString(", ")}")
                    }
                    if (item.file.imports.isNotEmpty()) {
                        appendLine("      imports: ${item.file.imports.take(5).joinToString(", ")}")
                    }
                }.trimEnd()
                if (currentLength + line.length > maxChars) {
                    sb.appendLine("    <!-- Truncated ${ranked.size - idx} lower-priority files due to context budget -->")
                    break
                }
                sb.appendLine(line)
                currentLength += line.length + 1
            }
            sb.appendLine("  </relevant_files>")
        }

        sb.appendLine("</context_engine_v2>")
        return sb.toString().trim()
    }
}
