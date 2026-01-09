package com.example.dummyshop.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.dummyshop.shared.domain.usecase.ReportBackgroundedUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class BackgroundReporter(
    private val appScope: CoroutineScope,
    private val reportBackgroundedUseCase: ReportBackgroundedUseCase
) : DefaultLifecycleObserver {

    override fun onStop(owner: LifecycleOwner) {
        appScope.launch {
            reportBackgroundedUseCase()
        }
    }
}
