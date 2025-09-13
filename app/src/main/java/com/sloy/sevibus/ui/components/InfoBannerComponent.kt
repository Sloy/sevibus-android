package com.sloy.sevibus.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun InfoBannerComponent(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Info,
    iconColor: Color = Color.Unspecified,
    containerColor: Color = Color.Unspecified,
    textColor: Color = Color.Unspecified,
    textStyle: TextStyle = SevTheme.typography.bodySmall,
    action: @Composable (() -> Unit)? = null,
) {
    Surface(
        modifier.fillMaxWidth(),
        color = containerColor.takeOrElse { SevTheme.colorScheme.errorContainer },
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                tint = iconColor.takeOrElse { SevTheme.colorScheme.error },
                modifier = Modifier.padding(end = 16.dp),
                contentDescription = null,
            )
            Text(
                text = text,
                style = textStyle,
                color = textColor.takeOrElse { SevTheme.colorScheme.onErrorContainer },
                modifier = Modifier.weight(1f)
            )
            action?.invoke()
        }
    }
}

@PreviewLightDark
@Composable
private fun TwoLinesPreview() {
    SevTheme {
        Surface {

            InfoBannerComponent(
                "Selecciona la parada de destino para ver la hora prevista de llegada.",
                Modifier.padding(16.dp)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TwoLinesActionPreview() {
    SevTheme {
        Surface {
            InfoBannerComponent(
                "Selecciona la parada de destino para ver la hora prevista de llegada.",
                Modifier.padding(16.dp),
                action = { TextButton(onClick = {}) { Text("Ver") } }
            )
        }
    }
}

@Preview
@Composable
private fun OneLinePreview() {
    SevTheme {
        InfoBannerComponent(
            "Selecciona la parada de destino.",
            Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
private fun OneLineActionPreview() {
    SevTheme {
        InfoBannerComponent(
            "Selecciona la parada de destino.",
            Modifier.padding(16.dp),
            action = { TextButton(onClick = {}) { Text("Ver") } }
        )
    }
}
