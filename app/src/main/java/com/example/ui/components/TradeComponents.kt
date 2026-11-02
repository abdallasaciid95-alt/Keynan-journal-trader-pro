package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Trade
import com.example.ui.theme.AsianSessionColor
import com.example.ui.theme.BreakevenGray
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.JournalBorder
import com.example.ui.theme.JournalSurface
import com.example.ui.theme.LondonSessionColor
import com.example.ui.theme.NewYorkSessionColor
import com.example.ui.theme.OtherSessionColor
import com.example.ui.theme.RedLoss
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun TradeItemCard(
  trade: Trade,
  accountName: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isBuy = trade.direction.equals("BUY", ignoreCase = true)
  val dirColor = if (isBuy) EmeraldProfit else RedLoss
  val pnlColor = when {
    trade.isOpen() -> GoldAccent
    trade.pnl > 0 -> EmeraldProfit
    trade.pnl < 0 -> RedLoss
    else -> BreakevenGray
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("trade_card_${trade.id}"),
    colors = CardDefaults.cardColors(containerColor = JournalSurface),
    shape = RoundedCornerShape(14.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Top row: Direction badge, Instrument, Account, Result Chip
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          // BUY/SELL badge
          Surface(
            color = dirColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, dirColor.copy(alpha = 0.5f))
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Icon(
                imageVector = if (isBuy) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = trade.direction,
                tint = dirColor,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = trade.direction,
                color = dirColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Instrument: Gold
          Text(
            text = "XAUUSD",
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            fontSize = 15.sp
          )

          Spacer(modifier = Modifier.width(6.dp))

          Text(
            text = "${trade.lotSize} lots",
            color = TextSecondary,
            fontSize = 12.sp
          )
        }

        // Result Chip
        ResultChip(result = trade.result)
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Middle row: Prices & Realized P/L
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row {
            Text(text = "Entry: ", color = TextMuted, fontSize = 12.sp)
            Text(
              text = String.format(Locale.US, "%.2f", trade.entryPrice),
              color = TextPrimary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "Exit: ", color = TextMuted, fontSize = 12.sp)
            Text(
              text = if (trade.isOpen()) "Open" else String.format(Locale.US, "%.2f", trade.exitPrice),
              color = TextPrimary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          }

          if (trade.stopLossPrice != null || trade.takeProfitPrice != null) {
            Row(modifier = Modifier.padding(top = 2.dp)) {
              if (trade.stopLossPrice != null) {
                Text(
                  text = "SL: ${String.format(Locale.US, "%.2f", trade.stopLossPrice)} ",
                  color = RedLoss.copy(alpha = 0.8f),
                  fontSize = 11.sp
                )
              }
              if (trade.takeProfitPrice != null) {
                Text(
                  text = "TP: ${String.format(Locale.US, "%.2f", trade.takeProfitPrice)}",
                  color = EmeraldProfit.copy(alpha = 0.8f),
                  fontSize = 11.sp
                )
              }
            }
          }
        }

        // Realized P/L
        Column(horizontalAlignment = Alignment.End) {
          if (trade.isOpen()) {
            Text(
              text = "OPEN TRADE",
              color = GoldAccent,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
          } else {
            val prefix = if (trade.pnl >= 0) "+" else ""
            Text(
              text = "$prefix$${String.format(Locale.US, "%,.2f", trade.pnl)}",
              color = pnlColor,
              fontSize = 17.sp,
              fontWeight = FontWeight.Black
            )
          }

          trade.getRiskToReward()?.let { rr ->
            Text(
              text = "1:${String.format(Locale.US, "%.1f", rr)} RR",
              fontSize = 10.sp,
              color = TextMuted
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Bottom Row: Date/Time, Session badge, Account Name, Screenshot indicator
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${trade.date} ${trade.entryTime}",
            color = TextMuted,
            fontSize = 11.sp
          )
          Spacer(modifier = Modifier.width(8.dp))
          SessionBadge(session = trade.session)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (trade.screenshotUrl.isNotBlank()) {
            Icon(
              imageVector = Icons.Default.Image,
              contentDescription = "Has chart screenshot",
              tint = GoldAccent,
              modifier = Modifier
                .size(16.dp)
                .padding(end = 4.dp)
            )
          }
          Text(
            text = accountName,
            color = TextSecondary,
            fontSize = 11.sp,
            maxLines = 1
          )
        }
      }
    }
  }
}

@Composable
fun ResultChip(result: String) {
  val (color, label) = when (result.lowercase()) {
    "win" -> EmeraldProfit to "WIN"
    "loss" -> RedLoss to "LOSS"
    "breakeven" -> BreakevenGray to "B/E"
    else -> GoldAccent to "OPEN"
  }

  Surface(
    color = color.copy(alpha = 0.15f),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
  ) {
    Text(
      text = label,
      color = color,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
    )
  }
}

@Composable
fun SessionBadge(session: String) {
  val (color, name) = when (session.lowercase()) {
    "asian" -> AsianSessionColor to "Asian"
    "london" -> LondonSessionColor to "London"
    "new_york" -> NewYorkSessionColor to "NY"
    else -> OtherSessionColor to "Other"
  }

  Surface(
    color = color.copy(alpha = 0.12f),
    shape = RoundedCornerShape(4.dp)
  ) {
    Text(
      text = name,
      color = color,
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium,
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
    )
  }
}

@Composable
fun StatMetricCard(
  title: String,
  value: String,
  subtitle: String? = null,
  valueColor: Color = TextPrimary,
  icon: ImageVector? = null,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = JournalSurface),
    shape = RoundedCornerShape(14.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title.uppercase(),
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          ),
          color = TextSecondary,
          maxLines = 1
        )
        if (icon != null) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = valueColor,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.Black,
          fontSize = 20.sp
        ),
        color = valueColor,
        maxLines = 1
      )

      if (subtitle != null) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = TextMuted,
          maxLines = 1
        )
      }
    }
  }
}
