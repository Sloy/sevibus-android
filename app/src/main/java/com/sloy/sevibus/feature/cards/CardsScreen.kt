@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalSharedTransitionApi::class)

package com.sloy.sevibus.feature.cards

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Contactless
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.CardId
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.infrastructure.EventCollector
import com.sloy.sevibus.infrastructure.FeatureFlags
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.extensions.performHapticGestureStart
import com.sloy.sevibus.infrastructure.extensions.performHapticSegmentTick
import com.sloy.sevibus.infrastructure.extensions.splitPhrase
import com.sloy.sevibus.infrastructure.extensions.swap
import com.sloy.sevibus.infrastructure.nfc.NfcState
import com.sloy.sevibus.infrastructure.nfc.NfcStateManager
import com.sloy.sevibus.ui.animation.animatePulse
import com.sloy.sevibus.ui.components.CircularIconButton
import com.sloy.sevibus.ui.components.InfoBannerComponent
import com.sloy.sevibus.ui.components.SurfaceButton
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.shimmer.Shimmer
import com.sloy.sevibus.ui.snackbar.LocalSnackbarHostState
import com.sloy.sevibus.ui.theme.SevTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.math.abs


@Composable
fun CardsScreen(onNavigateToHelp: () -> Unit) {
    val snackBar = LocalSnackbarHostState.current
    val viewModel = koinInject<CardViewModel>()
    val nfcStateManager = koinInject<NfcStateManager>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val newCardState by viewModel.newCardState.collectAsStateWithLifecycle()
    val nfcState by nfcStateManager.state.collectAsStateWithLifecycle()

    val nfcAnimation = remember { Animatable(1f) }

    EventCollector(nfcStateManager.events) { nfcReadEvent ->
        viewModel.onNewCardNumber(CardSerialNumberUtils.calculateVisibleSerialNumber(nfcReadEvent.cardId))
        nfcAnimation.animatePulse()
    }
    EventCollector(viewModel.events) {
        when (it) {
            is CardsScreenEvent.ShowMessage -> {
                snackBar.showSnackbar(it.message, withDismissAction = true)
            }
        }
    }

    CardsScreen(
        state,
        newCardState,
        nfcState,
        viewModel::onNewCardNumber,
        viewModel::onDeleteCard,
        viewModel::onStartReordering,
        viewModel::onReorderingDone,
        onNavigateToHelp,
        nfcAnimation,
    )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun CardsScreen(
    state: CardsScreenState,
    newCardState: CardsScreenNewCardState,
    nfcState: NfcState,
    onNewCardNumber: (String) -> Unit,
    onDeleteCard: (CardId) -> Unit,
    onStartReorder: () -> Unit = {},
    onReorderDone: (List<CardInfo>) -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    nfcAnimation: Animatable<Float, AnimationVector1D> = remember { Animatable(1f) },
) {
    val isReordering = state is CardsScreenState.Content && state.isReordering
    val cardsFromState = (state as? CardsScreenState.Content)?.cardsAndTransactions?.cards()
    var reorderedCards: List<CardInfo> by remember(cardsFromState) { mutableStateOf(cardsFromState ?: emptyList()) }

    BackHandler(isReordering) {
        onReorderDone(reorderedCards)
    }

    Column(Modifier.safeDrawingPadding()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(stringResource(R.string.navigation_cards), style = SevTheme.typography.headingLarge, modifier = Modifier.weight(1f))

            if (!isReordering) {
                CircularIconButton(onClick = {
                    onNavigateToHelp()
                }) {
                    Icon(
                        Icons.AutoMirrored.Outlined.HelpOutline,
                        tint = SevTheme.colorScheme.onSurfaceVariant,
                        contentDescription = "Apply order"
                    )
                }
            }


            if (cardsFromState?.size ?: 0 > 1) {
                CircularIconButton(onClick = {
                    if (isReordering) {
                        onReorderDone(reorderedCards)
                    } else {
                        onStartReorder()
                    }
                }) {
                    if (isReordering) {
                        Icon(Icons.Default.Done, contentDescription = "Apply order")
                    } else {
                        Icon(Icons.Default.SwapVert, tint = SevTheme.colorScheme.onSurfaceVariant, contentDescription = "Reorder cards")
                    }
                }
            }
        }

        val scrollState = rememberScrollState()
        val isScrolled = scrollState.value != 0
        AnimatedVisibility(visible = isScrolled) {
            HorizontalDivider()
        }

        when (state) {
            is CardsScreenState.Empty -> CardsEmptyState(nfcState, newCardState, onNewCardNumber)
            is CardsScreenState.Loading -> CardsLoadingShimmer()
            is CardsScreenState.Error -> CardsErrorState(state)
            is CardsScreenState.Content -> {
                SharedTransitionLayout() {
                    AnimatedContent(state.isReordering) { isReordering ->
                        if (isReordering) {
                            CardsReorderingState(reorderedCards, onSwap = { from, to ->
                                reorderedCards = reorderedCards.swap(from, to)
                            }, this@AnimatedContent)
                        } else {
                            CardsScreenContent(
                                state,
                                nfcState,
                                newCardState,
                                scrollState,
                                onNewCardNumber,
                                onDeleteCard,
                                nfcAnimation,
                                this@AnimatedContent
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun SharedTransitionScope.CardsScreenContent(
    state: CardsScreenState.Content,
    nfcState: NfcState,
    newCardState: CardsScreenNewCardState,
    scrollState: ScrollState,
    onNewCardNumber: (String) -> Unit,
    onDeleteCard: (CardId) -> Unit,
    nfcAnimation: Animatable<Float, AnimationVector1D>,
    animatedContentScope: AnimatedContentScope,
) {
    val scope = rememberCoroutineScope()
    Column(Modifier.verticalScroll(scrollState)) {
        val cardsTransactions = state.cardsAndTransactions
        val cards = cardsTransactions.cards()

        val pagerState = rememberPagerState { cardsTransactions.size + 1 }
        if (state.scrollToCard != null) {
            cards.find { it.serialNumber == state.scrollToCard }?.let { card ->
                LaunchedEffect(card) {
                    pagerState.animateScrollToPage(page = cards.indexOf(card))
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 48.dp, vertical = 36.dp),
            pageSpacing = 16.dp,
            modifier = Modifier
                .scale(nfcAnimation.value)
                .wrapContentHeight()
                .fillMaxWidth(),
        ) { page ->
            if (page <= cards.lastIndex) {
                val card = cards[page]
                CardPictureItem(
                    card,
                    Modifier
                        .height(190.dp)
                        .sharedElement(rememberSharedContentState(key = card.serialNumber), animatedContentScope)
                )
            } else {
                CardAddMoreItem(newCardState)
            }
        }

        val currentCardAndTransactions = cardsTransactions.getOrNull(pagerState.currentPage)
        val contentAlpha = 1 - abs(pagerState.currentPageOffsetFraction) * 2
        Box(Modifier.alpha(contentAlpha)) {
            if (currentCardAndTransactions != null) {
                ExistingCardsDetail(currentCardAndTransactions.card,
                    currentCardAndTransactions.transactions,
                    onDeleteCard = {
                        onDeleteCard(it)
                        scope.launch {
                            scrollState.animateScrollTo(0)
                        }
                    })
            } else {
                NewCardDetail(nfcState, newCardState, onNewCardNumber)
            }
        }
        Spacer(Modifier.height(80.dp)) // Bottom sheet padding
    }
}

@Composable
private fun ExistingCardsDetail(currentCard: CardInfo, transactionsState: TransactionsState, onDeleteCard: (CardId) -> Unit) {
    Column {
        if (FeatureFlags.showCardUpdateWarning) {
            WarningNotice()
            Spacer(Modifier.size(16.dp))
        }
        CardBalanceItem(currentCard)
        Spacer(Modifier.size(16.dp))
        CardInfoElement(currentCard)
        Spacer(Modifier.size(32.dp))
        Text("Actividad reciente", style = SevTheme.typography.headingSmall, modifier = Modifier.padding(bottom = 12.dp, start = 16.dp))
        when (transactionsState) {
            is TransactionsState.Loaded -> {
                CardTransactionsElement(transactionsState.transactions)
            }

            is TransactionsState.Loading -> {
                CardTransactionsShimmer()
            }

            is TransactionsState.Error -> {
                InfoBannerComponent(
                    text = "No se pudo cargar la actividad reciente, inténtalo más tarde",
                    icon = Icons.Filled.CloudOff,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            is TransactionsState.Empty -> {
                Text(
                    "No hay actividad reciente",
                    style = SevTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = SevTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.size(32.dp))
        DeleteButton(onClick = { onDeleteCard(currentCard.serialNumber) })
        Spacer(Modifier.size(32.dp))

    }
}

@Composable
private fun NewCardDetail(nfcState: NfcState, newCardState: CardsScreenNewCardState, onNewCardNumber: (String) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Text("Añadir tarjeta", style = SevTheme.typography.headingSmall, modifier = Modifier.padding(bottom = 16.dp))

        Surface(
            color = SevTheme.colorScheme.surface,
            shape = SevTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {

                Text(
                    "El número de serie se encuentra en la parte trasera de la tarjeta. Suele ser de 12 dígitos.",
                    style = SevTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp)
                )
                Surface(
                    border = BorderStroke(1.dp, SevTheme.colorScheme.outlineVariant),
                    color = SevTheme.colorScheme.outlineVariant,
                    shape = SevTheme.shapes.small,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    val focusManager = LocalFocusManager.current
                    val textColor = if (newCardState is CardsScreenNewCardState.InputForm) {
                        SevTheme.colorScheme.onSurface
                    } else {
                        SevTheme.colorScheme.onSurfaceVariant
                    }
                    BasicTextField(
                        value = newCardState.serialNumber,
                        onValueChange = {
                            onNewCardNumber(it.filter { it.isDigit() }.take(12))
                        },
                        enabled = newCardState is CardsScreenNewCardState.InputForm,
                        singleLine = true,
                        textStyle = SevTheme.typography.bodyStandardBold.copy(color = textColor),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Number),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                        }),
                        cursorBrush = SolidColor(SevTheme.colorScheme.onSurface),
                        visualTransformation = CardSerialNumberVisualTransformation(),
                        decorationBox = { innerTextField ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                    innerTextField()
                                    if (newCardState.serialNumber.isEmpty()) {
                                        Text(
                                            "Número de serie",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = SevTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
        Spacer(Modifier.size(16.dp))

        if (newCardState is CardsScreenNewCardState.InputForm) {
            when (nfcState) {
                NfcState.NOT_AVAILABLE -> {}
                NfcState.DISABLED -> {
                    val context = LocalContext.current
                    Text(
                        "Activa NFC para leer la tarjeta fácilmente acercándola al teléfono",
                        style = SevTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    SurfaceButton("Activar NFC", onClick = {
                        //TODO activity might not exist
                        context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                    }, icon = {
                        Icon(Icons.Outlined.Contactless, contentDescription = null)
                    }, modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                NfcState.ENABLED -> {
                    Text(
                        "O pasa la tarjeta por detrás del móvil", style = SevTheme.typography.bodySmall,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                    )
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_nfc))
                    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier
                            .size(152.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        if (newCardState is CardsScreenNewCardState.CheckingCard) {
            CircularProgressIndicator(
                Modifier
                    .size(48.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

    }
}

@Composable
fun SharedTransitionScope.CardsReorderingState(
    cards: List<CardInfo>,
    onSwap: (from: Int, to: Int) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    SevLogger.logD("Reordering cards: ${cards.map { it.type }}")
    val view = LocalView.current
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onSwap(from.index, to.index)
        view.performHapticSegmentTick()
    }

    LazyColumn(
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxHeight()
    ) {
        items(cards.size, key = { cards[it].serialNumber }) { index ->
            val card = cards[index]
            ReorderableItem(reorderableLazyListState, key = card.serialNumber) { isDragging ->
                Row {
                    CardPictureItem(
                        card = card,
                        modifier = Modifier
                            .weight(1f)
                            .sharedElement(
                                rememberSharedContentState(key = card.serialNumber),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                            .alpha(if (isDragging) 0.7f else 1f)
                            .longPressDraggableHandle(
                                onDragStarted = {
                                    view.performHapticGestureStart()
                                },
                            )
                    )

                    Icon(
                        Icons.Filled.DragIndicator,
                        contentDescription = "Arrastrar para reordenar",
                        tint = SevTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(16.dp)
                            .size(24.dp)
                            .draggableHandle(
                                onDragStarted = {
                                    view.performHapticGestureStart()
                                },
                            )
                    )

                }

            }

        }
    }
}

@Composable
private fun CardsErrorState(state: CardsScreenState.Error) {
    InfoBannerComponent(
        "Hubo un error cargando tus tarjetas",
        icon = Icons.Outlined.CloudOff,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Text(state.error.message ?: "Unknown error", modifier = Modifier.padding(16.dp))
}

@Composable
private fun CardsLoadingShimmer() {
    Shimmer(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(256f / 154f)
            .padding(16.dp)
            .clip(SevTheme.shapes.medium)
    )
}

@Composable
private fun CardsEmptyState(nfcState: NfcState, newCardState: CardsScreenNewCardState, onNewCardNumber: (String) -> Unit) {
    NewCardDetail(nfcState, newCardState, onNewCardNumber)
}

@Composable
private fun WarningNotice() {
    InfoBannerComponent(
        text = "Las operaciones de hoy no se verán reflejadas hasta mañana",
        icon = Icons.Outlined.Info,
        containerColor = SevTheme.colorScheme.surfaceContainer,
        contentColor = SevTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun DeleteButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Icon(Icons.Default.Delete, contentDescription = null)
        Spacer(Modifier.size(8.dp))
        Text("Eliminar")
    }
}

@Composable
private fun CardPictureItem(card: CardInfo, modifier: Modifier = Modifier) {
    val (backgroundColor, accentColor) = CardColors.getCardColors(card)
    val textLines = card.type.splitPhrase()
    Box(
        modifier
            .aspectRatio(256f / 154f)
            .shadow(16.dp, SevTheme.shapes.large)
            .background(backgroundColor)
    ) {
        Column {
            Icon(
                Icons.Outlined.Contactless,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp)
            )
            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, end = 32.dp)
            ) {
                textLines.forEach {
                    Text(
                        text = it,
                        style = SevTheme.typography.headingSmall,
                        color = accentColor,
                        textAlign = TextAlign.End,
                        maxLines = 2,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
            Box(
                Modifier

                    .height(38.dp)
                    .fillMaxWidth()
                    .background(accentColor)
            )
        }
    }
}

@Composable
private fun CardPictureItemSmall(card: CardInfo, modifier: Modifier = Modifier) {
    val (backgroundColor, accentColor) = CardColors.getCardColors(card)
    val textLines = card.type.splitPhrase()
    Box(
        modifier
            .aspectRatio(256f / 154f)
            .shadow(16.dp, SevTheme.shapes.small)
            .background(backgroundColor)
    ) {
        Column {
            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                textLines.forEach {
                    Text(
                        text = it,
                        style = SevTheme.typography.bodyExtraSmallBold,
                        color = accentColor,
                        textAlign = TextAlign.End,
                        maxLines = 2,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
            Box(
                Modifier
                    .height(16.dp)
                    .fillMaxWidth()
                    .background(accentColor)
            )
        }
    }
}

@Composable
private fun CardAddMoreItem(newCardState: CardsScreenNewCardState) {
    val tint = SevTheme.colorScheme.surfaceVariant
    when (newCardState) {
        is CardsScreenNewCardState.InputForm -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SevTheme.shapes.large)
                    .border(2.dp, tint, SevTheme.shapes.large)
                    .aspectRatio(256f / 154f), contentAlignment = Alignment.Center
            ) {
                Column {
                    Icon(
                        Icons.Default.Add, contentDescription = "Añadir bonobús", tint = tint, modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        is CardsScreenNewCardState.CheckingCard -> {
            Shimmer(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SevTheme.shapes.large)
                    .aspectRatio(256f / 154f)
            )
        }
    }
}

@Preview
@Composable
private fun LoadedWithTransactionsPreview() {
    ScreenPreview {
        val cards = Stubs.cards
        val transactions: Map<CardId, TransactionsState.Loaded> =
            cards.associate { it.serialNumber to TransactionsState.Loaded(Stubs.cardInfoTransactions) }
        CardsScreen(
            CardsScreenState.Content(cards.andTransactions(transactions)),
            CardsScreenNewCardState.InputForm(),
            NfcState.ENABLED,
            {},
            {})
    }
}

@Preview
@Composable
private fun LoadedWithTransactionsLoadingPreview() {
    ScreenPreview {
        val cards = Stubs.cards
        val transactions = cards.associate { it.serialNumber to TransactionsState.Loading }
        CardsScreen(
            CardsScreenState.Content(cards.andTransactions(transactions)),
            CardsScreenNewCardState.InputForm(),
            NfcState.ENABLED,
            {},
            {})
    }
}

@Preview
@Composable
private fun LoadedWithTransactionsEmptyPreview() {
    ScreenPreview {
        val cards = Stubs.cards
        val transactions = cards.associate { it.serialNumber to TransactionsState.Empty }
        CardsScreen(
            CardsScreenState.Content(cards.andTransactions(transactions)),
            CardsScreenNewCardState.InputForm(),
            NfcState.ENABLED,
            {},
            {})
    }
}

@Preview
@Composable
private fun LoadedWithTransactionsErrorPreview() {
    ScreenPreview {
        val cards = Stubs.cards
        val transactions = cards.associate { it.serialNumber to TransactionsState.Error(Exception("Preview error")) }
        CardsScreen(
            CardsScreenState.Content(cards.andTransactions(transactions)),
            CardsScreenNewCardState.InputForm(),
            NfcState.ENABLED,
            {},
            {})
    }
}

@Preview
@Composable
private fun LoadedPreview() {
    ScreenPreview {
        CardsScreen(CardsScreenState.Content(Stubs.cards.andTransactions()), CardsScreenNewCardState.InputForm(), NfcState.ENABLED, {}, {})
    }
}

@Preview
@Composable
private fun ReorderingPreview() {
    ScreenPreview {
        CardsScreen(
            CardsScreenState.Content(Stubs.cards.andTransactions(), isReordering = true),
            CardsScreenNewCardState.InputForm(),
            NfcState.ENABLED,
            {},
            {})
    }
}

@Preview
@Composable
private fun LoadingPreview() {
    ScreenPreview {
        CardsScreen(CardsScreenState.Loading, CardsScreenNewCardState.InputForm(), NfcState.ENABLED, {}, {})
    }
}

@Preview
@Composable
private fun EmptyNfcEnabledPreview() {
    ScreenPreview {
        CardsScreen(CardsScreenState.Empty, CardsScreenNewCardState.InputForm(), NfcState.ENABLED, {}, {})
    }
}

@Preview
@Composable
private fun EmptyCheckingCardPreview() {
    ScreenPreview {
        CardsScreen(
            CardsScreenState.Empty,
            CardsScreenNewCardState.CheckingCard(Stubs.cards.first().fullSerialNumber.toString()),
            NfcState.ENABLED,
            {},
            {})
    }
}

@Preview
@Composable
private fun EmptyNfcDisabledPreview() {
    ScreenPreview {
        CardsScreen(CardsScreenState.Empty, CardsScreenNewCardState.InputForm(), NfcState.DISABLED, {}, {})
    }
}

@Preview
@Composable
private fun ErrorPreview() {
    ScreenPreview {
        CardsScreen(
            CardsScreenState.Error(Exception("Preview error")),
            CardsScreenNewCardState.InputForm(),

            NfcState.ENABLED,
            {},
            {})
    }
}
