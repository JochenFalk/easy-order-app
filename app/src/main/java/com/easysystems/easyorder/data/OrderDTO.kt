package com.easysystems.easyorder.data

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class OrderDTO(
    @JsonProperty("id")
    val id: Int? = null,
    @JsonProperty("status")
    var status: Status? = null,
    @JsonProperty("items")
    var items: ArrayList<ItemDTO>? = null,
    @JsonProperty("total")
    var total: Double? = null,
    @JsonProperty("sessionId")
    val sessionId: Int? = null
) : Serializable {
    enum class Status {
        OPENED,
        SENT,
        LOCKED
    }
}