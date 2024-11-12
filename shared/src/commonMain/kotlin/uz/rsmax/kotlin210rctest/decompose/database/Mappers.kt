package uz.rsmax.kotlin210rctest.decompose.database

import uz.rsmax.AppUserTable
import uz.rsmax.ProductTable
import uz.rsmax.kotlin210rctest.decompose.AppUser
import uz.rsmax.kotlin210rctest.decompose.app.product.ProductItem

val userTableToData: (AppUserTable) -> AppUser = {
  AppUser(
    id = it.id,
    name = it.name,
    email = it.email,
  )
}

val productTableToData: (ProductTable) -> ProductItem = {
  ProductItem(
    id = it.id,
    name = it.name,
    description = it.description,
    price = it.price,
    isDeleted = it.isDeleted,
    createdAt = it.createdAt,
    modifiedAt = it.modifiedAt,
  )
}