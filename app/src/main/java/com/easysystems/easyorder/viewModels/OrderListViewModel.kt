package com.easysystems.easyorder.viewModels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.easysystems.easyorder.MainActivity
import com.easysystems.easyorder.data.OrderDTO

class OrderListViewModel : ViewModel() {

    var orderList: MutableLiveData<ArrayList<OrderDTO>> = MutableLiveData()
    var titleList: MutableLiveData<ArrayList<String>> = MutableLiveData()
    var dataRefreshError: MutableLiveData<Boolean> = MutableLiveData()
    var orderIsClearedSuccess: MutableLiveData<Boolean> = MutableLiveData()
    var orderIsSentSuccess: MutableLiveData<Boolean> = MutableLiveData()
    var updateBottomNavigation: MutableLiveData<Boolean> = MutableLiveData()

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
                    lastOrder.status = OrderDTO.Status.CANCELED

                    this.orderIsClearedSuccess.value = true
                    refreshData()

                    Log.i("Info", "Order with id ${lastOrder.id} has been cleared")
                }
            } else {
                this.orderIsClearedSuccess.value = false
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
                this.orderIsSentSuccess.value = false
            }
        }
    }

    private fun refreshData() {

        var count = 0
        val orders = MainActivity.sessionDTO?.orders
        val titles = ArrayList<String>()

        if (orders != null || orders?.size == 0) {

            this.titleList.value?.clear()

            orders.sortBy { it.id }
            orders.forEach { order ->

                if ((order.items?.size != 0) && (order.total != 0.0)) {
                    count++
                    titles.add("Order $count")
                }
            }

            this.titleList.value = titles
            this.orderList.value = orders

            this.dataRefreshError.value = false
            this.updateBottomNavigation.value = true

        } else {
            this.dataRefreshError.value = true
        }
    }
}