package com.easysystems.easyorder

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.easysystems.easyorder.adapters.CustomExpandableListAdapter
import com.easysystems.easyorder.data.OrderDTO
import com.easysystems.easyorder.data.SessionDTO
import com.easysystems.easyorder.databinding.FragmentOrdersBinding

class OrderListFragment(private val activity: MainActivity) : Fragment() {

    private lateinit var binding: FragmentOrdersBinding

    private var listView: ExpandableListView? = null
    private var listAdapter: ExpandableListAdapter? = null
    private var orders: ArrayList<OrderDTO>? = null
    private var titles: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {
                activity.supportFragmentManager.popBackStack()
                activity.toggleElements(MainActivity.ElementState.MENU)
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentOrdersBinding.inflate(inflater, container, false)

        val sessionDTO = MainActivity.sessionDTO

        orders = sessionDTO.orders
        titles = ArrayList<String>().apply {

            if (orders != null) {
                for (o in orders!!.withIndex()) {
                    this.add("Order ${o.index + 1}")
                }
            }
        }

        binding.btnClear.setOnClickListener {

            val order = sessionDTO.orders?.last()

            if (order != null) {
                if (order.items?.size != 0) {

                    var sessionTotal = sessionDTO.total
                    val orderTotal = order.total

                    if (sessionTotal != null && orderTotal != null) {
                        sessionTotal -= orderTotal

                        sessionDTO.total = sessionTotal
                        order.total = 0.0
                        order.items?.clear()
                        order.status = OrderDTO.Status.OPENED

                        activity.updateMainActivity()
                        activity.supportFragmentManager.popBackStack()
                        activity.toggleElements(MainActivity.ElementState.MENU)

                        Toast.makeText(
                            context,
                            "Your order has been cleared :)",
                            Toast.LENGTH_SHORT
                        )
                            .show()

                        Log.i("Info", "Order with id ${order.id} has been cleared")
                    }
                } else {

                    Toast.makeText(
                        context,
                        "There's nothing to clear here ;)",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }

        binding.btnSend.setOnClickListener {

            val order = sessionDTO.orders?.last()

            if (order != null) {
                if (order.items?.size != 0) {

                    order.status = OrderDTO.Status.SENT
                    activity.createOrder(sessionDTO)
                    activity.toggleElements(MainActivity.ElementState.MENU)

                    Toast.makeText(
                        context,
                        "Your order has been sent :)",
                        Toast.LENGTH_SHORT
                    )
                        .show()

                    Log.i("Info", "Order with id ${order.id} has been sent")
                } else {

                    Toast.makeText(
                        context,
                        "There's nothing to send here ;)",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }

        listAdapter = context?.let {
            CustomExpandableListAdapter(
                it, sessionDTO, orders, titles as ArrayList<String>
            )
        }

        listView = binding.listView
        listView!!.setAdapter(listAdapter)

        return binding.root
    }
}