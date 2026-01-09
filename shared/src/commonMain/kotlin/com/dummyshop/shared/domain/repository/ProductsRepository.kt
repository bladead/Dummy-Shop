package com.dummyshop.shared.domain.repository

import com.dummyshop.shared.core.result.AppResult
import com.dummyshop.shared.domain.model.ProductDetail
import com.dummyshop.shared.domain.model.ProductSummary
import com.dummyshop.shared.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface ProductsRepository {
    fun observeProducts(): Flow<List<ProductSummary>>
    fun observeProductDetail(productId: Long): Flow<ProductDetail?>
    fun observeSyncStatus(): Flow<SyncStatus>

    suspend fun refreshProducts(): AppResult<Unit>
    suspend fun refreshProduct(productId: Long): AppResult<Unit>

    suspend fun toggleFavorite(productId: Long): AppResult<Unit>
}
