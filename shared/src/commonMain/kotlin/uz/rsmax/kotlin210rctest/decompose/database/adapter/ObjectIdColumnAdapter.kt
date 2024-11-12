package uz.rsmax.kotlin210rctest.decompose.database.adapter

import app.cash.sqldelight.ColumnAdapter
import uz.rsmax.kotlin210rctest.decompose.database.ObjectId

@Suppress("FunctionName")
inline fun <reified T : ObjectId> ObjectIdColumnAdapter(crossinline decoder: (Long) -> T) =
  object : ColumnAdapter<T, Long> {
    override fun decode(databaseValue: Long): T = decoder(databaseValue)

    override fun encode(value: T): Long {
      return value.id
    }
  }

