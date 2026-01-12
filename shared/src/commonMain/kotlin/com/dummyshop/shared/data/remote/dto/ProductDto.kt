package com.dummyshop.shared.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Long,
    val title: String,
    val description: String = "",
    val price: Double,
    val category: String,
    val thumbnail: String? = null
)
