package com.jarves.mh.ecommerce

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class PriceWatchlistTest {

    private lateinit var manager: PriceWatchlistManager

    @Before
    fun setUp() {
        manager = PriceWatchlistManager.INSTANCE
        manager.clear()
    }

    @Test
    fun testAddToWatchlistAndRetrieve() {
        val flipkartUrl = "https://www.flipkart.com/apple-iphone-15-black-128-gb/p/itm6ac6485515ae4"
        val item = manager.addOrUpdate(
            url = flipkartUrl,
            title = "Apple iPhone 15 (Black, 128 GB)",
            price = 54999L,
            mrp = 69900L,
            targetPrice = 49999L,
        )

        assertEquals("Apple iPhone 15 (Black, 128 GB)", item.title)
        assertEquals(ECommercePlatform.FLIPKART, item.platform)
        assertEquals(54999L, item.currentPrice)
        assertEquals(54999L, item.lowestPrice)
        assertEquals(49999L, item.targetPrice)
        assertFalse("Target should not be met initially", item.isTargetMet)
        assertFalse("Price should not be dropped yet", item.hasPriceDropped)

        val items = manager.items.value
        assertEquals(1, items.size)
        assertEquals(item.id, items[0].id)
    }

    @Test
    fun testPriceDropDetection() {
        val item = manager.addOrUpdate(
            url = "https://www.amazon.in/Sony-WH-1000XM5-Wireless-Cancelling-Headphones/dp/B09XS7JWHH",
            title = "Sony WH-1000XM5",
            price = 29990L,
            mrp = 34990L,
        )

        val dropEvent = manager.evaluatePriceDrop(item, 24990L)
        assertNotNull("Should produce drop event", dropEvent)
        dropEvent!!
        assertEquals(29990L, dropEvent.previousPrice)
        assertEquals(24990L, dropEvent.newPrice)
        assertEquals(5000L, dropEvent.dropAmount)
        assertEquals(16, dropEvent.dropPercent)
        assertFalse(dropEvent.isTargetReached)
        assertTrue(dropEvent.alertTitle.contains("Price Dropped"))
        assertTrue(dropEvent.alertDetail.contains("Now ₹24990"))
    }

    @Test
    fun testTargetPriceThresholdReached() {
        val item = manager.addOrUpdate(
            url = "https://www.flipkart.com/samsung-galaxy-s24-ultra-5g/p/itm123",
            title = "Samsung Galaxy S24 Ultra",
            price = 129999L,
            targetPrice = 99999L,
        )

        val dropEvent = manager.evaluatePriceDrop(item, 98999L)
        assertNotNull(dropEvent)
        dropEvent!!
        assertTrue("Target price should be reached", dropEvent.isTargetReached)
        assertTrue(dropEvent.alertTitle.contains("Target Price Reached"))
    }

    @Test
    fun testNoDropWhenPriceIncreasesOrEqual() {
        val item = manager.addOrUpdate(
            url = "https://www.amazon.in/dp/B0D12345",
            title = "Product X",
            price = 1000L,
        )

        val eventIncrease = manager.evaluatePriceDrop(item, 1200L)
        assertNull("Price increase should not trigger drop alert", eventIncrease)

        val eventEqual = manager.evaluatePriceDrop(item, 1000L)
        assertNull("Equal price should not trigger drop alert", eventEqual)
    }

    @Test
    fun testRecordPriceCheckUpdatesLowestAndHistory() {
        val item = manager.addOrUpdate(
            url = "https://www.flipkart.com/item-test",
            title = "Test Item",
            price = 10000L,
        )

        val event = manager.recordPriceCheck(item.id, 8500L)
        assertNotNull(event)

        val updated = manager.items.value.first { it.id == item.id }
        assertEquals(8500L, updated.currentPrice)
        assertEquals(8500L, updated.lowestPrice)
        assertEquals(1500L, updated.totalSavingsFromInitial)
        assertTrue(updated.hasPriceDropped)
        assertEquals(2, updated.priceHistory.size)
    }

    @Test
    fun testAntiBanJitterIntervalRange() {
        for (seed in 1..20) {
            val random = Random(seed)
            val jitterMs = manager.calculateJitterDelayMs(baseMinutes = 25L, jitterMinutes = 10L, randomSource = random)
            val minutes = jitterMs / (60L * 1000L)
            assertTrue("Jitter interval must be >= 15 min (got $minutes)", minutes >= 15L)
            assertTrue("Jitter interval must be <= 35 min (got $minutes)", minutes <= 35L)
        }
    }

    @Test
    fun testOfficialPackageResolution() {
        assertEquals("com.flipkart.android", BuyActionHandler.resolveOfficialPackage(ECommercePlatform.FLIPKART))
        assertEquals("in.amazon.mShop.android.shopping", BuyActionHandler.resolveOfficialPackage(ECommercePlatform.AMAZON))
        assertNull(BuyActionHandler.resolveOfficialPackage(ECommercePlatform.GENERIC))
    }

    @Test
    fun testRemoveAndClear() {
        val item1 = manager.addOrUpdate("https://flipkart.com/1", "Item 1", 100L)
        val item2 = manager.addOrUpdate("https://amazon.in/2", "Item 2", 200L)
        assertEquals(2, manager.items.value.size)

        manager.remove(item1.id)
        assertEquals(1, manager.items.value.size)
        assertEquals(item2.id, manager.items.value[0].id)

        manager.clear()
        assertTrue(manager.items.value.isEmpty())
    }
}
