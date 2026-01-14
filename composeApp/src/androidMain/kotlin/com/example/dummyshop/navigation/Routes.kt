package com.example.dummyshop.navigation

object Routes {
    const val Products = "products"
    const val ProductDetail = "product/{id}"

    fun productDetail(productId: Long): String =
        ProductDetail.replace("{id}", productId.toString())
}
