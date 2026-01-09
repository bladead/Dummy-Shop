package com.dummyshop.shared.domain.usecase

import com.dummyshop.shared.domain.repository.ProductsRepository

class RefreshProductUseCase(
    private val repository: ProductsRepository
) {
    suspend operator fun invoke(productId: Long) = repository.refreshProduct(productId)
}
