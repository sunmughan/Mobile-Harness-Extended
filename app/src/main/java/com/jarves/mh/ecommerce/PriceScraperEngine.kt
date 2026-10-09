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
        // Strip non-breaking spaces and zero-width characters
        val normalized = raw.replace('\u00A0', ' ')
            .replace('\u200B', ' ')
            .replace('\u202F', ' ')
            .trim()
        // Strip currency prefixes like "Starts at", "From", "MRP", "Rs.", "rs.", "Re.", "INR"
        val clean = normalized
            .replace(Regex("(?i)\\b(starts?\\s+at|from|mrp)\\b"), "")
            .replace(Regex("(?i)\\b(rs|re|inr)\\.\\s*"), "")
            .replace(Regex("(?i)\\b(rs|re|inr)\\b"), "")
        // If range like "₹12,499 - ₹14,999" or "₹12,499 to ₹14,999", pick first price
        val firstPart = clean.split(Regex("(?i)\\s+(?:to|-|–|—)\\s+")).firstOrNull() ?: clean
        // Strip trailing paise/cents if separated by decimal point at the end of number (e.g. .00, .50)
        val withoutPaise = firstPart.replace(Regex("\\.\\d{1,2}$"), "")
        val digits = withoutPaise.replace(Regex("[^0-9]"), "")
        return digits.toLongOrNull() ?: 0L
    }

    /**
     * Extracts discount percentage from badges like "25% off", "-34%", "Save 40%", etc.
     */
    fun parseDiscountPercent(raw: String?): Int? {
        if (raw.isNullOrBlank()) return null
        val match = Regex("(\\d+)\\s*%").find(raw)
            ?: Regex("(?i)(?:save|off|flat|up\\s+to)\\s*(\\d+)").find(raw)
            ?: Regex("(\\d+)").find(raw)
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
                    function isSponsored(el) {
                        if (!el) return false;
                        var cur = el;
                        while (cur && cur !== document.body) {
                            if (cur.getAttribute && (cur.getAttribute('data-sponsored') === 'true' || cur.getAttribute('data-ad-id') || (cur.classList && (cur.classList.contains('sponsored') || cur.classList.contains('_2c2lc-'))))) return true;
                            if (cur.innerText && /^\s*sponsored\s*$/i.test(cur.innerText)) return true;
                            cur = cur.parentElement;
                        }
                        return false;
                    }

                    function queryFirst(selectors, skipSponsored) {
                        for (var i = 0; i < selectors.length; i++) {
                            var el = document.querySelector(selectors[i]);
                            if (el && (!skipSponsored || !isSponsored(el)) && el.innerText && el.innerText.trim().length > 0) {
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
                    ], true);

                    var currentPrice = queryFirst([
                        'div._30jeq3._16J06d',
                        'div._30jeq3',
                        'div[class*="Nx9bqj"]',
                        'div[class*="_30jeq3"]'
                    ], true);

                    var mrp = queryFirst([
                        'div._3I9_wc._2p6Xd0',
                        'div._3I9_wc',
                        'div[class*="yRaY8j"]'
                    ], true);

                    var discount = queryFirst([
                        'div._3Ay6Sb._31Dcoz',
                        'div._3Ay6Sb',
                        'div[class*="UkUFwK"] span',
                        'div[class*="UkUFwK"]'
                    ], true);

                    var dealBadge = queryFirst([
                        'div._3XINqE',
                        'div._2Tpdn3',
                        'div[class*="deal"]',
                        'div[class*="timer"]',
                        'span[class*="countdown"]'
                    ], false);

                    return JSON.stringify({
                        title: title || document.title || '',
                        currentPrice: currentPrice,
                        mrp: mrp,
                        discount: discount,
                        dealBadge: dealBadge
                    });
                })();
            """.trimIndent()

            ECommercePlatform.AMAZON -> """
                (function() {
                    function isSponsored(el) {
                        if (!el) return false;
                        var cur = el;
                        while (cur && cur !== document.body) {
                            if (cur.getAttribute && (cur.getAttribute('data-component-type') === 'sp-sponsored-result' || (cur.classList && (cur.classList.contains('s-sponsored-label-info-icon') || cur.classList.contains('puis-sponsored-label-text'))))) return true;
                            cur = cur.parentElement;
                        }
                        return false;
                    }

                    function queryFirst(selectors, skipSponsored) {
                        for (var i = 0; i < selectors.length; i++) {
                            var el = document.querySelector(selectors[i]);
                            if (el && (!skipSponsored || !isSponsored(el)) && el.innerText && el.innerText.trim().length > 0) {
                                return el.innerText.trim();
                            }
                        }
                        return '';
                    }

                    var title = queryFirst([
                        '#productTitle',
                        '#title',
                        'h1#title'
                    ], true);

                    var currentPrice = queryFirst([
                        '.apexPriceToPay span.a-offscreen',
                        'span.a-price-whole',
                        '#priceblock_ourprice',
                        '#priceblock_dealprice',
                        '#corePrice_desktop span.a-offscreen',
                        'span.a-price span.a-offscreen'
                    ], true);

                    var mrp = queryFirst([
                        'span.a-text-price span.a-offscreen',
                        '#listPrice',
                        'span.basisPrice span.a-offscreen'
                    ], true);

                    var discount = queryFirst([
                        'span.savingsPercentage',
                        'span[class*="savingsPercentage"]',
                        'span.reinventPriceSavingsPercentageMargin'
                    ], true);

                    var dealBadge = queryFirst([
                        '#dealBadgeSupportingText',
                        '#deal_badge',
                        'span[data-a-badge-color="deal"]',
                        'span.dealBadge',
                        '#dealBadge',
                        'span.badge-deal'
                    ], false);

                    return JSON.stringify({
                        title: title || document.title || '',
                        currentPrice: currentPrice,
                        mrp: mrp,
                        discount: discount,
                        dealBadge: dealBadge
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
                        discount: '',
                        dealBadge: ''
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
            if (json.optBoolean("isSponsored", false)) return null

            val title = json.optString("title", "").ifBlank { "Product (${platform.displayName})" }
            if (title.startsWith("[Sponsored]", ignoreCase = true) || title.startsWith("Sponsored:", ignoreCase = true)) {
                return null
            }

            val currentPriceStr = json.optString("currentPrice", "")
            val mrpStr = json.optString("mrp", "")
            val discountStr = json.optString("discount", "")
            val dealBadgeStr = json.optString("dealBadge", "")

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
                dealBadge = dealBadgeStr.takeIf { it.isNotBlank() },
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
