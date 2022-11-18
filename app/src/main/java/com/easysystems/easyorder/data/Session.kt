package com.easysystems.easyorder.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class Session(
    @JsonProperty("id")
    var id: Int = 0,
    @JsonProperty("status")
    var status: String? = null,
    @JsonProperty("total")
    var total: Double? = null,
    @JsonProperty("orders")
    var orders: ArrayList<Order>? = null
) : Serializable