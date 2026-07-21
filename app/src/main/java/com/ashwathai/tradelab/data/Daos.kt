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
}

@Dao
interface HoldingDao {
    @Query("SELECT * FROM holdings")
    fun getAllHoldingsFlow(): Flow<List<Holding>>

    @Query("SELECT * FROM holdings")
    suspend fun getAllHoldings(): List<Holding>

    @Query("SELECT * FROM holdings WHERE symbol = :symbol LIMIT 1")
    suspend fun getHoldingBySymbol(symbol: String): Holding?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: Holding)

    @Delete
    suspend fun deleteHolding(holding: Holding)

    @Query("DELETE FROM holdings WHERE symbol = :symbol")
    suspend fun deleteHoldingBySymbol(symbol: String)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)
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
