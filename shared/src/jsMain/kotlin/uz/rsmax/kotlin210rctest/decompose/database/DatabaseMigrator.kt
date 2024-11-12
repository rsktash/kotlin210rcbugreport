package uz.rsmax.kotlin210rctest.decompose.database

import app.cash.sqldelight.async.coroutines.awaitQuery
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import io.github.aakira.napier.Napier

fun DatabaseMigrator(
  driver: SqlDriver,
  schema: SqlSchema<QueryResult.AsyncValue<Unit>>,
): DatabaseMigrator = DatabaseMigratorImpl(driver, schema)

private class DatabaseMigratorImpl(
  private val driver: SqlDriver,
  private val schema: SqlSchema<QueryResult.AsyncValue<Unit>>,
) : DatabaseMigrator {
  override suspend fun migrateIfNeeded() {
    driver.execute(null, "CREATE TABLE IF NOT EXISTS db_migrations(version INT UNIQUE);", 0).await()
    val oldVersion =
      driver.awaitQuery(
        null,
        //        "PRAGMA $VERSION_PRAGMA",
        "SELECT version FROM db_migrations ORDER BY version DESC LIMIT 1",
        mapper = { cursor ->
          if (cursor.next().await()) {
            val vers = cursor.getLong(0)
            vers?.toInt()
          } else {
            null
          }
        },
        0,
      ) ?: 0
    Napier.d("PREV DB VERSION $oldVersion")

    val newVersion = schema.version

    Napier.d("CUR. DB VERSION $newVersion")

    if (oldVersion == 0) {
      schema.create(driver).await()
      driver.execute(null, "INSERT INTO db_migrations(version) VALUES($newVersion)", 0).await()
      //      driver.execute(null, "PRAGMA $VERSION_PRAGMA=$newVersion", 0).await()
    } else if (oldVersion < newVersion) {
      schema.migrate(driver, oldVersion.toLong(), newVersion).await()
      //      driver.execute(null, "PRAGMA $VERSION_PRAGMA=$newVersion", 0).await()
      driver.execute(null, "INSERT INTO db_migrations(version) VALUES($newVersion)", 0).await()
    }
  }

  companion object {
    private const val VERSION_PRAGMA = "user_version"
  }
}