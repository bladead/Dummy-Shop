package com.dummyshop.shared.core.result

fun <T> AppResult<T>.toUnit(): AppResult<Unit> =
    when (this) {
        is AppResult.Success -> AppResult.Success(Unit)
        is AppResult.Failure -> AppResult.Failure(error)
    }
