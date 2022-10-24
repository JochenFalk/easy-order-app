package com.easysystems.easyorder.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.easysystems.easyorder.R
import com.easysystems.easyorder.data.Item

class ItemAdapter(var itemList: ArrayList<Item>) :
    RecyclerView.Adapter<ItemAdapter.ItemListViewHolder>() {

    inner class ItemListViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var name: TextView = view.findViewById(R.id.cardViewName)
        var price: TextView = view.findViewById(R.id.cardViewPrice)
        var btnAdd: Button = view.findViewById(R.id.btnAdd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemListViewHolder {

        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_design, parent, false)

        return ItemListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemListViewHolder, position: Int) {

        val name = itemList[position].name.toString()
        val price = "€ " + itemList[position].price.toString()

        holder.name.text = name
        holder.price.text = price

        holder.btnAdd.setOnClickListener {

            println("Add item and show total")
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}