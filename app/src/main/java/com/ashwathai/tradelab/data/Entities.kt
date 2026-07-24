package com.ashwathai.tradelab.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val cash: Double = 25000.0,
    val startingCash: Double = 25000.0,
    val riskPreference: String = "Moderate",
    val currency: String = "INR",
    val completedLevels: String = "",
    val isArcadeMode: Boolean = false,
    val trialActionsCount: Int = 0,
    val isLoggedIn: Boolean = false,
    val userName: String = "",
    val userEmail: String = "",
    val isPremium: Boolean = false,
    val brokerageCredits: Int = 300,
    val indicatorsUnlockedUntil: Long = 0L,
    val aiAuditCredits: Int = 3,
    val fnoTokens: Int = 0,
    val portfolioResetsCount: Int = 0,
    val hasAcceptedSimDisclaimer: Boolean = false,
    val isWatchlistCompactMode: Boolean = false,
    val xp: Int = 1500,
    val dailyStreak: Int = 1,
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val shouldShowShieldDialog: Boolean = true,
    val phoneNumber: String = "",
    val userUniqueId: String = "",
    val profilePictureUrl: String = "",
    val isEmailVerified: Boolean = false,
    val isPhoneVerified: Boolean = false
)

@Entity(tableName = "holdings")
data class Holding(
    @PrimaryKey val symbol: String,
    val shares: Double,
    val averagePrice: Double,
    val sharesT1: Double = 0.0 // Shares bought today, settling T+1
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String,
    val type: String, // "BUY" or "SELL"
    val shares: Double,
    val price: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val isDelivery: Boolean = true, // True = Delivery, False = Intraday
    val charges: Double = 0.0,
    val tax: Double = 0.0
)

@Entity(tableName = "watchlist")
data class WatchlistItem(
    @PrimaryKey val symbol: String
)

@Entity(tableName = "stock_prices")
data class StockPrice(
    @PrimaryKey val symbol: String,
    val companyName: String,
    val currentPrice: Double,
    val dailyChangePct: Double,
    val previousClose: Double,
    val highPrice: Double,
    val lowPrice: Double,
    val historyData: String, // Comma-separated list of historical prices (e.g. "180.1,181.2,183.0,...") for line chart
    val targetPrice: Double? = null, // The real-world "Anchor" price we steer towards
    val sentimentBias: Double = 0.0 // -1.0 (Bearish) to 1.0 (Bullish) influenced by real news
)

@Entity(tableName = "watchlist_names")
data class WatchlistName(
    @PrimaryKey val id: Int, // 1 to 5
    val name: String
)

@Entity(tableName = "watchlist_items_v2", primaryKeys = ["watchlistId", "symbol"])
data class WatchlistItemV2(
    val watchlistId: Int,
    val symbol: String
)

@Entity(tableName = "pending_orders")
data class PendingOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String,
    val type: String, // "BUY" or "SELL"
    val orderType: String, // "Limit" or "GTT" or "Stop-Loss"
    val shares: Double,
    val triggerPrice: Double,
    val status: String = "PENDING", // "PENDING", "EXECUTED", "CANCELLED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "market_news")
data class MarketNews(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String,
    val title: String,
    val summary: String,
    val sentiment: String, // "BULLISH", "BEARISH", "NEUTRAL"
    val source: String = "TradeLab Desk", // e.g. "CNBC Awaaz", "Zee News"
    val url: String? = null,
    val isAiRefined: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "account_snapshots")
data class AccountSnapshot(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val totalValue: Double,
    val timestamp: Long = System.currentTimeMillis()
)
