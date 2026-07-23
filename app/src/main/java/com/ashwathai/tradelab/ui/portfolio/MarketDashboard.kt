package com.ashwathai.tradelab.ui.portfolio

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.tradelab.data.StockPrice
import com.ashwathai.tradelab.ui.theme.*

@Composable
fun MarketDashboardWidget(
    topGainers: List<StockPrice>,
    topLosers: List<StockPrice>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Movers Marquee - Expanded full width, No Breadth, No Index Cards
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .padding(vertical = 6.dp, horizontal = 12.dp)
        ) {
            MoversMarqueeCompact(topGainers, topLosers)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MoversMarqueeCompact(gainers: List<StockPrice>, losers: List<StockPrice>) {
    val allMovers = remember(gainers, losers) { gainers + losers }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .basicMarquee(
                iterations = Int.MAX_VALUE,
                spacing = MarqueeSpacing(32.dp),
                velocity = 40.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("MOVERS:", color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        allMovers.forEach { m ->
            val isUp = m.dailyChangePct >= 0
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(m.symbol.take(4), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${String.format("%.1f", m.dailyChangePct)}%",
                    color = if (isUp) AccentGreen else AccentRose,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
