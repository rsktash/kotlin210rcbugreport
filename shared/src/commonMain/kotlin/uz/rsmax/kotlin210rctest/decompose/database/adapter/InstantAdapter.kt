package uz.rsmax.kotlin210rctest.decompose.database.adapter

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.Instant

object InstantAdapter : ColumnAdapter<Instant, Long> {
  override fun decode(databaseValue: Long): Instant {
    return Instant.fromEpochMilliseconds(databaseValue)
  }

  override fun encode(value: Instant): Long {
    return value.toEpochMilliseconds()
  }
}
