package uz.rsmax.kotlin210rctest

import android.icu.text.DecimalFormatSymbols
import uz.rsmax.kotlin210rctest.decompose.appContext
import java.math.BigDecimal

actual fun Double.toStringIgnoreZero(): String =
  if(this == 0.0) "" else BigDecimal.valueOf(this).toPlainString()

actual fun getDecimalSeparator(): String =
  DecimalFormatSymbols.getInstance(appContext.resources.configuration.locales[0])
    .getDecimalSeparator()
    .toString()