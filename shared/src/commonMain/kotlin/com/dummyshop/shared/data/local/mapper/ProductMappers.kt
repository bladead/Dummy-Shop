package com.dummyshop.shared.data.local.mapper

import com.dummyshop.shared.domain.model.ProductDetail
import com.dummyshop.shared.domain.model.ProductSummary
import com.dummyshop.shared.data.local.db.SelectAllProducts
import com.dummyshop.shared.data.local.db.SelectProductById

internal fun SelectAllProducts.toDomain(): ProductSummary =
    ProductSummary(
        id = id,
        title = title,
        price = price,
        category = category,
        isFavorite = is_favorite
    )

internal fun SelectProductById.toDomain(): ProductDetail =
    ProductDetail(
        id = id,
        title = title,
        description = description,
        price = price,
        category = category,
        thumbnailUrl = thumbnail,
        isFavorite = is_favorite
    )
