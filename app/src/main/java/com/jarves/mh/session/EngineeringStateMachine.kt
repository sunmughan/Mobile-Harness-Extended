package com.jarves.mh.session

enum class EngineeringState {
    IDLE,
    ANALYZING,
    PLANNING,
    IMPLEMENTING,
    BUILDING,
    TESTING,
    ANALYZING_FAILURE,
    FIXING,
    RETESTING,
    REVIEWING,
    WAITING_FOR_APPROVAL,
    COMPLETED,
    FAILED,
    ROLLED_BACK,
}

data class EngineeringStepEvent(
    val state: EngineeringState,
    val stepTitle: String,
    val details: String? = null,
    val iteration: Int = 1,
    val timestampMillis: Long = System.currentTimeMillis(),
)

data class OrchestratorSnapshot(
    val state: EngineeringState,
    val currentIteration: Int,
    val maxIterations: Int,
    val planSteps: List<String>,
    val currentStepIndex: Int,
    val history: List<EngineeringStepEvent>,
    val failureSummary: String? = null,
)
