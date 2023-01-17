package com.easysystems.easyorder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.easysystems.easyorder.MainActivity
import com.easysystems.easyorder.R
import com.easysystems.easyorder.adapters.ExpandableOrderListAdapter
import com.easysystems.easyorder.data.OrderDTO
import com.easysystems.easyorder.data.SessionDTO
import com.easysystems.easyorder.databinding.FragmentOrdersBinding
import com.easysystems.easyorder.viewModels.OrderListViewModel
import java.text.DecimalFormat
import java.text.NumberFormat

class OrderListFragment : Fragment() {

    private lateinit var binding: FragmentOrdersBinding
    private lateinit var viewModel: OrderListViewModel
    private lateinit var listView: ExpandableListView
    private lateinit var listAdapter: ExpandableOrderListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[OrderListViewModel::class.java]

        listView = binding.expandableListView
        listAdapter = activity?.let { ExpandableOrderListAdapter() }!!
        listView.setAdapter(listAdapter)

        binding.btnClear.setOnClickListener {
            viewModel.clearOrder()
        }

        binding.btnSend.setOnClickListener {
            viewModel.sendOrder()
        }

        binding.btnCheckout.setOnClickListener {
            callPaymentFragment()
        }

        setObservers()
    }

    private fun setObservers() {

        viewModel.orderList.observe(viewLifecycleOwner) { orderList ->

            if (orderList != null) {
                listAdapter.orderList.clear()
                listAdapter.orderList.addAll(orderList)
            }
        }

        viewModel.titleList.observe(viewLifecycleOwner) { titleList ->

            if (titleList != null) {
                listAdapter.titleList.clear()
                listAdapter.titleList.addAll(titleList)
            }
        }

        viewModel.dataRefreshError.observe(viewLifecycleOwner) { boolean ->

            if (boolean) {
                Toast.makeText(
                    requireActivity(),
                    "Oeps! Something went wrong loading the orders:(",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.orderIsClearedSuccess.observe(viewLifecycleOwner) { boolean ->

            if (boolean) {
                listAdapter.notifyDataSetChanged()
                Toast.makeText(
                    context,
                    "Your order has been cleared :)",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    "There's nothing to clear here ;)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.orderIsSentSuccess.observe(viewLifecycleOwner) { boolean ->

            if (boolean) {
                listAdapter.notifyDataSetChanged()
                val session = MainActivity.sessionDTO
                session?.orders?.let {
                    session.addNewOrderToSession {
                        session.updateSession {
                        }
                    }
                }

                Toast.makeText(
                    context,
                    "Your order has been sent :)",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    "There's nothing to send here ;)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.updateBottomNavigation.observe(viewLifecycleOwner) { boolean ->

            if (boolean) {
                viewModel.updateBottomNavigation.value = false
                updateBottomNavigation()
            }
        }
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

    private fun updateBottomNavigation() {

        val decimal: NumberFormat = DecimalFormat("0.00")
        val sessionDTO = MainActivity.sessionDTO
        val checkoutBtnText =
            "${resources.getString(R.string.btnCheckout)} (Total: € ${decimal.format(sessionDTO?.total)})"

        binding.btnCheckout.text = checkoutBtnText
    }
}