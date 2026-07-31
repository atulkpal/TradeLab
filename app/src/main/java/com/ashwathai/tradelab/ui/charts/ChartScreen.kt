package com.ashwathai.tradelab.ui.charts

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ashwathai.tradelab.data.StockPrice
import com.ashwathai.tradelab.ui.TradingViewModel
import com.ashwathai.tradelab.ui.theme.*

@Composable
fun ChartScreen(
    viewModel: TradingViewModel
) {
    val stockPrices by viewModel.stockPrices.collectAsStateWithLifecycle()
    val selectedSymbol by viewModel.selectedStockSymbol.collectAsStateWithLifecycle()
    val selectedStock = stockPrices.find { it.symbol == selectedSymbol }
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    var showSimDisclaimer by remember { mutableStateOf(true) }
    var showChartTypeMenu by remember { mutableStateOf(false) }

    val timeframes = listOf("15m", "1H", "4H", "1D", "1W")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        // Ticker Search Bar
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    showSearchResults = it.isNotBlank()
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search Symbol (e.g. RELIANCE)", color = TextSubtle, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrandViolet) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = BrandViolet,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                )
            )

            if (showSearchResults) {
                val filtered = stockPrices.filter { 
                    it.symbol.contains(searchQuery, ignoreCase = true) || 
                    it.companyName.contains(searchQuery, ignoreCase = true) 
                }.take(5)

                if (filtered.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        elevation = CardDefaults.cardElevation(8.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column {
                            filtered.forEach { stock ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectStock(stock.symbol)
                                            searchQuery = ""
                                            showSearchResults = false
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(stock.symbol, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(stock.companyName, color = TextMuted, fontSize = 10.sp)
                                    }
                                    Text(
                                        "${if (stock.dailyChangePct >= 0) "+" else ""}${String.format("%.2f", stock.dailyChangePct)}%",
                                        color = if (stock.dailyChangePct >= 0) AccentGreen else AccentRose,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                HorizontalDivider(color = DarkBorder)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedStock != null) {
            // Simulated Data Disclosure Banner
            if (showSimDisclaimer) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentYellow.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, AccentYellow.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = AccentYellow, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chart data is simulated for educational purposes. Prices do not reflect real market values.", color = AccentYellow.copy(alpha = 0.8f), fontSize = 9.sp, lineHeight = 12.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = { showSimDisclaimer = false }, modifier = Modifier.size(18.dp)) {
                            Icon(Icons.Default.Clear, null, tint = AccentYellow.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }

            // Header: Symbol & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = selectedStock.symbol,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = selectedStock.companyName,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format("%.2f", selectedStock.currentPrice),
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${if (selectedStock.dailyChangePct >= 0) "+" else ""}${String.format("%.2f", selectedStock.dailyChangePct)}%",
                        color = if (selectedStock.dailyChangePct >= 0) AccentGreen else AccentRose,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timeframe Selector
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                timeframes.forEach { tf ->
                    TextButton(
                        onClick = { viewModel.setTimeframe(tf) },
                        colors = ButtonDefaults.textButtonColors(contentColor = if (selectedTimeframe == tf) BrandViolet else TextMuted),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp).then(
                            if (selectedTimeframe == tf) Modifier.border(1.dp, BrandViolet.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            else Modifier
                        )
                    ) {
                        Text(tf, fontSize = 10.sp, fontWeight = if (selectedTimeframe == tf) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Detailed Technical Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                StockLineChart(
                    pricesString = selectedStock.historyData,
                    isPositive = selectedStock.dailyChangePct >= 0,
                    showIndicators = true,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bottom Info: Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Open", String.format("%.2f", selectedStock.previousClose))
                StatItem("High", String.format("%.2f", selectedStock.highPrice))
                StatItem("Low", String.format("%.2f", selectedStock.lowPrice))
            }
        } else {
            // Empty State
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Select a ticker to view detailed charts", color = TextMuted, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
