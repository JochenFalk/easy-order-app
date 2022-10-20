package com.easysystems.easyorder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.easysystems.easyorder.collections.ItemCollection
import com.easysystems.easyorder.data.Item
import com.easysystems.easyorder.databinding.ActivityMainBinding
import java.io.Serializable

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private var isSessionOpen = true
    private val itemCollection = ItemCollection()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCheckout.setOnClickListener {

            println("Show checkout")
        }

        if (isSessionOpen) {
            itemCollection.getAllItems(this@MainActivity, binding) {
                if (it != null) callItemListFragment(it)
            }
//            itemCollection.getItemById(12, this@MainActivity, binding) {
//                if (it != null) {
//                    val itemList = ArrayList<Item>()
//                    itemList.add(it)
//                    callItemListFragment(itemList)
//                }
//            }
        } else {

            binding.progressBar.isVisible = false

            Toast.makeText(
                this@MainActivity,
                "No session opened. Returning to splash screen...",
                Toast.LENGTH_SHORT
            )
                .show()
        }
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
}