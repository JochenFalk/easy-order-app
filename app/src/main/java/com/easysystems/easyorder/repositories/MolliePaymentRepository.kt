package com.easysystems.easyorder.repositories

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.easysystems.easyorder.data.MolliePayment
import com.easysystems.easyorder.data.MolliePaymentDTO
import com.easysystems.easyorder.data.SessionDTO
import com.easysystems.easyorder.databinding.ActivityMainBinding
import com.easysystems.easyorder.helpclasses.AppSettings
import com.easysystems.easyorder.retrofit.RetrofitMolliePayment
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.lang.Exception

class MolliePaymentRepository {

    lateinit var payment: MolliePayment
    lateinit var paymentDTO: MolliePaymentDTO

    fun createPayment(
        session: SessionDTO,
        molliePayment: MolliePayment,
        context: Context,
        binding: ActivityMainBinding,
        callback: (MolliePaymentDTO?) -> Unit
    ) {

        paymentDTO = convertPaymentToPaymentDTO(session, molliePayment)

        val retrofitPayment = generateRetrofitPayment("backend")
        val call: Call<MolliePaymentDTO> = retrofitPayment.createPayment(paymentDTO)

        call.enqueue(object : Callback<MolliePaymentDTO> {
            override fun onResponse(
                call: Call<MolliePaymentDTO>,
                response: Response<MolliePaymentDTO>
            ) {

                if (response.isSuccessful) {

                    try {

                        paymentDTO = response.body() as MolliePaymentDTO
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

                Toast.makeText(
                    context,
                    "Failed to create payment!",
                    Toast.LENGTH_LONG
                ).show()

                Log.i("Info","Request failed with error: ${t.printStackTrace()} ${t.localizedMessage}")
            }
        })
    }

    fun retrievePayment(
        jsonString: String,
        sessionId: Int,
        context: Context,
        binding: ActivityMainBinding,
        callback: (MolliePayment?) -> Unit
    ) {

        val retrofitPayment = generateRetrofitPayment("mollieAPI")
        val body = jsonString.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val mollieHeader = getHeaderMap()

        val call: Call<MolliePayment> =
            retrofitPayment.retrievePaymentFromMollie(mollieHeader, body)

        call.enqueue(object : Callback<MolliePayment> {
            override fun onResponse(call: Call<MolliePayment>, response: Response<MolliePayment>) {

                if (response.isSuccessful) {

                    try {

                        payment = response.body() as MolliePayment
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

                Toast.makeText(
                    context,
                    "Failed to retrieve payment!",
                    Toast.LENGTH_LONG
                ).show()

                Log.i("Info","Request failed with error: ${t.printStackTrace()} ${t.localizedMessage}")
            }
        })
    }

    fun retrievePaymentUpdateById(
        mollieId: String,
        context: Context,
        binding: ActivityMainBinding,
        callback: (MolliePayment?) -> Unit
    ) {

        val retrofitPayment = generateRetrofitPayment("mollieAPI")
        val mollieHeader = getHeaderMap()
        val call: Call<MolliePayment> =
            retrofitPayment.retrievePaymentById(mollieHeader, mollieId)

        call.enqueue(object : Callback<MolliePayment> {
            override fun onResponse(call: Call<MolliePayment>, response: Response<MolliePayment>) {

                if (response.isSuccessful) {

                    try {

                        payment = response.body() as MolliePayment
                        callback(payment)

                        Log.i("Info", "Payment update successfully retrieved from Mollie: $payment")

                    } catch (ex: Exception) {

                        Log.i("Info", "Failed to retrieve payment update: $ex")
                    }
                } else {
                    Log.i(
                        "Info","Failed to retrieve payment update for Mollie id: $mollieId. Bad response: $response"
                    )
                }
            }

            override fun onFailure(call: Call<MolliePayment>, t: Throwable) {

                Toast.makeText(
                    context,
                    "Failed to retrieve payment update!",
                    Toast.LENGTH_LONG
                ).show()

                Log.i("Info","Request failed with error: ${t.printStackTrace()} ${t.localizedMessage}")
            }
        })
    }

    private fun generateRetrofitPayment(environment: String): RetrofitMolliePayment {

        val baseUrl = if (environment == "backend") {
            AppSettings.baseUrl
        } else if (environment == "mollieAPI") {
            AppSettings.mollieURLString
        } else {
            ""
        }

        val mapper = ObjectMapper().apply {
            this.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .build()

        return retrofit.create(RetrofitMolliePayment::class.java)
    }

    private fun getHeaderMap(): Map<String, String> {

        val headerMap = mutableMapOf<String, String>()
        val mollieAuthHeader = AppSettings.mollieAuthHeader
        val mollieToken = AppSettings.mollieToken

        headerMap[mollieAuthHeader] = mollieToken

        return headerMap
    }

    fun convertPaymentToPaymentDTO(session: SessionDTO, payment: MolliePayment): MolliePaymentDTO {

        val molliePaymentId = session.payment?.molliePaymentId
        val amount = HashMap<String, String>().apply {
            this["currency"] = payment.amount.currency
            this["value"] = payment.amount.value
        }
        val checkoutUrl = payment.links?.checkout?.href
        val sessionId = session.id

        return MolliePaymentDTO(
            molliePaymentId,
            amount,
            payment.createdAt,
            payment.description,
            payment.expiresAt,
            payment.id,
            payment.isCancelable,
            payment.mode,
            payment.profileId,
            checkoutUrl,
            payment.redirectUrl,
            payment.webhookUrl,
            payment.resource,
            payment.sequenceType,
            payment.status,
            sessionId
        )
    }
}