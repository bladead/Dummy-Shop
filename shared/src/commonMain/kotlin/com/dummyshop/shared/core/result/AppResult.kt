package com.dummyshop.shared.core.result

sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

inline fun <T, R> AppResult<T>.fold(
    onSuccess: (T) -> R,
    onFailure: (AppError) -> R
): R = when (this) {
    is AppResult.Success -> onSuccess(value)
    is AppResult.Failure -> onFailure(error)
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> =
    when (this) {
        is AppResult.Success -> AppResult.Success(transform(value))
        is AppResult.Failure -> this
    }

inline fun <T> AppResult<T>.mapError(transform: (AppError) -> AppError): AppResult<T> =
    when (this) {
        is AppResult.Success -> this
        is AppResult.Failure -> AppResult.Failure(transform(error))
    }


inline fun <T> AppResult<T>.getOrNull(): T? =
    (this as? AppResult.Success)?.value

inline fun <T> AppResult<T>.errorOrNull(): AppError? =
    (this as? AppResult.Failure)?.error
