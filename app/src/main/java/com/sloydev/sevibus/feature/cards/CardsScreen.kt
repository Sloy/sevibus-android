package com.sloydev.sevibus.feature.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.R
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.ui.ScreenPreview

@Composable
fun CardsRoute() {
    CardsScreen(Stubs.cards)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CardsScreen(cards: List<CardInfo>) {

    Column {
        CenterAlignedTopAppBar(title = { Text(stringResource(R.string.navigation_cards)) })

        val state = rememberPagerState { cards.size }

        /*LaunchedEffect(state.pageCount) {
            state.scrollToPage(1)
        }*/

        Column(Modifier.verticalScroll(rememberScrollState())) {

            HorizontalPager(
                state = state,
                contentPadding = PaddingValues(48.dp),
                pageSpacing = 16.dp,
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
            ) { page ->
                CardPictureItem(cards[page])
            }

            CardBalanceItem()
            Spacer(Modifier.size(16.dp))
            CardInfoCard(cards[0])
            Spacer(Modifier.size(16.dp))
            CardTransactionsCard(Stubs.cardTransactions)
            Spacer(Modifier.size(16.dp))
        }
    }
}

@Composable
private fun CardPictureItem(card: CardInfo) {
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
private fun CardBalanceItem() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Saldo disponible", style = MaterialTheme.typography.labelMedium)
        Row(verticalAlignment = Alignment.Bottom) {
            Text("8", style = MaterialTheme.typography.displayMedium)
            Text(",36 €", style = MaterialTheme.typography.displaySmall)
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
        CardsScreen(Stubs.cards)
    }
}