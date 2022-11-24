package com.easysystems.easyorder.retrofit

import com.easysystems.easyorder.data.ItemDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface RetrofitItem {

    @GET("items")
    fun retrieveAllItems() : Call<List<ItemDTO>>
    @GET("items/{id}")
    fun retrieveItemById(@Path("id") id: Int) : Call<ItemDTO>

}