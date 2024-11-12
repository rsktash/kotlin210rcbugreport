package uz.rsmax.kotlin210rctest

import platform.Foundation.NSNumberFormatter

actual fun Double.toStringIgnoreZero(): String =
  if(this == 0.0) "" else this.toString() // TODO

actual fun getDecimalSeparator(): String = NSNumberFormatter().decimalSeparator