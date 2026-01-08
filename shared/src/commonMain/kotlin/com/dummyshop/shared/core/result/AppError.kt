package com.dummyshop.shared.core.result

sealed interface AppError {

    sealed interface Network : AppError {
        data object NoInternet : Network
        data object Timeout : Network
        data class Unknown(val message: String?) : Network
    }

    sealed interface Api : AppError {
        val statusCode: Int
        val body: String?

        data class Client(
            override val statusCode: Int,
            override val body: String?
        ) : Api

        data class Server(
            override val statusCode: Int,
            override val body: String?
        ) : Api

        data class RateLimited(
            override val statusCode: Int,
            override val body: String?
        ) : Api

        data class Unexpected(
            override val statusCode: Int,
            override val body: String?
        ) : Api
    }

    sealed interface Data : AppError {
        data class Serialization(val message: String?) : Data
        data class Unknown(val message: String?) : Data
    }

    data class Unknown(val message: String?) : AppError
}

fun AppError.isRetryable(): Boolean =
    when (this) {
        is AppError.Network.NoInternet -> true
        is AppError.Network.Timeout -> true
        is AppError.Network.Unknown -> true
        is AppError.Api.Server -> true
        is AppError.Api.RateLimited -> true
        is AppError.Api.Unexpected -> true
        is AppError.Api.Client -> false
        is AppError.Data.Serialization -> false
        is AppError.Data.Unknown -> false
        is AppError.Unknown -> false
    }
