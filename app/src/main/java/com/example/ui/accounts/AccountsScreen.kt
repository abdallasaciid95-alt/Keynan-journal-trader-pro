package com.example.ui.accounts

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.analytics.TradingAnalyticsEngine
import com.example.data.model.AccountTransaction
import com.example.data.model.Trade
import com.example.data.model.TradingAccount
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
import java.util.Date
import java.util.Locale

@Composable
fun AccountsScreen(
  accounts: List<TradingAccount>,
  trades: List<Trade>,
  transactions: List<AccountTransaction>,
  selectedAccountId: String?,
  onSelectAccount: (String?) -> Unit,
  onCreateAccount: (TradingAccount) -> Unit,
  onUpdateAccount: (TradingAccount) -> Unit,
  onDeleteAccount: (String) -> Unit,
  onAddTransaction: (AccountTransaction) -> Unit,
  modifier: Modifier = Modifier
) {
  var showCreateDialog by remember { mutableStateOf(false) }
  var accountToEdit by remember { mutableStateOf<TradingAccount?>(null) }
  var accountForTx by remember { mutableStateOf<TradingAccount?>(null) }
  var accountToDelete by remember { mutableStateOf<TradingAccount?>(null) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(JournalBackground),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
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
            text = "Trading Accounts",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
            color = TextPrimary
          )
          Text(
            text = "Separated metrics and risk rules per account",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
        }

        Button(
          onClick = { showCreateDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldProfit),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
          modifier = Modifier.testTag("create_account_button")
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF042116), modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("New Account", color = Color(0xFF042116), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      }
    }

    // Formula explanation banner
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = JournalSurface,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "FORMULA: Calculated Balance = Starting Balance + Realized P/L + Deposits - Withdrawals",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = GoldAccent
          )
          Text(
            text = "Deposits & withdrawals are recorded strictly as capital transactions, separated from trading profit.",
            fontSize = 10.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp)
          )
        }
      }
    }

    if (accounts.isEmpty()) {
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
            Text("No Trading Accounts Created", fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              "Create a Personal, Challenge Phase 1, Phase 2, or Funded account to start tracking.",
              color = TextSecondary,
              fontSize = 12.sp,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = { showCreateDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldProfit)
            ) {
              Text("Create Account", color = Color(0xFF042116), fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    } else {
      items(accounts, key = { it.id }) { acc ->
        val isSelected = selectedAccountId == acc.id
        val summary = remember(acc, trades, transactions) {
          TradingAnalyticsEngine.calculateAccountBalance(acc, trades, transactions)
        }

        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = JournalSurface),
          shape = RoundedCornerShape(16.dp),
          border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) EmeraldProfit else JournalBorder
          )
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Name, Type, Status, Active Radio
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = acc.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                  )
                  if (isSelected) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                      color = EmeraldProfit.copy(alpha = 0.2f),
                      shape = RoundedCornerShape(4.dp)
                    ) {
                      Text("ACTIVE", color = EmeraldProfit, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                  }
                }
                Text(
                  text = "${acc.getDisplayType()} · ${acc.getDisplayStatus()}",
                  fontSize = 12.sp,
                  color = TextSecondary
                )
              }

              Row {
                IconButton(onClick = { accountForTx = acc }) {
                  Icon(imageVector = Icons.Default.Paid, contentDescription = "Deposit / Withdraw", tint = GoldAccent)
                }
                IconButton(onClick = { accountToEdit = acc }) {
                  Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary)
                }
                IconButton(onClick = { accountToDelete = acc }) {
                  Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RedLoss)
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Balances Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Calculated Balance", fontSize = 11.sp, color = TextSecondary)
                Text(
                  "$${String.format(Locale.US, "%,.2f", summary.calculatedBalance)}",
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Black,
                  color = TextPrimary
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text("Net Realized Trading P/L", fontSize = 11.sp, color = TextSecondary)
                val pnlCol = if (summary.netTradingPnl >= 0) EmeraldProfit else RedLoss
                Text(
                  "${if (summary.netTradingPnl >= 0) "+" else ""}$${String.format(Locale.US, "%,.2f", summary.netTradingPnl)}",
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = pnlCol
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-metrics: Starting, Deposits, Withdrawals
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Starting: $${String.format(Locale.US, "%,.0f", summary.startingBalance)}", fontSize = 11.sp, color = TextMuted)
              Text("Deposits: +$${String.format(Locale.US, "%,.0f", summary.totalDeposits)}", fontSize = 11.sp, color = TextMuted)
              Text("Withdrawals: -$${String.format(Locale.US, "%,.0f", summary.totalWithdrawals)}", fontSize = 11.sp, color = TextMuted)
            }

            // Target progress bar if set
            if (acc.profitTarget != null && acc.profitTarget > 0) {
              Spacer(modifier = Modifier.height(12.dp))
              HorizontalDivider(color = JournalBorder)
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Profit Target: $${String.format(Locale.US, "%,.0f", acc.profitTarget)}", fontSize = 11.sp, color = GoldAccent)
                Text("${String.format(Locale.US, "%.1f", summary.targetProgressPercent ?: 0.0)}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
              }
              Spacer(modifier = Modifier.height(4.dp))
              LinearProgressIndicator(
                progress = { ((summary.targetProgressPercent ?: 0.0) / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(4.dp),
                color = GoldAccent,
                trackColor = JournalBorder
              )
            }

            // Max Drawdown limit warning if set
            if (acc.maxOverallDrawdownLimit != null && acc.maxOverallDrawdownLimit > 0) {
              Spacer(modifier = Modifier.height(8.dp))
              val isExceeded = summary.currentDrawdown >= acc.maxOverallDrawdownLimit
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  "Max DD Limit: $${String.format(Locale.US, "%,.0f", acc.maxOverallDrawdownLimit)}",
                  fontSize = 11.sp,
                  color = if (isExceeded) RedLoss else TextSecondary
                )
                Text(
                  "Current Dip: $${String.format(Locale.US, "%,.0f", summary.currentDrawdown)}",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isExceeded) RedLoss else TextSecondary
                )
              }
              if (isExceeded) {
                Text(
                  "⚠️ Warning: Current drawdown exceeds your configured limit!",
                  color = RedLoss,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(top = 2.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action: Set as active account button
            if (!isSelected) {
              OutlinedButton(
                onClick = { onSelectAccount(acc.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
              ) {
                Text("Select as Active Account", fontSize = 12.sp)
              }
            }
          }
        }
      }
    }
  }

  // Create / Edit Account Dialog
  if (showCreateDialog || accountToEdit != null) {
    AccountFormDialog(
      initialAccount = accountToEdit,
      onDismiss = {
        showCreateDialog = false
        accountToEdit = null
      },
      onSave = { acc ->
        if (accountToEdit != null) {
          onUpdateAccount(acc)
        } else {
          onCreateAccount(acc)
        }
        showCreateDialog = false
        accountToEdit = null
      }
    )
  }

  // Deposit / Withdrawal Dialog
  if (accountForTx != null) {
    DepositWithdrawalDialog(
      account = accountForTx!!,
      onDismiss = { accountForTx = null },
      onSave = { tx ->
        onAddTransaction(tx)
        accountForTx = null
      }
    )
  }

  // Delete Account Confirmation
  if (accountToDelete != null) {
    AlertDialog(
      onDismissRequest = { accountToDelete = null },
      containerColor = JournalSurface,
      shape = RoundedCornerShape(16.dp),
      title = { Text("Delete Account?", color = TextPrimary) },
      text = {
        Text(
          "Are you sure you want to delete '${accountToDelete!!.name}'? Trades associated with this account will remain in your records.",
          color = TextSecondary
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteAccount(accountToDelete!!.id)
            if (selectedAccountId == accountToDelete!!.id) onSelectAccount(null)
            accountToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = RedLoss)
        ) {
          Text("Delete", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { accountToDelete = null }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountFormDialog(
  initialAccount: TradingAccount?,
  onDismiss: () -> Unit,
  onSave: (TradingAccount) -> Unit
) {
  var name by remember { mutableStateOf(initialAccount?.name ?: "") }
  var type by remember { mutableStateOf(initialAccount?.type ?: "personal") }
  var startingBalanceStr by remember { mutableStateOf(initialAccount?.startingBalance?.toString() ?: "10000") }
  var profitTargetStr by remember { mutableStateOf(initialAccount?.profitTarget?.toString() ?: "") }
  var maxDailyLossStr by remember { mutableStateOf(initialAccount?.maxDailyLossLimit?.toString() ?: "") }
  var maxOverallDrawdownStr by remember { mutableStateOf(initialAccount?.maxOverallDrawdownLimit?.toString() ?: "") }
  var startDate by remember { mutableStateOf(initialAccount?.startDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
  var status by remember { mutableStateOf(initialAccount?.status ?: "active") }
  var notes by remember { mutableStateOf(initialAccount?.notes ?: "") }
  var error by remember { mutableStateOf<String?>(null) }

  val typeOptions = listOf(
    "personal" to "Personal Account",
    "challenge_phase_1" to "Challenge Phase 1",
    "challenge_phase_2" to "Challenge Phase 2",
    "funded" to "Funded Account"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = JournalSurface,
    shape = RoundedCornerShape(16.dp),
    title = {
      Text(
        text = if (initialAccount != null) "Edit Account" else "New Trading Account",
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        if (error != null) {
          Text(text = error!!, color = RedLoss, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Account Name") },
          placeholder = { Text("e.g. FTMO 100k Challenge") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EmeraldProfit,
            unfocusedBorderColor = JournalBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Account type
        Text("Account Type", fontSize = 11.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          typeOptions.forEach { (key, label) ->
            val isSel = type == key
            Surface(
              modifier = Modifier
                .weight(1f)
                .clickable { type = key },
              color = if (isSel) EmeraldProfit.copy(alpha = 0.2f) else JournalSurface,
              shape = RoundedCornerShape(6.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) EmeraldProfit else JournalBorder)
            ) {
              Text(
                text = label.replace("Account", "").replace("Challenge", "Ch."),
                color = if (isSel) EmeraldProfit else TextSecondary,
                fontSize = 10.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(vertical = 6.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = startingBalanceStr,
          onValueChange = { startingBalanceStr = it },
          label = { Text("Starting Balance ($)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EmeraldProfit,
            unfocusedBorderColor = JournalBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = profitTargetStr,
            onValueChange = { profitTargetStr = it },
            label = { Text("Target ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = GoldAccent,
              unfocusedBorderColor = JournalBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            )
          )
          OutlinedTextField(
            value = maxOverallDrawdownStr,
            onValueChange = { maxOverallDrawdownStr = it },
            label = { Text("Max DD ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = RedLoss,
              unfocusedBorderColor = JournalBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            )
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val bal = startingBalanceStr.toDoubleOrNull()
          if (name.isBlank()) {
            error = "Please enter an account name."
            return@Button
          }
          if (bal == null || bal < 0) {
            error = "Please enter a valid starting balance."
            return@Button
          }

          val acc = TradingAccount(
            id = initialAccount?.id ?: "",
            userId = initialAccount?.userId ?: "",
            name = name.trim(),
            type = type,
            startingBalance = bal,
            profitTarget = profitTargetStr.toDoubleOrNull(),
            maxDailyLossLimit = maxDailyLossStr.toDoubleOrNull(),
            maxOverallDrawdownLimit = maxOverallDrawdownStr.toDoubleOrNull(),
            startDate = startDate,
            status = status,
            notes = notes,
            createdAt = initialAccount?.createdAt
          )
          onSave(acc)
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldProfit)
      ) {
        Text("Save Account", color = Color(0xFF042116), fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    }
  )
}

@Composable
private fun DepositWithdrawalDialog(
  account: TradingAccount,
  onDismiss: () -> Unit,
  onSave: (AccountTransaction) -> Unit
) {
  var type by remember { mutableStateOf("deposit") }
  var amountStr by remember { mutableStateOf("") }
  var note by remember { mutableStateOf("") }
  val date = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
  var error by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = JournalSurface,
    shape = RoundedCornerShape(16.dp),
    title = {
      Text(
        text = "Capital Transaction",
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text("Account: ${account.name}", fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(10.dp))

        if (error != null) {
          Text(text = error!!, color = RedLoss, fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = { type = "deposit" },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = if (type == "deposit") EmeraldProfit else JournalSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (type == "deposit") EmeraldProfit else JournalBorder)
          ) {
            Text("Deposit (+)", color = if (type == "deposit") Color(0xFF042116) else TextSecondary, fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = { type = "withdrawal" },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = if (type == "withdrawal") RedLoss else JournalSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (type == "withdrawal") RedLoss else JournalBorder)
          ) {
            Text("Withdrawal (-)", color = if (type == "withdrawal") Color.White else TextSecondary, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = amountStr,
          onValueChange = { amountStr = it },
          label = { Text("Amount ($)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EmeraldProfit,
            unfocusedBorderColor = JournalBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = note,
          onValueChange = { note = it },
          label = { Text("Note / Description") },
          placeholder = { Text("e.g. Monthly profit payout or reset fee") },
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
          val amt = amountStr.toDoubleOrNull()
          if (amt == null || amt <= 0) {
            error = "Please enter a valid amount greater than 0."
            return@Button
          }
          val tx = AccountTransaction(
            accountId = account.id,
            type = type,
            amount = amt,
            date = date,
            note = note.trim()
          )
          onSave(tx)
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldProfit)
      ) {
        Text("Save Transaction", color = Color(0xFF042116), fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    }
  )
}
