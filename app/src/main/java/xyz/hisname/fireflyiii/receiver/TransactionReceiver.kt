package xyz.hisname.fireflyiii.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.work.*
import xyz.hisname.fireflyiii.repository.workers.TranscationWorker
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils

class TransactionReceiver: BroadcastReceiver()  {

    override fun onReceive(context: Context, intent: Intent) {
        val sharedPref: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }
        val baseUrl: String by lazy { sharedPref.getString("fireflyUrl", "") }
        val accessToken: String by lazy { sharedPref.getString("access_token","") }
        if(baseUrl.isBlank() || accessToken.isBlank()){
            val notif = NotificationUtils(context)
            notif.showNotSignedIn()
        } else {
            val transactionData = Data.Builder()
                    .putString("description", intent.getStringExtra("description"))
                    .putString("date", intent.getStringExtra("date"))
                    .putString("amount", intent.getStringExtra("amount"))
                    .putString("currency", intent.getStringExtra("currency"))
                    .putString("billName", intent.getStringExtra("billName"))
                    .putString("tags", intent.getStringExtra("tags"))
                    .putString("categoryName", intent.getStringExtra("categoryName"))
                    .putString("budgetName", intent.getStringExtra("budgetName"))
                    .putString("interestDate", intent.getStringExtra("interestDate"))
                    .putString("bookDate", intent.getStringExtra("bookDate"))
                    .putString("processDate", intent.getStringExtra("processDate"))
                    .putString("dueDate", intent.getStringExtra("dueDate"))
                    .putString("paymentDate", intent.getStringExtra("paymentDate"))
                    .putString("invoiceDate", intent.getStringExtra("invoiceDate"))
            when {
                intent.action == "firefly.hisname.ADD_DEPOSIT" -> {
                    val depositData = transactionData.putString("destinationName",
                            intent.getStringExtra("destinationName"))
                            .putString("billName", intent.getStringExtra("billName"))
                    transactionWork(depositData, "deposit")
                }
                intent.action == "firefly.hisname.ADD_WITHDRAW" -> {
                    val withdrawData = transactionData.putString("sourceName",
                            intent.getStringExtra("sourceName"))
                    transactionWork(withdrawData, "withdrawal")
                }
                intent.action == "firefly.hisname.ADD_TRANSFER" -> {
                    val transferData = transactionData
                            .putString("sourceName", intent.getStringExtra("sourceName"))
                            .putString("destinationName", intent.getStringExtra("destinationName"))
                            .putString("piggyBankName", intent.getStringExtra("piggyBankName"))
                    transactionWork(transferData, "transfer")
                }
                else -> {

                }
            }
        }
    }

    private fun transactionWork(data: Data.Builder, type: String){
        val transactionWork = OneTimeWorkRequest.Builder(TranscationWorker::class.java)
                .setInputData(data.putString("transactionType" ,type).build())
                .setConstraints(Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .build()
        WorkManager.getInstance().enqueue(transactionWork)
    }
}