package com.ashwathai.tradelab.ui.portfolio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ashwathai.tradelab.data.LedgerEntry
import com.ashwathai.tradelab.ui.TradingViewModel
import com.ashwathai.tradelab.ui.common.formatCurrency
import com.ashwathai.tradelab.ui.common.formatLedgerAmount
import com.ashwathai.tradelab.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LedgerScreen(
    viewModel: TradingViewModel,
    onBack: () -> Unit
) {
    val ledgerEntries by viewModel.ledgerEntries.collectAsStateWithLifecycle()
    val stats by viewModel.portfolioStats.collectAsStateWithLifecycle()

    val totalCredits = ledgerEntries.filter { it.type == "CREDIT" }.sumOf { it.amount }
    val totalDebits = ledgerEntries.filter { it.type == "DEBIT" }.sumOf { it.amount }
    val netFlow = totalCredits - totalDebits

    val groupedEntries = groupByDateBucket(ledgerEntries)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "PRECISION TRADEBOOK",
                    color = BrandViolet,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Account Ledger",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (ledgerEntries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ReceiptLong, null, tint = TextMuted, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No activity recorded yet", color = TextSubtle)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    SummaryCard(totalCredits, totalDebits, netFlow, stats.currency)
                }

                groupedEntries.forEach { (bucket, entries) ->
                    item {
                        Text(
                            text = bucket,
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        )
                    }
                    items(entries) { entry ->
                        LedgerItem(entry, stats.currency)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(credits: Double, debits: Double, netFlow: Double, currency: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("SUMMARY", color = BrandViolet, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total Credits", color = TextMuted, fontSize = 10.sp)
                    Text(formatCurrency(credits, currency), color = AccentGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Debits", color = TextMuted, fontSize = 10.sp)
                    Text(formatCurrency(debits, currency), color = AccentRose, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = DarkBorder, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("NET FLOW", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(formatCurrency(netFlow, currency), color = if (netFlow >= 0) AccentGreen else AccentRose, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun groupByDateBucket(entries: List<LedgerEntry>): LinkedHashMap<String, List<LedgerEntry>> {
    val calendar = Calendar.getInstance()
    val now = calendar.timeInMillis

    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val todayStart = calendar.timeInMillis

    calendar.add(Calendar.DAY_OF_YEAR, -1)
    val yesterdayStart = calendar.timeInMillis

    calendar.add(Calendar.DAY_OF_YEAR, -6)
    val weekStart = calendar.timeInMillis

    val buckets = linkedMapOf(
        "Today" to mutableListOf<LedgerEntry>(),
        "Yesterday" to mutableListOf<LedgerEntry>(),
        "This Week" to mutableListOf<LedgerEntry>(),
        "Older" to mutableListOf<LedgerEntry>()
    )

    for (entry in entries) {
        when {
            entry.timestamp >= todayStart -> buckets["Today"]!!.add(entry)
            entry.timestamp >= yesterdayStart -> buckets["Yesterday"]!!.add(entry)
            entry.timestamp >= weekStart -> buckets["This Week"]!!.add(entry)
            else -> buckets["Older"]!!.add(entry)
        }
    }

    val result = LinkedHashMap<String, List<LedgerEntry>>()
    for ((key, value) in buckets) {
        if (value.isNotEmpty()) result[key] = value
    }
    return result
}

private fun getLedgerIcon(type: String, description: String): ImageVector {
    val desc = description.uppercase()
    return when {
        desc.contains("BUY") -> Icons.Default.ShoppingCart
        desc.contains("SELL") || desc.contains("EXIT") || desc.contains("SQUARED") -> Icons.Default.TrendingUp
        desc.contains("FEE") || desc.contains("STT") || desc.contains("CHARGE") || desc.contains("BROKERAGE") -> Icons.Default.RemoveCircleOutline
        desc.contains("DIVIDEND") || desc.contains("DEPOSIT") -> Icons.Default.AccountBalance
        desc.contains("OPTION") || desc.contains("F&O") || desc.contains("EXPIRED") -> Icons.Default.Analytics
        type == "DEBIT" -> Icons.Default.ArrowUpward
        else -> Icons.Default.ArrowDownward
    }
}

@Composable
fun LedgerItem(entry: LedgerEntry, currency: String) {
    val isDebit = entry.type == "DEBIT"
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm:ss", LocalConfiguration.current.locales[0])
    val dateStr = dateFormat.format(Date(entry.timestamp))
    val icon = getLedgerIcon(entry.type, entry.description)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, if (isDebit) AccentRose.copy(alpha = 0.1f) else AccentGreen.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isDebit) AccentRose.copy(alpha = 0.1f) else AccentGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = if (isDebit) AccentRose else AccentGreen, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateStr.uppercase(),
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.description,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "BALANCE",
                        color = TextMuted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatCurrency(entry.runningBalance, currency),
                        color = TextSubtle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = (if (isDebit) "-" else "+") + formatLedgerAmount(entry.amount, currency),
                color = if (isDebit) AccentRose else AccentGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}