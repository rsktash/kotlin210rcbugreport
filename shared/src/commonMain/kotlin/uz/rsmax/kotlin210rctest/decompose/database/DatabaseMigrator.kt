package uz.rsmax.kotlin210rctest.decompose.database

interface DatabaseMigrator {
  suspend fun migrateIfNeeded()

  companion object : DatabaseMigrator {
    override suspend fun migrateIfNeeded() {
      TODO("Not yet implemented")
    }
  }
}