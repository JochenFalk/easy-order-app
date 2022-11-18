package com.easysystems.easyorder.data

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class Order(
    @JsonProperty("id")
    val id: Int? = null,
    @JsonProperty("status")
    var status: String? = null,
    @JsonProperty("items")
    var items: ArrayList<Item>? = null,
    @JsonProperty("total")
    var total: Double? = null,
    @JsonProperty("sessionId")
    val sessionId: Int? = null
) : Serializable