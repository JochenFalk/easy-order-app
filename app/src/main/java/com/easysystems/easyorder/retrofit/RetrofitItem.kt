package com.easysystems.easyorder.retrofit

import com.easysystems.easyorder.data.Item
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface RetrofitItem {

    @GET("items")
    fun getAllItems() : Call<List<Item>>
    @GET("items/{id}")
    fun getItemById(@Path("id") id: Int) : Call<Item>

}