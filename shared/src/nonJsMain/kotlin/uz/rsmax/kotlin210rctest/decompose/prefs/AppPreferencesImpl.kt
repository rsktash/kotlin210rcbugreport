package uz.rsmax.kotlin210rctest.decompose.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath
import uz.rsmax.kotlin210rctest.decompose.producePath
import kotlin.reflect.KClass

class AppPreferencesImpl : AppPreferences {

  private var name: String = "prefs.preferences_pb"

  override fun setName(name: String) {
    this.name = name
  }

  @PublishedApi
  internal val prefs: DataStore<Preferences> by lazy {
    createDataStore(name)
  }

  override suspend fun putFloat(key: String, value: Float) {
    prefs.edit { it[floatPreferencesKey(key)] = value }
  }

  override suspend fun putInt(key: String, value: Int) {
    prefs.edit { it[intPreferencesKey(key)] = value }
  }

  override suspend fun putLong(key: String, value: Long) {
    prefs.edit { it[longPreferencesKey(key)] = value }
  }

  override suspend fun putDouble(key: String, value: Double) {
    prefs.edit { it[doublePreferencesKey(key)] = value }
  }

  override suspend fun putBoolean(key: String, value: Boolean) {
    prefs.edit { it[booleanPreferencesKey(key)] = value }
  }

  override suspend fun putString(key: String, value: String) {
    prefs.edit { it[stringPreferencesKey(key)] = value }
  }

  override suspend fun put(key: String, value: Set<String>) {
    prefs.edit { it[stringSetPreferencesKey(key)] = value }
  }

  override suspend fun clear() {
    prefs.edit { it.clear() }
  }

  override fun getFloatAsFlow(key: String): Flow<Float?> =
    prefs.data.map { it[floatPreferencesKey(key)] }

  override fun getIntAsFlow(key: String): Flow<Int?> = prefs.data.map { it[intPreferencesKey(key)] }

  override fun getLongAsFlow(key: String): Flow<Long?> =
    prefs.data.map { it[longPreferencesKey(key)] }

  override fun getDoubleAsFlow(key: String): Flow<Double?> =
    prefs.data.map { it[doublePreferencesKey(key)] }

  override fun getBooleanAsFlow(key: String): Flow<Boolean?> =
    prefs.data.map { it[booleanPreferencesKey(key)] }

  override fun getStringAsFlow(key: String): Flow<String?> =
    prefs.data.map { it[stringPreferencesKey(key)] }

  override fun getStringSetAsFlow(key: String): Flow<Set<String>?> =
    prefs.data.map { it[stringSetPreferencesKey(key)] }

  override suspend fun remove(key: String, klz: KClass<*>) {
    prefs.edit { it.remove(preferenceOf(key, klz)) }
  }

  private fun preferenceOf(key: String, klz: KClass<*>) =
    when (klz) {
      String::class -> stringPreferencesKey(key)
      Int::class -> intPreferencesKey(key)
      Float::class -> floatPreferencesKey(key)
      Double::class -> doublePreferencesKey(key)
      Boolean::class -> booleanPreferencesKey(key)
      Long::class -> longPreferencesKey(key)
      Set::class -> stringSetPreferencesKey(key)
      else -> stringSetPreferencesKey(key)
    }
}


private fun createDataStore(fileName: String): DataStore<Preferences> {
  return PreferenceDataStoreFactory.createWithPath(
    corruptionHandler = null,
    migrations = emptyList(),
    scope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
    produceFile = { producePath(fileName).toPath() },
  )
}