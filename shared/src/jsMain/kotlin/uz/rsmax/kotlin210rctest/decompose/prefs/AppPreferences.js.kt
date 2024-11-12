package uz.rsmax.kotlin210rctest.decompose.prefs

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uz.rsmax.JSPreferencesQueries

class AppPreferencesImpl(private val dao: JSPreferencesQueries) : AppPreferences {

  private var prefix: String = "pref"

  override fun setName(name: String) {
    prefix = name
  }

  private fun addPrefix(key: String) = "$prefix.$key"

  override suspend fun putFloat(key: String, value: Float) {
    dao.set(key = addPrefix(key), value = value.toString())
  }

  override suspend fun putInt(key: String, value: Int) {
    dao.set(key = addPrefix(key), value = value.toString())
  }

  override suspend fun putLong(key: String, value: Long) {
    dao.set(key = addPrefix(key), value = value.toString())
  }

  override suspend fun putDouble(key: String, value: Double) {
    dao.set(key = addPrefix(key), value = value.toString())
  }

  override suspend fun putBoolean(key: String, value: Boolean) {
    dao.set(key = addPrefix(key), value = value.toString())
  }

  override suspend fun putString(key: String, value: String) {
    dao.set(key = addPrefix(key), value = value)
  }

  override suspend fun put(key: String, value: Set<String>) {
    dao.set(key = addPrefix(key), value = value.joinToString(","))
  }

  override fun getFloatAsFlow(key: String): Flow<Float?> =
    dao.get(addPrefix(key)).asFlow().map { it.awaitAsOneOrNull()?.toFloat() }

  override fun getIntAsFlow(key: String): Flow<Int?> =
    dao.get(addPrefix(key)).asFlow().map { it.awaitAsOneOrNull()?.toInt() }

  override fun getLongAsFlow(key: String): Flow<Long?> =
    dao.get(addPrefix(key)).asFlow().map { it.awaitAsOneOrNull()?.toLong() }

  override fun getDoubleAsFlow(key: String): Flow<Double?> =
    dao.get(addPrefix(key)).asFlow().map { it.awaitAsOneOrNull()?.toDouble() }

  override fun getBooleanAsFlow(key: String): Flow<Boolean?> =
    dao.get(addPrefix(key)).asFlow().map { it.awaitAsOneOrNull()?.toBoolean() }

  override fun getStringAsFlow(key: String): Flow<String?> =
    dao.get(addPrefix(key)).asFlow().map { it.awaitAsOneOrNull() }

  override fun getStringSetAsFlow(key: String): Flow<Set<String>?> =
    dao.get(addPrefix(key)).asFlow().map { it.awaitAsOneOrNull()?.split(",")?.toSet() }

  override suspend fun remove(key: String, klz: KClass<*>) {
    dao.remove(addPrefix(key))
  }

  override suspend fun clear() {
    dao.clear()
  }
}
