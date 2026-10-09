package com.sloy.debugmenu.events

import android.content.ClipData
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sloy.debugmenu.base.DebugPreviewTheme
import com.sloy.debugmenu.base.PillSegmentedControl
import com.sloy.debugmenu.base.ScreenshotSuite
import com.sloy.debugmenu.base.ScreenshotTest
import com.sloy.debugmenu.events.viewer.BandEndItem
import com.sloy.debugmenu.events.viewer.BandHeaderItem
import com.sloy.debugmenu.events.viewer.EventItem
import com.sloy.debugmenu.events.viewer.EventRow
import com.sloy.debugmenu.events.viewer.EventSearchField
import com.sloy.debugmenu.events.viewer.EventsActionBar
import com.sloy.debugmenu.events.viewer.EventsActionBarHeight
import com.sloy.debugmenu.events.viewer.GapItem
import com.sloy.debugmenu.events.viewer.GapMarker
import com.sloy.debugmenu.events.viewer.LanesCard
import com.sloy.debugmenu.events.viewer.ScreenBandEnd
import com.sloy.debugmenu.events.viewer.ScreenBandHeader
import com.sloy.debugmenu.events.viewer.SessionItem
import com.sloy.debugmenu.events.viewer.SessionSummaryCard
import com.sloy.debugmenu.events.viewer.TimelineHead
import com.sloy.debugmenu.events.viewer.ViewerItem
import com.sloy.debugmenu.events.viewer.ViewerSampleData
import com.sloy.debugmenu.events.viewer.journeyItems
import com.sloy.debugmenu.events.viewer.lanesModel
import com.sloy.debugmenu.events.viewer.screenBandBackground
import com.sloy.debugmenu.events.viewer.timelineItems
import kotlinx.coroutines.launch

private const val TIMELINE = 0
private const val JOURNEY = 1
private const val LANES_KEY = "lanes"
private const val SEARCH_KEY = "search"
private const val HEAD_KEY = "head"
private const val NO_MATCHES_KEY = "no-matches"
private const val SCROLL_SLOP = 2f
private const val ACTION_BAR_MILLIS = 200

/**
 * Full-screen event viewer with a Timeline and a Journey view, search and JSON export.
 */
@Composable
fun EventLogScreen(eventStore: EventStore, onClose: () -> Unit) {
    val viewModel = viewModel { EventLogViewModel(eventStore) }
    val events by viewModel.events.collectAsStateWithLifecycle()
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    EventLogScreenContent(
        events = events,
        onClose = onClose,
        onClear = viewModel::onClearEvents,
        onCopy = { json, _ -> scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Events", json))) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventLogScreenContent(
    events: List<CapturedEvent>,
    onClose: () -> Unit,
    onClear: () -> Unit,
    initialView: Int = TIMELINE,
    initiallyExpanded: Set<String> = emptySet(),
    onCopy: (json: String, count: Int) -> Unit = { _, _ -> },
) {
    var selectedView by rememberSaveable { mutableIntStateOf(initialView) }
    var query by rememberSaveable { mutableStateOf("") }
    val isJourney = selectedView == JOURNEY
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val items = remember(events, query, isJourney) {
        if (isJourney) journeyItems(events) else timelineItems(events, query)
    }
    val listState = rememberLazyListState()
    var actionBarVisible by remember { mutableStateOf(true) }
    LaunchedEffect(selectedView) {
        listState.scrollToItem(0)
        actionBarVisible = true
    }
    val hideOnScroll = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -SCROLL_SLOP) actionBarVisible = false
                if (available.y > SCROLL_SLOP) actionBarVisible = true
                return Offset.Zero
            }
        }
    }
    val actionBarOffset by animateFloatAsState(if (actionBarVisible) 0f else 1f, tween(ACTION_BAR_MILLIS), label = "actionBarOffset")
    val surface = MaterialTheme.colorScheme.surface

    Scaffold(
        containerColor = surface,
        topBar = {
            TopAppBar(
                title = { Text("Events (${events.size})", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    PillSegmentedControl(
                        options = listOf("Timeline", "Journey"),
                        selectedIndex = selectedView,
                        onSelected = { selectedView = it },
                        modifier = Modifier.width(184.dp).padding(end = 12.dp),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = surface, scrolledContainerColor = surface),
            )
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .nestedScroll(hideOnScroll)
        ) {
            if (events.isEmpty()) {
                CenteredMessage("No events yet")
            } else {
                EventList(
                    events = events,
                    items = items,
                    isJourney = isJourney,
                    listState = listState,
                    initiallyExpanded = initiallyExpanded,
                    header = {
                        if (!isJourney) {
                            item(key = SEARCH_KEY) {
                                EventSearchField(query, { query = it }, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                            }
                            if (items.isEmpty()) {
                                item(key = NO_MATCHES_KEY) {
                                    Box(Modifier.fillParentMaxHeight(0.6f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        EmptyText("No matching events")
                                    }
                                }
                            } else {
                                item(key = HEAD_KEY) { TimelineHead(Modifier.padding(top = 4.dp)) }
                            }
                        }
                    },
                )
            }
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .graphicsLayer { translationY = actionBarOffset * size.height }
            ) {
                SnackbarHost(snackbar)
                EventsActionBar(
                    onCopy = {
                        onCopy(events.toSessionJson(), events.size)
                        scope.launch { snackbar.showSnackbar("Copied ${events.size} events") }
                    },
                    onClear = onClear,
                )
            }
        }
    }
}

@Composable
private fun CenteredMessage(text: String) {
    Box(Modifier.fillMaxSize().padding(bottom = EventsActionBarHeight), contentAlignment = Alignment.Center) {
        EmptyText(text)
    }
}

@Composable
private fun EmptyText(text: String) {
    Text(text, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun EventList(
    events: List<CapturedEvent>,
    items: List<ViewerItem>,
    isJourney: Boolean,
    listState: LazyListState,
    initiallyExpanded: Set<String>,
    header: LazyListScope.() -> Unit,
) {
    val scope = rememberCoroutineScope()
    val eventsById = remember(events) { events.associateBy { it.id } }
    val oldestEventKey = remember(items) { items.lastOrNull { it is EventItem }?.key }
    val visibleRange by remember(items, eventsById) {
        derivedStateOf {
            val visible = listState.layoutInfo.visibleItemsInfo
            val lanesBottom = visible.firstOrNull { it.key == LANES_KEY }?.let { it.offset + it.size } ?: Int.MIN_VALUE
            val timestamps = visible.filter { it.offset + it.size > lanesBottom }.mapNotNull { eventsById[it.key]?.timestampMillis }
            if (timestamps.isEmpty()) null else timestamps.min() to timestamps.max()
        }
    }
    val bottomPadding = EventsActionBarHeight + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyColumn(state = listState, contentPadding = PaddingValues(bottom = bottomPadding), modifier = Modifier.fillMaxSize()) {
        header()
        if (isJourney) {
            stickyHeader(key = LANES_KEY) {
                val (from, to) = visibleRange ?: (events.minOf { it.timestampMillis } to events.maxOf { it.timestampMillis })
                LanesCard(
                    model = remember(events, from, to) { lanesModel(events, from, to) },
                    onMarkClick = { id ->
                        val index = items.indexOfFirst { it.key == id }
                        val lanesHeight = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == LANES_KEY }?.size ?: 0
                        if (index >= 0) scope.launch { listState.animateScrollToItem(index + 1, -lanesHeight) }
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                )
            }
        }
        items(items, key = { it.key }) { item ->
            when (item) {
                is EventItem -> {
                    var expanded by rememberSaveable(item.key) { mutableStateOf(item.key in initiallyExpanded) }
                    EventRow(
                        event = item.event,
                        delta = item.delta,
                        expanded = expanded,
                        onToggle = { expanded = !expanded },
                        inBand = item.inBand,
                        isOldest = item.key == oldestEventKey,
                        modifier = if (item.inBand) Modifier.screenBandBackground() else Modifier,
                    )
                }
                is SessionItem -> {
                    var expanded by rememberSaveable(item.key) { mutableStateOf(item.key in initiallyExpanded) }
                    SessionSummaryCard(item.event, expanded, onToggle = { expanded = !expanded })
                }
                is GapItem -> GapMarker(item.label)
                is BandHeaderItem -> ScreenBandHeader(item)
                is BandEndItem -> ScreenBandEnd()
            }
        }
    }
}

@ScreenshotTest(ScreenshotSuite.Screens)
@PreviewLightDark
@Composable
internal fun EventLogScreenTimelinePreview() {
    DebugPreviewTheme {
        EventLogScreenContent(ViewerSampleData.events, {}, {}, initiallyExpanded = setOf(ViewerSampleData.expandedEventId))
    }
}

@ScreenshotTest(ScreenshotSuite.Screens)
@PreviewLightDark
@Composable
internal fun EventLogScreenJourneyPreview() {
    DebugPreviewTheme {
        EventLogScreenContent(ViewerSampleData.events, {}, {}, initialView = 1, initiallyExpanded = setOf(ViewerSampleData.expandedEventId))
    }
}

@ScreenshotTest(ScreenshotSuite.Screens)
@PreviewLightDark
@Composable
internal fun EventLogScreenSessionExpandedPreview() {
    DebugPreviewTheme {
        EventLogScreenContent(
            ViewerSampleData.events, {}, {},
            initiallyExpanded = setOf(ViewerSampleData.expandedEventId, ViewerSampleData.sessionSummaryId),
        )
    }
}

@ScreenshotTest(ScreenshotSuite.Screens)
@PreviewLightDark
@Composable
internal fun EventLogScreenEmptyPreview() {
    DebugPreviewTheme { EventLogScreenContent(emptyList(), {}, {}) }
}
