package com.easysystems.easyorder.repositories

import android.util.Log
import com.easysystems.easyorder.data.SessionDTO
import com.easysystems.easyorder.retrofit.RetrofitSession
import org.koin.java.KoinJavaComponent.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SessionRepository {

    private val retrofitSession : RetrofitSession by inject(RetrofitSession::class.java)

    fun verifyTabletop(tabletopId: Int, authCode: String, callback: (SessionDTO?)->Unit) {

        val call: Call<SessionDTO> = retrofitSession.verifyAndRetrieveSessionByTabletop(tabletopId, authCode)

        call.enqueue(object : Callback<SessionDTO> {
            override fun onResponse(call: Call<SessionDTO>, response: Response<SessionDTO>) {

                if (response.isSuccessful) {

                    try {

                        val sessionDTO = response.body() as SessionDTO
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

    fun updateSession(sessionToUpdate: SessionDTO, callback: (SessionDTO?) -> Unit) {

        sessionToUpdate.id?.let { retrofitSession.updateSession(it, sessionToUpdate) }
            ?.enqueue(object : Callback<SessionDTO> {
                override fun onResponse(call: Call<SessionDTO>, response: Response<SessionDTO>) {

                    if (response.isSuccessful) {

                        try {

                            val sessionDTO = response.body() as SessionDTO
                            callback(sessionDTO)

                            Log.i("Info", "Session updated successfully: $sessionDTO")

                        } catch (ex: Exception) {
                            Log.i("Info", "Session not found: $ex")
                        }
                    } else {
                        Log.i("Info", "Failed to update session for id: $sessionToUpdate.id / $sessionToUpdate")
                    }
                }

                override fun onFailure(call: Call<SessionDTO>, t: Throwable) {
                    Log.i("Info", "Failed to update session. Error: ${t.localizedMessage}")
                }
            })
    }
}