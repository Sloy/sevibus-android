package com.sloy.sevibus.feature.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sloy.sevibus.R
import com.sloy.sevibus.infrastructure.FeatureFlags
import com.sloy.sevibus.ui.theme.SevTheme
import java.time.LocalDate

const val TUSSAM_TITLES_URL = "https://www.tussam.es/es/titulos-de-viajes"

val LocalToday = staticCompositionLocalOf<LocalDate> { LocalDate.now() }

@Composable
fun elapsedText(elapsed: Elapsed): String = when (elapsed.unit) {
    ElapsedUnit.DAYS -> pluralStringResource(R.plurals.cards_elapsed_days, elapsed.amount, elapsed.amount)
    ElapsedUnit.MONTHS -> pluralStringResource(R.plurals.cards_elapsed_months, elapsed.amount, elapsed.amount)
    ElapsedUnit.YEARS -> pluralStringResource(R.plurals.cards_elapsed_years, elapsed.amount, elapsed.amount)
}

@Composable
fun daysLeftText(daysLeft: Int): String =
    if (daysLeft == 0) stringResource(R.string.cards_last_day)
    else pluralStringResource(R.plurals.cards_days_left, daysLeft, daysLeft)

@Composable
internal fun WarningPill(text: String, modifier: Modifier = Modifier) {
    val colors = SevTheme.extendedColors
    Text(
        text = text,
        style = SevTheme.typography.bodySmallBold.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
        color = colors.onWarningContainer,
        modifier = modifier
            .background(colors.warningContainer, CircleShape)
            .padding(vertical = 3.dp, horizontal = 10.dp),
    )
}

@Composable
fun MoreInfoButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = SevTheme.colorScheme.outlineVariant,
            contentColor = SevTheme.colorScheme.onSurface,
        ),
    ) {
        Text(
            text = stringResource(R.string.cards_more_info_button),
            style = SevTheme.typography.bodyStandardBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
fun OperationsDelayNote(modifier: Modifier = Modifier) {
    if (!FeatureFlags.showCardUpdateWarning) return
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        Icon(
            Icons.Outlined.Info,
            contentDescription = null,
            tint = SevTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            stringResource(R.string.cards_operations_delay_note),
            style = SevTheme.typography.bodySmall,
            color = SevTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
