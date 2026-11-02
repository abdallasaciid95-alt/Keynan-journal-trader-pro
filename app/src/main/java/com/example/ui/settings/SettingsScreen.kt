package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.export.CsvExporter
import com.example.data.model.AccountTransaction
import com.example.data.model.Trade
import com.example.data.model.TradingAccount
import com.example.data.model.UserSettings
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.JournalBackground
import com.example.ui.theme.JournalBorder
import com.example.ui.theme.JournalSurface
import com.example.ui.theme.RedLoss
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  userSettings: UserSettings?,
  accounts: List<TradingAccount>,
  trades: List<Trade>,
  transactions: List<AccountTransaction>,
  onUpdateSettings: (UserSettings) -> Unit,
  onNavigateToAccounts: () -> Unit,
  onNavigateToCalendar: () -> Unit,
  onNavigateToReports: () -> Unit,
  onSignOut: () -> Unit,
  onDeleteAllData: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val user = Firebase.auth.currentUser

  var showPinDialog by remember { mutableStateOf(false) }
  var showSignOutConfirm by remember { mutableStateOf(false) }
  var showDeleteDataConfirm by remember { mutableStateOf(false) }
  var showPrivacyDialog by remember { mutableStateOf(false) }

  var dailyGoalStr by remember(userSettings) { mutableStateOf(userSettings?.dailyProfitGoal?.toString() ?: "") }
  var maxTradesStr by remember(userSettings) { mutableStateOf(userSettings?.maxTradesPerDay?.toString() ?: "") }
  var selectedDefaultSession by remember(userSettings) { mutableStateOf(userSettings?.defaultSession ?: "london") }
  var selectedDefaultAccount by remember(userSettings) { mutableStateOf(userSettings?.defaultAccountId ?: "") }

  fun savePreferences() {
    val updated = UserSettings(
      userId = user?.uid ?: "",
      defaultAccountId = selectedDefaultAccount,
      defaultSession = selectedDefaultSession,
      dailyProfitGoal = dailyGoalStr.toDoubleOrNull(),
      maxTradesPerDay = maxTradesStr.toIntOrNull(),
      appLockEnabled = userSettings?.appLockEnabled ?: false,
      appLockPin = userSettings?.appLockPin ?: ""
    )
    onUpdateSettings(updated)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(JournalBackground),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    item {
      Text(
        text = "Settings & Preferences",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
        color = TextPrimary
      )
    }

    // Google Profile Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JournalSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (user?.photoUrl != null) {
            AsyncImage(
              model = user.photoUrl,
              contentDescription = "Profile Photo",
              modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .border(1.5.dp, GoldAccent, CircleShape)
            )
          } else {
            Box(
              modifier = Modifier
                .size(54.dp)
                .background(JournalBackground, CircleShape)
                .border(1.5.dp, GoldAccent, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(28.dp))
            }
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = user?.displayName ?: "Keynan Trader",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )
            Text(
              text = user?.email ?: "Authenticated via Google",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
              Box(modifier = Modifier.size(6.dp).background(EmeraldProfit, CircleShape))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Verified Cloud Sync Active", fontSize = 11.sp, color = EmeraldProfit)
            }
          }
        }
      }
    }

    // Quick Navigation Modules (Accounts, Calendar, Reports)
    item {
      Text("MODULE SHORTCUTS", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JournalSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
      ) {
        Column {
          SettingNavRow(
            icon = Icons.Default.AccountBalance,
            title = "Account Management",
            subtitle = "${accounts.size} active trading accounts",
            onClick = onNavigateToAccounts
          )
          HorizontalDivider(color = JournalBorder)
          SettingNavRow(
            icon = Icons.Default.CalendarMonth,
            title = "Trading Calendar",
            subtitle = "Monthly profit/loss visualization",
            onClick = onNavigateToCalendar
          )
          HorizontalDivider(color = JournalBorder)
          SettingNavRow(
            icon = Icons.Default.Description,
            title = "Reports & Statements",
            subtitle = "CSV & Excel financial statements",
            onClick = onNavigateToReports
          )
        }
      }
    }

    // App Lock & Security
    item {
      Text("SECURITY & PRIVACY", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }

    item {
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
              Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = GoldAccent)
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text("App PIN Lock", fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                  if (userSettings?.appLockEnabled == true) "4-digit PIN protection enabled" else "Disabled",
                  fontSize = 12.sp,
                  color = TextSecondary
                )
              }
            }

            Switch(
              checked = userSettings?.appLockEnabled == true,
              onCheckedChange = { isChecked ->
                if (isChecked) {
                  showPinDialog = true
                } else {
                  val updated = userSettings?.copy(appLockEnabled = false) ?: UserSettings(userId = user?.uid ?: "", appLockEnabled = false)
                  onUpdateSettings(updated)
                }
              },
              colors = SwitchDefaults.colors(checkedThumbColor = EmeraldProfit, checkedTrackColor = EmeraldProfit.copy(alpha = 0.3f))
            )
          }

          if (userSettings?.appLockEnabled == true) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
              onClick = { showPinDialog = true },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Change Security PIN", fontSize = 12.sp, color = GoldAccent)
            }
          }

          HorizontalDivider(color = JournalBorder, modifier = Modifier.padding(vertical = 12.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showPrivacyDialog = true },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = EmeraldProfit)
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text("Zero-Trust Privacy Policy", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                Text("Private cloud storage locked to your Google ID", fontSize = 11.sp, color = TextSecondary)
              }
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
          }
        }
      }
    }

    // Default Trading Session & Goals
    item {
      Text("TRADING DEFAULTS & LIMITS", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JournalSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          // Default Session
          Text("Default Trading Session", fontSize = 12.sp, color = TextSecondary)
          val sessions = listOf("asian" to "Asian", "london" to "London", "new_york" to "New York", "other" to "Other")
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            sessions.forEach { (key, label) ->
              val isSel = selectedDefaultSession == key
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clickable {
                    selectedDefaultSession = key
                    savePreferences()
                  },
                color = if (isSel) EmeraldProfit.copy(alpha = 0.2f) else JournalSurface,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) EmeraldProfit else JournalBorder)
              ) {
                Text(
                  text = label,
                  color = if (isSel) EmeraldProfit else TextSecondary,
                  fontSize = 11.sp,
                  fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                  modifier = Modifier.padding(vertical = 6.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Daily Profit Goal & Max Trades
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
              value = dailyGoalStr,
              onValueChange = {
                dailyGoalStr = it
                savePreferences()
              },
              label = { Text("Daily Profit Goal ($)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.weight(1f),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldProfit,
                unfocusedBorderColor = JournalBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
              ),
              shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
              value = maxTradesStr,
              onValueChange = {
                maxTradesStr = it
                savePreferences()
              },
              label = { Text("Max Trades / Day") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.weight(1f),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldProfit,
                unfocusedBorderColor = JournalBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
              ),
              shape = RoundedCornerShape(8.dp)
            )
          }
        }
      }
    }

    // Data Export & Danger Zone
    item {
      Text("DATA & ACCOUNT ACTIONS", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JournalSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Button(
            onClick = {
              val accountsMap = accounts.associate { it.id to it.name }
              val csv = CsvExporter.generateTradesCsv(trades, accountsMap)
              val filename = "Keynan_Trades_Full_Backup_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.csv"
              CsvExporter.shareCsv(context, filename, csv)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = JournalSurface),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent)
          ) {
            Text("Backup & Export All Trades (CSV)", color = GoldAccent, fontSize = 13.sp)
          }

          Button(
            onClick = { showSignOutConfirm = true },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("sign_out_button"),
            colors = ButtonDefaults.buttonColors(containerColor = JournalSurface),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
          ) {
            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Sign Out of Google Account", color = TextSecondary, fontSize = 13.sp)
          }

          Button(
            onClick = { showDeleteDataConfirm = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = RedLoss.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, RedLoss.copy(alpha = 0.5f))
          ) {
            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = RedLoss, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Purge All Cloud Journal Records", color = RedLoss, fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }

  // Set PIN Dialog
  if (showPinDialog) {
    PinSetupDialog(
      onDismiss = { showPinDialog = false },
      onPinSaved = { pin ->
        val updated = userSettings?.copy(appLockEnabled = true, appLockPin = pin)
          ?: UserSettings(userId = user?.uid ?: "", appLockEnabled = true, appLockPin = pin)
        onUpdateSettings(updated)
        showPinDialog = false
      }
    )
  }

  // Sign out confirmation
  if (showSignOutConfirm) {
    AlertDialog(
      onDismissRequest = { showSignOutConfirm = false },
      containerColor = JournalSurface,
      shape = RoundedCornerShape(16.dp),
      title = { Text("Sign Out?", color = TextPrimary) },
      text = { Text("Are you sure you want to sign out of your Google account? Your trading data remains safely stored in your private cloud database.", color = TextSecondary) },
      confirmButton = {
        Button(
          onClick = {
            showSignOutConfirm = false
            onSignOut()
          },
          colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
        ) {
          Text("Sign Out", color = Color(0xFF332700), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showSignOutConfirm = false }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }

  // Delete all data confirmation
  if (showDeleteDataConfirm) {
    AlertDialog(
      onDismissRequest = { showDeleteDataConfirm = false },
      containerColor = JournalSurface,
      shape = RoundedCornerShape(16.dp),
      title = { Text("PERMANENTLY PURGE DATA?", color = RedLoss) },
      text = { Text("This will permanently delete all trades, trading accounts, and transactions associated with your Google ID from the cloud database. This action CANNOT be undone.", color = TextSecondary) },
      confirmButton = {
        Button(
          onClick = {
            showDeleteDataConfirm = false
            onDeleteAllData()
          },
          colors = ButtonDefaults.buttonColors(containerColor = RedLoss)
        ) {
          Text("Yes, Delete All", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteDataConfirm = false }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }

  // Privacy info dialog
  if (showPrivacyDialog) {
    AlertDialog(
      onDismissRequest = { showPrivacyDialog = false },
      containerColor = JournalSurface,
      shape = RoundedCornerShape(16.dp),
      title = { Text("Zero-Trust Financial Privacy", color = TextPrimary) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("1. Exclusive Ownership: All trading entries, lot sizes, and P/L statements are stored strictly under your private Google authenticated ID.", fontSize = 12.sp, color = TextSecondary)
          Text("2. No Broker Integration: This application does not connect to MetaTrader, brokers, or automated trading feeds. All records are manually logged by you.", fontSize = 12.sp, color = TextSecondary)
          Text("3. Zero Credential Storage: Google passwords and broker credentials are never requested or stored.", fontSize = 12.sp, color = TextSecondary)
        }
      },
      confirmButton = {
        Button(onClick = { showPrivacyDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = EmeraldProfit)) {
          Text("Understood", color = Color(0xFF042116))
        }
      }
    )
  }
}

@Composable
private fun SettingNavRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(14.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .background(JournalBackground, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
        Text(subtitle, fontSize = 11.sp, color = TextSecondary)
      }
    }
    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
  }
}

@Composable
private fun PinSetupDialog(
  onDismiss: () -> Unit,
  onPinSaved: (String) -> Unit
) {
  var pin by remember { mutableStateOf("") }
  var confirmPin by remember { mutableStateOf("") }
  var error by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = JournalSurface,
    shape = RoundedCornerShape(16.dp),
    title = { Text("Configure Security PIN", color = TextPrimary) },
    text = {
      Column {
        if (error != null) {
          Text(text = error!!, color = RedLoss, fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
        }
        OutlinedTextField(
          value = pin,
          onValueChange = { if (it.length <= 4) pin = it },
          label = { Text("4-Digit PIN") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
          value = confirmPin,
          onValueChange = { if (it.length <= 4) confirmPin = it },
          label = { Text("Confirm 4-Digit PIN") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (pin.length != 4) {
            error = "PIN must be exactly 4 digits."
            return@Button
          }
          if (pin != confirmPin) {
            error = "PINs do not match."
            return@Button
          }
          onPinSaved(pin)
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldProfit)
      ) {
        Text("Enable Lock", color = Color(0xFF042116), fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
    }
  )
}
