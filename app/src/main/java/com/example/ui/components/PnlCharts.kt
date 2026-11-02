package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Trade
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.JournalBorder
import com.example.ui.theme.JournalSurface
import com.example.ui.theme.RedLoss
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

data class DayPnl(
  val date: String,
  val pnl: Double,
  val tradesCount: Int
)

@Composable
fun PnlBarChart(
  trades: List<Trade>,
  modifier: Modifier = Modifier
) {
  val closed = trades.filter { !it.isOpen() }
  val dailyMap = closed.groupBy { it.date }
  val dayPnlList = dailyMap.map { (date, tList) ->
    DayPnl(
      date = date,
      pnl = tList.sumOf { it.pnl },
      tradesCount = tList.size
    )
  }.sortedBy { it.date }.takeLast(14) // Last 14 active days

  var selectedIndex by remember { mutableStateOf<Int?>(null) }

  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = JournalSurface),
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "DAILY P/L PERFORMANCE",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            ),
            color = TextSecondary
          )
          val totalShown = dayPnlList.sumOf { it.pnl }
          Text(
            text = "${if (totalShown >= 0) "+" else ""}$${String.format(Locale.US, "%,.2f", totalShown)}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (totalShown >= 0) EmeraldProfit else RedLoss
          )
        }

        selectedIndex?.let { idx ->
          if (idx in dayPnlList.indices) {
            val item = dayPnlList[idx]
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = item.date,
                fontSize = 11.sp,
                color = TextSecondary
              )
              Text(
                text = "${if (item.pnl >= 0) "+" else ""}$${String.format(Locale.US, "%,.2f", item.pnl)} (${item.tradesCount}t)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (item.pnl >= 0) EmeraldProfit else RedLoss
              )
            }
          }
        } ?: Text(
          text = "Last ${dayPnlList.size} Days",
          style = MaterialTheme.typography.bodySmall,
          color = TextMuted
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (dayPnlList.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No closed trades recorded yet",
            color = TextMuted,
            fontSize = 13.sp
          )
        }
      } else {
        val maxAbsPnl = max(100.0, dayPnlList.maxOf { abs(it.pnl) })

        Canvas(
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .pointerInput(dayPnlList) {
              detectTapGestures { tapOffset ->
                val barSlotWidth = size.width / dayPnlList.size
                val clickedIdx = (tapOffset.x / barSlotWidth).toInt().coerceIn(0, dayPnlList.size - 1)
                selectedIndex = if (selectedIndex == clickedIdx) null else clickedIdx
              }
            }
        ) {
          val canvasWidth = size.width
          val canvasHeight = size.height
          val zeroY = canvasHeight / 2f
          val count = dayPnlList.size
          val barSlotWidth = canvasWidth / count
          val barWidth = (barSlotWidth * 0.6f).coerceAtLeast(8f)

          // Draw Zero Baseline
          drawLine(
            color = JournalBorder,
            start = Offset(0f, zeroY),
            end = Offset(canvasWidth, zeroY),
            strokeWidth = 1.5f
          )

          // Draw Bars
          dayPnlList.forEachIndexed { i, item ->
            val centerX = (i * barSlotWidth) + (barSlotWidth / 2f)
            val left = centerX - (barWidth / 2f)
            val barHeight = ((abs(item.pnl) / maxAbsPnl) * (zeroY - 10f)).toFloat().coerceAtLeast(4f)
            val isSelected = selectedIndex == i

            val barColor = if (item.pnl >= 0) {
              if (isSelected) EmeraldProfit else EmeraldProfit.copy(alpha = 0.85f)
            } else {
              if (isSelected) RedLoss else RedLoss.copy(alpha = 0.85f)
            }

            val top = if (item.pnl >= 0) zeroY - barHeight else zeroY

            drawRect(
              color = barColor,
              topLeft = Offset(left, top),
              size = Size(barWidth, barHeight)
            )

            if (isSelected) {
              // Highlight indicator
              drawCircle(
                color = TextPrimary,
                radius = 3.dp.toPx(),
                center = Offset(centerX, if (item.pnl >= 0) top - 6f else top + barHeight + 6f)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Axis labels
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = dayPnlList.first().date, fontSize = 10.sp, color = TextMuted)
          Text(text = "Tap bar for details", fontSize = 10.sp, color = TextMuted)
          Text(text = dayPnlList.last().date, fontSize = 10.sp, color = TextMuted)
        }
      }
    }
  }
}
