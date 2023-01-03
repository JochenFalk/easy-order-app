package com.easysystems.easyorder

import android.app.Application
import com.easysystems.easyorder.koin.koinHelpers
import com.easysystems.easyorder.koin.koinRepos
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(applicationContext)
            modules(
                koinRepos,
                koinHelpers
            )
        }
    }
}