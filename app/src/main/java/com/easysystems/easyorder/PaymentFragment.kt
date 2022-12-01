package com.easysystems.easyorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.easysystems.easyorder.adapters.CustomExpandableListAdapter
import com.easysystems.easyorder.data.OrderDTO
import com.easysystems.easyorder.data.SessionDTO
import com.easysystems.easyorder.databinding.FragmentPaymentBinding

class PaymentFragment(private val activity: MainActivity) : Fragment() {

    private lateinit var binding: FragmentPaymentBinding

    private var listView: ExpandableListView? = null
    private var listAdapter: ExpandableListAdapter? = null
    private var orders: List<OrderDTO>? = null
    private var titles: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                resetPayment()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPaymentBinding.inflate(inflater, container, false)

        val sessionDTO = MainActivity.sessionDTO

        orders = sessionDTO.orders
        titles = mutableListOf<String>().apply {

            if (orders != null) {
                var count = 0
                for (o in orders!!) {
                    count++
                    this.add("Order$count")
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

        listView!!.setOnGroupExpandListener { groupPosition ->
//            Toast.makeText(
//                context, (expandableListTitle as ArrayList<String>)[groupPosition] + " List Expanded.",
//                Toast.LENGTH_SHORT
//            ).show()
        }

        listView!!.setOnGroupCollapseListener { groupPosition ->
//            Toast.makeText(
//                context, (expandableListTitle as ArrayList<String>)[groupPosition] + " List Collapsed.",
//                Toast.LENGTH_SHORT
//            ).show()
        }

        listView!!.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
//            Toast.makeText(
//                context,
//                (expandableListTitle as ArrayList<String>)[groupPosition] + " -> "
//                        + expandableListDetail!![(expandableListTitle as ArrayList<String>)[groupPosition]]!![childPosition],
//                Toast.LENGTH_SHORT
//            ).show()
            false
        }

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        resetPayment()
    }

    private fun resetPayment() {

        val sessionDTO = MainActivity.sessionDTO

        if (sessionDTO.status != SessionDTO.Status.CLOSED) {

            sessionDTO.status = SessionDTO.Status.OPENED
            sessionDTO.orders?.last()?.status = OrderDTO.Status.OPENED

            activity.updateSession(sessionDTO) {}
            activity.toggleElements(MainActivity.ElementState.MENU)
            activity.supportFragmentManager.popBackStack()
        }
    }
}