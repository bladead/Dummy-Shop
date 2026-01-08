package com.dummyshop.shared.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

class AndroidHttpClientFactory : HttpClientFactory {
    override fun create(): HttpClient =
        HttpClient(OkHttp) {
            installCommonPlugins()
        }
}
