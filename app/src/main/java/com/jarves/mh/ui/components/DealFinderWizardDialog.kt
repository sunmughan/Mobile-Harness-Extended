package com.jarves.mh.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jarves.mh.ecommerce.BudgetRange
import com.jarves.mh.ecommerce.CuratedDealItem
import com.jarves.mh.ecommerce.DealCatalogRegistry
import com.jarves.mh.ecommerce.DealRealityScorer
import com.jarves.mh.ecommerce.DiscountPreference
import com.jarves.mh.ecommerce.ECommercePlatform
import com.jarves.mh.ecommerce.ShoppingCategory
import com.jarves.mh.ecommerce.WizardPreferences

/**
 * 4-Round Interactive Deal Finder Wizard Dialog.
 * Guides the user through Platform, Category, Budget, and Discount preferences
 * to match them with verified festive deals ready for stealth sniping.
 */
@Composable
fun DealFinderWizardDialog(
    onDismiss: () -> Unit,
    onSelectDeal: (CuratedDealItem) -> Unit,
) {
    var step by remember { mutableIntStateOf(1) } // 1 to 4
    var selectedPlatform by remember { mutableStateOf(ECommercePlatform.FLIPKART) }
    var compareBoth by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(ShoppingCategory.MOBILES) }
    var selectedBudget by remember { mutableStateOf(BudgetRange.PREMIUM) }
    var selectedDiscount by remember { mutableStateOf(DiscountPreference.MODERATE) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFF5722),
                            modifier = Modifier.size(28.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Festive Deal Assistant",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Round $step of 4 · Big Billion Days & GIF",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, "Close", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Progress Dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    for (i in 1..4) {
                        val active = i == step
                        val completed = i < step
                        Box(
                            modifier = Modifier
                                .size(if (active) 10.dp else 7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (active) Color(0xFFFF5722)
                                    else if (completed) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant,
                                ),
                        )
                        if (i < 4) {
                            Spacer(Modifier.width(8.dp))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Step Content
                AnimatedContent(
                    targetState = step,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "WizardStepTransition",
                ) { currentStep ->
                    when (currentStep) {
                        1 -> Step1PlatformSelection(
                            selectedPlatform = selectedPlatform,
                            compareBoth = compareBoth,
                            onSelect = { plat, both ->
                                selectedPlatform = plat
                                compareBoth = both
                            },
                        )
                        2 -> Step2CategorySelection(
                            selected = selectedCategory,
                            onSelect = { selectedCategory = it },
                        )
                        3 -> Step3BudgetSelection(
                            selectedBudget = selectedBudget,
                            selectedDiscount = selectedDiscount,
                            onSelectBudget = { selectedBudget = it },
                            onSelectDiscount = { selectedDiscount = it },
                        )
                        4 -> Step4DealResults(
                            prefs = WizardPreferences(
                                platform = selectedPlatform,
                                compareBothPlatforms = compareBoth,
                                category = selectedCategory,
                                budget = selectedBudget,
                                discountPreference = selectedDiscount,
                            ),
                            onSelectDeal = onSelectDeal,
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Navigation Buttons (Back / Next)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (step > 1) {
                        OutlinedButton(
                            onClick = { step-- },
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Back", fontSize = 12.sp)
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    if (step < 4) {
                        Button(
                            onClick = { step++ },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                        ) {
                            Text("Next Round", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Step1PlatformSelection(
    selectedPlatform: ECommercePlatform,
    compareBoth: Boolean,
    onSelect: (ECommercePlatform, Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Where do you want to hunt for discounts?",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
        Text(
            text = "Select your preferred sale battlefield or scan both simultaneously.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Flipkart Card
        PlatformChoiceCard(
            title = "Flipkart Big Billion Days",
            subtitle = "Electronics, iPhones, Pixel, Fashion & Appliances",
            color = Color(0xFF2874F0),
            isSelected = !compareBoth && selectedPlatform == ECommercePlatform.FLIPKART,
            onClick = { onSelect(ECommercePlatform.FLIPKART, false) },
        )

        // Amazon Card
        PlatformChoiceCard(
            title = "Amazon Great Indian Festival",
            subtitle = "Prime Early Access, Gaming gear, Laptops, TVs",
            color = Color(0xFFFF9900),
            isSelected = !compareBoth && selectedPlatform == ECommercePlatform.AMAZON,
            onClick = { onSelect(ECommercePlatform.AMAZON, false) },
        )

        // Compare Both Card
        PlatformChoiceCard(
            title = "⚔️ Compare Both (Price War Matrix)",
            subtitle = "Find which platform has the absolute cheapest price",
            color = Color(0xFF2E7D32),
            isSelected = compareBoth,
            onClick = { onSelect(ECommercePlatform.FLIPKART, true) },
        )
    }
}

@Composable
private fun PlatformChoiceCard(
    title: String,
    subtitle: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 0.5.dp,
            color = if (isSelected) color else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isSelected) color else MaterialTheme.colorScheme.onSurface)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isSelected) {
                Surface(shape = CircleShape, color = color, modifier = Modifier.size(20.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(13.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun Step2CategorySelection(
    selected: ShoppingCategory,
    onSelect: (ShoppingCategory) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("What category are you interested in?", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("Our AI will match verified price drops in this section.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        ShoppingCategory.entries.forEach { cat ->
            val isSelected = selected == cat
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(cat) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 0.5.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(cat.emoji, fontSize = 20.sp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(cat.displayName, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        Text(cat.description, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (isSelected) {
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun Step3BudgetSelection(
    selectedBudget: BudgetRange,
    selectedDiscount: DiscountPreference,
    onSelectBudget: (BudgetRange) -> Unit,
    onSelectDiscount: (DiscountPreference) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("What is your budget & discount expectation?", fontWeight = FontWeight.Bold, fontSize = 14.sp)

        Text("BUDGET BRACKET", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            BudgetRange.entries.forEach { budget ->
                val isSelected = selectedBudget == budget
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectBudget(budget) },
                    label = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(budget.displayName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(budget.tag, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Text("MINIMUM DISCOUNT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            DiscountPreference.entries.forEach { disc ->
                FilterChip(
                    selected = selectedDiscount == disc,
                    onClick = { onSelectDiscount(disc) },
                    label = { Text(disc.displayName, fontSize = 10.5.sp) },
                )
            }
        }
    }
}

@Composable
private fun Step4DealResults(
    prefs: WizardPreferences,
    onSelectDeal: (CuratedDealItem) -> Unit,
) {
    val deals = remember(prefs) { DealCatalogRegistry.findDeals(prefs) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("🔥 Matched Deals (${deals.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Filtered by your budget & category", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF2E7D32).copy(alpha = 0.15f),
            ) {
                Text(
                    text = "AI VERIFIED",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(deals) { deal ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    ),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFF5722).copy(alpha = 0.15f),
                            ) {
                                Text(
                                    text = deal.badge,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF5722),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                            ) {
                                Text(
                                    text = "Score: ${deal.dealRealityScore}/10 ⭐",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        Text(deal.title, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(deal.highlightSpec, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "₹${DealRealityScorer.formatInr(deal.currentPrice)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF2E7D32),
                                )
                                Text(
                                    text = "₹${DealRealityScorer.formatInr(deal.originalMrp)}",
                                    fontSize = 11.sp,
                                    textDecoration = TextDecoration.LineThrough,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = deal.claimedDiscount,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100),
                                )
                            }

                            Button(
                                onClick = { onSelectDeal(deal) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp),
                            ) {
                                Text("Snipe This", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
