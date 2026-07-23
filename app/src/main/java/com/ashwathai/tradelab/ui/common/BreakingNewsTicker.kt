package com.ashwathai.tradelab.ui.common

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.tradelab.data.MarketNews
import com.ashwathai.tradelab.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun BreakingNewsTicker(
    latestNews: List<MarketNews>,
    modifier: Modifier = Modifier
) {
    if (latestNews.isEmpty()) return

    // Ticker Logic: Cycle through news every 8 seconds
    var currentIndex by remember { mutableStateOf(0) }
    val currentNews = latestNews[currentIndex % latestNews.size]

    LaunchedEffect(latestNews) {
        while (true) {
            delay(8000)
            currentIndex++
        }
    }

    val barColor = when (currentNews.sentiment) {
        "BULLISH" -> Color(0xFF062319) // Deep Green
        "BEARISH" -> Color(0xFF280C11) // Deep Red
        else -> Color(0xFF0F0F1A) // Dark Navy
    }

    val accentColor = when (currentNews.sentiment) {
        "BULLISH" -> AccentGreen
        "BEARISH" -> AccentRose
        else -> BrandViolet
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(barColor)
            .border(0.5.dp, accentColor.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // BREAKING TAG (Pulsing)
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(accentColor.copy(alpha = alpha))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "BREAKING",
                color = if (currentNews.sentiment == "BEARISH" || currentNews.sentiment == "BULLISH") Color.White else Color.Black,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // CHANNEL BADGE
        Text(
            text = currentNews.source.uppercase(),
            color = if (currentNews.source.contains("CNBC")) Color(0xFFFFD700) else BrandViolet,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.widthIn(max = 100.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // THE HEADLINE (Marquee-like feel using crossfade)
        AnimatedContent(
            targetState = currentNews,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
            },
            modifier = Modifier.weight(1f),
            label = "headline"
        ) { news ->
            Text(
                text = news.title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        if (currentNews.isAiRefined) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "👑 PRO",
                color = AccentYellow,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
