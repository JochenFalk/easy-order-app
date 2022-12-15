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
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.easysystems.easyorder.data.*
import com.easysystems.easyorder.databinding.ActivityMainBinding
import com.easysystems.easyorder.helpclasses.AppSettings
import com.easysystems.easyorder.helpclasses.SharedPreferencesHelper
import com.easysystems.easyorder.repositories.ItemRepository
import com.easysystems.easyorder.repositories.MolliePaymentRepository
import com.easysystems.easyorder.repositories.OrderRepository
import com.easysystems.easyorder.repositories.SessionRepository
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
        var paymentMethod = ""
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

        supportActionBar?.title = "Welcome"

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnScan.setOnClickListener {
            val intent = Intent(applicationContext, ScannerActivity::class.java)
            startActivity(intent)
        }

        binding.btnOrders.setOnClickListener {
            callOrderListFragment()
        }

        binding.btnCheckout.setOnClickListener {
            callPaymentFragment()
        }

        binding.btnCloseSession.setOnClickListener {
            checkMolliePaymentStatus(sessionDTO, paymentMethod)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                supportFragmentManager.popBackStack()
                toggleElements(ElementState.MENU)
                return true
            }
        }
        return true
    }

    private fun getSession(id: Int) {

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
                callItemListFragment()
                updateMainActivity()
                Log.i("Info", "Session status is opened")
            }
            SessionDTO.Status.CLOSED -> {
                sharedPreferencesHelper.clearPreferences(this, "sessionData")
                startScanningActivity()
                Log.i("Info", "Session status is closed")
            }
            SessionDTO.Status.LOCKED -> {

                val savedStatus = sharedPreferencesHelper.retrievePreferences(this, "sessionData", "sessionStatus", "STRING")

                if (savedStatus == "LOCKED") {
                    resetPayment()
                } else {

                    Toast.makeText(
                        this@MainActivity,
                        "This session is locked because another person is performing a payment.",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    Log.i("Info", "Session status is locked")
                    toggleElements(ElementState.WELCOME)
                }
            }
            SessionDTO.Status.CHANGED -> {
                Log.i("Info", "Session status is changed")
                verifyPayment()
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

    private fun callItemListFragment() {

        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val itemListFragment = ItemListFragment(this@MainActivity)

        fragmentTransaction.replace(R.id.frame, itemListFragment).addToBackStack(null)
        fragmentTransaction.commit()

        toggleElements(ElementState.MENU)
    }

    private fun callOrderListFragment() {

        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val orderListFragment = OrderListFragment(this@MainActivity)

        fragmentTransaction.replace(R.id.frame, orderListFragment).addToBackStack(null)
        fragmentTransaction.commit()

        toggleElements(ElementState.ORDERS)
    }

    private fun callPaymentFragment() {

        sessionDTO.status = SessionDTO.Status.LOCKED
        sessionDTO.orders?.last()?.status = OrderDTO.Status.SENT

        updateSession(sessionDTO) {

            val fragmentManager: FragmentManager = supportFragmentManager
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            val paymentFragment = PaymentFragment(this@MainActivity)

            fragmentTransaction.replace(R.id.frame, paymentFragment).addToBackStack(null)
            fragmentTransaction.commit()

            toggleElements(ElementState.PAYMENT)
        }
    }

    private fun verifyPayment() {

        sessionDTO.payment?.id?.let { it ->
            molliePaymentRepository.retrievePaymentUpdateById(
                it,
                this@MainActivity,
                binding
            ) { molliePayment ->
                if (molliePayment != null) {

                    sessionDTO.payment = molliePaymentRepository.convertPaymentToPaymentDTO(
                        sessionDTO,
                        molliePayment
                    )

                    updateSession(sessionDTO) {
                        if (it != null) {
                            when (it.payment?.status?.uppercase()) {
                                "PAID" -> {

                                    Log.i("Info", "Payment status is paid")
                                    closeSession()
                                }
                                "EXPIRED" -> {

                                    Toast.makeText(
                                        this@MainActivity,
                                        "Payment was expired. Please try again.",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    Log.i("Info", "Payment status is expired")
                                    resetPayment()
                                }
                                "FAILED" -> {

                                    Toast.makeText(
                                        this@MainActivity,
                                        "Payment failed :( Please try again.",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    Log.i("Info", "Payment status is failed")
                                    resetPayment()
                                }
                                "CANCELED" -> {

                                    Toast.makeText(
                                        this@MainActivity,
                                        "Payment was canceled. Please try another payment method.",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    Log.i("Info", "Payment status is canceled")
                                    resetPayment()
                                }
                            }
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Something unexpected happened while checking your payment. " +
                                        " Please try again",
                                Toast.LENGTH_SHORT
                            ).show()

                            Log.i("Info", "Something unexpected happened while saving the payment")
                            resetPayment()
                        }
                    }
                }
            }
        }
    }

    private fun resetPayment() {

        if (sessionDTO.status != SessionDTO.Status.CLOSED) {

            sessionDTO.status = SessionDTO.Status.OPENED
            sessionDTO.orders?.last()?.status = OrderDTO.Status.OPENED

            updateSession(sessionDTO) {
                updateMainActivity()
                callItemListFragment()
            }
        }
    }

    private fun closeSession() {

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

            sessionDTO = SessionDTO()
            sharedPreferencesHelper.clearPreferences(this, "sessionData")

            Toast.makeText(
                this@MainActivity, "Payment received :). Thank you for using EasyOrder services.",
                Toast.LENGTH_LONG
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
                val ssid = wifiInfo.ssid.toString().substring(1, wifiInfo.ssid.length - 1)
                AppSettings.setAppConfiguration(ssid)
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

    override fun onPause() {
        super.onPause()

        updateSession(sessionDTO) {
            if (it != null) {
                sharedPreferencesHelper
                    .savePreferences(this, "sessionData", "sessionId", intValue = it.id)
                sharedPreferencesHelper
                    .savePreferences(this, "sessionData", "sessionStatus", stringValue = it.status.toString())
            }
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

    fun updateMainActivity() {

        val decimal: NumberFormat = DecimalFormat("0.00")
        val session = sessionDTO
        val sessionTotal = session.total
        val order = sessionDTO.orders?.last()
        val count = order?.items?.size

        val orderBtnText =
            "${resources.getString(R.string.btnOrders)} (Total: € ${decimal.format(sessionTotal)})"
        val checkoutBtnText =
            "${resources.getString(R.string.btnCheckout)} (Total: € ${decimal.format(sessionTotal)})"
        val closeSessionBtnText =
            "${resources.getString(R.string.btnCloseSession)} (Total € ${decimal.format(sessionTotal)})"

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

    private fun checkMolliePaymentStatus(sessionDTO: SessionDTO, paymentMethod: String) {

        val methodArray = this@MainActivity.resources?.getStringArray(R.array.payment_methods)

        if (methodArray != null) {

            if (paymentMethod != methodArray[0]) {
                if (sessionDTO.payment != null) {
                    if (sessionDTO.payment?.method != MainActivity.paymentMethod) {
                        createMolliePayment(sessionDTO, MainActivity.paymentMethod)
                    } else {
                        when (sessionDTO.payment?.status?.uppercase()) {
                            "OPEN" -> openMollieCheckout(sessionDTO.payment!!)
                            "CANCELED" -> {
                                createMolliePayment(sessionDTO, paymentMethod)
                            }
                            "PENDING" -> {}
                            "AUTHORIZED" -> {}
                            "EXPIRED" -> {
                                createMolliePayment(sessionDTO, paymentMethod)
                            }
                            "FAILED" -> {}
                            "PAID" -> {}
                        }
                    }
                } else {
                    createMolliePayment(sessionDTO, paymentMethod)
                }
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Please choose a payment method.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun createMolliePayment(sessionDTO: SessionDTO, paymentMethod: String) {

        val ngRokBaseURL = AppSettings.ngRokBaseURL
        val mollieRedirectUrl = AppSettings.mollieRedirectUrl
        val mollieWebhookMapping = AppSettings.mollieWebhookMapping
        val decimal: NumberFormat = DecimalFormat("0.00")
        val amount = decimal.format(sessionDTO.total).replace(',', '.')

        val jsonString = "{\n" +
                "   \"description\": \"Session #${sessionDTO.id.toString()}\",\n" +
                "   \"redirectUrl\": \"$mollieRedirectUrl\",\n" +
                "   \"webhookUrl\": \"$ngRokBaseURL$mollieWebhookMapping\",\n" +
                "   \"method\": \"$paymentMethod\",\n" +
                "   \"amount\": \n" +
                "  {\n" +
                "    \"currency\": \"EUR\",\n" +
                "    \"value\": \"$amount\"\n" +
                "  }\n" +
                "}"

        sessionDTO.id?.let { sessionId ->

            if (amount != "0.00") {
                molliePaymentRepository.retrievePayment(
                    jsonString,
                    sessionId,
                    this@MainActivity,
                    binding
                ) { paymentFromMollie ->

                    if (paymentFromMollie != null) {
                        paymentFromMollie.sessionId = sessionId
                        molliePaymentRepository.createPayment(
                            sessionDTO,
                            paymentFromMollie,
                            this@MainActivity,
                            binding
                        ) { paymentFromBackend ->
                            sessionDTO.status = SessionDTO.Status.CHANGED
                            sessionDTO.payment = paymentFromBackend

                            updateSession(sessionDTO) {

                                if (it != null) {
                                    it.payment?.let { payment -> openMollieCheckout(payment) }
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
    }

    private fun openMollieCheckout(payment: MolliePaymentDTO) {
        payment.checkoutUrl?.let {
            val browserIntent = Intent(Intent.ACTION_VIEW)
            browserIntent.data = Uri.parse(it)
            startActivity(browserIntent)
        }
    }

    fun toggleElements(state: ElementState) {

        elementState = state

        when (state) {

            ElementState.WELCOME -> {
                supportActionBar?.title = "Welcome"
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
                supportActionBar?.title = "Menu"
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
                supportActionBar?.title = "Orders"
                binding.btnOrders.isVisible = false
                binding.iconOrders.isVisible = false
                binding.iconOrdersBadge.isVisible = false
                binding.btnCheckout.isVisible = true
                binding.iconCheckout.isVisible = true
                binding.btnCloseSession.isVisible = false
                binding.iconCloseSession.isVisible = false
            }
            ElementState.PAYMENT -> {
                supportActionBar?.title = "Payment"
                binding.btnCheckout.isVisible = false
                binding.iconCheckout.isVisible = false
                binding.btnCloseSession.isVisible = true
                binding.iconCloseSession.isVisible = true
            }
        }
    }
}