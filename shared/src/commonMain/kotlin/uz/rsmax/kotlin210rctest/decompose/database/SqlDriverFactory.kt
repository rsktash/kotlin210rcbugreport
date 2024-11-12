package uz.rsmax.kotlin210rctest.decompose.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema

expect fun createDriver(name: String, schema: SqlSchema<QueryResult.AsyncValue<Unit>>): SqlDriver

internal const val DB_NAME = "decompose_test_db"
