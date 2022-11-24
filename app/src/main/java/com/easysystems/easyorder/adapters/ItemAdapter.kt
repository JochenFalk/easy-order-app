package com.easysystems.easyorder.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.easysystems.easyorder.MainActivity
import com.easysystems.easyorder.R
import com.easysystems.easyorder.data.ItemDTO
import com.easysystems.easyorder.data.SessionDTO

class ItemAdapter(var activity: MainActivity, var sessionDTO: SessionDTO, var itemDTOList: ArrayList<ItemDTO>) :
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

        val item = itemDTOList[position]
        val name = item.name
        val price = item.price
        val priceAsString = "€ $price"

        holder.name.text = name
        holder.price.text = priceAsString

        holder.btnAdd.setOnClickListener {

            val orders = sessionDTO.orders
            val order = orders?.last()

            order?.items?.add(itemDTOList[position])
            order?.total = order?.total?.plus(price!!)
            sessionDTO.total = sessionDTO.total?.plus(price!!)

            activity.passSessionToActivity(sessionDTO)

            Log.i("Info","Item added to order ${orders?.size}")
        }
    }

    override fun getItemCount(): Int {
        return itemDTOList.size
    }
}