package com.example.ui.auth

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.JournalBackground
import com.example.ui.theme.JournalBorder
import com.example.ui.theme.JournalSurface
import com.example.ui.theme.RedLoss
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AppLockScreen(
  expectedPin: String,
  onUnlocked: () -> Unit
) {
  var enteredPin by remember { mutableStateOf("") }
  var isError by remember { mutableStateOf(false) }

  fun handleDigit(digit: String) {
    if (enteredPin.length < 4) {
      val next = enteredPin + digit
      enteredPin = next
      isError = false
      if (next.length == 4) {
        if (next == expectedPin) {
          onUnlocked()
        } else {
          isError = true
          enteredPin = ""
        }
      }
    }
  }

  fun handleBackspace() {
    if (enteredPin.isNotEmpty()) {
      enteredPin = enteredPin.dropLast(1)
      isError = false
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(JournalBackground)
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(72.dp)
          .background(JournalSurface, CircleShape)
          .border(1.5.dp, GoldAccent, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = "Locked",
          tint = GoldAccent,
          modifier = Modifier.size(36.dp)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "JOURNAL LOCKED",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
        color = TextPrimary
      )

      Text(
        text = if (isError) "Incorrect PIN. Try again." else "Enter your 4-digit security PIN",
        style = MaterialTheme.typography.bodyMedium,
        color = if (isError) RedLoss else TextSecondary,
        modifier = Modifier.padding(top = 8.dp)
      )

      Spacer(modifier = Modifier.height(32.dp))

      // 4 Dots
      Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        repeat(4) { idx ->
          val isFilled = idx < enteredPin.length
          Box(
            modifier = Modifier
              .size(18.dp)
              .clip(CircleShape)
              .background(
                if (isError) RedLoss
                else if (isFilled) EmeraldProfit
                else JournalSurface
              )
              .border(
                1.5.dp,
                if (isError) RedLoss
                else if (isFilled) EmeraldProfit
                else JournalBorder,
                CircleShape
              )
          )
        }
      }

      Spacer(modifier = Modifier.height(48.dp))

      // Keypad
      val digits = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
      )

      Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        digits.forEach { row ->
          Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
          ) {
            row.forEach { item ->
              when (item) {
                "" -> {
                  Spacer(modifier = Modifier.size(70.dp))
                }
                "DEL" -> {
                  Box(
                    modifier = Modifier
                      .size(70.dp)
                      .clip(CircleShape)
                      .background(JournalSurface)
                      .clickable { handleBackspace() },
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.Backspace,
                      contentDescription = "Backspace",
                      tint = TextSecondary,
                      modifier = Modifier.size(24.dp)
                    )
                  }
                }
                else -> {
                  Box(
                    modifier = Modifier
                      .size(70.dp)
                      .clip(CircleShape)
                      .background(JournalSurface)
                      .border(1.dp, JournalBorder, CircleShape)
                      .clickable { handleDigit(item) },
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = item,
                      fontSize = 26.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = TextPrimary,
                      textAlign = TextAlign.Center
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
