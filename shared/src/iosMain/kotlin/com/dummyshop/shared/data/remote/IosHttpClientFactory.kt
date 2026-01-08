package com.dummyshop.shared.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

class IosHttpClientFactory : HttpClientFactory {
    override fun create(): HttpClient =
        HttpClient(Darwin) {
            installCommonPlugins()
        }
}
