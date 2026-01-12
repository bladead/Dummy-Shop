package com.example.dummyshop.di

import com.example.dummyshop.ui.detail.ProductDetailViewModel
import com.example.dummyshop.ui.list.ProductsListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun androidUiModule() = module {
    viewModel { ProductsListViewModel(get(), get(), get(), get()) }
    viewModel { ProductDetailViewModel(get(), get(), get()) }
}

