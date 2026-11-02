package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccountTransaction
import com.example.data.model.Trade
import com.example.data.model.TradingAccount
import com.example.data.model.UserSettings
import com.example.data.repository.TradingRepository
import com.example.ui.auth.AppLockScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.JournalBackground
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        AppRoot()
      }
    }
  }
}

@Composable
fun AppRoot() {
  var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

  DisposableEffect(Unit) {
    val listener = FirebaseAuth.AuthStateListener { auth ->
      currentUser = auth.currentUser
    }
    Firebase.auth.addAuthStateListener(listener)
    onDispose {
      Firebase.auth.removeAuthStateListener(listener)
    }
  }

  if (currentUser == null) {
    AuthScreen(
      onAuthSuccess = {
        currentUser = Firebase.auth.currentUser
      }
    )
  } else {
    AuthenticatedAppContent(
      userId = currentUser!!.uid
    )
  }
}

@Composable
fun AuthenticatedAppContent(userId: String) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }
  val repository = remember(userId) { TradingRepository(context) }

  val accounts by repository.observeAccounts().collectAsStateWithLifecycle(initialValue = emptyList())
  val trades by repository.observeTrades().collectAsStateWithLifecycle(initialValue = emptyList())
  val transactions by repository.observeTransactions().collectAsStateWithLifecycle(initialValue = emptyList())
  val userSettings by repository.observeSettings().collectAsStateWithLifecycle(initialValue = null)

  var isAppUnlocked by remember { mutableStateOf(false) }

  // Auto-bootstrap a default Personal Account if none exists yet
  LaunchedEffect(accounts) {
    if (accounts.isEmpty()) {
      repository.createAccount(
        TradingAccount(
          name = "Personal Gold Account",
          type = "personal",
          startingBalance = 10000.0,
          status = "active",
          notes = "Default trading account for XAUUSD"
        )
      )
    }
  }

  // App Lock Check
  val requiresLock = userSettings?.appLockEnabled == true &&
    (userSettings?.appLockPin?.isNotBlank() == true) &&
    !isAppUnlocked

  if (requiresLock) {
    AppLockScreen(
      expectedPin = userSettings!!.appLockPin,
      onUnlocked = { isAppUnlocked = true }
    )
  } else {
    AppNavigation(
      accounts = accounts,
      trades = trades,
      transactions = transactions,
      userSettings = userSettings,
      onSaveTrade = { trade ->
        scope.launch {
          if (trade.id.isBlank()) {
            repository.createTrade(trade)
          } else {
            repository.updateTrade(trade)
          }
        }
      },
      onDeleteTrade = { tradeId ->
        scope.launch {
          repository.deleteTrade(tradeId)
        }
      },
      onCreateAccount = { newAcc ->
        scope.launch {
          repository.createAccount(newAcc)
        }
      },
      onUpdateAccount = { updatedAcc ->
        scope.launch {
          repository.updateAccount(updatedAcc)
        }
      },
      onDeleteAccount = { accId ->
        scope.launch {
          repository.deleteAccount(accId)
        }
      },
      onAddTransaction = { tx ->
        scope.launch {
          repository.addTransaction(tx)
        }
      },
      onUpdateSettings = { settings ->
        scope.launch {
          repository.saveSettings(settings)
        }
      },
      onSignOut = {
        isAppUnlocked = false
        signOut(
          context = context,
          credentialManager = credentialManager,
          onSignOutComplete = {},
          scope = scope
        )
      },
      onDeleteAllData = {
        scope.launch {
          repository.deleteAllUserData()
        }
      }
    )
  }
}
