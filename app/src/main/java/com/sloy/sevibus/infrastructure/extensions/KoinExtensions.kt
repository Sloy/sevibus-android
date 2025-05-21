package com.sloy.sevibus.infrastructure.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import org.koin.compose.currentKoinScope
import org.koin.compose.koinInject
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.core.scope.Scope

/**
 * Wrapper around Koin's [koinInject] that is safe to be called inside a UI composable
 * that will be rendered by some Preview.
 *
 * Prevents errors due to Koin not being initialized in Preview mode.
 */
@Composable
inline fun <reified T> koinInjectOnUI(
    qualifier: Qualifier? = null,
    scope: Scope? = null,
    noinline parameters: ParametersDefinition? = null,
): T? = if (!LocalInspectionMode.current) {
    koinInject<T>(qualifier, scope ?: currentKoinScope(), parameters)
} else {
    null
}
