package com.sloy.debugmenu.events

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sloy.debugmenu.base.DebugPreviewTheme

/**
 * Full-screen list of captured events.
 */
@Composable
fun EventLogScreen(eventStore: EventStore, onClose: () -> Unit) {
    val viewModel = viewModel { EventLogViewModel(eventStore) }
    val events by viewModel.events.collectAsStateWithLifecycle()
    EventLogScreenContent(events = events, onClose = onClose, onClear = viewModel::onClearEvents)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventLogScreenContent(events: List<CapturedEvent>, onClose: () -> Unit, onClear: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Events (${events.size})") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Clear events")
                    }
                },
            )
        },
    ) { padding ->
        if (events.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                Text("No events yet", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = padding) {
                items(events, key = { it.id }) { event ->
                    EventRow(event)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: CapturedEvent) {
    var expanded by rememberSaveable(event.id) { mutableStateOf(false) }
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EventTypeIcon(EventType.of(event.name))
            Spacer(Modifier.width(8.dp))
            Text(event.name, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            Text(event.timestamp, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        AnimatedVisibility(visible = expanded) {
            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (event.properties.isEmpty()) {
                    Text("No properties", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                event.properties.forEach { (key, value) ->
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("$key: ") }
                            append(value)
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                FilledTonalButton(
                    onClick = { shareEvent(context, event) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                ) {
                    Text("Share")
                }
            }
        }
    }
}

private fun shareEvent(context: Context, event: CapturedEvent) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Event: ${event.name}")
        putExtra(Intent.EXTRA_TEXT, event.toJsonObject().toString())
    }
    context.startActivity(Intent.createChooser(intent, null))
}

@PreviewLightDark
@Composable
private fun EventLogScreenPreview() {
    DebugPreviewTheme {
        EventLogScreenContent(
            events = listOf(
                CapturedEvent("Add Favorite Clicked", mapOf("stopId" to "42"), timestampMillis = 0, id = "1"),
                CapturedEvent("Stop Details Viewed", emptyMap(), timestampMillis = 0, id = "2"),
                CapturedEvent("App Started", emptyMap(), timestampMillis = 0, id = "3"),
            ),
            onClose = {},
            onClear = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun EventLogScreenEmptyPreview() {
    DebugPreviewTheme {
        EventLogScreenContent(events = emptyList(), onClose = {}, onClear = {})
    }
}
