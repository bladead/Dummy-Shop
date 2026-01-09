package com.dummyshop.shared.domain.usecase

import com.dummyshop.shared.domain.repository.ProductsRepository

class ToggleFavoriteUseCase(
    private val repository: ProductsRepository
) {
    suspend operator fun invoke(productId: Long) = repository.toggleFavorite(productId)
}
