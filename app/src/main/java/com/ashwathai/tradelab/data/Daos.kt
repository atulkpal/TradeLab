package com.ashwathai.tradelab.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET hasAcceptedSimDisclaimer = :accepted WHERE id = 1")
    suspend fun updateSimDisclaimer(accepted: Boolean)

    @Query("UPDATE user_profile SET isWatchlistCompactMode = :isCompact WHERE id = 1")
    suspend fun updateWatchlistCompactMode(isCompact: Boolean)

    @Query("UPDATE user_profile SET shouldShowShieldDialog = :show WHERE id = 1")
    suspend fun updateShieldDialogPreference(show: Boolean)

    @Query("UPDATE user_profile SET themeMode = :themeMode WHERE id = 1")
    suspend fun updateThemeMode(themeMode: String)

    @Query("UPDATE user_profile SET isStealthMode = :stealth WHERE id = 1")
    suspend fun updateStealthMode(stealth: Boolean)

    @Query("UPDATE user_profile SET isZenMode = :zen WHERE id = 1")
    suspend fun updateZenMode(zen: Boolean)
}

@Dao
interface HoldingDao {
    @Query("SELECT * FROM holdings")
    fun getAllHoldingsFlow(): Flow<List<Holding>>

    @Query("SELECT * FROM holdings")
    suspend fun getAllHoldings(): List<Holding>

    @Query("SELECT * FROM holdings WHERE symbol = :symbol")
    suspend fun getHoldingsBySymbol(symbol: String): List<Holding>

    @Query("SELECT * FROM holdings WHERE symbol = :symbol AND isDelivery = :isDelivery LIMIT 1")
    suspend fun getHolding(symbol: String, isDelivery: Boolean): Holding?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: Holding)

    @Delete
    suspend fun deleteHolding(holding: Holding)

    @Query("DELETE FROM holdings WHERE symbol = :symbol AND isDelivery = :isDelivery")
    suspend fun deleteHolding(symbol: String, isDelivery: Boolean)

    @Query("DELETE FROM holdings WHERE symbol = :symbol")
    suspend fun deleteHoldingsBySymbol(symbol: String)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long
}

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist")
    fun getWatchlistFlow(): Flow<List<WatchlistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistItem(item: WatchlistItem)

    @Query("DELETE FROM watchlist WHERE symbol = :symbol")
    suspend fun deleteWatchlistItem(symbol: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE symbol = :symbol)")
    suspend fun isWatchlisted(symbol: String): Boolean
}

@Dao
interface StockPriceDao {
    @Query("SELECT * FROM stock_prices")
    fun getAllStockPricesFlow(): Flow<List<StockPrice>>

    @Query("SELECT * FROM stock_prices WHERE symbol = :symbol LIMIT 1")
    suspend fun getStockPrice(symbol: String): StockPrice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockPrices(prices: List<StockPrice>)

    @Query("UPDATE stock_prices SET targetPrice = :targetPrice WHERE symbol = :symbol")
    suspend fun updateTargetPrice(symbol: String, targetPrice: Double?)

    @Query("UPDATE stock_prices SET sentimentBias = :bias WHERE symbol = :symbol")
    suspend fun updateStockSentimentBias(symbol: String, bias: Double)
}

@Dao
interface WatchlistV2Dao {
    @Query("SELECT * FROM watchlist_names ORDER BY id ASC")
    fun getWatchlistNamesFlow(): Flow<List<WatchlistName>>

    @Query("SELECT * FROM watchlist_items_v2 WHERE watchlistId = :watchlistId")
    fun getWatchlistItemsFlow(watchlistId: Int): Flow<List<WatchlistItemV2>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistName(name: WatchlistName)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistItemV2(item: WatchlistItemV2)

    @Query("DELETE FROM watchlist_items_v2 WHERE watchlistId = :watchlistId AND symbol = :symbol")
    suspend fun deleteWatchlistItemV2(watchlistId: Int, symbol: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_items_v2 WHERE watchlistId = :watchlistId AND symbol = :symbol)")
    suspend fun isWatchlistedV2(watchlistId: Int, symbol: String): Boolean

    @Query("DELETE FROM watchlist_names WHERE id = :id")
    suspend fun deleteWatchlistName(id: Int)

    @Query("DELETE FROM watchlist_items_v2 WHERE watchlistId = :id")
    suspend fun deleteWatchlistItemsByWatchlistId(id: Int)
}

@Dao
interface PendingOrderDao {
    @Query("SELECT * FROM pending_orders ORDER BY timestamp DESC")
    fun getAllPendingOrdersFlow(): Flow<List<PendingOrder>>

    @Query("SELECT * FROM pending_orders WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun getPendingOrdersFlow(): Flow<List<PendingOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingOrder(order: PendingOrder)

    @Query("UPDATE pending_orders SET status = :status WHERE id = :id")
    suspend fun updateOrderStatus(id: Int, status: String)

    @Query("DELETE FROM pending_orders WHERE id = :id")
    suspend fun deletePendingOrder(id: Int)

    @Query("UPDATE pending_orders SET triggerPrice = :triggerPrice, trailingBaselinePrice = :baselinePrice WHERE id = :id")
    suspend fun updateTrailingOrder(id: Int, triggerPrice: Double, baselinePrice: Double)

    @Query("SELECT * FROM pending_orders WHERE status = 'PENDING' AND orderType = 'Limit' AND timestamp < :cutoffTimestamp")
    suspend fun getExpiredPendingOrders(cutoffTimestamp: Long): List<PendingOrder>
}

@Dao
interface AppNotificationDao {
    @Query("SELECT * FROM app_notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<AppNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification)

    @Query("UPDATE app_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("DELETE FROM app_notifications")
    suspend fun clearAll()
}

@Dao
interface MarketNewsDao {
    @Query("SELECT * FROM market_news WHERE symbol = :symbol ORDER BY timestamp DESC")
    fun getNewsBySymbolFlow(symbol: String): Flow<List<MarketNews>>

    @Query("SELECT * FROM market_news ORDER BY timestamp DESC LIMIT :limit")
    fun getLatestNewsFlow(limit: Int): Flow<List<MarketNews>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(news: List<MarketNews>)

    @Query("DELETE FROM market_news WHERE timestamp < :expiryTimestamp")
    suspend fun deleteOldNews(expiryTimestamp: Long)
}

@Dao
interface AccountSnapshotDao {
    @Query("SELECT * FROM account_snapshots ORDER BY timestamp ASC")
    fun getAllSnapshotsFlow(): Flow<List<AccountSnapshot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: AccountSnapshot)

    @Query("DELETE FROM account_snapshots WHERE timestamp < :expiryTimestamp")
    suspend fun deleteOldSnapshots(expiryTimestamp: Long)
}

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledger_entries ORDER BY timestamp DESC")
    fun getAllLedgerEntriesFlow(): Flow<List<LedgerEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntry(entry: LedgerEntry)

    @Query("DELETE FROM ledger_entries")
    suspend fun deleteAll()
}

@Dao
interface CandleEntryDao {
    @Query("SELECT * FROM candle_entries WHERE symbol = :symbol AND resolution = :resolution ORDER BY timestamp ASC")
    fun getCandlesFlow(symbol: String, resolution: String): Flow<List<CandleEntry>>

    @Query("SELECT * FROM candle_entries WHERE symbol = :symbol AND resolution = :resolution AND timestamp >= :since ORDER BY timestamp ASC")
    suspend fun getCandlesSince(symbol: String, resolution: String, since: Long): List<CandleEntry>

    @Query("SELECT * FROM candle_entries WHERE symbol = :symbol AND resolution = :resolution ORDER BY timestamp ASC")
    suspend fun getCandles(symbol: String, resolution: String): List<CandleEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandles(candles: List<CandleEntry>)

    @Query("DELETE FROM candle_entries WHERE symbol = :symbol")
    suspend fun deleteCandlesBySymbol(symbol: String)

    @Query("DELETE FROM candle_entries")
    suspend fun deleteAll()
}

@Dao
interface OptionContractDao {
    @Query("SELECT * FROM option_contracts WHERE isActive = 1")
    fun getAllActiveContractsFlow(): Flow<List<OptionContract>>

    @Query("SELECT * FROM option_contracts WHERE underlyingSymbol = :underlying AND isActive = 1")
    suspend fun getActiveContractsByUnderlying(underlying: String): List<OptionContract>

    @Query("SELECT * FROM option_contracts WHERE symbol = :symbol LIMIT 1")
    suspend fun getContract(symbol: String): OptionContract?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContract(contract: OptionContract)

    @Query("UPDATE option_contracts SET isActive = 0 WHERE symbol = :symbol")
    suspend fun deactivateContract(symbol: String)
}
