package com.easysystems.easyorder.koin

import com.easysystems.easyorder.helpclasses.AppSettings
import com.easysystems.easyorder.repositories.ItemRepository
import com.easysystems.easyorder.repositories.PaymentRepository
import com.easysystems.easyorder.repositories.OrderRepository
import com.easysystems.easyorder.repositories.SessionRepository
import com.easysystems.easyorder.retrofit.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

val koinRepos = module {
    single(named("backend")) {
        Retrofit.Builder()
            .baseUrl(AppSettings.baseUrl)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()
            .create(RetrofitBackendPayment::class.java)
    }
    single(named("mollie")) {

        val mapper = ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

        Retrofit.Builder()
            .baseUrl(AppSettings.mollieURLString)
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .build()
            .create(RetrofitMolliePayment::class.java)
    }
    single {
        Retrofit.Builder()
            .baseUrl(AppSettings.baseUrl)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()
            .create(RetrofitSession::class.java)
    }
    single {
        Retrofit.Builder()
            .baseUrl(AppSettings.baseUrl)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()
            .create(RetrofitOrder::class.java)
    }
    single {
        Retrofit.Builder()
            .baseUrl(AppSettings.baseUrl)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()
            .create(RetrofitItem::class.java)
    }
    single {
        PaymentRepository()
    }
    single {
        SessionRepository()
    }
    single {
        OrderRepository()
    }
    single {
        ItemRepository()
    }
}