package com.sloy.sevibus.infrastructure.analytics

abstract class SevEvent(
    val name: String,
    vararg val properties: Pair<String, Any?>
)
