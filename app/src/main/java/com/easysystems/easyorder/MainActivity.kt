package com.easysystems.easyorder

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.easysystems.easyorder.collections.ItemCollection
import com.easysystems.easyorder.data.Item
import com.easysystems.easyorder.databinding.ActivityMainBinding
import java.io.Serializable

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val itemCollection = ItemCollection()
    private var isSessionOpen = false

    companion object {
        const val RESULT = "RESULT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCheckout.setOnClickListener {
            println("Show checkout")
        }

        if (isSessionOpen) {
            loadSessionInfo()
        } else {
            binding.btnCheckout.isVisible = false
            callScannerActivity()
        }
    }

    private fun callScannerActivity() {

        binding.btnScan.setOnClickListener {
            val intent = Intent(applicationContext, ScannerActivity::class.java)
            startActivity(intent)
        }

        intent.getStringExtra(RESULT)?.let {

            if (it.contains("https://") || it.contains("http://")) {
                val actionIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                startActivity(actionIntent)
            } else {
                isSessionOpen = true
                binding.btnScan.isVisible = false
                println("Session verified by QR code")
                //TODO: Implement call to backend to verify
            }
        }
    }

    private fun loadSessionInfo() {

        itemCollection.getAllItems(this@MainActivity, binding) {
            if (it != null) {
                toggleMainElements()
                callItemListFragment(it)
            }
        }
        //            itemCollection.getItemById(12, this@MainActivity, binding) {
//                if (it != null) {
//                    toggleMainElements()
//                    val itemList = ArrayList<Item>()
//                    itemList.add(it)
//                    callItemListFragment(itemList)
//                }
//            }
    }

    private fun toggleMainElements() {
        binding.btnCheckout.isVisible = true
        binding.btnCheckout.isVisible = true
        binding.btnScan.isVisible = false
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
        if (isSessionOpen) loadSessionInfo()
    }
}