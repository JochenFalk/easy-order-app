package com.easysystems.easyorder.data

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class ItemDTO(
    @JsonProperty("id")
    val id: Int? = null,
    @JsonProperty("name")
    val name: String? = null,
    @JsonProperty("category")
    val category: Category? = null,
    @JsonProperty("price")
    val price: Double? = null
) : Serializable {
    enum class Category {
        APPETIZER,
        MAIN,
        DESERT,
        DRINKS
    }
}