package com.easysystems.easyorder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.easysystems.easyorder.MainActivity
import com.easysystems.easyorder.R
import com.easysystems.easyorder.adapters.ExpandableOrderListAdapter
import com.easysystems.easyorder.databinding.FragmentPaymentBinding
import com.easysystems.easyorder.viewModels.OrderListViewModel
import java.text.DecimalFormat
import java.text.NumberFormat

class PaymentFragment : Fragment() {

    private lateinit var binding: FragmentPaymentBinding
    private lateinit var viewModel: OrderListViewModel
    private lateinit var listView: ExpandableListView
    private lateinit var listAdapter: ExpandableOrderListAdapter

    private lateinit var spinner: Spinner
    private lateinit var spinnerAdapter: ArrayAdapter<CharSequence>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPaymentBinding.inflate(inflater, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    MainActivity.sessionDTO?.resetSession()
                    requireActivity().supportFragmentManager.popBackStack()
                }
            })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[OrderListViewModel::class.java]

        listView = binding.expandableListView
        listAdapter = activity?.let { ExpandableOrderListAdapter() }!!
        listView.setAdapter(listAdapter)

        spinner = binding.spinner.apply { this.setSelection(0, false) }
        spinnerAdapter = ArrayAdapter
            .createFromResource(
                requireContext(), R.array.payment_methods,
                R.layout.spinner_item
            ).apply {
                this.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>, view: View?,
                position: Int, id: Long
            ) {
                val methodArray =
                    requireContext().resources?.getStringArray(R.array.payment_methods)

                if (methodArray != null) {

                    when (val method = parent.getItemAtPosition(position) as String) {
                        methodArray[0] -> MainActivity.paymentMethod = method
                        methodArray[1] -> MainActivity.paymentMethod = "ideal"
                        methodArray[2] -> MainActivity.paymentMethod = "creditcard"
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.btnStartPayment.setOnClickListener {
            handlePaymentStatus()
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

        viewModel.updateBottomNavigation.observe(viewLifecycleOwner) { boolean ->

            if (boolean) {
                viewModel.updateBottomNavigation.value = false
                updateBottomNavigation()
            }
        }
    }

    private fun handlePaymentStatus() {

        val sessionDTO = MainActivity.sessionDTO
        val payment = MainActivity.paymentDTO
        val paymentMethod = MainActivity.paymentMethod
        val methodArray = requireContext().resources?.getStringArray(R.array.payment_methods)

        if (methodArray != null && sessionDTO != null) {
            if (paymentMethod != methodArray[0]) {
                if (payment != null) {
                    if (payment.method != paymentMethod) {
                        payment.status = "canceled"
                        payment.updatePaymentToBackend { molliePaymentDTO ->
                            if (molliePaymentDTO != null) {
                                sessionDTO.addNewPaymentToSession {
                                    sessionDTO.updateSession {
                                        MainActivity.paymentDTO?.getCheckoutIntent {
                                            startActivity(it)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        when (payment.status?.uppercase()) {
                            "OPEN" -> {
                                openMollieCheckout()
                            }
                            "CANCELED" -> {
                                addNewPaymentToSession()
                            }
                            "PENDING" -> {}
                            "AUTHORIZED" -> {}
                            "EXPIRED" -> {
                                addNewPaymentToSession()
                            }
                            "FAILED" -> {
                                addNewPaymentToSession()
                            }
                            "PAID" -> {}
                        }
                    }
                } else {
                    addNewPaymentToSession()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please choose a payment method.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun addNewPaymentToSession() {

        val sessionDTO = MainActivity.sessionDTO

        sessionDTO?.addNewPaymentToSession {
            sessionDTO.updateSession {
                openMollieCheckout()
            }
        }
    }

    private fun openMollieCheckout() {
        MainActivity.paymentDTO?.getCheckoutIntent { startActivity(it) }
    }

    private fun updateBottomNavigation() {

        val decimal: NumberFormat = DecimalFormat("0.00")
        val sessionDTO = MainActivity.sessionDTO
        val startPaymentBtnText =
            "${resources.getString(R.string.btnCloseSession)} (Total: € ${decimal.format(sessionDTO?.total)})"

        binding.btnStartPayment.text = startPaymentBtnText
    }
}