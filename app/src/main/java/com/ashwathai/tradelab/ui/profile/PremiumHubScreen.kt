package com.ashwathai.tradelab.ui.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.tradelab.ui.PortfolioStats
import com.ashwathai.tradelab.ui.TradingViewModel
import com.ashwathai.tradelab.ui.common.formatCurrency
import com.ashwathai.tradelab.ui.common.formatCurrencyNoDecimals
import com.ashwathai.tradelab.ui.theme.*
import java.util.Locale

@Composable
fun PremiumHubScreen(
    viewModel: TradingViewModel,
    stats: PortfolioStats,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(scrollState)
    ) {
        // Custom Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "TRADELAB PRO HUB",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }

        // 1. ROI CALCULATOR: THE CONVERSION ENGINE
        RoiCalculatorWidget(stats = stats)

        Spacer(modifier = Modifier.height(24.dp))

        // 2. PRO BENEFITS GRID
        Text(
            text = "ELITE INVESTOR PRIVILEGES",
            color = AccentYellow,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProBenefitItem(
                icon = Icons.Default.Psychology,
                title = "Unlimited AI Strategy Audits",
                desc = "No more watching ads for credits. Consult Gemini 2.5 Flash anytime for deep portfolio risk analysis."
            )
            ProBenefitItem(
                icon = Icons.Default.Shield,
                title = "Zero-Brokerage Waiver",
                desc = "Stop leaking simulated cash to fees. 100% of your trade profits stay in your wallet."
            )
            ProBenefitItem(
                icon = Icons.Default.SsidChart,
                title = "Advanced Technical Suite",
                desc = "Permanent access to RSI, EMA, and SMA indicators on all stock charts."
            )
            ProBenefitItem(
                icon = Icons.Default.Bolt,
                title = "Priority Order Execution",
                desc = "GTT and Limit orders trigger with higher precision in the simulation engine."
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 3. CTA CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AccentYellow.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, AccentYellow.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Upgrade to Pro Today",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Join 500+ serious learners mastering the markets without real-world risk.",
                    color = TextSubtle,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { viewModel.simulatePremiumPurchase() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "15-DAY FREE TRIAL • ₹99/mo",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Cancel anytime in Play Store. No commitment required.",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun RoiCalculatorWidget(stats: PortfolioStats) {
    var monthlyInvestment by remember { mutableStateOf(10000.0) }
    
    // Math: AI Return = User's current Return + 5% (Simulation of "Alpha")
    val userReturnPct = stats.totalPnLPct
    val aiAlphaPct = userReturnPct + 5.5 // The "Alpha" gap
    
    val userReturnAmt = monthlyInvestment * (userReturnPct / 100.0)
    val aiReturnAmt = monthlyInvestment * (aiAlphaPct / 100.0)
    val gap = aiReturnAmt - userReturnAmt

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "ROI ALPHA CALCULATOR",
                color = BrandViolet,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "The Cost of Human Bias",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Simulated Monthly Allocation: ${formatCurrencyNoDecimals(monthlyInvestment, stats.currency)}",
                color = TextSubtle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Slider(
                value = monthlyInvestment.toFloat(),
                onValueChange = { monthlyInvestment = it.toDouble().coerceAtLeast(1000.0) },
                valueRange = 1000f..100000f,
                colors = SliderDefaults.colors(
                    thumbColor = BrandViolet,
                    activeTrackColor = BrandViolet,
                    inactiveTrackColor = Color.White.copy(alpha = 0.05f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Comparative Bars
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // User Return Bar
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Standard Return (${String.format(Locale.US, "%.1f", userReturnPct)}%)", color = TextMuted, fontSize = 11.sp)
                        Text(formatCurrency(userReturnAmt, stats.currency), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f))) {
                        Box(modifier = Modifier.fillMaxWidth(0.4f).fillMaxHeight().background(TextMuted))
                    }
                }

                // AI Pro Alpha Bar
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pro AI Alpha (${String.format(Locale.US, "%.1f", aiAlphaPct)}%)", color = AccentYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(formatCurrency(aiReturnAmt, stats.currency), color = AccentYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f))) {
                        Box(modifier = Modifier.fillMaxWidth(0.7f).fillMaxHeight().background(AccentYellow))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AccentYellow.copy(alpha = 0.1f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pro strategy could have potentially yielded ${formatCurrency(gap, stats.currency)} more profit by neutralizing emotional biases.",
                    color = AccentYellow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ProBenefitItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AccentYellow.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = desc, color = TextSubtle, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}
