package com.dummyshop.shared.di

import com.dummyshop.shared.core.coroutines.AppDispatchers
import com.dummyshop.shared.core.coroutines.IosAppDispatchers
import com.dummyshop.shared.core.retry.RetryPolicy
import com.dummyshop.shared.core.time.IosTimeProvider
import com.dummyshop.shared.core.time.TimeProvider
import com.dummyshop.shared.data.local.DatabaseFactory
import com.dummyshop.shared.data.local.IosDatabaseFactory
import com.dummyshop.shared.data.local.datasource.ProductsLocalDataSource
import com.dummyshop.shared.data.remote.HttpClientFactory
import com.dummyshop.shared.data.remote.IosHttpClientFactory
import org.koin.core.module.Module
import org.koin.dsl.module

fun iosPlatformModule(): Module = module {
    single<AppDispatchers> { IosAppDispatchers() }
    single<TimeProvider> { IosTimeProvider() }
    single { RetryPolicy.Default }

    single<HttpClientFactory> { IosHttpClientFactory() }

    single<DatabaseFactory> { IosDatabaseFactory() }
    single { get<DatabaseFactory>().create() }
    single { ProductsLocalDataSource(database = get(), dispatchers = get()) }
}
