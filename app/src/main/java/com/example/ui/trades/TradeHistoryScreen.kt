package com.example.ui.trades

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

enum class TradeSortOption(val label: String) {
  NEWEST("Newest First"),
  OLDEST("Oldest First"),
  HIGHEST_PROFIT("Highest Profit"),
  LARGEST_LOSS("Largest Loss")
}

@Composable
fun TradeHistoryScreen(
  trades: List<Trade>,
  accounts: List<TradingAccount>,
  selectedAccountId: String?,
  onSelectAccount: (String?) -> Unit,
  onTradeClicked: (Trade) -> Unit,
  onNavigateToAddTrade: () -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var directionFilter by remember { mutableStateOf("ALL") } // ALL, BUY, SELL
  var resultFilter by remember { mutableStateOf("ALL") } // ALL, win, loss, breakeven, open
  var sessionFilter by remember { mutableStateOf("ALL") } // ALL, asian, london, new_york, other
  var sortOption by remember { mutableStateOf(TradeSortOption.NEWEST) }
  var showSortDropdown by remember { mutableStateOf(false) }

  var dateFilter by remember { mutableStateOf(DateFilterType.ALL_TIME) }
  var showCustomDateDialog by remember { mutableStateOf(false) }
  var customStartDate by remember { mutableStateOf("") }
  var customEndDate by remember { mutableStateOf("") }

  val accountsMap = remember(accounts) { accounts.associate { it.id to it.name } }

  // Filtering pipeline
  val filteredTrades = remember(
    trades, selectedAccountId, searchQuery, directionFilter, resultFilter, sessionFilter, dateFilter, customStartDate, customEndDate, sortOption
  ) {
    var list = trades

    // 1. Account Filter
    if (selectedAccountId != null) {
      list = list.filter { it.accountId == selectedAccountId }
    }

    // 2. Search Query
    if (searchQuery.isNotBlank()) {
      val q = searchQuery.trim().lowercase()
      list = list.filter {
        it.strategy.lowercase().contains(q) ||
          it.entryReason.lowercase().contains(q) ||
          it.notes.lowercase().contains(q) ||
          it.mistakes.lowercase().contains(q) ||
          it.lessons.lowercase().contains(q)
      }
    }

    // 3. Direction Filter
    if (directionFilter != "ALL") {
      list = list.filter { it.direction.equals(directionFilter, ignoreCase = true) }
    }

    // 4. Result Filter
    if (resultFilter != "ALL") {
      list = list.filter { it.result.equals(resultFilter, ignoreCase = true) }
    }

    // 5. Session Filter
    if (sessionFilter != "ALL") {
      list = list.filter { it.session.equals(sessionFilter, ignoreCase = true) }
    }

    // 6. Date Filter
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val now = Calendar.getInstance()
    when (dateFilter) {
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

    // 7. Sorting
    when (sortOption) {
      TradeSortOption.NEWEST -> list.sortedByDescending { "${it.date} ${it.entryTime}" }
      TradeSortOption.OLDEST -> list.sortedBy { "${it.date} ${it.entryTime}" }
      TradeSortOption.HIGHEST_PROFIT -> list.sortedByDescending { it.pnl }
      TradeSortOption.LARGEST_LOSS -> list.sortedBy { it.pnl }
    }
  }

  val totalPnL = remember(filteredTrades) {
    filteredTrades.filter { !it.isOpen() }.sumOf { it.pnl }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(JournalBackground),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Trade History",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
            color = TextPrimary
          )
          Text(
            text = "${filteredTrades.size} trades found",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
        }

        // Sort Selector
        Box {
          Surface(
            modifier = Modifier.testTag("sort_button"),
            color = JournalSurface,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
          ) {
            Row(
              modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              IconButton(
                onClick = { showSortDropdown = true },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort", tint = GoldAccent, modifier = Modifier.size(16.dp))
              }
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = sortOption.label,
                fontSize = 12.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
              )
            }
          }

          DropdownMenu(
            expanded = showSortDropdown,
            onDismissRequest = { showSortDropdown = false },
            modifier = Modifier.background(JournalSurface)
          ) {
            TradeSortOption.values().forEach { opt ->
              DropdownMenuItem(
                text = { Text(opt.label, color = if (sortOption == opt) EmeraldProfit else TextPrimary) },
                onClick = {
                  sortOption = opt
                  showSortDropdown = false
                }
              )
            }
          }
        }
      }
    }

    // Search bar
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search strategy, reasons, notes...", color = TextMuted) },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
            }
          }
        },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("search_trade_input"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = EmeraldProfit,
          unfocusedBorderColor = JournalBorder,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary,
          focusedContainerColor = JournalSurface,
          unfocusedContainerColor = JournalSurface
        ),
        shape = RoundedCornerShape(12.dp)
      )
    }

    // Filters Row: Direction (BUY/SELL) & Result (Win/Loss/BE/Open)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Result filters
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(
            "ALL" to "All Outcomes",
            "win" to "Wins",
            "loss" to "Losses",
            "breakeven" to "Break-Even",
            "open" to "Open Trades"
          ).forEach { (key, label) ->
            val isSel = resultFilter == key
            FilterChip(
              selected = isSel,
              onClick = { resultFilter = key },
              label = { Text(label, fontSize = 11.sp) },
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
              shape = RoundedCornerShape(6.dp)
            )
          }
        }

        // Direction & Session filters
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("ALL" to "All Directions", "BUY" to "BUY Only", "SELL" to "SELL Only").forEach { (key, label) ->
            val isSel = directionFilter == key
            FilterChip(
              selected = isSel,
              onClick = { directionFilter = key },
              label = { Text(label, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GoldAccent.copy(alpha = 0.2f),
                selectedLabelColor = GoldAccent,
                containerColor = JournalSurface,
                labelColor = TextSecondary
              ),
              border = FilterChipDefaults.filterChipBorder(
                borderColor = if (isSel) GoldAccent else JournalBorder,
                selectedBorderColor = GoldAccent,
                enabled = true,
                selected = isSel
              ),
              shape = RoundedCornerShape(6.dp)
            )
          }

          listOf("ALL" to "All Sessions", "asian" to "Asian", "london" to "London", "new_york" to "New York").forEach { (key, label) ->
            val isSel = sessionFilter == key
            FilterChip(
              selected = isSel,
              onClick = { sessionFilter = key },
              label = { Text(label, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.2f),
                selectedLabelColor = Color(0xFF38BDF8),
                containerColor = JournalSurface,
                labelColor = TextSecondary
              ),
              border = FilterChipDefaults.filterChipBorder(
                borderColor = if (isSel) Color(0xFF38BDF8) else JournalBorder,
                selectedBorderColor = Color(0xFF38BDF8),
                enabled = true,
                selected = isSel
              ),
              shape = RoundedCornerShape(6.dp)
            )
          }
        }
      }
    }

    // Filtered Summary bar
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = JournalSurface,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "Filtered Net Realized P/L:", fontSize = 12.sp, color = TextSecondary)
          val pnlCol = if (totalPnL >= 0) EmeraldProfit else RedLoss
          Text(
            text = "${if (totalPnL >= 0) "+" else ""}$${String.format(Locale.US, "%,.2f", totalPnL)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = pnlCol
          )
        }
      }
    }

    // Empty state
    if (filteredTrades.isEmpty()) {
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
            Text(
              text = "No Matching Trades",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Try adjusting your filters or record a new XAUUSD trade entry.",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = onNavigateToAddTrade,
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldProfit),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("Add Trade", color = Color(0xFF042116), fontWeight = FontWeight.Bold)
            }
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
