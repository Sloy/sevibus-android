package com.sloy.sevibus.feature.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.domain.model.CardInfo

private val ThumbnailShape = RoundedCornerShape(4.dp)

@Composable
fun CardThumbnail(card: CardInfo, modifier: Modifier = Modifier) {
    val (backgroundColor, accentColor) = CardColors.getCardColors(card)
    Column(
        modifier
            .size(width = 38.dp, height = 25.dp)
            .shadow(2.dp, ThumbnailShape)
            .background(backgroundColor)
    ) {
        Spacer(Modifier.weight(1f))
        Box(
            Modifier
                .fillMaxWidth()
                .height(7.dp)
                .background(accentColor)
        )
    }
}
