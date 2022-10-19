package com.easysystems.easyorder

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class Item(
    @JsonProperty("id")
    val itemId: Int? = null,
    @JsonProperty("name")
    val name: String? = null,
    @JsonProperty("category")
    val itemCategory: String? = null,
    @JsonProperty("price")
    val price: Float? = null
) : Serializable