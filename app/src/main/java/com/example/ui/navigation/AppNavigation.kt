package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountTransaction
import com.example.data.model.Trade
import com.example.data.model.TradingAccount
import com.example.data.model.UserSettings
import com.example.ui.accounts.AccountsScreen
import com.example.ui.analytics.AnalyticsScreen
import com.example.ui.calendar.TradingCalendarScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.reports.ReportsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.trades.AddEditTradeScreen
import com.example.ui.trades.TradeDetailDialog
import com.example.ui.trades.TradeHistoryScreen
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.JournalBackground
import com.example.ui.theme.JournalBorder
import com.example.ui.theme.JournalSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class NavTab(val label: String, val icon: ImageVector) {
  DASHBOARD("Home", Icons.Default.Home),
  TRADES("Trades", Icons.Default.FormatListBulleted),
  ADD_TRADE("Add", Icons.Default.Add),
  ANALYTICS("Analytics", Icons.Default.Analytics),
  SETTINGS("Settings", Icons.Default.Settings)
}

enum class SubScreen {
  NONE,
  ACCOUNTS,
  CALENDAR,
  REPORTS,
  EDIT_TRADE
}

@Composable
fun AppNavigation(
  accounts: List<TradingAccount>,
  trades: List<Trade>,
  transactions: List<AccountTransaction>,
  userSettings: UserSettings?,
  onSaveTrade: (Trade) -> Unit,
  onDeleteTrade: (String) -> Unit,
  onCreateAccount: (TradingAccount) -> Unit,
  onUpdateAccount: (TradingAccount) -> Unit,
  onDeleteAccount: (String) -> Unit,
  onAddTransaction: (AccountTransaction) -> Unit,
  onUpdateSettings: (UserSettings) -> Unit,
  onSignOut: () -> Unit,
  onDeleteAllData: () -> Unit
) {
  var currentTab by remember { mutableStateOf(NavTab.DASHBOARD) }
  var subScreen by remember { mutableStateOf(SubScreen.NONE) }
  var selectedAccountId by remember { mutableStateOf<String?>(userSettings?.defaultAccountId?.ifBlank { null }) }
  var inspectingTrade by remember { mutableStateOf<Trade?>(null) }
  var tradeToEdit by remember { mutableStateOf<Trade?>(null) }

  // Android Back Handler
  BackHandler(enabled = subScreen != SubScreen.NONE || currentTab != NavTab.DASHBOARD) {
    if (subScreen != SubScreen.NONE) {
      subScreen = SubScreen.NONE
    } else if (currentTab != NavTab.DASHBOARD) {
      currentTab = NavTab.DASHBOARD
    }
  }

  val accountsMap = remember(accounts) { accounts.associate { it.id to it.name } }

  Scaffold(
    bottomBar = {
      if (subScreen == SubScreen.NONE && currentTab != NavTab.ADD_TRADE) {
        NavigationBar(
          containerColor = JournalSurface,
          contentColor = EmeraldProfit
        ) {
          NavTab.values().forEach { tab ->
            val isSelected = currentTab == tab
            if (tab == NavTab.ADD_TRADE) {
              // Prominent Center Add Button
              NavigationBarItem(
                selected = false,
                onClick = { currentTab = NavTab.ADD_TRADE },
                icon = {
                  Surface(
                    color = EmeraldProfit,
                    shape = CircleShape,
                    modifier = Modifier
                      .size(44.dp)
                      .testTag("nav_add_trade_tab")
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Trade",
                        tint = Color(0xFF042116),
                        modifier = Modifier.size(24.dp)
                      )
                    }
                  }
                },
                label = null
              )
            } else {
              NavigationBarItem(
                selected = isSelected,
                onClick = { currentTab = tab },
                icon = {
                  Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.label,
                    modifier = Modifier.size(22.dp)
                  )
                },
                label = {
                  Text(
                    text = tab.label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = EmeraldProfit,
                  selectedTextColor = EmeraldProfit,
                  unselectedIconColor = TextMuted,
                  unselectedTextColor = TextMuted,
                  indicatorColor = EmeraldProfit.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_${tab.name.lowercase()}_tab")
              )
            }
          }
        }
      }
    },
    containerColor = JournalBackground
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      when {
        subScreen == SubScreen.ACCOUNTS -> {
          AccountsScreen(
            accounts = accounts,
            trades = trades,
            transactions = transactions,
            selectedAccountId = selectedAccountId,
            onSelectAccount = { selectedAccountId = it },
            onCreateAccount = onCreateAccount,
            onUpdateAccount = onUpdateAccount,
            onDeleteAccount = onDeleteAccount,
            onAddTransaction = onAddTransaction
          )
        }
        subScreen == SubScreen.CALENDAR -> {
          TradingCalendarScreen(
            trades = trades,
            accounts = accounts,
            onTradeClicked = { inspectingTrade = it }
          )
        }
        subScreen == SubScreen.REPORTS -> {
          ReportsScreen(
            accounts = accounts,
            trades = trades,
            transactions = transactions,
            selectedAccountId = selectedAccountId,
            onTradeClicked = { inspectingTrade = it }
          )
        }
        subScreen == SubScreen.EDIT_TRADE && tradeToEdit != null -> {
          AddEditTradeScreen(
            existingTrade = tradeToEdit,
            accounts = accounts,
            allExistingTrades = trades,
            defaultAccountId = selectedAccountId,
            defaultSession = userSettings?.defaultSession ?: "london",
            onSaveTrade = { updated ->
              onSaveTrade(updated)
              subScreen = SubScreen.NONE
              tradeToEdit = null
            },
            onNavigateBack = {
              subScreen = SubScreen.NONE
              tradeToEdit = null
            }
          )
        }
        currentTab == NavTab.DASHBOARD -> {
          DashboardScreen(
            accounts = accounts,
            trades = trades,
            transactions = transactions,
            selectedAccountId = selectedAccountId,
            onSelectAccount = { selectedAccountId = it },
            onNavigateToAddTrade = { currentTab = NavTab.ADD_TRADE },
            onNavigateToTrades = { currentTab = NavTab.TRADES },
            onTradeClicked = { inspectingTrade = it }
          )
        }
        currentTab == NavTab.TRADES -> {
          TradeHistoryScreen(
            trades = trades,
            accounts = accounts,
            selectedAccountId = selectedAccountId,
            onSelectAccount = { selectedAccountId = it },
            onTradeClicked = { inspectingTrade = it },
            onNavigateToAddTrade = { currentTab = NavTab.ADD_TRADE }
          )
        }
        currentTab == NavTab.ADD_TRADE -> {
          AddEditTradeScreen(
            existingTrade = null,
            accounts = accounts,
            allExistingTrades = trades,
            defaultAccountId = selectedAccountId,
            defaultSession = userSettings?.defaultSession ?: "london",
            onSaveTrade = { newTrade ->
              onSaveTrade(newTrade)
              currentTab = NavTab.DASHBOARD
            },
            onNavigateBack = { currentTab = NavTab.DASHBOARD }
          )
        }
        currentTab == NavTab.ANALYTICS -> {
          AnalyticsScreen(
            trades = trades,
            accounts = accounts,
            selectedAccountId = selectedAccountId
          )
        }
        currentTab == NavTab.SETTINGS -> {
          SettingsScreen(
            userSettings = userSettings,
            accounts = accounts,
            trades = trades,
            transactions = transactions,
            onUpdateSettings = onUpdateSettings,
            onNavigateToAccounts = { subScreen = SubScreen.ACCOUNTS },
            onNavigateToCalendar = { subScreen = SubScreen.CALENDAR },
            onNavigateToReports = { subScreen = SubScreen.REPORTS },
            onSignOut = onSignOut,
            onDeleteAllData = onDeleteAllData
          )
        }
      }
    }
  }

  // Inspect Trade Detail Modal
  inspectingTrade?.let { trade ->
    TradeDetailDialog(
      trade = trade,
      accountName = accountsMap[trade.accountId] ?: "Default Account",
      onDismiss = { inspectingTrade = null },
      onEdit = {
        tradeToEdit = trade
        inspectingTrade = null
        subScreen = SubScreen.EDIT_TRADE
      },
      onDelete = {
        onDeleteTrade(trade.id)
        inspectingTrade = null
      }
    )
  }
}
