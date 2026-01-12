package com.dummyshop.shared.data.remote.datasource

import com.dummyshop.shared.core.result.AppResult
import com.dummyshop.shared.core.result.map
import com.dummyshop.shared.data.remote.api.DummyShopApi
import com.dummyshop.shared.data.remote.dto.ProductDto
import com.dummyshop.shared.data.remote.dto.ProductsResponseDto

class ProductsRemoteDataSource(
    private val api: DummyShopApi
) {
    suspend fun fetchProducts(): AppResult<ProductsResponseDto> =
        api.getProducts()

    suspend fun fetchProduct(productId: Long): AppResult<ProductDto> =
        api.getProduct(productId)

    suspend fun reportAppBackgrounded(): AppResult<Unit> =
        api.reportBackground().map {}
}
