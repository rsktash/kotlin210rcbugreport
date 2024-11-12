package uz.rsmax.kotlin210rctest.decompose

import kotlinx.coroutines.CoroutineDispatcher

interface AppDispatchers {
  val main: CoroutineDispatcher
  val io: CoroutineDispatcher
  val unconfined: CoroutineDispatcher
}

expect fun AppDispatchers(): AppDispatchers