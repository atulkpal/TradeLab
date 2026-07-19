package com.ashwathai.tradelab.shared

object TradingHelper {
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
}
