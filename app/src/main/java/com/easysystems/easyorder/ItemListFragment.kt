package com.easysystems.easyorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.easysystems.easyorder.adapters.ItemAdapter
import com.easysystems.easyorder.databinding.FragmentItemListBinding

class ItemListFragment(private  val activity: MainActivity) : Fragment() {

    private lateinit var binding: FragmentItemListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                (activity as MainActivity).supportFragmentManager.popBackStack()
            }
        })
    }

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

        val itemListView: RecyclerView = binding.recyclerView
        val sessionDTO = MainActivity.sessionDTO
        val menuItems = MainActivity.menuItems

        val itemAdapter: ItemAdapter = activity.let {
            itemListView.layoutManager = LinearLayoutManager(it)
            ItemAdapter(activity, sessionDTO, menuItems)
        }

        itemListView.adapter = itemAdapter
    }
}