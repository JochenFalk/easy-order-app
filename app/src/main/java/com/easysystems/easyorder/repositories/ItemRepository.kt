package com.easysystems.easyorder.repositories

import android.content.Context
import android.util.Log
import com.easysystems.easyorder.data.ItemDTO
import com.easysystems.easyorder.databinding.ActivityMainBinding
import com.easysystems.easyorder.helpclasses.AppSettings
import com.easysystems.easyorder.retrofit.RetrofitItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

class ItemRepository {

    fun getAllItems(context: Context, binding: ActivityMainBinding, callback:(ArrayList<ItemDTO>?)->Unit) {

        val retrofitItem = generateRetrofitItem()
        val call: Call<List<ItemDTO>> = retrofitItem.retrieveAllItems()

        call.enqueue(object : Callback<List<ItemDTO>> {
            override fun onResponse(call: Call<List<ItemDTO>>, response: Response<List<ItemDTO>>) {

                if (response.isSuccessful) {

                    try {

                        val itemDTOList = response.body() as ArrayList<ItemDTO>
                        callback(itemDTOList)

                        Log.i("Info","Retrieved items successfully. List size: ${itemDTOList.size}")

                    } catch (ex: Exception) {

                        Log.i("Info","Returned list is empty: $ex")
                    }
                } else {

                    Log.i("Info","Failed to retrieve list!")
                }
            }

            override fun onFailure(call: Call<List<ItemDTO>>, t: Throwable) {

                Log.i("Info","Failed to retrieve list. Error: ${t.localizedMessage}")
            }
        })
    }

    private fun generateRetrofitItem(): RetrofitItem {

        val retrofit = Retrofit.Builder()
            .baseUrl(AppSettings.baseUrl)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()

        return retrofit.create(RetrofitItem::class.java)
    }
}