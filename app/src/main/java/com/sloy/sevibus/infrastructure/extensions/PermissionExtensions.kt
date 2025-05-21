package com.sloy.sevibus.infrastructure.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun rememberPermissionStateOnUI(
    permission: String,
    onPermissionResult: (Boolean) -> Unit = {}
): PermissionState? {
    return if (!LocalInspectionMode.current) {
        rememberPermissionState(permission, onPermissionResult)
    } else {
        null
    }
}