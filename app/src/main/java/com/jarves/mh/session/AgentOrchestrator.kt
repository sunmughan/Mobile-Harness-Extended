package com.jarves.mh.session

import com.jarves.mh.runtime.MemoryCategory
import com.jarves.mh.runtime.ProjectMemoryStore
import com.jarves.mh.workspace.WorkspaceCheckpointManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

/**
 * Autonomous Agent Orchestrator.
 * Drives the end-to-end engineering cycle:
 * UNDERSTAND -> PLAN -> IMPLEMENT -> BUILD -> TEST -> ANALYZE -> FIX -> RETEST -> REVIEW -> COMPLETED
 */
class AgentOrchestrator(
    private val checkpointManager: WorkspaceCheckpointManager? = null,
    private val memoryStore: ProjectMemoryStore? = null,
    val maxSelfHealingIterations: Int = 3,
) {
    val selfHealing = SelfHealingLoop(maxSelfHealingIterations)

    private val _snapshot = MutableStateFlow(
        OrchestratorSnapshot(
            state = EngineeringState.IDLE,
            currentIteration = 0,
            maxIterations = maxSelfHealingIterations,
            planSteps = emptyList(),
            currentStepIndex = 0,
            history = emptyList(),
        )
    )
    val snapshot: StateFlow<OrchestratorSnapshot> = _snapshot.asStateFlow()

    @Volatile private var baselineCheckpointId: String? = null
    @Volatile private var activeProjectId: String? = null

    fun startTask(
        projectId: String,
        workspaceDir: File,
        userGoal: String,
        plan: List<String> = emptyList(),
    ) {
        selfHealing.reset()
        activeProjectId = projectId
        // Save pre-task checkpoint if manager is configured
        val cp = checkpointManager?.createCheckpoint(projectId, workspaceDir, "Pre-task baseline: $userGoal")
        baselineCheckpointId = cp?.id

        val initialEvent = EngineeringStepEvent(
            state = EngineeringState.ANALYZING,
            stepTitle = "Analyzing requirements",
            details = userGoal,
            iteration = 1,
        )

        _snapshot.value = OrchestratorSnapshot(
            state = EngineeringState.ANALYZING,
            currentIteration = 1,
            maxIterations = maxSelfHealingIterations,
            planSteps = plan,
            currentStepIndex = 0,
            history = listOf(initialEvent),
        )
    }

    fun transition(state: EngineeringState, stepTitle: String, details: String? = null) {
        val event = EngineeringStepEvent(
            state = state,
            stepTitle = stepTitle,
            details = details,
            iteration = selfHealing.currentIteration + 1,
        )
        _snapshot.update { cur ->
            cur.copy(
                state = state,
                history = cur.history + event,
            )
        }
    }

    suspend fun executeVerificationLoop(
        projectId: String,
        buildAndTest: suspend () -> Pair<Boolean, String>,
        applyAutoFix: suspend (ErrorDiagnosis) -> Boolean,
    ): Boolean {
        transition(EngineeringState.BUILDING, "Building project")
        var (success, output) = buildAndTest()

        if (success) {
            transition(EngineeringState.TESTING, "Running tests")
            transition(EngineeringState.REVIEWING, "Verification successful")
            transition(EngineeringState.COMPLETED, "Task completed successfully")
            return true
        }

        while (!success && selfHealing.canRetry()) {
            transition(EngineeringState.ANALYZING_FAILURE, "Analyzing build/test error", output)
            val diagnosis = selfHealing.recordFailure(output)

            // Record error memory
            memoryStore?.record(
                projectId = projectId,
                category = MemoryCategory.ERROR_FIX,
                key = diagnosis.rootCause,
                content = diagnosis.suggestedFix,
            )

            transition(EngineeringState.FIXING, "Applying self-healing repair (${selfHealing.currentIteration}/$maxSelfHealingIterations)", diagnosis.suggestedFix)
            val fixApplied = applyAutoFix(diagnosis)
            if (!fixApplied) {
                transition(EngineeringState.FAILED, "Automatic fix could not be applied")
                return false
            }

            transition(EngineeringState.RETESTING, "Retesting after fix")
            val retestResult = buildAndTest()
            success = retestResult.first
            output = retestResult.second

            if (success) {
                transition(EngineeringState.REVIEWING, "All checks passed after self-healing repair")
                transition(EngineeringState.COMPLETED, "Task completed and verified")
                return true
            }
        }

        transition(EngineeringState.FAILED, "Exceeded maximum self-healing retries ($maxSelfHealingIterations)", output)
        return false
    }

    fun rollbackToBaseline(workspaceDir: File): Boolean {
        transition(EngineeringState.ROLLED_BACK, "Rolling back to baseline checkpoint")
        val pId = activeProjectId ?: return false
        val cpId = baselineCheckpointId ?: return false
        return checkpointManager?.rollbackToCheckpoint(pId, cpId, workspaceDir) ?: false
    }
}
