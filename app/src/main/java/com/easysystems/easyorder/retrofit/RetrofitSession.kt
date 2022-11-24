package com.easysystems.easyorder.retrofit

import com.easysystems.easyorder.data.SessionDTO
import retrofit2.Call
import retrofit2.http.*

interface RetrofitSession {

    @GET("sessions/{id}")
    fun retrieveSessionById(@Path("id") id: Int) : Call<SessionDTO>

    @GET("sessions/{tabletopId}/{authCode}")
    fun verifyAndRetrieveSessionByTabletop(
        @Path("tabletopId") tabletopId: Int,
        @Path("authCode") authCode: String) : Call<SessionDTO>

    @PUT("sessions/{id}")
    fun updateSession(@Path("id") id: Int, @Body sessionDTO: SessionDTO) : Call<SessionDTO>
}
