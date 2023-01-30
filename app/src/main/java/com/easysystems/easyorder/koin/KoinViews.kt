package com.easysystems.easyorder.koin

import com.easysystems.easyorder.adapters.OrderListAdapter
import com.easysystems.easyorder.viewModels.ItemListViewModel
import com.easysystems.easyorder.viewModels.OrderListViewModel
import org.koin.dsl.module

val koinViews = module {
    single {
        ItemListViewModel()
    }
    single {
        OrderListViewModel()
    }
    single {
        OrderListAdapter()
    }
}