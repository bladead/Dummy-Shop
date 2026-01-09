package com.dummyshop.shared.di

import com.dummyshop.shared.core.coroutines.AndroidAppDispatchers
import com.dummyshop.shared.core.coroutines.AppDispatchers
import com.dummyshop.shared.core.retry.RetryPolicy
import com.dummyshop.shared.core.time.AndroidTimeProvider
import com.dummyshop.shared.core.time.TimeProvider
import com.dummyshop.shared.data.local.AndroidDatabaseFactory
import com.dummyshop.shared.data.local.DatabaseFactory
import com.dummyshop.shared.data.local.datasource.ProductsLocalDataSource
import com.dummyshop.shared.data.remote.AndroidHttpClientFactory
import com.dummyshop.shared.data.remote.HttpClientFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

fun androidPlatformModule(): Module = module {
    single<AppDispatchers> { AndroidAppDispatchers() }
    single<TimeProvider> { AndroidTimeProvider() }
    single { RetryPolicy.Default }

    single<HttpClientFactory> { AndroidHttpClientFactory() }

    single<DatabaseFactory> { AndroidDatabaseFactory(context = androidContext()) }
    single { get<DatabaseFactory>().create() }
    single { ProductsLocalDataSource(database = get(), dispatchers = get()) }
}
