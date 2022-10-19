package com.easysystems.easyorder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.easysystems.easyorder.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.Serializable
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var itemList: ArrayList<Item>

    var isSessionOpen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getAllItems()
    }

    private fun getAllItems() {

        val ipAddress = "192.168.178.136"
        val baseURL = "http://$ipAddress:8080/v1/"

        val retrofit = Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()

        val retrofitAPI: RetrofitAPI = retrofit.create(RetrofitAPI::class.java)
        val call: Call<List<Item>> = retrofitAPI.getAllItems()

        call.enqueue(object : Callback<List<Item>> {
            override fun onResponse(call: Call<List<Item>>, response: Response<List<Item>>) {

                if (response.isSuccessful) {

                    try {

                        itemList = response.body() as ArrayList<Item>
                        binding.progressBar.isVisible = false

                        callItemListFragment(itemList)

                        println("Retrieved items successfully. List size: ${itemList.size}")

                    } catch (ex: Exception) {

                        binding.progressBar.isVisible = false

                        Toast.makeText(
                            applicationContext,
                            "Found list is empty!",
                            Toast.LENGTH_LONG
                        ).show()

                        println("Returned list is empty: $ex")
                    }
                } else {

                    binding.progressBar.isVisible = false

                    Toast.makeText(
                        applicationContext,
                        "Failed to retrieve list!",
                        Toast.LENGTH_LONG
                    ).show()

                    println("Failed to retrieve list!")
                }
            }

            override fun onFailure(call: Call<List<Item>>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Failed to retrieve list!",
                    Toast.LENGTH_LONG
                ).show()

                println("Request failed with error: ${t.localizedMessage}")
            }
        })
    }

    private fun callItemListFragment(itemList: ArrayList<Item>) {

        if (isSessionOpen)
        {
            val bundle = Bundle()
            bundle.putSerializable("itemList", itemList as Serializable?)

            val fragmentManager: FragmentManager = supportFragmentManager
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            val itemListFragment = ItemListFragment()

            itemListFragment.arguments = bundle
            fragmentTransaction.add(R.id.frame, itemListFragment)
            fragmentTransaction.commit()
        } else {
            Toast.makeText(this@MainActivity,
                "No session opened. Returning to splash screen!",
                Toast.LENGTH_SHORT)
                .show()
        }
    }
}