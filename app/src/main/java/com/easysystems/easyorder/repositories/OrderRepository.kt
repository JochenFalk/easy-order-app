package com.easysystems.easyorder.repositories

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.easysystems.easyorder.data.OrderDTO
import com.easysystems.easyorder.helpclasses.Settings
import com.easysystems.easyorder.databinding.ActivityMainBinding
import com.easysystems.easyorder.retrofit.RetrofitOrder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.lang.Exception

class OrderRepository {

    lateinit var orderDTO: OrderDTO

    fun retrieveOrderById(id: Int, context: Context, binding: ActivityMainBinding, callback:(OrderDTO?)->Unit) {

        val retrofitOrder = generateRetrofitOrder()
        val call: Call<OrderDTO> = retrofitOrder.retrieveOrderById(id)

        call.enqueue(object : Callback<OrderDTO> {
            override fun onResponse(call: Call<OrderDTO>, response: Response<OrderDTO>) {

                if (response.isSuccessful) {

                    try {

                        orderDTO = response.body() as OrderDTO
                        callback(orderDTO)

                        Log.i("Info","Retrieved order successfully: $orderDTO")

                    } catch (ex: Exception) {

                        Log.i("Info","Order not found: $ex")
                    }
                } else {
                    Log.i("Info","Failed to get order for id: $id")
                }
            }

            override fun onFailure(call: Call<OrderDTO>, t: Throwable) {

                Toast.makeText(
                    context,
                    "Failed to get order!",
                    Toast.LENGTH_LONG
                ).show()

                Log.i("Info","Request failed with error: ${t.localizedMessage}")
            }
        })
    }

    fun createOrder(sessionId: Int, context: Context, binding: ActivityMainBinding, callback:(OrderDTO?)->Unit) {

        orderDTO = OrderDTO(status = OrderDTO.Status.OPENED, items = ArrayList(), sessionId = sessionId, total = 0.0)

        val retrofitOrder = generateRetrofitOrder()
        val call: Call<OrderDTO> = retrofitOrder.createOrder(orderDTO)

        call.enqueue(object : Callback<OrderDTO> {
            override fun onResponse(call: Call<OrderDTO>, response: Response<OrderDTO>) {

                if (response.isSuccessful) {

                    try {

                        orderDTO = response.body() as OrderDTO
                        callback(orderDTO)

                        Log.i("Info","Order created successfully: $orderDTO")

                    } catch (ex: Exception) {

                        Log.i("Info","Failed to create order: $ex")
                    }
                } else {
                    Log.i("Info","Failed to create order with session id: $sessionId")
                }
            }

            override fun onFailure(call: Call<OrderDTO>, t: Throwable) {

                Toast.makeText(
                    context,
                    "Failed to create order!",
                    Toast.LENGTH_LONG
                ).show()

                Log.i("Info","Request failed with error: ${t.localizedMessage}")
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