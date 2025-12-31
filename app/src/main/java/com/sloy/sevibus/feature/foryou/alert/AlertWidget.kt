package com.sloy.sevibus.feature.foryou.alert

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sloy.sevibus.R
import com.sloy.sevibus.domain.model.CardId
import com.sloy.sevibus.infrastructure.analytics.events.Clicks
import com.sloy.sevibus.ui.components.SmallSurfaceButton
import com.sloy.sevibus.ui.preview.ScreenPreview
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
    when (state) {
        AlertState.Hidden -> return
        is AlertState.LowBalance -> AlertCard(state.cardId, false, onDismissAlert, onAlertClicked, modifier)
        is AlertState.NegativeBalance -> AlertCard(state.cardId, true, onDismissAlert, onAlertClicked, modifier)
    }
}

@Composable
private fun AlertCard(
    cardId: CardId,
    isNegative: Boolean,
    onDismissAlert: () -> Unit,
    onAlertClicked: (CardId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = SevTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Rounded.WarningAmber,
                tint = SevTheme.colorScheme.error.takeOrElse { SevTheme.colorScheme.error },
                modifier = Modifier.padding(end = 16.dp),
                contentDescription = null,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.foryou_card_alert_title),
                    style = SevTheme.typography.headingSmall,
                )
                Text(
                    style = SevTheme.typography.bodySmall,
                    text = stringResource(
                        if (isNegative) R.string.foryou_card_alert_negative_balance_description
                        else R.string.foryou_card_alert_low_balance_description
                    ),
                    color = SevTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SmallSurfaceButton(
                        onClick = { onDismissAlert() }, text = stringResource(R.string.foryou_card_alert_action_dismiss),
                        icon = {
                            Icon(Icons.Outlined.Close, contentDescription = null, tint = SevTheme.colorScheme.primary)
                        }
                    )
                    SmallSurfaceButton(
                        onClick = { onAlertClicked(cardId) }, text = stringResource(R.string.foryou_card_alert_action_see),
                        icon = {
                            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = SevTheme.colorScheme.primary)
                        }
                    )
                }
            }

        }
    }
}

@Preview
@Composable
internal fun AlertWidgetPreview() {
    ScreenPreview {
        AlertWidget(
            state = AlertState.LowBalance(cardId = 123456L),
            onAlertClicked = {},
            onDismissAlert = {}
        )
    }
}