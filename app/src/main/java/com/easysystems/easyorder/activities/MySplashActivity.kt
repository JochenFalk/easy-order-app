package com.easysystems.easyorder.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.easysystems.easyorder.MainActivity
import com.easysystems.easyorder.data.OrderDTO
import com.easysystems.easyorder.databinding.ActivitySplashBinding
import com.easysystems.easyorder.helpclasses.SharedPreferencesHelper
import com.easysystems.easyorder.repositories.OrderRepository
import com.easysystems.easyorder.repositories.SessionRepository
import org.koin.android.ext.android.inject

@SuppressLint("CustomSplashScreen")
class MySplashActivity : AppCompatActivity() {

    private val sessionRepository: SessionRepository by inject()
    private val orderRepository: OrderRepository by inject()
    private val sharedPreferencesHelper: SharedPreferencesHelper by inject()

    private lateinit var binding: ActivitySplashBinding

    companion object {
        const val RESULT = "RESULT"
    }

    override fun onBackPressed() {
//        super.onBackPressed() // Override back-press
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.title = "Welcome"

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        sessionRepository.verifyTabletop(tabletopId, authCode) { sessionDTO ->

            if (sessionDTO != null) {
                sharedPreferencesHelper
                    .savePreferences(this, "sessionData", "sessionId", intValue = sessionDTO.id)
                if (sessionDTO.orders?.size == 0) {
                    sessionDTO.id?.let { id ->
                        sessionDTO.orders.let { orders ->
                            orderRepository.createOrder(id) { order ->
                                orders?.add(order as OrderDTO)?.apply {
                                    sessionDTO.updateSession { updatedSession ->
                                        if (updatedSession != null) {
                                            updatedSession.orders?.sortBy { it.id }
                                            callMainActivity()
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    callMainActivity()
                }
            }
        }
    }

    private fun callMainActivity() {
        val intent = Intent(this@MySplashActivity, MainActivity::class.java)
        startActivity(intent)
    }
}