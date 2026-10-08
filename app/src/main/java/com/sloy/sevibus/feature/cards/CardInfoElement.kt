package com.sloy.sevibus.feature.cards

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.ui.preview.ScreenshotSuite
import com.sloy.sevibus.ui.preview.ScreenshotTest
import com.sloy.sevibus.ui.theme.SevTheme

private val SUBTITLE_MAX_BLUR = 6.dp

@Composable
fun CardInfoElement(card: CardInfo) {
    Column {
        Card(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {

            TitleSubtitleItem(stringResource(R.string.cards_serial_number_label), card.formattedSerialNumber)
            HorizontalDivider()

            TitleSubtitleItem(stringResource(R.string.cards_type_label), card.type)
        }
    }
}

@Composable
private fun TitleSubtitleItem(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(title, style = SevTheme.typography.bodySmallBold)
        AnimatedContent(
            targetState = subtitle,
            transitionSpec = {
                fadeIn(tween(SevTheme.motion.duration.d200, easing = SevTheme.motion.easing.ease)) togetherWith
                    fadeOut(tween(SevTheme.motion.duration.d200, easing = SevTheme.motion.easing.ease))
            },
            label = "subtitle",
        ) { value ->
            val blurRadius by transition.animateDp(
                transitionSpec = { tween(SevTheme.motion.duration.d200, easing = SevTheme.motion.easing.ease) },
                label = "subtitleBlur",
            ) { if (it == EnterExitState.Visible) 0.dp else SUBTITLE_MAX_BLUR }
            Text(
                value,
                modifier = Modifier.blur(blurRadius, BlurredEdgeTreatment.Unbounded).fillMaxWidth(),
                style = SevTheme.typography.bodyStandard,
                color = SevTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@Preview
@Composable
internal fun CardInfoElementPreview() {
    SevTheme {
        CardInfoElement(Stubs.cardWithAllFields)
    }
}
