package com.easysystems.easyorder.viewModels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.easysystems.easyorder.MainActivity
import com.easysystems.easyorder.data.ItemDTO
import com.easysystems.easyorder.data.ItemObservable
import com.easysystems.easyorder.data.OrderDTO
import com.easysystems.easyorder.data.OrderObservable
import java.text.DecimalFormat
import java.text.NumberFormat

class OrderListViewModel : ViewModel() {

    var orderObservableList: MutableLiveData<ArrayList<OrderObservable>> = MutableLiveData()
    var dataRefreshError: MutableLiveData<Boolean> = MutableLiveData()
    var orderIsClearedSuccess: MutableLiveData<Boolean> = MutableLiveData()
    var orderIsClearedFailed: MutableLiveData<Boolean> = MutableLiveData()
    var orderIsSentSuccess: MutableLiveData<Boolean> = MutableLiveData()
    var orderIsSentFailed: MutableLiveData<Boolean> = MutableLiveData()
    var updateBottomNavigation: MutableLiveData<Boolean> = MutableLiveData()
    var isEmptyList: MutableLiveData<Boolean> = MutableLiveData()

    init {
        refreshData()
    }

    fun clearOrder() {

        val session = MainActivity.sessionDTO
        val lastOrder = session?.orders?.last()

        if (session != null && lastOrder != null) {
            if (lastOrder.items?.size != 0) {

                var sessionTotal = session.total
                val orderTotal = lastOrder.total

                if (sessionTotal != null && orderTotal != null) {
                    sessionTotal -= orderTotal

                    session.total = sessionTotal
                    lastOrder.total = 0.0
                    lastOrder.items?.clear()
                    lastOrder.status = OrderDTO.Status.OPENED

                    this.orderIsClearedSuccess.value = true
                    refreshData()

                    Log.i("Info", "Order with id ${lastOrder.id} has been cleared")
                }
            } else {
                this.orderIsClearedFailed.value = true
            }
        }
    }

    fun sendOrder() {

        val sessionDTO = MainActivity.sessionDTO
        val lastOrder = sessionDTO?.orders?.last()

        if (lastOrder != null) {
            if (lastOrder.items?.size != 0) {
                lastOrder.status = OrderDTO.Status.SENT

                this.orderIsSentSuccess.value = true
                refreshData()

                Log.i("Info", "Order with id ${lastOrder.id} has been sent")
            } else {
                this.orderIsSentFailed.value = true
            }
        }
    }

    fun refreshData() {

        var count = 0
        val orders = MainActivity.sessionDTO?.orders
        val newOrderList: ArrayList<OrderObservable> = ArrayList()

        if (orders != null || orders?.size == 0) {

            orders.forEach { order ->

                if ((order.items?.size != 0) && (order.total != 0.0)) {

                    val items: ArrayList<ItemDTO>? = order.items
                    val newItemList: ArrayList<ItemObservable> = ArrayList()

                    val orderObservable = OrderObservable()
                    val decimal: NumberFormat = DecimalFormat("0.00")
                    val totalAsString = "€ ${decimal.format(order.total)}"

                    count++

                    orderObservable.id = order.id
                    orderObservable.status = order.status.toString()

                    items?.forEach { item ->

                        val itemObservable = ItemObservable()
                        val priceAsString = "€ ${decimal.format(item.price)}"

                        itemObservable.id = item.id
                        itemObservable.name = item.name
                        itemObservable.category = item.category.toString()
                        itemObservable.price = priceAsString

                        newItemList.add(itemObservable)
                        newOrderList.sortBy { it.id }
                    }

                    orderObservable.items = newItemList
                    orderObservable.total = totalAsString
                    orderObservable.sessionId = order.sessionId
                    orderObservable.title = "Order $count"

                    newOrderList.add(orderObservable)
                    newOrderList.sortBy { it.id }
                }
            }

            if (newOrderList.size != 0) {
                this.isEmptyList.value = newOrderList[0].items.size == 0
            } else {
                this.isEmptyList.value = true
            }

            this.orderObservableList.value = newOrderList
            this.updateBottomNavigation.value = true
        } else {
            this.dataRefreshError.value = true
        }
    }
}