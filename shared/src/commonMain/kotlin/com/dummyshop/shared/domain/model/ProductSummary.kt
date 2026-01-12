package com.dummyshop.shared.domain.model

data class ProductSummary(
    val id: Long,
    val title: String,
    val price: Double,
    val category: String,
    val isFavorite: Boolean
)
