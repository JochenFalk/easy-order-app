package com.easysystems.easyorder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.easysystems.easyorder.MainActivity
import com.easysystems.easyorder.R
import com.easysystems.easyorder.adapters.ItemAdapter
import com.easysystems.easyorder.databinding.FragmentItemListBinding
import java.text.DecimalFormat
import java.text.NumberFormat

class ItemListFragment : Fragment() {

    private lateinit var binding: FragmentItemListBinding

    private lateinit var itemListView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentItemListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Override back-press
            }
        })

        itemListView = binding.recyclerView
        itemAdapter = activity.let {
            itemListView.layoutManager = LinearLayoutManager(it)
            ItemAdapter()
        }

        itemListView.adapter = itemAdapter

        binding.btnOrders.setOnClickListener {
            callOrderListFragment()
        }

        updateView()
    }

    private fun callOrderListFragment() {

        val fragmentManager: FragmentManager = parentFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val orderListFragment = OrderListFragment()

        fragmentTransaction.replace(R.id.frame, orderListFragment).addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun updateView() {

        val decimal: NumberFormat = DecimalFormat("0.00")
        val sessionDTO = MainActivity.sessionDTO
        val sessionTotal = sessionDTO?.total
        val order = sessionDTO?.orders?.last()
        val orderBtnText =
            "${resources.getString(R.string.btnOrders)} (Total: € ${decimal.format(sessionTotal)})"
        val count = order?.items?.size

        binding.btnOrders.text = orderBtnText
        binding.iconOrdersBadge.text = count.toString()
    }
}