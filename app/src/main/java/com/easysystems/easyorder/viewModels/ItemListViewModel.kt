package com.easysystems.easyorder.viewModels

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.easysystems.easyorder.MainActivity
import com.easysystems.easyorder.R
import com.easysystems.easyorder.data.ItemDTO
import com.easysystems.easyorder.data.ItemObservable
import com.easysystems.easyorder.helpclasses.StringResourcesProvider
import com.easysystems.easyorder.repositories.ItemRepository
import org.koin.java.KoinJavaComponent.inject
import java.text.DecimalFormat
import java.text.NumberFormat

class ItemListViewModel : ViewModel() {

    private val stringResourcesProvider: StringResourcesProvider by inject(StringResourcesProvider::class.java)
    private val itemRepository: ItemRepository by inject(ItemRepository::class.java)

    private var itemList: ArrayList<ItemDTO> = ArrayList()

    var itemObservable: MutableLiveData<ItemObservable> = MutableLiveData()
    var itemObservableList: MutableLiveData<ArrayList<ItemObservable>> = MutableLiveData()
    var dataRetrievalError: MutableLiveData<Boolean> = MutableLiveData()
    var updateBottomNavigation: MutableLiveData<Boolean> = MutableLiveData()
    var isEmptyList: MutableLiveData<Boolean> = MutableLiveData()

    var valuta: String = stringResourcesProvider.getString(R.string.default_valuta)
    var isEditMode: Boolean = false

    init {
        this.updateBottomNavigation.value = true
        retrieveData()
    }

    fun addItem(itemObservable: ItemObservable) {

        val session = MainActivity.sessionDTO
        val order = session?.orders?.last()
        val item = itemList.find { item -> item.id == itemObservable.id }

        if (session != null && order != null) {

            if (item != null) {

                order.items?.add(item)
                order.total = item.price?.let { price -> order.total?.plus(price) }
                session.total = item.price?.let { price -> session.total?.plus(price) }

                Log.i("Info", "Item with id ${item.id} added to order ${order.id}")
            }

            MainActivity.sessionDTO = session

            this.isEmptyList.value = order.items?.size != 0
            this.updateBottomNavigation.value = true
        }
    }

    fun updateItemList(itemObservable: ItemObservable) {

        val newItemList = ArrayList<ItemObservable>().apply {
            itemObservableList.value?.let { this.addAll(it) }
        }
        val item = newItemList.find { item -> item.id == itemObservable.id }
        val decimal: NumberFormat = DecimalFormat("0.00")
        val priceFiltered = itemObservable.price?.replace(",", ".")
        val priceAsString = "${decimal.format(priceFiltered?.toDouble())}"

        if (item != null) {
            item.name = itemObservable.name
            item.image = itemObservable.image
            item.category = itemObservable.category
            item.price = priceAsString
            item.description = itemObservable.description
        }

        this.itemObservableList.value = newItemList
    }

    private fun retrieveData() {

        itemRepository.retrieveItems { items ->

            if (items != null) {

                val newItemList: ArrayList<ItemObservable> = ArrayList()

                items.forEach { item ->

                    val itemObservable = ItemObservable()
                    val decimal: NumberFormat = DecimalFormat("0.00")
                    val priceAsString = "${decimal.format(item.price)}"
                    val description = item.description ?: stringResourcesProvider.getString(R.string.cat_ipsum_dolor)

                    itemObservable.id = item.id
                    itemObservable.name = item.name
                    itemObservable.image =
                        stringResourcesProvider.getDrawable(R.drawable.default_image)
                    itemObservable.category = item.category.toString()
                    itemObservable.price = priceAsString
                    itemObservable.description = description

                    newItemList.add(itemObservable)
                    newItemList.sortBy { it.id }
                }

                this.itemList = items
                this.itemObservableList.value = newItemList
                this.dataRetrievalError.value = false
            } else {
                this.dataRetrievalError.value = true
            }
        }
    }
}