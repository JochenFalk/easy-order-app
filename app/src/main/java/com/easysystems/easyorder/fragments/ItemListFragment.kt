package com.easysystems.easyorder.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.easysystems.easyorder.MainActivity
import com.easysystems.easyorder.R
import com.easysystems.easyorder.adapters.ItemAdapter
import com.easysystems.easyorder.databinding.FragmentItemListBinding
import com.easysystems.easyorder.viewModels.ItemListViewModel
import java.text.DecimalFormat
import java.text.NumberFormat

class ItemListFragment : Fragment() {

    private lateinit var binding: FragmentItemListBinding
    private lateinit var viewModel: ItemListViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter

    private var isEmptyList: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentItemListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Override back-press
                }
            })

        viewModel = ViewModelProvider(this)[ItemListViewModel::class.java]

        recyclerView = binding.recyclerView
        itemAdapter = activity.let {
            recyclerView.layoutManager = LinearLayoutManager(it)
            ItemAdapter(viewModel)
        }
        recyclerView.adapter = itemAdapter

        binding.btnOrders.setOnClickListener {
            if (isEmptyList) {
                callOrderListFragment()
            } else {
                Toast.makeText(
                    requireActivity(),
                    "Try adding some items to your order first ;)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        setObservers()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setObservers() {

        viewModel.itemList.observe(viewLifecycleOwner) { itemList ->

            if (itemList != null) {
                itemAdapter.itemList.clear()
                itemAdapter.itemList.addAll(itemList)
                itemAdapter.notifyDataSetChanged()
            }
        }

        viewModel.dataRetrievalError.observe(viewLifecycleOwner) { boolean ->

            if (boolean) {
                Toast.makeText(
                    requireActivity(),
                    "Oeps! Something went wrong loading the menu items:(",
                    Toast.LENGTH_SHORT
                ).show()

                viewModel.dataRetrievalError.value = false
            }
        }

        viewModel.updateBottomNavigation.observe(viewLifecycleOwner) { boolean ->

            if (boolean) {
                updateBottomNavigation()
                viewModel.updateBottomNavigation.value = false
            }
        }

        viewModel.isEmptyList.observe(viewLifecycleOwner) { boolean ->
            isEmptyList = boolean
        }
    }

    private fun callOrderListFragment() {

        val fragmentManager: FragmentManager = parentFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val orderListFragment = OrderListFragment()

        fragmentTransaction.replace(R.id.frame, orderListFragment).addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun updateBottomNavigation() {

        val decimal: NumberFormat = DecimalFormat("0.00")
        val sessionDTO = MainActivity.sessionDTO
        val orderBtnText =
            "${resources.getString(R.string.btnOrders)} (Total: € ${decimal.format(sessionDTO?.total)})"
        val order = sessionDTO?.orders?.last()
        val count = order?.items?.size

        binding.btnOrders.text = orderBtnText
        binding.badgeOrders.text = count.toString()

        isEmptyList = MainActivity.sessionDTO?.orders?.get(0)?.items?.size !=0
    }

    override fun onResume() {
        super.onResume()
        updateBottomNavigation()
    }
}