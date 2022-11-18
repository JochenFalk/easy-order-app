package com.easysystems.easyorder.repositories

import android.content.Context
import android.widget.Toast
import com.easysystems.easyorder.helpclasses.Settings
import com.easysystems.easyorder.data.Session
import com.easysystems.easyorder.databinding.ActivityMainBinding
import com.easysystems.easyorder.retrofit.RetrofitSession
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.lang.Exception

class SessionRepository {

    lateinit var session: Session

    fun getSessionById(id: Int, context: Context, binding: ActivityMainBinding, callback:(Session?)->Unit) {

        val retrofitSession = generateRetrofitSession()
        val call: Call<Session> = retrofitSession.retrieveSessionById(id)

        call.enqueue(object : Callback<Session> {
            override fun onResponse(call: Call<Session>, response: Response<Session>) {

                if (response.isSuccessful) {

                    try {

                        session = response.body() as Session
                        callback(session)

                        println("Retrieved session successfully: $session")

                    } catch (ex: Exception) {

                        Toast.makeText(
                            context,
                            "Session not found",
                            Toast.LENGTH_LONG
                        ).show()

                        println("Session not found: $ex")
                    }
                } else {
                    println("Failed to get session for id: $id")
                }
            }

            override fun onFailure(call: Call<Session>, t: Throwable) {

                Toast.makeText(
                    context,
                    "Failed to get session!",
                    Toast.LENGTH_LONG
                ).show()

                println("Request failed with error: ${t.localizedMessage}")
            }
        })
    }

    fun verifyTabletop(tabletopId: Int, authCode: String, context: Context, binding: ActivityMainBinding, callback:(Session?)->Unit) {

        val retrofitSession = generateRetrofitSession()
        val call: Call<Session> = retrofitSession.verifyAndRetrieveSessionByTabletop(tabletopId, authCode)

        call.enqueue(object : Callback<Session> {
            override fun onResponse(call: Call<Session>, response: Response<Session>) {

                if (response.isSuccessful) {

                    try {

                        session = response.body() as Session
                        callback(session)

                        println("Retrieved session successfully: $session")

                    } catch (ex: Exception) {

                        Toast.makeText(
                            context,
                            "Session not found",
                            Toast.LENGTH_LONG
                        ).show()

                        println("Session not found: $ex")
                    }
                } else {
                    println("Failed to verify session for table id: $tabletopId")
                }
            }

            override fun onFailure(call: Call<Session>, t: Throwable) {

                Toast.makeText(
                    context,
                    "Failed to get session!",
                    Toast.LENGTH_LONG
                ).show()

                println("Request failed with error: ${t.localizedMessage}")
            }
        })
    }

    private fun generateRetrofitSession(): RetrofitSession {

        val retrofit = Retrofit.Builder()
            .baseUrl(Settings.baseURL)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()

        return retrofit.create(RetrofitSession::class.java)
    }
}