package com.example.dummyshop

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform