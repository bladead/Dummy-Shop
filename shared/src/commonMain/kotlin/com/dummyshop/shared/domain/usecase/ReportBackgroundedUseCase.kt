package com.dummyshop.shared.domain.usecase

import com.dummyshop.shared.domain.repository.ProductsRepository

class ReportBackgroundedUseCase(
    private val repository: ProductsRepository
) {
    suspend operator fun invoke() = repository.reportBackgrounded()
}
