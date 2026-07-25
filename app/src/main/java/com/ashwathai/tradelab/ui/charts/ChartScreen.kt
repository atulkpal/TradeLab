package com.ashwathai.tradelab.ui.charts

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
    
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }

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

            Spacer(modifier = Modifier.height(20.dp))

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
