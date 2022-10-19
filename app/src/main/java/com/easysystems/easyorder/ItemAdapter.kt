package com.easysystems.easyorder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(var itemList: ArrayList<Item>) :
    RecyclerView.Adapter<ItemAdapter.ItemListViewHolder>() {

    inner class ItemListViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var name: TextView = view.findViewById(R.id.cardViewName)
        var price: TextView = view.findViewById(R.id.cardViewPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemListViewHolder {

        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_design, parent, false) // Inflate what, where and how

        return ItemListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemListViewHolder, position: Int) {

        holder.name.text = itemList[position].name.toString()
        holder.price.text = itemList[position].price.toString()
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}