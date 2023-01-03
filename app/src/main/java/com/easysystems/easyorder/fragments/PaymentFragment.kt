package com.easysystems.easyorder.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.easysystems.easyorder.MainActivity
import com.easysystems.easyorder.R
import com.easysystems.easyorder.adapters.CustomExpandableListAdapter
import com.easysystems.easyorder.data.MolliePaymentDTO
import com.easysystems.easyorder.data.OrderDTO
import com.easysystems.easyorder.data.SessionDTO
import com.easysystems.easyorder.databinding.FragmentPaymentBinding
import com.easysystems.easyorder.helpclasses.AppSettings
import java.text.DecimalFormat
import java.text.NumberFormat

class PaymentFragment : Fragment() {

    private lateinit var binding: FragmentPaymentBinding

    private var listView: ExpandableListView? = null
    private var listAdapter: ExpandableListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPaymentBinding.inflate(inflater, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    resetPayment()
                }
            })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                val methodArray =
                    requireContext().resources?.getStringArray(R.array.payment_methods)

                if (methodArray != null) {

                    when (val paymentMethod = parent.getItemAtPosition(position) as String) {
                        methodArray[0] -> MainActivity.paymentMethod = paymentMethod
                        methodArray[1] -> MainActivity.paymentMethod = "ideal"
                        methodArray[2] -> MainActivity.paymentMethod = "creditcard"
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.btnStartPayment.setOnClickListener {
            checkMolliePaymentStatus()
        }

        updateView()
    }

    private fun checkMolliePaymentStatus() {

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
                                createMolliePayment(sessionDTO, paymentMethod)
                            }
                        }
                    } else {
                        when (payment.status?.uppercase()) {
                            "OPEN" -> {
                                openMollieCheckout(payment)
                            }
                            "CANCELED" -> {
                                createMolliePayment(sessionDTO, paymentMethod)
                            }
                            "PENDING" -> {}
                            "AUTHORIZED" -> {}
                            "EXPIRED" -> {
                                createMolliePayment(sessionDTO, paymentMethod)
                            }
                            "FAILED" -> {
                                createMolliePayment(sessionDTO, paymentMethod)
                            }
                            "PAID" -> {}
                        }
                    }
                } else {
                    createMolliePayment(sessionDTO, paymentMethod)
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

    private fun createMolliePayment(sessionDTO: SessionDTO, paymentMethod: String) {

        val decimal: NumberFormat = DecimalFormat("0.00")
        val amount = decimal.format(sessionDTO.total).replace(',', '.')

        if (amount != "0.00") {
            sessionDTO.createPayment(paymentMethod, amount) { paymentFromBackendDTO ->
                if (paymentFromBackendDTO != null) {
                    sessionDTO.status = SessionDTO.Status.CHANGED
                    sessionDTO.payments?.add(paymentFromBackendDTO)
                    sessionDTO.updateSession { openMollieCheckout(paymentFromBackendDTO) }
                }
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Please add something to your order before starting a payment ;)",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun openMollieCheckout(payment: MolliePaymentDTO) {
        payment.checkoutUrl?.let {
            val browserIntent = Intent(Intent.ACTION_VIEW)
            browserIntent.data = Uri.parse(it)
            startActivity(browserIntent)
        }
    }

    private fun resetPayment() {

        val sessionDTO = MainActivity.sessionDTO

        if (sessionDTO?.status != SessionDTO.Status.CLOSED) {
            sessionDTO?.status = SessionDTO.Status.OPENED
            if (sessionDTO?.orders?.last()?.status != OrderDTO.Status.OPENED) {
                sessionDTO?.createOrder {
                    sessionDTO.updateSession { }
                }
            } else {
                sessionDTO.updateSession { }
            }
        }
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun updateView() {

        val decimal: NumberFormat = DecimalFormat("0.00")
        val sessionDTO = MainActivity.sessionDTO
        val sessionTotal = sessionDTO?.total
        val startPaymentBtnText =
            "${resources.getString(R.string.btnCloseSession)} (Total: € ${
                decimal.format(
                    sessionTotal
                )
            })"

        binding.btnStartPayment.text = startPaymentBtnText

        listAdapter = context?.let { CustomExpandableListAdapter() }

        listView = binding.listView
        listView!!.setAdapter(listAdapter)
    }
}