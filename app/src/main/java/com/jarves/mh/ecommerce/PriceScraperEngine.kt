package com.jarves.mh.ecommerce

import org.json.JSONObject

/**
 * High-performance, anti-bot resilient DOM extraction engine for Flipkart and Amazon.
 * Designed to execute inside authenticated mobile Chromium WebViews or over CDP on port 9222.
 */
object PriceScraperEngine {

    /**
     * Strips currency symbols (₹, Rs.), commas, whitespace, and decimal paise to return a clean Long.
     */
    fun parsePriceNumber(raw: String?): Long {
        if (raw.isNullOrBlank()) return 0L
        // Strip currency prefixes like "Rs.", "rs.", "Re.", "INR"
        val clean = raw.replace(Regex("(?i)\\b(rs|re|inr)\\.\\s*"), "")
            .replace(Regex("(?i)\\b(rs|re|inr)\\b"), "")
        // Strip trailing paise/cents if separated by decimal point at the end of number (e.g. .00, .50)
        val withoutPaise = clean.replace(Regex("\\.\\d{1,2}$"), "")
        val digits = withoutPaise.replace(Regex("[^0-9]"), "")
        return digits.toLongOrNull() ?: 0L
    }

    /**
     * Extracts discount percentage from badges like "25% off", "-34%", etc.
     */
    fun parseDiscountPercent(raw: String?): Int? {
        if (raw.isNullOrBlank()) return null
        val match = Regex("(\\d+)\\s*%").find(raw) ?: Regex("(\\d+)").find(raw)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    /**
     * Generates a self-executing JavaScript snippet tailored for Flipkart or Amazon India DOM structures.
     * Evaluates multiple modern and legacy selector fallbacks.
     */
    fun getExtractionScript(platform: ECommercePlatform): String {
        return when (platform) {
            ECommercePlatform.FLIPKART -> """
                (function() {
                    function queryFirst(selectors) {
                        for (var i = 0; i < selectors.length; i++) {
                            var el = document.querySelector(selectors[i]);
                            if (el && el.innerText && el.innerText.trim().length > 0) {
                                return el.innerText.trim();
                            }
                        }
                        return '';
                    }

                    var title = queryFirst([
                        'span.B_NuCI',
                        'h1.yhB1nd span',
                        'span[class*="VU-ZEz"]',
                        'h1'
                    ]);

                    var currentPrice = queryFirst([
                        'div._30jeq3._16J06d',
                        'div._30jeq3',
                        'div[class*="Nx9bqj"]',
                        'div[class*="_30jeq3"]'
                    ]);

                    var mrp = queryFirst([
                        'div._3I9_wc._2p6Xd0',
                        'div._3I9_wc',
                        'div[class*="yRaY8j"]'
                    ]);

                    var discount = queryFirst([
                        'div._3Ay6Sb._31Dcoz',
                        'div._3Ay6Sb',
                        'div[class*="UkUFwK"] span',
                        'div[class*="UkUFwK"]'
                    ]);

                    return JSON.stringify({
                        title: title || document.title || '',
                        currentPrice: currentPrice,
                        mrp: mrp,
                        discount: discount
                    });
                })();
            """.trimIndent()

            ECommercePlatform.AMAZON -> """
                (function() {
                    function queryFirst(selectors) {
                        for (var i = 0; i < selectors.length; i++) {
                            var el = document.querySelector(selectors[i]);
                            if (el && el.innerText && el.innerText.trim().length > 0) {
                                return el.innerText.trim();
                            }
                        }
                        return '';
                    }

                    var title = queryFirst([
                        '#productTitle',
                        '#title',
                        'h1#title'
                    ]);

                    var currentPrice = queryFirst([
                        '.apexPriceToPay span.a-offscreen',
                        'span.a-price-whole',
                        '#priceblock_ourprice',
                        '#priceblock_dealprice',
                        '#corePrice_desktop span.a-offscreen',
                        'span.a-price span.a-offscreen'
                    ]);

                    var mrp = queryFirst([
                        'span.a-text-price span.a-offscreen',
                        '#listPrice',
                        'span.basisPrice span.a-offscreen'
                    ]);

                    var discount = queryFirst([
                        'span.savingsPercentage',
                        'span[class*="savingsPercentage"]',
                        'span.reinventPriceSavingsPercentageMargin'
                    ]);

                    return JSON.stringify({
                        title: title || document.title || '',
                        currentPrice: currentPrice,
                        mrp: mrp,
                        discount: discount
                    });
                })();
            """.trimIndent()

            ECommercePlatform.GENERIC -> """
                (function() {
                    var title = document.title || '';
                    var priceEls = document.querySelectorAll('[class*="price"], [id*="price"]');
                    var foundPrice = '';
                    for (var i = 0; i < priceEls.length; i++) {
                        var text = priceEls[i].innerText;
                        if (text && /[0-9]/.test(text)) {
                            foundPrice = text;
                            break;
                        }
                    }
                    return JSON.stringify({
                        title: title,
                        currentPrice: foundPrice,
                        mrp: '',
                        discount: ''
                    });
                })();
            """.trimIndent()
        }
    }

    /**
     * Unwraps and parses the JSON response returned by WebView.evaluateJavascript or CDP Runtime.evaluate.
     */
    fun parseScrapedPayload(rawResult: String?, url: String, platform: ECommercePlatform): ExtractedPriceData? {
        if (rawResult.isNullOrBlank() || rawResult == "null") return null

        val jsonString = unwrapJsJsonString(rawResult)
        return try {
            val json = JSONObject(jsonString)
            val title = json.optString("title", "").ifBlank { "Product (${platform.displayName})" }
            val currentPriceStr = json.optString("currentPrice", "")
            val mrpStr = json.optString("mrp", "")
            val discountStr = json.optString("discount", "")

            var currentPrice = parsePriceNumber(currentPriceStr)
            var mrp = parsePriceNumber(mrpStr)

            // If MRP was not found but current price is valid, fallback MRP to current price
            if (mrp == 0L && currentPrice > 0L) {
                mrp = currentPrice
            } else if (currentPrice == 0L && mrp > 0L) {
                currentPrice = mrp
            }

            if (currentPrice == 0L && mrp == 0L) {
                return null
            }

            ExtractedPriceData(
                platform = platform,
                url = url,
                title = title,
                currentPrice = currentPrice,
                mrp = mrp,
                claimedDiscountText = discountStr.takeIf { it.isNotBlank() },
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * WebView evaluateJavascript returns JSON-stringified results (e.g. "\"{\\\"title\\\": ...}\"").
     * Unwraps outer quotes and decodes escape sequences.
     */
    private fun unwrapJsJsonString(input: String): String {
        var str = input.trim()
        if (str.startsWith("\"") && str.endsWith("\"") && str.length >= 2) {
            // Strip outer quotes and unescape
            str = str.substring(1, str.length - 1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
        }
        return str
    }
}
