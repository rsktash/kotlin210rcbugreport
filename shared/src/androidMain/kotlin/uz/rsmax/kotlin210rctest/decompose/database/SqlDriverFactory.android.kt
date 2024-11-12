package uz.rsmax.kotlin210rctest.decompose.database

import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import uz.rsmax.kotlin210rctest.decompose.appContext
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory

actual fun createDriver(
  name: String,
  schema: SqlSchema<QueryResult.AsyncValue<Unit>>
): SqlDriver {
  val syncSchema = schema.synchronous()
  return AndroidSqliteDriver(
    schema = syncSchema,
    context = appContext,
    name = name,
    callback = PragmaCallback(syncSchema),
    factory = RequerySQLiteOpenHelperFactory(),
  )
}


private class PragmaCallback(schema: SqlSchema<QueryResult.Value<Unit>>) :
  AndroidSqliteDriver.Callback(schema) {
  override fun onConfigure(db: SupportSQLiteDatabase) {
    super.onConfigure(db)
    db.enableWriteAheadLogging()
  }

  override fun onCreate(db: SupportSQLiteDatabase) {
    super.onCreate(db)
    db.query("PRAGMA recursive_triggers = OFF;")
    db.query("PRAGMA journal_mode = WAL;")
  }
}