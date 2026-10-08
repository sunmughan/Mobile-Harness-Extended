package com.jarves.mh.ecommerce

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * AI Deal Reality & Price Drop Verification Scorer.
 * Analyzes whether festive sale discounts (Big Billion Days / Great Indian Festival)
 * are mathematically authentic vs artificially inflated MRP baselines.
 */
object DealRealityScorer {

    private val indianLocale = Locale.Builder().setLanguage("en").setRegion("IN").build()

    fun formatInr(amount: Long): String {
        return try {
            NumberFormat.getIntegerInstance(indianLocale).format(amount)
        } catch (_: Exception) {
            NumberFormat.getIntegerInstance(Locale.US).format(amount)
        }
    }

    /**
     * Evaluates extracted product pricing against user target and calculates Deal Reality Score (1.0 to 10.0).
     */
    fun evaluateDeal(
        extracted: ExtractedPriceData,
        targetPrice: Long?,
    ): DealVerdict {
        val currentPrice = extracted.currentPrice
        val mrp = extracted.mrp
        val claimedText = extracted.claimedDiscountText
        val claimedPercent = PriceScraperEngine.parseDiscountPercent(claimedText)

        val savings = (mrp - currentPrice).coerceAtLeast(0L)
        val actualPercent = if (mrp > 0L && mrp > currentPrice) {
            ((savings.toDouble() / mrp.toDouble()) * 100.0).roundToInt()
        } else {
            0
        }

        // Detect inflated baseline MRP:
        // When a seller claims a high discount badge (e.g. 50%) but actual math is significantly less (e.g. 15%)
        val inflationDetected = claimedPercent != null && claimedPercent > (actualPercent + 10)

        // Calculate reality score (1.0 to 10.0)
        var rawScore = when {
            actualPercent >= 45 -> 9.0f
            actualPercent in 30..44 -> 8.2f
            actualPercent in 20..29 -> 7.4f
            actualPercent in 10..19 -> 6.0f
            actualPercent in 5..9 -> 4.5f
            actualPercent in 1..4 -> 3.0f
            else -> 1.5f
        }

        if (inflationDetected) {
            rawScore -= 3.5f
        } else if (claimedPercent != null && abs(claimedPercent - actualPercent) <= 3) {
            rawScore += 0.8f // Transparent pricing reward
        }

        val isTargetMet = targetPrice != null && targetPrice > 0L && currentPrice <= targetPrice
        if (isTargetMet) {
            rawScore += 0.5f
        }

        val finalScore = ((rawScore.coerceIn(1.0f, 10.0f) * 10).roundToInt()) / 10.0f
        val isGenuine = finalScore >= 6.5f && !inflationDetected

        val formattedCurrent = formatInr(currentPrice)
        val formattedMrp = formatInr(mrp)
        val formattedSavings = formatInr(savings)

        val verdictTitle: String
        val verdictSummary: String
        val alertTriggered: Boolean

        if (isTargetMet) {
            verdictTitle = "🎉 Target Price Reached! (₹$formattedCurrent)"
            verdictSummary = "Real Discount Detected: Price dropped to ₹$formattedCurrent (Target: ₹${formatInr(targetPrice!!)}). You save ₹$formattedSavings ($actualPercent% OFF)!"
            alertTriggered = true
        } else if (inflationDetected) {
            verdictTitle = "⚠️ Inflated MRP Warning (Score: $finalScore/10)"
            verdictSummary = "Seller claims ${claimedPercent}% OFF, but real discount is only $actualPercent% against ₹$formattedMrp MRP."
            alertTriggered = false
        } else if (isGenuine) {
            verdictTitle = "🔥 Real Deal Verified (Score: $finalScore/10)"
            verdictSummary = "Genuine festive discount: ₹$formattedCurrent down from ₹$formattedMrp. You save ₹$formattedSavings ($actualPercent% OFF)."
            alertTriggered = false
        } else {
            verdictTitle = "ℹ️ Moderate Festive Deal (Score: $finalScore/10)"
            verdictSummary = "Current price is ₹$formattedCurrent ($actualPercent% savings). Monitor for further price drops."
            alertTriggered = false
        }

        return DealVerdict(
            realityScore = finalScore,
            isGenuineDiscount = isGenuine,
            isTargetMet = isTargetMet,
            actualDiscountPercent = actualPercent,
            claimedDiscountPercent = claimedPercent,
            savingsAmount = savings,
            inflationDetected = inflationDetected,
            verdictTitle = verdictTitle,
            verdictSummary = verdictSummary,
            alertTriggered = alertTriggered,
        )
    }
}
