package com.jarves.mh.ecommerce

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

/**
 * Dispatches 1-tap checkout actions either to native official apps (Flipkart / Amazon)
 * or to Mobile Harness's in-app Chromium session.
 */
object BuyActionHandler {

    const val FLIPKART_PACKAGE = "com.flipkart.android"
    const val AMAZON_IN_PACKAGE = "in.amazon.mShop.android.shopping"
    const val AMAZON_GLOBAL_PACKAGE = "com.amazon.mShop.android.shopping"

    /**
     * Resolves the target package for a platform.
     */
    fun resolveOfficialPackage(platform: ECommercePlatform): String? {
        return when (platform) {
            ECommercePlatform.FLIPKART -> FLIPKART_PACKAGE
            ECommercePlatform.AMAZON -> AMAZON_IN_PACKAGE
            ECommercePlatform.GENERIC -> null
        }
    }

    /**
     * Creates an Intent to open the product directly in the official shopping app.
     * If the official app is installed, targets that package directly.
     * If not installed, falls back to generic VIEW intent so any browser can handle it.
     */
    fun createOfficialAppIntent(
        context: Context,
        url: String,
        platform: ECommercePlatform = ECommercePlatform.detect(url),
    ): Intent {
        val uri = Uri.parse(url)
        val candidatePackage = resolveOfficialPackage(platform)

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        if (candidatePackage != null && isPackageInstalled(context, candidatePackage)) {
            intent.setPackage(candidatePackage)
        } else if (platform == ECommercePlatform.AMAZON && isPackageInstalled(context, AMAZON_GLOBAL_PACKAGE)) {
            intent.setPackage(AMAZON_GLOBAL_PACKAGE)
        }

        return intent
    }

    /**
     * Launches the official native app (or system browser fallback) for instant checkout.
     */
    fun openOfficialApp(
        context: Context,
        url: String,
        platform: ECommercePlatform = ECommercePlatform.detect(url),
    ): Boolean {
        return try {
            val intent = createOfficialAppIntent(context, url, platform)
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            try {
                // Secondary fallback if package-specific launch failed
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    /**
     * Checks whether an Android package is installed on the host device.
     */
    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: Throwable) {
            false
        }
    }
}
