package com.easysystems.easyorder.koin

import com.easysystems.easyorder.adapters.OrderListAdapter
import com.easysystems.easyorder.helpclasses.SharedPreferencesHelper
import com.easysystems.easyorder.helpclasses.StringResourcesProvider
import org.koin.dsl.module

val koinHelpers = module {
    single {
        SharedPreferencesHelper
    }
    single {
        StringResourcesProvider(get())
    }
}