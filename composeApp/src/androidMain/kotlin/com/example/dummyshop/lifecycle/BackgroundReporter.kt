package com.example.dummyshop.lifecycle

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.dummyshop.shared.domain.usecase.ReportBackgroundedUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


private const val TAG = "BackgroundReporter"

class BackgroundReporter(
    private val appScope: CoroutineScope,
    private val reportBackgroundedUseCase: ReportBackgroundedUseCase,
) : DefaultLifecycleObserver {

    override fun onStop(owner: LifecycleOwner) {
        appScope.launch {
            runCatching {
                val result = reportBackgroundedUseCase()
                Log.d(TAG, "reportBackgrounded result=$result")
            }.onFailure {
                Log.e(TAG, "reportBackgrounded failed", it)
            }
        }
    }
}
