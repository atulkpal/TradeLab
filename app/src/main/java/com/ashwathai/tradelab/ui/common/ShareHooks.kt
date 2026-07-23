package com.ashwathai.tradelab.ui.common

import com.ashwathai.tradelab.ui.PortfolioStats

object ShareHooks {
    private val PROFIT_HOOKS = listOf(
        "Flipped my capital! 🚀",
        "Market is red, my portfolio is green! 🔥",
        "Compounding is my new personality trait. 📈",
        "Waking up to gains is the best vibe. 💎",
        "Investing > Influencing. 💸",
        "Porto is looking juicy today! 🦁",
        "To the moon? No, to the bank! 🏦"
    )

    private val LOSS_HOOKS = listOf(
        "Buy the dip, they said... 📉",
        "Learning the hard way (and loving it). 🧠",
        "Diamond hands in a paper market. 💎",
        "Volatility is my teacher today. 🌪️",
        "Red is just a discount on future gains. 🛍️",
        "Market humble-pi today. 🥧",
        "Positions are temporary, knowledge is forever. 📖"
    )

    private val NEUTRAL_HOOKS = listOf(
        "Disciplined trading only. ⚖️",
        "Building my empire, one trade at a time. 🏰",
        "Consistency > Luck. 🎯",
        "Watching the charts like a pro. 🧐",
        "Trade Lab is where I train. 🏋️",
        "Paper trading, real strategies. 🛠️"
    )

    fun getRandomHook(stats: PortfolioStats): String {
        val hooks = when {
            stats.totalPnLPct > 2.0 -> PROFIT_HOOKS
            stats.totalPnLPct < -2.0 -> LOSS_HOOKS
            else -> NEUTRAL_HOOKS
        }
        return hooks.random()
    }
}
