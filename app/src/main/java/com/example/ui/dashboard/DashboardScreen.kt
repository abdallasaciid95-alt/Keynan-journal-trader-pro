package com.example.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.model.AccountTransaction
import com.example.data.model.Trade
import com.example.data.model.TradingAccount
import com.example.ui.components.CustomDateRangeDialog
import com.example.ui.components.PnlBarChart
import com.example.ui.components.StatMetricCard
import com.example.ui.components.TradeItemCard
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.JournalBackground
import com.example.ui.theme.JournalBorder
import com.example.ui.theme.JournalSurface
import com.example.ui.theme.RedLoss
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DateFilterType(val label: String) {
  TODAY("Today"),
  THIS_WEEK("This Week"),
  THIS_MONTH("This Month"),
  LAST_MONTH("Last Month"),
  ALL_TIME("All Time"),
  CUSTOM("Custom")
}

@Composable
fun DashboardScreen(
  accounts: List<TradingAccount>,
  trades: List<Trade>,
  transactions: List<AccountTransaction>,
  selectedAccountId: String?, // null means "All Accounts"
  onSelectAccount: (String?) -> Unit,
  onNavigateToAddTrade: () -> Unit,
  onNavigateToTrades: () -> Unit,
  onTradeClicked: (Trade) -> Unit,
  modifier: Modifier = Modifier
) {
  var dateFilter by remember { mutableStateOf(DateFilterType.THIS_MONTH) }
  var showAccountDropdown by remember { mutableStateOf(false) }
  var showCustomDateDialog by remember { mutableStateOf(false) }
  var customStartDate by remember { mutableStateOf("") }
  var customEndDate by remember { mutableStateOf("") }

  val dateFormat = remember { SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US) }
  val currentDateStr = remember { dateFormat.format(Date()) }

  // Filter trades by selected account
  val accountFilteredTrades = remember(trades, selectedAccountId) {
    if (selectedAccountId == null) trades else trades.filter { it.accountId == selectedAccountId }
  }

  // Filter trades by date
  val dateFilteredTrades = remember(accountFilteredTrades, dateFilter, customStartDate, customEndDate) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val now = Calendar.getInstance()
    val todayStr = sdf.format(now.time)

    when (dateFilter) {
      DateFilterType.TODAY -> accountFilteredTrades.filter { it.date == todayStr }
      DateFilterType.THIS_WEEK -> {
        val weekCal = Calendar.getInstance()
        weekCal.set(Calendar.DAY_OF_WEEK, weekCal.firstDayOfWeek)
        val startOfWeek = sdf.format(weekCal.time)
        accountFilteredTrades.filter { it.date >= startOfWeek }
      }
      DateFilterType.THIS_MONTH -> {
        val monthCal = Calendar.getInstance()
        monthCal.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = sdf.format(monthCal.time)
        accountFilteredTrades.filter { it.date >= startOfMonth }
      }
      DateFilterType.LAST_MONTH -> {
        val lastMonthCal = Calendar.getInstance()
        lastMonthCal.add(Calendar.MONTH, -1)
        lastMonthCal.set(Calendar.DAY_OF_MONTH, 1)
        val startOfLastMonth = sdf.format(lastMonthCal.time)
        val endOfLastMonthCal = Calendar.getInstance()
        endOfLastMonthCal.set(Calendar.DAY_OF_MONTH, 1)
        endOfLastMonthCal.add(Calendar.DAY_OF_MONTH, -1)
        val endOfLastMonth = sdf.format(endOfLastMonthCal.time)
        accountFilteredTrades.filter { it.date in startOfLastMonth..endOfLastMonth }
      }
      DateFilterType.ALL_TIME -> accountFilteredTrades
      DateFilterType.CUSTOM -> {
        if (customStartDate.isNotBlank() && customEndDate.isNotBlank()) {
          accountFilteredTrades.filter { it.date in customStartDate..customEndDate }
        } else accountFilteredTrades
      }
    }
  }

  // Calculate Net P/L for Today, This Week, This Month specifically
  val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
  val todayPnl = remember(accountFilteredTrades) {
    val todayStr = sdf.format(Date())
    accountFilteredTrades.filter { it.date == todayStr && !it.isOpen() }.sumOf { it.pnl }
  }
  val weeklyPnl = remember(accountFilteredTrades) {
    val weekCal = Calendar.getInstance()
    weekCal.set(Calendar.DAY_OF_WEEK, weekCal.firstDayOfWeek)
    val startOfWeek = sdf.format(weekCal.time)
    accountFilteredTrades.filter { it.date >= startOfWeek && !it.isOpen() }.sumOf { it.pnl }
  }
  val monthlyPnl = remember(accountFilteredTrades) {
    val monthCal = Calendar.getInstance()
    monthCal.set(Calendar.DAY_OF_MONTH, 1)
    val startOfMonth = sdf.format(monthCal.time)
    accountFilteredTrades.filter { it.date >= startOfMonth && !it.isOpen() }.sumOf { it.pnl }
  }

  // Balance calculation
  val (totalBalance, startingCapital) = remember(accounts, trades, transactions, selectedAccountId) {
    if (selectedAccountId == null) {
      val totalStart = accounts.sumOf { it.startingBalance }
      val totalNetPnl = trades.filter { !it.isOpen() }.sumOf { it.pnl }
      val totalDep = transactions.filter { it.type == "deposit" }.sumOf { it.amount }
      val totalWth = transactions.filter { it.type == "withdrawal" }.sumOf { it.amount }
      (totalStart + totalNetPnl + totalDep - totalWth) to totalStart
    } else {
      val acc = accounts.find { it.id == selectedAccountId }
      if (acc != null) {
        val summary = TradingAnalyticsEngine.calculateAccountBalance(acc, trades, transactions)
        summary.calculatedBalance to acc.startingBalance
      } else 0.0 to 0.0
    }
  }

  // Analytics for the selected filtered range
  val metrics = remember(dateFilteredTrades, startingCapital) {
    TradingAnalyticsEngine.calculatePerformance(dateFilteredTrades, startingCapital)
  }

  val accountsMap = remember(accounts) { accounts.associate { it.id to it.name } }
  val currentAccountName = remember(accounts, selectedAccountId) {
    if (selectedAccountId == null) "All Accounts"
    else accounts.find { it.id == selectedAccountId }?.name ?: "Selected Account"
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(JournalBackground),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Header Section
    item {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Welcome Back, Keynan",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 22.sp
              ),
              color = TextPrimary
            )
            Text(
              text = currentDateStr,
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary
            )
          }

          // Cloud Sync Live Badge
          Surface(
            color = EmeraldProfit.copy(alpha = 0.12f),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldProfit.copy(alpha = 0.3f))
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CloudDone,
                contentDescription = "Cloud Connected",
                tint = EmeraldProfit,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Live Sync",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldProfit
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Account Selector & Quick Add Trade button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box {
            Surface(
              modifier = Modifier
                .clickable { showAccountDropdown = true }
                .testTag("account_selector_dropdown"),
              color = JournalSurface,
              shape = RoundedCornerShape(10.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.AccountBalance,
                  contentDescription = null,
                  tint = GoldAccent,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = currentAccountName,
                  color = TextPrimary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold
                )
                Icon(
                  imageVector = Icons.Default.ArrowDropDown,
                  contentDescription = "Select Account",
                  tint = TextSecondary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            DropdownMenu(
              expanded = showAccountDropdown,
              onDismissRequest = { showAccountDropdown = false },
              modifier = Modifier.background(JournalSurface)
            ) {
              DropdownMenuItem(
                text = { Text("All Accounts", color = TextPrimary) },
                onClick = {
                  onSelectAccount(null)
                  showAccountDropdown = false
                }
              )
              accounts.forEach { acc ->
                DropdownMenuItem(
                  text = {
                    Column {
                      Text(acc.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                      Text(acc.getDisplayType(), color = TextSecondary, fontSize = 11.sp)
                    }
                  },
                  onClick = {
                    onSelectAccount(acc.id)
                    showAccountDropdown = false
                  }
                )
              }
            }
          }

          // Quick Add Trade Button
          Button(
            onClick = onNavigateToAddTrade,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldProfit),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            modifier = Modifier.testTag("quick_add_trade_button")
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = null,
              tint = Color(0xFF042116),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Add Trade",
              color = Color(0xFF042116),
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // 2. Date Filter Chips Row
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        DateFilterType.values().forEach { filter ->
          val isSelected = dateFilter == filter
          FilterChip(
            selected = isSelected,
            onClick = {
              if (filter == DateFilterType.CUSTOM) {
                showCustomDateDialog = true
              } else {
                dateFilter = filter
              }
            },
            label = {
              Text(
                text = if (filter == DateFilterType.CUSTOM && customStartDate.isNotBlank()) {
                  "$customStartDate ~ $customEndDate"
                } else filter.label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldProfit.copy(alpha = 0.2f),
              selectedLabelColor = EmeraldProfit,
              containerColor = JournalSurface,
              labelColor = TextSecondary
            ),
            border = FilterChipDefaults.filterChipBorder(
              borderColor = if (isSelected) EmeraldProfit else JournalBorder,
              selectedBorderColor = EmeraldProfit,
              enabled = true,
              selected = isSelected
            ),
            shape = RoundedCornerShape(8.dp)
          )
        }
      }
    }

    // 3. Primary Balance Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JournalSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldAccent.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "TOTAL ACCOUNT BALANCE",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              ),
              color = TextSecondary
            )
            Surface(
              color = GoldAccent.copy(alpha = 0.15f),
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = "USD ($)",
                color = GoldAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "$${String.format(Locale.US, "%,.2f", totalBalance)}",
            style = MaterialTheme.typography.headlineLarge.copy(
              fontWeight = FontWeight.Black,
              fontSize = 32.sp
            ),
            color = TextPrimary
          )

          Spacer(modifier = Modifier.height(14.dp))

          // 3-way P/L Sub-metrics: Today, Weekly, Monthly
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            PnlMiniBadge(label = "Today", amount = todayPnl)
            PnlMiniBadge(label = "This Week", amount = weeklyPnl)
            PnlMiniBadge(label = "This Month", amount = monthlyPnl)
          }
        }
      }
    }

    // 4. Performance Summary Grid
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StatMetricCard(
            title = "Total Trades",
            value = "${metrics.totalTrades}",
            subtitle = "${metrics.closedTradesCount} Closed · ${metrics.openTradesCount} Open",
            valueColor = TextPrimary,
            modifier = Modifier.weight(1f)
          )
          StatMetricCard(
            title = "Win Rate",
            value = "${String.format(Locale.US, "%.1f", metrics.winRate)}%",
            subtitle = "${metrics.winningTrades}W / ${metrics.losingTrades}L / ${metrics.breakevenTrades}BE",
            valueColor = if (metrics.winRate >= 50.0) EmeraldProfit else RedLoss,
            modifier = Modifier.weight(1f)
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StatMetricCard(
            title = "Avg Risk/Reward",
            value = metrics.averageRiskReward?.let { "1:${String.format(Locale.US, "%.2f", it)}" } ?: "N/A",
            subtitle = "From valid SL/TP data",
            valueColor = GoldAccent,
            modifier = Modifier.weight(1f)
          )
          StatMetricCard(
            title = "Max Drawdown",
            value = "$${String.format(Locale.US, "%,.2f", metrics.maxDrawdownAmount)}",
            subtitle = "${String.format(Locale.US, "%.1f", metrics.maxDrawdownPercent)}% from peak",
            valueColor = if (metrics.maxDrawdownAmount > 0) RedLoss else TextSecondary,
            modifier = Modifier.weight(1f)
          )
        }

        // Streak Banner
        Surface(
          modifier = Modifier.fillMaxWidth(),
          color = JournalSurface,
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Whatshot,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Current Streak:",
                color = TextSecondary,
                fontSize = 13.sp
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = metrics.currentStreak.getDisplay(),
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }
            Text(
              text = "Best: ${metrics.longestWinStreak}W",
              color = EmeraldProfit,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }

    // 5. Daily P/L Chart
    item {
      PnlBarChart(trades = dateFilteredTrades)
    }

    // 6. Recent Trades List Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "RECENT GOLD TRADES",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          ),
          color = TextPrimary
        )
        TextButton(onClick = onNavigateToTrades) {
          Text("View All (${trades.size})", color = EmeraldProfit, fontSize = 13.sp)
        }
      }
    }

    // Empty state if no trades recorded
    if (accountFilteredTrades.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = JournalSurface),
          shape = RoundedCornerShape(16.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .background(EmeraldProfit.copy(alpha = 0.1f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.ShowChart,
                contentDescription = null,
                tint = EmeraldProfit,
                modifier = Modifier.size(32.dp)
              )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "No XAUUSD Trades Recorded",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Begin logging your manual Gold trades to track performance, risk metrics, and discipline habits.",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
              onClick = onNavigateToAddTrade,
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldProfit),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Record First Trade", color = Color(0xFF042116), fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    } else {
      val recentTrades = accountFilteredTrades.sortedByDescending { "${it.date} ${it.entryTime}" }.take(5)
      items(recentTrades, key = { it.id }) { trade ->
        TradeItemCard(
          trade = trade,
          accountName = accountsMap[trade.accountId] ?: "Default Account",
          onClick = { onTradeClicked(trade) }
        )
      }
    }
  }

  if (showCustomDateDialog) {
    CustomDateRangeDialog(
      initialStartDate = customStartDate.ifBlank { "2026-10-01" },
      initialEndDate = customEndDate.ifBlank { "2026-11-02" },
      onDismiss = { showCustomDateDialog = false },
      onConfirm = { s, e ->
        customStartDate = s
        customEndDate = e
        dateFilter = DateFilterType.CUSTOM
        showCustomDateDialog = false
      }
    )
  }
}

@Composable
private fun PnlMiniBadge(label: String, amount: Double) {
  val isPositive = amount >= 0
  val color = if (isPositive) EmeraldProfit else RedLoss
  Column {
    Text(text = label, color = TextMuted, fontSize = 11.sp)
    Text(
      text = "${if (isPositive) "+" else ""}$${String.format(Locale.US, "%,.2f", amount)}",
      color = color,
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold
    )
  }
}
