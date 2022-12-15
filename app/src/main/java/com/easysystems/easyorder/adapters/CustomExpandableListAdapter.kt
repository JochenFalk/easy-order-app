package com.easysystems.easyorder.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.easysystems.easyorder.R
import com.easysystems.easyorder.data.ItemDTO
import com.easysystems.easyorder.data.OrderDTO
import com.easysystems.easyorder.data.SessionDTO
import java.text.DecimalFormat
import java.text.NumberFormat

class CustomExpandableListAdapter(
    private val context: Context,
    private val sessionDTO: SessionDTO,
    private val orders: List<OrderDTO>?,
    private val titles: List<String>?
) : BaseExpandableListAdapter() {

    override fun getChild(orderPosition: Int, itemPosition: Int): Any {
        return sessionDTO.orders?.get(orderPosition)?.items?.get(itemPosition) ?: listOf<ItemDTO>()
    }

    override fun getChildId(orderPosition: Int, itemPosition: Int): Long {
        return itemPosition.toLong()
    }

    override fun getChildView(
        orderPosition: Int, itemPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup
    ): View {

        var view = convertView

        if (view == null) {

            val layoutInflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = layoutInflater.inflate(R.layout.list_item, parent, false)

            val listItem = view.findViewById(R.id.expandedListItem) as TextView
            val listPrice = view.findViewById(R.id.expandedListPrice) as TextView

            val decimal: NumberFormat = DecimalFormat("0.00")
            val item = getChild(orderPosition, itemPosition) as ItemDTO
            val price = item.price
            val priceAsString = "€ ${decimal.format(price)}"

            listItem.text = item.name
            listPrice.text = priceAsString

            return view
        }

        return view
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return sessionDTO.orders?.get(listPosition)?.items?.size ?: 0
    }

    override fun getGroup(listPosition: Int): Any {
        return titles?.get(listPosition) ?: 0
    }

    override fun getGroupCount(): Int {
        return titles?.size ?: 0
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(
        listPosition: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup
    ): View {

        var view = convertView

        if (view == null) {
            val layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = layoutInflater.inflate(R.layout.list_group, parent, false)

            val listTitle = view.findViewById<View>(R.id.listTitle) as TextView
            val listTotal = view.findViewById(R.id.listTotal) as TextView
            val listStatus = view.findViewById(R.id.listStatus) as TextView

            val title = getGroup(listPosition) as String
            val items = sessionDTO.orders?.get(listPosition)?.items

            val decimal: NumberFormat = DecimalFormat("0.00")
            val order = orders?.get(listPosition)
            var total = 0.0

            if (items != null) {
                for (i in items) {
                    total += i.price!!
                }
            }

            val totalAsString = "Total: € ${decimal.format(total)}"

            listTitle.text = title
            listTotal.text = totalAsString
            listStatus.text = order?.status.toString()

            listTitle.setTypeface(null, Typeface.BOLD)

            return view
        }

        return view
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }
}