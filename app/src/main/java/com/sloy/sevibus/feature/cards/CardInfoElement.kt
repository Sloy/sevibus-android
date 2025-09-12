package com.sloy.sevibus.feature.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun CardInfoElement(card: CardInfo, onTopUpClicked: (CardInfo) -> Unit) {

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

            /*TitleSubtitleItem("Nombre", card.customName ?: card.type, endAccessory = {
                IconButton(onClick = { *//*TODO*//* }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar nombre")
                    }
                })*/
            //HorizontalDivider()

            TitleSubtitleItem(stringResource(R.string.cards_serial_number_label), card.formattedSerialNumber)
            HorizontalDivider()

            TitleSubtitleItem(stringResource(R.string.cards_type_label), card.type)

            if (card.balance != null) {
                HorizontalDivider()
                TitleSubtitleItem(
                    stringResource(R.string.cards_top_up), stringResource(R.string.cards_top_up_description),
                    onClick = {
                        onTopUpClicked(card)
                    }, endAccessory = {
                        Icon(
                            Icons.AutoMirrored.Filled.OpenInNew,
                            tint = SevTheme.colorScheme.onSurfaceVariant,
                            contentDescription = "Recargar"
                        )
                    })

            }

        }
    }
}

@Composable
private fun TitleSubtitleItem(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    endAccessory: @Composable (() -> Unit)? = null,
) {
    val clickModifier = if (onClick != null) modifier.clickable(onClick = onClick, role = Role.Button) else modifier
    Row(clickModifier, verticalAlignment = Alignment.CenterVertically) {
        Column(
            Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            Text(title, style = SevTheme.typography.bodySmallBold)
            Text(
                subtitle,
                style = SevTheme.typography.bodyStandard,
                color = SevTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(Modifier.padding(horizontal = 16.dp)) {
            CompositionLocalProvider(LocalContentColor provides SevTheme.colorScheme.onSurfaceVariant) {
                endAccessory?.let { it() }
            }
        }
    }
}

@Preview
@Composable
private fun CardInfoCardPreview() {
    SevTheme {
        CardInfoElement(Stubs.cardWithAllFields, {})
    }
}
