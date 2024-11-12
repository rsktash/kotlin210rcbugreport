package uz.rsmax.kotlin210rctest.decompose

import kotlinx.serialization.Serializable
import uz.rsmax.kotlin210rctest.decompose.database.ObjectId
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class UserId(override val id: Long): ObjectId

@Serializable
data class AppUser(
  val id: UserId,
  val name: String,
  val email: String,
)
