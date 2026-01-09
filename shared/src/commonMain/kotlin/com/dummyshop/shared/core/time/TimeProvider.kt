package com.dummyshop.shared.core.time

fun interface TimeProvider {
    fun nowMs(): Long
}
