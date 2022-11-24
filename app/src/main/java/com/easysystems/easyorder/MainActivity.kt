package com.easysystems.easyorder

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.easysystems.easyorder.repositories.ItemRepository
import com.easysystems.easyorder.repositories.SessionRepository
import com.easysystems.easyorder.data.ItemDTO
import com.easysystems.easyorder.data.OrderDTO
import com.easysystems.easyorder.data.SessionDTO
import com.easysystems.easyorder.databinding.ActivityMainBinding
import com.easysystems.easyorder.helpclasses.SharedPreferencesHelper
import com.easysystems.easyorder.repositories.OrderRepository
import java.io.Serializable
import java.text.DecimalFormat
import java.text.NumberFormat

class MainActivity : AppCompatActivity() {

    private var itemRepository = ItemRepository()
    private var orderRepository = OrderRepository()
    private var sessionRepository = SessionRepository()
    private val sharedPreferencesHelper = SharedPreferencesHelper

    private lateinit var binding: ActivityMainBinding

    companion object {
        lateinit var sessionDTO: SessionDTO
        const val RESULT = "RESULT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        sharedPreferencesHelper.clearPreferences(this, "sessionData")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOrders.setOnClickListener {
            callOrderListFragment(sessionDTO)
        }
    }

    private fun getSession(id: Int) {

        sessionRepository.getSessionById(id, this@MainActivity, binding) { session ->
            if (session != null) {
                session.orders?.sortBy { it.id }
                sessionDTO = session
                loadSessionInfo()
            }
        }
    }

    private fun loadSessionInfo() {

        when (sessionDTO.status.toString()) {

            "OPENED" -> {
                loadMenuItems()
                updateMainActivity()
                Log.i("Info", "Session status is opened")
            }
            "CLOSED" -> {
                Log.i("Info", "Session status is closed")
            }
            "LOCKED" -> {
                Log.i("Info", "Session status is locked")
            }
            else -> {
                startScanningActivity()
            }
        }
    }

    private fun loadMenuItems() {

        itemRepository.getAllItems(this@MainActivity, binding) {
            if (it != null) {
                toggleElements("MAIN")
                callItemListFragment(sessionDTO, it)
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
                val tabletopId = result[0].toInt()
                val authCode = result[1]

                verifyTable(tabletopId, authCode)
            }
        }
    }

    private fun verifyTable(tabletopId: Int, authCode: String) {

        sessionRepository.verifyTabletop(tabletopId, authCode, this@MainActivity, binding) {
            if (it != null) {
                sessionDTO = it
                sharedPreferencesHelper
                    .savePreferences(this, "sessionData", "sessionId", intValue = it.id)

                if (it.orders?.size == 0) {
                    createNewOrder(it)
                } else {
                    loadSessionInfo()
                }
            }
        }
    }

    private fun callItemListFragment(sessionDTO: SessionDTO, itemDTOList: ArrayList<ItemDTO>) {

        val bundle = Bundle()
        bundle.putSerializable("session", sessionDTO as Serializable)
        bundle.putSerializable("itemList", itemDTOList as Serializable?)

        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val itemListFragment = ItemListFragment()

        itemListFragment.arguments = bundle
        fragmentTransaction.replace(R.id.frame, itemListFragment)
        fragmentTransaction.commit()

        toggleElements("ORDERS")
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

        toggleElements("PAYMENT")
    }

    private fun updateMainActivity() {

        val decimal: NumberFormat = DecimalFormat("00.00")
        val session = sessionDTO
        val sessionTotal = session.total
        val order = sessionDTO.orders?.last()
        val orderTotal = order?.total
        val count = order?.items?.size
        val orderBtnText = "View and send your order (€ ${decimal.format(orderTotal)})"
        val checkoutBtnText = "Click and pay your total bill (€ ${decimal.format(sessionTotal)})"

        binding.iconOrdersBadge.text = count.toString()
        binding.btnOrders.text = orderBtnText
        binding.btnCheckout.text = checkoutBtnText
    }

    private fun updateSession(sessionDTO: SessionDTO) {

        sessionDTO.id?.let { id ->
            sessionRepository.updateSession(id, sessionDTO, this@MainActivity, binding) { session ->
                if (session != null) {
                    session.orders?.sortBy { it.id }
                    MainActivity.sessionDTO = session
                }
                loadSessionInfo()
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
    }

    override fun onPause() {
        super.onPause()

        val sessionId = sessionDTO.id

        if (sessionId != null) {
            updateSession(sessionDTO)
            sharedPreferencesHelper
                .savePreferences(this, "sessionData", "sessionId", intValue = sessionId)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        supportFragmentManager.popBackStack()
        toggleElements("ORDERS")
    }

    fun passSessionToActivity(sessionDTO: SessionDTO) {

        MainActivity.sessionDTO = sessionDTO
        updateMainActivity()
    }

    fun createNewOrder(sessionDTO: SessionDTO) {
        sessionDTO.id?.let { id ->
            val orders = sessionDTO.orders
            if (orders != null) {
                orderRepository.createOrder(id, this@MainActivity, binding) { order ->
                    orders.add(order as OrderDTO).apply {
                        orders.sortBy { it.id }
                        updateSession(sessionDTO)
                    }
                }
            }
        }
    }

    fun toggleElements(elementState: String) {

        when (elementState) {

            "MAIN" -> {
                binding.btnOrders.isVisible = false
                binding.iconOrders.isVisible = false
                binding.iconOrdersBadge.isVisible = false
                binding.btnScan.isVisible = true
                binding.iconScan.isVisible = true
                binding.btnCheckout.isVisible = false
                binding.iconCheckout.isVisible = false
                binding.textViewInstructions.isVisible = true
                binding.textViewWelcome.isVisible = true
                binding.frame.setBackgroundResource(R.drawable.welcome)
            }
            "ORDERS" -> {
                binding.btnOrders.isVisible = true
                binding.iconOrders.isVisible = true
                binding.iconOrdersBadge.isVisible = true
                binding.btnScan.isVisible = false
                binding.iconScan.isVisible = false
                binding.btnCheckout.isVisible = false
                binding.iconCheckout.isVisible = false
                binding.textViewInstructions.isVisible = false
                binding.textViewWelcome.isVisible = false
                binding.frame.setBackgroundResource(0)
            }
            "PAYMENT" -> {
                binding.btnOrders.isVisible = false
                binding.iconOrders.isVisible = false
                binding.iconOrdersBadge.isVisible = false
                binding.btnCheckout.isVisible = true
                binding.iconCheckout.isVisible = true
            }
            else -> {
                Log.w("Warning", "Provided element state ($elementState) is not a valid parameter!")
            }
        }
    }
}