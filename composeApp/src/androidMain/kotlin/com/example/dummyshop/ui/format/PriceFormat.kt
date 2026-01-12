package com.example.dummyshop.ui.format

import java.text.NumberFormat
import java.util.Locale

fun Double.formatAsPrice(locale: Locale = Locale.US): String =
    NumberFormat.getCurrencyInstance(locale).format(this)
