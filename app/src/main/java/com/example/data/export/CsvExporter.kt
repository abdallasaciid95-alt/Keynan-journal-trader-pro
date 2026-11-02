package com.example.data.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.AccountTransaction
import com.example.data.model.Trade
import com.example.data.model.TradingAccount
import java.io.File
import java.io.FileWriter

object CsvExporter {

  fun generateTradesCsv(trades: List<Trade>, accountsMap: Map<String, String>): String {
    val sb = StringBuilder()
    // Headers
    sb.append("Trade ID,Date,Time,Account,Instrument,Direction,Lot Size,Entry Price,Exit Price,Stop Loss,Take Profit,Result,Realized PnL (USD),Session,Strategy,Followed Plan,Respected Stop Loss,Emotional Trading,Overtraded,Mistakes,Lessons,Notes\n")

    for (t in trades) {
      val accName = accountsMap[t.accountId] ?: "Default Account"
      sb.append("\"${escape(t.id)}\",")
      sb.append("\"${escape(t.date)}\",")
      sb.append("\"${escape(t.entryTime)}\",")
      sb.append("\"${escape(accName)}\",")
      sb.append("\"${escape(t.instrument)}\",")
      sb.append("\"${escape(t.direction)}\",")
      sb.append("${t.lotSize},")
      sb.append("${t.entryPrice},")
      sb.append("${t.exitPrice},")
      sb.append("${t.stopLossPrice ?: ""},")
      sb.append("${t.takeProfitPrice ?: ""},")
      sb.append("\"${escape(t.result)}\",")
      sb.append("${t.pnl},")
      sb.append("\"${escape(t.session)}\",")
      sb.append("\"${escape(t.strategy)}\",")
      sb.append("\"${escape(t.followedPlan)}\",")
      sb.append("\"${escape(t.respectedStopLoss)}\",")
      sb.append("\"${escape(t.emotionalTrading)}\",")
      sb.append("\"${escape(t.overtraded)}\",")
      sb.append("\"${escape(t.mistakes)}\",")
      sb.append("\"${escape(t.lessons)}\",")
      sb.append("\"${escape(t.notes)}\"\n")
    }
    return sb.toString()
  }

  fun generateAccountSummaryCsv(
    accounts: List<TradingAccount>,
    trades: List<Trade>,
    transactions: List<AccountTransaction>
  ): String {
    val sb = StringBuilder()
    sb.append("Account Name,Type,Status,Starting Balance (USD),Net Trading PnL (USD),Total Deposits (USD),Total Withdrawals (USD),Calculated Balance (USD),Total Trades,Win Rate (%)\n")

    for (acc in accounts) {
      val accTrades = trades.filter { it.accountId == acc.id && !it.isOpen() }
      val netPnl = accTrades.sumOf { it.pnl }
      val wins = accTrades.count { it.isWin() || it.pnl > 0 }
      val winRate = if (accTrades.isNotEmpty()) (wins.toDouble() / accTrades.size) * 100.0 else 0.0

      val accTx = transactions.filter { it.accountId == acc.id }
      val dep = accTx.filter { it.type == "deposit" }.sumOf { it.amount }
      val wth = accTx.filter { it.type == "withdrawal" }.sumOf { it.amount }
      val bal = acc.startingBalance + netPnl + dep - wth

      sb.append("\"${escape(acc.name)}\",")
      sb.append("\"${escape(acc.getDisplayType())}\",")
      sb.append("\"${escape(acc.getDisplayStatus())}\",")
      sb.append("${acc.startingBalance},")
      sb.append("${netPnl},")
      sb.append("${dep},")
      sb.append("${wth},")
      sb.append("${bal},")
      sb.append("${accTrades.size},")
      sb.append("${String.format(java.util.Locale.US, "%.1f", winRate)}%\n")
    }
    return sb.toString()
  }

  fun shareCsv(context: Context, filename: String, csvContent: String) {
    try {
      val exportDir = File(context.cacheDir, "exports")
      if (!exportDir.exists()) exportDir.mkdirs()
      val file = File(exportDir, filename)
      FileWriter(file).use { it.write(csvContent) }

      val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
      )

      val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_SUBJECT, "Keynan Trading Journal Export: $filename")
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      context.startActivity(Intent.createChooser(intent, "Export Trading Journal CSV"))
    } catch (e: Exception) {
      // Fallback: share plain text
      val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, csvContent)
        type = "text/plain"
      }
      context.startActivity(Intent.createChooser(sendIntent, "Export Trading Data"))
    }
  }

  private fun escape(s: String): String {
    return s.replace("\"", "\"\"")
  }
}
