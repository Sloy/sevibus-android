package com.sloy.sevibus.feature.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCardOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.R
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun CardExpiredNotice(status: CardStatusUi.ExpiredCard, onMoreInfoClicked: () -> Unit, modifier: Modifier = Modifier) {
    val colors = SevTheme.extendedColors
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.warningSurface)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Outlined.CreditCardOff, contentDescription = null, tint = colors.onWarningSurfaceVariant)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.cards_card_expired_title), style = SevTheme.typography.headingSmall, color = colors.onWarningSurface)
                status.expiredOn?.let {
                    Text(
                        stringResource(R.string.cards_card_expired_text, CardDateFormatter.date(it)),
                        style = SevTheme.typography.bodySmall,
                        color = colors.onWarningSurfaceVariant,
                    )
                }
            }
        }
        MoreInfoButton(onMoreInfoClicked, Modifier.fillMaxWidth())
    }
}
