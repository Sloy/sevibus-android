package com.sloy.sevibus.feature.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.ui.preview.ScreenshotSuite
import com.sloy.sevibus.ui.preview.ScreenshotTest
import com.sloy.sevibus.ui.theme.SevTheme
import java.time.LocalDate

@Composable
fun CardPassSection(status: CardStatusUi.Pass, onMoreInfoClicked: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            stringResource(if (status.hasNextPeriod && status.expiredAgo == null) R.string.cards_current_period_until else R.string.cards_valid_until),
            style = SevTheme.typography.bodySmallBold,
        )
        Text(CardDateFormatter.date(status.validTo), style = SevTheme.typography.bodyStandard.copy(fontSize = 36.sp))
        status.expiredAgo?.let { elapsed ->
            WarningPill(stringResource(R.string.cards_expired_ago, elapsedText(elapsed)))
        }
        status.progress?.let { progress ->
            if (status.hasNextPeriod) TwoSegmentBar(progress) else SingleBar(progress)
        }
        if (status.expiredAgo != null) MoreInfoButton(onMoreInfoClicked, Modifier.fillMaxWidth())
        OperationsDelayNote()
    }
}

@Composable
private fun SingleBar(progress: PassProgress) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ProgressTrack(progress.fraction, Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(R.string.cards_period_since, CardDateFormatter.dayMonth(progress.validFrom)),
                style = SevTheme.typography.bodyExtraSmall,
                color = SevTheme.colorScheme.onSurfaceVariant,
            )
            Text(daysLeftText(progress.daysLeft), style = SevTheme.typography.bodyExtraSmallBold)
        }
    }
}

@Composable
private fun TwoSegmentBar(progress: PassProgress) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ProgressTrack(progress.fraction, Modifier.fillMaxWidth())
            Text(daysLeftText(progress.daysLeft), style = SevTheme.typography.bodyExtraSmallBold)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(SevTheme.colorScheme.onSurface.copy(alpha = 0.25f))
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                Text(stringResource(R.string.cards_next_period_paid), style = SevTheme.typography.bodyExtraSmallBold)
            }
        }
    }
}

@Composable
private fun ProgressTrack(fraction: Float, modifier: Modifier) {
    Box(
        modifier
            .height(6.dp)
            .clip(CircleShape)
            .background(SevTheme.colorScheme.outlineVariant)
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction)
                .clip(CircleShape)
                .background(SevTheme.colorScheme.onSurface)
        )
    }
}

@Composable
private fun CardPassSectionCasePreview(name: String, today: LocalDate = Stubs.cardCasesToday) {
    val status = cardStatusUi(cardCase(name).card, emptyList(), today) as CardStatusUi.Pass
    SevTheme {
        Surface {
            CardPassSection(status, onMoreInfoClicked = {})
        }
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@Preview
@Composable
internal fun CardPassSectionActivePreview() = CardPassSectionCasePreview("PassActive")

@ScreenshotTest(ScreenshotSuite.Components)
@Preview
@Composable
internal fun CardPassSectionNextPeriodPreview() = CardPassSectionCasePreview("PassNextPeriod")

@ScreenshotTest(ScreenshotSuite.Components)
@Preview
@Composable
internal fun CardPassSectionExpiredPreview() = CardPassSectionCasePreview("PassExpired")

@ScreenshotTest(ScreenshotSuite.Components)
@Preview
@Composable
internal fun CardPassSectionLastDayPreview() = CardPassSectionCasePreview("PassActive", today = LocalDate.of(2026, 11, 4))
