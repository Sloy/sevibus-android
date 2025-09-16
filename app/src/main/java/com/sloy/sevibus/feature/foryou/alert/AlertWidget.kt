package com.sloy.sevibus.feature.foryou.alert

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sloy.sevibus.R
import com.sloy.sevibus.domain.model.CardId
import com.sloy.sevibus.ui.components.InfoBannerComponent
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
            onAlertClicked = onAlertClicked
        )
    } else {
        AlertWidget(
            state = AlertState.Hidden,
            onAlertClicked = onAlertClicked
        )
    }
}

@Composable
private fun AlertWidget(
    state: AlertState,
    onAlertClicked: (CardId) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is AlertState.LowBalance -> {
            InfoBannerComponent(
                text = stringResource(R.string.foryou_card_low_balance_alert),
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                icon = Icons.Rounded.WarningAmber,
                iconColor = SevTheme.colorScheme.error,
                containerColor = SevTheme.colorScheme.surfaceContainer,
                textStyle = SevTheme.typography.bodySmallBold,
                action = {
                    TextButton(onClick = { onAlertClicked(state.cardId) }) {
                        Text(stringResource(R.string.foryou_card_low_balance_action))
                    }
                },
            )
        }

        is AlertState.NegativeBalance -> {
            InfoBannerComponent(
                text = stringResource(R.string.foryou_card_negative_balance_alert),
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                icon = Icons.Rounded.WarningAmber,
                iconColor = SevTheme.colorScheme.error,
                containerColor = SevTheme.colorScheme.surfaceContainer,
                textStyle = SevTheme.typography.bodySmallBold,
                action = {
                    TextButton(onClick = { onAlertClicked(state.cardId) }) {
                        Text(stringResource(R.string.foryou_card_low_balance_action))
                    }
                },
            )
        }

        is AlertState.Hidden -> {
            // Don't show anything
        }
    }
}

@Preview
@Composable
private fun AlertWidgetPreview() {
    ScreenPreview {
        AlertWidget(
            state = AlertState.LowBalance(cardId = 123456L),
            onAlertClicked = {},
        )
    }
}