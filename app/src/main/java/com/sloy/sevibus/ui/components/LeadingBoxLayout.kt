package com.sloy.sevibus.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * This Composable is meant to keep the same width on
 * all [androidx.compose.material3.ListItem]'s leading components in a column.
 */
@Composable
fun LeadingBoxLayout(minWidth: Dp, content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.size(minWidth), contentAlignment = Alignment.CenterStart) {
        content()
    }
}
