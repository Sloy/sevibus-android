package com.sloy.sevibus.feature.cards

import android.content.ClipData
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.ui.preview.ScreenshotSuite
import com.sloy.sevibus.ui.preview.ScreenshotTest
import com.sloy.sevibus.ui.theme.SevTheme
import kotlinx.coroutines.launch

private val VALUE_MAX_BLUR = 6.dp

@Composable
fun CardDetailsElement(details: CardDetailsUi, onCopySerial: () -> Unit) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    Card(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DetailRow(
                stringResource(R.string.cards_serial_number_label),
                details.serial,
                Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
            )
            val copyDescription = stringResource(R.string.cards_copy_serial)
            IconButton(
                onClick = {
                    val serial = details.serial.replace(" ", "")
                    scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(serial, serial))) }
                    onCopySerial()
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SevTheme.colorScheme.outlineVariant),
            ) {
                Icon(Icons.Outlined.ContentCopy, contentDescription = copyDescription, modifier = Modifier.size(20.dp))
            }
        }
        details.period?.let { period ->
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            DetailRow(
                stringResource(if (period.isCurrentPeriod) R.string.cards_current_period_label else R.string.cards_validity_period_label),
                CardDateFormatter.range(period.from, period.to),
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }
        details.physicalExpiry?.let { expiry ->
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            DetailRow(
                stringResource(R.string.cards_physical_expiry_label),
                CardDateFormatter.date(expiry),
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = SevTheme.typography.bodySmall, color = SevTheme.colorScheme.onSurfaceVariant)
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                fadeIn(tween(SevTheme.motion.duration.d200, easing = SevTheme.motion.easing.ease)) togetherWith
                    fadeOut(tween(SevTheme.motion.duration.d200, easing = SevTheme.motion.easing.ease))
            },
            label = "detailValue",
        ) { animatedValue ->
            val blurRadius by transition.animateDp(
                transitionSpec = { tween(SevTheme.motion.duration.d200, easing = SevTheme.motion.easing.ease) },
                label = "detailValueBlur",
            ) { if (it == EnterExitState.Visible) 0.dp else VALUE_MAX_BLUR }
            Text(
                animatedValue,
                modifier = Modifier
                    .blur(blurRadius, BlurredEdgeTreatment.Unbounded)
                    .fillMaxWidth(),
                style = SevTheme.typography.bodyStandardBold,
            )
        }
    }
}

@Composable
private fun CardDetailsElementCasePreview(name: String) {
    SevTheme {
        Surface {
            CardDetailsElement(cardDetailsUi(cardCase(name).card, Stubs.cardCasesToday), onCopySerial = {})
        }
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@Preview
@Composable
internal fun CardDetailsElementBalancePreview() = CardDetailsElementCasePreview("BalanceNormal")

@ScreenshotTest(ScreenshotSuite.Components)
@Preview
@Composable
internal fun CardDetailsElementPassPreview() = CardDetailsElementCasePreview("PassActive")

@ScreenshotTest(ScreenshotSuite.Components)
@Preview
@Composable
internal fun CardDetailsElementNextPeriodPreview() = CardDetailsElementCasePreview("PassNextPeriod")
