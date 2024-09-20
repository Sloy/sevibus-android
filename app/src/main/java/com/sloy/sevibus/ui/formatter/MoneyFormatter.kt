package com.sloy.sevibus.ui.formatter

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object MoneyFormatter {

    fun fromMillis(millis: Int, showPlusWhenPositive: Boolean = false): String =
        formatter.format(millis / 1000f)
            .apply { if (showPlusWhenPositive and (millis > 0)) return "+$this" }


    private val locale = Locale("es")
    private val formatter = NumberFormat
        .getCurrencyInstance(locale)
        .let { it as DecimalFormat }
        .also {
            it.decimalFormatSymbols = DecimalFormatSymbols(locale).apply {
                currencySymbol = Currency.getInstance("EUR").symbol
            }
            it.maximumFractionDigits = 2
            it.minimumFractionDigits = 2
        }
}