package com.jarves.mh.runtime

import java.io.File

data class ContextReference(
    val path: String,
    val language: String,
    val symbols: List<String>,
    val imports: List<String>,
    val reason: String,
)

class ContextEngine(private val projectIndex: ProjectIndex) {
    fun buildPromptContext(
        projectId: String,
        workspace: File,
        request: String,
        mentionedPaths: List<String> = emptyList(),
        maxReferences: Int = 12,
    ): String {
        val snapshot = projectIndex.ensureFresh(projectId, workspace)
        val references = linkedMapOf<String, ContextReference>()

        mentionedPaths.forEach { path ->
            snapshot.files.firstOrNull { it.path == path }?.let { file ->
                references[path] = ContextReference(
                    path = file.path,
                    language = file.language,
                    symbols = file.symbols,
                    imports = file.imports,
                    reason = "Explicit @mention",
                )
            }
        }

        projectIndex.search(snapshot, request, limit = maxReferences * 2)
            .forEach { file ->
                if (references.size < maxReferences) {
                    references.putIfAbsent(
                        file.path,
                        ContextReference(
                            path = file.path,
                            language = file.language,
                            symbols = file.symbols,
                            imports = file.imports,
                            reason = "Relevance match",
                        ),
                    )
                }
            }

        if (references.isEmpty()) return ""

        return buildString {
            appendLine("<relevant_project_context>")
            appendLine("The following workspace files were selected from the local project index. Use them as context pointers and inspect the actual files before editing.")
            references.values.forEach { reference ->
                appendLine("- " + reference.path + " [" + reference.language + "] — " + reference.reason)
                if (reference.symbols.isNotEmpty()) {
                    appendLine("  symbols: " + reference.symbols.take(12).joinToString(", "))
                }
                if (reference.imports.isNotEmpty()) {
                    appendLine("  imports: " + reference.imports.take(10).joinToString(", "))
                }
            }
            appendLine("</relevant_project_context>")
        }
    }
}
