package com.example.data.analytics

import com.example.data.model.AccountTransaction
import com.example.data.model.Trade
import com.example.data.model.TradingAccount
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class AccountBalanceSummary(
  val accountId: String,
  val accountName: String,
  val accountType: String,
  val startingBalance: Double,
  val netTradingPnl: Double,
  val totalDeposits: Double,
  val totalWithdrawals: Double,
  val calculatedBalance: Double,
  val profitTarget: Double?,
  val maxDailyLossLimit: Double?,
  val maxOverallDrawdownLimit: Double?,
  val currentDrawdown: Double,
  val currentDrawdownPercent: Double,
  val targetProgressPercent: Double?
)

data class PerformanceMetrics(
  val totalTrades: Int = 0,
  val closedTradesCount: Int = 0,
  val openTradesCount: Int = 0,
  val winningTrades: Int = 0,
  val losingTrades: Int = 0,
  val breakevenTrades: Int = 0,
  val winRate: Double = 0.0,
  val grossProfit: Double = 0.0,
  val grossLoss: Double = 0.0,
  val netPnl: Double = 0.0,
  val averageWin: Double = 0.0,
  val averageLoss: Double = 0.0,
  val largestWin: Double = 0.0,
  val largestLoss: Double = 0.0,
  val profitFactor: Double = 0.0,
  val averageRiskReward: Double? = null,
  val maxDrawdownAmount: Double = 0.0,
  val maxDrawdownPercent: Double = 0.0,
  val currentStreak: StreakInfo = StreakInfo(),
  val longestWinStreak: Int = 0,
  val longestLossStreak: Int = 0
)

data class StreakInfo(
  val type: StreakType = StreakType.NONE,
  val count: Int = 0
) {
  fun getDisplay(): String = when (type) {
    StreakType.WIN -> "🔥 $count Win${if (count > 1) "s" else ""} Streak"
    StreakType.LOSS -> "⚠️ $count Loss${if (count > 1) "es" else ""} Streak"
    StreakType.NONE -> "No Active Streak"
  }
}

enum class StreakType { WIN, LOSS, NONE }

data class StrategyStats(
  val strategy: String,
  val totalTrades: Int,
  val winRate: Double,
  val netPnl: Double,
  val wins: Int,
  val losses: Int
)

data class SessionStats(
  val session: String,
  val displayName: String,
  val totalTrades: Int,
  val winRate: Double,
  val netPnl: Double
)

data class DayOfWeekStats(
  val dayName: String,
  val dayIndex: Int,
  val totalTrades: Int,
  val winRate: Double,
  val netPnl: Double
)

data class DisciplineSummary(
  val totalEvaluated: Int,
  val followedPlanCount: Int,
  val followedPlanPercent: Double,
  val respectedStopLossCount: Int,
  val respectedStopLossPercent: Double,
  val enteredByStrategyCount: Int,
  val enteredByStrategyPercent: Double,
  val emotionalTradingCount: Int,
  val overtradedCount: Int,
  val pnlWhenPlanFollowed: Double,
  val pnlWhenPlanNotFollowed: Double,
  val topMistakes: List<Pair<String, Int>>
)

object TradingAnalyticsEngine {

  fun calculateAccountBalance(
    account: TradingAccount,
    trades: List<Trade>,
    transactions: List<AccountTransaction>
  ): AccountBalanceSummary {
    val accountTrades = trades.filter { it.accountId == account.id && !it.isOpen() }
    val netTradingPnl = accountTrades.sumOf { it.pnl }

    val accountTransactions = transactions.filter { it.accountId == account.id }
    val totalDeposits = accountTransactions.filter { it.type == "deposit" }.sumOf { it.amount }
    val totalWithdrawals = accountTransactions.filter { it.type == "withdrawal" }.sumOf { it.amount }

    val calculatedBalance = account.startingBalance + netTradingPnl + totalDeposits - totalWithdrawals

    // Calculate drawdown relative to starting + deposits
    val baseEquity = account.startingBalance + totalDeposits - totalWithdrawals
    val currentDrawdown = if (calculatedBalance < baseEquity) baseEquity - calculatedBalance else 0.0
    val currentDrawdownPercent = if (baseEquity > 0) (currentDrawdown / baseEquity) * 100.0 else 0.0

    val targetProgressPercent = account.profitTarget?.let { target ->
      if (target > 0) {
        val profitAboveStart = calculatedBalance - (account.startingBalance + totalDeposits - totalWithdrawals)
        ((profitAboveStart / target) * 100.0).coerceIn(0.0, 100.0)
      } else null
    }

    return AccountBalanceSummary(
      accountId = account.id,
      accountName = account.name,
      accountType = account.getDisplayType(),
      startingBalance = account.startingBalance,
      netTradingPnl = netTradingPnl,
      totalDeposits = totalDeposits,
      totalWithdrawals = totalWithdrawals,
      calculatedBalance = calculatedBalance,
      profitTarget = account.profitTarget,
      maxDailyLossLimit = account.maxDailyLossLimit,
      maxOverallDrawdownLimit = account.maxOverallDrawdownLimit,
      currentDrawdown = currentDrawdown,
      currentDrawdownPercent = currentDrawdownPercent,
      targetProgressPercent = targetProgressPercent
    )
  }

  fun calculatePerformance(trades: List<Trade>, startingEquity: Double = 10000.0): PerformanceMetrics {
    val closedTrades = trades.filter { !it.isOpen() }.sortedBy { "${it.date} ${it.entryTime}" }
    val openTrades = trades.filter { it.isOpen() }

    if (closedTrades.isEmpty()) {
      return PerformanceMetrics(
        totalTrades = trades.size,
        closedTradesCount = 0,
        openTradesCount = openTrades.size
      )
    }

    var wins = 0
    var losses = 0
    var breakeven = 0
    var grossProfit = 0.0
    var grossLoss = 0.0
    var largestWin = 0.0
    var largestLoss = 0.0
    val rrList = mutableListOf<Double>()

    for (t in closedTrades) {
      if (t.isWin() || t.pnl > 0.0001) {
        wins++
        grossProfit += t.pnl
        if (t.pnl > largestWin) largestWin = t.pnl
      } else if (t.isLoss() || t.pnl < -0.0001) {
        losses++
        val lossAbs = kotlin.math.abs(t.pnl)
        grossLoss += lossAbs
        if (lossAbs > largestLoss) largestLoss = lossAbs
      } else {
        breakeven++
      }

      t.getRiskToReward()?.let { rr ->
        if (rr in 0.1..50.0) rrList.add(rr)
      }
    }

    val totalDecided = wins + losses + breakeven
    val winRate = if (totalDecided > 0) (wins.toDouble() / totalDecided.toDouble()) * 100.0 else 0.0
    val netPnl = grossProfit - grossLoss
    val avgWin = if (wins > 0) grossProfit / wins else 0.0
    val avgLoss = if (losses > 0) grossLoss / losses else 0.0
    val profitFactor = if (grossLoss > 0.0001) grossProfit / grossLoss else if (grossProfit > 0) grossProfit else 0.0
    val avgRr = if (rrList.isNotEmpty()) rrList.average() else null

    // Max Drawdown calculation over cumulative closed trade equity
    var peak = startingEquity
    var runningEquity = startingEquity
    var maxDd = 0.0
    var maxDdPct = 0.0

    for (t in closedTrades) {
      runningEquity += t.pnl
      if (runningEquity > peak) {
        peak = runningEquity
      }
      val dd = peak - runningEquity
      if (dd > maxDd) {
        maxDd = dd
        maxDdPct = if (peak > 0) (dd / peak) * 100.0 else 0.0
      }
    }

    // Streaks
    var curStreakCount = 0
    var curStreakType = StreakType.NONE
    var maxWinStreak = 0
    var maxLossStreak = 0

    var tempWinStreak = 0
    var tempLossStreak = 0

    for (t in closedTrades) {
      if (t.pnl > 0) {
        tempWinStreak++
        tempLossStreak = 0
        if (tempWinStreak > maxWinStreak) maxWinStreak = tempWinStreak
      } else if (t.pnl < 0) {
        tempLossStreak++
        tempWinStreak = 0
        if (tempLossStreak > maxLossStreak) maxLossStreak = tempLossStreak
      } else {
        // Breakeven maintains or resets streak
        tempWinStreak = 0
        tempLossStreak = 0
      }
    }

    // Check last trade for current streak
    val lastTrades = closedTrades.reversed()
    if (lastTrades.isNotEmpty()) {
      val firstPnl = lastTrades.first().pnl
      if (firstPnl > 0) {
        curStreakType = StreakType.WIN
        curStreakCount = lastTrades.takeWhile { it.pnl > 0 }.size
      } else if (firstPnl < 0) {
        curStreakType = StreakType.LOSS
        curStreakCount = lastTrades.takeWhile { it.pnl < 0 }.size
      }
    }

    return PerformanceMetrics(
      totalTrades = trades.size,
      closedTradesCount = closedTrades.size,
      openTradesCount = openTrades.size,
      winningTrades = wins,
      losingTrades = losses,
      breakevenTrades = breakeven,
      winRate = winRate,
      grossProfit = grossProfit,
      grossLoss = grossLoss,
      netPnl = netPnl,
      averageWin = avgWin,
      averageLoss = avgLoss,
      largestWin = largestWin,
      largestLoss = largestLoss,
      profitFactor = profitFactor,
      averageRiskReward = avgRr,
      maxDrawdownAmount = maxDd,
      maxDrawdownPercent = maxDdPct,
      currentStreak = StreakInfo(curStreakType, curStreakCount),
      longestWinStreak = maxWinStreak,
      longestLossStreak = maxLossStreak
    )
  }

  fun analyzeStrategies(trades: List<Trade>): List<StrategyStats> {
    val closed = trades.filter { !it.isOpen() && it.strategy.isNotBlank() }
    val grouped = closed.groupBy { it.strategy.trim() }

    return grouped.map { (strat, tList) ->
      val wins = tList.count { it.isWin() || it.pnl > 0 }
      val losses = tList.count { it.isLoss() || it.pnl < 0 }
      val rate = if (tList.isNotEmpty()) (wins.toDouble() / tList.size) * 100.0 else 0.0
      val net = tList.sumOf { it.pnl }
      StrategyStats(
        strategy = strat,
        totalTrades = tList.size,
        winRate = rate,
        netPnl = net,
        wins = wins,
        losses = losses
      )
    }.sortedByDescending { it.netPnl }
  }

  fun analyzeSessions(trades: List<Trade>): List<SessionStats> {
    val closed = trades.filter { !it.isOpen() }
    val sessions = listOf("asian" to "Asian", "london" to "London", "new_york" to "New York", "other" to "Other")

    return sessions.map { (key, display) ->
      val sTrades = closed.filter { it.session.equals(key, ignoreCase = true) }
      val wins = sTrades.count { it.isWin() || it.pnl > 0 }
      val rate = if (sTrades.isNotEmpty()) (wins.toDouble() / sTrades.size) * 100.0 else 0.0
      val net = sTrades.sumOf { it.pnl }
      SessionStats(
        session = key,
        displayName = display,
        totalTrades = sTrades.size,
        winRate = rate,
        netPnl = net
      )
    }
  }

  fun analyzeDayOfWeek(trades: List<Trade>): List<DayOfWeekStats> {
    val closed = trades.filter { !it.isOpen() }
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val cal = Calendar.getInstance()

    val days = listOf(
      Calendar.MONDAY to "Monday",
      Calendar.TUESDAY to "Tuesday",
      Calendar.WEDNESDAY to "Wednesday",
      Calendar.THURSDAY to "Thursday",
      Calendar.FRIDAY to "Friday"
    )

    return days.map { (dayCal, name) ->
      val dTrades = closed.filter { t ->
        try {
          val date = sdf.parse(t.date)
          if (date != null) {
            cal.time = date
            cal.get(Calendar.DAY_OF_WEEK) == dayCal
          } else false
        } catch (e: Exception) {
          false
        }
      }
      val wins = dTrades.count { it.isWin() || it.pnl > 0 }
      val rate = if (dTrades.isNotEmpty()) (wins.toDouble() / dTrades.size) * 100.0 else 0.0
      val net = dTrades.sumOf { it.pnl }
      DayOfWeekStats(
        dayName = name,
        dayIndex = dayCal,
        totalTrades = dTrades.size,
        winRate = rate,
        netPnl = net
      )
    }
  }

  fun analyzeDiscipline(trades: List<Trade>): DisciplineSummary {
    val closed = trades.filter { !it.isOpen() }
    if (closed.isEmpty()) {
      return DisciplineSummary(
        totalEvaluated = 0,
        followedPlanCount = 0,
        followedPlanPercent = 0.0,
        respectedStopLossCount = 0,
        respectedStopLossPercent = 0.0,
        enteredByStrategyCount = 0,
        enteredByStrategyPercent = 0.0,
        emotionalTradingCount = 0,
        overtradedCount = 0,
        pnlWhenPlanFollowed = 0.0,
        pnlWhenPlanNotFollowed = 0.0,
        topMistakes = emptyList()
      )
    }

    val planFollowed = closed.filter { it.followedPlan == "yes" }
    val planNotFollowed = closed.filter { it.followedPlan == "no" }
    val slRespected = closed.filter { it.respectedStopLoss == "yes" }
    val stratFollowed = closed.filter { it.enteredByStrategy == "yes" }
    val emotional = closed.filter { it.emotionalTrading == "yes" }
    val overtrade = closed.filter { it.overtraded == "yes" }

    val mistakeMap = mutableMapOf<String, Int>()
    closed.forEach { t ->
      if (t.mistakes.isNotBlank()) {
        t.mistakes.split(",", ";").forEach { m ->
          val clean = m.trim()
          if (clean.isNotBlank() && !clean.equals("none", ignoreCase = true)) {
            mistakeMap[clean] = (mistakeMap[clean] ?: 0) + 1
          }
        }
      }
    }

    return DisciplineSummary(
      totalEvaluated = closed.size,
      followedPlanCount = planFollowed.size,
      followedPlanPercent = (planFollowed.size.toDouble() / closed.size) * 100.0,
      respectedStopLossCount = slRespected.size,
      respectedStopLossPercent = (slRespected.size.toDouble() / closed.size) * 100.0,
      enteredByStrategyCount = stratFollowed.size,
      enteredByStrategyPercent = (stratFollowed.size.toDouble() / closed.size) * 100.0,
      emotionalTradingCount = emotional.size,
      overtradedCount = overtrade.size,
      pnlWhenPlanFollowed = planFollowed.sumOf { it.pnl },
      pnlWhenPlanNotFollowed = planNotFollowed.sumOf { it.pnl },
      topMistakes = mistakeMap.toList().sortedByDescending { it.second }.take(5)
    )
  }
}
