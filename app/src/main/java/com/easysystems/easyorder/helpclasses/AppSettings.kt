package com.easysystems.easyorder.helpclasses

import android.util.Log

object AppSettings {

    private const val defaultIpAddress = "192.168.178.136" // "localhost
    const val defaultBaseURL = "http://$defaultIpAddress:8080/v1/"

    private const val ngRokIpAddress =
        "https://afc9-2001-1c05-2208-c300-d822-ad4f-1841-ae6.eu.ngrok.io"
    const val ngRokBaseURL = "$ngRokIpAddress/v1/"

    const val mollieURLString = "https://api.mollie.com/v2/"
    const val mollieAuthHeader = "Authorization"
    const val mollieToken = "Bearer test_yGsWtqGQQuKvNs4qtDEtsusscVdDAU"
    const val mollieRedirectUrl = "mollie-app://payment-return"
    const val mollieWebhookMapping = "mollieWebhook"

    var baseUrl = defaultBaseURL

    fun setAppConfiguration(ssid: String) {

        if (ssid == "Mediaan") {
            baseUrl = ngRokBaseURL
        }

        Log.i("Info", "SSID is: $ssid. BaseUrl is set to: $baseUrl")
    }
}