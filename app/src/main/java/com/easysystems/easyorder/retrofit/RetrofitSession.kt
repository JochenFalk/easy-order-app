package com.easysystems.easyorder.retrofit

import com.easysystems.easyorder.data.Session
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface RetrofitSession {

    @GET("sessions/{id}")
    fun retrieveSessionById(@Path("id") id: Int) : Call<Session>

    @GET("sessions/{tabletopId}/{authCode}")
    fun verifyAndRetrieveSessionByTabletop(
        @Path("tabletopId") tabletopId: Int,
        @Path("authCode") authCode: String) : Call<Session>
}
