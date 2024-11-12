package uz.rsmax.kotlin210rctest.decompose.prefs

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import uz.rsmax.kotlin210rctest.decompose.UserId
import kotlin.reflect.KClass

interface AppPreferences {

  fun setName(name: String)

  suspend fun putFloat(key: String, value: Float)

  suspend fun putInt(key: String, value: Int)

  suspend fun putLong(key: String, value: Long)

  suspend fun putDouble(key: String, value: Double)

  suspend fun putBoolean(key: String, value: Boolean)

  suspend fun putString(key: String, value: String)

  suspend fun put(key: String, value: Set<String>)

  fun getFloatAsFlow(key: String): Flow<Float?>

  fun getIntAsFlow(key: String): Flow<Int?>

  fun getLongAsFlow(key: String): Flow<Long?>

  fun getDoubleAsFlow(key: String): Flow<Double?>

  fun getBooleanAsFlow(key: String): Flow<Boolean?>

  fun getStringAsFlow(key: String): Flow<String?>

  fun getStringSetAsFlow(key: String): Flow<Set<String>?>

  suspend fun remove(key: String, klz: KClass<*>)

  suspend fun clear()
}

suspend fun AppPreferences.getLoggedInUser(): UserId? =
  getLongAsFlow(PREF_USER).firstOrNull()?.let(::UserId)

suspend fun AppPreferences.updateLoggedInUser(userId: UserId) {
  putLong(PREF_USER, userId.id)
}


private const val PREF_USER = "PREF_USER"