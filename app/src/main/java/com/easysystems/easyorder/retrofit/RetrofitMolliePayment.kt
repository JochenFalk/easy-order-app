package com.easysystems.easyorder.retrofit

import com.easysystems.easyorder.data.MolliePayment
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface RetrofitMolliePayment {

    @POST("payments")
    fun retrievePaymentFromMollie(@HeaderMap headers: Map<String, String>, @Body body: RequestBody) : Call<MolliePayment>

    @GET("payments/{id}")
    fun retrievePaymentById(@HeaderMap headers: Map<String, String>, @Path("id") id: String) : Call<MolliePayment>
}