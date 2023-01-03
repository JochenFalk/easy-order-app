package com.easysystems.easyorder.data

import com.fasterxml.jackson.annotation.JsonProperty

// This class is derived from Mollie payment response body with jsonToKotlinClass plugin
// -> https://plugins.jetbrains.com/plugin/9960-json-to-kotlin-class-jsontokotlinclass-/

data class MolliePayment(
    @JsonProperty("molliePaymentId")
    val molliePaymentId: Int? = 0,
    @JsonProperty("amount")
    val amount: Amount,
    @JsonProperty("createdAt")
    val createdAt: String,
    @JsonProperty("description")
    val description: String,
    @JsonProperty("expiresAt")
    val expiresAt: String?,
    @JsonProperty("id")
    val id: String,
    @JsonProperty("isCancelable")
    val isCancelable: Boolean,
    @JsonProperty("_links")
    val links: Links?,
    @JsonProperty("metadata")
    val metadata: Any?,
    @JsonProperty("method")
    val method: Any?,
    @JsonProperty("mode")
    val mode: String,
    @JsonProperty("profileId")
    val profileId: String,
    @JsonProperty("redirectUrl")
    val redirectUrl: String,
    @JsonProperty("webhookUrl")
    val webhookUrl: String,
    @JsonProperty("resource")
    val resource: String,
    @JsonProperty("sequenceType")
    val sequenceType: String,
    @JsonProperty("status")
    val status: String,
    @JsonProperty("sessionId")
    var sessionId: Int
) {

    fun convertPaymentToPaymentDTO(): MolliePaymentDTO {

        val payment = this
        val amount = HashMap<String, String>().apply {
            this["currency"] = payment.amount.currency
            this["value"] = payment.amount.value
        }

        return MolliePaymentDTO(
            molliePaymentId = payment.molliePaymentId,
            amount = amount,
            createdAt = payment.createdAt,
            description = payment.description,
            expiresAt = payment.expiresAt,
            id = payment.id,
            isCancelable = payment.isCancelable,
            method = payment.method.toString(),
            mode = payment.mode,
            profileId = payment.profileId,
            checkoutUrl = payment.links?.checkout?.href,
            redirectUrl = payment.redirectUrl,
            webhookUrl = payment.webhookUrl,
            resource = payment.resource,
            sequenceType = payment.sequenceType,
            status = payment.status,
            sessionId = payment.sessionId
        )
    }
}

data class Amount(
    @JsonProperty("currency")
    val currency: String,
    @JsonProperty("value")
    val value: String
)

data class Links(
    @JsonProperty("checkout")
    val checkout: Checkout?,
    @JsonProperty("dashboard")
    val dashboard: Dashboard?,
    @JsonProperty("documentation")
    val documentation: Documentation?,
    @JsonProperty("self")
    val self: Self?
)

data class Checkout(
    @JsonProperty("href")
    val href: String,
    @JsonProperty("type")
    val type: String
)

data class Dashboard(
    @JsonProperty("href")
    val href: String,
    @JsonProperty("type")
    val type: String
)

data class Documentation(
    @JsonProperty("href")
    val href: String,
    @JsonProperty("type")
    val type: String
)

data class Self(
    @JsonProperty("href")
    val href: String,
    @JsonProperty("type")
    val type: String
)