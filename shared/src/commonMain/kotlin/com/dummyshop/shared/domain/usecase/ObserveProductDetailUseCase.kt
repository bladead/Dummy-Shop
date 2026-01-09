package com.dummyshop.shared.domain.usecase

import com.dummyshop.shared.domain.repository.ProductsRepository

class ObserveProductDetailUseCase(
    private val repository: ProductsRepository
) {
    operator fun invoke(productId: Long) = repository.observeProductDetail(productId)
}
