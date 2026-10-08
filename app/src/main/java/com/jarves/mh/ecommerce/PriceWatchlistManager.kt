package com.jarves.mh.ecommerce

import android.content.Context
import com.jarves.mh.runtime.NotificationCoordinator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

/**
 * In-memory & background manager for tracking festive deals across Flipkart and Amazon.
 * Enforces anti-ban jitter intervals (25 ± 10 min) and sends instant high-priority notifications on price drops.
 */
class PriceWatchlistManager private constructor() {

    private val _items = MutableStateFlow<List<PriceWatchlistItem>>(emptyList())
    val items: StateFlow<List<PriceWatchlistItem>> = _items.asStateFlow()

    private val _alertsCount = MutableStateFlow(0)
    val alertsCount: StateFlow<Int> = _alertsCount.asStateFlow()

    /**
     * Adds a product to the watchlist, or updates it if already present.
     */
    fun addOrUpdate(
        url: String,
        title: String,
        price: Long,
        mrp: Long = price,
        targetPrice: Long? = null,
        platform: ECommercePlatform = ECommercePlatform.detect(url),
    ): PriceWatchlistItem {
        val normalizedUrl = normalizeUrl(url)
        val existing = _items.value.firstOrNull { normalizeUrl(it.url) == normalizedUrl }

        val item = if (existing != null) {
            existing.copy(
                title = title.ifBlank { existing.title },
                currentPrice = price,
                lowestPrice = minOf(existing.lowestPrice, price),
                mrp = if (mrp > 0L) mrp else existing.mrp,
                targetPrice = targetPrice ?: existing.targetPrice,
                lastCheckedTimeMs = System.currentTimeMillis(),
                priceHistory = existing.priceHistory + PriceHistoryEntry(price = price),
            )
        } else {
            PriceWatchlistItem(
                title = title,
                url = url,
                platform = platform,
                initialPrice = price,
                currentPrice = price,
                lowestPrice = price,
                mrp = mrp,
                targetPrice = targetPrice,
                lastCheckedTimeMs = System.currentTimeMillis(),
                priceHistory = listOf(PriceHistoryEntry(price = price)),
            )
        }

        _items.update { currentList ->
            val filtered = currentList.filterNot { normalizeUrl(it.url) == normalizedUrl }
            listOf(item) + filtered
        }

        return item
    }

    /**
     * Removes an item from tracking by ID.
     */
    fun remove(itemId: String) {
        _items.update { it.filterNot { item -> item.id == itemId } }
    }

    /**
     * Updates the user's desired target price threshold for an existing item.
     */
    fun updateTargetPrice(itemId: String, targetPrice: Long?) {
        _items.update { list ->
            list.map { item ->
                if (item.id == itemId) item.copy(targetPrice = targetPrice) else item
            }
        }
    }

    /**
     * Clears all items from the watchlist.
     */
    fun clear() {
        _items.value = emptyList()
    }

    /**
     * Evaluates a newly observed price against the existing item without modifying state.
     * Returns a PriceDropEvent if the price dropped or if the target price was achieved.
     */
    fun evaluatePriceDrop(item: PriceWatchlistItem, newPrice: Long): PriceDropEvent? {
        if (newPrice <= 0L) return null

        val isPriceDropped = newPrice < item.currentPrice
        val isTargetReached = item.targetPrice != null && newPrice <= item.targetPrice && item.currentPrice > item.targetPrice

        return if (isPriceDropped || isTargetReached) {
            PriceDropEvent(
                item = item,
                previousPrice = item.currentPrice,
                newPrice = newPrice,
                isTargetReached = item.targetPrice != null && newPrice <= item.targetPrice,
            )
        } else {
            null
        }
    }

    /**
     * Records a price check result for an item, updating its price history and lowest observed price.
     * If an alert condition is met, returns the PriceDropEvent and optionally posts a high-priority notification.
     */
    fun recordPriceCheck(
        itemId: String,
        newPrice: Long,
        newMrp: Long? = null,
        context: Context? = null,
    ): PriceDropEvent? {
        val currentItem = _items.value.firstOrNull { it.id == itemId } ?: return null
        val dropEvent = evaluatePriceDrop(currentItem, newPrice)

        val updatedItem = currentItem.copy(
            currentPrice = newPrice,
            lowestPrice = minOf(currentItem.lowestPrice, newPrice),
            mrp = newMrp ?: currentItem.mrp,
            lastCheckedTimeMs = System.currentTimeMillis(),
            priceHistory = currentItem.priceHistory + PriceHistoryEntry(price = newPrice),
        )

        _items.update { list ->
            list.map { if (it.id == itemId) updatedItem else it }
        }

        if (dropEvent != null) {
            _alertsCount.update { it + 1 }
            context?.let { ctx ->
                postPriceAlertNotification(ctx, dropEvent)
            }
        }

        return dropEvent
    }

    /**
     * Dispatches a high-priority notification with dual action buttons (Official App vs In-App Browser).
     */
    fun postPriceAlertNotification(context: Context, event: PriceDropEvent) {
        NotificationCoordinator.postPriceAlert(
            context = context,
            title = event.alertTitle,
            detail = event.alertDetail,
            productUrl = event.item.url,
        )
    }

    /**
     * Anti-ban jitter calculation: creates a randomized poll interval centered on [baseMinutes]
     * with random variance of ±[jitterMinutes] to evade bot detection and rate limits on Flipkart/Amazon.
     */
    fun calculateJitterDelayMs(
        baseMinutes: Long = 25L,
        jitterMinutes: Long = 10L,
        randomSource: Random = Random.Default,
    ): Long {
        val minMinutes = (baseMinutes - jitterMinutes).coerceAtLeast(1L)
        val maxMinutes = (baseMinutes + jitterMinutes).coerceAtLeast(minMinutes + 1)
        val selectedMinutes = randomSource.nextLong(minMinutes, maxMinutes + 1)
        return selectedMinutes * 60L * 1000L
    }

    private fun normalizeUrl(url: String): String {
        return url.trim().lowercase().split("?").firstOrNull().orEmpty()
    }

    companion object {
        val INSTANCE: PriceWatchlistManager by lazy { PriceWatchlistManager() }
    }
}
