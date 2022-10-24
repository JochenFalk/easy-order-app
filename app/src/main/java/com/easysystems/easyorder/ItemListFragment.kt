package com.easysystems.easyorder

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.easysystems.easyorder.adapters.ItemAdapter
import com.easysystems.easyorder.data.Item
import com.easysystems.easyorder.databinding.FragmentItemListBinding
import java.util.ArrayList

class ItemListFragment : Fragment() {

    private lateinit var binding: FragmentItemListBinding

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
        val itemList = (if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getSerializable("itemList")!!
        } else {
            arguments?.get("itemList")
        }) as ArrayList<Item>

        val itemAdapter: ItemAdapter? = activity?.let {
            itemListView.layoutManager = LinearLayoutManager(it)
            ItemAdapter(itemList)
        }

        itemListView.adapter = itemAdapter
    }
}