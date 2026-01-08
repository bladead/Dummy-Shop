package com.dummyshop.shared.data.remote

import com.dummyshop.shared.core.result.AppError
import com.dummyshop.shared.core.result.AppResult
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

internal suspend inline fun <reified T> safeApiCall(
    crossinline request: suspend () -> HttpResponse
): AppResult<T> {
    return try {
        val response = request()
        val statusCode = response.status.value

        if (statusCode in 200..299) {
            AppResult.Success(response.body<T>())
        } else {
            val responseBody = runCatching { response.bodyAsText() }.getOrNull()
            AppResult.Failure(mapHttpStatusToError(statusCode, responseBody))
        }
    } catch (cancellationException: CancellationException) {
        throw cancellationException
    } catch (timeoutException: HttpRequestTimeoutException) {
        AppResult.Failure(AppError.Network.Timeout)
    } catch (timeoutException: ConnectTimeoutException) {
        AppResult.Failure(AppError.Network.Timeout)
    } catch (ioException: kotlinx.io.IOException) {
        AppResult.Failure(AppError.Network.NoInternet)
    } catch (serializationException: SerializationException) {
        AppResult.Failure(AppError.Data.Serialization(serializationException.message))
    } catch (throwable: Throwable) {
        AppResult.Failure(AppError.Unknown(throwable.message))
    }
}

private fun mapHttpStatusToError(statusCode: Int, responseBody: String?): AppError =
    when (statusCode) {
        429 -> AppError.Api.RateLimited(statusCode = statusCode, body = responseBody)
        in 400..499 -> AppError.Api.Client(statusCode = statusCode, body = responseBody)
        in 500..599 -> AppError.Api.Server(statusCode = statusCode, body = responseBody)
        else -> AppError.Api.Unexpected(statusCode = statusCode, body = responseBody)
    }
