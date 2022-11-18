package com.easysystems.easyorder.repositories

import android.content.Context
import android.widget.Toast
import com.easysystems.easyorder.helpclasses.Settings
import com.easysystems.easyorder.data.Item
import com.easysystems.easyorder.databinding.ActivityMainBinding
import com.easysystems.easyorder.retrofit.RetrofitItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.lang.Exception

class ItemRepository {

    lateinit var item: Item

    fun getAllItems(context: Context, binding: ActivityMainBinding, callback:(ArrayList<Item>?)->Unit) {

        val retrofitItem = generateRetrofitItem()
        val call: Call<List<Item>> = retrofitItem.retrieveAllItems()

        call.enqueue(object : Callback<List<Item>> {
            override fun onResponse(call: Call<List<Item>>, response: Response<List<Item>>) {

                if (response.isSuccessful) {

                    try {

                        val itemList = response.body() as ArrayList<Item>
                        callback(itemList)

                        println("Retrieved items successfully. List size: ${itemList.size}")

                    } catch (ex: Exception) {

                        Toast.makeText(
                            context,
                            "Found list is empty!",
                            Toast.LENGTH_LONG
                        ).show()

                        println("Returned list is empty: $ex")
                    }
                } else {

                    Toast.makeText(
                        context,
                        "Failed to retrieve list!",
                        Toast.LENGTH_LONG
                    ).show()

                    println("Failed to retrieve list!")
                }
            }

            override fun onFailure(call: Call<List<Item>>, t: Throwable) {

                Toast.makeText(
                    context,
                    "Failed to retrieve list!",
                    Toast.LENGTH_LONG
                ).show()

                println("Request failed with error: ${t.localizedMessage}")
            }
        })
    }

    private fun generateRetrofitItem(): RetrofitItem {

        val retrofit = Retrofit.Builder()
            .baseUrl(Settings.baseURL)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()

        return retrofit.create(RetrofitItem::class.java)
    }
}