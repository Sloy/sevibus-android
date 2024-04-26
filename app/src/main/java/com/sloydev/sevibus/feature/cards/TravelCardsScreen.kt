package com.sloydev.sevibus.feature.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sloydev.sevibus.R
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.domain.model.TravelCard
import com.sloydev.sevibus.navigation.TopLevelDestination
import com.sloydev.sevibus.ui.components.SevCenterAlignedTopAppBar
import com.sloydev.sevibus.ui.preview.ScreenPreview
import com.sloydev.sevibus.ui.theme.SevTheme

fun NavGraphBuilder.travelCardsRoute() {
    composable(TopLevelDestination.CARDS.route) {
        TravelCardsScreen(Stubs.cards)
    }
}

@Composable
fun TravelCardsScreen(cards: List<TravelCard>) {
    Column {
        SevCenterAlignedTopAppBar(title = { Text(stringResource(R.string.navigation_cards)) })

        val state = rememberPagerState { cards.size + 1 }

        Column(Modifier.verticalScroll(rememberScrollState())) {

            HorizontalPager(
                state = state,
                contentPadding = PaddingValues(horizontal = 48.dp, vertical = 36.dp),
                pageSpacing = 16.dp,
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
            ) { page ->
                if (page <= cards.lastIndex) {
                    CardPictureItem(cards[page])
                } else {
                    CardAddMoreItem()
                }
            }

            if (state.currentPage <= cards.lastIndex) {
                val currentCard = cards[state.currentPage]
                ExistingCardDetail(currentCard)
            } else {
                NewCardDetail()
            }
        }
    }
}

@Composable
private fun NewCardDetail() {
    Column(Modifier.padding(16.dp)) {
        Text("Añadir tarjeta", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.size(16.dp))
        var input by remember { mutableStateOf("") }
        TextField(
            value = input,
            label = { Text("Número de serie") },
            placeholder = { Text("1234 1234 1234") },
            leadingIcon = { Icon(Icons.Default.AddCard, contentDescription = null) },
            supportingText = { Text("El número de serie se encuentra en la parte trasera de la tarjeta. Suele ser de 12 dígitos.") },
            keyboardOptions = KeyboardOptions.Default.copy(
                autoCorrectEnabled = false, //TODO added because of ambiguous copy function in alpha05, should be removed
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
            ),
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.size(32.dp))
        Text("o bien", Modifier.align(Alignment.CenterHorizontally), style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.size(32.dp))
        ExtendedFloatingActionButton(
            onClick = { /*TODO*/ },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Usar la cámara")
        }
    }
}

@Composable
private fun ExistingCardDetail(currentCard: TravelCard) {
    WarningNotice()
    Spacer(Modifier.size(16.dp))
    CardBalanceItem(currentCard)
    Spacer(Modifier.size(32.dp))
    TravelCardInfoElement(currentCard)
    Spacer(Modifier.size(32.dp))
    TravelCardTransactionsElement(Stubs.travelCardTransactions)
    Spacer(Modifier.size(32.dp))
    DeleteButton()
    Spacer(Modifier.size(32.dp))
}

@Composable
fun WarningNotice() {
    OutlinedCard(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.size(8.dp))
            Text(
                "Los datos pueden tardar 24h en actualizarse",
                style = SevTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DeleteButton() {
    OutlinedButton(
        onClick = { /*TODO*/ },
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
private fun CardPictureItem(card: TravelCard) {
    Image(
        painterResource(codeToResource(card.code)),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(256f / 154f)
            .shadow(16.dp, MaterialTheme.shapes.medium),
        contentDescription = null
    )
}

@Composable
private fun CardAddMoreItem() {
    val tint = Color.Gray.copy(alpha = 0.5f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .border(2.dp, tint, MaterialTheme.shapes.medium)
            .aspectRatio(256f / 154f), contentAlignment = Alignment.Center
    ) {
        Column {
            Icon(
                Icons.Default.Add, contentDescription = "Añadir bonobús", tint = tint, modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
private fun codeToResource(code: Int): Int {
    val res = LocalContext.current.resources
    val context = LocalContext.current.applicationContext
    //TODO use a different method or get images by URL
    return res.getIdentifier("t$code", "drawable", context.packageName)
}

@Preview
@Composable
private fun CardsScreenPreview() {
    ScreenPreview {
        TravelCardsScreen(Stubs.cards)
    }
}