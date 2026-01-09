package com.dummyshop.shared.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class BackgroundPingDto(
    val title: String,
    val description: String,
    val price: Int,
    val category: String
)
