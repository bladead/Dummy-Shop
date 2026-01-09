package com.dummyshop.shared.domain.usecase

import com.dummyshop.shared.domain.repository.ProductsRepository

class ObserveProductsUseCase(
    private val repository: ProductsRepository
) {
    operator fun invoke() = repository.observeProducts()
}
