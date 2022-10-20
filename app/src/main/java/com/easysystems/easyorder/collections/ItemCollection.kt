package com.easysystems.easyorder.collections

import android.content.Context
import android.widget.Toast
import androidx.core.view.isVisible
import com.easysystems.easyorder.data.Item
import com.easysystems.easyorder.databinding.ActivityMainBinding
import com.easysystems.easyorder.retrofit.RetrofitItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.lang.Exception

class ItemCollection {

    lateinit var item: Item

    fun getAllItems(context: Context, binding: ActivityMainBinding, callback:(ArrayList<Item>?)->Unit) {

        val retrofitItem = generateRetrofitItem()
        val call: Call<List<Item>> = retrofitItem.getAllItems()

        call.enqueue(object : Callback<List<Item>> {
            override fun onResponse(call: Call<List<Item>>, response: Response<List<Item>>) {

                if (response.isSuccessful) {

                    try {

                        val itemList = response.body() as ArrayList<Item>
                        binding.progressBar.isVisible = false
                        callback(itemList)

                        println("Retrieved items successfully. List size: ${itemList.size}")

                    } catch (ex: Exception) {

                        binding.progressBar.isVisible = false

                        Toast.makeText(
                            context,
                            "Found list is empty!",
                            Toast.LENGTH_LONG
                        ).show()

                        println("Returned list is empty: $ex")
                    }
                } else {

                    binding.progressBar.isVisible = false

                    Toast.makeText(
                        context,
                        "Failed to retrieve list!",
                        Toast.LENGTH_LONG
                    ).show()

                    println("Failed to retrieve list!")
                }
            }

            override fun onFailure(call: Call<List<Item>>, t: Throwable) {

                binding.progressBar.isVisible = false

                Toast.makeText(
                    context,
                    "Failed to retrieve list!",
                    Toast.LENGTH_LONG
                ).show()

                println("Request failed with error: ${t.localizedMessage}")
            }
        })
    }

    fun getItemById(id: Int, context: Context, binding: ActivityMainBinding, callback:(Item?)->Unit) {

        val retrofitItem = generateRetrofitItem()
        val call: Call<Item> = retrofitItem.getItemById(id)

        call.enqueue(object : Callback<Item> {
            override fun onResponse(call: Call<Item>, response: Response<Item>) {

                if (response.isSuccessful) {

                    try {

                        item = response.body() as Item
                        binding.progressBar.isVisible = false
                        callback(item)

                        println("Retrieved item successfully")

                    } catch (ex: Exception) {

                        binding.progressBar.isVisible = false

                        Toast.makeText(
                            context,
                            "Item not found",
                            Toast.LENGTH_LONG
                        ).show()

                        println("Item not found: $ex")
                    }
                } else {

                    binding.progressBar.isVisible = false

                    Toast.makeText(
                        context,
                        "Failed to get item!",
                        Toast.LENGTH_LONG
                    ).show()

                    println("Failed to get item!")
                }
            }

            override fun onFailure(call: Call<Item>, t: Throwable) {

                binding.progressBar.isVisible = false

                Toast.makeText(
                    context,
                    "Failed to get item!",
                    Toast.LENGTH_LONG
                ).show()

                println("Request failed with error: ${t.localizedMessage}")
            }
        })
    }

    private fun generateRetrofitItem(): RetrofitItem {

        val ipAddress = "192.168.178.136"
//        val ipAddress = "localhost"
        val baseURL = "http://$ipAddress:8080/v1/"

        val retrofit = Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()

        return retrofit.create(RetrofitItem::class.java)
    }
}