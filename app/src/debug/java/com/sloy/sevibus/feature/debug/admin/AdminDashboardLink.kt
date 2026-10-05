package com.sloy.sevibus.feature.debug.admin

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.sloy.debugmenu.base.DebugCell
import com.sloy.debugmenu.base.DebugMenu
import com.sloy.sevibus.ui.theme.SevTheme

private const val ADMIN_DASHBOARD_URL = "https://sevibus-admin.web.app"

@Composable
fun AdminDashboardLink() {
    val uriHandler = LocalUriHandler.current
    DebugCell(
        title = "Admin Dashboard",
        subtitle = ADMIN_DASHBOARD_URL,
        onClick = { uriHandler.openUri(ADMIN_DASHBOARD_URL) },
        end = {
            Icon(
                Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = "Open in browser",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        modifier = Modifier.padding(horizontal = 8.dp),
    )
}

@PreviewLightDark
@Composable
private fun AdminDashboardLinkPreview() {
    SevTheme {
        DebugMenu {
            AdminDashboardLink()
        }
    }
}
