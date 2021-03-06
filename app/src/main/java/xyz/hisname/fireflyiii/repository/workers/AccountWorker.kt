package xyz.hisname.fireflyiii.repository.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.google.gson.Gson
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.AccountsService
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils
import xyz.hisname.fireflyiii.util.retrofitCallback

class AccountWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters)  {


    override fun doWork(): Result {
        val name = inputData.getString("name") ?: ""
        val type = inputData.getString("type") ?: ""
        val currencyCode = inputData.getString("currencyCode") ?: ""
        val includeNetWorth = inputData.getInt("includeNetWorth",0)
        val accountRole = inputData.getString("accountRole")
        val ccType = inputData.getString("ccType")
        val ccMonthlyPaymentDate = inputData.getString("ccMonthlyPaymentDate")
        val liabilityType = inputData.getString("liabilityType")
        val liabilityAmount = inputData.getString("liabilityAmount")
        val liabilityStartDate = inputData.getString("liabilityStartDate")
        val interest = inputData.getString("interest")
        val interestPeriod = inputData.getString("interestPeriod")
        val accountNumber = inputData.getString("accountNumber")
        val notif = NotificationUtils(context)
        val accountService = RetrofitBuilder.getClient(baseUrl, accessToken)?.create(AccountsService::class.java)
        accountService?.addAccount(name,type,currencyCode,1,includeNetWorth,accountRole,ccType,
                ccMonthlyPaymentDate,liabilityType,liabilityAmount,liabilityStartDate,interest,interestPeriod,accountNumber)?.enqueue(
                retrofitCallback({ response ->
                    var errorBody = ""
                    var error = ""
                    if (response.errorBody() != null) {
                        errorBody = String(response.errorBody()?.bytes()!!)
                    }
                    val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
                    if(response.isSuccessful) {
                        notif.showAccountNotification("$name added successfully!", "Account Added")
                        Result.SUCCESS
                    } else {
                        error = when {
                            gson.errors.name != null -> gson.errors.name[0]
                            gson.errors.account_number != null -> gson.errors.account_number[0]
                            gson.errors.interest != null -> gson.errors.interest[0]
                            gson.errors.liabilityStartDate != null -> gson.errors.liabilityStartDate[0]
                            else -> "Error saving account"
                        }
                        notif.showAccountNotification(error, "Error Adding $name")
                        Result.FAILURE
                    }
                })
        )

        return Result.SUCCESS
    }
}