package uz.rsmax.kotlin210rctest

class JsPlatform: Platform {
  override val name: String = "JS"
}

actual fun getPlatform(): Platform {
  return JsPlatform()
}