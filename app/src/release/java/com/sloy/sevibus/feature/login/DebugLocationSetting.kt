package com.sloy.sevibus.feature.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sloy.sevibus.infrastructure.BuildVariant

@Composable
fun DebugLocationSetting(modifier: Modifier = Modifier) {
    // Empty on release
    if (BuildVariant.isDebug()) error("DebugSettings is using release implementation on debug")
}
