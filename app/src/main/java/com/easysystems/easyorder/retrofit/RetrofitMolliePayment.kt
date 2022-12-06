package com.easysystems.easyorder.retrofit

import com.easysystems.easyorder.data.MolliePayment
import com.easysystems.easyorder.data.MolliePaymentDTO
import com.easysystems.easyorder.data.OrderDTO
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface RetrofitMolliePayment {

    @POST("payments")
    fun retrievePaymentFromMollie(@HeaderMap headers: Map<String, String>, @Body body: RequestBody) : Call<MolliePayment>

    @POST("molliePayments")
    fun createPayment(@Body molliePayment: MolliePaymentDTO) : Call<MolliePaymentDTO>
}