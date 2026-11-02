package com.example.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.analytics.TradingAnalyticsEngine
import com.example.data.model.Trade
import com.example.data.model.TradingAccount
import com.example.ui.components.PnlBarChart
import com.example.ui.components.SessionBadge
import com.example.ui.components.StatMetricCard
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.JournalBackground
import com.example.ui.theme.JournalBorder
import com.example.ui.theme.JournalSurface
import com.example.ui.theme.RedLoss
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun AnalyticsScreen(
  trades: List<Trade>,
  accounts: List<TradingAccount>,
  selectedAccountId: String?,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Overview", "Strategies", "Sessions", "Day of Week", "Discipline")

  val filteredTrades = remember(trades, selectedAccountId) {
    if (selectedAccountId == null) trades else trades.filter { it.accountId == selectedAccountId }
  }

  val metrics = remember(filteredTrades) {
    TradingAnalyticsEngine.calculatePerformance(filteredTrades)
  }
  val strategyStats = remember(filteredTrades) {
    TradingAnalyticsEngine.analyzeStrategies(filteredTrades)
  }
  val sessionStats = remember(filteredTrades) {
    TradingAnalyticsEngine.analyzeSessions(filteredTrades)
  }
  val dayStats = remember(filteredTrades) {
    TradingAnalyticsEngine.analyzeDayOfWeek(filteredTrades)
  }
  val discipline = remember(filteredTrades) {
    TradingAnalyticsEngine.analyzeDiscipline(filteredTrades)
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(JournalBackground)
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "XAUUSD Analytics",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
        color = TextPrimary
      )
      Surface(
        color = GoldAccent.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
      ) {
        Text(
          text = if (selectedAccountId == null) "All Accounts" else accounts.find { it.id == selectedAccountId }?.name ?: "Account",
          color = GoldAccent,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }
    }

    // Scrollable Tab Row
    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      containerColor = JournalSurface,
      contentColor = EmeraldProfit,
      edgePadding = 16.dp,
      divider = {}
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = {
            Text(
              text = title,
              color = if (selectedTab == index) EmeraldProfit else TextSecondary,
              fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
              fontSize = 13.sp
            )
          }
        )
      }
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      when (selectedTab) {
        0 -> {
          // TAB 0: OVERVIEW & PERFORMANCE
          item {
            PnlBarChart(trades = filteredTrades)
          }

          item {
            Text("FINANCIAL HEALTH & PROFITABILITY", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          }

          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              StatMetricCard(
                title = "Net Realized P/L",
                value = "${if (metrics.netPnl >= 0) "+" else ""}$${String.format(Locale.US, "%,.2f", metrics.netPnl)}",
                subtitle = "Gross +$${String.format(Locale.US, "%,.0f", metrics.grossProfit)} / -$${String.format(Locale.US, "%,.0f", metrics.grossLoss)}",
                valueColor = if (metrics.netPnl >= 0) EmeraldProfit else RedLoss,
                modifier = Modifier.weight(1f)
              )
              StatMetricCard(
                title = "Profit Factor",
                value = if (metrics.profitFactor > 0) String.format(Locale.US, "%.2f", metrics.profitFactor) else "0.00",
                subtitle = "Gross Profit / Gross Loss",
                valueColor = if (metrics.profitFactor >= 1.5) EmeraldProfit else TextPrimary,
                modifier = Modifier.weight(1f)
              )
            }
          }

          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              StatMetricCard(
                title = "Win Rate",
                value = "${String.format(Locale.US, "%.1f", metrics.winRate)}%",
                subtitle = "${metrics.winningTrades} Wins · ${metrics.losingTrades} Losses",
                valueColor = if (metrics.winRate >= 50.0) EmeraldProfit else RedLoss,
                modifier = Modifier.weight(1f)
              )
              StatMetricCard(
                title = "Avg Risk/Reward",
                value = metrics.averageRiskReward?.let { "1:${String.format(Locale.US, "%.2f", it)}" } ?: "N/A",
                subtitle = "Calculated from SL & TP",
                valueColor = GoldAccent,
                modifier = Modifier.weight(1f)
              )
            }
          }

          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              StatMetricCard(
                title = "Avg Win / Loss",
                value = "$${String.format(Locale.US, "%,.0f", metrics.averageWin)}",
                subtitle = "Avg Loss: $${String.format(Locale.US, "%,.0f", metrics.averageLoss)}",
                valueColor = EmeraldProfit,
                modifier = Modifier.weight(1f)
              )
              StatMetricCard(
                title = "Largest Trade",
                value = "+$${String.format(Locale.US, "%,.0f", metrics.largestWin)}",
                subtitle = "Largest Loss: -$${String.format(Locale.US, "%,.0f", metrics.largestLoss)}",
                valueColor = EmeraldProfit,
                modifier = Modifier.weight(1f)
              )
            }
          }

          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              StatMetricCard(
                title = "Max Drawdown",
                value = "$${String.format(Locale.US, "%,.2f", metrics.maxDrawdownAmount)}",
                subtitle = "${String.format(Locale.US, "%.1f", metrics.maxDrawdownPercent)}% equity dip",
                valueColor = RedLoss,
                modifier = Modifier.weight(1f)
              )
              StatMetricCard(
                title = "Win Streak",
                value = "${metrics.longestWinStreak} Wins",
                subtitle = "Loss Streak: ${metrics.longestLossStreak} Losses",
                valueColor = GoldAccent,
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        1 -> {
          // TAB 1: STRATEGY PERFORMANCE
          item {
            Text("PERFORMANCE BY STRATEGY", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          }

          if (strategyStats.isEmpty()) {
            item {
              EmptyAnalyticsCard(message = "Enter strategy names when logging trades to see comparative analytics.")
            }
          } else {
            val best = strategyStats.first()
            item {
              Surface(
                modifier = Modifier.fillMaxWidth(),
                color = EmeraldProfit.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldProfit.copy(alpha = 0.4f))
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = EmeraldProfit)
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text("Top Performing Strategy", fontSize = 11.sp, color = EmeraldProfit, fontWeight = FontWeight.Bold)
                    Text(best.strategy, fontSize = 15.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                    Text("Net: +$${String.format(Locale.US, "%,.2f", best.netPnl)} (${String.format(Locale.US, "%.1f", best.winRate)}% WR across ${best.totalTrades} trades)", fontSize = 12.sp, color = TextSecondary)
                  }
                }
              }
            }

            items(strategyStats.size) { idx ->
              val s = strategyStats[idx]
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = JournalSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(s.strategy, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                    val col = if (s.netPnl >= 0) EmeraldProfit else RedLoss
                    Text("${if (s.netPnl >= 0) "+" else ""}$${String.format(Locale.US, "%,.2f", s.netPnl)}", fontWeight = FontWeight.Black, color = col)
                  }
                  Spacer(modifier = Modifier.height(6.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("${s.totalTrades} Trades (${s.wins}W / ${s.losses}L)", fontSize = 12.sp, color = TextSecondary)
                    Text("Win Rate: ${String.format(Locale.US, "%.1f", s.winRate)}%", fontSize = 12.sp, color = if (s.winRate >= 50) EmeraldProfit else RedLoss, fontWeight = FontWeight.Bold)
                  }
                  Spacer(modifier = Modifier.height(6.dp))
                  LinearProgressIndicator(
                    progress = { (s.winRate / 100.0).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(4.dp),
                    color = EmeraldProfit,
                    trackColor = JournalBorder
                  )
                }
              }
            }
          }
        }

        2 -> {
          // TAB 2: SESSION ANALYSIS
          item {
            Text("PERFORMANCE BY TRADING SESSION", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          }

          items(sessionStats.size) { idx ->
            val sess = sessionStats[idx]
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = JournalSurface),
              shape = RoundedCornerShape(14.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    SessionBadge(session = sess.session)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(sess.displayName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                  }
                  val col = if (sess.netPnl >= 0) EmeraldProfit else RedLoss
                  Text(
                    text = "${if (sess.netPnl >= 0) "+" else ""}$${String.format(Locale.US, "%,.2f", sess.netPnl)}",
                    fontWeight = FontWeight.Black,
                    color = col,
                    fontSize = 15.sp
                  )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("${sess.totalTrades} Total Trades", fontSize = 12.sp, color = TextSecondary)
                  Text("Win Rate: ${String.format(Locale.US, "%.1f", sess.winRate)}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (sess.winRate >= 50) EmeraldProfit else RedLoss)
                }

                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                  progress = { (sess.winRate / 100.0).toFloat().coerceIn(0f, 1f) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                  color = EmeraldProfit,
                  trackColor = JournalBorder
                )
              }
            }
          }
        }

        3 -> {
          // TAB 3: DAY OF WEEK ANALYSIS
          item {
            Text("PERFORMANCE BY DAY OF WEEK", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          }

          items(dayStats.size) { idx ->
            val d = dayStats[idx]
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = JournalSurface),
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
            ) {
              Row(
                modifier = Modifier
                  .padding(14.dp)
                  .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(d.dayName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                  Text("${d.totalTrades} Trades · WR: ${String.format(Locale.US, "%.1f", d.winRate)}%", fontSize = 11.sp, color = TextSecondary)
                }
                val col = if (d.netPnl >= 0) EmeraldProfit else RedLoss
                Text(
                  text = "${if (d.netPnl >= 0) "+" else ""}$${String.format(Locale.US, "%,.2f", d.netPnl)}",
                  fontWeight = FontWeight.Black,
                  color = col,
                  fontSize = 15.sp
                )
              }
            }
          }
        }

        4 -> {
          // TAB 4: DISCIPLINE & PSYCHOLOGY
          item {
            Text("DISCIPLINE & PSYCHOLOGY METRICS", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          }

          if (discipline.totalEvaluated == 0) {
            item {
              EmptyAnalyticsCard(message = "Answer the discipline review questions when logging trades to view psychology reports.")
            }
          } else {
            // Plan Followed vs Not Followed comparison!
            item {
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = JournalSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
              ) {
                Column(modifier = Modifier.padding(16.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = GoldAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Trading Plan Compliance Impact", fontWeight = FontWeight.Bold, color = TextPrimary)
                  }
                  Spacer(modifier = Modifier.height(12.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Column {
                      Text("When Plan Followed", fontSize = 11.sp, color = EmeraldProfit)
                      Text(
                        "${if (discipline.pnlWhenPlanFollowed >= 0) "+" else ""}$${String.format(Locale.US, "%,.2f", discipline.pnlWhenPlanFollowed)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = if (discipline.pnlWhenPlanFollowed >= 0) EmeraldProfit else RedLoss
                      )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                      Text("When Plan NOT Followed", fontSize = 11.sp, color = RedLoss)
                      Text(
                        "${if (discipline.pnlWhenPlanNotFollowed >= 0) "+" else ""}$${String.format(Locale.US, "%,.2f", discipline.pnlWhenPlanNotFollowed)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = if (discipline.pnlWhenPlanNotFollowed >= 0) EmeraldProfit else RedLoss
                      )
                    }
                  }
                }
              }
            }

            // Rates
            item {
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = JournalSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
              ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                  DisciplineProgressBar(label = "Trading Plan Followed", percent = discipline.followedPlanPercent, count = discipline.followedPlanCount, total = discipline.totalEvaluated)
                  DisciplineProgressBar(label = "Stop Loss Respected", percent = discipline.respectedStopLossPercent, count = discipline.respectedStopLossCount, total = discipline.totalEvaluated)
                  DisciplineProgressBar(label = "Entered By Strategy", percent = discipline.enteredByStrategyPercent, count = discipline.enteredByStrategyCount, total = discipline.totalEvaluated)
                }
              }
            }

            // Most Frequent Mistakes
            if (discipline.topMistakes.isNotEmpty()) {
              item {
                Card(
                  modifier = Modifier.fillMaxWidth(),
                  colors = CardDefaults.cardColors(containerColor = JournalSurface),
                  shape = RoundedCornerShape(14.dp),
                  border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
                ) {
                  Column(modifier = Modifier.padding(16.dp)) {
                    Text("MOST FREQUENT MISTAKES", style = MaterialTheme.typography.labelSmall, color = RedLoss)
                    Spacer(modifier = Modifier.height(10.dp))
                    discipline.topMistakes.forEach { (mistake, count) ->
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                      ) {
                        Text(mistake, fontSize = 13.sp, color = TextPrimary)
                        Text("$count times", fontSize = 12.sp, color = RedLoss, fontWeight = FontWeight.Bold)
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      // Mandatory Risk Disclaimer
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          color = JournalSurface.copy(alpha = 0.5f),
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
        ) {
          Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Analytics are calculated strictly from manually saved trade records. Past historical performance is for self-review and does not guarantee future market results.",
              color = TextMuted,
              fontSize = 11.sp
            )
          }
        }
      }
    }
  }
}

@Composable
private fun DisciplineProgressBar(label: String, percent: Double, count: Int, total: Int) {
  Column {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(label, fontSize = 12.sp, color = TextPrimary)
      Text("${String.format(Locale.US, "%.1f", percent)}% ($count/$total)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (percent >= 70) EmeraldProfit else RedLoss)
    }
    Spacer(modifier = Modifier.height(4.dp))
    LinearProgressIndicator(
      progress = { (percent / 100.0).toFloat().coerceIn(0f, 1f) },
      modifier = Modifier
        .fillMaxWidth()
        .height(4.dp),
      color = if (percent >= 70) EmeraldProfit else RedLoss,
      trackColor = JournalBorder
    )
  }
}

@Composable
private fun EmptyAnalyticsCard(message: String) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = JournalSurface),
    shape = RoundedCornerShape(14.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = message,
        color = TextMuted,
        fontSize = 13.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
      )
    }
  }
}
