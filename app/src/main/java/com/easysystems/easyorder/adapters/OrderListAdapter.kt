package com.easysystems.easyorder.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import com.easysystems.easyorder.data.ItemDTO
import com.easysystems.easyorder.data.OrderDTO
import com.easysystems.easyorder.databinding.ListGroupBinding
import com.easysystems.easyorder.databinding.ListItemBinding
import java.text.DecimalFormat
import java.text.NumberFormat

class OrderListAdapter : BaseExpandableListAdapter() {

    val titleList = ArrayList<String>()
    val orderList = ArrayList<OrderDTO>()

    private val decimal: NumberFormat = DecimalFormat("0.00")

    override fun getChild(orderPosition: Int, itemPosition: Int): Any {
        return orderList[orderPosition].items?.get(itemPosition)
            ?: listOf<ItemDTO>()
    }

    override fun getChildId(orderPosition: Int, itemPosition: Int): Long {
        return itemPosition.toLong()
    }

    override fun getChildView(
        orderPosition: Int, itemPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup
    ): View {

        val item = getChild(orderPosition, itemPosition) as ItemDTO
        val priceAsString = "€ ${decimal.format(item.price)}"

        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        binding.itemName.text = item.name
        binding.itemPrice.text = priceAsString

        return binding.root
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return orderList[listPosition].items?.size ?: 0
    }

    override fun getGroup(listPosition: Int): Any {
        return titleList[listPosition]
    }

    override fun getGroupCount(): Int {
        return titleList.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(
        listPosition: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup
    ): View {

        val title = getGroup(listPosition) as String
        val order = orderList[listPosition]
        val totalAsString = "Total: € ${decimal.format(order.total)}"

        val binding = ListGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        binding.titleText.text = title
        binding.orderTotal.text = totalAsString
        binding.orderStatus.text = order.status.toString()

        binding.titleText.setTypeface(null, Typeface.BOLD)

        return binding.root
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }
}