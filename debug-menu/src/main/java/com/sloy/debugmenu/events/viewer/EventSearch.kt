package com.sloy.debugmenu.events.viewer

import com.sloy.debugmenu.events.CapturedEvent

internal fun CapturedEvent.matches(query: String): Boolean {
    val text = query.trim()
    if (text.isEmpty() || name.contains(text, ignoreCase = true)) return true
    return properties.any { (key, value) ->
        key.contains(text, ignoreCase = true) ||
            value.contains(text, ignoreCase = true) ||
            "$key=$value".contains(text, ignoreCase = true) ||
            "$key $value".contains(text, ignoreCase = true)
    }
}
