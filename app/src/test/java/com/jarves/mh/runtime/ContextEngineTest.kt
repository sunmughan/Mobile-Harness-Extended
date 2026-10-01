package com.jarves.mh.runtime

import java.nio.file.Files
import org.junit.Assert.assertTrue
import org.junit.Test

class ContextEngineTest {
    @Test
    fun explicitMentionsAreIncludedBeforeRelevanceMatches() {
        val temp = Files.createTempDirectory("mobile-harness-context").toFile()
        try {
            File(temp, "ui").mkdirs()
            File(temp, "ui/Main.kt").writeText("class Main")
            File(temp, "data").mkdirs()
            File(temp, "data/Repository.kt").writeText("class Repository")

            val engine = ContextEngine(ProjectIndex(temp))
            val context = engine.buildPromptContext(
                projectId = "project",
                workspace = temp,
                request = "repository",
                mentionedPaths = listOf("ui/Main.kt"),
            )

            val mentionPosition = context.indexOf("- ui/Main.kt [Kotlin] — Explicit @mention")
            val relevancePosition = context.indexOf("data/Repository.kt")
            assertTrue(mentionPosition >= 0)
            assertTrue(relevancePosition >= 0)
            assertTrue(mentionPosition < relevancePosition)
        } finally {
            temp.deleteRecursively()
        }
    }
}
