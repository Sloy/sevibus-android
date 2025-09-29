package com.sloy.sevibus.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.domain.model.CustomIcon
import com.sloy.sevibus.domain.model.toImageVector
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun IconPicker(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onIconSelected: (CustomIcon?) -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        IconPickerContent(onIconSelected = onIconSelected)
    }
}

@Composable
private fun IconPickerContent(
    onIconSelected: (CustomIcon?) -> Unit,
    modifier: Modifier = Modifier
) {
    val allIcons = CustomIcon.entries.toList()

    FlowRow(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 4
    ) {
        allIcons.forEach { customIcon ->
            IconButton(
                onClick = { onIconSelected(customIcon) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = customIcon.toImageVector(),
                    contentDescription = customIcon.name,
                    tint = SevTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 250)
@Composable
private fun IconPickerPreview() {
    SevTheme {
        Surface(
            color = SevTheme.colorScheme.surface,
            shape = SevTheme.shapes.medium,
            border = BorderStroke(1.dp, SevTheme.colorScheme.outline)
        ) {
            IconPickerContent(
                onIconSelected = { }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 300, heightDp = 200)
@Composable
private fun IconPickerInContextPreview() {
    SevTheme {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            // Show a trigger button
            var expanded by remember { mutableStateOf(false) }

            IconButton(
                onClick = { expanded = true },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = CustomIcon.Home.toImageVector(),
                    contentDescription = "Select Icon",
                    tint = SevTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconPicker(
                expanded = expanded,
                onDismiss = { expanded = false },
                onIconSelected = { expanded = false }
            )
        }
    }
}
