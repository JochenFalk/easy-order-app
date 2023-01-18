package com.easysystems.easyorder.viewModels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.easysystems.easyorder.MainActivity
import com.easysystems.easyorder.data.ItemDTO
import com.easysystems.easyorder.repositories.ItemRepository
import org.koin.java.KoinJavaComponent.inject

class ItemListViewModel : ViewModel() {

    private val itemRepository: ItemRepository by inject(ItemRepository::class.java)

    var itemList: MutableLiveData<ArrayList<ItemDTO>> = MutableLiveData()
    var dataRetrievalError: MutableLiveData<Boolean> = MutableLiveData()
    var updateBottomNavigation: MutableLiveData<Boolean> = MutableLiveData()
    var isEmptyList: MutableLiveData<Boolean> = MutableLiveData()

    init {
        retrieveData()
    }

    fun addItem(item: ItemDTO) {

        val session = MainActivity.sessionDTO
        val order = session?.orders?.last()

        if (session != null && order != null) {

            order.items?.add(item)
            order.total = item.price?.let { price -> order.total?.plus(price) }
            session.total = item.price?.let { price -> session.total?.plus(price) }
            MainActivity.sessionDTO = session

            Log.i("Info", "Item with id ${item.id} added to order ${order.id}")

            this.isEmptyList.value = order.items?.size != 0
            this.updateBottomNavigation.value = true
        }
    }

    private fun retrieveData() {

        itemRepository.retrieveItems { items ->

            if (items != null) {
                this.itemList.value = items
                this.dataRetrievalError.value = false
            } else {
                this.dataRetrievalError.value = true
            }
        }
    }
}