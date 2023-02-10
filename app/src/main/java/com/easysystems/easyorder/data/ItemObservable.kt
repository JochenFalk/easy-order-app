package com.easysystems.easyorder.data

import android.graphics.drawable.Drawable
import java.io.FileDescriptor
import java.io.Serializable

data class ItemObservable(
    var id: Int? = null,
    var image: Drawable? = null,
    var name: String? = null,
    var price: String? = null,
    var category: String? = null,
    var description: String? = null
) : Serializable