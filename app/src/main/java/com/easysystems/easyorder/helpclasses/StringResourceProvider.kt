package com.easysystems.easyorder.helpclasses

import android.app.Application

class StringResourcesProvider(private val application: Application) {

    fun getString(stringResId: Int) : String {

        return application.resources.getString(stringResId)
    }

    fun getStringArray(stringArrayResId: Int) : Array<String> {

        return application.resources.getStringArray(stringArrayResId)
    }
}