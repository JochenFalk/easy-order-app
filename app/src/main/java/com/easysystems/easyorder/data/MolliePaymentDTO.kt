package com.easysystems.easyorder.data

import com.fasterxml.jackson.annotation.JsonProperty

data class MolliePaymentDTO(
    @JsonProperty("molliePaymentId")
    val molliePaymentId: Int?,
    @JsonProperty("amount")
    val amount: HashMap<String, String>? = HashMap(),
    @JsonProperty("createdAt")
    val createdAt: String? = null,
    @JsonProperty("description")
    val description: String? = null,
    @JsonProperty("expiresAt")
    val expiresAt: String? = null,
    @JsonProperty("id")
    val id: String? = null,
    @JsonProperty("isCancelable")
    val isCancelable: Boolean? = null,
    @JsonProperty("mode")
    val mode: String? = null,
    @JsonProperty("profileId")
    val profileId: String? = null,
    @JsonProperty("checkoutUrl")
    val checkoutUrl: String? = null,
    @JsonProperty("redirectUrl")
    val redirectUrl: String? = null,
    @JsonProperty("webhookUrl")
    val webhookUrl: String? = null,
    @JsonProperty("resource")
    val resource: String? = null,
    @JsonProperty("sequenceType")
    val sequenceType: String? = null,
    @JsonProperty("status")
    val status: String? = null,
    @JsonProperty("sessionId")
    val sessionId: Int? = null
)