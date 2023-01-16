package com.easysystems.easyorder.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.easysystems.easyorder.data.ItemDTO
import com.easysystems.easyorder.repositories.ItemRepository
import org.koin.java.KoinJavaComponent.inject

class ItemListViewModel : ViewModel() {

    private val itemRepository: ItemRepository by inject(ItemRepository::class.java)

    private var itemList: MutableLiveData<ArrayList<ItemDTO>> = MutableLiveData()
    private var itemListError: MutableLiveData<Boolean> = MutableLiveData()

    init {
        getAllItems()
    }

    fun getItemList(): LiveData<ArrayList<ItemDTO>> {
        return itemList
    }

    fun getItemListError(): LiveData<Boolean> {
        return itemListError
    }

    private fun getAllItems() {

        itemRepository.retrieveItems { items ->

            if (items != null) {
                this.itemList.value = items
                this.itemListError.value = false
            } else {
                this.itemListError.value = true
            }
        }
    }
}