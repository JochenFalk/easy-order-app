package com.easysystems.easyorder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.easysystems.easyorder.data.ItemDTO
import com.easysystems.easyorder.databinding.CardDesignBinding
import com.easysystems.easyorder.viewModels.ItemListViewModel
import java.text.DecimalFormat
import java.text.NumberFormat

class ItemAdapter(private val viewModel: ItemListViewModel) : RecyclerView.Adapter<ItemAdapter.ItemHolder>() {

    private lateinit var binding: CardDesignBinding
    val itemList = ArrayList<ItemDTO>()

    inner class ItemHolder : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemDTO) {

            val decimal: NumberFormat = DecimalFormat("0.00")
            val priceAsString = "€ ${decimal.format(item.price)}"

            binding.cardViewName.text = item.name
            binding.cardViewPrice.text = priceAsString
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {

        binding = CardDesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder()
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {

        val item: ItemDTO = itemList[position]
        holder.bind(item)

        binding.btnAdd.setOnClickListener {
            viewModel.addItem(item)
        }

        viewModel.updateBottomNavigation.value = true
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}