package com.sloy.sevibus.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun SurfaceButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Button(
        enabled = true,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors().copy(
            containerColor = SevTheme.colorScheme.outlineVariant,
            contentColor = SevTheme.colorScheme.onSurfaceVariant
        ),
        onClick = onClick,
    ) {
        content()
    }
}

@Composable
fun SurfaceButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: @Composable (() -> Unit)? = null) {
    SurfaceButton(onClick, modifier) {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(8.dp))
        }
        Text(
            color = SevTheme.colorScheme.onSurface,
            text = text,
            style = SevTheme.typography.bodyStandardBold,
            modifier = Modifier,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SmallSurfaceButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Button(
        enabled = true,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors().copy(
            containerColor = SevTheme.colorScheme.outlineVariant,
            contentColor = SevTheme.colorScheme.onSurfaceVariant
        ),
        onClick = onClick,
    ) {
        content()
    }
}

@Composable
fun SmallSurfaceButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: @Composable (() -> Unit)? = null) {
    SmallSurfaceButton(onClick, modifier) {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(4.dp))
        }
        Text(
            color = SevTheme.colorScheme.onSurface,
            text = text,
            style = SevTheme.typography.bodySmallBold,
            modifier = Modifier,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun TextPreview() {
    SevTheme {
        Box(Modifier.padding(16.dp)) {
            SurfaceButton("Hello world", {})
        }

    }
}

@Preview(showBackground = true)
@Composable
internal fun IconPreview() {
    SevTheme {
        Box(Modifier.padding(16.dp)) {
            SurfaceButton("Hello world", {}, icon = {
                Icon(
                    Icons.Default.LocationOn,
                    tint = SevTheme.colorScheme.primary,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            })
        }

    }
}

@Preview(showBackground = true)
@Composable
internal fun SmallIconPreview() {
    SevTheme {
        Box(Modifier.padding(16.dp)) {
            SmallSurfaceButton("Hello world", {}, icon = {
                Icon(
                    Icons.Default.LocationOn,
                    tint = SevTheme.colorScheme.primary,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            })
        }

    }
}
