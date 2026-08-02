package com.ashwathai.tradelab.data

import com.ashwathai.tradelab.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.random.Random
import java.util.Calendar
import java.util.TimeZone
import java.net.URLEncoder
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

import javax.inject.Inject
import javax.inject.Singleton

data class SearchResult(
    val symbol: String,
    val name: String,
    val exchange: String
)

@Singleton
class TradingRepository @Inject constructor(
    private val database: AppDatabase,
    private val disciplineCalculator: DisciplineCalculator
) {

    // Commodity lot sizes for MCX
    companion object {
        val INDIAN_TICKERS = listOf(
            "RELIANCE", "TCS", "INFY", "HDFCBANK", "ICICIBANK", "SBIN", "BHARTIARTL", "ITC",
            "LICI", "LT", "KOTAKBANK", "AXISBANK", "WIPRO", "ASIANPAINT", "HINDUNILVR",
            "MARUTI", "TATASTEEL", "M&M", "ADANIENT", "SUNPHARMA", "JSWSTEEL", "ONGC",
            "COALINDIA", "NTPC", "POWERGRID", "ULTRACEMCO", "TATAMOTORS", "BAJFINANCE", "HINDALCO",
            "HCLTECH", "TECHM", "LTIM", "COFORGE", "PERSISTENT", "MPHASIS", "KPITTECH", "INDUSINDBK",
            "BOB", "CANBK", "PNB", "FEDERALBNK", "IDFCFIRSTB", "BANDHANBNK", "YESBANK", "MCX",
            "RECLTD", "PFC", "LICHSGFIN", "HDFCLIFE", "SBILIFE", "BPCL", "IOC", "GAIL",
            "ADANIGREEN", "ADANIPOWER", "TATAPOWER", "NHPC", "SJVN", "IREDA", "BAJAJ-AUTO",
            "HEROMOTOCO", "EICHERMOT", "ASHOKLEY", "TVSMOTOR", "BHEL", "HAL", "BEL", "ADANIENTS",
            "VEDL", "NMDC", "SAIL", "NATIONALUM", "NESTLEIND", "BRITANNIA", "TATACONSUM", "VBL",
            "GODREJCP", "DABUR", "MARICO", "COLPAL", "MCDOWELL-N", "CIPLA", "DRREDDY", "APOLLOHOSP",
            "DIVISLAB", "LUPIN", "AUROPHARMA", "MAXHEALTH", "BIOCON", "GRASIM", "AMBUJACEM", "ACC",
            "SHREECEM", "DLF", "LODHA", "SOBHA", "INDIGO", "ZOMATO", "PAYTM", "NYKAA", "POLICYBZR"
        )

        val COMMODITY_LOT_SIZES = mapOf(
            "MCX_GOLD" to 10.0,
            "MCX_SILVER" to 30.0,
            "MCX_CRUDE" to 100.0,
            "MCX_NATGAS" to 1250.0,
            "MCX_COPPER" to 2500.0
        )

        val STRIKE_INTERVALS = mapOf(
            "RELIANCE" to 50.0, "TCS" to 50.0, "INFY" to 20.0,
            "HDFCBANK" to 50.0, "ICICIBANK" to 20.0, "SBIN" to 10.0,
            "TATAMOTORS" to 10.0, "LT" to 50.0, "WIPRO" to 10.0,
            "BHARTIARTL" to 10.0, "ITC" to 5.0, "BAJFINANCE" to 100.0
        )

        val COMMODITY_STT = 0.0001
    }

    var isSimulatedMode: Boolean = BuildConfig.DEBUG

    fun isIndianStockSymbol(symbol: String): Boolean {
        val upper = symbol.uppercase().trim()
        return upper.endsWith(".NS") || upper.endsWith(".BO") || INDIAN_TICKERS.contains(upper)
    }

    fun getConvertedStockPrice(priceInNativeCurrency: Double, symbol: String, targetCurrency: String): Double {
        val nativeCurrency = if (isIndianStockSymbol(symbol)) "INR" else "USD"
        if (nativeCurrency == targetCurrency) {
            return priceInNativeCurrency
        }
        return if (nativeCurrency == "USD" && targetCurrency == "INR") {
            priceInNativeCurrency * 83.0
        } else {
            priceInNativeCurrency / 83.0
        }
    }

    private val userProfileDao = database.userProfileDao()
    private val holdingDao = database.holdingDao()
    private val transactionDao = database.transactionDao()
    private val watchlistDao = database.watchlistDao()
    private val stockPriceDao = database.stockPriceDao()
    private val watchlistV2Dao = database.watchlistV2Dao()
    private val pendingOrderDao = database.pendingOrderDao()
    private val appNotificationDao = database.appNotificationDao()
    private val marketNewsDao = database.marketNewsDao()
    private val accountSnapshotDao = database.accountSnapshotDao()
    private val ledgerDao = database.ledgerDao()
    private val candleEntryDao = database.candleEntryDao()
    private val optionContractDao = database.optionContractDao()

    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfileFlow()
    val holdings: Flow<List<Holding>> = holdingDao.getAllHoldingsFlow()
    val transactions: Flow<List<Transaction>> = transactionDao.getAllTransactionsFlow()
    val watchlist: Flow<List<WatchlistItem>> = watchlistDao.getWatchlistFlow()
    val stockPrices: Flow<List<StockPrice>> = stockPriceDao.getAllStockPricesFlow()
    val ledgerEntries: Flow<List<LedgerEntry>> = ledgerDao.getAllLedgerEntriesFlow()

    suspend fun insertStockPrices(prices: List<StockPrice>) = withContext(Dispatchers.IO) {
        stockPriceDao.insertStockPrices(prices)
    }

    val watchlistNames: Flow<List<WatchlistName>> = watchlistV2Dao.getWatchlistNamesFlow()
    val pendingOrders: Flow<List<PendingOrder>> = pendingOrderDao.getAllPendingOrdersFlow()
    val activePendingOrders: Flow<List<PendingOrder>> = pendingOrderDao.getPendingOrdersFlow()
    val appNotifications: Flow<List<AppNotification>> = appNotificationDao.getAllNotificationsFlow()
    val latestNews: Flow<List<MarketNews>> = marketNewsDao.getLatestNewsFlow(20)
    val accountSnapshots: Flow<List<AccountSnapshot>> = accountSnapshotDao.getAllSnapshotsFlow()

    fun getNewsBySymbolFlow(symbol: String): Flow<List<MarketNews>> = marketNewsDao.getNewsBySymbolFlow(symbol)

    // Initialize default stock data and user profile if not present
    suspend fun initializeDataIfEmpty() = withContext(Dispatchers.IO) {
        val currentProfile = userProfileDao.getUserProfile()
        if (currentProfile == null) {
            userProfileDao.insertProfile(
                UserProfile(id = 1, cash = 25000.0, startingCash = 25000.0, riskPreference = "Moderate")
            )
        } else if (currentProfile.startingCash == 10000.0) {
            userProfileDao.insertProfile(
                currentProfile.copy(cash = 25000.0, startingCash = 25000.0)
            )
        }

        val prices = stockPriceDao.getAllStockPricesFlow().firstOrNull() ?: emptyList()
        if (prices.isEmpty()) {
            val now = System.currentTimeMillis()
            val initialStocks = listOf(
                StockPrice("RELIANCE", "Reliance Industries Ltd", 2950.50, 1.25, 2914.00, 2975.00, 2905.00, "$now|2914.00|2975.00|2905.00|2950.50|5000"),
                StockPrice("TCS", "Tata Consultancy Services", 3850.20, -0.80, 3881.30, 3910.00, 3825.00, "$now|3881.30|3910.00|3825.00|3850.20|4000"),
                StockPrice("INFY", "Infosys Limited", 1510.40, 0.45, 1503.60, 1525.00, 1495.00, "$now|1503.60|1525.00|1495.00|1510.40|3500"),
                StockPrice("HDFCBANK", "HDFC Bank Limited", 1610.10, 1.10, 1592.50, 1622.00, 1585.00, "$now|1592.50|1622.00|1585.00|1610.10|8000"),
                StockPrice("ICICIBANK", "ICICI Bank Limited", 1120.50, 0.85, 1111.00, 1130.00, 1110.00, "$now|1111.00|1130.00|1110.00|1120.50|6500"),
                StockPrice("SBIN", "State Bank of India", 785.40, 1.45, 774.20, 792.00, 770.00, "$now|774.20|792.00|770.00|785.40|12000"),
                StockPrice("BHARTIARTL", "Bharti Airtel Limited", 1385.20, -0.30, 1389.40, 1398.00, 1375.00, "$now|1389.40|1398.00|1375.00|1385.20|2500"),
                StockPrice("ITC", "ITC Limited", 432.10, 0.65, 429.30, 435.00, 427.00, "$now|429.30|435.00|427.00|432.10|15000"),
                StockPrice("WIPRO", "Wipro Limited", 482.50, -0.90, 486.90, 491.00, 480.00, "$now|486.90|491.00|480.00|482.50|4200"),
                StockPrice("HINDUNILVR", "Hindustan Unilever Ltd", 2465.00, 0.50, 2452.70, 2480.00, 2445.00, "$now|2452.70|2480.00|2445.00|2465.00|1800"),
                StockPrice("TATAMOTORS", "Tata Motors Limited", 965.80, 2.10, 946.00, 974.00, 940.00, "$now|946.00|974.00|940.00|965.80|9500"),
                StockPrice("TATASTEEL", "Tata Steel Limited", 165.40, -0.55, 166.30, 167.10, 164.20, "$now|166.30|167.10|164.20|165.40|25000"),
                StockPrice("LICI", "Life Insurance Corp of India", 975.30, 0.80, 967.50, 982.00, 961.00, "$now|967.50|982.00|961.00|975.30|3200"),
                StockPrice("LT", "Larsen & Toubro Limited", 3550.00, 1.15, 3509.60, 3580.00, 3495.00, "$now|3509.60|3580.00|3495.00|3550.00|1500"),
                StockPrice("KOTAKBANK", "Kotak Mahindra Bank", 1740.00, -0.40, 1747.00, 1758.00, 1725.00, "$now|1747.00|1758.00|1725.00|1740.00|2100"),
                StockPrice("AXISBANK", "Axis Bank Limited", 1050.40, 0.95, 1040.50, 1060.00, 1035.00, "$now|1040.50|1060.00|1035.00|1050.40|4800"),
                StockPrice("ASIANPAINT", "Asian Paints Limited", 2850.00, -1.20, 2884.60, 2900.00, 2835.00, "$now|2884.60|2900.00|2835.00|2850.00|1100"),
                StockPrice("M&M", "Mahindra & Mahindra Ltd", 1980.50, 1.65, 1948.30, 1995.00, 1935.00, "$now|1948.30|1995.00|1935.00|1980.50|3300"),
                StockPrice("ADANIENT", "Adani Enterprises Ltd", 3250.00, 2.45, 3172.30, 3280.00, 3150.00, "$now|3172.30|3280.00|3150.00|3250.00|5500"),
                StockPrice("SUNPHARMA", "Sun Pharmaceutical Industries", 1540.00, 0.55, 1531.60, 1555.00, 1520.00, "$now|1531.60|1555.00|1520.00|1540.00|2800"),
                StockPrice("JSWSTEEL", "JSW Steel Limited", 810.00, -0.65, 815.30, 822.00, 804.00, "$now|815.30|822.00|804.00|810.00|4100"),
                StockPrice("ONGC", "Oil & Natural Gas Corp", 275.00, 1.85, 270.00, 278.50, 268.20, "$now|270.00|278.50|268.20|275.00|18000"),
                StockPrice("COALINDIA", "Coal India Limited", 450.00, -0.35, 451.60, 456.00, 447.50, "$now|451.60|456.00|447.50|450.00|9000"),
                StockPrice("NTPC", "NTPC Limited", 345.00, 1.35, 340.40, 348.00, 338.00, "$now|340.40|348.00|338.00|345.00|11000"),
                StockPrice("POWERGRID", "Power Grid Corp of India", 285.00, 0.70, 283.00, 288.50, 281.00, "$now|283.00|288.50|281.00|285.00|7500"),
                StockPrice("ULTRACEMCO", "UltraTech Cement Limited", 9850.00, 1.45, 9709.20, 9920.00, 9680.00, "$now|9709.20|9920.00|9680.00|9850.00|450"),
                StockPrice("BAJFINANCE", "Bajaj Finance Limited", 6850.00, -1.15, 6929.70, 6970.00, 6810.00, "$now|6929.70|6970.00|6810.00|6850.00|2200"),
                StockPrice("HINDALCO", "Hindalco Industries Ltd", 580.00, 0.90, 574.80, 586.00, 571.20, "$now|574.80|586.00|571.20|580.00|10500"),
                StockPrice("HCLTECH", "HCL Technologies Limited", 1620.00, 0.40, 1613.50, 1635.00, 1602.00, "$now|1613.50|1635.00|1602.00|1620.00|3100"),
                StockPrice("TECHM", "Tech Mahindra Limited", 1250.00, -0.60, 1257.50, 1268.00, 1241.00, "$now|1257.50|1268.00|1241.00|1250.00|2800"),
                StockPrice("LTIM", "LTIMindtree Limited", 5200.00, 0.85, 5156.20, 5240.00, 5120.00, "$now|5156.20|5240.00|5120.00|5200.00|950"),
                StockPrice("COFORGE", "Coforge Limited", 5800.00, -1.45, 5885.30, 5920.00, 5750.00, "$now|5885.30|5920.00|5750.00|5800.00|400"),
                StockPrice("PERSISTENT", "Persistent Systems Limited", 3900.00, 1.25, 3851.85, 3930.00, 3820.00, "$now|3851.85|3930.00|3820.00|3900.00|650"),
                StockPrice("MPHASIS", "Mphasis Limited", 2400.00, -0.50, 2412.10, 2435.00, 2382.00, "$now|2412.10|2435.00|2382.00|2400.00|800"),
                StockPrice("KPITTECH", "KPIT Technologies Ltd", 1450.00, 2.15, 1419.50, 1468.00, 1405.00, "$now|1419.50|1468.00|1405.00|1450.00|2100"),
                StockPrice("INDUSINDBK", "IndusInd Bank Limited", 1480.00, -0.90, 1493.45, 1505.00, 1468.00, "$now|1493.45|1505.00|1468.00|1480.00|3200"),
                StockPrice("BOB", "Bank of Baroda", 250.00, 1.80, 245.55, 253.50, 243.20, "$now|245.55|253.50|243.20|250.00|15000"),
                StockPrice("CANBK", "Canara Bank", 115.00, 0.75, 114.15, 116.50, 113.20, "$now|114.15|116.50|113.20|115.00|18000"),
                StockPrice("PNB", "Punjab National Bank", 125.00, 2.10, 122.45, 126.80, 121.10, "$now|122.45|126.80|121.10|125.00|22000"),
                StockPrice("FEDERALBNK", "Federal Bank Limited", 155.00, -0.45, 155.70, 157.50, 153.80, "$now|155.70|157.50|153.80|155.00|12000"),
                StockPrice("IDFCFIRSTB", "IDFC First Bank Limited", 82.00, 0.55, 81.55, 83.20, 81.00, "$now|81.55|83.20|81.00|82.00|45000"),
                StockPrice("BANDHANBNK", "Bandhan Bank Limited", 190.00, -1.25, 192.40, 194.50, 188.20, "$now|192.40|194.50|188.20|190.00|9500"),
                StockPrice("YESBANK", "Yes Bank Limited", 24.50, 1.65, 24.10, 24.95, 23.85, "$now|24.10|24.95|23.85|24.50|100000"),
                StockPrice("MCX", "Multi Commodity Exchange", 3600.00, 2.50, 3512.20, 3645.00, 3495.00, "$now|3512.20|3645.00|3495.00|3600.00|1200"),
                StockPrice("RECLTD", "REC Limited", 480.00, 3.10, 465.55, 486.20, 461.50, "$now|465.55|486.20|461.50|480.00|14000"),
                StockPrice("PFC", "Power Finance Corporation", 420.00, 1.85, 412.35, 424.80, 408.20, "$now|412.35|424.80|408.20|420.00|16000"),
                StockPrice("LICHSGFIN", "LIC Housing Finance Ltd", 620.00, -0.80, 625.00, 631.50, 615.20, "$now|625.00|631.50|615.20|620.00|8500"),
                StockPrice("HDFCLIFE", "HDFC Life Insurance Co", 580.00, 0.45, 577.40, 584.50, 572.10, "$now|577.40|584.50|572.10|580.00|6200"),
                StockPrice("SBILIFE", "SBI Life Insurance Co", 1450.00, 1.25, 1432.10, 1462.00, 1422.00, "$now|1432.10|1462.00|1422.00|1450.00|1800"),
                StockPrice("BPCL", "Bharat Petroleum Corp", 610.00, -1.10, 616.80, 622.50, 604.80, "$now|616.80|622.50|604.80|610.00|11000"),
                StockPrice("IOC", "Indian Oil Corporation", 165.00, 0.90, 163.50, 166.80, 161.20, "$now|163.50|166.80|161.20|165.00|25000"),
                StockPrice("GAIL", "GAIL (India) Limited", 180.00, 1.45, 177.40, 182.50, 175.80, "$now|177.40|182.50|175.80|180.00|14000"),
                StockPrice("ADANIGREEN", "Adani Green Energy Ltd", 1650.00, 2.10, 1616.10, 1675.00, 1602.00, "$now|1616.10|1675.00|1602.00|1650.00|2100"),
                StockPrice("ADANIPOWER", "Adani Power Limited", 580.00, 1.65, 570.55, 587.50, 565.20, "$now|570.55|587.50|565.20|580.00|18000"),
                StockPrice("TATAPOWER", "Tata Power Company Ltd", 390.00, 0.85, 386.70, 394.50, 382.10, "$now|386.70|394.50|382.10|390.00|12000"),
                StockPrice("NHPC", "NHPC Limited", 90.00, 2.25, 88.00, 91.40, 87.25, "$now|88.00|91.40|87.25|90.00|55000"),
                StockPrice("SJVN", "SJVN Limited", 120.00, 1.85, 117.80, 122.40, 115.50, "$now|117.80|122.40|115.50|120.00|32000"),
                StockPrice("IREDA", "IREDA Limited", 170.00, 4.25, 163.05, 174.50, 160.50, "$now|163.05|174.50|160.50|170.00|120000"),
                StockPrice("BAJAJ-AUTO", "Bajaj Auto Limited", 8300.00, -0.65, 8354.30, 8420.00, 8240.00, "$now|8354.30|8420.00|8240.00|8300.00|150"),
                StockPrice("HEROMOTOCO", "Hero MotoCorp Limited", 4400.00, 0.45, 4380.30, 4440.00, 4345.00, "$now|4380.30|4440.00|4345.00|4400.00|350"),
                StockPrice("EICHERMOT", "Eicher Motors Limited", 3850.00, 1.25, 3802.45, 3885.00, 3770.00, "$now|3802.45|3885.00|3770.00|3850.00|420"),
                StockPrice("ASHOKLEY", "Ashok Leyland Limited", 175.00, -0.85, 176.50, 178.20, 173.10, "$now|176.50|178.20|173.10|175.00|16000"),
                StockPrice("TVSMOTOR", "TVS Motor Company Ltd", 2100.00, 1.50, 2068.95, 2125.00, 2050.00, "$now|2068.95|2125.00|2050.00|2100.00|1800"),
                StockPrice("BHEL", "Bharat Heavy Electricals", 225.00, 2.45, 219.60, 228.40, 217.50, "$now|219.60|228.40|217.50|225.00|18000"),
                StockPrice("HAL", "Hindustan Aeronautics Ltd", 3100.00, 3.50, 2995.15, 3145.00, 2975.00, "$now|2995.15|3145.00|2975.00|3100.00|1200"),
                StockPrice("BEL", "Bharat Electronics Ltd", 195.00, 1.15, 192.75, 198.50, 190.20, "$now|192.75|198.50|190.20|195.00|14000"),
                StockPrice("ADANIENTS", "Adani Enterprises Ltd", 3220.00, 1.45, 3173.95, 3255.00, 3150.00, "$now|3173.95|3255.00|3150.00|3220.00|4500"),
                StockPrice("VEDL", "Vedanta Limited", 280.00, -0.90, 282.55, 286.40, 277.10, "$now|282.55|286.40|277.10|280.00|18000"),
                StockPrice("NMDC", "NMDC Limited", 230.00, 1.65, 226.25, 233.50, 224.10, "$now|226.25|233.50|224.10|230.00|14000"),
                StockPrice("SAIL", "Steel Authority of India", 135.00, -0.45, 135.60, 137.40, 133.50, "$now|135.60|137.40|133.50|135.00|28000"),
                StockPrice("NATIONALUM", "National Aluminium Co", 155.00, 1.25, 153.08, 157.40, 151.20, "$now|153.08|157.40|151.20|155.00|16000"),
                StockPrice("NESTLEIND", "Nestle India Limited", 2500.00, -0.50, 2512.55, 2530.00, 2482.00, "$now|2512.55|2530.00|2482.00|2500.00|210"),
                StockPrice("BRITANNIA", "Britannia Industries Ltd", 4900.00, 0.85, 4858.70, 4945.00, 4820.00, "$now|4858.70|4945.00|4820.00|4900.00|350"),
                StockPrice("TATACONSUM", "Tata Consumer Products", 1150.00, 1.10, 1137.50, 1162.00, 1128.00, "$now|1137.50|1162.00|1128.00|1150.00|3200"),
                StockPrice("VBL", "Varun Beverages Limited", 1400.00, 2.35, 1367.85, 1422.00, 1355.00, "$now|1367.85|1422.00|1355.00|1400.00|2100"),
                StockPrice("GODREJCP", "Godrej Consumer Products", 1220.00, -0.45, 1225.50, 1238.00, 1210.00, "$now|1225.50|1238.00|1210.00|1220.00|2800"),
                StockPrice("DABUR", "Dabur India Limited", 530.00, 0.65, 526.55, 534.20, 522.10, "$now|526.55|534.20|522.10|530.00|5500"),
                StockPrice("MARICO", "Marico Limited", 510.00, -0.30, 511.55, 515.50, 506.20, "$now|511.55|515.50|506.20|510.00|6200"),
                StockPrice("COLPAL", "Colgate-Palmolive (India)", 2550.00, 0.95, 2525.95, 2580.00, 2512.00, "$now|2525.95|2580.00|2512.00|2550.00|1400"),
                StockPrice("MCDOWELL-N", "United Spirits Limited", 1100.00, 1.25, 1086.40, 1115.00, 1078.00, "$now|1086.40|1115.00|1078.00|1100.00|3200"),
                StockPrice("CIPLA", "Cipla Limited", 1450.00, 0.45, 1435.15, 1462.00, 1421.00, "$now|1435.15|1462.00|1421.00|1450.00|4500"),
                StockPrice("DRREDDY", "Dr Reddy's Laboratories", 6100.00, -1.25, 6177.20, 6215.00, 6048.00, "$now|6177.20|6215.00|6048.00|6100.00|350"),
                StockPrice("APOLLOHOSP", "Apollo Hospitals Enterprise", 6150.00, 0.85, 6098.15, 6220.00, 6050.00, "$now|6098.15|6220.00|6050.00|6150.00|550"),
                StockPrice("DIVISLAB", "Divi's Laboratories Ltd", 3500.00, 1.45, 3449.95, 3540.00, 3422.00, "$now|3449.95|3540.00|3422.00|3500.00|1100"),
                StockPrice("LUPIN", "Lupin Limited", 1600.00, -0.65, 1610.45, 1625.00, 1585.00, "$now|1610.45|1625.00|1585.00|1600.00|2500"),
                StockPrice("AUROPHARMA", "Aurobindo Pharma Ltd", 1050.00, 1.15, 1038.05, 1062.00, 1025.00, "$now|1038.05|1062.00|1025.00|1050.00|3100"),
                StockPrice("MAXHEALTH", "Max Healthcare Institute", 780.00, 1.85, 765.85, 792.00, 758.00, "$now|765.85|792.00|758.00|780.00|6500"),
                StockPrice("BIOCON", "Biocon Limited", 265.00, -1.10, 267.95, 271.20, 262.10, "$now|267.95|271.20|262.10|265.00|15000"),
                StockPrice("GRASIM", "Grasim Industries Ltd", 2200.00, 0.95, 2179.30, 2225.00, 2162.00, "$now|2179.30|2225.00|2162.00|2200.00|1800"),
                StockPrice("AMBUJACEM", "Ambuja Cements Limited", 600.00, 1.45, 591.40, 608.50, 588.00, "$now|591.40|608.50|588.00|600.00|9500"),
                StockPrice("ACC", "ACC Limited", 2500.00, -0.50, 2512.55, 2535.00, 2480.00, "$now|2512.55|2535.00|2480.00|2500.00|1100"),
                StockPrice("SHREECEM", "Shree Cement Limited", 26000.00, 1.10, 25717.10, 26250.00, 25510.00, "$now|25717.10|26250.00|25510.00|26000.00|85"),
                StockPrice("DLF", "DLF Limited", 850.00, 2.25, 831.30, 862.00, 825.00, "$now|831.30|862.00|825.00|850.00|7500"),
                StockPrice("LODHA", "Macrotech Developers Ltd", 1050.00, 1.35, 1036.00, 1065.00, 1022.00, "$now|1036.00|1065.00|1022.00|1050.00|2100"),
                StockPrice("SOBHA", "Sobha Limited", 1400.00, -1.15, 1416.30, 1432.00, 1381.00, "$now|1416.30|1432.00|1381.00|1400.00|1500"),
                StockPrice("INDIGO", "InterGlobe Aviation Ltd", 3100.00, 1.65, 3049.65, 3140.00, 3025.00, "$now|3049.65|3140.00|3025.00|3100.00|1100"),
                StockPrice("ZOMATO", "Zomato Limited", 160.00, 2.45, 156.15, 163.40, 154.80, "$now|156.15|163.40|154.80|160.00|120000"),
                StockPrice("PAYTM", "One 97 Communications", 410.00, -2.15, 419.00, 424.80, 405.10, "$now|419.00|424.80|405.10|410.00|8500"),
                StockPrice("NYKAA", "FSN E-Commerce Ventures", 160.00, 0.75, 158.80, 162.40, 156.80, "$now|158.80|162.40|156.80|160.00|18000"),
                StockPrice("POLICYBZR", "PB Fintech Limited", 1100.00, 1.85, 1080.00, 1120.00, 1072.00, "$now|1080.00|1120.00|1072.00|1100.00|3300"),
                
                // US Equity
                StockPrice("AAPL", "Apple Inc.", 182.41, 0.78, 181.0, 183.50, 180.20, "$now|181.00|183.50|180.20|182.41|12000"),
                StockPrice("TSLA", "Tesla Motors", 214.50, -2.05, 219.0, 221.30, 212.80, "$now|219.00|221.30|212.80|214.50|15000"),
                StockPrice("NVDA", "NVIDIA Corporation", 875.12, 2.96, 850.0, 880.00, 845.20, "$now|850.00|880.00|845.20|875.12|8500"),
                StockPrice("GOOG", "Alphabet Inc.", 151.60, 1.07, 150.0, 152.90, 149.50, "$now|150.00|152.90|149.50|151.60|4200"),
                StockPrice("MSFT", "Microsoft Corp.", 415.50, -0.60, 418.0, 422.00, 413.50, "$now|418.00|422.00|413.50|415.50|3100"),
                StockPrice("AMZN", "Amazon.com Inc.", 178.15, 1.80, 175.0, 179.20, 173.80, "$now|175.00|179.20|173.80|178.15|5500"),
                
                // Cryptocurrencies
                StockPrice("BTC", "Bitcoin (Digital Asset)", 62500.00, 2.12, 61200.0, 63100.00, 60800.00, "$now|61200.00|63100.00|60800.00|62500.00|150"),
                StockPrice("ETH", "Ethereum (Digital Asset)", 3450.00, -1.99, 3520.0, 3560.00, 3410.00, "$now|3520.00|3560.00|3410.00|3450.00|850"),

                // Global Commodities (USD prices)
                StockPrice("GLOBAL_GOLD", "Gold (Global COMEX Index)", 2420.50, 0.45, 2409.60, 2430.00, 2405.00, "$now|2409.60|2430.00|2405.00|2420.50|120"),
                StockPrice("GLOBAL_SILVER", "Silver (Global NYMEX Index)", 29.20, -0.65, 29.39, 29.60, 29.05, "$now|29.39|29.60|29.05|29.20|1500"),
                StockPrice("GLOBAL_CRUDE", "Crude Oil Brent (Global NYMEX)", 81.50, 1.12, 80.60, 82.20, 80.10, "$now|80.60|82.20|80.10|81.50|250"),
                StockPrice("GLOBAL_NATGAS", "Natural Gas (Global NYMEX)", 2.45, -2.15, 2.50, 2.55, 2.40, "$now|2.50|2.55|2.40|2.45|180"),
                StockPrice("GLOBAL_COPPER", "Copper (Global COMEX)", 4.45, 0.85, 4.41, 4.52, 4.38, "$now|4.41|4.52|4.38|4.45|110"),

                // MCX Indian Commodities (INR converted prices)
                StockPrice("MCX_GOLD", "Gold MCX (per 10g)", 64650.00, 0.45, 64360.00, 64900.00, 64240.00, "$now|64360.00|64900.00|64240.00|64650.00|850"),
                StockPrice("MCX_SILVER", "Silver MCX (per kg)", 77950.00, -0.65, 78460.00, 79020.00, 77550.00, "$now|78460.00|79020.00|77550.00|77950.00|1200"),
                StockPrice("MCX_CRUDE", "Crude Oil MCX (per barrel)", 6764.50, 1.12, 6689.80, 6822.60, 6648.30, "$now|6689.80|6822.60|6648.30|6764.50|2500"),
                StockPrice("MCX_NATGAS", "Natural Gas MCX (per MMBtu)", 203.35, -2.15, 207.50, 211.65, 199.20, "$now|207.50|211.65|199.20|203.35|1800"),
                StockPrice("MCX_COPPER", "Copper MCX (per kg)", 814.25, 0.85, 806.95, 827.05, 801.45, "$now|806.95|827.05|801.45|814.25|1100"),
                
                // Indices
                StockPrice("NIFTY50", "Nifty 50 Index", 24500.0, 0.45, 24390.0, 24550.0, 24300.0, "$now|24390.00|24550.00|24300.00|24500.00|0"),
                StockPrice("BANKNIFTY", "Nifty Bank Index", 52500.0, -0.20, 52605.0, 52800.0, 52400.0, "$now|52605.00|52800.00|52400.00|52500.00|0"),
                StockPrice("NIFTYIT", "Nifty IT Index", 38000.0, 0.15, 37943.0, 38100.0, 37850.0, "$now|37943.00|38100.00|37850.00|38000.00|0")
            )
            val seededStocks = initialStocks.map { stock ->
                val parts = stock.historyData.split("|")
                if (parts.size >= 6) {
                    val ts = parts[0].toLongOrNull() ?: now
                    val o = parts[1]; val h = parts[2]; val l = parts[3]; val c = parts[4]; val v = parts[5]
                    val prevCandleTs = ts - 60000
                    val prevCloseStr = String.format("%.2f", stock.previousClose)
                    val newHistory = "$prevCandleTs|$prevCloseStr|$prevCloseStr|$prevCloseStr|$prevCloseStr|$v;$ts|$o|$h|$l|$c|$v"
                    stock.copy(historyData = newHistory)
                } else stock
            }
            stockPriceDao.insertStockPrices(seededStocks)
        }

        val wNames = watchlistV2Dao.getWatchlistNamesFlow().firstOrNull() ?: emptyList()
        if (wNames.isEmpty()) {
            watchlistV2Dao.insertWatchlistName(WatchlistName(1, "Trade Lab"))
        }

        // Generate historical candle data for all tickers
        val allPrices = stockPriceDao.getAllStockPricesFlow().firstOrNull() ?: emptyList()
        if (allPrices.isNotEmpty()) {
            val existingCandles = candleEntryDao.getCandles(allPrices.first().symbol, "15m")
            if (existingCandles.isEmpty()) {
                generateHistoricalData(allPrices)
            }
        }
    }

    private suspend fun generateHistoricalData(prices: List<StockPrice>) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
        val candleInterval = 15L * 60 * 1000
        val totalCandles = 30 * 24 * 4

        for (stock in prices) {
            if (stock.symbol.contains("_CE_") || stock.symbol.contains("_PE_")) continue
            var price = stock.currentPrice * (1.0 + (Random.nextDouble() - 0.5) * 0.1)
            val candles = mutableListOf<CandleEntry>()

            for (i in 0 until totalCandles) {
                val ts = thirtyDaysAgo + i * candleInterval
                val change = (Random.nextDouble() - 0.48) * 0.6
                val open = price
                val close = price * (1.0 + change / 100.0)
                val high = maxOf(open, close) * (1.0 + Random.nextDouble() * 0.3 / 100.0)
                val low = minOf(open, close) * (1.0 - Random.nextDouble() * 0.3 / 100.0)
                price = close

                candles.add(CandleEntry(
                    symbol = stock.symbol,
                    timestamp = ts,
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = Random.nextDouble() * 10000 + 1000,
                    resolution = "15m"
                ))
            }

            candleEntryDao.insertCandles(candles)
        }
    }

    // Execute Buy Transaction
    suspend fun buyStock(symbol: String, shares: Double, isDelivery: Boolean = true): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isMarketOpen(symbol)) {
            return@withContext Result.failure(Exception("Cannot execute trade. The market for $symbol is currently closed."))
        }
        val profile = userProfileDao.getUserProfile() ?: return@withContext Result.failure(Exception("User Profile not found"))
        val stock = stockPriceDao.getStockPrice(symbol) ?: return@withContext Result.failure(Exception("Stock symbol not found"))
        val existingHolding = holdingDao.getHolding(symbol, isDelivery)

        val totalValueStock = shares * stock.currentPrice
        val totalValueProfileCurrency = getConvertedStockPrice(totalValueStock, symbol, profile.currency)
        
        // --- REALISTIC CHARGES & TAXES ---
        val isCommodity = isCommoditySymbol(symbol)
        val sttRate = when {
            isCommodity -> COMMODITY_STT
            isDelivery -> 0.001
            else -> 0.00025
        }
        val stt = totalValueProfileCurrency * sttRate
        val miscCharges = totalValueProfileCurrency * 0.0001
        val brokerageFee = if (profile.isPremium || profile.brokerageCredits >= 20) 0.0 else totalValueProfileCurrency * 0.0005
        val creditsToConsume = if (!profile.isPremium && profile.brokerageCredits >= 20) 20 else 0

        // Lot size enforcement for commodities
        val lotSize = COMMODITY_LOT_SIZES[symbol]
        if (lotSize != null && shares % lotSize > 0.0001) {
            return@withContext Result.failure(Exception("$symbol trades in lots of ${lotSize.toInt()}. Please enter a valid lot multiple."))
        }

        // --- COVERING vs LONG ENTRY LOGIC ---
        if (existingHolding != null && existingHolding.shares < -0.0001) {
            // CASE A: COVERING SHORT
            val shortSize = kotlin.math.abs(existingHolding.shares)
            val sharesToCover = minOf(shares, shortSize)
            
            // Profit calculation for covered shares: (SellPrice - BuyPrice)
            val entryPricePerShare = existingHolding.averagePrice
            val buyPricePerShare = stock.currentPrice
            val profitPerShare = entryPricePerShare - buyPricePerShare
            val totalProfit = getConvertedStockPrice(sharesToCover * profitPerShare, symbol, profile.currency)
            
            // Release margin for covered portion (assume 5x leverage used at entry)
            val isLeverageUnlocked = profile.isPremium || profile.leverageUnlockedUntil > System.currentTimeMillis()
            val entryValueCovered = getConvertedStockPrice(sharesToCover * entryPricePerShare, symbol, profile.currency)
            val marginReleased = if (isLeverageUnlocked) (entryValueCovered / 5.0) else entryValueCovered
            
            val totalRefund = marginReleased + totalProfit - stt - miscCharges - brokerageFee

            // 1. Update Profile
            val updatedProfile = profile.copy(
                cash = profile.cash + totalRefund,
                brokerageCredits = (profile.brokerageCredits - creditsToConsume).coerceAtLeast(0)
            )
            userProfileDao.insertProfile(updatedProfile)

            // 2. Update Holdings
            val remainingShort = existingHolding.shares + sharesToCover
            if (kotlin.math.abs(remainingShort) < 0.0001) {
                holdingDao.deleteHolding(symbol, isDelivery)
            } else {
                holdingDao.insertHolding(existingHolding.copy(shares = remainingShort))
            }

            // 3. Handle leftover shares (Reverse to Long)
            if (shares > shortSize) {
                val leftover = shares - shortSize
                buyStock(symbol, leftover, isDelivery) // Recursive call for the remaining long portion
            }
        } else {
            // CASE B: LONG ENTRY (Standard Buy)
            val isLeverageUnlocked = if (!isDelivery) (profile.isPremium || profile.leverageUnlockedUntil > System.currentTimeMillis()) else false
            val requiredMargin = if (isLeverageUnlocked) (totalValueProfileCurrency / 5.0) else totalValueProfileCurrency
            val totalDeduction = requiredMargin + stt + miscCharges + brokerageFee

            if (profile.cash < totalDeduction) {
                val sym = if (profile.currency == "INR") "₹" else "$"
                return@withContext Result.failure(Exception("Insufficient funds. Required Margin: $sym${String.format("%.2f", totalDeduction)}, Available: $sym${String.format("%.2f", profile.cash)}"))
            }

            // 1. Calculate updated profile state (Do not write yet)
            val updatedProfile = profile.copy(
                cash = profile.cash - totalDeduction,
                brokerageCredits = (profile.brokerageCredits - creditsToConsume).coerceAtLeast(0)
            )

            // 2. Update Holdings
            if (existingHolding != null) {
                val totalExistingShares = existingHolding.shares + existingHolding.sharesT1
                val newTotalShares = totalExistingShares + shares
                val avgPrice = ((totalExistingShares * existingHolding.averagePrice) + totalValueStock) / newTotalShares
                
                if (isDelivery) {
                    holdingDao.insertHolding(existingHolding.copy(sharesT1 = existingHolding.sharesT1 + shares, averagePrice = avgPrice))
                } else {
                    holdingDao.insertHolding(existingHolding.copy(shares = existingHolding.shares + shares, averagePrice = avgPrice))
                }
            } else {
                holdingDao.insertHolding(
                    Holding(
                        symbol = symbol, 
                        shares = if (isDelivery) 0.0 else shares, 
                        averagePrice = stock.currentPrice,
                        sharesT1 = if (isDelivery) shares else 0.0,
                        isDelivery = isDelivery
                    )
                )
            }

            // 3. Log Transaction
            val txId = transactionDao.insertTransaction(
                Transaction(
                    symbol = symbol, 
                    type = "BUY", 
                    shares = shares, 
                    price = stock.currentPrice,
                    isDelivery = isDelivery,
                    charges = miscCharges + brokerageFee,
                    tax = stt
                )
            ).toInt()

            // 3.1 Log to Ledger (Single Consolidated Entry)
            val breakdown = "BUY $shares shares of $symbol\n" +
                            "Principal: ${String.format("%.4f", requiredMargin)}\n" +
                            "STT: ${String.format("%.4f", stt)}\n" +
                            "Charges: ${String.format("%.4f", miscCharges + brokerageFee)}"
            
            recordLedgerEntry(breakdown, "DEBIT", totalDeduction, updatedProfile.cash, symbol, txId)

            // 4. Update Discipline Score & Final Profile Save
            val newHoldings = holdingDao.getAllHoldings()
            val totalValue = updatedProfile.cash + newHoldings.sumOf { h -> 
                val s = stockPriceDao.getStockPrice(h.symbol)
                val p = s?.currentPrice ?: h.averagePrice
                getConvertedStockPrice((h.shares + h.sharesT1) * p, h.symbol, updatedProfile.currency)
            }
            val newScore = disciplineCalculator.calculateNewScore(updatedProfile, newHoldings, totalValueProfileCurrency, totalValue)
            val badges = disciplineCalculator.evaluateBadges(newScore, newHoldings).joinToString(",")
            
            userProfileDao.insertProfile(updatedProfile.copy(
                disciplineScore = newScore,
                activeBadges = badges,
                lastDisciplineUpdate = System.currentTimeMillis()
            ))
        }

        Result.success(Unit)
    }

    // Execute Sell Transaction
    suspend fun sellStock(symbol: String, shares: Double, isDelivery: Boolean = true): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isMarketOpen(symbol)) {
            return@withContext Result.failure(Exception("Cannot execute trade. The market for $symbol is currently closed."))
        }
        val profile = userProfileDao.getUserProfile() ?: return@withContext Result.failure(Exception("User Profile not found"))
        val stock = stockPriceDao.getStockPrice(symbol) ?: return@withContext Result.failure(Exception("Stock symbol not found"))
        val existingHolding = holdingDao.getHolding(symbol, isDelivery)

        val totalValueStock = shares * stock.currentPrice
        val totalValueProfileCurrency = getConvertedStockPrice(totalValueStock, symbol, profile.currency)

        // --- REALISTIC CHARGES & TAXES ---
        val isCommodity = isCommoditySymbol(symbol)
        val sttRate = when {
            isCommodity -> COMMODITY_STT
            isDelivery -> 0.001
            else -> 0.00025
        }
        val stt = totalValueProfileCurrency * sttRate
        val miscCharges = totalValueProfileCurrency * 0.0001
        val brokerageFee = if (profile.isPremium || profile.brokerageCredits >= 20) 0.0 else totalValueProfileCurrency * 0.0005
        val creditsToConsume = if (!profile.isPremium && profile.brokerageCredits >= 20) 20 else 0

        // --- SHORT SELLING vs LONG EXIT LOGIC ---
        if (existingHolding != null && existingHolding.shares > 0.0001) {
            // CASE A: LONG EXIT (Selling what you own)
            val availableToSell = if (isDelivery) existingHolding.shares else (existingHolding.shares + existingHolding.sharesT1)
            if (availableToSell < shares) {
                val msg = if (isDelivery) "Insufficient settled shares for delivery exit. (Wait for T+1 settlement)" else "Insufficient total shares."
                return@withContext Result.failure(Exception(msg))
            }

            // Margin Release logic
            val costBasisPerShare = existingHolding.averagePrice
            val costOfSharesSold = shares * costBasisPerShare
            val costInProfileCurrency = getConvertedStockPrice(costOfSharesSold, symbol, profile.currency)
            val profitLoss = totalValueProfileCurrency - costInProfileCurrency
            val marginToRefund = if (isDelivery) totalValueProfileCurrency else (costInProfileCurrency / 5.0 + profitLoss)
            
            val totalCredit = marginToRefund - stt - miscCharges - brokerageFee

            // 1. Calculate updated profile state (Do not write yet)
            val updatedProfile = profile.copy(
                cash = profile.cash + totalCredit,
                brokerageCredits = (profile.brokerageCredits - creditsToConsume).coerceAtLeast(0)
            )

            // 2. Update Holdings
            if (isDelivery) {
                val remainingSettled = existingHolding.shares - shares
                if (remainingSettled + existingHolding.sharesT1 > 0.0001) {
                    holdingDao.insertHolding(existingHolding.copy(shares = remainingSettled))
                } else {
                    holdingDao.deleteHolding(symbol, isDelivery)
                }
            } else {
                var remToDeduct = shares
                var newSettled = existingHolding.shares
                var newT1 = existingHolding.sharesT1
                if (newSettled >= remToDeduct) {
                    newSettled -= remToDeduct
                } else {
                    remToDeduct -= newSettled
                    newSettled = 0.0
                    newT1 -= remToDeduct
                }
                if (newSettled + newT1 > 0.0001) {
                    holdingDao.insertHolding(existingHolding.copy(shares = newSettled, sharesT1 = newT1))
                } else {
                    holdingDao.deleteHolding(symbol, isDelivery)
                }
            }

            // 3. Log Transaction
            val txId = transactionDao.insertTransaction(
                Transaction(
                    symbol = symbol, 
                    type = "SELL", 
                    shares = shares, 
                    price = stock.currentPrice,
                    isDelivery = isDelivery,
                    charges = miscCharges + brokerageFee,
                    tax = stt
                )
            ).toInt()

            // 3.1 Log to Ledger (Single Consolidated Entry)
            val principalCredit = totalCredit + stt + miscCharges + brokerageFee
            val breakdown = "SELL $shares shares of $symbol\n" +
                            "Principal Credit: ${String.format("%.4f", principalCredit)}\n" +
                            "STT: ${String.format("%.4f", stt)}\n" +
                            "Charges: ${String.format("%.4f", miscCharges + brokerageFee)}"
            
            recordLedgerEntry(breakdown, "CREDIT", totalCredit, updatedProfile.cash, symbol, txId)

            // 4. Update Discipline Score & Final Profile Save
            val newHoldings = holdingDao.getAllHoldings()
            val totalValue = updatedProfile.cash + newHoldings.sumOf { h -> 
                val s = stockPriceDao.getStockPrice(h.symbol)
                val p = s?.currentPrice ?: h.averagePrice
                getConvertedStockPrice((h.shares + h.sharesT1) * p, h.symbol, updatedProfile.currency)
            }
            val newScore = disciplineCalculator.calculateNewScore(updatedProfile, newHoldings, totalValueProfileCurrency, totalValue)
            val badges = disciplineCalculator.evaluateBadges(newScore, newHoldings).joinToString(",")
            
            userProfileDao.insertProfile(updatedProfile.copy(
                disciplineScore = newScore,
                activeBadges = badges,
                lastDisciplineUpdate = System.currentTimeMillis()
            ))

        } else {
            // CASE B: SHORT ENTRY (Selling what you DON'T own)
            if (isDelivery) {
                return@withContext Result.failure(Exception("Short selling is not allowed in CNC (Delivery) mode. Switch to MIS."))
            }

            // Check if leverage is unlocked
            val isLeverageUnlocked = profile.isPremium || profile.leverageUnlockedUntil > System.currentTimeMillis()
            val requiredMargin = if (isLeverageUnlocked) (totalValueProfileCurrency / 5.0) else totalValueProfileCurrency
            val totalDeduction = requiredMargin + stt + miscCharges + brokerageFee

            if (profile.cash < totalDeduction) {
                return@withContext Result.failure(Exception("Insufficient funds for short entry. Required: $totalDeduction"))
            }

            // 1. Calculate updated profile state (Do not write yet)
            val updatedProfile = profile.copy(
                cash = profile.cash - totalDeduction,
                brokerageCredits = (profile.brokerageCredits - creditsToConsume).coerceAtLeast(0)
            )

            // 2. Update Holdings (Negative shares)
            if (existingHolding != null) {
                // Already short, add to it
                val newShares = existingHolding.shares - shares
                // Average price for short: Weighted average of entry prices
                val totalSharesAbs = kotlin.math.abs(existingHolding.shares)
                val newAvg = ((totalSharesAbs * existingHolding.averagePrice) + (shares * stock.currentPrice)) / (totalSharesAbs + shares)
                holdingDao.insertHolding(existingHolding.copy(shares = newShares, averagePrice = newAvg))
            } else {
                holdingDao.insertHolding(
                    Holding(
                        symbol = symbol,
                        shares = -shares,
                        averagePrice = stock.currentPrice,
                        isDelivery = false
                    )
                )
            }

            // 3. Log Transaction
            val txId = transactionDao.insertTransaction(
                Transaction(
                    symbol = symbol, 
                    type = "SELL", 
                    shares = shares, 
                    price = stock.currentPrice,
                    isDelivery = isDelivery,
                    charges = miscCharges + brokerageFee,
                    tax = stt
                )
            ).toInt()

            // 3.1 Log to Ledger (Single Consolidated Entry)
            val breakdown = "SHORT SELL $shares shares of $symbol\n" +
                            "Margin Blocked: ${String.format("%.4f", requiredMargin)}\n" +
                            "STT: ${String.format("%.4f", stt)}\n" +
                            "Charges: ${String.format("%.4f", miscCharges + brokerageFee)}"
            
            recordLedgerEntry(breakdown, "DEBIT", totalDeduction, updatedProfile.cash, symbol, txId)

            // 4. Update Discipline Score & Final Profile Save
         val newHoldings = holdingDao.getAllHoldings()
            val totalValue = updatedProfile.cash + newHoldings.sumOf { h -> 
                val s = stockPriceDao.getStockPrice(h.symbol)
                val p = s?.currentPrice ?: h.averagePrice
                getConvertedStockPrice((h.shares + h.sharesT1) * p, h.symbol, updatedProfile.currency)
            }
            val newScore = disciplineCalculator.calculateNewScore(updatedProfile, newHoldings, totalValueProfileCurrency, totalValue)
            val badges = disciplineCalculator.evaluateBadges(newScore, newHoldings).joinToString(",")
            
            userProfileDao.insertProfile(updatedProfile.copy(
                disciplineScore = newScore,
                activeBadges = badges,
                lastDisciplineUpdate = System.currentTimeMillis()
            ))
        }

        Result.success(Unit)
    }

    // Reset Portfolio to Initial Value
    suspend fun resetPortfolio(startingBalance: Double, risk: String) = withContext(Dispatchers.IO) {
        val currentProfile = userProfileDao.getUserProfile()
        val curr = currentProfile?.currency ?: "INR"
        val levels = currentProfile?.completedLevels ?: ""
        val arcade = currentProfile?.isArcadeMode ?: false
        val isPremium = currentProfile?.isPremium ?: false
        val currentResets = currentProfile?.portfolioResetsCount ?: 0

        if (!isPremium && currentResets >= 3) {
            throw Exception("Unpaid version is limited to 3 resets. Go Pro for unlimited resets!")
        }

        // Drop existing positions & transaction logs
        val holdingsList = holdingDao.getAllHoldings()
        for (h in holdingsList) {
            holdingDao.deleteHoldingsBySymbol(h.symbol)
        }

        userProfileDao.insertProfile(
            UserProfile(
                id = 1,
                cash = startingBalance,
                startingCash = startingBalance,
                riskPreference = risk,
                currency = curr,
                completedLevels = levels,
                isArcadeMode = arcade,
                isPremium = isPremium,
                portfolioResetsCount = currentResets + 1
            )
        )
        ledgerDao.deleteAll()
        recordLedgerEntry("Account Initialized / Reset", "CREDIT", startingBalance, startingBalance)
    }

    suspend fun updateCurrency(currency: String) = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext
        userProfileDao.insertProfile(profile.copy(currency = currency))
    }

    suspend fun completeTutorialLevel(levelId: Int, reward: Double) = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext
        val levelsList = profile.completedLevels.split(",").filter { it.isNotBlank() }.toMutableSet()
        if (!levelsList.contains(levelId.toString())) {
            levelsList.add(levelId.toString())
            val newLevels = levelsList.joinToString(",")
            val newCash = profile.cash + reward
            userProfileDao.insertProfile(
                profile.copy(
                    completedLevels = newLevels,
                    cash = newCash,
                    startingCash = profile.startingCash + reward
                )
            )
            recordLedgerEntry("Mission Reward: Level $levelId", "CREDIT", reward, newCash)
        }
    }

    suspend fun setArcadeMode(enabled: Boolean) = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext
        userProfileDao.insertProfile(profile.copy(isArcadeMode = enabled))
    }

    suspend fun getClaimedMissionIds(): Set<String> = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext emptySet()
        profile.claimedMissions.split(",").filter { it.isNotBlank() }.toSet()
    }

    suspend fun claimMissionReward(missionId: Int, title: String, reward: Double) = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext
        val claimed = profile.claimedMissions.split(",").filter { it.isNotBlank() }.toMutableSet()
        if (!claimed.contains(missionId.toString())) {
            claimed.add(missionId.toString())
            val newClaimed = claimed.joinToString(",")
            val newCash = profile.cash + reward
            userProfileDao.insertProfile(
                profile.copy(
                    claimedMissions = newClaimed,
                    cash = newCash,
                    startingCash = profile.startingCash + reward
                )
            )
            recordLedgerEntry("Mission Reward: $title", "CREDIT", reward, newCash)
        }
    }

    // Toggle Watchlist Membership
    suspend fun toggleWatchlist(symbol: String): Boolean = withContext(Dispatchers.IO) {
        val isPresent = watchlistDao.isWatchlisted(symbol)
        if (isPresent) {
            watchlistDao.deleteWatchlistItem(symbol)
            false
        } else {
            watchlistDao.insertWatchlistItem(WatchlistItem(symbol))
            true
        }
    }

    // Check if item is watchlisted
    suspend fun isWatchlisted(symbol: String): Boolean = withContext(Dispatchers.IO) {
        watchlistDao.isWatchlisted(symbol)
    }

    // Determine Yahoo symbol suffix
    fun getYahooSymbol(symbol: String): String {
        val upper = symbol.uppercase().trim()
        return when {
            upper == "BTC" -> "BTC-USD"
            upper == "ETH" -> "ETH-USD"
            upper == "GLOBAL_GOLD" || upper == "MCX_GOLD" -> "GC=F"
            upper == "GLOBAL_SILVER" || upper == "MCX_SILVER" -> "SI=F"
            upper == "GLOBAL_CRUDE" || upper == "MCX_CRUDE" -> "CL=F"
            upper == "GLOBAL_NATGAS" || upper == "MCX_NATGAS" -> "NG=F"
            upper == "GLOBAL_COPPER" || upper == "MCX_COPPER" -> "HG=F"
            upper.endsWith(".NS") || upper.endsWith(".BO") -> upper
            INDIAN_TICKERS.contains(upper) -> "$upper.NS"
            else -> upper
        }
    }

    // Indian Market Holidays for 2026 (NSE/BSE)
    // Format: "YYYY-MM-DD"
    private val INDIAN_MARKET_HOLIDAYS = setOf(
        "2026-01-26", "2026-03-06", "2026-03-27", "2026-04-14", "2026-05-01", 
        "2026-05-22", "2026-08-15", "2026-10-02", "2026-10-21", "2026-11-12", 
        "2026-12-25",
        "2027-01-26", "2027-03-22", "2027-03-26", "2027-04-01", "2027-04-14",
        "2027-05-01", "2027-08-15", "2027-10-02", "2027-10-09", "2027-11-01",
        "2027-12-25"
    )

    private fun isIndianMarketHoliday(calendar: Calendar): Boolean {
        val dateStr = String.format("%04d-%02d-%02d", 
            calendar.get(Calendar.YEAR), 
            calendar.get(Calendar.MONTH) + 1, 
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        return INDIAN_MARKET_HOLIDAYS.contains(dateStr)
    }

    // Determine if the market for a symbol is open based on its actual exchange hours
    fun isMarketOpen(symbol: String): Boolean {
        if (isSimulatedMode) {
            return true
        }
        val cleanSymbol = if (symbol.contains("_CE_")) {
            symbol.substringBefore("_CE_")
        } else if (symbol.contains("_PE_")) {
            symbol.substringBefore("_PE_")
        } else {
            symbol
        }
        val uppercaseSymbol = cleanSymbol.uppercase().trim()
        
        // Crypto is open 24/7
        if (uppercaseSymbol.contains("BTC") || uppercaseSymbol.contains("ETH") || uppercaseSymbol.endsWith("-USD")) {
            return true
        }

        val isIndianStock = uppercaseSymbol.endsWith(".NS") || 
                           uppercaseSymbol.endsWith(".BO") || 
                           uppercaseSymbol.startsWith("MCX_") ||
                           INDIAN_TICKERS.contains(uppercaseSymbol)

        val tz = if (isIndianStock) {
            TimeZone.getTimeZone("Asia/Kolkata")
        } else {
            TimeZone.getTimeZone("America/New_York")
        }

        val calendar = Calendar.getInstance(tz)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        // Saturday and Sunday are closed
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return false
        }

        // Check for Indian Market Holidays
        if (isIndianStock && isIndianMarketHoliday(calendar)) {
            return false
        }

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val totalMinutes = hour * 60 + minute

        return if (isIndianStock) {
            if (uppercaseSymbol.startsWith("MCX_")) {
                // MCX: 9:00 AM to 11:30 PM (or 11:55 PM)
                totalMinutes in 540..1410
            } else {
                // Indian Equities: 9:15 AM (555 mins) to 3:30 PM (930 mins)
                totalMinutes in 555..930
            }
        } else {
            // US Market: 9:30 AM (570 mins) to 4:00 PM (960 mins)
            totalMinutes in 570..960
        }
    }

    // Fetch live delayed price from Yahoo Finance
    suspend fun fetchLiveDelayedPrice(symbol: String): StockPrice? = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val yahooSymbol = getYahooSymbol(symbol)
            val url = "https://query1.finance.yahoo.com/v8/finance/chart/$yahooSymbol?interval=15m&range=1d"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val bodyString = response.body?.string() ?: return@use null

                val json = JSONObject(bodyString)
                val chartObj = json.optJSONObject("chart") ?: return@use null
                val resultArr = chartObj.optJSONArray("result") ?: return@use null
                if (resultArr.length() == 0) return@use null
                val resultObj = resultArr.getJSONObject(0)

                val meta = resultObj.optJSONObject("meta") ?: return@use null
                var rawCurrentPrice = meta.optDouble("regularMarketPrice", 0.0)
                var rawPreviousClose = meta.optDouble("previousClose", rawCurrentPrice)
                var rawHighPrice = meta.optDouble("regularMarketDayHigh", rawCurrentPrice)
                var rawLowPrice = meta.optDouble("regularMarketDayLow", rawCurrentPrice)
                var companyName = meta.optString("longName", meta.optString("shortName", symbol))

                val upperSymbol = symbol.uppercase().trim()
                if (upperSymbol.startsWith("MCX_")) {
                    rawCurrentPrice = convertToMCXPrice(upperSymbol, rawCurrentPrice)
                    rawPreviousClose = convertToMCXPrice(upperSymbol, rawPreviousClose)
                    rawHighPrice = convertToMCXPrice(upperSymbol, rawHighPrice)
                    rawLowPrice = convertToMCXPrice(upperSymbol, rawLowPrice)
                    companyName = when (upperSymbol) {
                        "MCX_GOLD" -> "Gold MCX (per 10g)"
                        "MCX_SILVER" -> "Silver MCX (per kg)"
                        "MCX_CRUDE" -> "Crude Oil MCX (per barrel)"
                        "MCX_NATGAS" -> "Natural Gas MCX (per MMBtu)"
                        "MCX_COPPER" -> "Copper MCX (per kg)"
                        else -> companyName
                    }
                } else if (upperSymbol.startsWith("GLOBAL_")) {
                    companyName = when (upperSymbol) {
                        "GLOBAL_GOLD" -> "Gold (Global COMEX Index)"
                        "GLOBAL_SILVER" -> "Silver (Global NYMEX Index)"
                        "GLOBAL_CRUDE" -> "Crude Oil Brent (Global NYMEX)"
                        "GLOBAL_NATGAS" -> "Natural Gas (Global NYMEX)"
                        "GLOBAL_COPPER" -> "Copper (Global COMEX)"
                        else -> companyName
                    }
                }

                val indicators = resultObj.optJSONObject("indicators")
                val quote = indicators?.optJSONArray("quote")
                var historyData = ""
                if (quote != null && quote.length() > 0) {
                    val quoteObj = quote.getJSONObject(0)
                    val openArr = quoteObj.optJSONArray("open")
                    val highArr = quoteObj.optJSONArray("high")
                    val lowArr = quoteObj.optJSONArray("low")
                    val closeArr = quoteObj.optJSONArray("close")
                    val volArr = quoteObj.optJSONArray("volume")
                    val timestampArr = resultObj.optJSONArray("timestamp")

                    if (closeArr != null && closeArr.length() > 0 && timestampArr != null && openArr != null && highArr != null && lowArr != null) {
                        val segments = mutableListOf<String>()
                        for (i in 0 until closeArr.length()) {
                            if (!closeArr.isNull(i) && !openArr.isNull(i) && !highArr.isNull(i) && !lowArr.isNull(i)) {
                                val time = timestampArr.optLong(i, 0L) * 1000L
                                var o = openArr.getDouble(i)
                                var h = highArr.getDouble(i)
                                var l = lowArr.getDouble(i)
                                var c = closeArr.getDouble(i)
                                val v = volArr?.optDouble(i, 0.0) ?: 0.0

                                if (upperSymbol.startsWith("MCX_")) {
                                    o = convertToMCXPrice(upperSymbol, o)
                                    h = convertToMCXPrice(upperSymbol, h)
                                    l = convertToMCXPrice(upperSymbol, l)
                                    c = convertToMCXPrice(upperSymbol, c)
                                }
                                
                                segments.add("$time|${String.format("%.2f", o)}|${String.format("%.2f", h)}|${String.format("%.2f", l)}|${String.format("%.2f", c)}|${v.toLong()}")
                            }
                        }
                        val trimmedSegments = if (segments.size > 24) segments.takeLast(24) else segments
                        historyData = trimmedSegments.joinToString(";")
                    }
                }

                if (historyData.isBlank()) {
                    val now = System.currentTimeMillis()
                    historyData = "$now|${String.format("%.2f", rawPreviousClose)}|${String.format("%.2f", rawHighPrice)}|${String.format("%.2f", rawLowPrice)}|${String.format("%.2f", rawCurrentPrice)}|1000"
                }

                val dailyChangePct = if (rawPreviousClose > 0.0) {
                    ((rawCurrentPrice - rawPreviousClose) / rawPreviousClose) * 100.0
                } else {
                    0.0
                }

                StockPrice(
                    symbol = upperSymbol,
                    companyName = companyName,
                    currentPrice = Math.round(rawCurrentPrice * 100.0) / 100.0,
                    dailyChangePct = Math.round(dailyChangePct * 100.0) / 100.0,
                    previousClose = Math.round(rawPreviousClose * 100.0) / 100.0,
                    highPrice = Math.round(rawHighPrice * 100.0) / 100.0,
                    lowPrice = Math.round(rawLowPrice * 100.0) / 100.0,
                    historyData = historyData
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Convert USD commodity price to converted INR price based on MCX unit specifications
    fun convertToMCXPrice(symbol: String, usdPrice: Double): Double {
        val usdToInr = 83.0
        return when (symbol.uppercase().trim()) {
            "MCX_GOLD" -> {
                // USD Price is per troy ounce (31.1035 grams)
                // MCX trades per 10 grams in INR
                val pricePerGramUsd = usdPrice / 31.1035
                pricePerGramUsd * 10.0 * usdToInr
            }
            "MCX_SILVER" -> {
                // USD Price is per troy ounce (31.1035 grams)
                // MCX trades per 1 kg (1000 grams) in INR
                val pricePerGramUsd = usdPrice / 31.1035
                pricePerGramUsd * 1000.0 * usdToInr
            }
            "MCX_CRUDE" -> {
                // USD Price is per barrel. MCX is also per barrel.
                usdPrice * usdToInr
            }
            "MCX_NATGAS" -> {
                // USD Price is per MMBtu. MCX is also per MMBtu.
                usdPrice * usdToInr
            }
            "MCX_COPPER" -> {
                // USD Price is per pound (lb). MCX is per kg.
                // 1 kg = 2.20462 lbs
                val pricePerKgUsd = usdPrice * 2.20462
                pricePerKgUsd * usdToInr
            }
            else -> usdPrice
        }
    }

    // Dynamic Yahoo Finance Autocomplete API Search for NSE/BSE and other tickers
    suspend fun searchYahooFinanceAutocomplete(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val client = OkHttpClient()
            // Yahoo Finance search autocomplete endpoint
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://query2.finance.yahoo.com/v1/finance/search?q=$encodedQuery&lang=en-IN&region=IN"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use emptyList()
                val bodyString = response.body?.string() ?: return@use emptyList()
                val json = JSONObject(bodyString)
                val quotes = json.optJSONArray("quotes") ?: return@use emptyList()
                val results = mutableListOf<SearchResult>()
                for (i in 0 until quotes.length()) {
                    val quote = quotes.getJSONObject(i)
                    val symbol = quote.optString("symbol", "")
                    val name = quote.optString("longname", quote.optString("shortname", symbol))
                    val exchange = quote.optString("exchange", quote.optString("exchDisp", ""))
                    if (symbol.isNotBlank()) {
                        results.add(SearchResult(symbol, name, exchange))
                    }
                }
                results
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Update all stock prices from Yahoo Finance API
    // Refactored for Steered Simulation: Updates targetPrice instead of currentPrice
    suspend fun updateAllPricesFromYahoo() = withContext(Dispatchers.IO) {
        val prices = stockPriceDao.getAllStockPricesFlow().firstOrNull() ?: return@withContext
        
        val deferreds = prices.map { stock ->
            async {
                val updated = fetchLiveDelayedPrice(stock.symbol)
                if (updated != null) {
                    // Update only the anchor (targetPrice)
                    stockPriceDao.updateTargetPrice(stock.symbol, updated.currentPrice)
                }
            }
        }
        
        deferreds.forEach { it.await() }
        
        // Match pending orders based on the NEW steering-derived prices happens in simulateMarketTick
    }

    fun calculateOptionPremium(
        underlyingPrice: Double,
        strike: Double,
        isCall: Boolean,
        dte: Int = 7
    ): Double {
        val dteFactor = dte.coerceAtLeast(1) / 30.0
        val volatility = 0.25 // 25% volatility approximation
        val intrinsicValue = if (isCall) {
            (underlyingPrice - strike).coerceAtLeast(0.0)
        } else {
            (strike - underlyingPrice).coerceAtLeast(0.0)
        }
        val stdDev = (underlyingPrice * volatility * kotlin.math.sqrt(dteFactor)).coerceAtLeast(0.01)
        val distance = underlyingPrice - strike
        val exponent = - (distance * distance) / (2.0 * stdDev * stdDev)
        val extrinsicValue = (underlyingPrice * 0.05 * kotlin.math.sqrt(dteFactor)) * kotlin.math.exp(exponent)
        val rawPremium = intrinsicValue + extrinsicValue
        return (rawPremium).coerceAtLeast(0.01)
    }

    // Realistic Stock Price Fluctuation (Market Tick Simulation)
    suspend fun simulateMarketTick() = withContext(Dispatchers.IO) {
        val prices = stockPriceDao.getAllStockPricesFlow().firstOrNull() ?: return@withContext

        // 1. Separate options and standard tickers
        val (optionStocks, standardStocks) = prices.partition { 
            it.symbol.contains("_CE_") || it.symbol.contains("_PE_")
        }

        // 2. Fluctuate standard tickers with "Steering / Anchored" logic
        val updatedStandardPrices = standardStocks.map { stock ->
            // In Live mode, only wiggle if the market is actually open
            if (!isSimulatedMode && !isMarketOpen(stock.symbol)) {
                return@map stock
            }

            // A. Random Noise with tiered volatility
            val volatilityPct = getSymbolVolatility(stock.symbol)
            val noisePct = (Random.nextDouble() * volatilityPct * 2.0) - volatilityPct
            val noiseDelta = stock.currentPrice * (noisePct / 100.0)

            // B. Steering / Gravity Drift
            // Gently nudge the price towards the real-world Anchor (targetPrice)
            // We move 5% baseline, but up to 15% if news sentiment aligns (Option B)
            val driftDelta = if (stock.targetPrice != null) {
                val distance = stock.targetPrice - stock.currentPrice
                val isAligning = (distance > 0 && stock.sentimentBias > 0) || (distance < 0 && stock.sentimentBias < 0)
                val boost = if (isAligning) (kotlin.math.abs(stock.sentimentBias) * 0.1) else 0.0
                distance * (0.05 + boost)
            } else {
                // Pure Simulation Fallback
                val organicDriftPct = (Random.nextDouble() * 0.2) - 0.1 // -0.1% to +0.1%
                stock.currentPrice * (organicDriftPct / 100.0)
            }

            // Final tick calculation
            val newPrice = (stock.currentPrice + noiseDelta + driftDelta).coerceAtLeast(0.01)

            // Update history list (OHLCV segments)
            val segments = stock.historyData.split(";").toMutableList()
            val lastSegment = segments.lastOrNull()
            val now = System.currentTimeMillis()
            
            val newHistoryData = if (lastSegment != null) {
                val parts = lastSegment.split("|")
                if (parts.size >= 6) {
                    val timestamp = parts[0].toLongOrNull() ?: now
                    var o = parts[1].toDoubleOrNull() ?: newPrice
                    var h = parts[2].toDoubleOrNull() ?: newPrice
                    var l = parts[3].toDoubleOrNull() ?: newPrice
                    var c = parts[4].toDoubleOrNull() ?: newPrice
                    var v = parts[5].toLongOrNull() ?: 0L

                    // Check if it's time for a new candle (e.g. 1 minute passed)
                    if (now - timestamp >= 60000L) {
                        // New candle
                        val newSegment = "$now|${String.format("%.2f", newPrice)}|${String.format("%.2f", newPrice)}|${String.format("%.2f", newPrice)}|${String.format("%.2f", newPrice)}|${Random.nextLong(100, 5000)}"
                        segments.add(newSegment)
                        if (segments.size > 24) segments.removeAt(0)
                        segments.joinToString(";")
                    } else {
                        // Update existing candle
                        h = maxOf(h, newPrice)
                        l = minOf(l, newPrice)
                        c = newPrice
                        v += Random.nextLong(10, 500)
                        segments[segments.size - 1] = "$timestamp|${String.format("%.2f", o)}|${String.format("%.2f", h)}|${String.format("%.2f", l)}|${String.format("%.2f", c)}|$v"
                        segments.joinToString(";")
                    }
                } else {
                    "$now|${String.format("%.2f", newPrice)}|${String.format("%.2f", newPrice)}|${String.format("%.2f", newPrice)}|${String.format("%.2f", newPrice)}|1000"
                }
            } else {
                "$now|${String.format("%.2f", newPrice)}|${String.format("%.2f", newPrice)}|${String.format("%.2f", newPrice)}|${String.format("%.2f", newPrice)}|1000"
            }

            // Daily high and low
            val newHigh = maxOf(stock.highPrice, newPrice)
            val newLow = minOf(stock.lowPrice, newPrice)

            // Update daily percentage change from the original previousClose
            val dailyChangePct = ((newPrice - stock.previousClose) / stock.previousClose) * 100.0

            stock.copy(
                currentPrice = Math.round(newPrice * 100.0) / 100.0,
                dailyChangePct = Math.round(dailyChangePct * 100.0) / 100.0,
                highPrice = Math.round(newHigh * 100.0) / 100.0,
                lowPrice = Math.round(newLow * 100.0) / 100.0,
                historyData = newHistoryData
            )
        }

        // 3. Update option tickers based on their updated underlying standard stock price
        val updatedOptionPrices = optionStocks.map { option ->
            val isCall = option.symbol.contains("_CE_")
            val separator = if (isCall) "_CE_" else "_PE_"
            val parts = option.symbol.split(separator)
            val underlyingSymbol = parts[0]
            val strikePrice = parts.getOrNull(1)?.toDoubleOrNull() ?: 100.0

            if (!isSimulatedMode && !isMarketOpen(underlyingSymbol)) {
                return@map option
            }

            val underlyingStock = updatedStandardPrices.find { it.symbol == underlyingSymbol }
            val underlyingPrice = underlyingStock?.currentPrice ?: strikePrice

            // Calculate new premium using 7 DTE
            val newPremium = calculateOptionPremium(underlyingPrice, strikePrice, isCall, 7)

            val segments = option.historyData.split(";").toMutableList()
            val lastSegment = segments.lastOrNull()
            val now = System.currentTimeMillis()
            
            val newHistoryData = if (lastSegment != null) {
                val p = lastSegment.split("|")
                if (p.size >= 6) {
                    val timestamp = p[0].toLongOrNull() ?: now
                    var o = p[1].toDoubleOrNull() ?: newPremium
                    var h = p[2].toDoubleOrNull() ?: newPremium
                    var l = p[3].toDoubleOrNull() ?: newPremium
                    var c = p[4].toDoubleOrNull() ?: newPremium
                    var v = p[5].toLongOrNull() ?: 0L

                    if (now - timestamp >= 60000L) {
                        val newSegment = "$now|${String.format("%.2f", newPremium)}|${String.format("%.2f", newPremium)}|${String.format("%.2f", newPremium)}|${String.format("%.2f", newPremium)}|${Random.nextLong(10, 500)}"
                        segments.add(newSegment)
                        if (segments.size > 24) segments.removeAt(0)
                        segments.joinToString(";")
                    } else {
                        h = maxOf(h, newPremium)
                        l = minOf(l, newPremium)
                        c = newPremium
                        v += Random.nextLong(1, 50)
                        segments[segments.size - 1] = "$timestamp|${String.format("%.2f", o)}|${String.format("%.2f", h)}|${String.format("%.2f", l)}|${String.format("%.2f", c)}|$v"
                        segments.joinToString(";")
                    }
                } else {
                    "$now|${String.format("%.2f", newPremium)}|${String.format("%.2f", newPremium)}|${String.format("%.2f", newPremium)}|${String.format("%.2f", newPremium)}|100"
                }
            } else {
                "$now|${String.format("%.2f", newPremium)}|${String.format("%.2f", newPremium)}|${String.format("%.2f", newPremium)}|${String.format("%.2f", newPremium)}|100"
            }

            val newHigh = maxOf(option.highPrice, newPremium)
            val newLow = minOf(option.lowPrice, newPremium)
            val dailyChangePct = if (option.previousClose > 0) ((newPremium - option.previousClose) / option.previousClose) * 100.0 else 0.0

            option.copy(
                currentPrice = Math.round(newPremium * 100.0) / 100.0,
                dailyChangePct = Math.round(dailyChangePct * 100.0) / 100.0,
                highPrice = Math.round(newHigh * 100.0) / 100.0,
                lowPrice = Math.round(newLow * 100.0) / 100.0,
                historyData = newHistoryData
            )
        }

        val allUpdated = updatedStandardPrices + updatedOptionPrices
        stockPriceDao.insertStockPrices(allUpdated)
        
        // 4. Update Indices based on constituent performance
        updateIndices(allUpdated)
        
        // 5. Margin & Institutional Order Processing (Track B)
        checkMarginMaintenance()
        processTrailingStopLosses()

        generateContextualNews(updatedStandardPrices)
        matchPendingOrders()

        // 6. EOD cleanup: Cancel expired Limit orders
        cancelExpiredLimitOrders()

        // 7. F&O Expiry check
        checkOptionExpiry()
    }

    private suspend fun cancelExpiredLimitOrders() = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val totalMinutes = hour * 60 + minute

        // After market close (3:30 PM IST = 930 mins), cancel all pending Limit orders
        if (totalMinutes > 930 && !isSimulatedMode) {
            val cutoff = System.currentTimeMillis()
            val expired = pendingOrderDao.getExpiredPendingOrders(cutoff)
            for (order in expired) {
                pendingOrderDao.updateOrderStatus(order.id, "CANCELLED")
                addNotification("Order Cancelled: Limit order for ${order.symbol} expired at market close.")
            }
        }
    }

    private suspend fun checkOptionExpiry() = withContext(Dispatchers.IO) {
        val options = optionContractDao.getAllActiveContractsFlow().firstOrNull() ?: return@withContext
        val now = System.currentTimeMillis()

        for (contract in options) {
            if (contract.expiry <= now) {
                val holding = holdingDao.getHolding(contract.symbol, true)
                    ?: holdingDao.getHolding(contract.symbol, false)
                if (holding != null) {
                    val totalShares = holding.shares + holding.sharesT1
                    if (totalShares > 0.0001) {
                        val profile = userProfileDao.getUserProfile() ?: continue
                        val pnl = -totalShares * holding.averagePrice
                        val settledCash = profile.cash + pnl
                        userProfileDao.insertProfile(profile.copy(cash = settledCash))
                        holdingDao.deleteHolding(holding)
                        optionContractDao.deactivateContract(contract.symbol)
                        addNotification("Option Expired: ${contract.symbol} expired worthless. P&L: ${String.format("%.2f", pnl)}")
                    }
                }
            }
        }
    }

    // --- MARGIN & MAINTENANCE LOGIC (Sprint 18.1) ---
    private suspend fun checkMarginMaintenance() = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext
        val holdings = holdingDao.getAllHoldings()
        val prices = stockPriceDao.getAllStockPricesFlow().firstOrNull() ?: return@withContext

        var usedMargin = 0.0
        var unrealizedPnL = 0.0

        for (h in holdings) {
            val stock = prices.find { it.symbol == h.symbol } ?: continue
            val livePrice = stock.currentPrice
            val totalShares = h.shares + h.sharesT1
            val totalSharesAbs = kotlin.math.abs(totalShares)

            val convertedLivePrice = getConvertedStockPrice(livePrice, h.symbol, profile.currency)
            val convertedAvgPrice = getConvertedStockPrice(h.averagePrice, h.symbol, profile.currency)

            if (!h.isDelivery) {
                // MIS positions use 5x margin
                usedMargin += (totalSharesAbs * convertedLivePrice) / 5.0
            }

            // Calculate P/L
            if (totalShares >= 0) {
                unrealizedPnL += (totalShares * (convertedLivePrice - convertedAvgPrice))
            } else {
                unrealizedPnL += (totalSharesAbs * (convertedAvgPrice - convertedLivePrice))
            }
        }

        val accountEquity = profile.cash + unrealizedPnL
        
        // Auto-liquidation threshold: Equity falls below 50% of Used Margin
        if (usedMargin > 0 && accountEquity < (usedMargin * 0.5)) {
            val sym = if (profile.currency == "INR") "₹" else "$"
            addNotification("🚨 MARGIN CALL: Account equity ($sym${String.format("%.2f", accountEquity)}) fell below 50% maintenance margin ($sym${String.format("%.2f", usedMargin * 0.5)}). Auto-liquidating MIS positions.")
            
            // Square off largest MIS positions first
            val misPositions = holdings.filter { !it.isDelivery }.sortedByDescending { kotlin.math.abs(it.shares + it.sharesT1) }
            for (pos in misPositions) {
                val totalShares = pos.shares + pos.sharesT1
                if (totalShares > 0) {
                    sellStock(pos.symbol, totalShares, isDelivery = false)
                } else if (totalShares < 0) {
                    buyStock(pos.symbol, kotlin.math.abs(totalShares), isDelivery = false)
                }
            }
        }
    }

    // --- TRAILING STOP-LOSS LOGIC (Sprint 18.2) ---
    private suspend fun processTrailingStopLosses() = withContext(Dispatchers.IO) {
        val pending = pendingOrderDao.getPendingOrdersFlow().firstOrNull() ?: return@withContext
        val prices = stockPriceDao.getAllStockPricesFlow().firstOrNull() ?: return@withContext
        val profile = userProfileDao.getUserProfile() ?: return@withContext

        val trailingOrders = pending.filter { it.status == "PENDING" && it.isTrailing && it.orderType == "Stop-Loss" }
        if (trailingOrders.isEmpty()) return@withContext

        for (order in trailingOrders) {
            val stock = prices.find { it.symbol == order.symbol } ?: continue
            val currentPrice = getConvertedStockPrice(stock.currentPrice, order.symbol, profile.currency)
            
            // Baseline initialization
            var baseline = order.trailingBaselinePrice ?: currentPrice
            var triggerPrice = order.triggerPrice
            val gap = order.trailingGap

            var updated = false

            if (order.type == "SELL") {
                // SELL Stop-Loss (Trails a LONG position)
                // Move trigger UP if price reaches new HIGH
                if (currentPrice > baseline) {
                    baseline = currentPrice
                    triggerPrice = baseline - gap
                    updated = true
                }
            } else {
                // BUY Stop-Loss (Trails a SHORT position)
                // Move trigger DOWN if price reaches new LOW
                if (currentPrice < baseline) {
                    baseline = currentPrice
                    triggerPrice = baseline + gap
                    updated = true
                }
            }

            if (updated) {
                pendingOrderDao.updateTrailingOrder(order.id, triggerPrice, baseline)
            }
        }
    }

    private suspend fun updateIndices(allPrices: List<StockPrice>) {
        val indices = allPrices.filter { it.symbol in listOf("NIFTY50", "BANKNIFTY", "NIFTYIT") }
        if (indices.isEmpty()) return

        val now = System.currentTimeMillis()
        val updatedIndices = indices.map { index ->
            val constituents = when (index.symbol) {
                "NIFTY50" -> listOf("RELIANCE", "TCS", "HDFCBANK", "ICICIBANK", "INFY", "ITC", "BHARTIARTL")
                "BANKNIFTY" -> listOf("HDFCBANK", "ICICIBANK", "SBIN", "KOTAKBANK", "AXISBANK")
                "NIFTYIT" -> listOf("TCS", "INFY", "WIPRO", "HCLTECH", "TECHM")
                else -> emptyList()
            }

            if (constituents.isEmpty()) return@map index

            // Calculate weighted average change
            var totalChange = 0.0
            var validCount = 0
            constituents.forEach { sym ->
                val s = allPrices.find { it.symbol == sym }
                if (s != null) {
                    totalChange += s.dailyChangePct
                    validCount++
                }
            }

            if (validCount == 0) return@map index
            val avgChangePct = totalChange / validCount
            
            // New Index Price = Prev Close * (1 + avgChange/100)
            val newPrice = index.previousClose * (1 + avgChangePct / 100.0)
            
            // Professional OHLCV History Logic
            val segments = if (index.historyData.contains("|")) {
                index.historyData.split(";").toMutableList()
            } else {
                mutableListOf()
            }

            val lastSegment = segments.lastOrNull()
            val newHistoryData = if (lastSegment != null) {
                val parts = lastSegment.split("|")
                if (parts.size >= 6) {
                    val timestamp = parts[0].toLongOrNull() ?: now
                    var o = parts[1].toDoubleOrNull() ?: newPrice
                    var h = parts[2].toDoubleOrNull() ?: newPrice
                    var l = parts[3].toDoubleOrNull() ?: newPrice
                    var c = parts[4].toDoubleOrNull() ?: newPrice
                    var v = parts[5].toLongOrNull() ?: 0L

                    if (now - timestamp >= 60000L) {
                        // New candle
                        val newSegment = "$now|${String.format("%.2f", newPrice)}|${String.format("%.2f", newPrice)}|${String.format("%.2f", newPrice)}|${String.format("%.2f", newPrice)}|0"
                        segments.add(newSegment)
                        if (segments.size > 24) segments.removeAt(0)
                        segments.joinToString(";")
                    } else {
                        // Update existing candle
                        h = maxOf(h, newPrice)
                        l = minOf(l, newPrice)
                        c = newPrice
                        segments[segments.size - 1] = "$timestamp|${String.format("%.2f", o)}|${String.format("%.2f", h)}|${String.format("%.2f", l)}|${String.format("%.2f", c)}|$v"
                        segments.joinToString(";")
                    }
                } else {
                    "$now|${String.format("%.2f", newPrice)}|${String.format("%.2f", newPrice)}|${String.format("%.2f", newPrice)}|${String.format("%.2f", newPrice)}|0"
                }
            } else {
                "$now|${String.format("%.2f", newPrice)}|${String.format("%.2f", newPrice)}|${String.format("%.2f", newPrice)}|${String.format("%.2f", newPrice)}|0"
            }

            index.copy(
                currentPrice = Math.round(newPrice * 100.0) / 100.0,
                dailyChangePct = Math.round(avgChangePct * 100.0) / 100.0,
                highPrice = maxOf(index.highPrice, newPrice),
                lowPrice = minOf(index.lowPrice, newPrice),
                historyData = newHistoryData
            )
        }
        stockPriceDao.insertStockPrices(updatedIndices)
    }

    suspend fun recordAccountSnapshot(totalValue: Double) = withContext(Dispatchers.IO) {
        accountSnapshotDao.insertSnapshot(AccountSnapshot(totalValue = totalValue))
        // Cleanup: Keep only last 30 snapshots
        accountSnapshotDao.deleteOldSnapshots(System.currentTimeMillis() - 30 * 24 * 3600 * 1000L)
    }

    private suspend fun generateContextualNews(stocks: List<StockPrice>) {
        val newsItems = mutableListOf<MarketNews>()
        stocks.forEach { stock ->
            val absChange = kotlin.math.abs(stock.dailyChangePct)
            if (absChange >= 1.5) { // Significant move threshold
                val sentiment = if (stock.dailyChangePct > 0) "BULLISH" else "BEARISH"
                val title = if (sentiment == "BULLISH") {
                    listOf(
                        "${stock.symbol} surges as buyers take control",
                        "Bullish momentum picks up for ${stock.symbol}",
                        "${stock.symbol} breaks resistance in latest rally"
                    ).random()
                } else {
                    listOf(
                        "${stock.symbol} faces selling pressure",
                        "Bearish clouds hover over ${stock.symbol}",
                        "${stock.symbol} tests key support levels"
                    ).random()
                }
                
                newsItems.add(MarketNews(
                    symbol = stock.symbol,
                    title = title,
                    summary = "The stock has moved ${String.format("%.2f", stock.dailyChangePct)}% in the current session as wiggles gravitate toward real-world anchors.",
                    sentiment = sentiment,
                    timestamp = System.currentTimeMillis()
                ))
            }
        }
        
        if (newsItems.isNotEmpty()) {
            marketNewsDao.insertNews(newsItems)
            
            // Clean up old news (keep only last 24 hours)
            marketNewsDao.deleteOldNews(System.currentTimeMillis() - 24 * 3600 * 1000L)
            
            // For Pro users, periodically refine news with AI
            val profile = userProfileDao.getUserProfile()
            if (profile?.isPremium == true) {
                refineNewsWithAi(newsItems)
            }
        }
    }

    private suspend fun refineNewsWithAi(news: List<MarketNews>) {
        // In a real implementation, this would call Gemini.
        // For now, we simulate the refinement with more professional strings.
        val refined = news.map { n ->
            if (n.sentiment == "BULLISH") {
                n.copy(
                    title = "[AI Refined] Institutional demand drives ${n.symbol} breakout",
                    summary = "Advanced sentiment analysis detects a strong bullish divergence as ${n.symbol} outperforms peers in the current anchored simulation cycle.",
                    isAiRefined = true
                )
            } else {
                n.copy(
                    title = "[AI Refined] Macro headwinds weigh on ${n.symbol} valuation",
                    summary = "Algorithmic scanning identifies short-term distribution patterns as ${n.symbol} drifts toward lower anchor support zones.",
                    isAiRefined = true
                )
            }
        }
        marketNewsDao.insertNews(refined)
    }

    suspend fun setWatchlistCompactMode(compact: Boolean) = withContext(Dispatchers.IO) {
        userProfileDao.updateWatchlistCompactMode(compact)
    }

    // Fetch and Sync Real-World News from Yahoo Finance
    suspend fun syncNewsFromYahoo(symbol: String) = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val yahooSymbol = getYahooSymbol(symbol)
            val url = "https://query2.finance.yahoo.com/v1/finance/search?q=$yahooSymbol&newsCount=5&quotesCount=0"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext
                
                val body = response.body?.string() ?: return@withContext
                val json = JSONObject(body)
                val newsArray = json.optJSONArray("news") ?: return@withContext
                
                val items = mutableListOf<MarketNews>()
                var totalBias = 0.0
                
                for (i in 0 until newsArray.length()) {
                    val obj = newsArray.getJSONObject(i)
                    val rawTitle = obj.optString("title")
                    val publisher = obj.optString("publisher")
                    
                    // Simple local sentiment scoring (Free Tier)
                    val titleLower = rawTitle.lowercase()
                    val bias = when {
                        titleLower.contains("buy") || titleLower.contains("surge") || titleLower.contains("bull") || titleLower.contains("growth") -> 0.4
                        titleLower.contains("sell") || titleLower.contains("drop") || titleLower.contains("bear") || titleLower.contains("fall") -> -0.4
                        else -> 0.0
                    }
                    totalBias += bias
                    
                    items.add(MarketNews(
                        symbol = symbol,
                        title = rawTitle,
                        summary = "Breaking report via $publisher.",
                        sentiment = if (bias > 0) "BULLISH" else if (bias < 0) "BEARISH" else "NEUTRAL",
                        source = mapPublisherToLocal(publisher, isIndianStockSymbol(symbol)),
                        url = obj.optString("link"),
                        timestamp = obj.optLong("providerPublishTime") * 1000L
                    ))
                }
                
                if (items.isNotEmpty()) {
                    marketNewsDao.insertNews(items)
                    // Update Stock Sentiment Bias for Option B influence
                    val avgBias = (totalBias / items.size).coerceIn(-1.0, 1.0)
                    stockPriceDao.updateStockSentimentBias(symbol, avgBias)
                    
                    // Pro AI Refinement
                    val profile = userProfileDao.getUserProfile()
                    if (profile?.isPremium == true) {
                        refineNewsWithGemini(symbol, items)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun mapPublisherToLocal(original: String, isIndian: Boolean): String {
        if (!isIndian) return original
        val upper = original.uppercase()
        // Expanded Brand Mapping for Indian Market Realism
        return when {
            upper.contains("YAHOO") || upper.contains("REUTERS") -> 
                listOf("CNBC Awaaz", "Zee News", "NDTV Profit").random()
            upper.contains("BLOOMBERG") || upper.contains("ZACKS") || upper.contains("BARRON") -> 
                listOf("ET Now", "Moneycontrol", "Mint").random()
            upper.contains("FORBES") || upper.contains("INVESTOR") -> 
                listOf("Business Standard", "Financial Express", "The Hindu").random()
            else -> original
        }
    }

    private suspend fun refineNewsWithGemini(symbol: String, news: List<MarketNews>) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) return

        try {
            val headlines = news.take(3).joinToString("\n") { "- ${it.title}" }
            // SENSATIONALIZED TV PROMPT
            val systemPrompt = """
                You are a senior editor at a leading Indian financial news channel (like CNBC Awaaz or Zee Business).
                Read these real-world headlines for $symbol and write a sensationalized, high-impact "BREAKING NEWS" brief.
                
                RULES:
                1. Use dramatic vocabulary: "ON FIRE", "CRASH FEARS", "RECOVERY MODE", "INSTITUTIONAL ATTACK".
                2. Keep it to exactly one punchy sentence.
                3. Provide a sentiment score from -1.0 (Total Panic) to 1.0 (Euphoria).
                
                Format: [DRAMATIC BRIEF] | [SCORE]
                
                Headlines:
                $headlines
            """.trimIndent()

            val client = OkHttpClient()
            val jsonBody = """
                {
                    "contents": [{
                        "parts": [{"text": "$systemPrompt"}]
                    }]
                }
            """.trimIndent()

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonBody.toRequestBody(mediaType)
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext
                    
                    val bodyString = response.body?.string() ?: ""
                    val matchResult = "\"text\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(bodyString)
                    val rawResponse = matchResult?.groupValues?.get(1) ?: return@withContext
                    
                    val parts = rawResponse.split("|")
                    if (parts.size >= 2) {
                        val aiBrief = parts[0].trim().replace("\\n", " ").replace("\\\"", "\"")
                        val aiScore = parts[1].trim().toDoubleOrNull() ?: 0.0
                        
                        // Upsert refined news
                        val refinedItem = MarketNews(
                            symbol = symbol,
                            title = aiBrief,
                            summary = "Exclusive AI-powered sentiment analysis from TradeLab Pro desk.",
                            sentiment = if (aiScore > 0.3) "BULLISH" else if (aiScore < -0.3) "BEARISH" else "NEUTRAL",
                            source = listOf("CNBC Awaaz", "Zee News", "NDTV Profit").random(),
                            isAiRefined = true,
                            timestamp = System.currentTimeMillis()
                        )
                        marketNewsDao.insertNews(listOf(refinedItem))
                        stockPriceDao.updateStockSentimentBias(symbol, aiScore)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Match Pending Orders
    suspend fun matchPendingOrders() = withContext(Dispatchers.IO) {
        val pending = pendingOrderDao.getPendingOrdersFlow().firstOrNull() ?: emptyList()
        val prices = stockPriceDao.getAllStockPricesFlow().firstOrNull() ?: emptyList()
        val profile = userProfileDao.getUserProfile() ?: return@withContext

        for (order in pending) {
            val stock = prices.find { it.symbol == order.symbol } ?: continue
            val convertedPrice = getConvertedStockPrice(stock.currentPrice, order.symbol, profile.currency)
            val triggerPrice = order.triggerPrice

            val shouldTrigger = when {
                order.type == "BUY" && (order.orderType == "Limit" || order.orderType == "GTT") -> convertedPrice <= triggerPrice
                order.type == "BUY" && order.orderType == "Stop-Loss" -> convertedPrice >= triggerPrice
                order.type == "SELL" && (order.orderType == "Limit" || order.orderType == "GTT") -> convertedPrice >= triggerPrice
                order.type == "SELL" && order.orderType == "Stop-Loss" -> convertedPrice <= triggerPrice
                else -> false
            }

            if (shouldTrigger) {
                val res = if (order.type == "BUY") {
                    buyStock(order.symbol, order.shares, order.isDelivery)
                } else {
                    sellStock(order.symbol, order.shares, order.isDelivery)
                }

                val sym = if (profile.currency == "INR") "₹" else "$"
                if (res.isSuccess) {
                    pendingOrderDao.updateOrderStatus(order.id, "EXECUTED")
                    addNotification("Order Executed: ${order.type} ${order.shares} shares of ${order.symbol} at $sym${String.format("%.2f", convertedPrice)} (Triggered by ${order.orderType} at $sym${String.format("%.2f", triggerPrice)})")
                    
                    // --- BRACKET ORDER LOGIC (OCO LEGS SPAWNING) ---
                    if (order.targetPrice != null || order.stopLossPrice != null) {
                        // This was a primary leg execution. Now spawn the target and stop-loss legs.
                        val parentId = order.id
                        if (order.targetPrice != null) {
                            pendingOrderDao.insertPendingOrder(
                                PendingOrder(
                                    symbol = order.symbol,
                                    type = if (order.type == "BUY") "SELL" else "BUY",
                                    orderType = "Limit",
                                    shares = order.shares,
                                    triggerPrice = order.targetPrice,
                                    isDelivery = order.isDelivery,
                                    parentOrderId = parentId
                                )
                            )
                        }
                        if (order.stopLossPrice != null) {
                            pendingOrderDao.insertPendingOrder(
                                PendingOrder(
                                    symbol = order.symbol,
                                    type = if (order.type == "BUY") "SELL" else "BUY",
                                    orderType = "Stop-Loss",
                                    shares = order.shares,
                                    triggerPrice = order.stopLossPrice,
                                    isDelivery = order.isDelivery,
                                    parentOrderId = parentId
                                )
                            )
                        }
                    }

                    // --- OCO CANCELLATION LOGIC ---
                    if (order.parentOrderId != null) {
                        // This was an OCO leg (Target or SL). Cancel all other legs with same parent.
                        val others = pending.filter { it.parentOrderId == order.parentOrderId && it.id != order.id }
                        for (other in others) {
                            pendingOrderDao.updateOrderStatus(other.id, "CANCELLED")
                        }
                    }

                } else {
                    pendingOrderDao.updateOrderStatus(order.id, "CANCELLED")
                    addNotification("Order Failed: ${order.type} ${order.shares} shares of ${order.symbol} due to insufficient resources.")
                }
            }
        }
    }

    // Multi-Watchlist management methods
    fun getWatchlistItemsFlow(watchlistId: Int): Flow<List<WatchlistItemV2>> {
        return watchlistV2Dao.getWatchlistItemsFlow(watchlistId)
    }

    suspend fun renameWatchlist(watchlistId: Int, newName: String) = withContext(Dispatchers.IO) {
        watchlistV2Dao.insertWatchlistName(WatchlistName(watchlistId, newName))
    }

    suspend fun addWatchlistItemV2(watchlistId: Int, symbol: String) = withContext(Dispatchers.IO) {
        watchlistV2Dao.insertWatchlistItemV2(WatchlistItemV2(watchlistId, symbol))
    }

    suspend fun removeWatchlistItemV2(watchlistId: Int, symbol: String) = withContext(Dispatchers.IO) {
        watchlistV2Dao.deleteWatchlistItemV2(watchlistId, symbol)
    }

    suspend fun isWatchlistedV2(watchlistId: Int, symbol: String): Boolean = withContext(Dispatchers.IO) {
        watchlistV2Dao.isWatchlistedV2(watchlistId, symbol)
    }

    suspend fun addNewWatchlist(name: String): Result<Int> = withContext(Dispatchers.IO) {
        val currentNames = watchlistV2Dao.getWatchlistNamesFlow().firstOrNull() ?: emptyList()
        val profile = userProfileDao.getUserProfile()
        val isPremium = profile?.isPremium == true
        val maxAllowed = if (isPremium) 10 else 5
        if (currentNames.size >= maxAllowed) {
            return@withContext Result.failure(Exception("Maximum of $maxAllowed watchlists allowed. Go Pro for up to 10!"))
        }
        val existingIds = currentNames.map { it.id }.toSet()
        val nextId = (1..maxAllowed).firstOrNull { it !in existingIds } ?: return@withContext Result.failure(Exception("Maximum of $maxAllowed watchlists allowed"))
        watchlistV2Dao.insertWatchlistName(WatchlistName(nextId, name))
        Result.success(nextId)
    }

    suspend fun deleteWatchlist(id: Int) = withContext(Dispatchers.IO) {
        watchlistV2Dao.deleteWatchlistName(id)
        watchlistV2Dao.deleteWatchlistItemsByWatchlistId(id)
    }

    // Market Ticker & Industry Mapping
    private val TICKER_INDUSTRY_MAP = mapOf(
        "RELIANCE" to "Energy & Petrochemicals",
        "TCS" to "IT Services",
        "INFY" to "IT Services",
        "HDFCBANK" to "Banking & Finance",
        "ICICIBANK" to "Banking & Finance",
        "SBIN" to "Banking & Finance",
        "BHARTIARTL" to "Telecommunications",
        "ITC" to "FMCG & Consumer Goods",
        "WIPRO" to "IT Services",
        "HINDUNILVR" to "FMCG & Consumer Goods",
        "TATAMOTORS" to "Automotive",
        "TATASTEEL" to "Metals & Mining",
        "AAPL" to "Technology",
        "TSLA" to "Automotive & Energy",
        "MSFT" to "Technology",
        "BTC-USD" to "Cryptocurrency",
        "ETH-USD" to "Cryptocurrency",
        "MCX_GOLD" to "Commodities",
        "MCX_CRUDE" to "Commodities"
    )

    fun getIndustryForSymbol(symbol: String): String {
        val clean = symbol.substringBefore(".NS").substringBefore(".BO").uppercase()
        return TICKER_INDUSTRY_MAP[clean] ?: "Diversified"
    }

    fun getSymbolVolatility(symbol: String): Double {
        val upper = symbol.uppercase().trim()
        return when {
            upper.startsWith("MCX_") || upper.startsWith("GLOBAL_") -> 0.8
            upper == "BTC" || upper == "ETH" || upper.endsWith("-USD") -> 1.5
            else -> 0.3
        }
    }

    fun isCommoditySymbol(symbol: String): Boolean {
        val upper = symbol.uppercase().trim()
        return upper.startsWith("MCX_") || upper.startsWith("GLOBAL_")
    }

    // Pending Orders management methods
    suspend fun insertPendingOrder(order: PendingOrder) = withContext(Dispatchers.IO) {
        pendingOrderDao.insertPendingOrder(order)
    }

    suspend fun deletePendingOrder(id: Int) = withContext(Dispatchers.IO) {
        pendingOrderDao.deletePendingOrder(id)
    }

    // App Notifications management methods
    suspend fun addNotification(message: String) = withContext(Dispatchers.IO) {
        appNotificationDao.insertNotification(AppNotification(message = message))
    }

    suspend fun markNotificationAsRead(id: Int) = withContext(Dispatchers.IO) {
        appNotificationDao.markAsRead(id)
    }

    suspend fun clearNotifications() = withContext(Dispatchers.IO) {
        appNotificationDao.clearAll()
    }

    // Trial Actions Counter & Gate Management
    suspend fun incrementTrialActions(): Boolean = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext false
        if (profile.isLoggedIn) return@withContext false

        val nextCount = profile.trialActionsCount + 1
        userProfileDao.insertProfile(profile.copy(trialActionsCount = nextCount))
        
        return@withContext nextCount >= 7
    }

    suspend fun registerOrLogin(userName: String, userEmail: String, phoneNumber: String = "") = withContext(Dispatchers.IO) {
        val existingProfile = userProfileDao.getUserProfile()
        val uniqueId = existingProfile?.userUniqueId?.ifBlank { UUID.randomUUID().toString().take(8).uppercase() } ?: UUID.randomUUID().toString().take(8).uppercase()
        
        if (existingProfile == null) {
            userProfileDao.insertProfile(
                UserProfile(
                    id = 1,
                    cash = 25000.0,
                    startingCash = 25000.0,
                    riskPreference = "Moderate",
                    isLoggedIn = true,
                    userName = userName,
                    userEmail = userEmail,
                    phoneNumber = phoneNumber,
                    userUniqueId = uniqueId,
                    trialActionsCount = 0
                )
            )
        } else {
            userProfileDao.insertProfile(
                existingProfile.copy(
                    isLoggedIn = true,
                    userName = userName.ifBlank { existingProfile.userName },
                    userEmail = userEmail.ifBlank { existingProfile.userEmail },
                    phoneNumber = phoneNumber.ifBlank { existingProfile.phoneNumber },
                    userUniqueId = uniqueId,
                    trialActionsCount = 0
                )
            )
        }
    }

    suspend fun updateUserProfile(name: String, email: String, phone: String) = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext
        userProfileDao.insertProfile(
            profile.copy(
                userName = name,
                userEmail = email,
                phoneNumber = phone
            )
        )
    }

    suspend fun purchasePremium() = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext
        userProfileDao.insertProfile(profile.copy(isPremium = true))
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext
        userProfileDao.insertProfile(
            profile.copy(
                isLoggedIn = false,
                userName = "",
                userEmail = "",
                trialActionsCount = 0,
                isPremium = false
            )
        )
    }

    suspend fun earnBrokerageCredits(amount: Int) = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext
        userProfileDao.insertProfile(profile.copy(brokerageCredits = profile.brokerageCredits + amount))
    }

    suspend fun earnEmergencyCash(amount: Double) = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext
        val newCash = profile.cash + amount
        userProfileDao.insertProfile(profile.copy(cash = newCash))
        recordLedgerEntry("Emergency Capital Recharge", "CREDIT", amount, newCash)
    }

    suspend fun earnAiAuditCredit() = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext
        userProfileDao.insertProfile(profile.copy(aiAuditCredits = profile.aiAuditCredits + 1))
    }

    suspend fun useAiAuditCredit(): Boolean = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext false
        if (profile.aiAuditCredits > 0) {
            userProfileDao.insertProfile(profile.copy(aiAuditCredits = profile.aiAuditCredits - 1))
            true
        } else {
            false
        }
    }

    suspend fun exitOptionPosition(symbol: String, shares: Double): Result<String> = withContext(Dispatchers.IO) {
        val optionContract = optionContractDao.getContract(symbol)
        val stock = stockPriceDao.getStockPrice(symbol)
        val holding = holdingDao.getHolding(symbol, true) ?: holdingDao.getHolding(symbol, false)
        if (holding == null || stock == null) {
            return@withContext Result.failure(Exception("Option position not found"))
        }
        val profile = userProfileDao.getUserProfile() ?: return@withContext Result.failure(Exception("Profile not found"))

        val totalShares = holding.shares + holding.sharesT1
        val exitShares = minOf(shares, totalShares)
        val currentPremium = stock.currentPrice
        val costBasis = holding.averagePrice
        val pnl = exitShares * (currentPremium - costBasis)
        val cashCredit = exitShares * currentPremium

        val updatedProfile = profile.copy(cash = profile.cash + cashCredit)
        userProfileDao.insertProfile(updatedProfile)

        val remainingShares = totalShares - exitShares
        if (remainingShares < 0.0001) {
            holdingDao.deleteHolding(holding)
            if (optionContract != null) {
                optionContractDao.deactivateContract(optionContract.symbol)
            }
        } else {
            holdingDao.insertHolding(holding.copy(shares = remainingShares))
        }

        val breakdown = "F&O EXIT $symbol\n" +
                        "Entry: ${String.format("%.2f", costBasis)}\n" +
                        "Exit: ${String.format("%.2f", currentPremium)}\n" +
                        "P&L: ${String.format("%.2f", pnl)}"
        recordLedgerEntry(breakdown, "CREDIT", cashCredit, updatedProfile.cash, symbol, null)

        if (pnl >= 0) addNotification("Option position closed: $symbol profit +${String.format("%.2f", pnl)}")
        else addNotification("Option position closed: $symbol loss ${String.format("%.2f", pnl)}")

        Result.success("Closed $exitShares shares of $symbol")
    }

    suspend fun exitAllFnoPositions(): Result<String> = withContext(Dispatchers.IO) {
        val holdings = holdingDao.getAllHoldings()
        val optionHoldings = holdings.filter { it.symbol.contains("_CE_") || it.symbol.contains("_PE_") }
        if (optionHoldings.isEmpty()) return@withContext Result.failure(Exception("No F&O positions to close"))
        var closedCount = 0
        for (h in optionHoldings) {
            val totalShares = h.shares + h.sharesT1
            val result = exitOptionPosition(h.symbol, totalShares)
            if (result.isSuccess) closedCount++
        }
        Result.success("Closed $closedCount F&O positions")
    }

    private fun calculateNextMarketClose(from: Long): Long {
        val tz = TimeZone.getTimeZone("Asia/Kolkata")
        val cal = Calendar.getInstance(tz)
        cal.timeInMillis = from

        // If it's already past 3:30 PM today, start looking from tomorrow
        if (cal.get(Calendar.HOUR_OF_DAY) > 15 || (cal.get(Calendar.HOUR_OF_DAY) == 15 && cal.get(Calendar.MINUTE) >= 30)) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Set to 3:30 PM
        cal.set(Calendar.HOUR_OF_DAY, 15)
        cal.set(Calendar.MINUTE, 30)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // Skip Weekends and Holidays
        while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || 
               cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || 
               isIndianMarketHoliday(cal)) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return cal.timeInMillis
    }

    suspend fun unlockIntradaySession() = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext
        val now = System.currentTimeMillis()
        
        val newExpiry = if (profile.leverageUnlockedUntil > now) {
            // Already active, stack the NEXT market day
            calculateNextMarketClose(profile.leverageUnlockedUntil + 1000L)
        } else {
            // Expired, unlock for the current/upcoming session
            calculateNextMarketClose(now)
        }
        
        userProfileDao.insertProfile(profile.copy(leverageUnlockedUntil = newExpiry))
    }

    // Auto Square-off MIS Positions (Simulated at 3:20 PM IST)
    suspend fun checkAutoSquareOff() = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        if (hour == 15 && minute >= 20 && minute < 30) {
            val allHoldings = holdingDao.getAllHoldings()
            val misPositions = allHoldings.filter { !it.isDelivery }
            if (misPositions.isNotEmpty()) {
                addNotification("🕒 MIS Auto Square-off active (3:20 PM IST). Squaring off ${misPositions.size} intraday positions.")
                for (holding in misPositions) {
                    val totalShares = holding.shares + holding.sharesT1
                    if (totalShares > 0) {
                        sellStock(holding.symbol, totalShares, isDelivery = false)
                    } else if (totalShares < 0) {
                        buyStock(holding.symbol, kotlin.math.abs(totalShares), isDelivery = false)
                    }
                }
            }
        }
    }

    suspend fun unlockPremiumIndicators(durationHours: Int) = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext
        val expiryTime = System.currentTimeMillis() + (durationHours * 60 * 60 * 1000L)
        userProfileDao.insertProfile(profile.copy(indicatorsUnlockedUntil = expiryTime))
    }

    suspend fun earnFnoTokens(amount: Int) = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext
        userProfileDao.insertProfile(profile.copy(fnoTokens = profile.fnoTokens + amount))
    }

    suspend fun useFnoToken(): Boolean = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext false
        if (profile.fnoTokens > 0) {
            userProfileDao.insertProfile(profile.copy(fnoTokens = profile.fnoTokens - 1))
            true
        } else {
            false
        }
    }

    suspend fun insertOrUpdateOptionPrice(optionSymbol: String, underlyingPrice: Double, strike: Double, isCall: Boolean) = withContext(Dispatchers.IO) {
        val premium = calculateOptionPremium(underlyingPrice, strike, isCall, 7)
        val existing = stockPriceDao.getStockPrice(optionSymbol)
        
        val cleanUnderlying = optionSymbol.substringBefore("_")
        val companyName = "$cleanUnderlying " + (if (isCall) "Call" else "Put") + " Option (${strike})"
        val newStockPrice = StockPrice(
            symbol = optionSymbol,
            companyName = companyName,
            currentPrice = premium,
            dailyChangePct = existing?.dailyChangePct ?: 0.0,
            previousClose = existing?.previousClose ?: premium,
            highPrice = existing?.highPrice ?: premium,
            lowPrice = existing?.lowPrice ?: premium,
            historyData = existing?.historyData ?: "$premium,$premium"
        )
        stockPriceDao.insertStockPrices(listOf(newStockPrice))
    }

    suspend fun acceptSimDisclaimer() = withContext(Dispatchers.IO) {
        userProfileDao.updateSimDisclaimer(true)
    }

    suspend fun updateUserStreak() = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext
        val now = System.currentTimeMillis()
        val lastActive = profile.lastActiveTimestamp

        val calNow = Calendar.getInstance()
        calNow.timeInMillis = now
        val calLast = Calendar.getInstance()
        calLast.timeInMillis = lastActive

        val dayNow = calNow.get(Calendar.DAY_OF_YEAR)
        val yearNow = calNow.get(Calendar.YEAR)
        val dayLast = calLast.get(Calendar.DAY_OF_YEAR)
        val yearLast = calLast.get(Calendar.YEAR)

        if (yearNow == yearLast && dayNow == dayLast) {
            // Already active today, do nothing
            return@withContext
        }

        // --- T+1 SETTLEMENT LOGIC ---
        // Since it's a new day, move all sharesT1 to settled shares
        val currentHoldings = holdingDao.getAllHoldings()
        currentHoldings.forEach { h ->
            if (h.sharesT1 > 0) {
                holdingDao.insertHolding(
                    h.copy(
                        shares = h.shares + h.sharesT1,
                        sharesT1 = 0.0
                    )
                )
            }
        }

        val isConsecutive = if (yearNow == yearLast) {
            dayNow == dayLast + 1
        } else if (yearNow == yearLast + 1) {
            dayLast >= 365 && dayNow == 1 // Simple leap year check fallback
        } else {
            false
        }

        val newStreak = if (isConsecutive) profile.dailyStreak + 1 else 1
        
        // Update Discipline Score for patience/daily check
        val newScore = disciplineCalculator.calculateNewScore(profile, currentHoldings, 0.0, profile.cash + currentHoldings.sumOf { h -> 
            val s = stockPriceDao.getStockPrice(h.symbol)
            val p = s?.currentPrice ?: h.averagePrice
            getConvertedStockPrice((h.shares + h.sharesT1) * p, h.symbol, profile.currency)
        })
        val badges = disciplineCalculator.evaluateBadges(newScore, currentHoldings).joinToString(",")

        userProfileDao.insertProfile(
            profile.copy(
                dailyStreak = newStreak,
                lastActiveTimestamp = now,
                xp = profile.xp + (newStreak * 50), // Bonus XP for streaks
                disciplineScore = newScore,
                activeBadges = badges
            )
        )
    }

    suspend fun addXp(amount: Int) = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext
        userProfileDao.insertProfile(profile.copy(xp = profile.xp + amount))
    }

    private suspend fun recordLedgerEntry(
        description: String,
        type: String,
        amount: Double,
        runningBalance: Double,
        symbol: String? = null,
        refId: Int? = null
    ) {
        ledgerDao.insertLedgerEntry(
            LedgerEntry(
                description = description,
                type = type,
                amount = amount,
                runningBalance = runningBalance,
                symbol = symbol,
                refId = refId
            )
        )
    }

    suspend fun updateShieldDialogPreference(show: Boolean) = withContext(Dispatchers.IO) {
        userProfileDao.updateShieldDialogPreference(show)
    }
}
