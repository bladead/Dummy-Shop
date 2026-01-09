package com.dummyshop.shared.core.time

class AndroidTimeProvider : TimeProvider {
    override fun nowMs(): Long = System.currentTimeMillis()
}
