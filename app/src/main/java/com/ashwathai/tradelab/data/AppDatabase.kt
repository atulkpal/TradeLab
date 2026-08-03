package com.ashwathai.tradelab.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserProfile::class,
        Holding::class,
        Transaction::class,
        WatchlistItem::class,
        StockPrice::class,
        CandleEntry::class,
        OptionContract::class,
        WatchlistName::class,
        WatchlistItemV2::class,
        PendingOrder::class,
        AppNotification::class,
        MarketNews::class,
        AccountSnapshot::class,
        LedgerEntry::class
    ],
    version = 25,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun holdingDao(): HoldingDao
    abstract fun transactionDao(): TransactionDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun stockPriceDao(): StockPriceDao
    abstract fun candleEntryDao(): CandleEntryDao
    abstract fun optionContractDao(): OptionContractDao
    abstract fun watchlistV2Dao(): WatchlistV2Dao
    abstract fun pendingOrderDao(): PendingOrderDao
    abstract fun appNotificationDao(): AppNotificationDao
    abstract fun marketNewsDao(): MarketNewsDao
    abstract fun accountSnapshotDao(): AccountSnapshotDao
    abstract fun ledgerDao(): LedgerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "paper_trader_db"
                )
                .addMigrations(MIGRATION_22_23, MIGRATION_23_24, MIGRATION_24_25)
                .fallbackToDestructiveMigration()
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .build()
                INSTANCE = instance
                instance
            }
        }

        // v22 -> v23: track claimed mission IDs so rewards can be claimed once
        val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN claimedMissions TEXT NOT NULL DEFAULT ''")
            }
        }

        // v23 -> v24: Multi-theme support
        val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN themeMode TEXT NOT NULL DEFAULT 'SERIOUS'")
            }
        }

        // v24 -> v25: Stealth and Zen modes
        val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN isStealthMode INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN isZenMode INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
