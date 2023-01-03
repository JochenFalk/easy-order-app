package com.easysystems.easyorder.koin

import com.easysystems.easyorder.helpclasses.SharedPreferencesHelper
import org.koin.dsl.module

val koinHelpers = module {
    single {
        SharedPreferencesHelper
    }
}