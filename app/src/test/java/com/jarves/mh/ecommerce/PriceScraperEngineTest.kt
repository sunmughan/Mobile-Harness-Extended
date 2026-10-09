package com.jarves.mh.ecommerce

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceScraperEngineTest {

    @Test
    fun testPlatformDetection() {
        assertEquals(
            ECommercePlatform.FLIPKART,
            ECommercePlatform.detect("https://www.flipkart.com/apple-iphone-15-black-128-gb/p/itm6ac6485515ae4"),
        )
        assertEquals(
            ECommercePlatform.FLIPKART,
            ECommercePlatform.detect("https://dl.flipkart.com/s/abcdef"),
        )
        assertEquals(
            ECommercePlatform.AMAZON,
            ECommercePlatform.detect("https://www.amazon.in/Apple-iPhone-15-128-GB/dp/B0CHX1W1XY"),
        )
        assertEquals(
            ECommercePlatform.AMAZON,
            ECommercePlatform.detect("https://amzn.to/3xyz"),
        )
        assertEquals(
            ECommercePlatform.GENERIC,
            ECommercePlatform.detect("https://example.com/item"),
        )
    }

    @Test
    fun testParsePriceNumber() {
        assertEquals(51999L, PriceScraperEngine.parsePriceNumber("₹51,999"))
        assertEquals(149900L, PriceScraperEngine.parsePriceNumber("₹ 1,49,900"))
        assertEquals(4999L, PriceScraperEngine.parsePriceNumber("Rs. 4,999.00"))
        assertEquals(999L, PriceScraperEngine.parsePriceNumber("  999/-  "))
        assertEquals(12999L, PriceScraperEngine.parsePriceNumber("Starts at ₹12,999"))
        assertEquals(1499L, PriceScraperEngine.parsePriceNumber("From ₹1,499"))
        assertEquals(34990L, PriceScraperEngine.parsePriceNumber("MRP: ₹34,990"))
        assertEquals(12499L, PriceScraperEngine.parsePriceNumber("₹12,499 - ₹14,999"))
        assertEquals(24999L, PriceScraperEngine.parsePriceNumber("₹24,999 to ₹27,999"))
        assertEquals(59900L, PriceScraperEngine.parsePriceNumber("₹\u00A059,900"))
        assertEquals(1299L, PriceScraperEngine.parsePriceNumber("Rs 1,299"))
        assertEquals(0L, PriceScraperEngine.parsePriceNumber(null))
        assertEquals(0L, PriceScraperEngine.parsePriceNumber(""))
    }

    @Test
    fun testParseDiscountPercent() {
        assertEquals(25, PriceScraperEngine.parseDiscountPercent("25% off"))
        assertEquals(30, PriceScraperEngine.parseDiscountPercent("-30%"))
        assertEquals(50, PriceScraperEngine.parseDiscountPercent("50%"))
        assertEquals(40, PriceScraperEngine.parseDiscountPercent("Save 40%"))
        assertEquals(15, PriceScraperEngine.parseDiscountPercent("Flat 15% off"))
        assertEquals(60, PriceScraperEngine.parseDiscountPercent("Up to 60% off"))
        assertNull(PriceScraperEngine.parseDiscountPercent(null))
        assertNull(PriceScraperEngine.parseDiscountPercent("Special Offer"))
    }

    @Test
    fun testExtractionScriptsContainExpectedSelectors() {
        val flipkartScript = PriceScraperEngine.getExtractionScript(ECommercePlatform.FLIPKART)
        assertTrue(flipkartScript.contains("_30jeq3"))
        assertTrue(flipkartScript.contains("_3I9_wc"))
        assertTrue(flipkartScript.contains("_3Ay6Sb"))
        assertTrue(flipkartScript.contains("isSponsored"))
        assertTrue(flipkartScript.contains("dealBadge"))

        val amazonScript = PriceScraperEngine.getExtractionScript(ECommercePlatform.AMAZON)
        assertTrue(amazonScript.contains("productTitle"))
        assertTrue(amazonScript.contains("a-price-whole"))
        assertTrue(amazonScript.contains("savingsPercentage"))
        assertTrue(amazonScript.contains("isSponsored"))
        assertTrue(amazonScript.contains("dealBadge"))
    }

    @Test
    fun testParseScrapedPayload() {
        val rawJson = """{"title":"iPhone 15 128GB","currentPrice":"₹51,999","mrp":"₹69,900","discount":"25% off","dealBadge":"Deal of the Day"}"""
        val parsed = PriceScraperEngine.parseScrapedPayload(
            rawJson,
            "https://www.flipkart.com/test",
            ECommercePlatform.FLIPKART,
        )

        assertNotNull(parsed)
        assertEquals("iPhone 15 128GB", parsed?.title)
        assertEquals(51999L, parsed?.currentPrice)
        assertEquals(69900L, parsed?.mrp)
        assertEquals("25% off", parsed?.claimedDiscountText)
        assertEquals("Deal of the Day", parsed?.dealBadge)
    }

    @Test
    fun testSponsoredAdFiltering() {
        // Explicit isSponsored flag
        val sponsoredJson = """{"title":"Generic Cable","currentPrice":"₹499","mrp":"₹999","isSponsored":true}"""
        val parsedSponsored = PriceScraperEngine.parseScrapedPayload(
            sponsoredJson,
            "https://www.amazon.in/test",
            ECommercePlatform.AMAZON,
        )
        assertNull(parsedSponsored)

        // Title prefix [Sponsored]
        val prefixSponsoredJson = """{"title":"[Sponsored] Bluetooth Earbuds","currentPrice":"₹1,299","mrp":"₹2,999"}"""
        val parsedPrefix = PriceScraperEngine.parseScrapedPayload(
            prefixSponsoredJson,
            "https://www.flipkart.com/test",
            ECommercePlatform.FLIPKART,
        )
        assertNull(parsedPrefix)

        // Title prefix Sponsored:
        val prefixColonJson = """{"title":"Sponsored: Power Bank 20000mAh","currentPrice":"₹1,599","mrp":"₹3,499"}"""
        val parsedPrefixColon = PriceScraperEngine.parseScrapedPayload(
            prefixColonJson,
            "https://www.amazon.in/test",
            ECommercePlatform.AMAZON,
        )
        assertNull(parsedPrefixColon)
    }

    @Test
    fun testAntiBanJitterIntervalMath() {
        val baseMin = 20L
        val minJitter = 5L
        val maxJitter = 15L

        for (i in 0 until 50) {
            val intervalMs = PriceHuntWebInterceptor.calculateNextJitterIntervalMs(baseMin, minJitter, maxJitter)
            val minExpected = (baseMin + minJitter) * 60 * 1000L
            val maxExpected = (baseMin + maxJitter) * 60 * 1000L
            assertTrue(
                "Interval $intervalMs should be between $minExpected and $maxExpected",
                intervalMs in minExpected..maxExpected,
            )
        }
    }

    @Test
    fun testDealRealityScorerGenuineDrop() {
        val extracted = ExtractedPriceData(
            platform = ECommercePlatform.FLIPKART,
            url = "https://flipkart.com/test",
            title = "iPhone 15",
            currentPrice = 49999L,
            mrp = 69900L,
            claimedDiscountText = "28% off",
        )

        val verdict = DealRealityScorer.evaluateDeal(
            extracted = extracted,
            targetPrice = 52000L, // Target price met!
        )

        assertTrue(verdict.isGenuineDiscount)
        assertTrue(verdict.isTargetMet)
        assertTrue(verdict.alertTriggered)
        assertFalse(verdict.inflationDetected)
        assertTrue("Score should be high for genuine target hit", verdict.realityScore >= 7.0f)
        assertEquals(28, verdict.actualDiscountPercent)
        assertEquals(19901L, verdict.savingsAmount)
    }

    @Test
    fun testDealRealityScorerInflatedMrpDetection() {
        val extracted = ExtractedPriceData(
            platform = ECommercePlatform.AMAZON,
            url = "https://amazon.in/test",
            title = "Budget Phone",
            currentPrice = 14500L,
            mrp = 15000L,
            claimedDiscountText = "50% off", // Fake 50% badge when actual is ~3%
        )

        val verdict = DealRealityScorer.evaluateDeal(
            extracted = extracted,
            targetPrice = 12000L,
        )

        assertTrue("Should detect fake/inflated discount", verdict.inflationDetected)
        assertFalse("Should not be genuine discount", verdict.isGenuineDiscount)
        assertFalse("Target price not met", verdict.isTargetMet)
        assertFalse("Alert should not trigger", verdict.alertTriggered)
        assertTrue("Score should be penalised for fake claim", verdict.realityScore <= 5.0f)
    }

    @Test
    fun testPriceHuntControllerStateManagement() {
        PriceHuntController.updateUrl("https://www.amazon.in/dp/B0CHX1W1XY")
        assertEquals(ECommercePlatform.AMAZON, PriceHuntController.state.value.platform)

        PriceHuntController.updateTargetPrice("45000")
        assertEquals(45000L, PriceHuntController.state.value.targetPrice)

        PriceHuntController.toggleAutoTracking(true)
        assertTrue(PriceHuntController.state.value.isAutoTracking)
        assertNotNull(PriceHuntController.state.value.nextCheckTimeMs)

        PriceHuntController.toggleAutoTracking(false)
        assertFalse(PriceHuntController.state.value.isAutoTracking)
        assertNull(PriceHuntController.state.value.nextCheckTimeMs)
    }
}
