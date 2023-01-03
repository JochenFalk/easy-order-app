package com.easysystems.easyorder.retrofit

import com.easysystems.easyorder.data.OrderDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RetrofitOrder {

    @POST("orders")
    fun createOrder(@Body orderDTO: OrderDTO) : Call<OrderDTO>
}
