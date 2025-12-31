package com.sloy.sevibus.feature.cards

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.R
import com.sloy.sevibus.ui.components.CircularIconButton
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.theme.SevTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsHelpScreen() {
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollState = rememberScrollState()
    val hasElevation = scrollState.value != 0
    val shadowElevation by animateDpAsState(
        targetValue = if (hasElevation) 1.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "toolbar shadow animation"
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.navigation_cards_help)) },
                navigationIcon = {
                    CircularIconButton(
                        onClick = { onBackPressedDispatcher?.onBackPressed() },
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close screen")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = SevTheme.colorScheme.background,
                    scrolledContainerColor = SevTheme.colorScheme.background,
                ),
                modifier = Modifier.shadow(elevation = shadowElevation, spotColor = SevTheme.colorScheme.onSurfaceVariant)
            )
        },
        content = { paddingValues ->
            Column(
                Modifier
                    .verticalScroll(scrollState)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                Question("La información de la tarjeta no se actualiza")
                Answer("La información de la tarjeta se actualiza en un plazo de <b>24h</b>. Por lo tanto sólo verás el saldo y la actividad del día anterior.")

                Question("¿Puedo validar mi viaje con la aplicación en el bus?")
                Answer("Ojalá, pero no. SeviBus sólo muestra la información de la tarjeta pero no la reemplaza. Debes seguir pasando tu tarjeta física al subirte al bus o tranvía.")

                Question("He recargado la tarjeta pero no me aparece el dinero")
                Answer("La recaga online será efectiva en un plazo de <b>24h</b>, y después de validar la tarjeta en un bus o tranvía. Hasta entonces, seguriá apareciendo el saldo anterior.")

                Question("He utilizado la tarjeta pero no me aparece el último viaje")
                Answer("El saldo de la tarjeta se actualiza en un plazo de <b>24h</b>. Por lo tanto sólo verás el saldo y la actividad del día anterior.")

                Question("Número de tarjeta no es válido")
                Answer("Si tu tarjeta es nueva, puede que no esté activada. Debes validarla en un bus o tranvía, vuelve a probar en un plazo de 24 horas. Si ya la has validado y sigue sin funcionar, ponte en contacto con <a href='mailto:sevibus@sloydev.com'>nosotros</a>.")

                Question("¿Por qué no se actualiza la información hasta pasadas 24 horas?")
                Answer("Es una limitación técnica de Tussam, y no hay nada que podamos hacer desde SeviBus. Recuerda que SeviBus es una app independiente y no relacionada con Tussam.")

                Question("Al pasar mi tarjeta en el bus o tranvía me da error")
                Answer("Desde SeviBus no podemos ayudarte con problemas al usar la tarjeta. Ponte en contacto con <a href='https://www.tussam.es/contacto/'>Tussam</a>.")
            }
        }
    )

}

@Composable
private fun Question(text: String, modifier: Modifier = Modifier) {
    Text(text, style = SevTheme.typography.headingSmall, modifier = modifier.padding(bottom = 4.dp))
}

@Composable
private fun Answer(htmlText: String, modifier: Modifier = Modifier) {
    HtmlText(htmlText, style = SevTheme.typography.bodySmall, modifier = modifier.padding(bottom = 40.dp))
}

@Composable
private fun HtmlText(
    html: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    linkColor: Color = SevTheme.colorScheme.primary,
) {
    val annotated = AnnotatedString.fromHtml(
        html, linkStyles = TextLinkStyles(
            style = SpanStyle(
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline,
                color = linkColor
            ),
            pressedStyle = SpanStyle(background = SevTheme.colorScheme.surfaceContainerHighest)
        )
    )
    Text(
        text = annotated,
        modifier = modifier,
        style = style
    )
}

@Preview
@Composable
internal fun CardHelpPreview() {
    ScreenPreview {
        CardsHelpScreen()
    }
}
