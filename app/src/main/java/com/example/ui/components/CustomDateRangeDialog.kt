package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.JournalBorder
import com.example.ui.theme.JournalSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CustomDateRangeDialog(
  initialStartDate: String,
  initialEndDate: String,
  onDismiss: () -> Unit,
  onConfirm: (startDate: String, endDate: String) -> Unit
) {
  var startDate by remember { mutableStateOf(initialStartDate) }
  var endDate by remember { mutableStateOf(initialEndDate) }
  val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

  fun setPreset(daysAgo: Int) {
    val cal = Calendar.getInstance()
    endDate = sdf.format(cal.time)
    cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
    startDate = sdf.format(cal.time)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = JournalSurface,
    shape = RoundedCornerShape(16.dp),
    title = {
      Text(
        text = "Select Date Range",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary
      )
    },
    text = {
      Column {
        Text(
          text = "Filter trades within custom dates (YYYY-MM-DD):",
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary,
          modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = { setPreset(7) },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text("Last 7D", color = TextSecondary)
          }
          OutlinedButton(
            onClick = { setPreset(30) },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text("Last 30D", color = TextSecondary)
          }
          OutlinedButton(
            onClick = { setPreset(90) },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text("Last 90D", color = TextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = startDate,
          onValueChange = { startDate = it },
          label = { Text("Start Date (YYYY-MM-DD)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EmeraldProfit,
            unfocusedBorderColor = JournalBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = endDate,
          onValueChange = { endDate = it },
          label = { Text("End Date (YYYY-MM-DD)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EmeraldProfit,
            unfocusedBorderColor = JournalBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (startDate.isNotBlank() && endDate.isNotBlank()) {
            onConfirm(startDate.trim(), endDate.trim())
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldProfit)
      ) {
        Text("Apply Filter")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    }
  )
}
