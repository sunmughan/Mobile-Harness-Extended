package com.jarves.mh.ecommerce

import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import java.util.concurrent.ThreadLocalRandom

/**
 * Firewall bypass, anti-ban guardrails, and resource blocking engine.
 * Speeds up page loads from ~5s down to <1.5s by dropping heavy media and trackers,
 * while maintaining authentic session cookies to bypass Akamai/Cloudflare bot shields.
 */
object PriceHuntWebInterceptor {

    private val BLOCKED_EXTENSIONS = setOf(
        ".jpg", ".jpeg", ".png", ".webp", ".gif", ".svg", ".ico",
        ".mp4", ".webm", ".avi", ".mov", ".m3u8",
        ".woff", ".woff2", ".ttf", ".eot", ".otf",
    )

    private val BLOCKED_TRACKER_HOSTS = listOf(
        "google-analytics.com",
        "googletagmanager.com",
        "doubleclick.net",
        "facebook.net",
        "clarity.ms",
        "criteo.com",
        "branch.io",
        "scorecardresearch.com",
        "appsflyer.com",
        "hotjar.com",
        "bat.bing.com",
    )

    /**
     * Checks if a web resource request should be blocked.
     * During interactive browsing, media (images, fonts, SVGs) are preserved by default
     * so modern single-page apps (Flipkart, Amazon) render and hydrate properly.
     * Heavy media is only dropped when blockMedia is explicitly requested (e.g. background headless scraping).
     */
    fun shouldBlockRequest(
        request: WebResourceRequest?,
        isSniperActive: Boolean,
        blockMedia: Boolean = false,
    ): Boolean {
        if (!isSniperActive || request == null) return false
        val uri = request.url ?: return false
        val path = uri.path?.lowercase().orEmpty()
        val host = uri.host?.lowercase().orEmpty()

        // Block media/image/font extensions only if blockMedia is enabled
        if (blockMedia) {
            for (ext in BLOCKED_EXTENSIONS) {
                if (path.endsWith(ext) || path.contains("$ext?")) {
                    return true
                }
            }
        }

        // Block ad and tracking networks
        for (tracker in BLOCKED_TRACKER_HOSTS) {
            if (host.contains(tracker)) {
                return true
            }
        }

        return false
    }

    /**
     * Creates an empty dummy response (200 OK with 0 bytes) to cancel blocked network calls instantly.
     */
    fun createEmptyResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            200,
            "OK",
            emptyMap(),
            ByteArrayInputStream(ByteArray(0)),
        )
    }

    /**
     * Anti-Ban Jitter Polling Algorithm:
     * Calculates the next poll delay = base_time (e.g. 25 min) + uniform random jitter (e.g. 5-15 min).
     * Prevents clockwork polling patterns from triggering server-side IP flags or captchas.
     */
    fun calculateNextJitterIntervalMs(
        baseMinutes: Long = 25L,
        minJitterMinutes: Long = 5L,
        maxJitterMinutes: Long = 15L,
    ): Long {
        val baseMs = baseMinutes * 60 * 1000L
        val minJitterMs = minJitterMinutes * 60 * 1000L
        val maxJitterMs = (maxJitterMinutes.coerceAtLeast(minJitterMinutes + 1)) * 60 * 1000L
        val jitterMs = ThreadLocalRandom.current().nextLong(minJitterMs, maxJitterMs)
        return baseMs + jitterMs
    }

    /**
     * Flushes the native Android CookieManager to disk to maintain persistent session state.
     */
    fun persistSessionCookies() {
        runCatching {
            CookieManager.getInstance().flush()
        }
    }
}
