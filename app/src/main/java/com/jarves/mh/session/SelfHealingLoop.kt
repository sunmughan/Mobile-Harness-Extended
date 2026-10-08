package com.jarves.mh.session

enum class ErrorType {
    COMPILATION_ERROR,
    TEST_FAILURE,
    SYNTAX_ERROR,
    MISSING_DEPENDENCY,
    RUNTIME_CRASH,
    UNKNOWN,
}

data class ErrorDiagnosis(
    val type: ErrorType,
    val affectedFiles: List<String>,
    val rootCause: String,
    val suggestedFix: String,
)

/**
 * Intelligent error analysis and self-healing loop for autonomous builds and tests.
 */
class SelfHealingLoop(val maxIterations: Int = 3) {

    var currentIteration: Int = 0
        private set

    private val failureHistory = mutableListOf<ErrorDiagnosis>()

    fun canRetry(): Boolean = currentIteration < maxIterations

    fun recordFailure(output: String): ErrorDiagnosis {
        currentIteration++
        val diagnosis = diagnose(output)
        failureHistory.add(diagnosis)
        return diagnosis
    }

    fun reset() {
        currentIteration = 0
        failureHistory.clear()
    }

    fun getFailures(): List<ErrorDiagnosis> = failureHistory.toList()

    fun diagnose(output: String): ErrorDiagnosis {
        val lines = output.lines()
        val fileRegex = Regex("""(?:([A-Za-z0-9_./\\-]+\.(?:kt|java|ts|js|py|go|rs)):(\d+))""")
        val affected = lines.flatMap { line ->
            fileRegex.findAll(line).map { it.groupValues[1] }
        }.distinct()

        val lower = output.lowercase()
        return when {
            lower.contains("unresolved reference") || lower.contains("cannot find symbol") -> {
                ErrorDiagnosis(
                    type = ErrorType.COMPILATION_ERROR,
                    affectedFiles = affected,
                    rootCause = "Missing import or unresolved symbol",
                    suggestedFix = "Add missing imports or verify symbol declaration",
                )
            }
            lower.contains("assertionerror") || lower.contains("tests failed") || lower.contains("test failed") -> {
                ErrorDiagnosis(
                    type = ErrorType.TEST_FAILURE,
                    affectedFiles = affected,
                    rootCause = "Test assertion mismatch",
                    suggestedFix = "Inspect test expectations and update implementation logic",
                )
            }
            lower.contains("syntax error") || lower.contains("expecting") -> {
                ErrorDiagnosis(
                    type = ErrorType.SYNTAX_ERROR,
                    affectedFiles = affected,
                    rootCause = "Syntax violation",
                    suggestedFix = "Fix brackets, keywords, or punctuation in affected files",
                )
            }
            lower.contains("could not resolve") || lower.contains("package does not exist") -> {
                ErrorDiagnosis(
                    type = ErrorType.MISSING_DEPENDENCY,
                    affectedFiles = affected,
                    rootCause = "Unresolved package dependency",
                    suggestedFix = "Check dependencies in build configuration or package manager",
                )
            }
            lower.contains("exception in thread") || lower.contains("caused by:") -> {
                ErrorDiagnosis(
                    type = ErrorType.RUNTIME_CRASH,
                    affectedFiles = affected,
                    rootCause = "Unhandled runtime exception",
                    suggestedFix = "Add null checks or handle exception safely",
                )
            }
            else -> {
                ErrorDiagnosis(
                    type = ErrorType.UNKNOWN,
                    affectedFiles = affected,
                    rootCause = "Build or test failure",
                    suggestedFix = "Review compiler/runner log output",
                )
            }
        }
    }
}
