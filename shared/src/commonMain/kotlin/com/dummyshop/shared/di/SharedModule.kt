package com.dummyshop.shared.di

import com.dummyshop.shared.data.remote.api.ApiConfig
import com.dummyshop.shared.data.remote.api.DummyShopApi
import com.dummyshop.shared.data.remote.datasource.ProductsRemoteDataSource
import com.dummyshop.shared.data.repository.ProductsRepositoryImpl
import com.dummyshop.shared.domain.repository.ProductsRepository
import com.dummyshop.shared.domain.usecase.ObserveProductDetailUseCase
import com.dummyshop.shared.domain.usecase.ObserveProductsUseCase
import com.dummyshop.shared.domain.usecase.ObserveSyncStatusUseCase
import com.dummyshop.shared.domain.usecase.RefreshProductUseCase
import com.dummyshop.shared.domain.usecase.RefreshProductsUseCase
import com.dummyshop.shared.domain.usecase.ReportBackgroundedUseCase
import com.dummyshop.shared.domain.usecase.ToggleFavoriteUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

fun sharedModule(): Module = module {
    single { get<com.dummyshop.shared.data.remote.HttpClientFactory>().create() }
    single { DummyShopApi(httpClient = get(), baseUrl = ApiConfig.BASE_URL) }
    single { ProductsRemoteDataSource(api = get()) }

    single<ProductsRepository> {
        ProductsRepositoryImpl(
            localDataSource = get(),
            remoteDataSource = get(),
            timeProvider = get(),
            retryPolicy = get()
        )
    }

    factory { ObserveProductsUseCase(repository = get()) }
    factory { ObserveProductDetailUseCase(repository = get()) }
    factory { ObserveSyncStatusUseCase(repository = get()) }
    factory { RefreshProductsUseCase(repository = get()) }
    factory { RefreshProductUseCase(repository = get()) }
    factory { ToggleFavoriteUseCase(repository = get()) }
    factory { ReportBackgroundedUseCase(repository = get()) }
}
