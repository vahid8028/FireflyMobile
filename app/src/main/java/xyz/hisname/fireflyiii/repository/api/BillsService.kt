package xyz.hisname.fireflyiii.repository.api

import retrofit2.Call
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants.Companion.BILL_API_ENDPONT
import xyz.hisname.fireflyiii.repository.models.bills.BillsModel

// Link to relevant doc: https://firefly-iii.readthedocs.io/en/latest/api/bills.html
interface BillsService {

    @GET(BILL_API_ENDPONT)
    fun getBills(): Call<BillsModel>


    @GET("$BILL_API_ENDPONT/{id}")
    fun getBillById(@Path("id") id: String): Call<BillsModel>

    @FormUrlEncoded
    @POST(BILL_API_ENDPONT)
    fun createBill(@Field("name") name: String, @Field("match") match: String,
                   @Field("amount_min") amountMin: String, @Field("amount_max") amountMax: String,
                   @Field("date") date: String, @Field("repeat_freq") repeatFreq: String,
                   @Field("skip") skip: String, @Field("automatch") automatch: String,
                   @Field("active") active: String, @Field("currency_id") currencyId: String,
                   @Field("notes") notes: String?
    ): Call<BillsModel>

    @DELETE("$BILL_API_ENDPONT/{id}")
    fun deleteBillById(@Path("id") id: String): Call<BillsModel>

}