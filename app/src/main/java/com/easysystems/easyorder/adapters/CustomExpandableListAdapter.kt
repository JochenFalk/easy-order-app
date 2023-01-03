package com.easysystems.easyorder.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import com.easysystems.easyorder.MainActivity
import com.easysystems.easyorder.data.ItemDTO
import com.easysystems.easyorder.databinding.ListGroupBinding
import com.easysystems.easyorder.databinding.ListItemBinding
import java.text.DecimalFormat
import java.text.NumberFormat

class CustomExpandableListAdapter : BaseExpandableListAdapter() {

    private val titles = getTitles()

    override fun getChild(orderPosition: Int, itemPosition: Int): Any {
        return MainActivity.sessionDTO?.orders?.get(orderPosition)?.items?.get(itemPosition)
            ?: listOf<ItemDTO>()
    }

    override fun getChildId(orderPosition: Int, itemPosition: Int): Long {
        return itemPosition.toLong()
    }

    override fun getChildView(
        orderPosition: Int, itemPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup
    ): View {

        val context = parent.context
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ListItemBinding.inflate(layoutInflater, parent, false)

        val decimal: NumberFormat = DecimalFormat("0.00")
        val item = getChild(orderPosition, itemPosition) as ItemDTO
        val price = item.price
        val priceAsString = "€ ${decimal.format(price)}"

        binding.expandedListItem.text = item.name
        binding.expandedListPrice.text = priceAsString

        return binding.root
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return MainActivity.sessionDTO?.orders?.get(listPosition)?.items?.size ?: 0
    }

    override fun getGroup(listPosition: Int): Any {
        return titles[listPosition]
    }

    override fun getGroupCount(): Int {
        return titles.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(
        listPosition: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup
    ): View {

        val context = parent.context
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ListGroupBinding.inflate(layoutInflater, parent, false)

        val title = getGroup(listPosition) as String
        val items = MainActivity.sessionDTO?.orders?.get(listPosition)?.items

        val decimal: NumberFormat = DecimalFormat("0.00")
        val order = MainActivity.sessionDTO?.orders?.get(listPosition)
        var total = 0.0

        if (items != null) {
            for (i in items) {
                total += i.price!!
            }
        }

        val totalAsString = "Total: € ${decimal.format(total)}"

        binding.listTitle.text = title
        binding.listTotal.text = totalAsString
        binding.listStatus.text = order?.status.toString()

        binding.listTitle.setTypeface(null, Typeface.BOLD)

        return binding.root
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }

    private fun getTitles(): MutableList<String> {

        val titles = mutableListOf<String>()
        val orders = MainActivity.sessionDTO?.orders

        if (orders != null) {

            var count = 0

            orders.sortBy { it.id }
            orders.forEach { order ->

                if ((order.items?.size != 0) && (order.total != 0.0)) {
                    count++
                    titles.add("Order $count")
                }
            }
        }

        return titles
    }
}