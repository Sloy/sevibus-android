package com.sloy.sevibus.feature.debug.http

import java.util.UUID

data class HttpOverlayItem(
    val method: String = "GET",
    val endpoint: String = "/unknown",
    val status: Int? = null,
    val id: String = UUID.randomUUID().toString(),
)
