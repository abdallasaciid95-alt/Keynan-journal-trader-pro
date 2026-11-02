package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class TradingAccount(
  @DocumentId val id: String = "",
  val userId: String = "",
  val name: String = "",
  val type: String = "personal", // personal, challenge_phase_1, challenge_phase_2, funded
  val startingBalance: Double = 0.0,
  val profitTarget: Double? = null,
  val maxDailyLossLimit: Double? = null,
  val maxOverallDrawdownLimit: Double? = null,
  val startDate: String = "",
  val status: String = "active", // active, passed, failed, archived
  val notes: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  fun getDisplayType(): String = when (type) {
    "personal" -> "Personal Account"
    "challenge_phase_1" -> "Challenge Phase 1"
    "challenge_phase_2" -> "Challenge Phase 2"
    "funded" -> "Funded Account"
    else -> type.replace("_", " ").replaceFirstChar { it.uppercase() }
  }

  fun getDisplayStatus(): String = when (status) {
    "active" -> "Active"
    "passed" -> "Passed"
    "failed" -> "Failed"
    "archived" -> "Archived"
    else -> status.replaceFirstChar { it.uppercase() }
  }
}

@IgnoreExtraProperties
data class Trade(
  @DocumentId val id: String = "",
  val userId: String = "",
  val accountId: String = "",
  val instrument: String = "XAUUSD", // Strictly Gold
  val date: String = "", // YYYY-MM-DD
  val entryTime: String = "", // HH:mm
  val direction: String = "BUY", // BUY, SELL
  val entryPrice: Double = 0.0,
  val exitPrice: Double = 0.0,
  val lotSize: Double = 0.01,
  val stopLossPrice: Double? = null,
  val takeProfitPrice: Double? = null,
  val result: String = "open", // win, loss, breakeven, open
  val pnl: Double = 0.0, // Authoritative realized USD P/L (0 for open)
  val session: String = "london", // asian, london, new_york, other
  val strategy: String = "",
  val entryReason: String = "",
  val marketConditions: String = "",
  val emotions: String = "",
  val mistakes: String = "",
  val lessons: String = "",
  val notes: String = "",
  val screenshotUrl: String = "",
  val followedPlan: String = "yes", // yes, no, na
  val respectedStopLoss: String = "yes", // yes, no, na
  val enteredByStrategy: String = "yes", // yes, no, na
  val emotionalTrading: String = "no", // yes, no, na
  val overtraded: String = "no", // yes, no, na
  val whatLearned: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  fun isOpen(): Boolean = result.equals("open", ignoreCase = true)
  fun isWin(): Boolean = result.equals("win", ignoreCase = true)
  fun isLoss(): Boolean = result.equals("loss", ignoreCase = true)
  fun isBreakeven(): Boolean = result.equals("breakeven", ignoreCase = true)

  fun getRiskToReward(): Double? {
    if (stopLossPrice == null || takeProfitPrice == null || entryPrice == 0.0) return null
    val risk = kotlin.math.abs(entryPrice - stopLossPrice)
    val reward = kotlin.math.abs(takeProfitPrice - entryPrice)
    return if (risk > 0.0001) reward / risk else null
  }

  fun getSessionDisplay(): String = when (session.lowercase()) {
    "asian" -> "Asian Session"
    "london" -> "London Session"
    "new_york" -> "New York Session"
    else -> "Other Session"
  }
}

@IgnoreExtraProperties
data class AccountTransaction(
  @DocumentId val id: String = "",
  val userId: String = "",
  val accountId: String = "",
  val type: String = "deposit", // deposit, withdrawal
  val amount: Double = 0.0,
  val date: String = "",
  val note: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserSettings(
  val userId: String = "",
  val defaultAccountId: String = "",
  val defaultSession: String = "london",
  val dailyProfitGoal: Double? = null,
  val maxTradesPerDay: Int? = null,
  val appLockEnabled: Boolean = false,
  val appLockPin: String = ""
)
