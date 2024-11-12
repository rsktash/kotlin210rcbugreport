package uz.rsmax.kotlin210rctest.decompose

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun producePath(fileName: String): String {
  val documentDirectory: NSURL? =
    NSFileManager.defaultManager.URLForDirectory(
      directory = NSDocumentDirectory,
      inDomain = NSUserDomainMask,
      appropriateForURL = null,
      create = false,
      error = null,
    )
  return requireNotNull(documentDirectory).path + "/$fileName"
}

@OptIn(ExperimentalForeignApi::class)
actual fun produceCachePath(fileName: String): String {
  val documentDirectory: NSURL? =
    NSFileManager.defaultManager.URLForDirectory(
      directory = NSCachesDirectory,
      inDomain = NSUserDomainMask,
      appropriateForURL = null,
      create = false,
      error = null,
    )
  return requireNotNull(documentDirectory).path + "/$fileName"
}