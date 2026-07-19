// Trade Lab Web Sandbox State Engine & Interactive Controller

// 1. Core Stock Data & Default Asset Baseline Configuration
const STOCKS_BASELINE = [
    { symbol: "RELIANCE", companyName: "Reliance Industries Ltd", currentPrice: 2950.50, dailyChangePct: 1.25, previousClose: 2914.00, highPrice: 2975.00, lowPrice: 2905.00, history: [2850.0, 2880.5, 2870.0, 2920.0, 2914.0, 2950.50], currency: "INR" },
    { symbol: "TCS", companyName: "Tata Consultancy Services", currentPrice: 3850.20, dailyChangePct: -0.80, previousClose: 3881.30, highPrice: 3910.00, lowPrice: 3825.00, history: [3780.0, 3815.0, 3840.0, 3890.0, 3881.3, 3850.20], currency: "INR" },
    { symbol: "INFY", companyName: "Infosys Limited", currentPrice: 1510.40, dailyChangePct: 0.45, previousClose: 1503.60, highPrice: 1525.00, lowPrice: 1495.00, history: [1475.0, 1490.0, 1485.0, 1512.0, 1503.6, 1510.40], currency: "INR" },
    { symbol: "HDFCBANK", companyName: "HDFC Bank Limited", currentPrice: 1610.10, dailyChangePct: 1.10, previousClose: 1592.50, highPrice: 1622.00, lowPrice: 1585.00, history: [1560.0, 1575.5, 1570.0, 1595.0, 1592.5, 1610.10], currency: "INR" },
    { symbol: "TATASTEEL", companyName: "Tata Steel Limited", currentPrice: 165.40, dailyChangePct: -0.55, previousClose: 166.30, highPrice: 167.10, lowPrice: 164.20, history: [161.2, 163.4, 162.8, 165.1, 166.3, 165.40], currency: "INR" },
    { symbol: "ICICIBANK", companyName: "ICICI Bank Limited", currentPrice: 1120.40, dailyChangePct: 0.75, previousClose: 1112.05, highPrice: 1128.50, lowPrice: 1108.00, history: [1090.0, 1102.3, 1098.5, 1115.0, 1112.05, 1120.40], currency: "INR" },
    { symbol: "SBIN", companyName: "State Bank of India", currentPrice: 835.60, dailyChangePct: -1.20, previousClose: 845.75, highPrice: 852.00, lowPrice: 831.10, history: [815.0, 824.5, 830.0, 848.0, 845.75, 835.60], currency: "INR" },
    { symbol: "BHARTIARTL", companyName: "Bharti Airtel Limited", currentPrice: 1425.30, dailyChangePct: 1.65, previousClose: 1402.15, highPrice: 1435.00, lowPrice: 1395.00, history: [1360.0, 1378.0, 1372.5, 1405.0, 1402.15, 1425.30], currency: "INR" },
    { symbol: "ITC", companyName: "ITC Limited", currentPrice: 430.20, dailyChangePct: -0.35, previousClose: 431.70, highPrice: 434.50, lowPrice: 428.10, history: [422.0, 425.4, 424.0, 432.5, 431.7, 430.20], currency: "INR" },
    { symbol: "LT", companyName: "Larsen & Toubro Limited", currentPrice: 3540.80, dailyChangePct: 0.90, previousClose: 3509.20, highPrice: 3565.00, lowPrice: 3495.00, history: [3440.0, 3475.0, 3462.0, 3515.0, 3509.2, 3540.80], currency: "INR" },
    { symbol: "TATAMOTORS", companyName: "Tata Motors Limited", currentPrice: 965.25, dailyChangePct: 2.10, previousClose: 945.40, highPrice: 975.00, lowPrice: 940.00, history: [910.0, 928.5, 922.0, 948.0, 945.4, 965.25], currency: "INR" },
    { symbol: "M&M", companyName: "Mahindra & Mahindra Ltd", currentPrice: 2820.40, dailyChangePct: -0.45, previousClose: 2833.15, highPrice: 2855.00, lowPrice: 2795.00, history: [2750.0, 2780.0, 2772.0, 2840.0, 2833.15, 2820.40], currency: "INR" }
];

// Academy Questions Database
const ACADEMY_LEVELS = [
    { id: 1, title: "Level 1: What is a Stock?", topic: "Equities Terminology", rewardAmt: 5000, concept: "A stock represents fractional ownership in a business. When a company flourishes, the share value increases.", question: "What does buying a stock represent?", options: ["A loan that the company must repay", "A unit of fractional ownership", "A contract for free products"], correctIndex: 1, lectures: ["A stock (or share) represents a unit of fractional ownership in a company. When you purchase a stock, you become a part-owner.", "As the business grows and earns higher profits, investors are willing to pay more for each share, leading to capital appreciation."] },
    { id: 2, title: "Level 2: Brokers & Exchanges", topic: "Market Infrastructure", rewardAmt: 5000, concept: "Exchanges are marketplaces where stock prices are matched, and Brokers are regulated intermediaries.", question: "What is the primary role of a stock broker?", options: ["To guarantee that every stock goes up", "To act as a regulated intermediary sending orders to the exchange", "To print new shares"], correctIndex: 1, lectures: ["An exchange (like NYSE, NASDAQ, NSE) is a highly regulated marketplace.", "Individual retail investors cannot buy shares directly from the exchange. You need a certified broker to transmit orders."] },
    { id: 3, title: "Level 3: Order Types", topic: "Advanced Executions", rewardAmt: 10000, concept: "Market orders buy instantly, while Limit orders specify prices. GTT orders stay active.", question: "Which order type is ideal for waiting patiently to buy a stock at a lower support price?", options: ["Market Order", "GTT or Limit Order", "Margin Intraday Order"], correctIndex: 1, lectures: ["A Market Order executes instantly at current prices. A Limit Order specifies the exact maximum price you are willing to pay.", "GTT (Good Till Triggered) orders stay active for weeks or months, triggering only when your target price hits."] },
    { id: 4, title: "Level 4: Position Sizing", topic: "Risk Mitigation", rewardAmt: 10000, concept: "Position sizing means limiting how much of your capital is allocated to any single trade to prevent catastrophic losses.", question: "If your total capital is ₹25,000, what is the maximum disciplined allocation for a single stock trade?", options: ["₹15,000 to ₹25,000 (60% to 100%)", "₹2,500 to ₹3,750 (10% to 15%)", "Exactly ₹0"], correctIndex: 1, lectures: ["Going 'all-in' on a single stock is a catastrophic retail mistake. If that stock drops 30%, a third of your life savings is gone.", "Disciplined investors enforce a maximum position sizing rule, limiting any single asset to 10-15% of their total investment portfolio."] },
    { id: 5, title: "Level 5: Diversification", topic: "Portfolio Health", rewardAmt: 10000, concept: "Diversification means distributing your cash across distinct stocks, sectors, and asset classes to spread risk.", question: "Why do retail investors use diversification?", options: ["To completely avoid taxes", "To protect overall capital from crashing if a single sector sinks", "To speed up order matching"], correctIndex: 1, lectures: ["Owning five tech stocks is not diversification. A tech regulatory setback will crash all five together.", "True diversification means distributing assets across healthcare, finance, consumer staples, and technology to insulate from sector shocks."] },
    { id: 6, title: "Level 6: Technical Basics", topic: "Chart Interpretation", rewardAmt: 15000, concept: "Candlestick charts display Open, Close, High, and Low prices. A hammer at support hints at trend reversal.", question: "What does a candlestick with a long lower shadow (hammer) near a support level suggest?", options: ["Sellers have completely dominated", "Buyers rejected lower prices, suggesting reversal", "The broker has disconnected"], correctIndex: 1, lectures: ["A candle displays high, low, open, and close prices. The wick indicates price extremes rejected during that timeframe.", "A hammer candle at support indicates that sellers tried to drive prices lower, but aggressive buyers stepped in, hinting at bullish momentum."] }
];

// 2. Application Variables & Persistent States Engine
let state = {
    profile: {
        cash: 10000.0,
        startingCash: 10000.0,
        riskPreference: "Moderate",
        currency: "INR",
        brokerageCredits: 300,
        completedLevels: [], // Array of level IDs
        isCalibrated: false,
        userName: "Retail Pioneer",
        userEmail: "atulkpal@gmail.com",
        investorPersonality: "Disciplined Compounder"
    },
    holdings: [], // { symbol, shares, averagePrice }
    transactions: [], // { id, symbol, type, shares, price, timestamp }
    pendingOrders: [], // { id, symbol, type, orderType, shares, triggerPrice, status, timestamp }
    stockPrices: JSON.parse(JSON.stringify(STOCKS_BASELINE)),
    selectedSymbol: "RELIANCE",
    selectedOrderType: "Market",
    activeTab: "portfolio",
    showSMA: false,
    showEMA: false,
    showRSI: false
};

const USD_INR_RATE = 83.0;

// Initialize app data from local storage if available
function loadState() {
    const saved = localStorage.getItem("tradelab_web_state");
    if (saved) {
        try {
            const parsed = JSON.parse(saved);
            if (parsed.profile) state.profile = parsed.profile;
            if (parsed.holdings) state.holdings = parsed.holdings;
            if (parsed.transactions) state.transactions = parsed.transactions;
            if (parsed.pendingOrders) state.pendingOrders = parsed.pendingOrders;
            if (parsed.stockPrices && parsed.stockPrices.length > 0) state.stockPrices = parsed.stockPrices;
            if (parsed.selectedSymbol) state.selectedSymbol = parsed.selectedSymbol;
        } catch (e) {
            console.error("Local storage corruption detected. Resetting state.", e);
        }
    }
}

function saveState() {
    localStorage.setItem("tradelab_web_state", JSON.stringify(state));
}

// 3. Conversions & Formatting Utilities
function isIndianStock(symbol) {
    const stock = state.stockPrices.find(s => s.symbol === symbol);
    return stock ? stock.currency === "INR" : true;
}

function getConvertedPrice(price, symbol, targetCurrency) {
    const isInd = isIndianStock(symbol);
    const nativeCurrency = isInd ? "INR" : "USD";
    if (nativeCurrency === targetCurrency) return price;
    
    if (nativeCurrency === "USD" && targetCurrency === "INR") {
        return price * USD_INR_RATE;
    } else {
        return price / USD_INR_RATE;
    }
}

function formatCurrency(val, currency = state.profile.currency) {
    const symbol = currency === "INR" ? "₹" : "$";
    return symbol + val.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

// Calculate Net Worth: Cash + value of holdings converted to prefered currency
function calculateNetWorth() {
    let holdingsVal = 0;
    state.holdings.forEach(holding => {
        const stock = state.stockPrices.find(s => s.symbol === holding.symbol);
        const livePrice = stock ? stock.currentPrice : holding.averagePrice;
        const converted = getConvertedPrice(livePrice, holding.symbol, state.profile.currency);
        holdingsVal += (holding.shares * converted);
    });
    return state.profile.cash + holdingsVal;
}

function calculateHoldingsValue() {
    let holdingsVal = 0;
    state.holdings.forEach(holding => {
        const stock = state.stockPrices.find(s => s.symbol === holding.symbol);
        const livePrice = stock ? stock.currentPrice : holding.averagePrice;
        const converted = getConvertedPrice(livePrice, holding.symbol, state.profile.currency);
        holdingsVal += (holding.shares * converted);
    });
    return holdingsVal;
}

// 4. Simulated Live Ticker Loop
function startMarketSimulation() {
    setInterval(() => {
        state.stockPrices.forEach(stock => {
            // Random fluctuation walk between -1.2% and +1.2%
            const pct = (Math.random() - 0.5) * 0.024;
            const prevPrice = stock.currentPrice;
            stock.currentPrice = parseFloat((stock.currentPrice * (1 + pct)).toFixed(2));
            stock.dailyChangePct = parseFloat((((stock.currentPrice - stock.previousClose) / stock.previousClose) * 100).toFixed(2));
            
            if (stock.currentPrice > stock.highPrice) stock.highPrice = stock.currentPrice;
            if (stock.currentPrice < stock.lowPrice) stock.lowPrice = stock.currentPrice;

            // Push to historic queue, limit to last 20
            stock.history.push(stock.currentPrice);
            if (stock.history.length > 20) stock.history.shift();

            // Trigger flashing animations on changes if visible
            const isTarget = stock.symbol === state.selectedSymbol;
            if (isTarget) {
                const element = document.getElementById("chart-stock-price");
                if (element) {
                    element.classList.remove("flash-up", "flash-down");
                    void element.offsetWidth; // trigger reflow
                    element.classList.add(stock.currentPrice >= prevPrice ? "flash-up" : "flash-down");
                }
            }
        });

        // Evaluate triggers for Stop-Loss, Limit, GTT Orders
        checkPendingOrders();

        saveState();
        refreshUI();
    }, 2500);
}

function checkPendingOrders() {
    let updated = false;
    state.pendingOrders.forEach(order => {
        if (order.status !== "PENDING") return;
        const stock = state.stockPrices.find(s => s.symbol === order.symbol);
        if (!stock) return;

        const livePrice = stock.currentPrice;
        let triggers = false;

        if (order.type === "BUY") {
            if (order.orderType === "Limit" && livePrice <= order.triggerPrice) triggers = true;
        } else if (order.type === "SELL") {
            if (order.orderType === "Limit" && livePrice >= order.triggerPrice) triggers = true;
            if (order.orderType === "Stop-Loss" && livePrice <= order.triggerPrice) triggers = true;
            if (order.orderType === "GTT" && livePrice >= order.triggerPrice) triggers = true;
        }

        if (triggers) {
            // Execute order
            executeTransaction(order.symbol, order.type, order.shares, livePrice, order.orderType);
            order.status = "EXECUTED";
            updated = true;
            showToast(`Order Executed: ${order.type} ${order.shares} ${order.symbol} at ${formatCurrency(livePrice, stock.currency)}`, "emerald");
        }
    });

    if (updated) {
        state.pendingOrders = state.pendingOrders.filter(o => o.status === "PENDING");
        saveState();
    }
}

function executeTransaction(symbol, type, shares, price, executionType = "Market") {
    const stock = state.stockPrices.find(s => s.symbol === symbol);
    const nativeCur = stock ? stock.currency : "INR";
    const convertedPrice = getConvertedPrice(price, symbol, state.profile.currency);
    const totalCost = shares * convertedPrice;

    // Commission Calculations
    let commission = state.profile.brokerageCredits > 0 ? 0 : (state.profile.currency === "INR" ? 20.0 : 0.25);
    if (state.profile.brokerageCredits > 0) {
        state.profile.brokerageCredits = Math.max(0, state.profile.brokerageCredits - 10);
    }

    if (type === "BUY") {
        state.profile.cash -= (totalCost + commission);
        
        // Add or update holdings
        const existing = state.holdings.find(h => h.symbol === symbol);
        if (existing) {
            const newTotalShares = existing.shares + shares;
            existing.averagePrice = ((existing.shares * existing.averagePrice) + (shares * price)) / newTotalShares;
            existing.shares = newTotalShares;
        } else {
            state.holdings.push({ symbol, shares, averagePrice: price });
        }

        // Mission complete check
        checkMissions("has_traded");
    } else {
        state.profile.cash += (totalCost - commission);
        
        // Subtract holdings
        const existing = state.holdings.find(h => h.symbol === symbol);
        if (existing) {
            existing.shares -= shares;
            if (existing.shares <= 0.001) {
                state.holdings = state.holdings.filter(h => h.symbol !== symbol);
            }
        }
    }

    // Add log
    state.transactions.unshift({
        id: Date.now(),
        symbol,
        type,
        shares,
        price,
        executionType,
        timestamp: Date.now()
    });

    saveState();
}

// 5. Drawing Responsive SVG Charts with Indicators (SMA / EMA / RSI)
function drawCharts() {
    const stock = state.stockPrices.find(s => s.symbol === state.selectedSymbol);
    if (!stock) return;

    // Render Price line
    const svgPrice = document.getElementById("price-vector-chart");
    if (!svgPrice) return;

    const data = stock.history;
    const minVal = Math.min(...data) * 0.995;
    const maxVal = Math.max(...data) * 1.005;
    const range = maxVal - minVal;

    const width = svgPrice.clientWidth || 600;
    const height = svgPrice.clientHeight || 250;

    let points = "";
    data.forEach((val, i) => {
        const x = (i / (data.length - 1)) * width;
        const y = height - ((val - minVal) / range) * height;
        points += `${x},${y} `;
    });

    // Create price Polyline
    let html = `<polyline fill="none" stroke="#06b6d4" stroke-width="3" points="${points}" />`;

    // Overlay Simple Moving Average (SMA-5)
    if (state.showSMA) {
        let smaPoints = "";
        data.forEach((val, i) => {
            if (i >= 4) {
                const slice = data.slice(i - 4, i + 1);
                const sum = slice.reduce((a, b) => a + b, 0);
                const sma = sum / 5;
                const x = (i / (data.length - 1)) * width;
                const y = height - ((sma - minVal) / range) * height;
                smaPoints += `${x},${y} `;
            }
        });
        if (smaPoints) {
            html += `<polyline fill="none" stroke="#f59e0b" stroke-dasharray="4" stroke-width="2" points="${smaPoints}" />`;
        }
    }

    // Overlay Exponential Moving Average (EMA-5)
    if (state.showEMA) {
        let emaPoints = "";
        let ema = data[0];
        const k = 2 / (5 + 1);
        data.forEach((val, i) => {
            ema = val * k + ema * (1 - k);
            const x = (i / (data.length - 1)) * width;
            const y = height - ((ema - minVal) / range) * height;
            emaPoints += `${x},${y} `;
        });
        html += `<polyline fill="none" stroke="#d946ef" stroke-dasharray="4" stroke-width="2" points="${emaPoints}" />`;
    }

    svgPrice.innerHTML = html;

    // Render secondary RSI chart
    const rsiContainer = document.getElementById("rsi-chart-container");
    const rsiChart = document.getElementById("rsi-vector-chart");
    if (state.showRSI) {
        rsiContainer.classList.remove("hidden");
        // Calculate relative strength index over the history
        let rsiData = [];
        data.forEach((val, idx) => {
            if (idx === 0) {
                rsiData.push(50); // baseline start
            } else {
                // simple rolling relative gains
                const prevSlice = data.slice(0, idx + 1);
                let gains = 0;
                let losses = 0;
                for (let j = 1; j < prevSlice.length; j++) {
                    const d = prevSlice[j] - prevSlice[j - 1];
                    if (d > 0) gains += d;
                    else losses -= d;
                }
                if (losses === 0) rsiData.push(100);
                else {
                    const rs = gains / losses;
                    rsiData.push(100 - (100 / (1 + rs)));
                }
            }
        });

        const rsiW = rsiChart.clientWidth || 600;
        const rsiH = rsiChart.clientHeight || 96;

        let rsiPts = "";
        rsiData.forEach((val, i) => {
            const x = (i / (rsiData.length - 1)) * rsiW;
            const y = rsiH - (val / 100) * rsiH;
            rsiPts += `${x},${y} `;
        });

        // Horizontal guideline boundaries for 30 and 70 (Overbought / Oversold)
        const line30 = rsiH - (30 / 100) * rsiH;
        const line70 = rsiH - (70 / 100) * rsiH;

        rsiChart.innerHTML = `
            <line x1="0" y1="${line70}" x2="${rsiW}" y2="${line70}" stroke="#6366f1" stroke-dasharray="2" stroke-width="1" opacity="0.4" />
            <line x1="0" y1="${line30}" x2="${rsiW}" y2="${line30}" stroke="#6366f1" stroke-dasharray="2" stroke-width="1" opacity="0.4" />
            <polyline fill="none" stroke="#818cf8" stroke-width="2" points="${rsiPts}" />
        `;
    } else {
        rsiContainer.classList.add("hidden");
    }
}

// 6. Interactive Trading Panel Controls & Risk Auditor
function updateTradingOrderBreakdown() {
    const stock = state.stockPrices.find(s => s.symbol === state.selectedSymbol);
    if (!stock) return;

    const sharesInput = document.getElementById("order-shares-input");
    const shares = parseFloat(sharesInput.value) || 0;

    let executionPrice = stock.currentPrice;
    if (state.selectedOrderType !== "Market") {
        const triggerInput = document.getElementById("order-trigger-input");
        executionPrice = parseFloat(triggerInput.value) || stock.currentPrice;
    }

    const convertedPrice = getConvertedPrice(executionPrice, stock.symbol, state.profile.currency);
    const subtotal = shares * convertedPrice;
    
    // Fee calculations
    let fee = state.profile.brokerageCredits > 0 ? 0 : (state.profile.currency === "INR" ? 20.0 : 0.25);
    const totalCost = subtotal + fee;

    document.getElementById("summary-subtotal").innerText = formatCurrency(subtotal);
    document.getElementById("summary-brokerage").innerText = state.profile.brokerageCredits > 0 ? "₹0.00 (Shield Active)" : formatCurrency(fee);
    document.getElementById("summary-total").innerText = formatCurrency(shares > 0 ? totalCost : 0);

    // Dynamic Sizing Sizing Rating Diagnostics
    const auditPlaceholder = document.getElementById("audit-placeholder");
    const auditResults = document.getElementById("audit-results-card");

    if (shares <= 0) {
        auditPlaceholder.classList.remove("hidden");
        auditResults.classList.add("hidden");
    } else {
        auditPlaceholder.classList.add("hidden");
        auditResults.classList.remove("hidden");

        const netWorth = calculateNetWorth();
        const allocPct = (totalCost / netWorth) * 100;

        document.getElementById("audit-alloc-pct").innerText = `${allocPct.toFixed(2)}%`;
        const barWidth = Math.min(100, allocPct);
        const barElement = document.getElementById("audit-alloc-bar");
        barElement.style.width = `${barWidth}%`;

        const badgeSize = document.getElementById("audit-badge-size");
        const textSize = document.getElementById("audit-text-size");
        const badgeType = document.getElementById("audit-badge-type");
        const textType = document.getElementById("audit-text-type");

        // Position Sizing Evaluation (10-15% maximum standard)
        if (allocPct <= 15.0) {
            badgeSize.className = "px-2.5 py-1 rounded-md text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20";
            badgeSize.innerText = "Sizing: Disciplined";
            textSize.innerText = `Excellent position size! Allocated ${allocPct.toFixed(1)}% of net worth. If this asset collapses, your recovery curve remains highly manageable.`;
        } else {
            badgeSize.className = "px-2.5 py-1 rounded-md text-[10px] font-bold bg-rose-500/10 text-rose-400 border border-rose-500/20";
            badgeSize.innerText = "Sizing: High Risk";
            textSize.innerText = `Caution: Highly concentrated position (${allocPct.toFixed(1)}%). Committing over 15% of net worth to a single asset invites extreme drawdowns if fundamentals shift unexpectedly.`;
        }

        // Execution Sizing Evaluation
        if (state.selectedOrderType === "Market") {
            badgeType.className = "px-2.5 py-1 rounded-md text-[10px] font-bold bg-amber-500/10 text-amber-400 border border-amber-500/20";
            badgeType.innerText = "Order: Market Slip";
            textType.innerText = "Executing at Market price is fast but prone to slippage during volatility. Consider Limit orders for precise entries.";
        } else {
            badgeType.className = "px-2.5 py-1 rounded-md text-[10px] font-bold bg-cyan-500/10 text-cyan-400 border border-cyan-500/20";
            badgeType.innerText = "Order: Technical GTT";
            textType.innerText = "Great choice. Using Limit or Triggered rules protects capital from slippage and enforces trading strategy.";
        }
    }
}

// 7. Interactive Learn-to-Earn Academy Hub
function loadAcademyModules() {
    const grid = document.getElementById("academy-levels-grid");
    if (!grid) return;

    let html = "";
    ACADEMY_LEVELS.forEach(level => {
        const isCompleted = state.profile.completedLevels.includes(level.id);
        const borderStyle = isCompleted ? "border-emerald-500/30 bg-[#0D1527]/40" : "border-slate-800 bg-[#0D1527]/10";
        
        html += `
            <div class="card p-5 rounded-2xl border ${borderStyle} flex flex-col justify-between gap-4">
                <div class="space-y-2">
                    <div class="flex justify-between items-start gap-2">
                        <span class="text-[10px] font-bold uppercase tracking-wider text-slate-500">${level.topic}</span>
                        ${isCompleted ? 
                            '<span class="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">Passed</span>' : 
                            `<span class="text-[10px] font-bold px-2 py-0.5 rounded-full bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">+${formatCurrency(level.rewardAmt)}</span>`
                        }
                    </div>
                    <h3 class="font-['Space_Grotesk'] font-bold text-base text-slate-100">${level.title}</h3>
                    <p class="text-xs text-slate-400 leading-relaxed">${level.concept}</p>
                </div>
                
                <button onclick="openAcademyQuiz(${level.id})" class="w-full py-2.5 rounded-xl font-bold text-xs transition-all ${isCompleted ? 'bg-slate-900 border border-slate-800 text-slate-400 hover:text-slate-200' : 'bg-cyan-500 text-white hover:bg-cyan-600'}">
                    ${isCompleted ? 'REVIEW CONTENT' : 'START LESSON & QUIZ'}
                </button>
            </div>
        `;
    });

    grid.innerHTML = html;
    document.getElementById("academy-completed-ratio").innerText = `${state.profile.completedLevels.length}/6`;
}

function openAcademyQuiz(id) {
    const level = ACADEMY_LEVELS.find(l => l.id === id);
    if (!level) return;

    let lectureHtml = "";
    level.lectures.forEach((lec, idx) => {
        lectureHtml += `
            <div class="space-y-1">
                <h5 class="font-bold text-xs text-cyan-400">${lec.title}</h5>
                <p class="text-xs text-slate-300 leading-relaxed">${lec.content}</p>
            </div>
        `;
    });

    let optionsHtml = "";
    level.options.forEach((opt, idx) => {
        optionsHtml += `
            <button onclick="submitAcademyAnswer(${level.id}, ${idx})" class="w-full p-4 rounded-xl border border-slate-800 hover:border-cyan-500 text-left bg-slate-900/60 font-semibold text-xs text-slate-300 transition-all hover:text-slate-100">
                ${opt}
            </button>
        `;
    });

    const isCompleted = state.profile.completedLevels.includes(level.id);

    // Modal view injection directly inside body
    const modal = document.createElement("div");
    modal.id = "academy-quiz-modal";
    modal.className = "fixed inset-0 bg-slate-950/90 backdrop-blur-md z-50 flex items-center justify-center p-6";
    modal.innerHTML = `
        <div class="max-w-2xl w-full bg-slate-900 border border-slate-800 p-6 rounded-3xl space-y-6 max-h-[90vh] overflow-y-auto">
            <div class="flex justify-between items-start gap-4">
                <div>
                    <span class="text-[9px] uppercase font-bold text-slate-500 tracking-wider">${level.topic}</span>
                    <h3 class="font-['Space_Grotesk'] font-bold text-lg text-white mt-0.5">${level.title}</h3>
                </div>
                <button onclick="closeAcademyModal()" class="text-slate-500 hover:text-slate-300">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                </button>
            </div>

            <!-- Lectures section -->
            <div class="space-y-4 border-b border-slate-800/80 pb-6">
                <h4 class="text-xs uppercase font-bold tracking-wider text-slate-400">Tactical Lecture Notes</h4>
                <div class="space-y-3">${lectureHtml}</div>
            </div>

            <!-- Quiz Section -->
            <div class="space-y-4">
                <h4 class="text-xs uppercase font-bold tracking-wider text-slate-400 flex items-center justify-between">
                    <span>Interactive Certification Quiz</span>
                    ${isCompleted ? '<span class="text-[10px] text-emerald-400 font-bold uppercase tracking-wider">Already completed</span>' : `<span class="text-[10px] text-cyan-400 font-bold uppercase tracking-wider">Reward: +${formatCurrency(level.rewardAmt)}</span>`}
                </h4>
                <p class="font-semibold text-sm text-slate-200">${level.question}</p>
                <div class="space-y-3">${optionsHtml}</div>
            </div>
        </div>
    `;

    document.body.appendChild(modal);
}

function submitAcademyAnswer(levelId, idx) {
    const level = ACADEMY_LEVELS.find(l => l.id === levelId);
    if (!level) return;

    if (idx === level.correctIndex) {
        const isAlreadyDone = state.profile.completedLevels.includes(levelId);
        if (!isAlreadyDone) {
            state.profile.completedLevels.push(levelId);
            state.profile.cash += getConvertedPrice(level.rewardAmt, "TATASTEEL", state.profile.currency);
            saveState();
            showToast(`Passed Level! Credited +${formatCurrency(level.rewardAmt)} to Cash wallet.`, "emerald");
            
            // Check Academy Mission Trigger
            if (state.profile.completedLevels.length >= 3) {
                checkMissions("completed_3_modules");
            }
        } else {
            showToast("Review complete! Correct answer.", "cyan");
        }
        closeAcademyModal();
        loadAcademyModules();
        refreshUI();
    } else {
        showToast("Incorrect answer. Study the lecture notes and try again!", "rose");
    }
}

function closeAcademyModal() {
    const modal = document.getElementById("academy-quiz-modal");
    if (modal) modal.remove();
}

// 8. 60-Second Investor Profiler & Calibration
function submitProfilerQuiz() {
    // Collect active state choices
    state.profile.isCalibrated = true;
    state.profile.cash = getConvertedPrice(10000, "TATASTEEL", state.profile.currency);
    state.profile.startingCash = state.profile.cash;
    state.profile.investorPersonality = "Disciplined Swing Trader";
    state.profile.riskPreference = "Moderate";
    
    // Check calibration mission
    checkMissions("has_calibrated");

    saveState();
    showToast("Profile calibrated! Calibration details locked.", "emerald");
    refreshUI();
}

// 9. Gemini AI Behavior Diagnosis (Local & Real API calls)
function runBehavioralAIDiagnosis() {
    const terminal = document.getElementById("ai-output-terminal");
    if (!terminal) return;

    // Loading State
    terminal.innerHTML = `
        <p class="text-indigo-400 animate-pulse"># Processing local transaction ledger databases...</p>
        <p class="mt-1 text-slate-500"># Evaluating average purchase prices against holdings duration...</p>
        <p class="mt-1 text-slate-500 animate-pulse"># Querying Ashwathey AI Hub diagnostic logic...</p>
    `;

    const customKey = document.getElementById("ai-api-key").value.trim();

    // Analyze transaction logs for real-world biases
    const buyTrades = state.transactions.filter(t => t.type === "BUY");
    const sellTrades = state.transactions.filter(t => t.type === "SELL");

    let fomoScore = 0;
    let revengeScore = 0;
    let suncostScore = 0;

    // Detect Poor Sizing Biases
    const overSizedTradesCount = state.transactions.filter(t => {
        const value = t.shares * t.price;
        const netWorth = calculateNetWorth();
        const valueInPref = getConvertedPrice(value, t.symbol, state.profile.currency);
        return (valueInPref / netWorth) > 0.15;
    }).length;

    // Detect FOMO: buying stocks that are up on the day
    buyTrades.forEach(trade => {
        const stock = state.stockPrices.find(s => s.symbol === trade.symbol);
        if (stock && stock.dailyChangePct > 2.0) fomoScore++;
    });

    // Detect Revenge Trading: Buying multiple times immediately following a Sell loss
    if (state.transactions.length >= 3) {
        for (let i = 0; i < state.transactions.length - 2; i++) {
            if (state.transactions[i+2].type === "SELL" && state.transactions[i+1].type === "BUY" && state.transactions[i].type === "BUY") {
                revengeScore++;
            }
        }
    }

    // Prepare Diagnostic Report Content
    let auditOutputHtml = "";

    if (customKey) {
        // Trigger actual API integration
        fetch(`https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${customKey}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                contents: [{
                    parts: [{
                        text: `You are Ashwathey, a professional behavioral trading advisor auditing a retail client. The client has completed ${state.transactions.length} trades. Holdings count: ${state.holdings.length}. Net worth: ${formatCurrency(calculateNetWorth())}. Write a brief, punchy (maximum 150 words) behavioral audit diagnosing potential biases (FOMO, Revenge Trading, Sunk-Cost) based on these stats, outlining strategic remedies. Keep formatting clean with console style bullet points.`
                    }]
                }]
            })
        })
        .then(res => res.json())
        .then(json => {
            const text = json.candidates[0].content.parts[0].text;
            terminal.innerHTML = `
                <p class="text-indigo-400"># Gemini live response retrieved successfully.</p>
                <div class="mt-3 text-slate-100 whitespace-pre-line">${text}</div>
            `;
        })
        .catch(err => {
            console.error("Gemini API connection error:", err);
            terminal.innerHTML = `<p class="text-rose-400"># API Error: Unable to negotiate credentials with Gemini endpoint. Please verify your API Key or fallback to local diagnostics.</p>`;
        });
    } else {
        // High-fidelity fallback simulated analyzer
        setTimeout(() => {
            const hasOverSized = overSizedTradesCount > 0;
            const hasFomo = fomoScore > 0;
            const hasRevenge = revengeScore > 0;

            auditOutputHtml = `
                <p class="text-emerald-400"># LOCAL AUDIT REPORT GENERATED SUCCESSFULLY.</p>
                <p class="text-slate-500 mt-1">--------------------------------------------------</p>
                <p class="mt-3 text-slate-200 font-bold uppercase tracking-wider text-[11px] text-indigo-400">Psychological Biases Diagnostics:</p>
                
                <ul class="space-y-3 mt-2 pl-2">
                    <li class="flex items-start gap-2">
                        <span class="${hasOverSized ? 'text-rose-400' : 'text-emerald-400'} font-bold">▶</span>
                        <div>
                            <strong class="text-slate-200">Sizing Over-Concentration: ${hasOverSized ? 'HIGH RISK' : 'LOW RISK'}</strong>
                            <p class="text-slate-400 mt-0.5">${hasOverSized ? 'Client logs reveal multiple oversized positions (>15%). This suggests gambling tendencies rather than disciplined allocation.' : 'Excellent job! All executions remained under 15% net worth guidelines.'}</p>
                        </div>
                    </li>
                    <li class="flex items-start gap-2">
                        <span class="${hasFomo ? 'text-amber-400' : 'text-emerald-400'} font-bold">▶</span>
                        <div>
                            <strong class="text-slate-200">Fear of Missing Out (FOMO): ${hasFomo ? 'WARNING' : 'HEALTHY'}</strong>
                            <p class="text-slate-400 mt-0.5">${hasFomo ? 'Detected buying stocks during major daily positive momentum. Retail speculators buying green peaks are vulnerable to immediate pullbacks.' : 'No active FOMO behaviors detected. You entered positions under support benchmarks.'}</p>
                        </div>
                    </li>
                    <li class="flex items-start gap-2">
                        <span class="${hasRevenge ? 'text-rose-400' : 'text-emerald-400'} font-bold">▶</span>
                        <div>
                            <strong class="text-slate-200">Revenge speculation (Over-trading): ${hasRevenge ? 'DANGER' : 'HEALTHY'}</strong>
                            <p class="text-slate-400 mt-0.5">${hasRevenge ? 'Detected immediate repetitive trades following an exit loss. This is a severe cognitive bias to recoup losses through aggression.' : 'You maintained a healthy cooldown period between exit losses and new entries.'}</p>
                        </div>
                    </li>
                </ul>

                <p class="text-slate-500 mt-4">--------------------------------------------------</p>
                <p class="mt-2 text-indigo-300 font-bold uppercase tracking-wider text-[10px]">Strategic Advisor Remedies:</p>
                <p class="text-slate-300 mt-1">1. Impose a strict 2-hour trading cooldown following any realized losses.<br>2. Always utilize GTT trigger prices instead of executing market orders blindly.<br>3. Commit to never allowing a single asset value to cross 12% of total net worth.</p>
            `;
            terminal.innerHTML = auditOutputHtml;
        }, 1500);
    }
}

// 10. Daily Missions Tracker
const MISSIONS_CATALOG = [
    { id: "has_traded", title: "Practice Frontier", desc: "Execute your first simulator buy order.", reward: 500, done: false },
    { id: "completed_3_modules", title: "Theory Pioneer", desc: "Pass at least 3 risk modules in Academy.", reward: 1000, done: false },
    { id: "has_calibrated", title: "Discipline Shield", desc: "Complete the Realistic Investor Profiler.", reward: 1500, done: false }
];

function checkMissions(id) {
    const savedMissions = JSON.parse(localStorage.getItem("tradelab_web_missions") || "{}");
    if (savedMissions[id]) return; // already done

    const mission = MISSIONS_CATALOG.find(m => m.id === id);
    if (mission) {
        savedMissions[id] = true;
        localStorage.setItem("tradelab_web_missions", JSON.stringify(savedMissions));
        
        // Reward cash
        state.profile.cash += getConvertedPrice(mission.reward, "TATASTEEL", state.profile.currency);
        saveState();
        showToast(`Mission Complete! Earned +${formatCurrency(mission.reward)}`, "emerald");
        refreshUI();
    }
}

function loadMissionsList() {
    const container = document.getElementById("missions-container");
    if (!container) return;

    const savedMissions = JSON.parse(localStorage.getItem("tradelab_web_missions") || "{}");

    let html = "";
    MISSIONS_CATALOG.forEach(m => {
        const isDone = !!savedMissions[m.id];
        const opacity = isDone ? "opacity-60" : "opacity-100";
        const statusColor = isDone ? "text-emerald-400" : "text-slate-400";
        const icon = isDone ? 
            `<svg class="w-4 h-4 text-emerald-400" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path></svg>` :
            `<div class="w-4 h-4 rounded-full border border-slate-700"></div>`;

        html += `
            <div class="flex items-start gap-2 text-xs font-medium ${opacity}">
                <div class="mt-0.5">${icon}</div>
                <div class="flex-1">
                    <h4 class="text-slate-200 font-semibold">${m.title}</h4>
                    <p class="text-[10px] text-slate-500 leading-tight mt-0.5">${m.desc}</p>
                    <span class="text-[9px] ${statusColor} font-mono mt-1 block">+${formatCurrency(m.reward)} ${isDone ? '(Claimed)' : ''}</span>
                </div>
            </div>
        `;
    });

    container.innerHTML = html;
}

// 11. AdMob Simulated Video Ad Shields
let adCallback = null;
let forceAdFail = false;

function triggerCelebrationAnimation(type = "presents") {
    const container = document.createElement("div");
    container.className = "fixed inset-0 pointer-events-none z-[100] overflow-hidden";
    document.body.appendChild(container);

    const emojis = type === "fire" ? ["🔥", "💥", "⚡", "✨"] : ["🎁", "🪙", "💎", "💰", "✨"];
    const count = 40;

    for (let i = 0; i < count; i++) {
        const item = document.createElement("div");
        item.innerText = emojis[Math.floor(Math.random() * emojis.length)];
        item.style.position = "absolute";
        item.style.left = Math.random() * 100 + "vw";
        item.style.top = "-50px";
        item.style.fontSize = Math.random() * 24 + 16 + "px";
        item.style.userSelect = "none";
        
        const duration = Math.random() * 2 + 1.5;
        const delay = Math.random() * 0.5;
        const rotation = Math.random() * 360;
        
        item.animate([
            { transform: `translateY(0) rotate(0deg)`, opacity: 1 },
            { transform: `translateY(110vh) rotate(${rotation + 360}deg)`, opacity: 0 }
        ], {
            duration: duration * 1000,
            delay: delay * 1000,
            easing: "cubic-bezier(0.1, 0.8, 0.3, 1)"
        });

        container.appendChild(item);
    }

    setTimeout(() => {
        container.remove();
    }, 4000);
}

function triggerRewardedAdFlow(callback) {
    adCallback = callback;
    const modal = document.getElementById("rewarded-ad-modal");
    modal.classList.remove("hidden");

    // Reset states
    const stateLoading = document.getElementById("ad-state-loading");
    const stateError = document.getElementById("ad-state-error");
    const statePlaying = document.getElementById("ad-state-playing");

    stateLoading.classList.remove("hidden");
    stateError.classList.add("hidden");
    statePlaying.classList.add("hidden");

    // Connect to force ad fail click
    const forceFailBtn = document.getElementById("btn-force-ad-fail");
    if (forceFailBtn) {
        forceFailBtn.onclick = (e) => {
            e.stopPropagation();
            forceAdFail = true;
            showToast("Sponsor network failure scheduled!", "rose");
        };
    }

    // Simulate ad loading for 1.2 seconds
    setTimeout(() => {
        // 20% natural failure chance, or manual force
        const shouldFail = forceAdFail || (Math.random() < 0.20);
        forceAdFail = false; // reset flag

        stateLoading.classList.add("hidden");

        if (shouldFail) {
            // SHOW ERROR STATE
            stateError.classList.remove("hidden");
            showToast("Connection Error: Could not load ad.", "rose");

            // Wire up retry button
            document.getElementById("btn-ad-retry").onclick = () => {
                triggerRewardedAdFlow(callback);
            };

            // Wire up fallback button
            document.getElementById("btn-ad-fallback").onclick = () => {
                modal.classList.add("hidden");
                state.profile.cash += 5000.0;
                saveState();
                refreshUI();
                showToast("Ad failed to load. Direct Credit Fallback +₹5,000 Cash applied!", "amber");
                triggerCelebrationAnimation("presents");
                adCallback = null;
            };
        } else {
            // SHOW PLAYING STATE
            statePlaying.classList.remove("hidden");

            let remaining = 5;
            const countdown = document.getElementById("ad-timer-countdown");
            const closeBtn = document.getElementById("btn-close-ad");
            countdown.innerText = remaining;
            closeBtn.disabled = true;
            closeBtn.innerText = `WAITING FOR SPONSOR SPOTLIGHT (${remaining}s)`;
            closeBtn.className = "w-full py-3 rounded-xl font-bold bg-slate-800 text-slate-500 cursor-not-allowed text-xs transition-all";

            const interval = setInterval(() => {
                remaining--;
                countdown.innerText = remaining;
                closeBtn.innerText = `WAITING FOR SPONSOR SPOTLIGHT (${remaining}s)`;
                if (remaining <= 0) {
                    clearInterval(interval);
                    closeBtn.disabled = false;
                    closeBtn.innerText = "CLOSE SANDBOX SPONSOR & CLAIM REWARD";
                    closeBtn.className = "w-full py-3 rounded-xl font-bold bg-cyan-500 hover:bg-cyan-600 text-white shadow-lg shadow-cyan-500/10 cursor-pointer text-xs transition-all";
                }
            }, 1000);
        }
    }, 1200);
}

function closeSimulatedAd() {
    const modal = document.getElementById("rewarded-ad-modal");
    modal.classList.add("hidden");
    if (adCallback) {
        adCallback();
        adCallback = null;
        triggerCelebrationAnimation("fire");
    }
}

// 12. Main UI Rendering & Event Handling
function refreshUI() {
    const isINR = state.profile.currency === "INR";
    const prefCur = state.profile.currency;

    // Header updates
    const netWorth = calculateNetWorth();
    document.getElementById("portfolio-net-worth").innerText = formatCurrency(netWorth);
    document.getElementById("portfolio-cash").innerText = formatCurrency(state.profile.cash);
    document.getElementById("portfolio-holdings").innerText = formatCurrency(calculateHoldingsValue());
    document.getElementById("portfolio-risk").innerText = state.profile.riskPreference;
    document.getElementById("brokerage-credits-value").innerText = state.profile.brokerageCredits;

    // Portfolio change pct
    const totalPnL = netWorth - state.profile.startingCash;
    const pnlPct = state.profile.startingCash > 0 ? (totalPnL / state.profile.startingCash) * 100 : 0;
    const changeBadge = document.getElementById("portfolio-change-badge");
    const sign = totalPnL >= 0 ? "+" : "";
    changeBadge.innerText = `${sign}${formatCurrency(totalPnL)} (${sign}${pnlPct.toFixed(2)}%)`;
    if (totalPnL >= 0) {
        changeBadge.className = "inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-0.5 rounded-full mt-2 bg-emerald-500/10 text-emerald-400 border border-emerald-500/20";
    } else {
        changeBadge.className = "inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-0.5 rounded-full mt-2 bg-rose-500/10 text-rose-400 border border-rose-500/20";
    }

    // Active Holdings Table
    const tableBody = document.getElementById("holdings-table-body");
    document.getElementById("holdings-count").innerText = state.holdings.length;
    if (state.holdings.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="6" class="py-8 text-center text-slate-500 text-xs font-semibold">
                    No active positions in portfolio. Open the Watchlist Desk to purchase shares.
                </td>
            </tr>
        `;
    } else {
        let hHtml = "";
        state.holdings.forEach(holding => {
            const stock = state.stockPrices.find(s => s.symbol === holding.symbol);
            if (!stock) return;

            const currentPriceInPref = getConvertedPrice(stock.currentPrice, holding.symbol, prefCur);
            const avgPriceInPref = getConvertedPrice(holding.averagePrice, holding.symbol, prefCur);
            const marketVal = holding.shares * currentPriceInPref;
            const costBasis = holding.shares * avgPriceInPref;
            const pnl = marketVal - costBasis;
            const pnlPct = costBasis > 0 ? (pnl / costBasis) * 100 : 0;

            const pnlColor = pnl >= 0 ? "text-emerald-400" : "text-rose-400";
            const pnlSign = pnl >= 0 ? "+" : "";

            hHtml += `
                <tr class="border-b border-slate-800/40 hover:bg-slate-800/10 transition-all">
                    <td class="py-4 pl-2">
                        <span class="block font-bold text-slate-100">${holding.symbol}</span>
                        <span class="text-[10px] text-slate-500 font-medium">${stock.companyName}</span>
                    </td>
                    <td class="py-4 text-right font-mono">${holding.shares.toFixed(2)}</td>
                    <td class="py-4 text-right font-mono">${formatCurrency(avgPriceInPref)}</td>
                    <td class="py-4 text-right font-mono">${formatCurrency(currentPriceInPref)}</td>
                    <td class="py-4 text-right font-mono">${formatCurrency(marketVal)}</td>
                    <td class="py-4 text-right pr-2 ${pnlColor} font-mono font-bold">${pnlSign}${formatCurrency(pnl)} (${pnlSign}${pnlPct.toFixed(1)}%)</td>
                </tr>
            `;
        });
        tableBody.innerHTML = hHtml;
    }

    // Pending Orders panel
    const pendingContainer = document.getElementById("pending-orders-container");
    const activePendings = state.pendingOrders.filter(o => o.status === "PENDING");
    document.getElementById("pending-count").innerText = activePendings.length;
    if (activePendings.length === 0) {
        pendingContainer.innerHTML = `
            <div class="border border-dashed border-slate-800 rounded-xl p-4 text-center text-slate-500 text-xs font-semibold">
                No active Limit/Stop triggers configured.
            </div>
        `;
    } else {
        let pHtml = "";
        activePendings.forEach(order => {
            pHtml += `
                <div class="p-3 bg-slate-900 border border-slate-800 rounded-xl flex items-center justify-between text-xs">
                    <div>
                        <span class="font-bold text-slate-200">${order.symbol}</span>
                        <span class="mx-1.5 px-1.5 py-0.5 rounded text-[10px] font-bold uppercase ${order.type === 'BUY' ? 'bg-emerald-500/10 text-emerald-400' : 'bg-rose-500/10 text-rose-400'}">${order.type}</span>
                        <span class="text-slate-400 font-medium font-mono">${order.shares} shrs @ ${order.orderType}</span>
                    </div>
                    <div class="flex items-center gap-3">
                        <span class="font-bold text-slate-200 font-mono">${formatCurrency(order.triggerPrice)}</span>
                        <button onclick="cancelPendingOrder(${order.id})" class="text-rose-400 hover:text-rose-300 font-bold uppercase text-[10px]">Cancel</button>
                    </div>
                </div>
            `;
        });
        pendingContainer.innerHTML = pHtml;
    }

    // Transactions History panel
    const transContainer = document.getElementById("transactions-container");
    if (state.transactions.length === 0) {
        transContainer.innerHTML = `
            <div class="border border-dashed border-slate-800 rounded-xl p-4 text-center text-slate-500 text-xs font-semibold">
                Transaction history is empty.
            </div>
        `;
    } else {
        let tHtml = "";
        state.transactions.forEach(t => {
            const date = new Date(t.timestamp).toLocaleTimeString();
            tHtml += `
                <div class="p-3 bg-slate-900/40 border border-slate-800/50 rounded-xl flex items-center justify-between text-xs">
                    <div>
                        <span class="font-bold text-slate-300">${t.symbol}</span>
                        <span class="mx-1.5 px-1.5 py-0.5 rounded text-[10px] font-bold uppercase ${t.type === 'BUY' ? 'bg-emerald-500/10 text-emerald-400' : 'bg-rose-500/10 text-rose-400'}">${t.type}</span>
                        <span class="text-slate-500 font-medium font-mono">${t.shares} shrs (${t.executionType})</span>
                    </div>
                    <div class="text-right">
                        <span class="font-bold text-slate-300 font-mono block">${formatCurrency(t.price)}</span>
                        <span class="text-[9px] text-slate-600 block mt-0.5">${date}</span>
                    </div>
                </div>
            `;
        });
        transContainer.innerHTML = tHtml;
    }

    // Watchlist sidebar stocks list
    const watchlistContainer = document.getElementById("watchlist-stocks-container");
    const searchVal = document.getElementById("stock-search").value.toLowerCase().trim();
    let wHtml = "";
    state.stockPrices.forEach(stock => {
        if (searchVal && !stock.symbol.toLowerCase().includes(searchVal) && !stock.companyName.toLowerCase().includes(searchVal)) return;

        const isSelected = stock.symbol === state.selectedSymbol;
        const activeClass = isSelected ? "border-cyan-500/40 bg-cyan-500/10" : "border-slate-800/80 bg-slate-900/40";
        const convertedPrice = getConvertedPrice(stock.currentPrice, stock.symbol, prefCur);
        const changeSign = stock.dailyChangePct >= 0 ? "+" : "";
        const changeColor = stock.dailyChangePct >= 0 ? "text-emerald-400" : "text-rose-400";

        wHtml += `
            <div onclick="selectWatchlistStock('${stock.symbol}')" class="p-3 border rounded-xl flex items-center justify-between text-xs cursor-pointer hover:border-slate-700 transition-all ${activeClass}">
                <div>
                    <strong class="text-slate-100 font-bold block">${stock.symbol}</strong>
                    <span class="text-[10px] text-slate-500 block max-w-[120px] truncate">${stock.companyName}</span>
                </div>
                <div class="text-right">
                    <strong class="text-slate-200 font-bold block font-mono">${formatCurrency(convertedPrice)}</strong>
                    <span class="font-bold font-mono text-[10px] ${changeColor}">${changeSign}${stock.dailyChangePct}%</span>
                </div>
            </div>
        `;
    });
    watchlistContainer.innerHTML = wHtml || '<div class="text-center py-4 text-slate-500 text-xs">No matching symbols found.</div>';

    // Selected Workstation Update
    const currentStock = state.stockPrices.find(s => s.symbol === state.selectedSymbol);
    if (currentStock) {
        document.getElementById("chart-stock-icon").innerText = currentStock.symbol;
        document.getElementById("chart-stock-name").innerText = currentStock.companyName;
        document.getElementById("chart-stock-symbol").innerText = `${currentStock.symbol} • Indian Equity`;
        
        const priceInPref = getConvertedPrice(currentStock.currentPrice, currentStock.symbol, prefCur);
        document.getElementById("chart-stock-price").innerText = formatCurrency(priceInPref);
        
        const changeSign = currentStock.dailyChangePct >= 0 ? "+" : "";
        const changeColor = currentStock.dailyChangePct >= 0 ? "text-emerald-400" : "text-rose-400";
        document.getElementById("chart-stock-change").innerText = `${changeSign}${currentStock.dailyChangePct}%`;
        document.getElementById("chart-stock-change").className = `text-xs font-bold ${changeColor} font-mono mt-1 block`;

        // Limits stats row
        const highPriceInPref = getConvertedPrice(currentStock.highPrice, currentStock.symbol, prefCur);
        const lowPriceInPref = getConvertedPrice(currentStock.lowPrice, currentStock.symbol, prefCur);
        const prevCloseInPref = getConvertedPrice(currentStock.previousClose, currentStock.symbol, prefCur);

        document.getElementById("val-prev-close").innerText = formatCurrency(prevCloseInPref);
        document.getElementById("val-day-high").innerText = formatCurrency(highPriceInPref);
        document.getElementById("val-day-low").innerText = formatCurrency(lowPriceInPref);
        document.getElementById("val-asset-class").innerText = "Nifty 50 Blue-chip";

        document.getElementById("trigger-currency-symbol").innerText = "₹";

        drawCharts();
    }

    // Missions widget update
    loadMissionsList();
}

function selectWatchlistStock(symbol) {
    state.selectedSymbol = symbol;
    saveState();
    refreshUI();
    updateTradingOrderBreakdown();
}

function cancelPendingOrder(id) {
    state.pendingOrders = state.pendingOrders.filter(o => o.id !== id);
    saveState();
    refreshUI();
    showToast("Trigger Order cancelled successfully.", "cyan");
}

// Global Tab Toggler
document.querySelectorAll(".tab-btn").forEach(btn => {
    btn.addEventListener("click", () => {
        const tab = btn.getAttribute("data-tab");
        state.activeTab = tab;

        // Toggle active button style
        document.querySelectorAll(".tab-btn").forEach(b => {
            b.className = "tab-btn flex-1 md:flex-none flex items-center justify-center md:justify-start gap-3 px-4 py-3 rounded-xl font-medium transition-all text-sm text-slate-400 hover:bg-slate-800/40 hover:text-slate-200";
        });
        if (tab === "ai-hub") {
            btn.className = "tab-btn flex-1 md:flex-none flex items-center justify-center md:justify-start gap-3 px-4 py-3 rounded-xl font-medium transition-all text-sm bg-indigo-600/20 text-indigo-400 border border-indigo-500/30";
        } else {
            btn.className = "tab-btn flex-1 md:flex-none flex items-center justify-center md:justify-start gap-3 px-4 py-3 rounded-xl font-medium transition-all text-sm bg-cyan-500/10 text-cyan-400 border border-cyan-500/20";
        }

        // Toggle panels
        document.querySelectorAll(".tab-panel").forEach(p => p.classList.add("hidden"));
        document.getElementById(`panel-${tab}`).classList.remove("hidden");

        if (tab === "academy") {
            loadAcademyModules();
        }

        drawCharts();
    });
});

// Toast system
function showToast(msg, theme = "cyan") {
    const toast = document.getElementById("toast-notification");
    toast.innerText = msg;
    
    // Theme colors
    if (theme === "emerald") {
        toast.className = "fixed bottom-6 right-6 px-5 py-3.5 rounded-xl border border-emerald-500/20 bg-[#0d1c22] shadow-xl text-xs font-semibold flex items-center gap-3 transition-all transform opacity-100 z-50 text-emerald-400 glow-emerald";
    } else if (theme === "rose") {
        toast.className = "fixed bottom-6 right-6 px-5 py-3.5 rounded-xl border border-rose-500/20 bg-[#210f13] shadow-xl text-xs font-semibold flex items-center gap-3 transition-all transform opacity-100 z-50 text-rose-400 glow-rose";
    } else {
        toast.className = "fixed bottom-6 right-6 px-5 py-3.5 rounded-xl border border-cyan-500/20 bg-[#0d1d2b] shadow-xl text-xs font-semibold flex items-center gap-3 transition-all transform opacity-100 z-50 text-cyan-400 glow-cyan";
    }

    setTimeout(() => {
        toast.className = "fixed bottom-6 right-6 px-5 py-3.5 rounded-xl border border-slate-800 bg-[#0D1527] shadow-xl text-xs font-semibold flex items-center gap-3 transition-all transform translate-y-20 opacity-0 z-50";
    }, 3000);
}

// Order forms triggers
document.querySelectorAll(".order-type-btn").forEach(btn => {
    btn.addEventListener("click", () => {
        const type = btn.getAttribute("data-order-type");
        state.selectedOrderType = type;

        document.querySelectorAll(".order-type-btn").forEach(b => {
            b.className = "order-type-btn flex-1 pb-3 text-center border-b-2 border-transparent text-slate-400 font-semibold hover:text-slate-200";
        });
        btn.className = "order-type-btn flex-1 pb-3 text-center border-b-2 border-cyan-500 text-cyan-400 font-semibold";

        const triggerPriceWrapper = document.getElementById("trigger-price-input-wrapper");
        const triggerPriceLabel = document.getElementById("trigger-price-label");
        
        if (type === "Market") {
            triggerPriceWrapper.classList.add("hidden");
        } else {
            triggerPriceWrapper.classList.remove("hidden");
            triggerPriceLabel.innerText = `${type} Target Trigger Price`;
        }

        updateTradingOrderBreakdown();
    });
});

// Watch ad click handler
document.getElementById("brokerage-shield-pill").addEventListener("click", () => {
    triggerRewardedAdFlow(() => {
        state.profile.brokerageCredits = Math.min(1000, state.profile.brokerageCredits + 100);
        saveState();
        refreshUI();
        showToast("Simulated Ad Finished! Brokerage Shield credited +100 credits.", "emerald");
    });
});

// Emergency recharges
document.getElementById("btn-emergency-recharge").addEventListener("click", () => {
    triggerRewardedAdFlow(() => {
        state.profile.cash += getConvertedPrice(10000.0, "TATASTEEL", state.profile.currency);
        saveState();
        refreshUI();
        showToast("Emergency Bankruptcy Cash injected! Credited +₹10,000", "emerald");
    });
});

// Reset portfolio
document.getElementById("btn-reset-portfolio").addEventListener("click", () => {
    if (confirm("Reset and refund simulator back to original ₹10,000 baseline? Your holdings and transaction history will be cleared. This action requires supporting our sponsors by watching a quick ad.")) {
        triggerRewardedAdFlow(() => {
            localStorage.removeItem("tradelab_web_state");
            localStorage.removeItem("tradelab_web_missions");
            state = {
                profile: {
                    cash: 10000.0,
                    startingCash: 10000.0,
                    riskPreference: "Moderate",
                    currency: "INR",
                    brokerageCredits: 300,
                    completedLevels: [],
                    isCalibrated: false,
                    userName: "Retail Pioneer",
                    userEmail: "atulkpal@gmail.com",
                    investorPersonality: "Disciplined Compounder"
                },
                holdings: [],
                transactions: [],
                pendingOrders: [],
                stockPrices: JSON.parse(JSON.stringify(STOCKS_BASELINE)),
                selectedSymbol: "RELIANCE",
                selectedOrderType: "Market",
                activeTab: "portfolio",
                showSMA: false,
                showEMA: false,
                showRSI: false
            };
            saveState();
            refreshUI();
            showToast("Simulator wallet successfully refunded & reset to defaults!", "cyan");
        });
    }
});

// Buy and Sell executers
document.getElementById("order-buy-btn").addEventListener("click", () => {
    const stock = state.stockPrices.find(s => s.symbol === state.selectedSymbol);
    if (!stock) return;

    const sharesInput = document.getElementById("order-shares-input");
    const shares = parseFloat(sharesInput.value) || 0;
    if (shares <= 0) {
        showToast("Please enter a valid shares quantity.", "rose");
        return;
    }

    let executionPrice = stock.currentPrice;
    if (state.selectedOrderType !== "Market") {
        const triggerInput = document.getElementById("order-trigger-input");
        executionPrice = parseFloat(triggerInput.value) || 0;
        if (executionPrice <= 0) {
            showToast("Please enter a valid execution trigger price.", "rose");
            return;
        }
    }

    const convertedPrice = getConvertedPrice(executionPrice, stock.symbol, state.profile.currency);
    const cost = shares * convertedPrice;
    let fee = state.profile.brokerageCredits > 0 ? 0 : (state.profile.currency === "INR" ? 20.0 : 0.25);
    const totalCost = cost + fee;

    if (state.profile.cash < totalCost) {
        showToast("Insufficient cash balance on-hand to process trade.", "rose");
        return;
    }

    if (state.selectedOrderType === "Market") {
        executeTransaction(stock.symbol, "BUY", shares, stock.currentPrice, "Market");
        showToast(`Market BUY Executed: ${shares} ${stock.symbol} shares purchased.`, "emerald");
        sharesInput.value = "";
    } else {
        // Place Pending Trigger order
        state.pendingOrders.unshift({
            id: Date.now(),
            symbol: stock.symbol,
            type: "BUY",
            orderType: state.selectedOrderType,
            shares,
            triggerPrice: executionPrice,
            status: "PENDING",
            timestamp: Date.now()
        });
        saveState();
        showToast(`Trigger Order Placed: BUY ${shares} ${stock.symbol} when price hits ${formatCurrency(executionPrice, stock.currency)}`, "cyan");
        sharesInput.value = "";
    }
    
    refreshUI();
    updateTradingOrderBreakdown();
});

document.getElementById("order-sell-btn").addEventListener("click", () => {
    const stock = state.stockPrices.find(s => s.symbol === state.selectedSymbol);
    if (!stock) return;

    const sharesInput = document.getElementById("order-shares-input");
    const shares = parseFloat(sharesInput.value) || 0;
    if (shares <= 0) {
        showToast("Please enter a valid shares quantity.", "rose");
        return;
    }

    const existingHolding = state.holdings.find(h => h.symbol === stock.symbol);
    if (!existingHolding || existingHolding.shares < shares) {
        showToast("Insufficient shares in portfolio to execute sell order.", "rose");
        return;
    }

    let executionPrice = stock.currentPrice;
    if (state.selectedOrderType !== "Market") {
        const triggerInput = document.getElementById("order-trigger-input");
        executionPrice = parseFloat(triggerInput.value) || 0;
        if (executionPrice <= 0) {
            showToast("Please enter a valid execution trigger price.", "rose");
            return;
        }
    }

    if (state.selectedOrderType === "Market") {
        executeTransaction(stock.symbol, "SELL", shares, stock.currentPrice, "Market");
        showToast(`Market SELL Executed: Sold ${shares} shares of ${stock.symbol}`, "emerald");
        sharesInput.value = "";
    } else {
        // Place Pending Trigger order
        state.pendingOrders.unshift({
            id: Date.now(),
            symbol: stock.symbol,
            type: "SELL",
            orderType: state.selectedOrderType,
            shares,
            triggerPrice: executionPrice,
            status: "PENDING",
            timestamp: Date.now()
        });
        saveState();
        showToast(`Trigger Order Placed: SELL ${shares} ${stock.symbol} when price hits ${formatCurrency(executionPrice, stock.currency)}`, "cyan");
        sharesInput.value = "";
    }

    refreshUI();
    updateTradingOrderBreakdown();
});

// Indicators click handlers
document.getElementById("btn-overlay-sma").addEventListener("click", (e) => {
    state.showSMA = !state.showSMA;
    e.target.className = state.showSMA ? 
        "px-3 py-1.5 rounded-lg border border-amber-500 bg-amber-500/10 text-amber-400 font-semibold transition-all" : 
        "px-3 py-1.5 rounded-lg border border-slate-800 hover:border-amber-500/50 bg-slate-900/60 font-semibold transition-all hover:text-slate-200";
    drawCharts();
});

document.getElementById("btn-overlay-ema").addEventListener("click", (e) => {
    state.showEMA = !state.showEMA;
    e.target.className = state.showEMA ? 
        "px-3 py-1.5 rounded-lg border border-fuchsia-500 bg-fuchsia-500/10 text-fuchsia-400 font-semibold transition-all" : 
        "px-3 py-1.5 rounded-lg border border-slate-800 hover:border-fuchsia-500/50 bg-slate-900/60 font-semibold transition-all hover:text-slate-200";
    drawCharts();
});

document.getElementById("btn-overlay-rsi").addEventListener("click", (e) => {
    state.showRSI = !state.showRSI;
    e.target.className = state.showRSI ? 
        "px-3 py-1.5 rounded-lg border border-indigo-500 bg-indigo-500/10 text-indigo-400 font-semibold transition-all" : 
        "px-3 py-1.5 rounded-lg border border-slate-800 hover:border-indigo-500/50 bg-slate-900/60 font-semibold transition-all hover:text-slate-200";
    drawCharts();
});

// Search input listeners
document.getElementById("stock-search").addEventListener("input", refreshUI);

// Event attachments
document.getElementById("order-shares-input").addEventListener("input", updateTradingOrderBreakdown);
document.getElementById("order-trigger-input").addEventListener("input", updateTradingOrderBreakdown);
document.getElementById("btn-close-ad").addEventListener("click", closeSimulatedAd);
document.getElementById("btn-trigger-ai-audit").addEventListener("click", runBehavioralAIDiagnosis);
document.getElementById("btn-submit-profiler").addEventListener("click", submitProfilerQuiz);

// Currency buttons
document.getElementById("currency-inr-btn").addEventListener("click", (e) => {
    if (state.profile.currency !== "INR") {
        state.profile.cash = state.profile.cash * USD_INR_RATE;
        state.profile.startingCash = state.profile.startingCash * USD_INR_RATE;
        state.profile.currency = "INR";
        
        e.target.className = "px-3 py-1.5 rounded-md font-semibold bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 transition-all";
        document.getElementById("currency-usd-btn").className = "px-3 py-1.5 rounded-md font-semibold text-slate-400 hover:text-slate-200 transition-all";
        
        saveState();
        refreshUI();
        updateTradingOrderBreakdown();
    }
});

document.getElementById("currency-usd-btn").addEventListener("click", (e) => {
    if (state.profile.currency !== "USD") {
        state.profile.cash = state.profile.cash / USD_INR_RATE;
        state.profile.startingCash = state.profile.startingCash / USD_INR_RATE;
        state.profile.currency = "USD";
        
        e.target.className = "px-3 py-1.5 rounded-md font-semibold bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 transition-all";
        document.getElementById("currency-inr-btn").className = "px-3 py-1.5 rounded-md font-semibold text-slate-400 hover:text-slate-200 transition-all";
        
        saveState();
        refreshUI();
        updateTradingOrderBreakdown();
    }
});

// Start application
loadState();
refreshUI();
startMarketSimulation();
updateTradingOrderBreakdown();
drawCharts();
