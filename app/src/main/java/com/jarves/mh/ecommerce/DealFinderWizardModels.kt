package com.jarves.mh.ecommerce

/**
 * Categories available in the Festive Deal Hunter questionnaire.
 */
enum class ShoppingCategory(
    val displayName: String,
    val emoji: String,
    val description: String,
) {
    MOBILES(
        displayName = "Smartphones & Tablets",
        emoji = "📱",
        description = "iPhones, Samsung Galaxy, Pixel, OnePlus, iPad",
    ),
    LAPTOPS(
        displayName = "Laptops & Computing",
        emoji = "💻",
        description = "MacBook, Gaming laptops, monitors, accessories",
    ),
    AUDIO_TVS(
        displayName = "Smart TVs & Audio",
        emoji = "📺",
        description = "4K OLED/QLED TVs, ANC headphones, soundbars",
    ),
    GAMING(
        displayName = "Gaming & Consoles",
        emoji = "🎮",
        description = "PS5, Xbox, Nintendo, Controllers, gaming gear",
    ),
    WEARABLES(
        displayName = "Wearables & Watches",
        emoji = "⌚",
        description = "Apple Watch, Galaxy Watch, fitness trackers",
    ),
    ALL(
        displayName = "All Categories",
        emoji = "🔥",
        description = "Top trending festive deals across all departments",
    );
}

/**
 * Budget range presets for festive sale shopping.
 */
enum class BudgetRange(
    val displayName: String,
    val minInr: Long,
    val maxInr: Long,
    val tag: String,
) {
    BUDGET("Under ₹15,000", 0L, 15000L, "Budget Pick"),
    MID_RANGE("₹15,000 – ₹35,000", 15000L, 35000L, "Value for Money"),
    PREMIUM("₹35,000 – ₹65,000", 35000L, 65000L, "Premium Performance"),
    FLAGSHIP("₹65,000+", 65000L, Long.MAX_VALUE, "Flagship Elite"),
    ANY("Any Budget", 0L, Long.MAX_VALUE, "All Prices");
}

/**
 * Minimum discount threshold filter.
 */
enum class DiscountPreference(
    val displayName: String,
    val minDiscountPercent: Int,
    val description: String,
) {
    ANY("Any Discount", 5, "Any valid festive drop"),
    MODERATE("20%+ Off", 20, "Solid festive price drop"),
    MAJOR("30%+ Off", 30, "Massive Big Billion Days / GIF drop"),
    HALF_PRICE("50%+ Super Deals", 50, "Historic clearance / flash deals");
}

/**
 * Aggregated user selections from the 4-round questionnaire.
 */
data class WizardPreferences(
    val platform: ECommercePlatform = ECommercePlatform.FLIPKART,
    val compareBothPlatforms: Boolean = false,
    val category: ShoppingCategory = ShoppingCategory.MOBILES,
    val budget: BudgetRange = BudgetRange.PREMIUM,
    val discountPreference: DiscountPreference = DiscountPreference.MODERATE,
    val customTargetPrice: Long? = null,
)

/**
 * Curated product item matching festive deals.
 */
data class CuratedDealItem(
    val id: String,
    val title: String,
    val platform: ECommercePlatform,
    val category: ShoppingCategory,
    val currentPrice: Long,
    val originalMrp: Long,
    val claimedDiscount: String,
    val dealRealityScore: Float,
    val productUrl: String,
    val badge: String,
    val highlightSpec: String,
)
