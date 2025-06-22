package com.sloy.sevibus.feature.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun SettingsSection(title: String, items: @Composable () -> Unit) {
    SectionTitle(title)
    Card {
        items()
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    leadingIcon: ImageVector,
    onClick: (() -> Unit)? = null,
    endIcon: ImageVector? = null,
) {
    SettingsItem(title, subtitle, leadingIcon, onClick, endComponent = {
        if (endIcon != null) {
            Icon(
                endIcon,
                contentDescription = null,
                tint = SevTheme.colorScheme.outline,
                modifier = Modifier
                    .size(24.dp)
            )
        }
    })
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    leadingIcon: ImageVector,
    onClick: (() -> Unit)? = null,
    endComponent: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .apply {
                if (onClick != null) {
                    clickable { onClick() }
                }
            }
            .padding(16.dp)) {
        Icon(
            leadingIcon,
            contentDescription = null,
            tint = SevTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(end = 16.dp)
                .size(24.dp)
        )
        Column(Modifier.weight(1f)) {
            Text(title, style = SevTheme.typography.bodyStandardBold)
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle, style = SevTheme.typography.bodySmall, color = SevTheme.colorScheme.onSurfaceVariant
            )
        }
        endComponent()
    }

}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = SevTheme.typography.headingSmall, modifier = Modifier.padding(bottom = 12.dp))
}
