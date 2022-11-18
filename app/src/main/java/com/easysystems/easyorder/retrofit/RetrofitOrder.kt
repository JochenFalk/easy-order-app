package com.easysystems.easyorder.retrofit

import com.easysystems.easyorder.data.Order
import com.easysystems.easyorder.data.Session
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RetrofitOrder {

    @GET("orders/{id}")
    fun retrieveOrderById(@Path("id") id: Int) : Call<Order>

    @POST("orders")
    fun createOrder(@Body order: Order) : Call<Order>
}
