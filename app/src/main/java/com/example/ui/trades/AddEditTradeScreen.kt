package com.example.ui.trades

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Trade
import com.example.data.model.TradingAccount
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTradeScreen(
  existingTrade: Trade? = null,
  accounts: List<TradingAccount>,
  allExistingTrades: List<Trade>,
  defaultAccountId: String?,
  defaultSession: String,
  onSaveTrade: (Trade) -> Unit,
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  val sdfDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
  val sdfTime = remember { SimpleDateFormat("HH:mm", Locale.US) }
  val now = remember { Date() }

  var accountId by remember {
    mutableStateOf(
      existingTrade?.accountId
        ?: defaultAccountId
        ?: accounts.firstOrNull()?.id
        ?: ""
    )
  }
  var date by remember { mutableStateOf(existingTrade?.date ?: sdfDate.format(now)) }
  var entryTime by remember { mutableStateOf(existingTrade?.entryTime ?: sdfTime.format(now)) }
  var direction by remember { mutableStateOf(existingTrade?.direction ?: "BUY") }
  var entryPriceStr by remember { mutableStateOf(existingTrade?.entryPrice?.let { if (it > 0) it.toString() else "" } ?: "") }
  var exitPriceStr by remember { mutableStateOf(existingTrade?.exitPrice?.let { if (it > 0) it.toString() else "" } ?: "") }
  var lotSizeStr by remember { mutableStateOf(existingTrade?.lotSize?.toString() ?: "1.00") }
  var stopLossStr by remember { mutableStateOf(existingTrade?.stopLossPrice?.toString() ?: "") }
  var takeProfitStr by remember { mutableStateOf(existingTrade?.takeProfitPrice?.toString() ?: "") }
  var result by remember { mutableStateOf(existingTrade?.result ?: "win") }
  var pnlStr by remember { mutableStateOf(existingTrade?.pnl?.toString() ?: "") }
  var session by remember { mutableStateOf(existingTrade?.session ?: defaultSession) }
  var strategy by remember { mutableStateOf(existingTrade?.strategy ?: "") }
  var entryReason by remember { mutableStateOf(existingTrade?.entryReason ?: "") }
  var marketConditions by remember { mutableStateOf(existingTrade?.marketConditions ?: "") }
  var emotions by remember { mutableStateOf(existingTrade?.emotions ?: "") }
  var mistakes by remember { mutableStateOf(existingTrade?.mistakes ?: "") }
  var lessons by remember { mutableStateOf(existingTrade?.lessons ?: "") }
  var notes by remember { mutableStateOf(existingTrade?.notes ?: "") }
  var screenshotUrl by remember { mutableStateOf(existingTrade?.screenshotUrl ?: "") }

  // Discipline questions
  var followedPlan by remember { mutableStateOf(existingTrade?.followedPlan ?: "yes") }
  var respectedStopLoss by remember { mutableStateOf(existingTrade?.respectedStopLoss ?: "yes") }
  var enteredByStrategy by remember { mutableStateOf(existingTrade?.enteredByStrategy ?: "yes") }
  var emotionalTrading by remember { mutableStateOf(existingTrade?.emotionalTrading ?: "no") }
  var overtraded by remember { mutableStateOf(existingTrade?.overtraded ?: "no") }
  var whatLearned by remember { mutableStateOf(existingTrade?.whatLearned ?: "") }

  var isSubmitting by remember { mutableStateOf(false) }
  var validationError by remember { mutableStateOf<String?>(null) }
  var showDuplicateWarningDialog by remember { mutableStateOf(false) }
  var pendingTradeToSave by remember { mutableStateOf<Trade?>(null) }

  // Photo picker for trade chart screenshot
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    uri?.let {
      scope.launch {
        try {
          val base64 = withContext(Dispatchers.IO) {
            val input: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(input)
            input?.close()
            if (bitmap != null) {
              // Scale down image to avoid huge payload (max 1024 width)
              val scaled = if (bitmap.width > 1024) {
                val ratio = 1024f / bitmap.width
                Bitmap.createScaledBitmap(bitmap, 1024, (bitmap.height * ratio).toInt(), true)
              } else bitmap
              val stream = ByteArrayOutputStream()
              scaled.compress(Bitmap.CompressFormat.JPEG, 75, stream)
              val bytes = stream.toByteArray()
              "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
            } else ""
          }
          if (base64.isNotBlank()) {
            screenshotUrl = base64
          }
        } catch (e: Exception) {
          validationError = "Failed to load screenshot: ${e.message}"
        }
      }
    }
  }

  // Contract calculation helper: 1 Standard Lot of XAUUSD = 100 troy ounces
  fun calculateEstimatePnl() {
    val entry = entryPriceStr.toDoubleOrNull()
    val exit = exitPriceStr.toDoubleOrNull()
    val lots = lotSizeStr.toDoubleOrNull() ?: 1.0
    if (entry != null && exit != null) {
      val diff = if (direction == "BUY") exit - entry else entry - exit
      val est = diff * lots * 100.0 // Standard gold contract size: 100 oz
      pnlStr = String.format(Locale.US, "%.2f", est)
      if (est > 0) result = "win" else if (est < 0) result = "loss" else result = "breakeven"
    }
  }

  fun executeSave(trade: Trade) {
    isSubmitting = true
    onSaveTrade(trade)
  }

  fun onSaveClicked() {
    validationError = null
    val entryPrice = entryPriceStr.toDoubleOrNull()
    val exitPrice = exitPriceStr.toDoubleOrNull() ?: 0.0
    val lotSize = lotSizeStr.toDoubleOrNull()
    val pnl = if (result == "open") 0.0 else (pnlStr.toDoubleOrNull() ?: 0.0)

    if (accountId.isBlank()) {
      validationError = "Please select or create a trading account first."
      return
    }
    if (date.isBlank() || entryTime.isBlank()) {
      validationError = "Trading Date and Entry Time are required."
      return
    }
    if (entryPrice == null || entryPrice <= 0) {
      validationError = "Please enter a valid Gold Entry Price (e.g. 2745.50)."
      return
    }
    if (lotSize == null || lotSize <= 0) {
      validationError = "Please enter a valid Lot Size (e.g. 0.10 or 1.00)."
      return
    }
    if (result != "open" && exitPriceStr.isBlank()) {
      validationError = "Please enter Exit Price for a closed trade."
      return
    }
    if (strategy.isBlank()) {
      validationError = "Please enter or select a Trading Strategy."
      return
    }

    val trade = Trade(
      id = existingTrade?.id ?: "",
      userId = existingTrade?.userId ?: "",
      accountId = accountId,
      instrument = "XAUUSD",
      date = date.trim(),
      entryTime = entryTime.trim(),
      direction = direction,
      entryPrice = entryPrice,
      exitPrice = exitPrice,
      lotSize = lotSize,
      stopLossPrice = stopLossStr.toDoubleOrNull(),
      takeProfitPrice = takeProfitStr.toDoubleOrNull(),
      result = result,
      pnl = pnl,
      session = session,
      strategy = strategy.trim(),
      entryReason = entryReason.trim(),
      marketConditions = marketConditions.trim(),
      emotions = emotions.trim(),
      mistakes = mistakes.trim(),
      lessons = lessons.trim(),
      notes = notes.trim(),
      screenshotUrl = screenshotUrl,
      followedPlan = followedPlan,
      respectedStopLoss = respectedStopLoss,
      enteredByStrategy = enteredByStrategy,
      emotionalTrading = emotionalTrading,
      overtraded = overtraded,
      whatLearned = whatLearned.trim(),
      createdAt = existingTrade?.createdAt
    )

    // Duplicate check: warns if a trade with same date, time, and entry price already exists
    if (existingTrade == null) {
      val duplicate = allExistingTrades.any {
        it.accountId == accountId &&
          it.date == trade.date &&
          it.entryTime == trade.entryTime &&
          it.direction == trade.direction &&
          kotlin.math.abs(it.entryPrice - trade.entryPrice) < 0.01
      }
      if (duplicate) {
        pendingTradeToSave = trade
        showDuplicateWarningDialog = true
        return
      }
    }

    executeSave(trade)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = if (existingTrade != null) "Edit Trade" else "Record Gold Trade",
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = TextPrimary
            )
          }
        },
        actions = {
          Button(
            onClick = { onSaveClicked() },
            enabled = !isSubmitting,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldProfit),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .padding(end = 8.dp)
              .testTag("save_trade_button")
          ) {
            if (isSubmitting) {
              CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF042116), strokeWidth = 2.dp)
            } else {
              Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF042116), modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Save Trade", color = Color(0xFF042116), fontWeight = FontWeight.Bold)
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = JournalBackground)
      )
    },
    containerColor = JournalBackground
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Validation error banner
      if (validationError != null) {
        Surface(
          color = RedLoss.copy(alpha = 0.15f),
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, RedLoss)
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = RedLoss)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = validationError!!, color = RedLoss, fontSize = 13.sp)
          }
        }
      }

      // SECTION 1: INSTRUMENT & ACCOUNT
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
            Text("INSTRUMENT", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Surface(
              color = GoldAccent.copy(alpha = 0.2f),
              shape = RoundedCornerShape(6.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent)
            ) {
              Text(
                text = "XAUUSD · Gold (Locked)",
                color = GoldAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Account selector dropdown
          Text("Trading Account", fontSize = 12.sp, color = TextSecondary)
          Spacer(modifier = Modifier.height(4.dp))
          var accountExpanded by remember { mutableStateOf(false) }
          val selectedAccount = accounts.find { it.id == accountId }

          ExposedDropdownMenuBox(
            expanded = accountExpanded,
            onExpandedChange = { accountExpanded = it }
          ) {
            OutlinedTextField(
              value = selectedAccount?.let { "${it.name} (${it.getDisplayType()})" } ?: "Select Account",
              onValueChange = {},
              readOnly = true,
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldProfit,
                unfocusedBorderColor = JournalBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
              ),
              shape = RoundedCornerShape(10.dp)
            )
            ExposedDropdownMenu(
              expanded = accountExpanded,
              onDismissRequest = { accountExpanded = false },
              modifier = Modifier.background(JournalSurface)
            ) {
              accounts.forEach { acc ->
                DropdownMenuItem(
                  text = {
                    Column {
                      Text(acc.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                      Text(acc.getDisplayType(), color = TextSecondary, fontSize = 11.sp)
                    }
                  },
                  onClick = {
                    accountId = acc.id
                    accountExpanded = false
                  }
                )
              }
            }
          }
        }
      }

      // SECTION 2: TRADE EXECUTION & DIRECTION
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JournalSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("EXECUTION DETAILS", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          Spacer(modifier = Modifier.height(12.dp))

          // BUY vs SELL segmented buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Button(
              onClick = { direction = "BUY" },
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("direction_buy_button"),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (direction == "BUY") EmeraldProfit else JournalSurface
              ),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (direction == "BUY") EmeraldProfit else JournalBorder
              )
            ) {
              Text(
                text = "BUY / LONG",
                color = if (direction == "BUY") Color(0xFF042116) else TextSecondary,
                fontWeight = FontWeight.Bold
              )
            }

            Button(
              onClick = { direction = "SELL" },
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("direction_sell_button"),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (direction == "SELL") RedLoss else JournalSurface
              ),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (direction == "SELL") RedLoss else JournalBorder
              )
            ) {
              Text(
                text = "SELL / SHORT",
                color = if (direction == "SELL") Color.White else TextSecondary,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Date & Time
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
              value = date,
              onValueChange = { date = it },
              label = { Text("Date (YYYY-MM-DD)") },
              modifier = Modifier.weight(1.2f),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldProfit,
                unfocusedBorderColor = JournalBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
              ),
              shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
              value = entryTime,
              onValueChange = { entryTime = it },
              label = { Text("Time (HH:mm)") },
              modifier = Modifier.weight(0.8f),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldProfit,
                unfocusedBorderColor = JournalBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
              ),
              shape = RoundedCornerShape(10.dp)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Entry Price, Exit Price, Lot Size
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
              value = entryPriceStr,
              onValueChange = { entryPriceStr = it },
              label = { Text("Entry Price") },
              placeholder = { Text("2750.50") },
              modifier = Modifier
                .weight(1f)
                .testTag("entry_price_input"),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldProfit,
                unfocusedBorderColor = JournalBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
              ),
              shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
              value = exitPriceStr,
              onValueChange = { exitPriceStr = it },
              label = { Text("Exit Price") },
              placeholder = { Text("2762.00") },
              modifier = Modifier
                .weight(1f)
                .testTag("exit_price_input"),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldProfit,
                unfocusedBorderColor = JournalBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
              ),
              shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
              value = lotSizeStr,
              onValueChange = { lotSizeStr = it },
              label = { Text("Lot Size") },
              placeholder = { Text("1.00") },
              modifier = Modifier
                .weight(0.8f)
                .testTag("lot_size_input"),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldProfit,
                unfocusedBorderColor = JournalBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
              ),
              shape = RoundedCornerShape(10.dp)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Stop Loss & Take Profit (Optional)
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
              value = stopLossStr,
              onValueChange = { stopLossStr = it },
              label = { Text("Stop Loss (opt)") },
              modifier = Modifier.weight(1f),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RedLoss,
                unfocusedBorderColor = JournalBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
              ),
              shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
              value = takeProfitStr,
              onValueChange = { takeProfitStr = it },
              label = { Text("Take Profit (opt)") },
              modifier = Modifier.weight(1f),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldProfit,
                unfocusedBorderColor = JournalBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
              ),
              shape = RoundedCornerShape(10.dp)
            )
          }
        }
      }

      // SECTION 3: RESULT & REALIZED P/L
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JournalSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("FINANCIAL RESULT", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          Spacer(modifier = Modifier.height(12.dp))

          // Result selector: Win, Loss, Break-even, Open
          val results = listOf("win" to "Win", "loss" to "Loss", "breakeven" to "Break-Even", "open" to "Open Trade")
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            results.forEach { (key, label) ->
              val isSel = result == key
              val col = when (key) {
                "win" -> EmeraldProfit
                "loss" -> RedLoss
                "breakeven" -> Color(0xFF94A3B8)
                else -> GoldAccent
              }
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clickable { result = key },
                color = if (isSel) col.copy(alpha = 0.2f) else JournalSurface,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) col else JournalBorder)
              ) {
                Text(
                  text = label,
                  color = if (isSel) col else TextSecondary,
                  fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 11.sp,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                  modifier = Modifier.padding(vertical = 10.dp)
                )
              }
            }
          }

          if (result != "open") {
            Spacer(modifier = Modifier.height(16.dp))

            // Authoritative Realized PnL input
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedTextField(
                value = pnlStr,
                onValueChange = { pnlStr = it },
                label = { Text("Actual Realized P/L ($)") },
                placeholder = { Text("e.g. 1250.00 or -450.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                  .weight(1f)
                  .testTag("realized_pnl_input"),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = EmeraldProfit,
                  unfocusedBorderColor = JournalBorder,
                  focusedTextColor = TextPrimary,
                  unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(10.dp)
              )

              // Quick estimate helper button
              OutlinedButton(
                onClick = { calculateEstimatePnl() },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                modifier = Modifier.height(54.dp)
              ) {
                Icon(imageVector = Icons.Default.Calculate, contentDescription = "Calculate", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Estimate", fontSize = 12.sp)
              }
            }

            Text(
              text = "Entered P/L is authoritative for all calculations (adjust for broker commissions/swap).",
              color = TextMuted,
              fontSize = 11.sp,
              modifier = Modifier.padding(top = 4.dp)
            )
          } else {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Open trade unrealized P/L is excluded from closed performance stats until closed.",
              color = GoldAccent,
              fontSize = 11.sp
            )
          }
        }
      }

      // SECTION 4: SESSION & STRATEGY
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JournalSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("SESSION & STRATEGY", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          Spacer(modifier = Modifier.height(12.dp))

          // Sessions
          Text("Trading Session", fontSize = 12.sp, color = TextSecondary)
          Spacer(modifier = Modifier.height(4.dp))
          val sessions = listOf("asian" to "Asian", "london" to "London", "new_york" to "New York", "other" to "Other")
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            sessions.forEach { (key, name) ->
              val isSel = session == key
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clickable { session = key },
                color = if (isSel) EmeraldProfit.copy(alpha = 0.15f) else JournalSurface,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isSel) EmeraldProfit else JournalBorder
                )
              ) {
                Text(
                  text = name,
                  color = if (isSel) EmeraldProfit else TextSecondary,
                  fontSize = 11.sp,
                  fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                  modifier = Modifier.padding(vertical = 8.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedTextField(
            value = strategy,
            onValueChange = { strategy = it },
            label = { Text("Trading Strategy") },
            placeholder = { Text("e.g. Liquidity Sweep + FVG") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("strategy_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = EmeraldProfit,
              unfocusedBorderColor = JournalBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
          )

          // Strategy suggestion chips
          val stratSuggestions = listOf("London Breakout", "NY Reversal", "FVG Retest", "Liquidity Sweep", "Trend Continuation")
          Row(
            modifier = Modifier
              .padding(top = 8.dp)
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            stratSuggestions.forEach { s ->
              Surface(
                modifier = Modifier.clickable { strategy = s },
                color = JournalSurface,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
              ) {
                Text(
                  text = s,
                  color = TextSecondary,
                  fontSize = 11.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }
        }
      }

      // SECTION 5: DISCIPLINE REVIEW
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JournalSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("TRADING DISCIPLINE REVIEW", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          Spacer(modifier = Modifier.height(12.dp))

          DisciplineQuestionRow(
            question = "Did I follow my trading plan?",
            selected = followedPlan,
            onSelect = { followedPlan = it }
          )
          DisciplineQuestionRow(
            question = "Did I respect my stop loss?",
            selected = respectedStopLoss,
            onSelect = { respectedStopLoss = it }
          )
          DisciplineQuestionRow(
            question = "Did I enter according to strategy?",
            selected = enteredByStrategy,
            onSelect = { enteredByStrategy = it }
          )
          DisciplineQuestionRow(
            question = "Was I trading emotionally?",
            selected = emotionalTrading,
            onSelect = { emotionalTrading = it }
          )
          DisciplineQuestionRow(
            question = "Did I overtrade today?",
            selected = overtraded,
            onSelect = { overtraded = it }
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = whatLearned,
            onValueChange = { whatLearned = it },
            label = { Text("What did I learn from this trade?") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = EmeraldProfit,
              unfocusedBorderColor = JournalBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
          )
        }
      }

      // SECTION 6: NOTES, EMOTIONS & MISTAKES
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JournalSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JournalBorder)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("PSYCHOLOGY & OBSERVATIONS", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = entryReason,
            onValueChange = { entryReason = it },
            label = { Text("Entry Reason / Confluence") },
            placeholder = { Text("e.g. 4H support bounce, 15m MSS") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = EmeraldProfit,
              unfocusedBorderColor = JournalBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = marketConditions,
            onValueChange = { marketConditions = it },
            label = { Text("Market Conditions") },
            placeholder = { Text("e.g. Strong Bullish trend, High news volatility") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = EmeraldProfit,
              unfocusedBorderColor = JournalBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = emotions,
            onValueChange = { emotions = it },
            label = { Text("Emotions Before Entry") },
            placeholder = { Text("e.g. Calm & Patient, or FOMO rush") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = EmeraldProfit,
              unfocusedBorderColor = JournalBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = mistakes,
            onValueChange = { mistakes = it },
            label = { Text("Mistakes Made") },
            placeholder = { Text("e.g. None, Chased entry, Moved stop loss") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = RedLoss,
              unfocusedBorderColor = JournalBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Additional Trade Notes") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = EmeraldProfit,
              unfocusedBorderColor = JournalBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
          )
        }
      }

      // SECTION 7: CHART SCREENSHOT ATTACHMENT
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
            Text("CHART SCREENSHOT", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Button(
              onClick = {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = JournalSurface),
              border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent)
            ) {
              Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (screenshotUrl.isNotBlank()) "Replace Photo" else "Attach Screenshot", color = GoldAccent, fontSize = 12.sp)
            }
          }

          if (screenshotUrl.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, JournalBorder, RoundedCornerShape(10.dp))
            ) {
              AsyncImage(
                model = screenshotUrl,
                contentDescription = "Chart Screenshot",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )
              IconButton(
                onClick = { screenshotUrl = "" },
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(8.dp)
                  .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                  .size(32.dp)
              ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(18.dp))
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }

  // Duplicate Warning Dialog
  if (showDuplicateWarningDialog && pendingTradeToSave != null) {
    AlertDialog(
      onDismissRequest = { showDuplicateWarningDialog = false },
      containerColor = JournalSurface,
      shape = RoundedCornerShape(16.dp),
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = GoldAccent)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Potential Duplicate Entry", color = TextPrimary)
        }
      },
      text = {
        Text(
          text = "A trade with the same date ($date), time ($entryTime), direction ($direction), and entry price ($entryPriceStr) already exists for this account. Do you still want to save this record?",
          color = TextSecondary,
          fontSize = 13.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showDuplicateWarningDialog = false
            executeSave(pendingTradeToSave!!)
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldProfit)
        ) {
          Text("Yes, Save Trade", color = Color(0xFF042116), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDuplicateWarningDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }
}

@Composable
private fun DisciplineQuestionRow(
  question: String,
  selected: String,
  onSelect: (String) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = question,
      style = MaterialTheme.typography.bodySmall,
      color = TextPrimary,
      modifier = Modifier.weight(1f)
    )

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
      listOf("yes" to "Yes", "no" to "No", "na" to "N/A").forEach { (key, label) ->
        val isSel = selected == key
        val col = if (key == "yes") EmeraldProfit else if (key == "no") RedLoss else BreakevenGray
        Surface(
          modifier = Modifier.clickable { onSelect(key) },
          color = if (isSel) col.copy(alpha = 0.2f) else JournalSurface,
          shape = RoundedCornerShape(6.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) col else JournalBorder)
        ) {
          Text(
            text = label,
            color = if (isSel) col else TextSecondary,
            fontSize = 10.sp,
            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }
    }
  }
}
