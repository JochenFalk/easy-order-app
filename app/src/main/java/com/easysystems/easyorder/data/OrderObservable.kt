package com.easysystems.easyorder.data

data class OrderObservable(
    var id: Int? = null,
    var status: String? = null,
    var title: String? = null,
    var items: ArrayList<ItemObservable> = ArrayList(),
    var total: String? = null,
    var sessionId: Int? = null
)