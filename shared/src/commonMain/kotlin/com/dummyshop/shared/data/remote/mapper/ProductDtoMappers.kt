package com.dummyshop.shared.data.remote.mapper

import com.dummyshop.shared.data.local.datasource.LocalUpsertProduct
import com.dummyshop.shared.data.remote.dto.ProductDto

internal fun ProductDto.toLocalUpsertProduct(): LocalUpsertProduct =
    LocalUpsertProduct(
        id = id,
        title = title,
        description = description,
        price = price,
        category = category,
        thumbnailUrl = thumbnail
    )
