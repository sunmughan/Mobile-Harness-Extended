package com.jarves.mh.ecommerce

/**
 * Supported e-commerce platforms for festive price hunt automation.
 */
enum class ECommercePlatform(
    val displayName: String,
    val domainSnippet: String,
    val primaryColorHex: Long,
    val badgeText: String,
) {
    FLIPKART(
        displayName = "Flipkart",
        domainSnippet = "flipkart.com",
        primaryColorHex = 0xFF2874F0, // Flipkart Blue
        badgeText = "Big Billion Days",
    ),
    AMAZON(
        displayName = "Amazon India",
        domainSnippet = "amazon.in",
        primaryColorHex = 0xFFFF9900, // Amazon Orange
        badgeText = "Great Indian Festival",
    ),
    GENERIC(
        displayName = "E-Commerce Store",
        domainSnippet = "",
        primaryColorHex = 0xFF4CAF50,
        badgeText = "Festive Deal",
    );

    companion object {
        fun detect(url: String): ECommercePlatform {
            val lower = url.lowercase().trim()
            return when {
                lower.contains("flipkart.com") || lower.contains("dl.flipkart.com") -> FLIPKART
                lower.contains("amazon.in") || lower.contains("amzn.to") || lower.contains("amzn.in") || lower.contains("amazon.com") -> AMAZON
                else -> GENERIC
            }
        }
    }
}

/**
 * Raw price data extracted directly from the e-commerce DOM.
 */
data class ExtractedPriceData(
    val platform: ECommercePlatform,
    val url: String,
    val title: String,
    val currentPrice: Long,
    val mrp: Long,
    val claimedDiscountText: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
)

/**
 * Verification result computed by the AI deal reality scorer.
 */
data class DealVerdict(
    val realityScore: Float, // 1.0 to 10.0
    val isGenuineDiscount: Boolean,
    val isTargetMet: Boolean,
    val actualDiscountPercent: Int,
    val claimedDiscountPercent: Int?,
    val savingsAmount: Long,
    val inflationDetected: Boolean,
    val verdictTitle: String,
    val verdictSummary: String,
    val alertTriggered: Boolean,
)

/**
 * State of the in-browser Price Hunt sniper session.
 */
data class PriceHuntState(
    val url: String = "",
    val platform: ECommercePlatform = ECommercePlatform.FLIPKART,
    val targetPriceInput: String = "",
    val targetPrice: Long? = null,
    val isScanning: Boolean = false,
    val scanStep: String? = null,
    val lastExtracted: ExtractedPriceData? = null,
    val lastVerdict: DealVerdict? = null,
    val errorMessage: String? = null,
    val isAutoTracking: Boolean = false,
    val nextCheckTimeMs: Long? = null,
    val scanHistory: List<ExtractedPriceData> = emptyList(),
)
