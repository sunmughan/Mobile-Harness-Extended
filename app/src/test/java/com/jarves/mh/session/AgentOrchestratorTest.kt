package com.jarves.mh.session

import com.jarves.mh.runtime.ProjectMemoryStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class AgentOrchestratorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var workspaceDir: File
    private lateinit var memoryDir: File
    private lateinit var orchestrator: AgentOrchestrator

    @Before
    fun setUp() {
        workspaceDir = tempFolder.newFolder("orchestrator_workspace")
        memoryDir = tempFolder.newFolder("orchestrator_memory")
        val memoryStore = ProjectMemoryStore(memoryDir)
        orchestrator = AgentOrchestrator(
            checkpointManager = null,
            memoryStore = memoryStore,
            maxSelfHealingIterations = 3,
        )
    }

    @Test
    fun `selfHealingLoop diagnoses compilation error and suggests fix`() {
        val loop = SelfHealingLoop(maxIterations = 2)
        val errorLog = """
            e: src/Main.kt:15:20 Unresolved reference 'calculateTotal'.
        """.trimIndent()

        val diag = loop.recordFailure(errorLog)
        assertEquals(ErrorType.COMPILATION_ERROR, diag.type)
        assertTrue(diag.affectedFiles.contains("src/Main.kt"))
        assertTrue(loop.canRetry())
    }

    @Test
    fun `executeVerificationLoop succeeds immediately if build passes`() = runBlocking {
        orchestrator.startTask("proj-1", workspaceDir, "Add feature")

        val result = orchestrator.executeVerificationLoop(
            projectId = "proj-1",
            buildAndTest = { Pair(true, "BUILD SUCCESSFUL") },
            applyAutoFix = { true },
        )

        assertTrue(result)
        assertEquals(EngineeringState.COMPLETED, orchestrator.snapshot.value.state)
    }

    @Test
    fun `executeVerificationLoop triggers self healing and recovers on retry`() = runBlocking {
        orchestrator.startTask("proj-1", workspaceDir, "Fix logic")

        var attempt = 0
        val result = orchestrator.executeVerificationLoop(
            projectId = "proj-1",
            buildAndTest = {
                attempt++
                if (attempt == 1) {
                    Pair(false, "e: src/Calc.kt:5 Unresolved reference 'add'")
                } else {
                    Pair(true, "BUILD SUCCESSFUL")
                }
            },
            applyAutoFix = { diagnosis ->
                assertEquals(ErrorType.COMPILATION_ERROR, diagnosis.type)
                true // Successfully applied fix
            },
        )

        assertTrue(result)
        assertEquals(EngineeringState.COMPLETED, orchestrator.snapshot.value.state)
        assertEquals(1, orchestrator.selfHealing.currentIteration)
    }

    @Test
    fun `executeVerificationLoop fails gracefully when exceeding max retries`() = runBlocking {
        orchestrator.startTask("proj-1", workspaceDir, "Complex refactor")

        val result = orchestrator.executeVerificationLoop(
            projectId = "proj-1",
            buildAndTest = { Pair(false, "Tests failed with assertion error") },
            applyAutoFix = { true },
        )

        assertFalse(result)
        assertEquals(EngineeringState.FAILED, orchestrator.snapshot.value.state)
        assertEquals(3, orchestrator.selfHealing.currentIteration)
    }
}
