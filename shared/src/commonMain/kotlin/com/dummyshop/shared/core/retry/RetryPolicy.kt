package com.dummyshop.shared.core.retry

import com.dummyshop.shared.core.result.AppError
import com.dummyshop.shared.core.result.AppResult
import kotlinx.coroutines.delay
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class RetryPolicy(
    val maxAttempts: Int,
    val initialDelay: Duration,
    val backoffFactor: Double,
    val jitterRatio: Double
) {
    init {
        require(maxAttempts >= 1)
        require(backoffFactor >= 1.0)
        require(jitterRatio in 0.0..1.0)
    }

    companion object {
        val Default = RetryPolicy(
            maxAttempts = 2,
            initialDelay = 250.milliseconds,
            backoffFactor = 2.0,
            jitterRatio = 0.2
        )
    }
}

suspend inline fun <T> retryingAppResult(
    policy: RetryPolicy,
    crossinline shouldRetry: (AppError) -> Boolean,
    crossinline block: suspend () -> AppResult<T>
): AppResult<T> {
    var attemptIndex = 1
    var currentDelay = policy.initialDelay

    while (true) {
        val result = try {
            block()
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        }

        if (result is AppResult.Success) return result

        val error = (result as AppResult.Failure).error
        val canRetry = attemptIndex < policy.maxAttempts && shouldRetry(error)
        if (!canRetry) return result

        val jitterMsMax = (currentDelay.inWholeMilliseconds * policy.jitterRatio).toLong()
        val jitterMs = if (jitterMsMax > 0) Random.nextLong(0, jitterMsMax + 1) else 0L

        delay(currentDelay + jitterMs.milliseconds)
        currentDelay *= policy.backoffFactor
        attemptIndex++
    }
}

