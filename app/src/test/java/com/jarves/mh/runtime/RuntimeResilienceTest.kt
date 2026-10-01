package com.jarves.mh.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeResilienceTest {
    @Test fun transientNetworkErrorsAreRetryable() {
        assertTrue(RuntimeFailureClassifier.isTransientNetworkFailure("Network error: connection reset by peer"))
        assertTrue(RuntimeFailureClassifier.isTransientNetworkFailure("HTTP 503 Service Unavailable"))
        assertTrue(RuntimeFailureClassifier.isTransientNetworkFailure("SocketTimeoutException: timeout"))
        assertTrue(RuntimeFailureClassifier.isTransientNetworkFailure("DNS resolution failed"))
    }

    @Test fun permanentProviderErrorsAreNotRetryable() {
        assertFalse(RuntimeFailureClassifier.isTransientNetworkFailure("HTTP 401 authentication failed"))
        assertFalse(RuntimeFailureClassifier.isTransientNetworkFailure("HTTP 429 quota exceeded"))
        assertFalse(RuntimeFailureClassifier.isTransientNetworkFailure("invalid model"))
        assertFalse(RuntimeFailureClassifier.isTransientNetworkFailure("Stopped by user"))
    }

    @Test fun retryBackoffIsBounded() {
        val policy = RuntimeRetryPolicy(maxAutomaticRetries = 5, initialBackoffMillis = 1000, maxBackoffMillis = 4000, jitterRatio = 0.0)
        assertEquals(1000L, policy.delayMillis(1, 0.5))
        assertEquals(2000L, policy.delayMillis(2, 0.5))
        assertEquals(4000L, policy.delayMillis(5, 0.5))
    }
}
