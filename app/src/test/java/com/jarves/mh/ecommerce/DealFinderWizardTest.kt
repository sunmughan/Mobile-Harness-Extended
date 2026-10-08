package com.jarves.mh.ecommerce

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DealFinderWizardTest {

    @Test
    fun testCatalogHasItemsAcrossAllCategories() {
        val deals = DealCatalogRegistry.getAllDeals()
        assertTrue("Catalog should contain curated deals", deals.size >= 10)

        val categories = deals.map { it.category }.toSet()
        assertTrue(categories.contains(ShoppingCategory.MOBILES))
        assertTrue(categories.contains(ShoppingCategory.LAPTOPS))
        assertTrue(categories.contains(ShoppingCategory.AUDIO_TVS))
        assertTrue(categories.contains(ShoppingCategory.GAMING))
        assertTrue(categories.contains(ShoppingCategory.WEARABLES))
    }

    @Test
    fun testFilterByCategory() {
        val prefs = WizardPreferences(
            category = ShoppingCategory.MOBILES,
            compareBothPlatforms = true,
            budget = BudgetRange.ANY,
            discountPreference = DiscountPreference.ANY,
        )

        val results = DealCatalogRegistry.findDeals(prefs)
        assertFalse(results.isEmpty())
        assertTrue(results.all { it.category == ShoppingCategory.MOBILES })
    }

    @Test
    fun testFilterByBudgetRange() {
        val budgetPrefs = WizardPreferences(
            category = ShoppingCategory.MOBILES,
            compareBothPlatforms = true,
            budget = BudgetRange.BUDGET, // Under 15K
            discountPreference = DiscountPreference.ANY,
        )

        val results = DealCatalogRegistry.findDeals(budgetPrefs)
        assertFalse(results.isEmpty())
        assertTrue(results.all { it.currentPrice <= 15000L })
    }

    @Test
    fun testFilterByPlatform() {
        val flipkartPrefs = WizardPreferences(
            platform = ECommercePlatform.FLIPKART,
            compareBothPlatforms = false,
            category = ShoppingCategory.ALL,
            budget = BudgetRange.ANY,
            discountPreference = DiscountPreference.ANY,
        )

        val flipkartResults = DealCatalogRegistry.findDeals(flipkartPrefs)
        assertFalse(flipkartResults.isEmpty())
        assertTrue(flipkartResults.all { it.platform == ECommercePlatform.FLIPKART })

        val amazonPrefs = WizardPreferences(
            platform = ECommercePlatform.AMAZON,
            compareBothPlatforms = false,
            category = ShoppingCategory.ALL,
            budget = BudgetRange.ANY,
            discountPreference = DiscountPreference.ANY,
        )

        val amazonResults = DealCatalogRegistry.findDeals(amazonPrefs)
        assertFalse(amazonResults.isEmpty())
        assertTrue(amazonResults.all { it.platform == ECommercePlatform.AMAZON })
    }

    @Test
    fun testPriceWarCompareBothPlatforms() {
        val comparePrefs = WizardPreferences(
            compareBothPlatforms = true,
            category = ShoppingCategory.ALL,
            budget = BudgetRange.ANY,
            discountPreference = DiscountPreference.ANY,
        )

        val results = DealCatalogRegistry.findDeals(comparePrefs)
        val platforms = results.map { it.platform }.toSet()
        assertTrue("Should contain both platforms for price war", platforms.contains(ECommercePlatform.FLIPKART))
        assertTrue("Should contain both platforms for price war", platforms.contains(ECommercePlatform.AMAZON))
    }

    @Test
    fun testTrendingDeals() {
        val trending = DealCatalogRegistry.getTrendingDeals(3)
        assertEquals(3, trending.size)
        // Trending items should be sorted descending by dealRealityScore
        assertTrue(trending[0].dealRealityScore >= trending[1].dealRealityScore)
        assertTrue(trending[1].dealRealityScore >= trending[2].dealRealityScore)
    }

    @Test
    fun testGracefulFallbackWhenStrictFilterYieldsNoDirectHit() {
        // Query a very narrow filter (Gaming under 15K on Flipkart)
        val strictPrefs = WizardPreferences(
            platform = ECommercePlatform.FLIPKART,
            compareBothPlatforms = false,
            category = ShoppingCategory.GAMING,
            budget = BudgetRange.BUDGET, // Gaming consoles are typically > 25K
            discountPreference = DiscountPreference.HALF_PRICE,
        )

        val results = DealCatalogRegistry.findDeals(strictPrefs)
        assertNotNull(results)
        assertFalse("Should provide fallback deals instead of empty list", results.isEmpty())
    }
}
