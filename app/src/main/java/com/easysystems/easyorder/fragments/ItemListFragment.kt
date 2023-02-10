package com.easysystems.easyorder.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.easysystems.easyorder.MainActivity
import com.easysystems.easyorder.R
import com.easysystems.easyorder.adapters.ItemListAdapter
import com.easysystems.easyorder.databinding.FragmentItemListBinding
import com.easysystems.easyorder.viewModels.ItemListViewModel
import java.text.DecimalFormat
import java.text.NumberFormat

class ItemListFragment : Fragment() {

    private lateinit var binding: FragmentItemListBinding
    private lateinit var viewModel: ItemListViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemListAdapter: ItemListAdapter

    private var isEmptyList: Boolean = false

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val inflater = TransitionInflater.from(requireContext())
//        exitTransition = inflater.inflateTransition(R.transition.fade)
//        enterTransition = inflater.inflateTransition(R.transition.slide)
//    }

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

        setHasOptionsMenu(true)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Override back-press
                }
            })

        viewModel = ViewModelProvider(requireActivity())[ItemListViewModel::class.java]

        arguments?.getBoolean("isEditMode").let {
            if (it != null) {
                viewModel.isEditMode = it
            }
        }

        recyclerView = binding.recyclerView
        itemListAdapter = activity.let {
            recyclerView.layoutManager = LinearLayoutManager(it)
            ItemListAdapter(viewModel)
        }
        recyclerView.adapter = itemListAdapter

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

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.item_list_menu, menu)
        menu.findItem(R.id.itemEdit)?.isVisible = !viewModel.isEditMode
        menu.findItem(R.id.itemDone)?.isVisible = viewModel.isEditMode
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.itemEdit -> {
                setEditMode(true)
            }
            R.id.itemDone -> {
                setEditMode(false)
            }
        }
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setObservers() {

        viewModel.itemObservable.observe(viewLifecycleOwner) { item ->
            viewModel.updateItemList(item)
            Log.i("Info", "Item observed: ${item.price}")
        }

        viewModel.itemObservableList.observe(viewLifecycleOwner) { itemList ->

            if (itemList != null) {
                itemListAdapter.itemList = itemList
                itemListAdapter.notifyDataSetChanged()
                Log.i("Info", "Item list observed: ${itemList[0].price}")
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
        val orderListFragment = OrderListFragment()

        fragmentManager.commit {
            setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.slide_in,
                R.anim.fade_out
            )
            replace(R.id.frame, orderListFragment)
            addToBackStack(null)
        }
    }

    private fun setEditMode(boolean: Boolean) {

        val fragmentManager: FragmentManager = parentFragmentManager
        val itemListFragment = ItemListFragment()
        val bundle = Bundle()

        bundle.putBoolean("isEditMode", boolean)
        itemListFragment.arguments = bundle

        fragmentManager.commit {
            setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.slide_in,
                R.anim.slide_out
            )
            replace(R.id.frame, itemListFragment)
            replace(this@ItemListFragment.id, itemListFragment)
        }
    }

    private fun updateBottomNavigation() {

        val decimal: NumberFormat = DecimalFormat("0.00")
        val sessionDTO = MainActivity.sessionDTO
        val orderBtnText =
            "${resources.getString(R.string.btn_orders)} (Total: € ${decimal.format(sessionDTO?.total)})"
        val order = sessionDTO?.orders?.last()
        val count = order?.items?.size

        binding.btnOrders.text = orderBtnText
        binding.badgeOrders.text = count.toString()

        isEmptyList = MainActivity.sessionDTO?.orders?.get(0)?.items?.size != 0
    }

    override fun onResume() {
        super.onResume()
        updateBottomNavigation()
    }
}