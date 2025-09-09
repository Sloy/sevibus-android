package com.sloy.sevibus.infrastructure.extensions

import androidx.compose.ui.Modifier

fun Modifier.applyIf(
    condition: Boolean,
    modifier: Modifier.() -> Modifier
) = if (condition) then(modifier(Modifier)) else this