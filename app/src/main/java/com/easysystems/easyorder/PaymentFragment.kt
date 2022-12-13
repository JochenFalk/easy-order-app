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

        val spinner = binding.spinner.apply { this.setSelection(0, false) }
        val staticAdapter = ArrayAdapter
            .createFromResource(
                requireContext(), R.array.payment_methods,
                R.layout.spinner_item
            )
        staticAdapter
            .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = staticAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?,
                position: Int, id: Long
            ) {

                val methodArray = requireContext().resources?.getStringArray(R.array.payment_methods)

                if (methodArray != null) {

                    val paymentMethod = parent.getItemAtPosition(position) as String

                    when (paymentMethod) {
                        methodArray[0] -> MainActivity.paymentMethod = paymentMethod
                        methodArray[1] -> MainActivity.paymentMethod = "ideal"
                        methodArray[2] -> MainActivity.paymentMethod = "creditcard"
                        methodArray[3] -> MainActivity.paymentMethod = "applepay"
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
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