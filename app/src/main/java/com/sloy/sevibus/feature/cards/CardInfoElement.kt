package com.sloy.sevibus.feature.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.ui.preview.ScreenshotSuite
import com.sloy.sevibus.ui.preview.ScreenshotTest
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun CardInfoElement(card: CardInfo) {
    Column {
        Text(
            stringResource(R.string.cards_data_section),
            style = SevTheme.typography.headingSmall,
            modifier = Modifier.padding(bottom = 12.dp, start = 16.dp)
        )
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
        Text(
            subtitle,
            style = SevTheme.typography.bodyStandard,
            color = SevTheme.colorScheme.onSurfaceVariant
        )
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
