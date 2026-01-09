package com.dummyshop.shared.data.local

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.dummyshop.shared.data.local.db.DummyShopDatabase

class AndroidDatabaseFactory(
    private val context: Context
) : DatabaseFactory {
    override fun create(): DummyShopDatabase =
        DummyShopDatabase(
            driver = AndroidSqliteDriver(
                schema = DummyShopDatabase.Schema,
                context = context,
                name = "dummyshop.db"
            )
        )
}
