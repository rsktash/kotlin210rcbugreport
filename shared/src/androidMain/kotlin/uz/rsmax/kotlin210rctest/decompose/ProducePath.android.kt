package uz.rsmax.kotlin210rctest.decompose

import android.content.Context


actual fun producePath(fileName: String): String =
  appContext.filesDir.resolve(fileName).absolutePath

actual fun produceCachePath(fileName: String): String =
  appContext.cacheDir.resolve(fileName).absolutePath

internal lateinit var appContext: Context


fun initContext(context: Context){
  appContext = context
}