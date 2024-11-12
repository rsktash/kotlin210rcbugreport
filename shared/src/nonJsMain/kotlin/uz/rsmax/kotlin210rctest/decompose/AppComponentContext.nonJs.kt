package uz.rsmax.kotlin210rctest.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import uz.rsmax.AppDatabase
import uz.rsmax.kotlin210rctest.decompose.database.DB_NAME
import uz.rsmax.kotlin210rctest.decompose.database.DatabaseMigrator
import uz.rsmax.kotlin210rctest.decompose.database.createAppDatabase
import uz.rsmax.kotlin210rctest.decompose.database.createDriver
import uz.rsmax.kotlin210rctest.decompose.prefs.AppPreferencesImpl

actual fun DefaultAppComponentContext(
  componentContext: ComponentContext,
  storeFactory: StoreFactory,
): DefaultAppComponentContext = DefaultAppComponentContext(
  componentContext = componentContext,
  database = createAppDatabase(createDriver(DB_NAME, AppDatabase.Schema)),
  preferences = AppPreferencesImpl(),
  storeFactory = storeFactory,
  dispatchers = AppDispatchers(),
  migrator = DatabaseMigrator,
)