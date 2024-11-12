package uz.rsmax.kotlin210rctest.decompose.app.product

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import uz.rsmax.kotlin210rctest.decompose.database.ObjectId
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class ProductId(override val id: Long) : ObjectId {

  fun isNew(): Boolean = this == NewId

  companion object {
    val NewId = ProductId(Long.MIN_VALUE)
  }
}

data class ProductItem(
  val id: ProductId,
  val name: String,
  val description: String,
  val price: Double,
  val isDeleted: Boolean,
  val createdAt: Instant,
  val modifiedAt: Instant,
) {
  companion object {
    fun createNew(): ProductItem = ProductItem(
      id = ProductId.NewId,
      name = "",
      description = "",
      price = 0.0,
      isDeleted = false,
      createdAt = Clock.System.now(),
      modifiedAt = Clock.System.now(),
    )
  }
}