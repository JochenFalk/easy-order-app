package com.easysystems.easyorder.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import com.easysystems.easyorder.data.ItemObservable
import com.easysystems.easyorder.data.OrderObservable
import com.easysystems.easyorder.databinding.ListGroupBinding
import com.easysystems.easyorder.databinding.ListItemBinding

class OrderListAdapter : BaseExpandableListAdapter() {

    var orderList = ArrayList<OrderObservable>()

    override fun getChild(orderPosition: Int, itemPosition: Int): Any {
        return orderList[orderPosition].items[itemPosition]
    }

    override fun getChildId(orderPosition: Int, itemPosition: Int): Long {
        return itemPosition.toLong()
    }

    override fun getChildView(
        orderPosition: Int, itemPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup
    ): View {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Using data-binding in layout causes flickering of list!?
        binding.itemObservable = getChild(orderPosition, itemPosition) as ItemObservable
        return binding.root
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return orderList[listPosition].items.size
    }

    override fun getGroup(listPosition: Int): Any {
        return orderList[listPosition].title.toString()
    }

    override fun getGroupCount(): Int {
        return orderList.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(
        listPosition: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup
    ): View {
        val binding = ListGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Using data-binding in layout causes flickering of list!?
        binding.orderObservable = orderList[listPosition]
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