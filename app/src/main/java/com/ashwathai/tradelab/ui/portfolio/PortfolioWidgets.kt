package com.ashwathai.tradelab.ui.portfolio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.tradelab.ui.PortfolioStats
import com.ashwathai.tradelab.ui.common.formatCurrencyNoDecimals
import com.ashwathai.tradelab.ui.theme.*

@Composable
fun PortfolioCard(stats: PortfolioStats, onSimulateTick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("portfolio_card"),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Total Portfolio Value",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formatCurrencyNoDecimals(stats.totalValue, stats.currency),
                            color = Color.White,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-1).sp
                        )
                    }
                }

                if (stats.isArcadeMode) {
                    IconButton(
                        onClick = onSimulateTick,
                        modifier = Modifier
                            .testTag("simulate_tick_button")
                            .size(40.dp)
                            .background(AccentYellow.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, AccentYellow.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Simulate",
                            tint = AccentYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Available Cash",
                        color = TextSubtle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatCurrencyNoDecimals(stats.cash, stats.currency),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                val returnsPct = stats.totalPnLPct
                val badgeBg = if (returnsPct >= 0) AccentGreenDark else AccentRoseDark
                val badgeText = if (returnsPct >= 0) AccentGreen else AccentRose
                val sign = if (returnsPct >= 0) "+" else ""

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).background(badgeText, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$sign${String.format("%.2f", returnsPct)}%",
                        color = badgeText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, valueColor: Color, percent: String?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label.uppercase(),
                color = TextSubtle,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = valueColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            if (percent != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = percent,
                    color = valueColor.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
