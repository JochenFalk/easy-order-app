package com.easysystems.easyorder.data

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class SessionDTO(
    @JsonProperty("id")
    var id: Int? = null,
    @JsonProperty("status")
    var status: Status? = null,
    @JsonProperty("tabletopDTO")
    var tabletopDTO: TabletopDTO? = null,
    @JsonProperty("total")
    var total: Double? = null,
    @JsonProperty("orders")
    var orders: ArrayList<OrderDTO>? = null,
    @JsonProperty("payment")
    var payment: MolliePaymentDTO? = null
) : Serializable {
    enum class Status {
        OPENED,
        CLOSED,
        LOCKED
    }
}