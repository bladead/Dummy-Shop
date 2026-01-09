package com.dummyshop.shared.data.local

import com.dummyshop.shared.data.local.db.DummyShopDatabase

interface DatabaseFactory {
    fun create(): DummyShopDatabase
}
