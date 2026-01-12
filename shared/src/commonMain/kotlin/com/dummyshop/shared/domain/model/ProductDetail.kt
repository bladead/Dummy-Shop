package com.dummyshop.shared.domain.model

data class ProductDetail(
    val id: Long,
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val thumbnailUrl: String?,
    val isFavorite: Boolean
)
