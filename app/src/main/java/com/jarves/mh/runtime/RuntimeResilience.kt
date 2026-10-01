package com.jarves.mh.runtime

import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Locale

object RuntimeFailureClassifier {
    fun isTransientNetworkFailure(reason: String): Boolean {
        val value = reason.lowercase(Locale.ROOT)
        if (value.isBlank() || isPermanentProviderFailure(value) || value.contains("stopped by user")) return false
        return listOf(
            "network error", "network unavailable", "offline", "connection reset",
            "connection refused", "connection closed", "connection aborted",
            "connection timed out", "connect timed out", "socket timeout", "sockettimeoutexception",
            "socketexception", "unknownhostexception", "connectexception", "dns",
            "temporary failure", "temporarily unavailable", "service unavailable",
            "bad gateway", "gateway timeout", "http 408", "http 425", "http 502",
            "http 503", "http 504", "http 522", "http 524", "eof", "broken pipe",
            "stream closed", "stream reset", "unexpected end of", "transport error",
        ).any(value::contains)
    }

    private fun isPermanentProviderFailure(value: String): Boolean =
        listOf(
            "authentication", "unauthorized", "forbidden", "http 401", "http 403",
            "api key", "invalid api", "invalid model", "unknown model", "quota",
            "out of credits", "rate limit", "http 429", "user not found", "not signed in",
        ).any(value::contains)

    fun normalizeThrowable(error: Throwable): String = when (error) {
        is UnknownHostException -> "Network unavailable: DNS lookup failed."
        is SocketTimeoutException -> "Network error: connection timed out."
        is ConnectException -> "Network error: connection could not be established."
        is SocketException -> "Network error: " + error.message.orEmpty().ifBlank { "socket connection failed" } + "."
        else -> error.message.orEmpty().ifBlank { error::class.java.simpleName }
    }
}

data class RuntimeRetryPolicy(
    val maxAutomaticRetries: Int = 5,
    val initialBackoffMillis: Long = 1_500L,
    val maxBackoffMillis: Long = 15_000L,
    val jitterRatio: Double = 0.20,
) {
    init {
        require(maxAutomaticRetries >= 0)
        require(initialBackoffMillis > 0)
        require(maxBackoffMillis >= initialBackoffMillis)
        require(jitterRatio in 0.0..0.5)
    }

    fun delayMillis(attempt: Int, jitter: Double): Long {
        require(attempt >= 1)
        val exponential = initialBackoffMillis.toDouble() * (1L shl (attempt - 1).coerceAtMost(20))
        val bounded = exponential.coerceAtMost(maxBackoffMillis.toDouble())
        val factor = 1.0 + ((jitter * 2.0) - 1.0) * jitterRatio
        return (bounded * factor).toLong().coerceAtLeast(250L)
    }
}
