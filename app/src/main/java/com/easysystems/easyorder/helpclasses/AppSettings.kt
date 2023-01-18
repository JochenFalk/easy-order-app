package com.easysystems.easyorder.helpclasses

import android.util.Log

object AppSettings {

    private const val defaultIpAddress = "192.168.178.136" // "localhost
    private const val defaultBaseURL = "http://$defaultIpAddress:8080/v1/"

    private const val ngRokIpAddress = "https://fa42-2001-1c05-2208-c300-b9a6-42b8-65e3-9bc4.eu.ngrok.io"
    const val ngRokBaseURL = "$ngRokIpAddress/v1/"

    private const val primaryNetworkSSID = "Mediaan"
    private const val secondaryNetworkSSID = "Quattro Gattoni"

    const val mollieURLString = "https://api.mollie.com/v2/"
    const val mollieAuthHeader = "Authorization"
    const val mollieToken = "Bearer test_yGsWtqGQQuKvNs4qtDEtsusscVdDAU"
    const val mollieRedirectUrl = "mollie-app://payment-return"
    const val mollieWebhookMapping = "mollieWebhook"

    var baseUrl = defaultBaseURL
    var isConnectedToWifi = false

    fun setAppConfiguration(ssid: String, callback: (Boolean) -> Unit) {

        if (!isConnectedToWifi) {
            if (ssid == primaryNetworkSSID || ssid == secondaryNetworkSSID) {
                baseUrl = ngRokBaseURL
                isConnectedToWifi = true
            }
            Log.i("Info", "SSID is: $ssid. BaseUrl is set to: $baseUrl")
        }
        callback(isConnectedToWifi)
    }
}