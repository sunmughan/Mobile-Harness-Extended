package com.jarves.mh.runtime

import com.jarves.mh.model.AgentKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class SkillManagerTest {

    @Test
    fun ensureDefaultSkillsProvisionsBrowserAutomation() {
        val tempDir = Files.createTempDirectory("skill-manager-test").toFile()
        try {
            val manager = SkillManager(baseDir = tempDir)
            manager.ensureDefaultSkills()

            val skills = manager.installed()
            val browser = skills.firstOrNull { it.name == "browser-automation" }
            assertNotNull("browser-automation skill should be provisioned", browser)
            assertTrue(browser!!.description.contains("Chromium"))
            assertEquals("built-in", browser.source)

            val skillFile = File(browser.path, "SKILL.md")
            assertTrue(skillFile.isFile)
            val content = skillFile.readText()
            assertTrue(content.contains("name: browser-automation"))
            assertTrue(content.contains("pocket-browser start"))
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun syncToWorkspaceCopiesSkillsIntoAgentsDirectory() {
        val skillsDir = Files.createTempDirectory("skills-dir").toFile()
        val workspaceDir = Files.createTempDirectory("workspace-dir").toFile()
        try {
            val manager = SkillManager(baseDir = skillsDir)
            manager.ensureDefaultSkills()

            val synced = manager.syncToWorkspace(workspaceDir)
            assertTrue(synced.any { it.name == "browser-automation" })

            val syncedFile = File(workspaceDir, ".agents/skills/browser-automation/SKILL.md")
            assertTrue("SKILL.md should be copied to .agents/skills/browser-automation", syncedFile.isFile)
            assertTrue(syncedFile.readText().contains("pocket-browser"))
        } finally {
            skillsDir.deleteRecursively()
            workspaceDir.deleteRecursively()
        }
    }

    @Test
    fun buildPromptContextIncludesProvisionedSkills() {
        val tempDir = Files.createTempDirectory("skill-prompt-test").toFile()
        try {
            val manager = SkillManager(baseDir = tempDir)
            manager.ensureDefaultSkills()

            val promptContext = manager.buildPromptContext(AgentKind.ANTIGRAVITY)
            assertTrue(promptContext.contains("<mobile_harness_skills>"))
            assertTrue(promptContext.contains("browser-automation"))
            assertTrue(promptContext.contains("Selected agent: antigravity"))
            assertTrue(promptContext.contains("</mobile_harness_skills>"))
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun removeSkillDeletesFolderAndRegistryEntry() {
        val tempDir = Files.createTempDirectory("skill-remove-test").toFile()
        try {
            val manager = SkillManager(baseDir = tempDir)
            manager.ensureDefaultSkills()
            assertTrue(manager.installed().any { it.name == "browser-automation" })

            // Create a custom skill
            val customSkillDir = File(tempDir, "custom-lint").apply { mkdirs() }
            File(customSkillDir, "SKILL.md").writeText(
                """---
name: custom-lint
description: Custom linting skill
---
# Lint
""".trimIndent()
            )
            // Add to registry
            val registryFile = File(tempDir, "registry.json")
            val registry = org.json.JSONArray(registryFile.readText())
            registry.put(org.json.JSONObject().apply {
                put("name", "custom-lint")
                put("description", "Custom linting skill")
                put("source", "custom")
                put("installedAtMillis", System.currentTimeMillis())
                put("path", customSkillDir.absolutePath)
            })
            registryFile.writeText(registry.toString())

            assertTrue(manager.installed().any { it.name == "custom-lint" })

            val removed = manager.remove("custom-lint")
            assertTrue(removed)
            assertFalse(manager.installed().any { it.name == "custom-lint" })
            assertFalse(customSkillDir.exists())
            // Built-in skill remains intact
            assertTrue(manager.installed().any { it.name == "browser-automation" })
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
