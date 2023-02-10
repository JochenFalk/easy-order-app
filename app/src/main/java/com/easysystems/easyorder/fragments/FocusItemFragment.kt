package com.easysystems.easyorder.fragments

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.easysystems.easyorder.MainActivity
import com.easysystems.easyorder.R
import com.easysystems.easyorder.data.ItemObservable
import com.easysystems.easyorder.databinding.FragmentFocusItemBinding
import com.easysystems.easyorder.viewModels.ItemListViewModel
import java.text.DecimalFormat
import java.text.NumberFormat

class FocusItemFragment(private val holderParent: ViewGroup) : Fragment() {
    private lateinit var binding: FragmentFocusItemBinding
    private lateinit var viewModel: ItemListViewModel
    private lateinit var itemObservable: ItemObservable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFocusItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[ItemListViewModel::class.java]
        itemObservable = arguments?.getSerializable("itemToFocus") as ItemObservable

        binding.viewModel = viewModel
        binding.itemObservable = itemObservable

        binding.btnAddFromFocusItem.setOnClickListener {
            addItem(itemObservable)
        }

        binding.textViewDescription.movementMethod= ScrollingMovementMethod()

        setObservers()
    }

    private fun setObservers() {

        viewModel.updateBottomNavigation.observe(viewLifecycleOwner) { boolean ->

            if (boolean) {
                updateBottomNavigation()

                viewModel.updateBottomNavigation.value = false
            }
        }
    }

    private fun addItem(itemObservable: ItemObservable) {
        viewModel.addItem(itemObservable)
    }

    private fun updateBottomNavigation() {

        val decimal: NumberFormat = DecimalFormat("0.00")
        val sessionDTO = MainActivity.sessionDTO
        val addBtnText =
            "${resources.getString(R.string.add_to_order)} (Total: € ${decimal.format(sessionDTO?.total)})"
        val order = sessionDTO?.orders?.last()
        val count = order?.items?.size

        binding.btnAddFromFocusItem.text = addBtnText
        binding.badgeOrders.text = count.toString()
    }

    override fun onResume() {
        super.onResume()
        updateBottomNavigation()
    }
}