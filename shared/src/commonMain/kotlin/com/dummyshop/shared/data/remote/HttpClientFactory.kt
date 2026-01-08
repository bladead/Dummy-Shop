package com.dummyshop.shared.data.remote

import io.ktor.client.HttpClient

interface HttpClientFactory {
    fun create(): HttpClient
}
