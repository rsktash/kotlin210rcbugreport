package uz.rsmax.kotlin210rctest.decompose.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import org.w3c.dom.Worker

actual fun createDriver(
  name: String,
  schema: SqlSchema<QueryResult.AsyncValue<Unit>>
): SqlDriver =
  WebWorkerDriver(Worker(js("new URL('sqlite.worker.js', import.meta.url)").unsafeCast<String>()).also {
    it.postMessage("${name}?vfs=opfs")
  })