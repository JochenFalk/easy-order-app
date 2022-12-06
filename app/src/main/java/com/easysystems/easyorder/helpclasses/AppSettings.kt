package com.easysystems.easyorder.helpclasses

import android.util.Log
import com.easysystems.easyorder.MainActivity

object AppSettings {

    private const val defaultIpAddress = "192.168.178.136" // "localhost
    const val defaultBaseURL = "http://$defaultIpAddress:8080/v1/"

    private const val ngRokIpAddress =
        "https://4238-83-232-94-162.eu.ngrok.io"
    const val ngRokBaseURL = "$ngRokIpAddress/v1/"

    const val mollieURLString = "https://api.mollie.com/v2/"
    const val mollieAuthHeader = "Authorization"
    const val mollieToken = "Bearer test_yGsWtqGQQuKvNs4qtDEtsusscVdDAU"
    const val mollieRedirectUrl = "mollie-app://payment-return"
    const val mollieWebhookMapping = "molliePayments/"

    var baseUrl = defaultBaseURL

    fun setAppConfiguration(ssid: String) {

//        if (ssid == """Mediaan""") {
//            baseUrl = ngRokBaseURL
//        }
        baseUrl = ngRokBaseURL

        Log.i("Info", "BaseUrl is set to: $baseUrl. SSIS is: $ssid")
    }
}