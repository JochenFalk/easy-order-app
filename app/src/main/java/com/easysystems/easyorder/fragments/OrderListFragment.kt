package com.easysystems.easyorder.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.easysystems.easyorder.MainActivity
import com.easysystems.easyorder.R
import com.easysystems.easyorder.adapters.CustomExpandableListAdapter
import com.easysystems.easyorder.data.OrderDTO
import com.easysystems.easyorder.data.SessionDTO
import com.easysystems.easyorder.databinding.FragmentOrdersBinding
import java.text.DecimalFormat
import java.text.NumberFormat

class OrderListFragment : Fragment() {

    private lateinit var binding: FragmentOrdersBinding

    private var listView: ExpandableListView? = null
    private var listAdapter: ExpandableListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClear.setOnClickListener {

            val sessionDTO = MainActivity.sessionDTO
            val lastOrder = sessionDTO?.orders?.last()

            if (lastOrder != null) {
                if (lastOrder.items?.size != 0) {

                    var sessionTotal = sessionDTO.total
                    val orderTotal = lastOrder.total

                    if (sessionTotal != null && orderTotal != null) {
                        sessionTotal -= orderTotal

                        sessionDTO.total = sessionTotal
                        lastOrder.total = 0.0
                        lastOrder.items?.clear()
                        lastOrder.status = OrderDTO.Status.OPENED

                        requireActivity().supportFragmentManager.popBackStack()

                        Toast.makeText(
                            context,
                            "Your order has been cleared :)",
                            Toast.LENGTH_SHORT
                        )
                            .show()

                        Log.i("Info", "Order with id ${lastOrder.id} has been cleared")
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

            val sessionDTO = MainActivity.sessionDTO
            val lastOrder = sessionDTO?.orders?.last()

            if (lastOrder != null) {
                if (lastOrder.items?.size != 0) {
                    lastOrder.status = OrderDTO.Status.SENT
                    sessionDTO.orders.let {
                        sessionDTO.createOrder {
                            sessionDTO.updateSession {
                                updateView()
                            }
                        }
                    }
                    Toast.makeText(
                        context,
                        "Your order has been sent :)",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    Log.i("Info", "Order with id ${lastOrder.id} has been sent")
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

        binding.btnCheckout.setOnClickListener {
            callPaymentFragment()
        }

        updateView()
    }

    private fun callPaymentFragment() {

        val sessionDTO = MainActivity.sessionDTO
        val lastOrder = sessionDTO?.orders?.last()

        if (lastOrder != null) {
            if ((lastOrder.items?.size != 0) && (lastOrder.total != 0.0)) {
                lastOrder.status = OrderDTO.Status.SENT
            }
        }

        sessionDTO?.status = SessionDTO.Status.LOCKED
        sessionDTO?.updateSession {

            val fragmentManager: FragmentManager = parentFragmentManager
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            val paymentFragment = PaymentFragment()

            fragmentTransaction.replace(R.id.frame, paymentFragment).addToBackStack(null)
            fragmentTransaction.commit()
        }
    }

    private fun updateView() {

        val decimal: NumberFormat = DecimalFormat("0.00")
        val sessionDTO = MainActivity.sessionDTO
        val sessionTotal = sessionDTO?.total
        val checkoutBtnText =
            "${resources.getString(R.string.btnCheckout)} (Total: € ${decimal.format(sessionTotal)})"

        binding.btnCheckout.text = checkoutBtnText

        listAdapter = context?.let { CustomExpandableListAdapter() }

        listView = binding.listView
        listView!!.setAdapter(listAdapter)
    }
}