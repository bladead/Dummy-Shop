package com.dummyshop.shared.data.repository

import com.dummyshop.shared.core.result.AppError
import com.dummyshop.shared.core.result.AppResult
import com.dummyshop.shared.core.result.isRetryable
import com.dummyshop.shared.core.retry.RetryPolicy
import com.dummyshop.shared.core.retry.retryingAppResult
import com.dummyshop.shared.core.time.TimeProvider
import com.dummyshop.shared.data.local.datasource.ProductsLocalDataSource
import com.dummyshop.shared.data.remote.datasource.ProductsRemoteDataSource
import com.dummyshop.shared.data.remote.mapper.toLocalUpsertProduct
import com.dummyshop.shared.domain.model.ProductDetail
import com.dummyshop.shared.domain.model.ProductSummary
import com.dummyshop.shared.domain.model.SyncStatus
import com.dummyshop.shared.domain.repository.ProductsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class ProductsRepositoryImpl(
    private val localDataSource: ProductsLocalDataSource,
    private val remoteDataSource: ProductsRemoteDataSource,
    private val timeProvider: TimeProvider,
    private val retryPolicy: RetryPolicy = RetryPolicy.Default
) : ProductsRepository {

    override fun observeProducts(): Flow<List<ProductSummary>> =
        localDataSource.observeProducts()

    override fun observeProductDetail(productId: Long): Flow<ProductDetail?> =
        localDataSource.observeProductDetail(productId)

    override fun observeSyncStatus(): Flow<SyncStatus> =
        localDataSource.observeSyncStatus()

    override suspend fun refreshProducts(): AppResult<Unit> {
        val nowMs = timeProvider.nowMs()

        val remoteResult = retryingAppResult(
            policy = retryPolicy,
            shouldRetry = AppError::isRetryable
        ) {
            remoteDataSource.fetchProducts()
        }

        return when (remoteResult) {
            is AppResult.Success -> {
                val products = remoteResult.value.products.map { productDto ->
                    productDto.toLocalUpsertProduct()
                }

                safeLocalCall {
                    localDataSource.upsertProducts(products = products, nowMs = nowMs)
                    localDataSource.markRefreshSuccess(nowMs)
                }
            }

            is AppResult.Failure -> {
                safeLocalCall { localDataSource.markRefreshFailure(nowMs) }
                AppResult.Failure(remoteResult.error)
            }
        }
    }

    override suspend fun refreshProduct(productId: Long): AppResult<Unit> {
        val nowMs = timeProvider.nowMs()

        val remoteResult = retryingAppResult(
            policy = retryPolicy,
            shouldRetry = AppError::isRetryable
        ) {
            remoteDataSource.fetchProduct(productId)
        }

        return when (remoteResult) {
            is AppResult.Success -> {
                val product = remoteResult.value.toLocalUpsertProduct()

                safeLocalCall {
                    localDataSource.upsertProduct(product = product, nowMs = nowMs)
                    localDataSource.markRefreshSuccess(nowMs)
                }
            }

            is AppResult.Failure -> {
                safeLocalCall { localDataSource.markRefreshFailure(nowMs) }
                AppResult.Failure(remoteResult.error)
            }
        }
    }

    override suspend fun toggleFavorite(productId: Long): AppResult<Unit> {
        val nowMs = timeProvider.nowMs()
        return safeLocalCall {
            localDataSource.toggleFavorite(productId = productId, nowMs = nowMs)
        }
    }

    override suspend fun reportBackgrounded(): AppResult<Unit> =
        remoteDataSource.reportAppBackgrounded()
}

private suspend inline fun safeLocalCall(
    crossinline block: suspend () -> Unit
): AppResult<Unit> {
    return try {
        block()
        AppResult.Success(Unit)
    } catch (cancellationException: CancellationException) {
        throw cancellationException
    } catch (throwable: Throwable) {
        AppResult.Failure(AppError.Data.Unknown(throwable.message))
    }
}
