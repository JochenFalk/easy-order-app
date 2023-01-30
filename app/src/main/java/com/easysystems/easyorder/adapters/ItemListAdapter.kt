package com.easysystems.easyorder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.easysystems.easyorder.data.ItemObservable
import com.easysystems.easyorder.databinding.CardDesignBinding
import com.easysystems.easyorder.viewModels.ItemListViewModel

class ItemListAdapter(private val viewModel: ItemListViewModel) :
    RecyclerView.Adapter<ItemListAdapter.ItemHolder>() {

    var itemList = ArrayList<ItemObservable>()

    inner class ItemHolder(private val binding: CardDesignBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(itemObservable: ItemObservable?) {

            if (itemObservable != null) {
                binding.itemObservable = itemObservable
                binding.btnAdd.setOnClickListener {
                    viewModel.addItem(itemObservable)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = CardDesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}