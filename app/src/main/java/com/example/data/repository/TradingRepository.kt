package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AccountTransaction
import com.example.data.model.Trade
import com.example.data.model.TradingAccount
import com.example.data.model.UserSettings
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class TradingRepository(private val db: FirebaseFirestore) {

  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private val auth = Firebase.auth

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Trading Journal records.")
  }

  // --- ACCOUNTS ---
  fun observeAccounts(): Flow<List<TradingAccount>> = flow {
    val uid = requireUserId()
    val path = "users/$uid/accounts"
    emitAll(
      db.collection("users").document(uid).collection("accounts")
        .snapshots()
        .map { snapshot -> snapshot.toObjects(TradingAccount::class.java) }
        .catch { e ->
          if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
          throw e
        }
    )
  }

  suspend fun createAccount(account: TradingAccount): String {
    val uid = requireUserId()
    val ref = db.collection("users").document(uid).collection("accounts").document()
    val payload = hashMapOf<String, Any?>(
      "userId" to uid,
      "name" to account.name.trim(),
      "type" to account.type,
      "startingBalance" to account.startingBalance,
      "profitTarget" to account.profitTarget,
      "maxDailyLossLimit" to account.maxDailyLossLimit,
      "maxOverallDrawdownLimit" to account.maxOverallDrawdownLimit,
      "startDate" to account.startDate,
      "status" to account.status,
      "notes" to account.notes,
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null }

    try {
      ref.set(payload).await()
      return ref.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, ref.path)
      throw e
    }
  }

  suspend fun updateAccount(account: TradingAccount) {
    val uid = requireUserId()
    require(account.id.isNotBlank()) { "Account ID must not be blank" }
    val ref = db.collection("users").document(uid).collection("accounts").document(account.id)
    val payload = hashMapOf<String, Any?>(
      "userId" to uid,
      "name" to account.name.trim(),
      "type" to account.type,
      "startingBalance" to account.startingBalance,
      "profitTarget" to account.profitTarget,
      "maxDailyLossLimit" to account.maxDailyLossLimit,
      "maxOverallDrawdownLimit" to account.maxOverallDrawdownLimit,
      "startDate" to account.startDate,
      "status" to account.status,
      "notes" to account.notes,
      "createdAt" to account.createdAt,
      "updatedAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null }

    try {
      ref.set(payload).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, ref.path)
      throw e
    }
  }

  suspend fun deleteAccount(accountId: String) {
    val uid = requireUserId()
    val ref = db.collection("users").document(uid).collection("accounts").document(accountId)
    try {
      ref.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, ref.path)
      throw e
    }
  }

  // --- TRADES ---
  fun observeTrades(): Flow<List<Trade>> = flow {
    val uid = requireUserId()
    val path = "users/$uid/trades"
    emitAll(
      db.collection("users").document(uid).collection("trades")
        .snapshots()
        .map { snapshot -> snapshot.toObjects(Trade::class.java) }
        .catch { e ->
          if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
          throw e
        }
    )
  }

  suspend fun createTrade(trade: Trade): String {
    val uid = requireUserId()
    val ref = db.collection("users").document(uid).collection("trades").document()
    val payload = hashMapOf<String, Any?>(
      "userId" to uid,
      "accountId" to trade.accountId,
      "instrument" to "XAUUSD",
      "date" to trade.date,
      "entryTime" to trade.entryTime,
      "direction" to trade.direction,
      "entryPrice" to trade.entryPrice,
      "exitPrice" to trade.exitPrice,
      "lotSize" to trade.lotSize,
      "stopLossPrice" to trade.stopLossPrice,
      "takeProfitPrice" to trade.takeProfitPrice,
      "result" to trade.result,
      "pnl" to trade.pnl,
      "session" to trade.session,
      "strategy" to trade.strategy,
      "entryReason" to trade.entryReason,
      "marketConditions" to trade.marketConditions,
      "emotions" to trade.emotions,
      "mistakes" to trade.mistakes,
      "lessons" to trade.lessons,
      "notes" to trade.notes,
      "screenshotUrl" to trade.screenshotUrl,
      "followedPlan" to trade.followedPlan,
      "respectedStopLoss" to trade.respectedStopLoss,
      "enteredByStrategy" to trade.enteredByStrategy,
      "emotionalTrading" to trade.emotionalTrading,
      "overtraded" to trade.overtraded,
      "whatLearned" to trade.whatLearned,
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null }

    try {
      ref.set(payload).await()
      return ref.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, ref.path)
      throw e
    }
  }

  suspend fun updateTrade(trade: Trade) {
    val uid = requireUserId()
    require(trade.id.isNotBlank()) { "Trade ID must not be blank" }
    val ref = db.collection("users").document(uid).collection("trades").document(trade.id)
    val payload = hashMapOf<String, Any?>(
      "userId" to uid,
      "accountId" to trade.accountId,
      "instrument" to "XAUUSD",
      "date" to trade.date,
      "entryTime" to trade.entryTime,
      "direction" to trade.direction,
      "entryPrice" to trade.entryPrice,
      "exitPrice" to trade.exitPrice,
      "lotSize" to trade.lotSize,
      "stopLossPrice" to trade.stopLossPrice,
      "takeProfitPrice" to trade.takeProfitPrice,
      "result" to trade.result,
      "pnl" to trade.pnl,
      "session" to trade.session,
      "strategy" to trade.strategy,
      "entryReason" to trade.entryReason,
      "marketConditions" to trade.marketConditions,
      "emotions" to trade.emotions,
      "mistakes" to trade.mistakes,
      "lessons" to trade.lessons,
      "notes" to trade.notes,
      "screenshotUrl" to trade.screenshotUrl,
      "followedPlan" to trade.followedPlan,
      "respectedStopLoss" to trade.respectedStopLoss,
      "enteredByStrategy" to trade.enteredByStrategy,
      "emotionalTrading" to trade.emotionalTrading,
      "overtraded" to trade.overtraded,
      "whatLearned" to trade.whatLearned,
      "createdAt" to trade.createdAt,
      "updatedAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null }

    try {
      ref.set(payload).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, ref.path)
      throw e
    }
  }

  suspend fun deleteTrade(tradeId: String) {
    val uid = requireUserId()
    val ref = db.collection("users").document(uid).collection("trades").document(tradeId)
    try {
      ref.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, ref.path)
      throw e
    }
  }

  // --- TRANSACTIONS ---
  fun observeTransactions(): Flow<List<AccountTransaction>> = flow {
    val uid = requireUserId()
    val path = "users/$uid/transactions"
    emitAll(
      db.collection("users").document(uid).collection("transactions")
        .snapshots()
        .map { snapshot -> snapshot.toObjects(AccountTransaction::class.java) }
        .catch { e ->
          if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
          throw e
        }
    )
  }

  suspend fun addTransaction(tx: AccountTransaction): String {
    val uid = requireUserId()
    val ref = db.collection("users").document(uid).collection("transactions").document()
    val payload = hashMapOf<String, Any?>(
      "userId" to uid,
      "accountId" to tx.accountId,
      "type" to tx.type,
      "amount" to tx.amount,
      "date" to tx.date,
      "note" to tx.note,
      "createdAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null }

    try {
      ref.set(payload).await()
      return ref.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, ref.path)
      throw e
    }
  }

  suspend fun deleteTransaction(txId: String) {
    val uid = requireUserId()
    val ref = db.collection("users").document(uid).collection("transactions").document(txId)
    try {
      ref.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, ref.path)
      throw e
    }
  }

  // --- SETTINGS ---
  fun observeSettings(): Flow<UserSettings?> = flow {
    val uid = requireUserId()
    val path = "users/$uid/settings/preferences"
    emitAll(
      db.collection("users").document(uid).collection("settings").document("preferences")
        .snapshots()
        .map { snapshot -> snapshot.toObject(UserSettings::class.java) }
        .catch { e ->
          if (e is Exception) handleFirestoreError(e, OperationType.GET, path)
          throw e
        }
    )
  }

  suspend fun saveSettings(settings: UserSettings) {
    val uid = requireUserId()
    val ref = db.collection("users").document(uid).collection("settings").document("preferences")
    val payload = hashMapOf<String, Any?>(
      "userId" to uid,
      "defaultAccountId" to settings.defaultAccountId,
      "defaultSession" to settings.defaultSession,
      "dailyProfitGoal" to settings.dailyProfitGoal,
      "maxTradesPerDay" to settings.maxTradesPerDay,
      "appLockEnabled" to settings.appLockEnabled,
      "appLockPin" to settings.appLockPin
    ).filterValues { it != null }

    try {
      ref.set(payload).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, ref.path)
      throw e
    }
  }

  // --- SECURE DATA PURGE ---
  suspend fun deleteAllUserData() {
    val uid = requireUserId()
    val trades = db.collection("users").document(uid).collection("trades").get().await()
    for (doc in trades.documents) {
      doc.reference.delete().await()
    }
    val accounts = db.collection("users").document(uid).collection("accounts").get().await()
    for (doc in accounts.documents) {
      doc.reference.delete().await()
    }
    val txs = db.collection("users").document(uid).collection("transactions").get().await()
    for (doc in txs.documents) {
      doc.reference.delete().await()
    }
    db.collection("users").document(uid).collection("settings").document("preferences").delete().await()
  }
}
