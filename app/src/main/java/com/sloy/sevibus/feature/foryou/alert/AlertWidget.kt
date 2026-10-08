package com.sloy.sevibus.feature.foryou.alert

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.CardId
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.feature.cards.CardThumbnail
import com.sloy.sevibus.infrastructure.analytics.events.Clicks
import com.sloy.sevibus.ui.formatter.MoneyFormatter
import com.sloy.sevibus.ui.preview.ScreenshotSuite
import com.sloy.sevibus.ui.preview.ScreenshotTest
import com.sloy.sevibus.ui.theme.SevTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun AlertWidget(onAlertClicked: (CardId) -> Unit) {
    if (!LocalView.current.isInEditMode) {
        val viewModel = koinViewModel<AlertViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        AlertWidget(
            state = state,
            onAlertClicked = { cardId ->
                viewModel.onTrack(Clicks.CardAlertViewClicked)
                onAlertClicked(cardId)
            },
            onDismissAlert = {
                viewModel.onTrack(Clicks.CardAlertDismissClicked)
                viewModel.onDismissAlert()
            }
        )
    } else {
        AlertWidget(
            state = AlertState.Hidden,
            onAlertClicked = onAlertClicked,
            onDismissAlert = {}
        )
    }
}

@Composable
private fun AlertWidget(
    state: AlertState,
    onAlertClicked: (CardId) -> Unit,
    onDismissAlert: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleState = state.takeUnless { it == AlertState.Hidden }
    var lastVisibleState by remember { mutableStateOf(visibleState) }
    if (visibleState != null) lastVisibleState = visibleState
    val duration = SevTheme.motion.duration
    AnimatedVisibility(
        visible = visibleState != null,
        modifier = modifier,
        enter = SevTheme.motion.anim.fadeAndScaleIn(
            expandDuration = duration.d400,
            fadeDuration = duration.d200,
            scaleDuration = duration.d400,
            initialScale = 0.9f,
            contentDelay = duration.d150,
        ),
        exit = SevTheme.motion.anim.fadeAndScaleOut(
            fadeDuration = duration.d150,
            scaleDuration = duration.d200,
            shrinkDuration = duration.d300,
            targetScale = 0.9f,
            shrinkDelay = duration.d100,
        ),
    ) {
        when (val displayedState = lastVisibleState) {
            is AlertState.LowBalance -> LowBalanceCard(
                card = displayedState.card,
                title = R.string.foryou_card_alert_low_balance_title,
                onClick = { onAlertClicked(displayedState.card.serialNumber) },
                onDismiss = onDismissAlert,
            )

            is AlertState.NegativeBalance -> LowBalanceCard(
                card = displayedState.card,
                title = R.string.foryou_card_alert_negative_balance_title,
                onClick = { onAlertClicked(displayedState.card.serialNumber) },
                onDismiss = onDismissAlert,
            )

            AlertState.Hidden, null -> Unit
        }
    }
}

@Composable
private fun LowBalanceCard(
    card: CardInfo,
    @StringRes title: Int,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SevTheme.extendedColors
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = colors.warningSurface,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, top = 14.dp, bottom = 14.dp, end = 4.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.widthIn(min = 52.dp),
                ) {
                    CardThumbnail(card)
                    Text(
                        text = MoneyFormatter.fromCents(card.balance ?: 0),
                        style = SevTheme.typography.bodySmallBold.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum",
                        ),
                        color = colors.onWarningSurface,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
                Column {
                    Text(
                        text = stringResource(title),
                        style = SevTheme.typography.headingSmall,
                        color = SevTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.foryou_card_alert_tap_to_top_up),
                        style = SevTheme.typography.bodySmall,
                        color = colors.onWarningSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .padding(top = 6.dp, end = 4.dp)
                    .size(44.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.foryou_card_alert_dismiss),
                    tint = SevTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@PreviewLightDark
@Composable
internal fun AlertWidgetLowBalancePreview() {
    SevTheme {
        Surface {
            AlertWidget(
                state = AlertState.LowBalance(Stubs.cards[0].copy(balance = 164)),
                onAlertClicked = {},
                onDismissAlert = {},
            )
        }
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@PreviewLightDark
@Composable
internal fun AlertWidgetNegativeBalancePreview() {
    SevTheme {
        Surface {
            AlertWidget(
                state = AlertState.NegativeBalance(Stubs.cards[3]),
                onAlertClicked = {},
                onDismissAlert = {},
            )
        }
    }
}
