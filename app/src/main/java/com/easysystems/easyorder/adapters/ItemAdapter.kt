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
import java.text.DecimalFormat
import java.text.NumberFormat

class ItemAdapter : RecyclerView.Adapter<ItemAdapter.ItemListViewHolder>() {

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

        val sessionDTO = MainActivity.sessionDTO
        val itemDTOList = MainActivity.itemDTOList

        val item = itemDTOList?.get(position)
        val name = item?.name
        val price = item?.price
        val priceAsString = "€ $price"

        holder.name.text = name
        holder.price.text = priceAsString

        holder.btnAdd.setOnClickListener {

            val orders = sessionDTO?.orders
            val order = orders?.last()

            order?.items?.add(itemDTOList!![position])
            order?.total = order?.total?.plus(price!!)
            sessionDTO?.total = sessionDTO?.total?.plus(price!!)
            MainActivity.sessionDTO = sessionDTO

            Log.i("Info", "Item added to order ${orders?.size}")

            updateView(holder)
        }
    }

    private fun updateView(holder: ItemListViewHolder) {
        val decimal: NumberFormat = DecimalFormat("0.00")
        val sessionDTO = MainActivity.sessionDTO
        val sessionTotal = sessionDTO?.total
        val order = sessionDTO?.orders?.last()
        val orderBtnText =
            "${holder.itemView.rootView.resources.getString(R.string.btnOrders)} (Total: € ${decimal.format(sessionTotal)})"
        val count = order?.items?.size

        val btnOrders = holder.itemView.rootView.findViewById<Button>(R.id.btnOrders)
        val iconOrdersBadge = holder.itemView.rootView.findViewById<com.joanzapata.iconify.widget.IconButton>(R.id.iconOrdersBadge)

        btnOrders.text = orderBtnText
        iconOrdersBadge.text = count.toString()
    }

    override fun getItemCount(): Int {
        return MainActivity.itemDTOList?.size ?: 0
    }
}