package com.ashwathai.tradelab.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.tradelab.ui.PortfolioStats
import com.ashwathai.tradelab.ui.common.formatCurrency
import com.ashwathai.tradelab.ui.theme.*
import java.util.Locale

@Composable
fun PortfolioShareCard(
    stats: PortfolioStats,
    modifier: Modifier = Modifier
) {
    val isProfit = stats.totalPnL >= 0
    val accentColor = if (isProfit) AccentGreen else AccentRose
    val bgGradient = if (isProfit) {
        Brush.verticalGradient(listOf(Color(0xFF0F1A0F), Color(0xFF000000)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF1A0F0F), Color(0xFF000000)))
    }

    Surface(
        modifier = modifier
            .width(360.dp)
            .height(480.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color.Black // Solid base
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .border(2.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header: App Brand
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BrandViolet)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "TRADE LAB",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Central Performance Metrics
                Text(
                    text = "MY PORTFOLIO PERFORMANCE",
                    color = TextSubtle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatCurrency(stats.totalValue, stats.currency),
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isProfit) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${if (isProfit) "+" else ""}${String.format(Locale.US, "%.2f", stats.totalPnLPct)}%",
                            color = accentColor,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "TOTAL RETURNS",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Footer / Call to Action
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Realistic Paper Trading Simulator",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "ashwathai.com/tradelab",
                    color = BrandViolet,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Neon Glow effect in corner
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 50.dp, y = 50.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(accentColor.copy(alpha = 0.15f), Color.Transparent)
                        )
                    )
            )
        }
    }
}
