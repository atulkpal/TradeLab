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
    private val database: AppDatabase
) {

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

    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfileFlow()
    val holdings: Flow<List<Holding>> = holdingDao.getAllHoldingsFlow()
    val transactions: Flow<List<Transaction>> = transactionDao.getAllTransactionsFlow()
    val watchlist: Flow<List<WatchlistItem>> = watchlistDao.getWatchlistFlow()
    val stockPrices: Flow<List<StockPrice>> = stockPriceDao.getAllStockPricesFlow()

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
            val initialStocks = listOf(
                StockPrice("RELIANCE", "Reliance Industries Ltd", 2950.50, 1.25, 2914.00, 2975.00, 2905.00, "2850.0,2880.5,2870.0,2920.0,2914.0,2950.50"),
                StockPrice("TCS", "Tata Consultancy Services", 3850.20, -0.80, 3881.30, 3910.00, 3825.00, "3780.0,3815.0,3840.0,3890.0,3881.3,3850.20"),
                StockPrice("INFY", "Infosys Limited", 1510.40, 0.45, 1503.60, 1525.00, 1495.00, "1475.0,1490.0,1485.0,1512.0,1503.6,1510.40"),
                StockPrice("HDFCBANK", "HDFC Bank Limited", 1610.10, 1.10, 1592.50, 1622.00, 1585.00, "1560.0,1575.5,1570.0,1595.0,1592.5,1610.10"),
                StockPrice("ICICIBANK", "ICICI Bank Limited", 1120.50, 0.85, 1111.00, 1130.00, 1110.00, "1090.0,1105.0,1100.0,1115.0,1111.0,1120.50"),
                StockPrice("SBIN", "State Bank of India", 785.40, 1.45, 774.20, 792.00, 770.00, "750.0,762.0,758.0,776.0,774.2,785.40"),
                StockPrice("BHARTIARTL", "Bharti Airtel Limited", 1385.20, -0.30, 1389.40, 1398.00, 1375.00, "1350.0,1368.0,1365.0,1392.0,1389.4,1385.20"),
                StockPrice("ITC", "ITC Limited", 432.10, 0.65, 429.30, 435.00, 427.00, "418.0,422.0,420.5,431.0,429.3,432.10"),
                StockPrice("WIPRO", "Wipro Limited", 482.50, -0.90, 486.90, 491.00, 480.00, "470.0,478.0,475.0,488.0,486.9,482.50"),
                StockPrice("HINDUNILVR", "Hindustan Unilever Ltd", 2465.00, 0.50, 2452.70, 2480.00, 2445.00, "2410.0,2432.0,2425.0,2458.0,2452.7,2465.00"),
                StockPrice("TATAMOTORS", "Tata Motors Limited", 965.80, 2.10, 946.00, 974.00, 940.00, "910.0,928.0,922.0,948.0,946.0,965.80"),
                StockPrice("TATASTEEL", "Tata Steel Limited", 165.40, -0.55, 166.30, 167.10, 164.20, "161.2,163.4,162.8,165.1,166.3,165.40"),
                StockPrice("LICI", "Life Insurance Corp of India", 975.30, 0.80, 967.50, 982.00, 961.00, "945.0,958.0,955.0,970.0,967.5,975.30"),
                StockPrice("LT", "Larsen & Toubro Limited", 3550.00, 1.15, 3509.60, 3580.00, 3495.00, "3420.0,3450.0,3480.0,3520.0,3509.6,3550.00"),
                StockPrice("KOTAKBANK", "Kotak Mahindra Bank", 1740.00, -0.40, 1747.00, 1758.00, 1725.00, "1710.0,1735.0,1730.0,1750.0,1747.0,1740.00"),
                StockPrice("AXISBANK", "Axis Bank Limited", 1050.40, 0.95, 1040.50, 1060.00, 1035.00, "1010.0,1025.0,1020.0,1045.0,1040.5,1050.40"),
                StockPrice("ASIANPAINT", "Asian Paints Limited", 2850.00, -1.20, 2884.60, 2900.00, 2835.00, "2810.0,2840.0,2830.0,2890.0,2884.6,2850.00"),
                StockPrice("M&M", "Mahindra & Mahindra Ltd", 1980.50, 1.65, 1948.30, 1995.00, 1935.00, "1900.0,1925.0,1915.0,1955.0,1948.3,1980.50"),
                StockPrice("ADANIENT", "Adani Enterprises Ltd", 3250.00, 2.45, 3172.30, 3280.00, 3150.00, "3050.0,3120.0,3100.0,3195.0,3172.3,3250.00"),
                StockPrice("SUNPHARMA", "Sun Pharmaceutical Industries", 1540.00, 0.55, 1531.60, 1555.00, 1520.00, "1490.0,1515.0,1510.0,1535.0,1531.6,1540.00"),
                StockPrice("JSWSTEEL", "JSW Steel Limited", 810.00, -0.65, 815.30, 822.00, 804.00, "785.0,802.0,798.0,818.0,815.3,810.00"),
                StockPrice("ONGC", "Oil & Natural Gas Corp", 275.00, 1.85, 270.00, 278.50, 268.20, "260.0,265.5,263.0,271.5,270.0,275.00"),
                StockPrice("COALINDIA", "Coal India Limited", 450.00, -0.35, 451.60, 456.00, 447.50, "438.0,444.0,442.5,453.0,451.6,450.00"),
                StockPrice("NTPC", "NTPC Limited", 345.00, 1.35, 340.40, 348.00, 338.00, "325.0,332.0,330.5,342.0,340.4,345.00"),
                StockPrice("POWERGRID", "Power Grid Corp of India", 285.00, 0.70, 283.00, 288.50, 281.00, "272.0,278.0,275.5,284.5,283.0,285.00"),
                StockPrice("ULTRACEMCO", "UltraTech Cement Limited", 9850.00, 1.45, 9709.20, 9920.00, 9680.00, "9450.0,9620.0,9580.0,9740.0,9709.2,9850.00"),
                StockPrice("BAJFINANCE", "Bajaj Finance Limited", 6850.00, -1.15, 6929.70, 6970.00, 6810.00, "6720.0,6840.0,6800.0,6960.0,6929.7,6850.00"),
                StockPrice("HINDALCO", "Hindalco Industries Ltd", 580.00, 0.90, 574.80, 586.00, 571.20, "552.0,564.0,561.5,576.0,574.8,580.00"),
                StockPrice("HCLTECH", "HCL Technologies Limited", 1620.00, 0.40, 1613.50, 1635.00, 1602.00, "1570.0,1595.0,1590.0,1618.0,1613.5,1620.00"),
                StockPrice("TECHM", "Tech Mahindra Limited", 1250.00, -0.60, 1257.50, 1268.00, 1241.00, "1210.0,1235.0,1230.0,1262.0,1257.5,1250.00"),
                StockPrice("LTIM", "LTIMindtree Limited", 5200.00, 0.85, 5156.20, 5240.00, 5120.00, "5020.0,5090.0,5060.0,5180.0,5156.2,5200.00"),
                StockPrice("COFORGE", "Coforge Limited", 5800.00, -1.45, 5885.30, 5920.00, 5750.00, "5650.0,5780.0,5730.0,5910.0,5885.3,5800.00"),
                StockPrice("PERSISTENT", "Persistent Systems Limited", 3900.00, 1.25, 3851.85, 3930.00, 3820.00, "3720.0,3790.0,3760.0,3870.0,3851.85,3900.00"),
                StockPrice("MPHASIS", "Mphasis Limited", 2400.00, -0.50, 2412.10, 2435.00, 2382.00, "2320.0,2370.0,2350.0,2420.0,2412.1,2400.00"),
                StockPrice("KPITTECH", "KPIT Technologies Ltd", 1450.00, 2.15, 1419.50, 1468.00, 1405.00, "1360.0,1395.0,1380.0,1425.0,1419.5,1450.00"),
                StockPrice("INDUSINDBK", "IndusInd Bank Limited", 1480.00, -0.90, 1493.45, 1505.00, 1468.00, "1430.0,1465.0,1450.0,1498.0,1493.45,1480.00"),
                StockPrice("BOB", "Bank of Baroda", 250.00, 1.80, 245.55, 253.50, 243.20, "235.0,241.0,239.5,247.0,245.55,250.00"),
                StockPrice("CANBK", "Canara Bank", 115.00, 0.75, 114.15, 116.50, 113.20, "108.0,111.5,110.8,114.5,114.15,115.00"),
                StockPrice("PNB", "Punjab National Bank", 125.00, 2.10, 122.45, 126.80, 121.10, "115.0,119.2,118.0,123.0,122.45,125.00"),
                StockPrice("FEDERALBNK", "Federal Bank Limited", 155.00, -0.45, 155.70, 157.50, 153.80, "148.0,152.0,151.5,156.2,155.7,155.00"),
                StockPrice("IDFCFIRSTB", "IDFC First Bank Limited", 82.00, 0.55, 81.55, 83.20, 81.00, "78.2,80.4,79.8,81.9,81.55,82.00"),
                StockPrice("BANDHANBNK", "Bandhan Bank Limited", 190.00, -1.25, 192.40, 194.50, 188.20, "182.0,188.5,186.0,193.5,192.4,190.00"),
                StockPrice("YESBANK", "Yes Bank Limited", 24.50, 1.65, 24.10, 24.95, 23.85, "22.5,23.6,23.1,24.3,24.1,24.50"),
                StockPrice("MCX", "Multi Commodity Exchange", 3600.00, 2.50, 3512.20, 3645.00, 3495.00, "3380.0,3450.0,3420.0,3530.0,3512.2,3600.00"),
                StockPrice("RECLTD", "REC Limited", 480.00, 3.10, 465.55, 486.20, 461.50, "442.0,455.0,450.5,468.0,465.55,480.00"),
                StockPrice("PFC", "Power Finance Corporation", 420.00, 1.85, 412.35, 424.80, 408.20, "392.0,402.5,399.0,414.5,412.35,420.00"),
                StockPrice("LICHSGFIN", "LIC Housing Finance Ltd", 620.00, -0.80, 625.00, 631.50, 615.20, "595.0,612.0,608.0,627.0,625.0,620.00"),
                StockPrice("HDFCLIFE", "HDFC Life Insurance Co", 580.00, 0.45, 577.40, 584.50, 572.10, "555.0,568.0,565.5,579.0,577.4,580.00"),
                StockPrice("SBILIFE", "SBI Life Insurance Co", 1450.00, 1.25, 1432.10, 1462.00, 1422.00, "1380.0,1415.0,1408.0,1438.0,1432.1,1450.00"),
                StockPrice("BPCL", "Bharat Petroleum Corp", 610.00, -1.10, 616.80, 622.50, 604.80, "585.0,602.0,598.0,619.0,616.8,610.00"),
                StockPrice("IOC", "Indian Oil Corporation", 165.00, 0.90, 163.50, 166.80, 161.20, "155.0,160.2,158.5,164.2,163.5,165.00"),
                StockPrice("GAIL", "GAIL (India) Limited", 180.00, 1.45, 177.40, 182.50, 175.80, "168.0,173.5,171.2,178.5,177.4,180.00"),
                StockPrice("ADANIGREEN", "Adani Green Energy Ltd", 1650.00, 2.10, 1616.10, 1675.00, 1602.00, "1540.0,1585.0,1570.0,1628.0,1616.1,1650.00"),
                StockPrice("ADANIPOWER", "Adani Power Limited", 580.00, 1.65, 570.55, 587.50, 565.20, "542.0,558.0,553.5,574.0,570.55,580.00"),
                StockPrice("TATAPOWER", "Tata Power Company Ltd", 390.00, 0.85, 386.70, 394.50, 382.10, "365.0,378.0,375.2,389.2,386.7,390.00"),
                StockPrice("NHPC", "NHPC Limited", 90.00, 2.25, 88.00, 91.40, 87.25, "82.5,85.4,84.6,88.9,88.0,90.00"),
                StockPrice("SJVN", "SJVN Limited", 120.00, 1.85, 117.80, 122.40, 115.50, "108.5,112.4,111.0,118.9,117.8,120.00"),
                StockPrice("IREDA", "IREDA Limited", 170.00, 4.25, 163.05, 174.50, 160.50, "145.0,155.5,152.0,165.4,163.05,170.00"),
                StockPrice("BAJAJ-AUTO", "Bajaj Auto Limited", 8300.00, -0.65, 8354.30, 8420.00, 8240.00, "8120.0,8240.0,8200.0,8390.0,8354.3,8300.00"),
                StockPrice("HEROMOTOCO", "Hero MotoCorp Limited", 4400.00, 0.45, 4380.30, 4440.00, 4345.00, "4250.0,4320.0,4300.0,4395.0,4380.3,4400.00"),
                StockPrice("EICHERMOT", "Eicher Motors Limited", 3850.00, 1.25, 3802.45, 3885.00, 3770.00, "3680.0,3750.0,3720.0,3835.0,3802.45,3850.00"),
                StockPrice("ASHOKLEY", "Ashok Leyland Limited", 175.00, -0.85, 176.50, 178.20, 173.10, "168.0,172.5,171.0,177.4,176.5,175.00"),
                StockPrice("TVSMOTOR", "TVS Motor Company Ltd", 2100.00, 1.50, 2068.95, 2125.00, 2050.00, "1980.0,2020.0,2005.0,2085.0,2068.95,2100.00"),
                StockPrice("BHEL", "Bharat Heavy Electricals", 225.00, 2.45, 219.60, 228.40, 217.50, "205.0,212.4,210.0,221.8,219.6,225.00"),
                StockPrice("HAL", "Hindustan Aeronautics Ltd", 3100.00, 3.50, 2995.15, 3145.00, 2975.00, "2850.0,2920.0,2890.0,3025.0,2995.15,3100.00"),
                StockPrice("BEL", "Bharat Electronics Ltd", 195.00, 1.15, 192.75, 198.50, 190.20, "182.0,188.5,185.0,194.2,192.75,195.00"),
                StockPrice("ADANIENTS", "Adani Enterprises Ltd", 3220.00, 1.45, 3173.95, 3255.00, 3150.00, "3040.0,3120.0,3090.0,3198.0,3173.95,3220.00"),
                StockPrice("VEDL", "Vedanta Limited", 280.00, -0.90, 282.55, 286.40, 277.10, "268.0,274.5,272.0,283.8,282.55,280.00"),
                StockPrice("NMDC", "NMDC Limited", 230.00, 1.65, 226.25, 233.50, 224.10, "215.0,221.4,219.0,228.5,226.25,230.00"),
                StockPrice("SAIL", "Steel Authority of India", 135.00, -0.45, 135.60, 137.40, 133.50, "128.0,132.4,131.5,136.2,135.6,135.00"),
                StockPrice("NATIONALUM", "National Aluminium Co", 155.00, 1.25, 153.08, 157.40, 151.20, "142.0,148.5,145.8,154.2,153.08,155.00"),
                StockPrice("NESTLEIND", "Nestle India Limited", 2500.00, -0.50, 2512.55, 2530.00, 2482.00, "2420.0,2470.0,2450.0,2520.0,2512.55,2500.00"),
                StockPrice("BRITANNIA", "Britannia Industries Ltd", 4900.00, 0.85, 4858.70, 4945.00, 4820.00, "4710.0,4785.0,4760.0,4890.0,4858.7,4900.00"),
                StockPrice("TATACONSUM", "Tata Consumer Products", 1150.00, 1.10, 1137.50, 1162.00, 1128.00, "1090.0,1115.0,1108.0,1142.0,1137.5,1150.00"),
                StockPrice("VBL", "Varun Beverages Limited", 1400.00, 2.35, 1367.85, 1422.00, 1355.00, "1310.0,1345.0,1330.0,1382.0,1367.85,1400.00"),
                StockPrice("GODREJCP", "Godrej Consumer Products", 1220.00, -0.45, 1225.50, 1238.00, 1210.00, "1180.0,1205.0,1198.0,1232.0,1225.5,1220.00"),
                StockPrice("DABUR", "Dabur India Limited", 530.00, 0.65, 526.55, 534.20, 522.10, "508.0,516.5,514.0,529.5,526.55,530.00"),
                StockPrice("MARICO", "Marico Limited", 510.00, -0.30, 511.55, 515.50, 506.20, "495.0,502.5,500.0,513.5,511.55,510.00"),
                StockPrice("COLPAL", "Colgate-Palmolive (India)", 2550.00, 0.95, 2525.95, 2580.00, 2512.00, "2450.0,2495.0,2480.0,2542.0,2525.95,2550.00"),
                StockPrice("MCDOWELL-N", "United Spirits Limited", 1100.00, 1.25, 1086.40, 1115.00, 1078.00, "1050.0,1072.0,1065.0,1095.0,1086.4,1100.00"),
                StockPrice("CIPLA", "Cipla Limited", 1450.00, 0.45, 1435.15, 1462.00, 1421.00, "1380.0,1415.0,1408.0,1445.0,1435.15,1450.00"),
                StockPrice("DRREDDY", "Dr Reddy's Laboratories", 6100.00, -1.25, 6177.20, 6215.00, 6048.00, "5950.0,6080.0,6040.0,6210.0,6177.2,6100.00"),
                StockPrice("APOLLOHOSP", "Apollo Hospitals Enterprise", 6150.00, 0.85, 6098.15, 6220.00, 6050.00, "5890.0,6020.0,5980.0,6140.0,6098.15,6150.00"),
                StockPrice("DIVISLAB", "Divi's Laboratories Ltd", 3500.00, 1.45, 3449.95, 3540.00, 3422.00, "3310.0,3385.0,3360.0,3480.0,3449.95,3500.00"),
                StockPrice("LUPIN", "Lupin Limited", 1600.00, -0.65, 1610.45, 1625.00, 1585.00, "1520.0,1575.0,1560.0,1618.0,1610.45,1600.00"),
                StockPrice("AUROPHARMA", "Aurobindo Pharma Ltd", 1050.00, 1.15, 1038.05, 1062.00, 1025.00, "992.0,1015.0,1008.0,1044.0,1038.05,1050.00"),
                StockPrice("MAXHEALTH", "Max Healthcare Institute", 780.00, 1.85, 765.85, 792.00, 758.00, "725.0,748.0,742.0,774.0,765.85,780.00"),
                StockPrice("BIOCON", "Biocon Limited", 265.00, -1.10, 267.95, 271.20, 262.10, "252.0,261.4,258.9,269.4,267.95,265.00"),
                StockPrice("GRASIM", "Grasim Industries Ltd", 2200.00, 0.95, 2179.30, 2225.00, 2162.00, "2110.0,2145.0,2130.0,2192.0,2179.3,2200.00"),
                StockPrice("AMBUJACEM", "Ambuja Cements Limited", 600.00, 1.45, 591.40, 608.50, 588.00, "568.0,582.0,578.5,594.5,591.4,600.00"),
                StockPrice("ACC", "ACC Limited", 2500.00, -0.50, 2512.55, 2535.00, 2480.00, "2410.0,2465.0,2442.0,2518.0,2512.55,2500.00"),
                StockPrice("SHREECEM", "Shree Cement Limited", 26000.00, 1.10, 25717.10, 26250.00, 25510.00, "24950.0,25420.0,25280.0,25840.0,25717.1,26000.00"),
                StockPrice("DLF", "DLF Limited", 850.00, 2.25, 831.30, 862.00, 825.00, "795.0,818.0,812.0,844.0,831.3,850.00"),
                StockPrice("LODHA", "Macrotech Developers Ltd", 1050.00, 1.35, 1036.00, 1065.00, 1022.00, "992.0,1015.0,1008.0,1042.0,1036.0,1050.00"),
                StockPrice("SOBHA", "Sobha Limited", 1400.00, -1.15, 1416.30, 1432.00, 1381.00, "1342.0,1385.0,1368.0,1424.0,1416.3,1400.00"),
                StockPrice("INDIGO", "InterGlobe Aviation Ltd", 3100.00, 1.65, 3049.65, 3140.00, 3025.00, "2910.0,2980.0,2955.0,3075.0,3049.65,3100.00"),
                StockPrice("ZOMATO", "Zomato Limited", 160.00, 2.45, 156.15, 163.40, 154.80, "145.0,151.2,148.9,158.4,156.15,160.00"),
                StockPrice("PAYTM", "One 97 Communications", 410.00, -2.15, 419.00, 424.80, 405.10, "385.0,402.0,398.0,414.5,419.0,410.00"),
                StockPrice("NYKAA", "FSN E-Commerce Ventures", 160.00, 0.75, 158.80, 162.40, 156.80, "148.0,152.4,150.5,159.2,158.8,160.00"),
                StockPrice("POLICYBZR", "PB Fintech Limited", 1100.00, 1.85, 1080.00, 1120.00, 1072.00, "1025.0,1055.0,1048.0,1092.0,1080.0,1100.00"),
                
                // US Equity
                StockPrice("AAPL", "Apple Inc.", 182.41, 0.78, 181.0, 183.50, 180.20, "177.5,178.9,180.1,179.4,181.0,182.41"),
                StockPrice("TSLA", "Tesla Motors", 214.50, -2.05, 219.0, 221.30, 212.80, "225.1,222.0,223.4,218.9,219.0,214.50"),
                StockPrice("NVDA", "NVIDIA Corporation", 875.12, 2.96, 850.0, 880.00, 845.20, "815.0,830.5,842.0,838.0,850.0,875.12"),
                StockPrice("GOOG", "Alphabet Inc.", 151.60, 1.07, 150.0, 152.90, 149.50, "146.2,148.0,147.5,149.1,150.0,151.60"),
                StockPrice("MSFT", "Microsoft Corp.", 415.50, -0.60, 418.0, 422.00, 413.50, "408.0,412.5,419.0,420.5,418.0,415.50"),
                StockPrice("AMZN", "Amazon.com Inc.", 178.15, 1.80, 175.0, 179.20, 173.80, "169.5,172.0,174.1,173.2,175.0,178.15"),
                
                // Cryptocurrencies
                StockPrice("BTC", "Bitcoin (Digital Asset)", 62500.00, 2.12, 61200.0, 63100.00, 60800.00, "59100.0,60400.0,59800.0,61500.0,61200.0,62500.00"),
                StockPrice("ETH", "Ethereum (Digital Asset)", 3450.00, -1.99, 3520.0, 3560.00, 3410.00, "3320.0,3440.0,3490.0,3510.0,3520.0,3450.00"),

                // Global Commodities (USD prices)
                StockPrice("GLOBAL_GOLD", "Gold (Global COMEX Index)", 2420.50, 0.45, 2409.60, 2430.00, 2405.00, "2380.0,2395.0,2402.5,2410.0,2409.6,2420.50"),
                StockPrice("GLOBAL_SILVER", "Silver (Global NYMEX Index)", 29.20, -0.65, 29.39, 29.60, 29.05, "28.5,28.8,29.1,29.4,29.39,29.20"),
                StockPrice("GLOBAL_CRUDE", "Crude Oil Brent (Global NYMEX)", 81.50, 1.12, 80.60, 82.20, 80.10, "78.5,79.2,79.8,81.1,80.6,81.50"),
                StockPrice("GLOBAL_NATGAS", "Natural Gas (Global NYMEX)", 2.45, -2.15, 2.50, 2.55, 2.40, "2.6,2.55,2.48,2.52,2.50,2.45"),
                StockPrice("GLOBAL_COPPER", "Copper (Global COMEX)", 4.45, 0.85, 4.41, 4.52, 4.38, "4.25,4.32,4.30,4.44,4.41,4.45"),

                // MCX Indian Commodities (INR converted prices)
                StockPrice("MCX_GOLD", "Gold MCX (per 10g)", 64650.00, 0.45, 64360.00, 64900.00, 64240.00, "63570.0,63970.0,64170.0,64370.0,64360.0,64650.00"),
                StockPrice("MCX_SILVER", "Silver MCX (per kg)", 77950.00, -0.65, 78460.00, 79020.00, 77550.00, "76080.0,76880.0,77680.0,78480.0,78460.0,77950.00"),
                StockPrice("MCX_CRUDE", "Crude Oil MCX (per barrel)", 6764.50, 1.12, 6689.80, 6822.60, 6648.30, "6515.5,6573.6,6623.4,6731.3,6689.8,6764.50"),
                StockPrice("MCX_NATGAS", "Natural Gas MCX (per MMBtu)", 203.35, -2.15, 207.50, 211.65, 199.20, "215.8,211.6,205.8,209.1,207.5,203.35"),
                StockPrice("MCX_COPPER", "Copper MCX (per kg)", 814.25, 0.85, 806.95, 827.05, 801.45, "777.6,790.4,786.7,812.4,806.95,814.25"),
                
                // Indices
                StockPrice("NIFTY50", "Nifty 50 Index", 24500.0, 0.45, 24390.0, 24550.0, 24300.0, "24000,24100,24200,24390,24500"),
                StockPrice("BANKNIFTY", "Nifty Bank Index", 52500.0, -0.20, 52605.0, 52800.0, 52400.0, "52000,52200,52400,52605,52500"),
                StockPrice("NIFTYIT", "Nifty IT Index", 38000.0, 0.15, 37943.0, 38100.0, 37850.0, "37500,37700,37800,37943,38000")
            )
            stockPriceDao.insertStockPrices(initialStocks)

            // Pre-add no items to watchlist
        }

        val wNames = watchlistV2Dao.getWatchlistNamesFlow().firstOrNull() ?: emptyList()
        if (wNames.isEmpty()) {
            watchlistV2Dao.insertWatchlistName(WatchlistName(1, "Trade Lab"))
        }
    }

    // Execute Buy Transaction
    suspend fun buyStock(symbol: String, shares: Double, isDelivery: Boolean = true): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isMarketOpen(symbol)) {
            return@withContext Result.failure(Exception("Cannot execute trade. The market for $symbol is currently closed."))
        }
        val profile = userProfileDao.getUserProfile() ?: return@withContext Result.failure(Exception("User Profile not found"))
        val stock = stockPriceDao.getStockPrice(symbol) ?: return@withContext Result.failure(Exception("Stock symbol not found"))

        val totalValueStock = shares * stock.currentPrice
        val totalValueProfileCurrency = getConvertedStockPrice(totalValueStock, symbol, profile.currency)
        
        // --- REALISTIC CHARGES & TAXES (Simulated Indian Market) ---
        // STT (Securities Transaction Tax): 0.1% for Delivery, 0.025% for Intraday (Sell side mostly, but simplified here)
        val sttRate = if (isDelivery) 0.001 else 0.00025
        val stt = totalValueProfileCurrency * sttRate
        
        // SEBI + Stamp Duty + Transaction Charges (~0.01% combined)
        val miscCharges = totalValueProfileCurrency * 0.0001
        
        // Brokerage: 0.05% or 20 credits waiver
        val brokerageFee = if (profile.isPremium || profile.brokerageCredits >= 20) 0.0 else totalValueProfileCurrency * 0.0005
        val creditsToConsume = if (!profile.isPremium && profile.brokerageCredits >= 20) 20 else 0

        val totalDeduction = totalValueProfileCurrency + stt + miscCharges + brokerageFee

        val sym = if (profile.currency == "INR") "₹" else "$"
        if (profile.cash < totalDeduction) {
            return@withContext Result.failure(Exception("Insufficient funds. Required: $sym${String.format("%.2f", totalDeduction)} (incl. charges), Available: $sym${String.format("%.2f", profile.cash)}"))
        }

        // 1. Update Profile
        val updatedProfile = profile.copy(
            cash = profile.cash - totalDeduction,
            brokerageCredits = (profile.brokerageCredits - creditsToConsume).coerceAtLeast(0)
        )
        userProfileDao.insertProfile(updatedProfile)

        // 2. Update Holdings (T+1 logic: moves to sharesT1 if delivery)
        val existingHolding = holdingDao.getHoldingBySymbol(symbol)
        if (existingHolding != null) {
            val totalExistingShares = existingHolding.shares + existingHolding.sharesT1
            val newTotalShares = totalExistingShares + shares
            val avgPrice = ((totalExistingShares * existingHolding.averagePrice) + totalValueProfileCurrency) / newTotalShares
            
            if (isDelivery) {
                holdingDao.insertHolding(existingHolding.copy(sharesT1 = existingHolding.sharesT1 + shares, averagePrice = avgPrice))
            } else {
                // Intraday adds directly to 'settled' shares but they'll be squared off by EOD logic (future epic)
                holdingDao.insertHolding(existingHolding.copy(shares = existingHolding.shares + shares, averagePrice = avgPrice))
            }
        } else {
            holdingDao.insertHolding(
                Holding(
                    symbol = symbol, 
                    shares = if (isDelivery) 0.0 else shares, 
                    averagePrice = stock.currentPrice,
                    sharesT1 = if (isDelivery) shares else 0.0
                )
            )
        }

        // 3. Insert Transaction Log
        transactionDao.insertTransaction(
            Transaction(
                symbol = symbol, 
                type = "BUY", 
                shares = shares, 
                price = stock.currentPrice,
                isDelivery = isDelivery,
                charges = miscCharges + brokerageFee,
                tax = stt
            )
        )

        Result.success(Unit)
    }

    // Execute Sell Transaction
    suspend fun sellStock(symbol: String, shares: Double, isDelivery: Boolean = true): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isMarketOpen(symbol)) {
            return@withContext Result.failure(Exception("Cannot execute trade. The market for $symbol is currently closed."))
        }
        val profile = userProfileDao.getUserProfile() ?: return@withContext Result.failure(Exception("User Profile not found"))
        val stock = stockPriceDao.getStockPrice(symbol) ?: return@withContext Result.failure(Exception("Stock symbol not found"))
        val holding = holdingDao.getHoldingBySymbol(symbol) ?: return@withContext Result.failure(Exception("No holdings for this stock"))

        val availableToSell = if (isDelivery) holding.shares else (holding.shares + holding.sharesT1)
        if (availableToSell < shares) {
            val msg = if (isDelivery) "Insufficient settled shares for delivery exit. (Wait for T+1 settlement)" else "Insufficient total shares."
            return@withContext Result.failure(Exception(msg))
        }

        val totalValueStock = shares * stock.currentPrice
        val totalValueProfileCurrency = getConvertedStockPrice(totalValueStock, symbol, profile.currency)
        
        // Charges & Taxes
        val sttRate = if (isDelivery) 0.001 else 0.00025
        val stt = totalValueProfileCurrency * sttRate
        val miscCharges = totalValueProfileCurrency * 0.0001
        val brokerageFee = if (profile.isPremium || profile.brokerageCredits >= 20) 0.0 else totalValueProfileCurrency * 0.0005
        val creditsToConsume = if (!profile.isPremium && profile.brokerageCredits >= 20) 20 else 0

        val totalCredit = totalValueProfileCurrency - stt - miscCharges - brokerageFee

        // 1. Update Profile
        val updatedProfile = profile.copy(
            cash = profile.cash + totalCredit,
            brokerageCredits = (profile.brokerageCredits - creditsToConsume).coerceAtLeast(0)
        )
        userProfileDao.insertProfile(updatedProfile)

        // 2. Update Holdings
        if (isDelivery) {
            val remainingSettled = holding.shares - shares
            if (remainingSettled + holding.sharesT1 > 0.0001) {
                holdingDao.insertHolding(holding.copy(shares = remainingSettled))
            } else {
                holdingDao.deleteHoldingBySymbol(symbol)
            }
        } else {
            // Intraday sell from total pool
            var remToDeduct = shares
            var newSettled = holding.shares
            var newT1 = holding.sharesT1
            
            if (newSettled >= remToDeduct) {
                newSettled -= remToDeduct
            } else {
                remToDeduct -= newSettled
                newSettled = 0.0
                newT1 -= remToDeduct
            }
            
            if (newSettled + newT1 > 0.0001) {
                holdingDao.insertHolding(holding.copy(shares = newSettled, sharesT1 = newT1))
            } else {
                holdingDao.deleteHoldingBySymbol(symbol)
            }
        }

        // 3. Log
        transactionDao.insertTransaction(
            Transaction(
                symbol = symbol, 
                type = "SELL", 
                shares = shares, 
                price = stock.currentPrice,
                isDelivery = isDelivery,
                charges = miscCharges + brokerageFee,
                tax = stt
            )
        )

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
            holdingDao.deleteHoldingBySymbol(h.symbol)
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
            userProfileDao.insertProfile(
                profile.copy(
                    completedLevels = newLevels,
                    cash = profile.cash + reward,
                    startingCash = profile.startingCash + reward
                )
            )
        }
    }

    suspend fun setArcadeMode(enabled: Boolean) = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext
        userProfileDao.insertProfile(profile.copy(isArcadeMode = enabled))
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
                    val closeArr = quoteObj.optJSONArray("close")
                    if (closeArr != null && closeArr.length() > 0) {
                        val historyPoints = mutableListOf<Double>()
                        for (i in 0 until closeArr.length()) {
                            if (!closeArr.isNull(i)) {
                                var closeVal = closeArr.getDouble(i)
                                if (upperSymbol.startsWith("MCX_")) {
                                    closeVal = convertToMCXPrice(upperSymbol, closeVal)
                                }
                                historyPoints.add(closeVal)
                            }
                        }
                        val trimmedPoints = if (historyPoints.size > 12) historyPoints.takeLast(12) else historyPoints
                        historyData = trimmedPoints.joinToString(",") { String.format("%.2f", it) }
                    }
                }

                if (historyData.isBlank()) {
                    historyData = "$rawPreviousClose,$rawCurrentPrice"
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

            // A. Random Noise (Reduced variance for premium feel)
            // Random change between -0.4% and +0.4%
            val noisePct = (Random.nextDouble() * 0.8) - 0.4
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

            // Update history list (comma-separated, max 12 points for smooth canvas drawing)
            val historyPoints = stock.historyData.split(",").toMutableList()
            historyPoints.add(String.format("%.2f", newPrice))
            if (historyPoints.size > 12) {
                historyPoints.removeAt(0)
            }
            val newHistoryData = historyPoints.joinToString(",")

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

            val historyPoints = option.historyData.split(",").toMutableList()
            historyPoints.add(String.format("%.2f", newPremium))
            if (historyPoints.size > 12) {
                historyPoints.removeAt(0)
            }
            val newHistoryData = historyPoints.joinToString(",")

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
        
        generateContextualNews(updatedStandardPrices)
        matchPendingOrders()
    }

    private suspend fun updateIndices(allPrices: List<StockPrice>) {
        val indices = allPrices.filter { it.symbol in listOf("NIFTY50", "BANKNIFTY", "NIFTYIT") }
        if (indices.isEmpty()) return

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
            
            val historyPoints = index.historyData.split(",").toMutableList()
            historyPoints.add(String.format("%.2f", newPrice))
            if (historyPoints.size > 12) historyPoints.removeAt(0)

            index.copy(
                currentPrice = Math.round(newPrice * 100.0) / 100.0,
                dailyChangePct = Math.round(avgChangePct * 100.0) / 100.0,
                highPrice = maxOf(index.highPrice, newPrice),
                lowPrice = minOf(index.lowPrice, newPrice),
                historyData = historyPoints.joinToString(",")
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
                    buyStock(order.symbol, order.shares)
                } else {
                    sellStock(order.symbol, order.shares)
                }

                val sym = if (profile.currency == "INR") "₹" else "$"
                if (res.isSuccess) {
                    pendingOrderDao.updateOrderStatus(order.id, "EXECUTED")
                    addNotification("Order Executed: ${order.type} ${order.shares} shares of ${order.symbol} at $sym${String.format("%.2f", convertedPrice)} (Triggered by ${order.orderType} at $sym${String.format("%.2f", triggerPrice)})")
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

    suspend fun registerOrLogin(userName: String, userEmail: String) = withContext(Dispatchers.IO) {
        val existingProfile = userProfileDao.getUserProfile()
        if (existingProfile == null) {
            // Force create a profile if it doesn't exist (prevents race condition on first launch)
            userProfileDao.insertProfile(
                UserProfile(
                    id = 1,
                    cash = 25000.0,
                    startingCash = 25000.0,
                    riskPreference = "Moderate",
                    isLoggedIn = true,
                    userName = userName,
                    userEmail = userEmail,
                    trialActionsCount = 0
                )
            )
        } else {
            userProfileDao.insertProfile(
                existingProfile.copy(
                    isLoggedIn = true,
                    userName = userName,
                    userEmail = userEmail,
                    trialActionsCount = 0
                )
            )
        }
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
        userProfileDao.insertProfile(profile.copy(cash = profile.cash + amount))
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
        userProfileDao.insertProfile(
            profile.copy(
                dailyStreak = newStreak,
                lastActiveTimestamp = now,
                xp = profile.xp + (newStreak * 50) // Bonus XP for streaks
            )
        )
    }

    suspend fun addXp(amount: Int) = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile() ?: return@withContext
        userProfileDao.insertProfile(profile.copy(xp = profile.xp + amount))
    }

    suspend fun updateShieldDialogPreference(show: Boolean) = withContext(Dispatchers.IO) {
        userProfileDao.updateShieldDialogPreference(show)
    }
}
