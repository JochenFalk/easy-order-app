package com.easysystems.easyorder

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

public interface RetrofitAPI {

    @GET("items")
    fun getAllItems() : Call<List<Item>>
    @GET("items/{id}")
    fun getItemById(@Path("id") id: Int) : Call<Item>

}