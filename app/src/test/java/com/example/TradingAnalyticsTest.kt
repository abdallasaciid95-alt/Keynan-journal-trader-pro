package com.example

import com.example.data.analytics.TradingAnalyticsEngine
import com.example.data.model.AccountTransaction
import com.example.data.model.Trade
import com.example.data.model.TradingAccount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TradingAnalyticsTest {

  @Test
  fun testAccountBalanceCalculation() {
    val account = TradingAccount(
      id = "acc1",
      userId = "user1",
      name = "Funded Gold",
      startingBalance = 50000.0
    )

    val trades = listOf(
      Trade(accountId = "acc1", result = "win", pnl = 1500.0),
      Trade(accountId = "acc1", result = "loss", pnl = -500.0),
      Trade(accountId = "acc1", result = "open", pnl = 0.0) // Open trade should not count
    )

    val txs = listOf(
      AccountTransaction(accountId = "acc1", type = "deposit", amount = 2000.0),
      AccountTransaction(accountId = "acc1", type = "withdrawal", amount = 1000.0)
    )

    val summary = TradingAnalyticsEngine.calculateAccountBalance(account, trades, txs)

    // Starting (50000) + Net Pnl (1000) + Deposits (2000) - Withdrawals (1000) = 52000
    assertEquals(52000.0, summary.calculatedBalance, 0.001)
    assertEquals(1000.0, summary.netTradingPnl, 0.001)
  }

  @Test
  fun testPerformanceMetricsWinRateAndStreaks() {
    val trades = listOf(
      Trade(date = "2026-11-01", entryTime = "09:00", result = "win", pnl = 1000.0, entryPrice = 2740.0, stopLossPrice = 2730.0, takeProfitPrice = 2760.0),
      Trade(date = "2026-11-01", entryTime = "14:00", result = "win", pnl = 500.0),
      Trade(date = "2026-11-02", entryTime = "10:00", result = "loss", pnl = -400.0),
      Trade(date = "2026-11-02", entryTime = "15:00", result = "win", pnl = 800.0)
    )

    val perf = TradingAnalyticsEngine.calculatePerformance(trades, startingEquity = 10000.0)

    assertEquals(4, perf.totalTrades)
    assertEquals(3, perf.winningTrades)
    assertEquals(1, perf.losingTrades)
    assertEquals(75.0, perf.winRate, 0.01)
    assertEquals(1900.0, perf.netPnl, 0.01)
    assertEquals(2, perf.longestWinStreak)
  }
}
