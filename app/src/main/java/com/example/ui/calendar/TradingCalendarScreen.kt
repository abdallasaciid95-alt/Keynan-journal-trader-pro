package com.example.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Trade
import com.example.data.model.TradingAccount
import com.example.ui.components.TradeItemCard
import com.example.ui.theme.BreakevenGray
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
import java.util.Locale

@Composable
fun TradingCalendarScreen(
  trades: List<Trade>,
  accounts: List<TradingAccount>,
  onTradeClicked: (Trade) -> Unit,
  modifier: Modifier = Modifier
) {
  var currentMonthCal by remember {
    mutableStateOf(Calendar.getInstance().apply {
      set(Calendar.DAY_OF_MONTH, 1)
    })
  }

  var selectedDateStr by remember { mutableStateOf<String?>(null) }
  var showEmptyDaysHighlight by remember { mutableStateOf(false) }

  val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }
  val sdfDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
  val accountsMap = remember(accounts) { accounts.associate { it.id to it.name } }

  // Days in month calculation
  val (daysGrid, monthPnl, monthTradesCount) = remember(currentMonthCal, trades) {
    val cal = currentMonthCal.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val monthIndex = cal.get(Calendar.MONTH)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val grid = mutableListOf<String?>()
    // Padding before 1st of month (aligning Monday as start or Sunday as start)
    // Let's use Monday as 1st column:
    // Sunday in java is 1, Monday is 2
    val shift = (firstDayOfWeek - 2 + 7) % 7
    repeat(shift) { grid.add(null) }

    for (d in 1..maxDays) {
      cal.set(Calendar.DAY_OF_MONTH, d)
      grid.add(sdfDate.format(cal.time))
    }

    // Trades in this month
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val startStr = sdfDate.format(cal.time)
    cal.set(Calendar.DAY_OF_MONTH, maxDays)
    val endStr = sdfDate.format(cal.time)

    val monthTrades = trades.filter { it.date in startStr..endStr && !it.isOpen() }
    val pnl = monthTrades.sumOf { it.pnl }
    Triple(grid, pnl, monthTrades.size)
  }

  // Trades on selected date
  val selectedDateTrades = remember(selectedDateStr, trades) {
    if (selectedDateStr == null) emptyList()
    else trades.filter { it.date == selectedDateStr }
  }

  val selectedDatePnl = remember(selectedDateTrades) {
    selectedDateTrades.filter { !it.isOpen() }.sumOf { it.pnl }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(JournalBackground)
      .padding(16.dp)
  ) {
    // Header & Month Navigator
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = {
          currentMonthCal = (currentMonthCal.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
          }
          selectedDateStr = null
        }
      ) {
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Month", tint = TextPrimary)
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = monthYearFormat.format(currentMonthCal.time),
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = TextPrimary
        )
        Text(
          text = "Net: ${if (monthPnl >= 0) "+" else ""}$${String.format(Locale.US, "%,.2f", monthPnl)} · $monthTradesCount trades",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = if (monthPnl >= 0) EmeraldProfit else RedLoss
        )
      }

      IconButton(
        onClick = {
          currentMonthCal = (currentMonthCal.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
          }
          selectedDateStr = null
        }
      ) {
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month", tint = TextPrimary)
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Days of Week Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
        Text(
          text = day,
          modifier = Modifier.weight(1f),
          textAlign = TextAlign.Center,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Calendar Grid
    val rows = daysGrid.chunked(7)
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = JournalSurface),
      shape = RoundedCornerShape(14.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
    ) {
      Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEach { week ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            week.forEach { dateStr ->
              if (dateStr == null) {
                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
              } else {
                val dayTrades = trades.filter { it.date == dateStr && !it.isOpen() }
                val hasTrades = dayTrades.isNotEmpty()
                val dayPnl = dayTrades.sumOf { it.pnl }
                val isSelected = selectedDateStr == dateStr
                val dayNum = dateStr.takeLast(2).toIntOrNull()?.toString() ?: ""

                val indicatorColor = when {
                  !hasTrades -> if (showEmptyDaysHighlight) JournalBorder else Color.Transparent
                  dayPnl > 0.001 -> EmeraldProfit
                  dayPnl < -0.001 -> RedLoss
                  else -> BreakevenGray
                }

                Box(
                  modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                      if (isSelected) EmeraldProfit.copy(alpha = 0.25f)
                      else if (hasTrades) indicatorColor.copy(alpha = 0.12f)
                      else Color.Transparent
                    )
                    .border(
                      1.dp,
                      if (isSelected) EmeraldProfit
                      else if (hasTrades) indicatorColor.copy(alpha = 0.4f)
                      else JournalBorder.copy(alpha = 0.3f),
                      RoundedCornerShape(8.dp)
                    )
                    .clickable {
                      selectedDateStr = if (selectedDateStr == dateStr) null else dateStr
                    },
                  contentAlignment = Alignment.Center
                ) {
                  Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                  ) {
                    Text(
                      text = dayNum,
                      fontSize = 11.sp,
                      fontWeight = if (hasTrades || isSelected) FontWeight.Bold else FontWeight.Normal,
                      color = if (isSelected) TextPrimary else if (hasTrades) indicatorColor else TextMuted
                    )
                    if (hasTrades) {
                      Box(
                        modifier = Modifier
                          .size(5.dp)
                          .background(indicatorColor, CircleShape)
                      )
                    }
                  }
                }
              }
            }
            // Fill remaining days in row if partial
            if (week.size < 7) {
              repeat(7 - week.size) {
                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Toggle for highlighting empty days
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Highlight empty days",
        fontSize = 12.sp,
        color = TextSecondary
      )
      Switch(
        checked = showEmptyDaysHighlight,
        onCheckedChange = { showEmptyDaysHighlight = it },
        colors = SwitchDefaults.colors(
          checkedThumbColor = EmeraldProfit,
          checkedTrackColor = EmeraldProfit.copy(alpha = 0.3f)
        )
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Selected Date Details
    if (selectedDateStr != null) {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = JournalSurface,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(text = selectedDateStr!!, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
            Text(text = "${selectedDateTrades.size} trades recorded", fontSize = 11.sp, color = TextSecondary)
          }
          val col = if (selectedDatePnl >= 0) EmeraldProfit else RedLoss
          Text(
            text = "${if (selectedDatePnl >= 0) "+" else ""}$${String.format(Locale.US, "%,.2f", selectedDatePnl)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = col
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(selectedDateTrades, key = { it.id }) { trade ->
          TradeItemCard(
            trade = trade,
            accountName = accountsMap[trade.accountId] ?: "Default Account",
            onClick = { onTradeClicked(trade) }
          )
        }
      }
    } else {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Tap any date to inspect daily trades and realized P/L",
          color = TextMuted,
          fontSize = 12.sp
        )
      }
    }
  }
}
