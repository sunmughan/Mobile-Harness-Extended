package com.jarves.mh.ui.components

import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarves.mh.ecommerce.BuyActionHandler
import com.jarves.mh.ecommerce.DealRealityScorer
import com.jarves.mh.ecommerce.ECommercePlatform
import com.jarves.mh.ecommerce.PriceDropEvent
import com.jarves.mh.ecommerce.PriceHuntController
import com.jarves.mh.ecommerce.PriceWatchlistItem
import com.jarves.mh.ecommerce.PriceWatchlistManager

@Composable
fun PriceHuntWidget(
    webView: WebView?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val state by PriceHuntController.state.collectAsState()
    var showWizardDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
        ) {
            // Header with Festive Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFF5722),
                        modifier = Modifier.size(32.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Big Billion Days & Festival Sniper",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Stealth CDP scraper & AI Deal Reality engine",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFF5722).copy(alpha = 0.15f),
                ) {
                    Text(
                        text = "LIVE HUNT",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF5722),
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Platform Selection Chips (Flipkart & Amazon)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = state.platform == ECommercePlatform.FLIPKART,
                    onClick = { PriceHuntController.selectPlatform(ECommercePlatform.FLIPKART) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2874F0)),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Flipkart", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2874F0).copy(alpha = 0.18f),
                        selectedLabelColor = Color(0xFF1565C0),
                    ),
                )

                FilterChip(
                    selected = state.platform == ECommercePlatform.AMAZON,
                    onClick = { PriceHuntController.selectPlatform(ECommercePlatform.AMAZON) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF9900)),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Amazon", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFF9900).copy(alpha = 0.18f),
                        selectedLabelColor = Color(0xFFE65100),
                    ),
                )

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = { showWizardDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722).copy(alpha = 0.15f)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp),
                ) {
                    Text("🎯 Deal Wizard", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5722), maxLines = 1)
                }
            }

            if (showWizardDialog) {
                DealFinderWizardDialog(
                    onDismiss = { showWizardDialog = false },
                    onSelectDeal = { deal ->
                        PriceHuntController.updateUrl(deal.productUrl)
                        PriceHuntController.updateTargetPrice(deal.currentPrice.toString())
                        showWizardDialog = false
                        PriceHuntController.executeScan(
                            context = context,
                            webView = webView,
                            onNavigate = onNavigate,
                        )
                    },
                )
            }

            Spacer(Modifier.height(10.dp))

            // Product Link Input
            OutlinedTextField(
                value = state.url,
                onValueChange = { PriceHuntController.updateUrl(it) },
                label = { Text("Product Link (Flipkart / Amazon)", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
                trailingIcon = {
                    IconButton(onClick = {
                        val clip = clipboardManager.getText()?.text.orEmpty().trim()
                        if (clip.isNotBlank()) {
                            PriceHuntController.updateUrl(clip)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste from clipboard",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                },
            )

            Spacer(Modifier.height(8.dp))

            // Target Price Input & Auto-track Switch Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = state.targetPriceInput,
                    onValueChange = { PriceHuntController.updateTargetPrice(it) },
                    label = { Text("Target Alert Price", fontSize = 12.sp) },
                    prefix = { Text("₹", fontWeight = FontWeight.Bold) },
                    placeholder = { Text("e.g. 49,999", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.height(56.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Column {
                            Text("Auto-Snipe", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Text("25±10m jitter", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                        Switch(
                            checked = state.isAutoTracking,
                            onCheckedChange = { PriceHuntController.toggleAutoTracking(it) },
                            modifier = Modifier.scale(0.85f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Snipe Action Button
            Button(
                onClick = {
                    PriceHuntController.executeScan(
                        context = context,
                        webView = webView,
                        onNavigate = onNavigate,
                    )
                },
                enabled = !state.isScanning && state.url.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32),
                ),
                shape = RoundedCornerShape(10.dp),
            ) {
                if (state.isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Stealth Sniping...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Snipe Price & Verify Deal", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Scanning Status / Progress Animation
            AnimatedVisibility(visible = state.isScanning) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = Color(0xFF2E7D32),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = state.scanStep ?: "Connecting stealth session...",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            // Error display
            state.errorMessage?.let { error ->
                Spacer(Modifier.height(8.dp))
                Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }

            // Deal Verdict Alert Popup Card
            state.lastVerdict?.let { verdict ->
                Spacer(Modifier.height(12.dp))
                DealVerdictCard(
                    verdict = verdict,
                    extracted = state.lastExtracted,
                    targetPrice = state.targetPrice,
                    onOpenTab = {
                        state.lastExtracted?.url?.let { onNavigate(it) }
                    },
                    onDismiss = { PriceHuntController.dismissVerdict() },
                )
            }

            // Festive Deal Watchlist (Active Multi-Product Tracking)
            val watchlistItems by PriceWatchlistManager.INSTANCE.items.collectAsState()
            if (watchlistItems.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                WatchlistTray(
                    items = watchlistItems,
                    onOpenApp = { item ->
                        BuyActionHandler.openOfficialApp(context, item.url, item.platform)
                    },
                    onOpenInBrowser = { item ->
                        onNavigate(item.url)
                    },
                    onRemove = { item ->
                        PriceWatchlistManager.INSTANCE.remove(item.id)
                    },
                    onTestAlert = { item ->
                        PriceWatchlistManager.INSTANCE.recordPriceCheck(
                            itemId = item.id,
                            newPrice = (item.currentPrice * 0.90).toLong().coerceAtLeast(1L),
                            context = context,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun DealVerdictCard(
    verdict: com.jarves.mh.ecommerce.DealVerdict,
    extracted: com.jarves.mh.ecommerce.ExtractedPriceData?,
    targetPrice: Long?,
    onOpenTab: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val isAlert = verdict.alertTriggered
    val isWarning = verdict.inflationDetected
    var isTracked by remember(extracted?.url) {
        mutableStateOf(
            extracted?.url?.let { url ->
                PriceWatchlistManager.INSTANCE.items.value.any { it.url.equals(url, ignoreCase = true) }
            } ?: false
        )
    }

    val containerColor = when {
        isAlert -> Color(0xFF1B5E20).copy(alpha = 0.12f)
        isWarning -> Color(0xFFB71C1C).copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    }

    val borderColor = when {
        isAlert -> Color(0xFF2E7D32)
        isWarning -> Color(0xFFD32F2F)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            // Header row with Reality Score badge and Dismiss button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isAlert) Color(0xFF2E7D32) else if (isWarning) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary,
                ) {
                    Text(
                        text = "AI REALITY SCORE: ${verdict.realityScore}/10 ⭐",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Product Title
            extracted?.title?.let { title ->
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                )
            }

            Spacer(Modifier.height(6.dp))

            // Pricing Row: Current Price vs MRP
            if (extracted != null && extracted.currentPrice > 0L) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "₹${DealRealityScorer.formatInr(extracted.currentPrice)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isAlert) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface,
                    )

                    if (extracted.mrp > extracted.currentPrice) {
                        Text(
                            text = "₹${DealRealityScorer.formatInr(extracted.mrp)}",
                            fontSize = 13.sp,
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF388E3C).copy(alpha = 0.15f),
                        ) {
                            Text(
                                text = "${verdict.actualDiscountPercent}% OFF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Verdict Summary Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isAlert) Color(0xFF2E7D32).copy(alpha = 0.15f)
                        else if (isWarning) Color(0xFFD32F2F).copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = when {
                        isAlert -> Icons.Default.CheckCircle
                        isWarning -> Icons.Default.Warning
                        else -> Icons.Default.Bolt
                    },
                    contentDescription = null,
                    tint = if (isAlert) Color(0xFF2E7D32) else if (isWarning) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = verdict.verdictTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isAlert) Color(0xFF1B5E20) else if (isWarning) Color(0xFFB71C1C) else MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = verdict.verdictSummary,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // 1-Tap Action: Official App Instant Checkout (Flipkart / Amazon)
            if (extracted != null && extracted.url.isNotBlank()) {
                val platformColor = Color(extracted.platform.primaryColorHex)
                Button(
                    onClick = {
                        BuyActionHandler.openOfficialApp(context, extracted.url, extracted.platform)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = platformColor),
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "⚡ Buy Now (${extracted.platform.displayName} App)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                Spacer(Modifier.height(6.dp))
            }

            // Dual Action: Buy In-App (Chromium) & Track Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onOpenTab,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("🛒 Buy In-App", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                if (extracted != null && extracted.url.isNotBlank()) {
                    Button(
                        onClick = {
                            PriceWatchlistManager.INSTANCE.addOrUpdate(
                                url = extracted.url,
                                title = extracted.title,
                                price = extracted.currentPrice,
                                mrp = extracted.mrp,
                                targetPrice = targetPrice,
                                platform = extracted.platform,
                            )
                            isTracked = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTracked) Color(0xFF2E7D32) else MaterialTheme.colorScheme.secondary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isTracked) "✓ Tracking" else "🔔 Track Price",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchlistTray(
    items: List<PriceWatchlistItem>,
    onOpenApp: (PriceWatchlistItem) -> Unit,
    onOpenInBrowser: (PriceWatchlistItem) -> Unit,
    onRemove: (PriceWatchlistItem) -> Unit,
    onTestAlert: (PriceWatchlistItem) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Festive Watchlist (${items.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                ) {
                    Text(
                        text = "Jitter 25±10m anti-ban",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            items.forEach { item ->
                WatchlistItemRow(
                    item = item,
                    onOpenApp = { onOpenApp(item) },
                    onOpenInBrowser = { onOpenInBrowser(item) },
                    onRemove = { onRemove(item) },
                    onTestAlert = { onTestAlert(item) },
                )
            }
        }
    }
}

@Composable
private fun WatchlistItemRow(
    item: PriceWatchlistItem,
    onOpenApp: () -> Unit,
    onOpenInBrowser: () -> Unit,
    onRemove: () -> Unit,
    onTestAlert: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(item.platform.primaryColorHex),
                ) {
                    Text(
                        text = item.platform.displayName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (item.hasPriceDropped) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                        ) {
                            Text(
                                text = "📉 -₹${item.totalSavingsFromInitial}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            )
                        }
                    }

                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(22.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remove",
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Text(
                text = item.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "₹${DealRealityScorer.formatInr(item.currentPrice)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = if (item.hasPriceDropped) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface,
                    )
                    if (item.targetPrice != null) {
                        Text(
                            text = "Target: ₹${DealRealityScorer.formatInr(item.targetPrice)}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = onOpenApp,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(item.platform.primaryColorHex)),
                    ) {
                        Text("⚡ App", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onOpenInBrowser,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text("🌐 In-App", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }

                    IconButton(
                        onClick = onTestAlert,
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Test Alert Notification",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
