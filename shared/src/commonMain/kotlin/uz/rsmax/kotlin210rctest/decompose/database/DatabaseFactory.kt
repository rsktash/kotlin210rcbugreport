package uz.rsmax.kotlin210rctest.decompose.database

import app.cash.sqldelight.db.SqlDriver
import uz.rsmax.AppDatabase
import uz.rsmax.AppUserTable
import uz.rsmax.ProductTable
import uz.rsmax.kotlin210rctest.decompose.UserId
import uz.rsmax.kotlin210rctest.decompose.app.product.ProductId
import uz.rsmax.kotlin210rctest.decompose.database.adapter.InstantAdapter
import uz.rsmax.kotlin210rctest.decompose.database.adapter.ObjectIdColumnAdapter


fun createAppDatabase(driver: SqlDriver): AppDatabase = AppDatabase(
  driver = driver,
  ProductTableAdapter = ProductTable.Adapter(
    idAdapter = ObjectIdColumnAdapter(::ProductId),
    createdAtAdapter = InstantAdapter,
    modifiedAtAdapter = InstantAdapter,
  ),
  AppUserTableAdapter = AppUserTable.Adapter(
    idAdapter = ObjectIdColumnAdapter(::UserId)
  )
)