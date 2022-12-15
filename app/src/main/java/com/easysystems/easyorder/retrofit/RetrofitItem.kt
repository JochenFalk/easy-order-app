package com.easysystems.easyorder.retrofit

import com.easysystems.easyorder.data.ItemDTO
import retrofit2.Call
import retrofit2.http.GET

interface RetrofitItem {

    @GET("items")
    fun retrieveAllItems() : Call<List<ItemDTO>>
}