package com.easysystems.easyorder

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.easysystems.easyorder.activities.MySplashActivity
import com.easysystems.easyorder.data.MolliePaymentDTO
import com.easysystems.easyorder.data.SessionDTO
import com.easysystems.easyorder.databinding.ActivityMainBinding
import com.easysystems.easyorder.fragments.ItemListFragment
import com.easysystems.easyorder.fragments.OrderListFragment
import com.easysystems.easyorder.fragments.PaymentFragment
import com.easysystems.easyorder.helpclasses.AppSettings
import com.easysystems.easyorder.helpclasses.SharedPreferencesHelper
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val sharedPreferencesHelper: SharedPreferencesHelper by inject()

    companion object {
        var sessionDTO: SessionDTO? = SessionDTO()
        var paymentDTO: MolliePaymentDTO? = null
        var paymentMethod = ""
        var isAppLoaded = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                supportFragmentManager.popBackStack()
            }
        })

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                return@addOnBackStackChangedListener
            }

            when (supportFragmentManager.fragments.last()) {
                is ItemListFragment -> {
                    supportActionBar?.title = "Menu"
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                }
                is OrderListFragment -> {
                    supportActionBar?.title = "Orders"
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                }
                is PaymentFragment -> {
                    supportActionBar?.title = "Payment"
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                supportFragmentManager.popBackStack()
                return true
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkWifiConnection()
        }
    }

    override fun onPause() {
        super.onPause()

        sessionDTO?.updateSession { sessionDTO ->

            if (sessionDTO != null) {

                sharedPreferencesHelper
                    .savePreferences(
                        this,
                        "sessionData",
                        "sessionId",
                        intValue = sessionDTO.id
                    )
                sharedPreferencesHelper
                    .savePreferences(
                        this,
                        "sessionData",
                        "sessionStatus",
                        stringValue = sessionDTO.status.toString()
                    )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (!isAppLoaded) {
            checkWifiConnection()
        } else {

            sharedPreferencesHelper
                .retrievePreferences(
                    this,
                    "sessionData",
                    "sessionId",
                    "INT"
                ).let { sessionId ->

                    if (sessionId as Int == 0) {
                        callSplashActivity()
                    } else {
                        handleSessionState()
                    }
                }
        }
    }

    private fun callSplashActivity() {
        val intent = Intent(this@MainActivity, MySplashActivity::class.java)
        startActivity(intent)
    }

    private fun callItemListFragment() {

        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val itemListFragment = ItemListFragment()

        fragmentTransaction.replace(R.id.frame, itemListFragment).addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun checkWifiConnection() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
        } else {
            if (!isAppLoaded) {

                val wifiManager =
                    applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo: WifiInfo = wifiManager.connectionInfo

                if (wifiInfo.supplicantState == SupplicantState.COMPLETED) {

                    val ssid = wifiInfo.ssid.toString().substring(1, wifiInfo.ssid.length - 1)
                    AppSettings.setAppConfiguration(ssid) { boolean ->

                        isAppLoaded = boolean
                        callSplashActivity()
                    }
                }
            }
        }
    }

    private fun handleSessionState() {

        when (sessionDTO?.status) {
            SessionDTO.Status.OPENED -> {
                callItemListFragment()
                Log.i("Info", "Session status is opened")
            }
            SessionDTO.Status.CLOSED -> {
                sharedPreferencesHelper.clearPreferences(this, "sessionData")
                callSplashActivity()
                Log.i("Info", "Session status is closed")
            }
            SessionDTO.Status.LOCKED -> {
                val savedStatus = sharedPreferencesHelper.retrievePreferences(
                    this,
                    "sessionData",
                    "sessionStatus",
                    "STRING"
                )
                if (savedStatus == "LOCKED") {
                    sessionDTO?.resetPaymentStatus { resetSession ->

                        resetSession?.updateSession {
                            callItemListFragment()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "This session is locked because another person is performing a payment.",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    callSplashActivity()
                    Log.i("Info", "Session status is locked")
                }
            }
            SessionDTO.Status.CHANGED -> {
                handlePaymentStateChange()
                Log.i("Info", "Session status is changed")
            }
            else -> {
                callSplashActivity()
            }
        }
    }

    private fun handlePaymentStateChange() {

        paymentDTO?.updatePaymentFromMollie { updatedPayment ->

            updatedPayment?.sessionId = sessionDTO?.id
            updatedPayment?.molliePaymentId = paymentDTO?.molliePaymentId
            updatedPayment?.updatePaymentToBackend { molliePaymentDTO ->

                paymentDTO = molliePaymentDTO
                sessionDTO?.payments?.last {
                    sessionDTO?.payments?.remove(it)!!
                    sessionDTO?.payments?.add(paymentDTO!!)!!
                }
                sessionDTO?.updateSession { sessionDTO ->

                    if (sessionDTO != null) {

                        when (paymentDTO?.status?.uppercase()) {
                            "PAID" -> {
                                Log.i("Info", "Payment status is paid")
                                sessionDTO.closeSession { closedSession ->

                                    closedSession?.updateSession {
                                        sharedPreferencesHelper.clearPreferences(
                                            this,
                                            "sessionData"
                                        )
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Payment received :). Thank you for using EasyOrder services.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        callSplashActivity()
                                    }
                                }
                            }
                            "OPEN" -> {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Payment failed for unknown reasons. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.i("Info", "Payment status is open")
                                paymentDTO?.status = "failed"
                                sessionDTO.resetPaymentStatus { resetSession ->

                                    resetSession?.payments = ArrayList()
                                    resetSession?.updateSession {
                                        callItemListFragment()
                                    }
                                }
                            }
                            "EXPIRED" -> {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Payment was expired. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.i("Info", "Payment status is expired")
                                sessionDTO.resetPaymentStatus { resetSession ->

                                    resetSession?.updateSession {
                                        callItemListFragment()
                                    }
                                }
                            }
                            "FAILED" -> {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Payment failed :( Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.i("Info", "Payment status is failed")
                                sessionDTO.resetPaymentStatus { resetSession ->

                                    resetSession?.updateSession {
                                        callItemListFragment()
                                    }
                                }
                            }
                            "CANCELED" -> {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Payment was canceled. Please try another payment method.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.i("Info", "Payment status is canceled")
                                sessionDTO.resetPaymentStatus { resetSession ->

                                    resetSession?.updateSession {
                                        callItemListFragment()
                                    }
                                }
                            }
                        }
                    }
                    Log.i("Info", "Session status is changed")
                }
            }
        }
    }
}