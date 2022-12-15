package com.easysystems.easyorder.repositories

import android.content.Context
import android.util.Log
import com.easysystems.easyorder.data.SessionDTO
import com.easysystems.easyorder.databinding.ActivityMainBinding
import com.easysystems.easyorder.helpclasses.AppSettings
import com.easysystems.easyorder.retrofit.RetrofitSession
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

class SessionRepository {

    lateinit var sessionDTO: SessionDTO

    fun getSessionById(id: Int, context: Context, binding: ActivityMainBinding, callback:(SessionDTO?)->Unit) {

        val retrofitSession = generateRetrofitSession()
        val call: Call<SessionDTO> = retrofitSession.retrieveSessionById(id)

        call.enqueue(object : Callback<SessionDTO> {
            override fun onResponse(call: Call<SessionDTO>, response: Response<SessionDTO>) {

                if (response.isSuccessful) {

                    try {

                        sessionDTO = response.body() as SessionDTO
                        callback(sessionDTO)

                        Log.i("Info","Retrieved session successfully: $sessionDTO")

                    } catch (ex: Exception) {

                        Log.i("Info","Session not found: $ex")
                    }
                } else {
                    Log.i("Info","Failed to get session for id: $id")
                }
            }

            override fun onFailure(call: Call<SessionDTO>, t: Throwable) {

                Log.i("Info","Failed to get session. Error: ${t.localizedMessage}")
            }
        })
    }

    fun updateSession(id: Int, sessionDTO: SessionDTO, context: Context, binding: ActivityMainBinding, callback:(SessionDTO?)->Unit) {

        var updatedSession = sessionDTO

        val retrofitSession = generateRetrofitSession()
        val call: Call<SessionDTO> = retrofitSession.updateSession(id, updatedSession)

        call.enqueue(object : Callback<SessionDTO> {
            override fun onResponse(call: Call<SessionDTO>, response: Response<SessionDTO>) {

                if (response.isSuccessful) {

                    try {

                        updatedSession = response.body() as SessionDTO
                        callback(updatedSession)

                        Log.i("Info","Session updated successfully: $updatedSession")

                    } catch (ex: Exception) {

                        Log.i("Info","Session not found: $ex")
                    }
                } else {
                    Log.i("Info","Failed to update session for id: $id / $sessionDTO")
                }
            }

            override fun onFailure(call: Call<SessionDTO>, t: Throwable) {

                Log.i("Info","Failed to update session. Error: ${t.localizedMessage}")
            }
        })
    }

    fun verifyTabletop(tabletopId: Int, authCode: String, context: Context, binding: ActivityMainBinding, callback: (SessionDTO?)->Unit) {

        val retrofitSession = generateRetrofitSession()
        val call: Call<SessionDTO> = retrofitSession.verifyAndRetrieveSessionByTabletop(tabletopId, authCode)

        call.enqueue(object : Callback<SessionDTO> {
            override fun onResponse(call: Call<SessionDTO>, response: Response<SessionDTO>) {

                if (response.isSuccessful) {

                    try {

                        sessionDTO = response.body() as SessionDTO
                        callback(sessionDTO)

                        Log.i("Info","Retrieved session successfully: $sessionDTO")

                    } catch (ex: Exception) {

                        Log.i("Info","Session not found: $ex")
                    }
                } else {
                    Log.i("Info","Failed to verify session for table id: $tabletopId")
                }
            }

            override fun onFailure(call: Call<SessionDTO>, t: Throwable) {

                Log.i("Info","Failed to verify session. Error: ${t.localizedMessage}")
            }
        })
    }

    private fun generateRetrofitSession(): RetrofitSession {

        val retrofit = Retrofit.Builder()
            .baseUrl(AppSettings.baseUrl)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()

        return retrofit.create(RetrofitSession::class.java)
    }
}