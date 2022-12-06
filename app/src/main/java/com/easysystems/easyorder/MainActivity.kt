package com.easysystems.easyorder

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.easysystems.easyorder.data.ItemDTO
import com.easysystems.easyorder.data.OrderDTO
import com.easysystems.easyorder.data.SessionDTO
import com.easysystems.easyorder.databinding.ActivityMainBinding
import com.easysystems.easyorder.helpclasses.AppSettings
import com.easysystems.easyorder.helpclasses.SharedPreferencesHelper
import com.easysystems.easyorder.repositories.ItemRepository
import com.easysystems.easyorder.repositories.MolliePaymentRepository
import com.easysystems.easyorder.repositories.OrderRepository
import com.easysystems.easyorder.repositories.SessionRepository
import java.io.Serializable
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var elementState = ElementState.WELCOME
    private var itemRepository = ItemRepository()
    private var orderRepository = OrderRepository()
    private var sessionRepository = SessionRepository()
    private var molliePaymentRepository = MolliePaymentRepository()
    private val sharedPreferencesHelper = SharedPreferencesHelper

    private lateinit var binding: ActivityMainBinding

    companion object {
        var sessionDTO = SessionDTO()
        var menuItems = ArrayList<ItemDTO>()
        const val RESULT = "RESULT"
    }

    enum class ElementState {
        WELCOME,
        MENU,
        ORDERS,
        PAYMENT
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnScan.setOnClickListener {
            val intent = Intent(applicationContext, ScannerActivity::class.java)
            startActivity(intent)
        }

        binding.btnOrders.setOnClickListener {
            callOrderListFragment(sessionDTO)
        }

        binding.btnCheckout.setOnClickListener {
            callPaymentFragment(sessionDTO)
        }

        binding.btnCloseSession.setOnClickListener {
            createMolliePayment(sessionDTO)
        }
    }

    private fun getSession(id: Int) {

        sharedPreferencesHelper.clearPreferences(this@MainActivity, "sessionData")

        sessionRepository.getSessionById(id, this@MainActivity, binding) { session ->
            if (session != null) {
                session.orders?.sortBy { it.id }
                sessionDTO = session

                sharedPreferencesHelper
                    .savePreferences(this, "sessionData", "sessionId", intValue = session.id)

                loadSessionInfo()
            }
        }
    }

    private fun loadSessionInfo() {

        when (sessionDTO.status) {

            SessionDTO.Status.OPENED -> {
                callItemListFragment(sessionDTO, menuItems)
                updateMainActivity()
                Log.i("Info", "Session status is opened")
            }
            SessionDTO.Status.CLOSED -> {
                sharedPreferencesHelper.clearPreferences(this, "sessionData")
                startScanningActivity()
                Log.i("Info", "Session status is closed")
            }
            SessionDTO.Status.LOCKED -> {
                Toast.makeText(
                    this@MainActivity,
                    "This session is locked because another person is performing a payment.",
                    Toast.LENGTH_LONG
                )
                    .show()
                Log.i("Info", "Session status is locked")
                toggleElements(ElementState.WELCOME)
            }
            else -> {
                toggleElements(ElementState.WELCOME)
                startScanningActivity()
            }
        }
    }

    private fun loadMenuItems() {

        itemRepository.getAllItems(this@MainActivity, binding) {
            if (it != null) {
                menuItems = it
            }
        }
    }

    private fun startScanningActivity() {

        binding.btnScan.setOnClickListener {
            val intent = Intent(applicationContext, ScannerActivity::class.java)
            startActivity(intent)
        }

        intent.getStringExtra(RESULT)?.let {

            if (it.contains("https://") || it.contains("http://")) {
                val actionIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                startActivity(actionIntent)
            } else {

                val result: List<String> = it.split("-")

                if (result.size == 2) {
                    val tabletopId = result[0].toInt()
                    val authCode = result[1]

                    verifyTable(tabletopId, authCode)
                }
            }
        }
    }

    private fun verifyTable(tabletopId: Int, authCode: String) {

        sessionRepository.verifyTabletop(tabletopId, authCode, this@MainActivity, binding) {
            if (it != null) {
                sessionDTO = it
                if (it.orders?.size == 0) {
                    createOrder(it)
                } else {
                    loadSessionInfo()
                }
            }
        }
    }

    private fun callItemListFragment(session: SessionDTO, itemList: ArrayList<ItemDTO>) {

        val bundle = Bundle()
        bundle.putSerializable("session", session as Serializable)
        bundle.putSerializable("itemList", itemList as Serializable?)

        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val itemListFragment = ItemListFragment(this@MainActivity)

        itemListFragment.arguments = bundle
        fragmentTransaction.replace(R.id.frame, itemListFragment).addToBackStack(null)
        fragmentTransaction.commit()

        toggleElements(ElementState.MENU)
    }

    private fun callOrderListFragment(sessionDTO: SessionDTO) {

        val bundle = Bundle()
        bundle.putSerializable("session", sessionDTO as Serializable)

        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val orderListFragment = OrderListFragment(this@MainActivity)

        orderListFragment.arguments = bundle
        fragmentTransaction.replace(R.id.frame, orderListFragment).addToBackStack(null)
        fragmentTransaction.commit()

        toggleElements(ElementState.ORDERS)
    }

    private fun callPaymentFragment(sessionDTO: SessionDTO) {

        Companion.sessionDTO.status = SessionDTO.Status.LOCKED
        Companion.sessionDTO.orders?.last()?.status = OrderDTO.Status.SENT

        updateSession(sessionDTO) {

            val bundle = Bundle()
            bundle.putSerializable("session", sessionDTO as Serializable)

            val fragmentManager: FragmentManager = supportFragmentManager
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            val paymentFragment = PaymentFragment(this@MainActivity)

            paymentFragment.arguments = bundle
            fragmentTransaction.replace(R.id.frame, paymentFragment).addToBackStack(null)
            fragmentTransaction.commit()

            toggleElements(ElementState.PAYMENT)
        }
    }

    private fun closeSession() {

        // before closing the session app should check payment status received via webhook from Mollie
        // not implemented yet, not receiving webhook?

        sessionDTO.status = SessionDTO.Status.CLOSED
        sessionDTO.orders?.last()?.status = OrderDTO.Status.SENT

        updateSession(sessionDTO) {

            if (it != null) sessionDTO = it

            supportFragmentManager.apply {
                for (fragment in fragments) {
                    beginTransaction().remove(fragment).commit()
                }
                popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }

            sharedPreferencesHelper.clearPreferences(this, "sessionData")

            Toast.makeText(
                this@MainActivity, "Payment received :). Thank you for using EasyOrder services.",
                Toast.LENGTH_SHORT
            ).show()

            toggleElements(ElementState.WELCOME)
        }
    }

    private fun checkWifiConnection() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
        } else {

            val wifiManager =
                applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo: WifiInfo = wifiManager.connectionInfo

            if (wifiInfo.supplicantState == SupplicantState.COMPLETED) {
                AppSettings.setAppConfiguration(wifiInfo.ssid)
                loadMenuItems()
            }
        }
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

    override fun onResume() {
        super.onResume()

        sharedPreferencesHelper
            .retrievePreferences(this, "sessionData", "sessionId", "INT").let {
                if (it as Int == 0) {
                    startScanningActivity()
                } else {
                    getSession(it)
                }
            }

        checkWifiConnection()
    }

    override fun onPause() {
        super.onPause()

        updateSession(sessionDTO) {
            if (it != null) {
                sharedPreferencesHelper
                    .savePreferences(this, "sessionData", "sessionId", intValue = it.id)
            }
        }
    }

    fun updateMainActivity() {

        val decimal: NumberFormat = DecimalFormat("0.00")
        val session = sessionDTO
        val sessionTotal = session.total
        val order = sessionDTO.orders?.last()
        val count = order?.items?.size

        val orderBtnText =
            "View and send orders (Total: € ${decimal.format(sessionTotal)})"
        val checkoutBtnText =
            "Select payment options (Total: € ${decimal.format(sessionTotal)})"
        val closeSessionBtnText =
            "Click to pay (Total € ${decimal.format(sessionTotal)})"

        binding.iconOrdersBadge.text = count.toString()
        binding.btnOrders.text = orderBtnText
        binding.btnCheckout.text = checkoutBtnText
        binding.btnCloseSession.text = closeSessionBtnText
    }

    fun updateSession(sessionDTO: SessionDTO, callback: (SessionDTO?) -> Unit) {

        sessionDTO.id?.let { id ->
            sessionRepository.updateSession(id, sessionDTO, this@MainActivity, binding) { session ->
                if (session != null) {
                    session.orders?.sortBy { it.id }
                    MainActivity.sessionDTO = session
                    callback(session)
                }
            }
        }
    }

    fun passSessionToActivity(sessionDTO: SessionDTO) {

        MainActivity.sessionDTO = sessionDTO
        updateMainActivity()
    }

    fun createOrder(sessionDTO: SessionDTO) {
        sessionDTO.id?.let { sessionId ->
            val orders = sessionDTO.orders
            if (orders != null) {
                orderRepository.createOrder(sessionId, this@MainActivity, binding) { order ->
                    orders.add(order as OrderDTO).apply {
                        orders.sortBy { it.id }
                        updateSession(sessionDTO) {
                            loadSessionInfo()
                        }
                    }
                }
            }
        }
    }

    private fun createMolliePayment(sessionDTO: SessionDTO) {

        if (sessionDTO.payment == null) {

            val ngRokBaseURL = AppSettings.ngRokBaseURL
            val mollieRedirectUrl = AppSettings.mollieRedirectUrl
            val mollieWebhookMapping = AppSettings.mollieWebhookMapping
            val decimal: NumberFormat = DecimalFormat("0.00")
            val amount = decimal.format(sessionDTO.total).replace(',', '.')

            val jsonString = "{\n" +
                    "   \"description\": \"Session #${sessionDTO.id.toString()}\",\n" +
                    "   \"redirectUrl\": \"$mollieRedirectUrl\",\n" +
                    "   \"webhookUrl\": \"$ngRokBaseURL$mollieWebhookMapping\",\n" +
                    "   \"amount\": \n" +
                    "  {\n" +
                    "    \"currency\": \"EUR\",\n" +
                    "    \"value\": \"$amount\"\n" +
                    "  }\n" +
                    "}"

            sessionDTO.id?.let { sessionId ->

                if (amount != "0.0") {
                    molliePaymentRepository.retrievePayment(
                        jsonString,
                        sessionId,
                        this@MainActivity,
                        binding
                    ) { paymentFromMollie ->

                        if (paymentFromMollie != null) {
                            paymentFromMollie.sessionId = sessionId
                            molliePaymentRepository.createPayment(
                                sessionId,
                                paymentFromMollie,
                                this@MainActivity,
                                binding
                            ) { paymentFromBackend ->
                                sessionDTO.payment = paymentFromBackend
                                updateSession(sessionDTO) {
                                    val checkoutUrl = paymentFromBackend?.checkoutUrl
                                    if (checkoutUrl != null) {
                                        val browserIntent = Intent(Intent.ACTION_VIEW)
                                        browserIntent.data = Uri.parse(checkoutUrl)
                                        startActivity(browserIntent)
                                        closeSession()
                                    }
                                }
                            }
                        }
                    }
                } else {

                    Toast.makeText(
                        this@MainActivity,
                        "Please add something to your order before starting a payment ;)",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            sessionDTO.payment?.let {
                val checkoutUrl = it.checkoutUrl
                if (checkoutUrl != null) {
                    val browserIntent = Intent(Intent.ACTION_VIEW)
                    browserIntent.data = Uri.parse(checkoutUrl)
                    startActivity(browserIntent)
                }
            }
        }
    }

    fun toggleElements(state: ElementState) {

        elementState = state

        when (state) {

            ElementState.WELCOME -> {
                binding.btnOrders.isVisible = false
                binding.iconOrders.isVisible = false
                binding.iconOrdersBadge.isVisible = false
                binding.btnScan.isVisible = true
                binding.iconScan.isVisible = true
                binding.btnCheckout.isVisible = false
                binding.iconCheckout.isVisible = false
                binding.btnCloseSession.isVisible = false
                binding.iconCloseSession.isVisible = false
                binding.textViewInstructions.isVisible = true
                binding.textViewWelcome.isVisible = true
                binding.frame.setBackgroundResource(R.drawable.welcome)
            }
            ElementState.MENU -> {
                binding.btnOrders.isVisible = true
                binding.iconOrders.isVisible = true
                binding.iconOrdersBadge.isVisible = true
                binding.btnScan.isVisible = false
                binding.iconScan.isVisible = false
                binding.btnCheckout.isVisible = false
                binding.iconCheckout.isVisible = false
                binding.btnCloseSession.isVisible = false
                binding.iconCloseSession.isVisible = false
                binding.textViewInstructions.isVisible = false
                binding.textViewWelcome.isVisible = false
                binding.frame.setBackgroundResource(0)
            }
            ElementState.ORDERS -> {
                binding.btnOrders.isVisible = false
                binding.iconOrders.isVisible = false
                binding.iconOrdersBadge.isVisible = false
                binding.btnCheckout.isVisible = true
                binding.iconCheckout.isVisible = true
                binding.btnCloseSession.isVisible = false
                binding.iconCloseSession.isVisible = false
            }
            ElementState.PAYMENT -> {
                binding.btnCheckout.isVisible = false
                binding.iconCheckout.isVisible = false
                binding.btnCloseSession.isVisible = true
                binding.iconCloseSession.isVisible = true
            }
        }
    }
}