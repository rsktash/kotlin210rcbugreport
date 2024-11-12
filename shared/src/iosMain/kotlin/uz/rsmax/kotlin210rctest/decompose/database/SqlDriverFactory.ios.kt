package uz.rsmax.kotlin210rctest.decompose.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual fun createDriver(
  name: String,
  schema: SqlSchema<QueryResult.AsyncValue<Unit>>
): SqlDriver {
  return NativeSqliteDriver(schema.synchronous(), name)
}