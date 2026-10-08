package com.jarves.mh.ecommerce

/**
 * Registry of curated blockbuster festive deals for Big Billion Days & Great Indian Festival.
 * Serves live recommendations to the in-browser interactive questionnaire wizard.
 */
object DealCatalogRegistry {

    private val CATALOG = listOf(
        // Smartphones & Tablets
        CuratedDealItem(
            id = "iphone-15",
            title = "Apple iPhone 15 (128 GB, Black)",
            platform = ECommercePlatform.FLIPKART,
            category = ShoppingCategory.MOBILES,
            currentPrice = 48999L,
            originalMrp = 69900L,
            claimedDiscount = "30% off",
            dealRealityScore = 9.4f,
            productUrl = "https://www.flipkart.com/apple-iphone-15-black-128-gb/p/itm6ac6485515ae4",
            badge = "🔥 BBD Blockbuster",
            highlightSpec = "A16 Bionic · 48MP Main Camera · USB-C",
        ),
        CuratedDealItem(
            id = "iphone-15-plus",
            title = "Apple iPhone 15 Plus (128 GB, Blue)",
            platform = ECommercePlatform.FLIPKART,
            category = ShoppingCategory.MOBILES,
            currentPrice = 59999L,
            originalMrp = 79900L,
            claimedDiscount = "25% off",
            dealRealityScore = 9.1f,
            productUrl = "https://www.flipkart.com/apple-iphone-15-plus-blue-128-gb/p/itmc49cfa3fa34da",
            badge = "🔥 Mega Battery Deal",
            highlightSpec = "6.7\" Super Retina · All-Day 26h Battery",
        ),
        CuratedDealItem(
            id = "samsung-s23",
            title = "Samsung Galaxy S23 5G (8GB / 128GB)",
            platform = ECommercePlatform.FLIPKART,
            category = ShoppingCategory.MOBILES,
            currentPrice = 37999L,
            originalMrp = 74999L,
            claimedDiscount = "49% off",
            dealRealityScore = 9.5f,
            productUrl = "https://www.flipkart.com/samsung-galaxy-s23-5g-phantom-black-128-gb/p/itm2847c1704e6e0",
            badge = "⚡ 49% Real Drop",
            highlightSpec = "Snapdragon 8 Gen 2 · Dynamic AMOLED 2X",
        ),
        CuratedDealItem(
            id = "pixel-8",
            title = "Google Pixel 8 (Hazel, 128 GB)",
            platform = ECommercePlatform.FLIPKART,
            category = ShoppingCategory.MOBILES,
            currentPrice = 34999L,
            originalMrp = 75999L,
            claimedDiscount = "54% off",
            dealRealityScore = 9.6f,
            productUrl = "https://www.flipkart.com/google-pixel-8-hazel-128-gb/p/itm5a8aa52c1cb73",
            badge = "🌟 AI Camera King",
            highlightSpec = "Tensor G3 · Best Take · 7 Yrs OS Updates",
        ),
        CuratedDealItem(
            id = "oneplus-12r",
            title = "OnePlus 12R (Cool Blue, 8GB RAM, 128GB)",
            platform = ECommercePlatform.AMAZON,
            category = ShoppingCategory.MOBILES,
            currentPrice = 35999L,
            originalMrp = 39999L,
            claimedDiscount = "10% off",
            dealRealityScore = 8.4f,
            productUrl = "https://www.amazon.in/dp/B0CQPN49ST",
            badge = "⚡ Amazon GIF Special",
            highlightSpec = "Snapdragon 8 Gen 2 · 100W SUPERVOOC",
        ),
        CuratedDealItem(
            id = "redmi-note-13-pro",
            title = "Redmi Note 13 Pro 5G (Midnight Black, 128 GB)",
            platform = ECommercePlatform.FLIPKART,
            category = ShoppingCategory.MOBILES,
            currentPrice = 19999L,
            originalMrp = 28999L,
            claimedDiscount = "31% off",
            dealRealityScore = 8.8f,
            productUrl = "https://www.flipkart.com/redmi-note-13-pro-5g-midnight-black-128-gb/p/itm7f3747cbcf2c9",
            badge = "💎 Under 20K Champion",
            highlightSpec = "200MP OIS Camera · 1.5K 120Hz AMOLED",
        ),
        CuratedDealItem(
            id = "realme-p1",
            title = "Realme P1 5G (Phoenix Red, 128 GB)",
            platform = ECommercePlatform.FLIPKART,
            category = ShoppingCategory.MOBILES,
            currentPrice = 12999L,
            originalMrp = 20999L,
            claimedDiscount = "38% off",
            dealRealityScore = 8.7f,
            productUrl = "https://www.flipkart.com/realme-p1-5g-phoenix-red-128-gb/p/itmca449fe8423aa",
            badge = "🏷️ Best Budget 5G",
            highlightSpec = "Dimensity 7050 · 120Hz AMOLED Screen",
        ),

        // Laptops & Computing
        CuratedDealItem(
            id = "macbook-air-m2",
            title = "Apple MacBook Air M2 (13.6\", 8GB, 256GB SSD)",
            platform = ECommercePlatform.FLIPKART,
            category = ShoppingCategory.LAPTOPS,
            currentPrice = 66990L,
            originalMrp = 99900L,
            claimedDiscount = "33% off",
            dealRealityScore = 9.5f,
            productUrl = "https://www.flipkart.com/apple-2022-macbook-air-m2-8-gb-256-gb-ssd-mac-os-monterey-mly33hn-a/p/itm5a8505500e588",
            badge = "🔥 Historic Low Price",
            highlightSpec = "Apple M2 · Liquid Retina · 18h Battery",
        ),
        CuratedDealItem(
            id = "asus-tuf-f15",
            title = "ASUS TUF Gaming F15 (Core i5 11th Gen, RTX 2050)",
            platform = ECommercePlatform.AMAZON,
            category = ShoppingCategory.LAPTOPS,
            currentPrice = 47990L,
            originalMrp = 73990L,
            claimedDiscount = "35% off",
            dealRealityScore = 8.9f,
            productUrl = "https://www.amazon.in/dp/B0CR1HKQ3W",
            badge = "🎮 Gaming Value Deal",
            highlightSpec = "144Hz FHD · 16GB RAM · 512GB NVMe SSD",
        ),
        CuratedDealItem(
            id = "ipad-10th-gen",
            title = "Apple iPad (10th Gen) 10.9\" Wi-Fi 64GB",
            platform = ECommercePlatform.FLIPKART,
            category = ShoppingCategory.LAPTOPS,
            currentPrice = 28999L,
            originalMrp = 34900L,
            claimedDiscount = "17% off",
            dealRealityScore = 8.6f,
            productUrl = "https://www.flipkart.com/apple-ipad-10th-gen-64-gb-rom-10-9-inch-wi-fi-only-silver/p/itm3d2c8fb85501d",
            badge = "🍎 Sub-30K iPad",
            highlightSpec = "A14 Bionic · Liquid Retina · USB-C",
        ),

        // Smart TVs & Audio
        CuratedDealItem(
            id = "sony-bravia-55",
            title = "Sony Bravia 55\" 4K Ultra HD Smart LED Google TV",
            platform = ECommercePlatform.AMAZON,
            category = ShoppingCategory.AUDIO_TVS,
            currentPrice = 52990L,
            originalMrp = 99900L,
            claimedDiscount = "47% off",
            dealRealityScore = 9.3f,
            productUrl = "https://www.amazon.in/dp/B0C3CGYCGN",
            badge = "📺 Cinema Grade 4K",
            highlightSpec = "X1 4K Processor · Dolby Audio · Google TV",
        ),
        CuratedDealItem(
            id = "xiaomi-tv-43",
            title = "Xiaomi Smart TV X 43\" 4K Dolby Vision",
            platform = ECommercePlatform.FLIPKART,
            category = ShoppingCategory.AUDIO_TVS,
            currentPrice = 21999L,
            originalMrp = 32999L,
            claimedDiscount = "33% off",
            dealRealityScore = 8.9f,
            productUrl = "https://www.flipkart.com/mi-x-series-108-cm-43-inch-ultra-hd-4k-smart-google-tv/p/itm2ca8d2ca3df12",
            badge = "🎯 4K Value King",
            highlightSpec = "Bezel-less Metal · Dolby Vision · 30W Sound",
        ),
        CuratedDealItem(
            id = "sony-wh1000xm4",
            title = "Sony WH-1000XM4 Industry Leading ANC Headphones",
            platform = ECommercePlatform.AMAZON,
            category = ShoppingCategory.AUDIO_TVS,
            currentPrice = 19990L,
            originalMrp = 29990L,
            claimedDiscount = "33% off",
            dealRealityScore = 9.4f,
            productUrl = "https://www.amazon.in/dp/B0863TXGM3",
            badge = "🎧 #1 ANC Headphones",
            highlightSpec = "Dual Noise Sensor · 30h Battery · Hi-Res LDAC",
        ),
        CuratedDealItem(
            id = "airpods-pro-2",
            title = "Apple AirPods Pro (2nd Gen) with MagSafe Case (USB-C)",
            platform = ECommercePlatform.FLIPKART,
            category = ShoppingCategory.AUDIO_TVS,
            currentPrice = 18999L,
            originalMrp = 24900L,
            claimedDiscount = "24% off",
            dealRealityScore = 8.9f,
            productUrl = "https://www.flipkart.com/apple-airpods-pro-2nd-generation-magsafe-case-usb-c/p/itm2c92e1069df89",
            badge = "⭐ Pro Audio Drop",
            highlightSpec = "H2 Chip · 2x Stronger ANC · Adaptive Audio",
        ),

        // Gaming & Consoles
        CuratedDealItem(
            id = "ps5-slim",
            title = "Sony PlayStation 5 Slim Console (Disc Edition)",
            platform = ECommercePlatform.AMAZON,
            category = ShoppingCategory.GAMING,
            currentPrice = 49990L,
            originalMrp = 54990L,
            claimedDiscount = "9% off",
            dealRealityScore = 8.8f,
            productUrl = "https://www.amazon.in/dp/B0CZ7K2B2S",
            badge = "🎮 Next-Gen Gaming",
            highlightSpec = "1TB SSD · 4K 120Hz · Ultra HD Blu-ray",
        ),
        CuratedDealItem(
            id = "xbox-series-s",
            title = "Microsoft Xbox Series S 512GB All-Digital Console",
            platform = ECommercePlatform.FLIPKART,
            category = ShoppingCategory.GAMING,
            currentPrice = 28990L,
            originalMrp = 37990L,
            claimedDiscount = "24% off",
            dealRealityScore = 8.7f,
            productUrl = "https://www.flipkart.com/xbox-series-s/p/itm937d57c5a0b73",
            badge = "⚡ All-Digital Power",
            highlightSpec = "Xbox Velocity Architecture · 120 FPS Gaming",
        ),

        // Wearables
        CuratedDealItem(
            id = "galaxy-watch-4",
            title = "Samsung Galaxy Watch 4 Classic LTE (46mm)",
            platform = ECommercePlatform.FLIPKART,
            category = ShoppingCategory.WEARABLES,
            currentPrice = 9999L,
            originalMrp = 39999L,
            claimedDiscount = "75% off",
            dealRealityScore = 9.7f,
            productUrl = "https://www.flipkart.com/samsung-galaxy-watch-4-classic-lte-46mm/p/itm2847c1704e6e0",
            badge = "🔥 75% Clearance Deal",
            highlightSpec = "Wear OS · ECG & Blood Pressure · Rotating Bezel",
        ),
    )

    fun getAllDeals(): List<CuratedDealItem> = CATALOG

    fun getTrendingDeals(limit: Int = 5): List<CuratedDealItem> =
        CATALOG.sortedByDescending { it.dealRealityScore }.take(limit)

    /**
     * Filters catalog based on user responses in the Deal Finder questionnaire.
     */
    fun findDeals(prefs: WizardPreferences): List<CuratedDealItem> {
        return CATALOG.filter { item ->
            // Platform filter (match platform or allow all if compareBothPlatforms is selected)
            val matchPlatform = prefs.compareBothPlatforms || item.platform == prefs.platform

            // Category filter
            val matchCategory = prefs.category == ShoppingCategory.ALL || item.category == prefs.category

            // Budget filter
            val matchBudget = when (prefs.budget) {
                BudgetRange.ANY -> true
                else -> item.currentPrice in prefs.budget.minInr..prefs.budget.maxInr
            }

            // Discount filter
            val matchDiscount = when (prefs.discountPreference) {
                DiscountPreference.ANY -> true
                DiscountPreference.MODERATE -> {
                    val discount = PriceScraperEngine.parseDiscountPercent(item.claimedDiscount) ?: 0
                    discount >= 20
                }
                DiscountPreference.MAJOR -> {
                    val discount = PriceScraperEngine.parseDiscountPercent(item.claimedDiscount) ?: 0
                    discount >= 30
                }
                DiscountPreference.HALF_PRICE -> {
                    val discount = PriceScraperEngine.parseDiscountPercent(item.claimedDiscount) ?: 0
                    discount >= 45
                }
            }

            matchPlatform && matchCategory && matchBudget && matchDiscount
        }.ifEmpty {
            // If strict filtering produces no match, relax discount & platform to give closest relevant deals
            CATALOG.filter { item ->
                (prefs.category == ShoppingCategory.ALL || item.category == prefs.category) &&
                    (prefs.budget == BudgetRange.ANY || item.currentPrice in prefs.budget.minInr..prefs.budget.maxInr)
            }.ifEmpty {
                // Absolute fallback: return top trending
                getTrendingDeals(4)
            }
        }
    }
}
