package com.easysystems.easyorder.data

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class TabletopDTO(
    @JsonProperty("id")
    val id: Int? = null,
    @JsonProperty("authCode")
    val authCode: String? = null
) : Serializable