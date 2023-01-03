package com.easysystems.easyorder.repositories

import android.util.Log
import com.easysystems.easyorder.data.OrderDTO
import com.easysystems.easyorder.retrofit.RetrofitOrder
import org.koin.java.KoinJavaComponent.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrderRepository {

    private val retrofitOrder: RetrofitOrder by inject(RetrofitOrder::class.java)

    fun createOrder(sessionId: Int, callback: (OrderDTO?) -> Unit) {

        val newOrder = OrderDTO(
            status = OrderDTO.Status.OPENED,
            items = ArrayList(),
            sessionId = sessionId,
            total = 0.0
        )
        val call: Call<OrderDTO> = retrofitOrder.createOrder(newOrder)

        call.enqueue(object : Callback<OrderDTO> {
            override fun onResponse(call: Call<OrderDTO>, response: Response<OrderDTO>) {

                if (response.isSuccessful) {

                    try {

                        val orderDTO = response.body() as OrderDTO
                        callback(orderDTO)

                        Log.i("Info", "Order created successfully: $orderDTO")

                    } catch (ex: Exception) {
                        Log.i("Info", "Failed to create order: $ex")
                    }
                } else {
                    Log.i(
                        "Info",
                        "Failed to create order for session $sessionId. Bad response: $response"
                    )
                }
            }

            override fun onFailure(call: Call<OrderDTO>, t: Throwable) {
                Log.i("Info", "Failed to create order. Error:  ${t.localizedMessage}")
            }
        })
    }
}