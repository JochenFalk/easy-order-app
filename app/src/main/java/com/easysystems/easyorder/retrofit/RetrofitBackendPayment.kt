package com.easysystems.easyorder.retrofit

import com.easysystems.easyorder.data.MolliePaymentDTO
import retrofit2.Call
import retrofit2.http.*

interface RetrofitBackendPayment {

    @PUT("molliePayments/{id}")
    fun updatePaymentToBackend(@Path("id") id: Int, @Body molliePayment: MolliePaymentDTO) : Call<MolliePaymentDTO>

    @POST("molliePayments")
    fun createPaymentOnBackend(@Body molliePayment: MolliePaymentDTO) : Call<MolliePaymentDTO>
}