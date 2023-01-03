package com.easysystems.easyorder.repositories

import android.util.Log
import com.easysystems.easyorder.data.MolliePayment
import com.easysystems.easyorder.data.MolliePaymentDTO
import com.easysystems.easyorder.data.SessionDTO
import com.easysystems.easyorder.helpclasses.AppSettings
import com.easysystems.easyorder.retrofit.RetrofitBackendPayment
import com.easysystems.easyorder.retrofit.RetrofitMolliePayment
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentRepository {

    private val retrofitBackendPayment: RetrofitBackendPayment by KoinJavaComponent.inject(
        RetrofitBackendPayment::class.java,
        named("backend")
    )
    private val retrofitMolliePayment: RetrofitMolliePayment by KoinJavaComponent.inject(
        RetrofitMolliePayment::class.java,
        named("mollie")
    )

    fun createPaymentOnBackend(
        session: SessionDTO,
        molliePaymentDTO: MolliePaymentDTO,
        callback: (MolliePaymentDTO?) -> Unit
    ) {

        val call: Call<MolliePaymentDTO> =
            retrofitBackendPayment.createPaymentOnBackend(molliePaymentDTO)

        call.enqueue(object : Callback<MolliePaymentDTO> {
            override fun onResponse(
                call: Call<MolliePaymentDTO>,
                response: Response<MolliePaymentDTO>
            ) {

                if (response.isSuccessful) {

                    try {

                        val paymentDTO = response.body() as MolliePaymentDTO
                        callback(paymentDTO)

                        Log.i("Info", "Payment created successfully on backend: $paymentDTO")

                    } catch (ex: Exception) {
                        Log.i("Info", "Failed to create payment: $ex")
                    }
                } else {
                    Log.i(
                        "Info",
                        "Failed to create payment with session id: ${session.id}. Bad response: $response"
                    )
                }
            }

            override fun onFailure(call: Call<MolliePaymentDTO>, t: Throwable) {
                Log.i(
                    "Info", "Failed to create payment. Error: ${t.localizedMessage}"
                )
            }
        })
    }

    fun updatePaymentToBackend(
        molliePaymentDTO: MolliePaymentDTO,
        callback: (MolliePaymentDTO?) -> Unit
    ) {

        molliePaymentDTO.molliePaymentId?.let {
            retrofitBackendPayment.updatePaymentToBackend(
                it,
                molliePaymentDTO
            )
        }
            ?.enqueue(object : Callback<MolliePaymentDTO> {
                override fun onResponse(
                    call: Call<MolliePaymentDTO>,
                    response: Response<MolliePaymentDTO>
                ) {

                    if (response.isSuccessful) {

                        try {

                            val paymentDTO = response.body() as MolliePaymentDTO
                            callback(paymentDTO)

                            Log.i("Info", "Mollie payment updated successfully: $paymentDTO")

                        } catch (ex: Exception) {

                            Log.i("Info", "Mollie payment not found: $ex")
                        }
                    } else {
                        Log.i(
                            "Info",
                            "Failed to update Mollie payment for id: ${molliePaymentDTO.molliePaymentId} / $response"
                        )
                    }
                }

                override fun onFailure(call: Call<MolliePaymentDTO>, t: Throwable) {
                    Log.i("Info", "Failed to update Mollie payment. Error: ${t.localizedMessage}")
                }
            })
    }

    fun retrievePaymentFromMollie(
        jsonString: String,
        sessionId: Int,
        callback: (MolliePayment?) -> Unit
    ) {

        val body = jsonString.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val mollieHeader = getHeaderMap()

        val call: Call<MolliePayment> =
            retrofitMolliePayment.retrievePaymentFromMollie(mollieHeader, body)

        call.enqueue(object : Callback<MolliePayment> {
            override fun onResponse(call: Call<MolliePayment>, response: Response<MolliePayment>) {

                if (response.isSuccessful) {

                    try {

                        val payment = response.body() as MolliePayment
                        callback(payment)

                        Log.i("Info", "Payment successfully retrieved from Mollie: $payment++")

                    } catch (ex: Exception) {
                        Log.i("Info", "Failed to retrieve payment: $ex")
                    }
                } else {
                    Log.i(
                        "Info",
                        "Failed to retrieve payment for session id: $sessionId. Bad response: $response"
                    )
                }
            }

            override fun onFailure(call: Call<MolliePayment>, t: Throwable) {
                Log.i(
                    "Info", "Request failed with error: ${t.localizedMessage}"
                )
            }
        })
    }

    fun retrievePaymentUpdateById(
        mollieId: String,
        callback: (MolliePayment?) -> Unit
    ) {

        val mollieHeader = getHeaderMap()
        val call: Call<MolliePayment> =
            retrofitMolliePayment.retrievePaymentById(mollieHeader, mollieId)

        call.enqueue(object : Callback<MolliePayment> {
            override fun onResponse(call: Call<MolliePayment>, response: Response<MolliePayment>) {

                if (response.isSuccessful) {

                    try {

                        val payment = response.body() as MolliePayment
                        callback(payment)

                        Log.i("Info", "Payment update successfully retrieved from Mollie: $payment")

                    } catch (ex: Exception) {
                        Log.i("Info", "Failed to retrieve payment update: $ex")
                    }
                } else {
                    Log.i(
                        "Info",
                        "Failed to retrieve payment update for Mollie id: $mollieId. Bad response: $response"
                    )
                }
            }

            override fun onFailure(call: Call<MolliePayment>, t: Throwable) {
                Log.i(
                    "Info", "Failed to retrieve payment update. Error: ${t.localizedMessage}"
                )
            }
        })
    }

    private fun getHeaderMap(): Map<String, String> {

        val headerMap = mutableMapOf<String, String>()
        val mollieAuthHeader = AppSettings.mollieAuthHeader
        val mollieToken = AppSettings.mollieToken

        headerMap[mollieAuthHeader] = mollieToken

        return headerMap
    }
}