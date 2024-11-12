package uz.rsmax.kotlin210rctest.decompose

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual fun AppDispatchers(): AppDispatchers = object : AppDispatchers {
  override val main: CoroutineDispatcher = Dispatchers.Main.immediate
  override val io: CoroutineDispatcher = Dispatchers.Default
  override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}