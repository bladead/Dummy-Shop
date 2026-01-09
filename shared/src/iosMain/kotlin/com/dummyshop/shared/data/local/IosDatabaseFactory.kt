package com.dummyshop.shared.data.local

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.dummyshop.shared.data.local.db.DummyShopDatabase

class IosDatabaseFactory : DatabaseFactory {
    override fun create(): DummyShopDatabase =
        DummyShopDatabase(
            driver = NativeSqliteDriver(
                schema = DummyShopDatabase.Schema,
                name = "dummyshop.db"
            )
        )
}
