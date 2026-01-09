package com.dummyshop.shared.data.remote.api

import com.dummyshop.shared.core.result.AppResult
import com.dummyshop.shared.data.remote.dto.BackgroundPingDto
import com.dummyshop.shared.data.remote.dto.ProductDto
import com.dummyshop.shared.data.remote.dto.ProductsResponseDto
import com.dummyshop.shared.data.remote.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class DummyShopApi(
    private val httpClient: HttpClient,
    private val baseUrl: String
) {
    suspend fun getProducts(): AppResult<ProductsResponseDto> =
        safeApiCall {
            httpClient.get(urlString = "$baseUrl/products")
        }

    suspend fun getProduct(productId: Long): AppResult<ProductDto> =
        safeApiCall {
            httpClient.get(urlString = "$baseUrl/products/$productId")
        }

    suspend fun reportBackground(): AppResult<ProductDto> =
        safeApiCall {
            httpClient.post(urlString = "$baseUrl/products/add") {
                setBody(
                    BackgroundPingDto(
                        title = "BackgroundPing",
                        description = "App moved to background",
                        price = 0,
                        category = "internal"
                    )
                )
            }
        }
}
