package com.example.ui.reports

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.analytics.TradingAnalyticsEngine
import com.example.data.export.CsvExporter
import com.example.data.model.AccountTransaction
import com.example.data.model.Trade
import com.example.data.model.TradingAccount
import com.example.ui.components.CustomDateRangeDialog
import com.example.ui.components.TradeItemCard
import com.example.ui.dashboard.DateFilterType
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

@Composable
fun ReportsScreen(
  accounts: List<TradingAccount>,
  trades: List<Trade>,
  transactions: List<AccountTransaction>,
  selectedAccountId: String?,
  onTradeClicked: (Trade) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var reportRange by remember { mutableStateOf(DateFilterType.THIS_MONTH) }
  var showCustomDialog by remember { mutableStateOf(false) }
  var customStartDate by remember { mutableStateOf("") }
  var customEndDate by remember { mutableStateOf("") }

  val accountsMap = remember(accounts) { accounts.associate { it.id to it.name } }
  val targetAccount = remember(accounts, selectedAccountId) {
    if (selectedAccountId == null) null else accounts.find { it.id == selectedAccountId }
  }

  // Filtered trades by account & date
  val filteredTrades = remember(trades, selectedAccountId, reportRange, customStartDate, customEndDate) {
    var list = if (selectedAccountId == null) trades else trades.filter { it.accountId == selectedAccountId }

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val now = Calendar.getInstance()
    when (reportRange) {
      DateFilterType.TODAY -> {
        val todayStr = sdf.format(now.time)
        list = list.filter { it.date == todayStr }
      }
      DateFilterType.THIS_WEEK -> {
        val weekCal = Calendar.getInstance()
        weekCal.set(Calendar.DAY_OF_WEEK, weekCal.firstDayOfWeek)
        val startOfWeek = sdf.format(weekCal.time)
        list = list.filter { it.date >= startOfWeek }
      }
      DateFilterType.THIS_MONTH -> {
        val monthCal = Calendar.getInstance()
        monthCal.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = sdf.format(monthCal.time)
        list = list.filter { it.date >= startOfMonth }
      }
      DateFilterType.LAST_MONTH -> {
        val lastMonthCal = Calendar.getInstance()
        lastMonthCal.add(Calendar.MONTH, -1)
        lastMonthCal.set(Calendar.DAY_OF_MONTH, 1)
        val start = sdf.format(lastMonthCal.time)
        val endCal = Calendar.getInstance()
        endCal.set(Calendar.DAY_OF_MONTH, 1)
        endCal.add(Calendar.DAY_OF_MONTH, -1)
        val end = sdf.format(endCal.time)
        list = list.filter { it.date in start..end }
      }
      DateFilterType.ALL_TIME -> {}
      DateFilterType.CUSTOM -> {
        if (customStartDate.isNotBlank() && customEndDate.isNotBlank()) {
          list = list.filter { it.date in customStartDate..customEndDate }
        }
      }
    }
    list.sortedByDescending { "${it.date} ${it.entryTime}" }
  }

  val metrics = remember(filteredTrades) {
    TradingAnalyticsEngine.calculatePerformance(filteredTrades)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(JournalBackground),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header & Export Action
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Trading Statements",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
            color = TextPrimary
          )
          Text(
            text = "Auditable performance report & CSV exports",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
        }

        Button(
          onClick = {
            val csv = CsvExporter.generateTradesCsv(filteredTrades, accountsMap)
            val filename = "Keynan_Trades_${reportRange.name.lowercase()}_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.csv"
            CsvExporter.shareCsv(context, filename, csv)
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldProfit),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color(0xFF042116), modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Export CSV", color = Color(0xFF042116), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      }
    }

    // Range selector chips
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        DateFilterType.values().forEach { filter ->
          val isSel = reportRange == filter
          FilterChip(
            selected = isSel,
            onClick = {
              if (filter == DateFilterType.CUSTOM) {
                showCustomDialog = true
              } else {
                reportRange = filter
              }
            },
            label = {
              Text(
                text = if (filter == DateFilterType.CUSTOM && customStartDate.isNotBlank()) {
                  "$customStartDate ~ $customEndDate"
                } else filter.label,
                fontSize = 12.sp
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldProfit.copy(alpha = 0.2f),
              selectedLabelColor = EmeraldProfit,
              containerColor = JournalSurface,
              labelColor = TextSecondary
            ),
            border = FilterChipDefaults.filterChipBorder(
              borderColor = if (isSel) EmeraldProfit else JournalBorder,
              selectedBorderColor = EmeraldProfit,
              enabled = true,
              selected = isSel
            ),
            shape = RoundedCornerShape(8.dp)
          )
        }
      }
    }

    // Statement Summary Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JournalSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "STATEMENT SUMMARY",
              style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
              color = TextSecondary
            )
            Text(
              text = targetAccount?.name ?: "All Accounts Combined",
              color = GoldAccent,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }

          HorizontalDivider(color = JournalBorder)

          ReportRow("Selected Period", reportRange.label)
          ReportRow("Total Trades", "${metrics.totalTrades} (${metrics.winningTrades}W / ${metrics.losingTrades}L / ${metrics.breakevenTrades}BE)")
          ReportRow("Win Rate", "${String.format(Locale.US, "%.1f", metrics.winRate)}%")
          ReportRow("Gross Profit", "+$${String.format(Locale.US, "%,.2f", metrics.grossProfit)}")
          ReportRow("Gross Loss", "-$${String.format(Locale.US, "%,.2f", metrics.grossLoss)}")
          ReportRow("Net Realized P/L", "${if (metrics.netPnl >= 0) "+" else ""}$${String.format(Locale.US, "%,.2f", metrics.netPnl)}", isBold = true, isPositive = metrics.netPnl >= 0)
          ReportRow("Profit Factor", if (metrics.profitFactor > 0) String.format(Locale.US, "%.2f", metrics.profitFactor) else "0.00")
          ReportRow("Max Historical Drawdown", "$${String.format(Locale.US, "%,.2f", metrics.maxDrawdownAmount)}")
        }
      }
    }

    // Account Summary CSV export button
    item {
      Button(
        onClick = {
          val csv = CsvExporter.generateAccountSummaryCsv(accounts, trades, transactions)
          val filename = "Keynan_Accounts_Summary_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.csv"
          CsvExporter.shareCsv(context, filename, csv)
        },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = JournalSurface),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent)
      ) {
        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Export Account Balances & Transactions CSV", color = GoldAccent, fontSize = 13.sp)
      }
    }

    item {
      Text(
        text = "TRADES IN THIS STATEMENT (${filteredTrades.size})",
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondary
      )
    }

    if (filteredTrades.isEmpty()) {
      item {
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
            Text("No trades match the selected date range.", color = TextMuted, fontSize = 12.sp)
          }
        }
      }
    } else {
      items(filteredTrades, key = { it.id }) { trade ->
        TradeItemCard(
          trade = trade,
          accountName = accountsMap[trade.accountId] ?: "Default Account",
          onClick = { onTradeClicked(trade) }
        )
      }
    }
  }

  if (showCustomDialog) {
    CustomDateRangeDialog(
      initialStartDate = customStartDate.ifBlank { "2026-10-01" },
      initialEndDate = customEndDate.ifBlank { "2026-11-02" },
      onDismiss = { showCustomDialog = false },
      onConfirm = { s, e ->
        customStartDate = s
        customEndDate = e
        reportRange = DateFilterType.CUSTOM
        showCustomDialog = false
      }
    )
  }
}

@Composable
private fun ReportRow(
  label: String,
  value: String,
  isBold: Boolean = false,
  isPositive: Boolean? = null
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = label, fontSize = 13.sp, color = TextSecondary)
    val color = when (isPositive) {
      true -> EmeraldProfit
      false -> RedLoss
      null -> TextPrimary
    }
    Text(
      text = value,
      fontSize = if (isBold) 14.sp else 13.sp,
      fontWeight = if (isBold) FontWeight.Black else FontWeight.Medium,
      color = color
    )
  }
}
