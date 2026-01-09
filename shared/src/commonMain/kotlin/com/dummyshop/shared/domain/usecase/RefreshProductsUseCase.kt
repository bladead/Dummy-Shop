package com.dummyshop.shared.domain.usecase

import com.dummyshop.shared.domain.repository.ProductsRepository

class RefreshProductsUseCase(
    private val repository: ProductsRepository
) {
    suspend operator fun invoke() = repository.refreshProducts()
}
