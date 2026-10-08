package com.jarves.mh.ecommerce

import java.util.UUID

/**
 * Historical price point for a tracked product.
 */
data class PriceHistoryEntry(
    val price: Long,
    val timestamp: Long = System.currentTimeMillis(),
)

/**
 * Product tracked in the festive sale price hunter watchlist.
 */
data class PriceWatchlistItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val platform: ECommercePlatform = ECommercePlatform.detect(url),
    val initialPrice: Long,
    val currentPrice: Long,
    val lowestPrice: Long = minOf(initialPrice, currentPrice),
    val mrp: Long = currentPrice,
    val targetPrice: Long? = null,
    val alertOnAnyDrop: Boolean = true,
    val lastCheckedTimeMs: Long = System.currentTimeMillis(),
    val priceHistory: List<PriceHistoryEntry> = listOf(PriceHistoryEntry(price = currentPrice, timestamp = lastCheckedTimeMs)),
) {
    val isTargetMet: Boolean
        get() = targetPrice != null && currentPrice <= targetPrice

    val hasPriceDropped: Boolean
        get() = currentPrice < initialPrice

    val totalSavingsFromInitial: Long
        get() = (initialPrice - currentPrice).coerceAtLeast(0L)

    val actualDiscountPercent: Int
        get() = if (mrp > 0L && currentPrice < mrp) {
            (((mrp - currentPrice) * 100) / mrp).toInt()
        } else {
            0
        }

    val priceDropFromInitialPercent: Int
        get() = if (initialPrice > 0L && currentPrice < initialPrice) {
            (((initialPrice - currentPrice) * 100) / initialPrice).toInt()
        } else {
            0
        }
}

/**
 * Event generated when a product price drops or reaches the target threshold.
 */
data class PriceDropEvent(
    val item: PriceWatchlistItem,
    val previousPrice: Long,
    val newPrice: Long,
    val dropAmount: Long = (previousPrice - newPrice).coerceAtLeast(0L),
    val dropPercent: Int = if (previousPrice > 0L) (((previousPrice - newPrice) * 100) / previousPrice).toInt() else 0,
    val isTargetReached: Boolean = item.targetPrice != null && newPrice <= item.targetPrice,
    val timestamp: Long = System.currentTimeMillis(),
) {
    val alertTitle: String
        get() = when {
            isTargetReached -> "🎯 Target Price Reached: ${item.title.take(32)}!"
            dropPercent >= 20 -> "🔥 FLASH SALE DROP (-$dropPercent%): ${item.title.take(32)}"
            else -> "📉 Price Dropped (-₹$dropAmount): ${item.title.take(32)}"
        }

    val alertDetail: String
        get() = "Now ₹$newPrice (was ₹$previousPrice). Target: ${item.targetPrice?.let { "₹$it" } ?: "Any drop"}. Tap to buy!"
}

/**
 * High level state of the active festive watchlist.
 */
data class WatchlistState(
    val items: List<PriceWatchlistItem> = emptyList(),
    val isAutoTrackingActive: Boolean = false,
    val lastCheckTimestamp: Long = 0L,
    val totalAlertsTriggered: Int = 0,
)
