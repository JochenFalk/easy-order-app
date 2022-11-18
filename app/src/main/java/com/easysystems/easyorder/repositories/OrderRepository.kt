package com.easysystems.easyorder.repositories

import android.content.Context
import android.widget.Toast
import com.easysystems.easyorder.data.Item
import com.easysystems.easyorder.data.Order
import com.easysystems.easyorder.helpclasses.Settings
import com.easysystems.easyorder.data.Session
import com.easysystems.easyorder.databinding.ActivityMainBinding
import com.easysystems.easyorder.retrofit.RetrofitOrder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.lang.Exception

class OrderRepository {

    lateinit var order: Order

    fun retrieveOrderById(id: Int, context: Context, binding: ActivityMainBinding, callback:(Order?)->Unit) {

        val retrofitOrder = generateRetrofitOrder()
        val call: Call<Order> = retrofitOrder.retrieveOrderById(id)

        call.enqueue(object : Callback<Order> {
            override fun onResponse(call: Call<Order>, response: Response<Order>) {

                if (response.isSuccessful) {

                    try {

                        order = response.body() as Order
                        callback(order)

                        println("Retrieved order successfully: $order")

                    } catch (ex: Exception) {

                        Toast.makeText(
                            context,
                            "Order not found",
                            Toast.LENGTH_LONG
                        ).show()

                        println("Order not found: $ex")
                    }
                } else {
                    println("Failed to get order for id: $id")
                }
            }

            override fun onFailure(call: Call<Order>, t: Throwable) {

                Toast.makeText(
                    context,
                    "Failed to get order!",
                    Toast.LENGTH_LONG
                ).show()

                println("Request failed with error: ${t.localizedMessage}")
            }
        })
    }

    fun createOrder(sessionId: Int, context: Context, binding: ActivityMainBinding, callback:(Order?)->Unit) {

        order = Order(status = "OPENED", items = ArrayList(), sessionId = sessionId, total = 0.0)

        val retrofitOrder = generateRetrofitOrder()
        val call: Call<Order> = retrofitOrder.createOrder(order)

        call.enqueue(object : Callback<Order> {
            override fun onResponse(call: Call<Order>, response: Response<Order>) {

                if (response.isSuccessful) {

                    try {

                        order = response.body() as Order
                        callback(order)

                        println("Order created successfully: $order")

                    } catch (ex: Exception) {

                        println("Failed to create order: $ex")
                    }
                } else {
                    println("Failed to create order with session id: $sessionId")
                }
            }

            override fun onFailure(call: Call<Order>, t: Throwable) {

                Toast.makeText(
                    context,
                    "Failed to create order!",
                    Toast.LENGTH_LONG
                ).show()

                println("Request failed with error: ${t.localizedMessage}")
            }
        })
    }

    private fun generateRetrofitOrder(): RetrofitOrder {

        val retrofit = Retrofit.Builder()
            .baseUrl(Settings.baseURL)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()

        return retrofit.create(RetrofitOrder::class.java)
    }
}