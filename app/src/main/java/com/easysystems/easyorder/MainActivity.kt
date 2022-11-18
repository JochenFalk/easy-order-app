package com.easysystems.easyorder

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.easysystems.easyorder.repositories.ItemRepository
import com.easysystems.easyorder.repositories.SessionRepository
import com.easysystems.easyorder.data.Item
import com.easysystems.easyorder.data.Order
import com.easysystems.easyorder.data.Session
import com.easysystems.easyorder.databinding.ActivityMainBinding
import com.easysystems.easyorder.helpclasses.SharedPreferencesHelper
import com.easysystems.easyorder.repositories.OrderRepository
import java.io.Serializable
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private var itemRepository = ItemRepository()
    private var orderRepository = OrderRepository()
    private var sessionRepository = SessionRepository()
    private val sharedPreferencesHelper = SharedPreferencesHelper

    private lateinit var session: Session
    private lateinit var binding: ActivityMainBinding

    companion object {
        const val RESULT = "RESULT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        sharedPreferencesHelper.clearPreferences(this, "sessionData")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCheckout.setOnClickListener {
            println("Show orders")
        }

        session = Session()
    }

    private fun getSession(id: Int) {

        sessionRepository.getSessionById(id, this@MainActivity, binding) {
            if (it != null) {
                this.session = it
                loadSessionInfo()
            }
        }
    }

    private fun loadSessionInfo() {

        when (session.status) {

            "OPENED" -> {
                loadMenuItems()
            }
            "CLOSED" -> {
                println("Session status is closed")
            }
            "LOCKED" -> {
                println("Session status is locked")
            }
            else -> {
                startScanningActivity()
            }
        }
    }

    private fun loadMenuItems() {

        itemRepository.getAllItems(this@MainActivity, binding) {
            if (it != null) {
                toggleMainElements()
                callItemListFragment(it)
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
                this.session = it

                if (session.orders?.size == 0) {
                    orderRepository.createOrder(session.id, this@MainActivity, binding) { order ->
                        session.orders?.add(order as Order)
                    }
                }

                loadSessionInfo()
            }
        }
    }

    private fun toggleMainElements() {
        binding.btnCheckout.isVisible = true
        binding.btnCheckout.isVisible = true
        binding.btnScan.isVisible = false
        binding.iconCheckout.isVisible = true
        binding.iconScan.isVisible = false
        binding.textViewInstructions.isVisible = false
        binding.textViewWelcome.isVisible = false
        binding.frame.setBackgroundResource(0)
    }

    private fun callItemListFragment(itemList: ArrayList<Item>) {

        val bundle = Bundle()
        bundle.putSerializable("itemList", itemList as Serializable?)

        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val itemListFragment = ItemListFragment()

        itemListFragment.arguments = bundle
        fragmentTransaction.add(R.id.frame, itemListFragment)
        fragmentTransaction.commit()
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

        val sessionId = session.id
        session = Session()
        sharedPreferencesHelper
            .savePreferences(this, "sessionData", "sessionId", intValue = sessionId)
    }
}