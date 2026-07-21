package com.ashwathai.tradelab.di

import android.content.Context
import com.ashwathai.tradelab.data.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideUserProfileDao(database: AppDatabase) = database.userProfileDao()

    @Provides
    fun provideHoldingDao(database: AppDatabase) = database.holdingDao()

    @Provides
    fun provideTransactionDao(database: AppDatabase) = database.transactionDao()

    @Provides
    fun provideWatchlistDao(database: AppDatabase) = database.watchlistDao()

    @Provides
    fun provideStockPriceDao(database: AppDatabase) = database.stockPriceDao()

    @Provides
    fun provideWatchlistV2Dao(database: AppDatabase) = database.watchlistV2Dao()

    @Provides
    fun providePendingOrderDao(database: AppDatabase) = database.pendingOrderDao()

    @Provides
    fun provideAppNotificationDao(database: AppDatabase) = database.appNotificationDao()
}
