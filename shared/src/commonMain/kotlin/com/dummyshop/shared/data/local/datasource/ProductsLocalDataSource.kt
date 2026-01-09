package com.dummyshop.shared.data.local.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.dummyshop.shared.core.coroutines.AppDispatchers
import com.dummyshop.shared.data.local.db.DummyShopDatabase
import com.dummyshop.shared.data.local.mapper.toDomain
import com.dummyshop.shared.data.local.meta.SyncMetaKeys
import com.dummyshop.shared.domain.model.ProductDetail
import com.dummyshop.shared.domain.model.ProductSummary
import com.dummyshop.shared.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ProductsLocalDataSource(
    private val database: DummyShopDatabase,
    private val dispatchers: AppDispatchers
) {

    fun observeProducts(): Flow<List<ProductSummary>> =
        database.dummyShopDatabaseQueries
            .selectAllProducts()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { productRows -> productRows.map { productRow -> productRow.toDomain() } }

    fun observeProductDetail(productId: Long): Flow<ProductDetail?> =
        database.dummyShopDatabaseQueries
            .selectProductById(productId)
            .asFlow()
            .mapToOneOrNull(dispatchers.io)
            .map { productRow -> productRow?.toDomain() }

    fun observeSyncStatus(): Flow<SyncStatus> {
        val lastSuccessMsFlow = observeMetaLongOrNull(SyncMetaKeys.LAST_SUCCESS_MS)
        val lastFailureMsFlow = observeMetaLongOrNull(SyncMetaKeys.LAST_FAILURE_MS)

        return combine(lastSuccessMsFlow, lastFailureMsFlow) { lastSuccessMs, lastFailureMs ->
            when {
                lastSuccessMs == null && lastFailureMs == null -> SyncStatus.Unknown
                lastFailureMs != null && (lastSuccessMs == null || lastFailureMs > lastSuccessMs) ->
                    SyncStatus.Stale(lastSuccessAtMs = lastSuccessMs, lastFailureAtMs = lastFailureMs)
                lastSuccessMs != null -> SyncStatus.UpToDate(lastSuccessAtMs = lastSuccessMs)
                else -> SyncStatus.Unknown
            }
        }
    }

    suspend fun upsertProducts(
        products: List<LocalUpsertProduct>,
        nowMs: Long
    ) = withContext(dispatchers.io) {
        database.transaction {
            products.forEach { product ->
                database.dummyShopDatabaseQueries.upsertProduct(
                    id = product.id,
                    title = product.title,
                    description = product.description,
                    price = product.price.toLong(),
                    category = product.category,
                    thumbnail = product.thumbnailUrl,
                    updated_at_ms = nowMs
                )
            }
        }
    }

    suspend fun upsertProduct(
        product: LocalUpsertProduct,
        nowMs: Long
    ) = withContext(dispatchers.io) {
        database.dummyShopDatabaseQueries.upsertProduct(
            id = product.id,
            title = product.title,
            description = product.description,
            price = product.price.toLong(),
            category = product.category,
            thumbnail = product.thumbnailUrl,
            updated_at_ms = nowMs
        )
    }

    suspend fun toggleFavorite(
        productId: Long,
        nowMs: Long
    ) = withContext(dispatchers.io) {
        database.transaction {
            val isFavorite = database.dummyShopDatabaseQueries.isFavorite(productId).executeAsOne()
            if (isFavorite) {
                database.dummyShopDatabaseQueries.removeFavorite(productId)
            } else {
                database.dummyShopDatabaseQueries.addFavorite(productId, nowMs)
            }
        }
    }

    suspend fun markRefreshSuccess(nowMs: Long) = withContext(dispatchers.io) {
        database.dummyShopDatabaseQueries.setMeta(SyncMetaKeys.LAST_SUCCESS_MS, nowMs.toString())
    }

    suspend fun markRefreshFailure(nowMs: Long) = withContext(dispatchers.io) {
        database.dummyShopDatabaseQueries.setMeta(SyncMetaKeys.LAST_FAILURE_MS, nowMs.toString())
    }

    private fun observeMetaLongOrNull(key: String): Flow<Long?> =
        database.dummyShopDatabaseQueries
            .getMeta(key)
            .asFlow()
            .mapToOneOrNull(dispatchers.io)
            .map { metaValue -> metaValue?.toLongOrNull() }
}

data class LocalUpsertProduct(
    val id: Long,
    val title: String,
    val description: String,
    val price: Int,
    val category: String,
    val thumbnailUrl: String?
)
