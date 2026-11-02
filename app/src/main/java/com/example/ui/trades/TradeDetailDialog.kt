package com.example.ui.trades

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.Trade
import com.example.ui.components.ResultChip
import com.example.ui.components.SessionBadge
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
fun TradeDetailDialog(
  trade: Trade,
  accountName: String,
  onDismiss: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  var showDeleteConfirm by remember { mutableStateOf(false) }
  var showFullScreenImage by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .background(JournalBackground),
      color = JournalBackground
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp)
      ) {
        // Top action bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
          }

          Text(
            text = "Trade Details",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )

          Row {
            IconButton(onClick = onEdit) {
              Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = GoldAccent)
            }
            IconButton(onClick = { showDeleteConfirm = true }) {
              Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RedLoss)
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Main Result & Direction Banner
          Card(
            modifier = Modifier.fillMaxWidth(),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    color = (if (trade.direction == "BUY") EmeraldProfit else RedLoss).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (trade.direction == "BUY") EmeraldProfit else RedLoss)
                  ) {
                    Text(
                      text = trade.direction,
                      color = if (trade.direction == "BUY") EmeraldProfit else RedLoss,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "XAUUSD (Gold)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                  )
                }
                ResultChip(result = trade.result)
              }

              Spacer(modifier = Modifier.height(12.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(text = "REALIZED PROFIT/LOSS", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                  val pnlColor = when {
                    trade.isOpen() -> GoldAccent
                    trade.pnl > 0 -> EmeraldProfit
                    trade.pnl < 0 -> RedLoss
                    else -> TextSecondary
                  }
                  Text(
                    text = if (trade.isOpen()) "OPEN" else "${if (trade.pnl >= 0) "+" else ""}$${String.format(Locale.US, "%,.2f", trade.pnl)}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = pnlColor
                  )
                }

                trade.getRiskToReward()?.let { rr ->
                  Surface(
                    color = GoldAccent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
                  ) {
                    Column(
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                      horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                      Text("RISK / REWARD", fontSize = 9.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                      Text("1:${String.format(Locale.US, "%.2f", rr)}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = GoldAccent)
                    }
                  }
                }
              }
            }
          }

          // Execution specifications
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = JournalSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text("TRADE SPECIFICATIONS", style = MaterialTheme.typography.labelSmall, color = TextSecondary)

              DetailRow("Trading Account", accountName)
              DetailRow("Date & Time", "${trade.date} at ${trade.entryTime}")
              DetailRow("Session", trade.getSessionDisplay())
              DetailRow("Lot Size", "${trade.lotSize} lots")
              DetailRow("Entry Price", "$${String.format(Locale.US, "%.2f", trade.entryPrice)}")
              DetailRow("Exit Price", if (trade.isOpen()) "Still Open" else "$${String.format(Locale.US, "%.2f", trade.exitPrice)}")
              if (trade.stopLossPrice != null) {
                DetailRow("Stop Loss Price", "$${String.format(Locale.US, "%.2f", trade.stopLossPrice)}")
              }
              if (trade.takeProfitPrice != null) {
                DetailRow("Take Profit Price", "$${String.format(Locale.US, "%.2f", trade.takeProfitPrice)}")
              }
              if (trade.strategy.isNotBlank()) {
                DetailRow("Strategy", trade.strategy)
              }
            }
          }

          // Discipline Review Card
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = JournalSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text("DISCIPLINE SCORECARD", style = MaterialTheme.typography.labelSmall, color = TextSecondary)

              DisciplineAnswerRow("Followed Trading Plan", trade.followedPlan)
              DisciplineAnswerRow("Respected Stop Loss", trade.respectedStopLoss)
              DisciplineAnswerRow("Entered By Strategy", trade.enteredByStrategy)
              DisciplineAnswerRow("Emotional Trading", trade.emotionalTrading)
              DisciplineAnswerRow("Overtraded", trade.overtraded)

              if (trade.whatLearned.isNotBlank()) {
                HorizontalDivider(color = JournalBorder, modifier = Modifier.padding(vertical = 4.dp))
                Text("Lesson Learned:", fontSize = 12.sp, color = TextSecondary)
                Text(trade.whatLearned, fontSize = 13.sp, color = TextPrimary)
              }
            }
          }

          // Psychology, Mistakes & Notes
          if (trade.entryReason.isNotBlank() || trade.marketConditions.isNotBlank() || trade.emotions.isNotBlank() || trade.mistakes.isNotBlank() || trade.notes.isNotBlank()) {
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = JournalSurface),
              shape = RoundedCornerShape(14.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
            ) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("NOTES & PSYCHOLOGY", style = MaterialTheme.typography.labelSmall, color = TextSecondary)

                if (trade.entryReason.isNotBlank()) {
                  Text("Entry Reason / Confluences:", fontSize = 11.sp, color = TextSecondary)
                  Text(trade.entryReason, fontSize = 13.sp, color = TextPrimary)
                  Spacer(modifier = Modifier.height(4.dp))
                }
                if (trade.marketConditions.isNotBlank()) {
                  Text("Market Conditions:", fontSize = 11.sp, color = TextSecondary)
                  Text(trade.marketConditions, fontSize = 13.sp, color = TextPrimary)
                  Spacer(modifier = Modifier.height(4.dp))
                }
                if (trade.emotions.isNotBlank()) {
                  Text("Emotions Before Entry:", fontSize = 11.sp, color = TextSecondary)
                  Text(trade.emotions, fontSize = 13.sp, color = TextPrimary)
                  Spacer(modifier = Modifier.height(4.dp))
                }
                if (trade.mistakes.isNotBlank()) {
                  Text("Mistakes Made:", fontSize = 11.sp, color = RedLoss)
                  Text(trade.mistakes, fontSize = 13.sp, color = TextPrimary)
                  Spacer(modifier = Modifier.height(4.dp))
                }
                if (trade.notes.isNotBlank()) {
                  Text("Additional Notes:", fontSize = 11.sp, color = TextSecondary)
                  Text(trade.notes, fontSize = 13.sp, color = TextPrimary)
                }
              }
            }
          }

          // Chart Screenshot Card
          if (trade.screenshotUrl.isNotBlank()) {
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
                  Text("ATTACHED CHART SCREENSHOT", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                  Text("Tap to view full", fontSize = 11.sp, color = GoldAccent)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { showFullScreenImage = true }
                ) {
                  AsyncImage(
                    model = trade.screenshotUrl,
                    contentDescription = "Chart Screenshot",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(24.dp))
        }
      }
    }
  }

  // Full Screen Image Dialog
  if (showFullScreenImage && trade.screenshotUrl.isNotBlank()) {
    Dialog(
      onDismissRequest = { showFullScreenImage = false },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black),
        contentAlignment = Alignment.Center
      ) {
        AsyncImage(
          model = trade.screenshotUrl,
          contentDescription = "Full chart screenshot",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Fit
        )
        IconButton(
          onClick = { showFullScreenImage = false },
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(16.dp)
            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
        ) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }
      }
    }
  }

  // Delete Confirmation Dialog
  if (showDeleteConfirm) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirm = false },
      containerColor = JournalSurface,
      shape = RoundedCornerShape(16.dp),
      title = { Text("Delete Trade Record?", color = TextPrimary) },
      text = {
        Text(
          "Are you sure you want to permanently delete this XAUUSD trade record? This action cannot be undone.",
          color = TextSecondary
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showDeleteConfirm = false
            onDelete()
          },
          colors = ButtonDefaults.buttonColors(containerColor = RedLoss)
        ) {
          Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirm = false }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }
}

@Composable
private fun DetailRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, fontSize = 13.sp, color = TextSecondary)
    Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
  }
}

@Composable
private fun DisciplineAnswerRow(question: String, answer: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = question, fontSize = 12.sp, color = TextSecondary)
    val (col, text) = when (answer.lowercase()) {
      "yes" -> EmeraldProfit to "YES"
      "no" -> RedLoss to "NO"
      else -> TextMuted to "N/A"
    }
    Surface(
      color = col.copy(alpha = 0.15f),
      shape = RoundedCornerShape(4.dp)
    ) {
      Text(
        text = text,
        color = col,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
      )
    }
  }
}
