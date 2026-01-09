package com.dummyshop.shared.domain.usecase

import com.dummyshop.shared.domain.repository.ProductsRepository

class ObserveSyncStatusUseCase(
    private val repository: ProductsRepository
) {
    operator fun invoke() = repository.observeSyncStatus()
}
