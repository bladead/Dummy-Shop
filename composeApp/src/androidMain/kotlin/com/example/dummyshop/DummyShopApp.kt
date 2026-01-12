package com.example.dummyshop

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.dummyshop.di.androidUiModule
import com.example.dummyshop.lifecycle.BackgroundReporter
import com.dummyshop.shared.di.androidPlatformModule
import com.dummyshop.shared.di.sharedModule
import com.dummyshop.shared.domain.usecase.ReportBackgroundedUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DummyShopApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@DummyShopApp)
            modules(
                sharedModule(),
                androidPlatformModule(),
                androidUiModule()
            )
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(
            BackgroundReporter(
                appScope = appScope,
                reportBackgroundedUseCase = get<ReportBackgroundedUseCase>()
            )
        )
    }
}
