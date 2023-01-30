package com.easysystems.easyorder.data

import android.graphics.drawable.Drawable
import com.easysystems.easyorder.R
import com.easysystems.easyorder.helpclasses.StringResourcesProvider
import org.koin.java.KoinJavaComponent

data class ItemObservable(
    var id: Int? = null,
    var name: String? = null,
    var image: Drawable? = null,
    var category: String? = null,
    var price: String? = null
) {

    private val stringResourcesProvider: StringResourcesProvider by KoinJavaComponent.inject(
        StringResourcesProvider::class.java
    )

    init {
        image = stringResourcesProvider.getDrawable(R.drawable.ic_launcher_foreground)
    }
}