package uz.rsmax.kotlin210rctest

actual fun Double.toStringIgnoreZero(): String {
  return if (this == 0.0) ""
  else {
    val kotlinNumber = this
    js("kotlinNumber.toLocaleString()") as String
  }
}

actual fun getDecimalSeparator(): String =
  js("var n = 1.1; var d = n.toLocaleString().substring(1, 2); return d;").unsafeCast<String>()