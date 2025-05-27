package com.sloy.sevibus.feature.login

import android.content.res.Resources
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ContactSupport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Http
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sloy.sevibus.R
import com.sloy.sevibus.domain.model.LoggedUser
import com.sloy.sevibus.feature.debug.http.HttpOverlayState
import com.sloy.sevibus.infrastructure.BuildVariant
import com.sloy.sevibus.infrastructure.extensions.koinInjectOnUI
import com.sloy.sevibus.infrastructure.nightmode.NightModeSetting
import com.sloy.sevibus.infrastructure.nightmode.NightModeSelectorBottomSheet
import com.sloy.sevibus.infrastructure.session.FirebaseAuthService
import com.sloy.sevibus.ui.components.CircularIconButton
import com.sloy.sevibus.ui.components.SurfaceButton
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.theme.SevTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    val viewModel = koinViewModel<SettingsViewModel>()
    val state by viewModel.state.collectAsState()
    val nightModeState by viewModel.currentNightModeState.collectAsState()
    SettingsScreen(
        state,
        nightModeState,
        onLoginClick = { viewModel.onLoginClick(context) },
        onLogoutClick = { viewModel.onLogoutClick(context) },
        onNightModeChange = { viewModel.onNightModeChange(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsScreenState,
    currentNightMode: NightModeSetting,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onNightModeChange: (NightModeSetting) -> Unit = {}
) {
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollState = rememberScrollState()
    val hasElevation = scrollState.value != 0
    val shadowElevation by animateDpAsState(
        targetValue = if (hasElevation) 1.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "toolbar shadow animation"
    )

    val activity = LocalActivity.current
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.navigation_settings)) },
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
            if (showBottomSheet) {
                NightModeSelectorBottomSheet(
                    sheetState = sheetState,
                    currentMode = currentNightMode,
                    onDismissRequest = { showBottomSheet = false },
                    onModeSelected = { mode ->
                        onNightModeChange(mode)
                        activity?.splashScreen?.setSplashScreenTheme(
                            when (mode) {
                                NightModeSetting.LIGHT -> R.style.Theme_SeviBus4
                                NightModeSetting.DARK -> R.style.Theme_SeviBus4_Dark
                                NightModeSetting.FOLLOW_SYSTEM -> Resources.ID_NULL
                            }
                        )
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            showBottomSheet = false
                            AppCompatDelegate.setDefaultNightMode(mode.systemUiMode)
                        }
                    }
                )
            }

            Column(
                Modifier
                    .verticalScroll(scrollState)
                    .padding(paddingValues)
                    .safeDrawingPadding()
                    .padding(horizontal = 16.dp)
            ) {
                SectionTitle("Tu perfil")
                Card(Modifier.animateContentSize()) {
                    when (state) {
                        is SettingsScreenState.LoggedOut -> AccountContentLoggedOut(state, onLoginClick = onLoginClick)
                        is SettingsScreenState.LoggedIn -> AccountContentLoggedIn(state, onLogoutClick = onLogoutClick)
                    }
                }
                Spacer(Modifier.height(16.dp))

                SectionTitle("Apariencia")
                Card {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                            .clickable {
                                showBottomSheet = true
                            }
                            .padding(16.dp)
                    ) {
                        Icon(
                            Icons.Outlined.DarkMode,
                            contentDescription = null,
                            tint = SevTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(24.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text("Modo oscuro", style = SevTheme.typography.bodyStandardBold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                currentNightMode.title,
                                style = SevTheme.typography.bodySmall,
                                color = SevTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = SevTheme.colorScheme.outline,
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                SectionTitle("Servicios")
                Card {
                    val uriHandler = LocalUriHandler.current
                    Row(
                        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                            .clickable {
                                uriHandler.openUri("https://docs.google.com/forms/d/e/1FAIpQLSeSvAtEva0oKiPm-kQgIazXWqa2bjjgf-Y3fngVrm6SZSC6WA/viewform?usp=dialog")
                            }
                            .padding(16.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ContactSupport,
                            contentDescription = null,
                            tint = SevTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(24.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text("Danos tu opinión", style = SevTheme.typography.bodyStandardBold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "¿Tienes algún problema o sugerencia? ¡Ayúdanos a mejorar!",
                                style = SevTheme.typography.bodySmall,
                                color = SevTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = SevTheme.colorScheme.outline,
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                }
                if (BuildVariant.isDebug()) {
                    Spacer(Modifier.height(32.dp))
                    SectionTitle("Debug")
                    Card {
                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                        val coroutineScope = rememberCoroutineScope()
                        val firebaseService = koinInjectOnUI<FirebaseAuthService>()

                        Row(
                            verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                                .clickable {
                                    coroutineScope.launch { firebaseService?.signOut() }
                                }
                                .padding(16.dp)) {
                            Icon(
                                Icons.Outlined.LocalFireDepartment,
                                contentDescription = null,
                                tint = SevTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(24.dp)
                            )
                            Column(Modifier.weight(1f)) {
                                Text("Firebase logout \uD83D\uDC1E", style = SevTheme.typography.bodyStandardBold)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Cierra sesión en Firebase pero no en Google, para probar el auto-login.",
                                    style = SevTheme.typography.bodySmall,
                                    color = SevTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        DebugLocationSetting()

                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))

                        val httpOverlayState = koinInjectOnUI<HttpOverlayState>()
                        Row(
                            verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                                .clickable {
                                    httpOverlayState?.setVisibility(!httpOverlayState.isVisible)
                                }
                                .padding(16.dp)) {
                            Icon(
                                Icons.Outlined.Http,
                                contentDescription = null,
                                tint = SevTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(24.dp)
                            )
                            Column(Modifier.weight(1f)) {
                                Text("Mostrar peticiones \uD83D\uDC1E", style = SevTheme.typography.bodyStandardBold)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Muestra las peticiones HTTP en pantalla",
                                    style = SevTheme.typography.bodySmall,
                                    color = SevTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = httpOverlayState?.isVisible ?: false,
                                onCheckedChange = { httpOverlayState?.setVisibility(it) },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                    }
                }
                Spacer(Modifier.height(32.dp))
                Text(
                    "SeviBus es una aplicación sin ánimo de lucro, independiente no oficial. No tiene ninguna relación con la empresa Tussam ni el ayuntamiento de Sevilla. Los datos mostrados podrían no ser exactos.",
                    color = SevTheme.colorScheme.onSurfaceVariant,
                    style = SevTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
                HorizontalDivider(Modifier.padding(vertical = 24.dp))

                Image(
                    painter = painterResource(id = R.drawable.illustration_bus),
                    contentDescription = "Drawing of a stop with a heart",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .padding(top = 32.dp, bottom = 16.dp)
                        .height(100.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Text(
                    buildAnnotatedString {
                        append("Desarrollada por ")

                        withLink(
                            LinkAnnotation.Url(
                                "https://x.com/sloydev",
                                styles = TextLinkStyles(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.SemiBold,
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    pressedStyle = SpanStyle(background = SevTheme.colorScheme.surfaceContainerHighest)
                                )
                            )
                        ) {
                            append("Rafa Vázquez ↗")
                        }

                        append(" en Sevilla y diseñada por ")
                        withLink(
                            LinkAnnotation.Url(
                                "https://donbailon.com/2025",
                                styles = TextLinkStyles(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.SemiBold,
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    pressedStyle = SpanStyle(background = SevTheme.colorScheme.surfaceContainerHighest)
                                )
                            )
                        ) {
                            append("Alex Bailon ↗")
                        }
                        append(" en un pueblecito de la Costa Brava.")
                        toAnnotatedString()
                    },
                    style = SevTheme.typography.bodySmall,
                    color = SevTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Text(
                    "Versión ${version()}",
                    color = SevTheme.colorScheme.onSurfaceVariant,
                    style = SevTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }

        }
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = SevTheme.typography.headingSmall, modifier = Modifier.padding(bottom = 12.dp))
}

@Composable
private fun version(): String {
    if (LocalView.current.isInEditMode) {
        return "0.0.0"
    }
    val context = LocalContext.current
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    return packageInfo.versionName ?: "0.0.0"
}

@Composable
private fun AccountContentLoggedOut(state: SettingsScreenState.LoggedOut, onLoginClick: () -> Unit) {
    Column(Modifier.padding(16.dp)) {
        Text(
            "Inicia sesión para guardar tus paradas favoritas y tus tarjetas de bonobús.",
            style = SevTheme.typography.bodyExtraSmall,
            color = SevTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        SurfaceButton("Inicia sesión con Google", icon = {
            if (state.isInProgress) {
                CircularProgressIndicator(Modifier.size(18.dp))
            } else {
                Icon(
                    tint = Color.Unspecified,
                    painter = painterResource(id = com.google.android.gms.base.R.drawable.googleg_standard_color_18),
                    contentDescription = "Google logo",
                    modifier = Modifier.size(18.dp),
                )

            }
        }, onClick = onLoginClick, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun AccountContentLoggedIn(state: SettingsScreenState.LoggedIn, onLogoutClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row {
            AsyncImage(
                model = state.user.photoUrl,
                modifier = Modifier
                    .border(4.dp, SevTheme.colorScheme.outlineVariant, CircleShape)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .size(40.dp),
                contentDescription = "Profile image",
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(state.user.displayName, style = SevTheme.typography.bodyStandardBold)
                Text(
                    state.user.email,
                    textAlign = TextAlign.Center,
                    style = SevTheme.typography.bodySmall,
                    color = SevTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Tu perfil se usa para almacenar tus paradas favoritas y tus tarjetas de bonobús. Así siempre podras cambiar de móvil sin perder nada.",
            style = SevTheme.typography.bodyExtraSmall,
            color = SevTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        SurfaceButton("Cerrar sesión", onClick = onLogoutClick, modifier = Modifier.fillMaxWidth())
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun LoggedInPreview() {
    SevTheme {
        SettingsScreen(
            SettingsScreenState.LoggedIn(LoggedUser("Bonifacio Ramírez Alcántara", "pepe@gmail.com", null)),
            NightModeSetting.FOLLOW_SYSTEM,
            {},
            {})
    }
}

@Preview
@Composable
private fun LoggedOutPreview() {
    ScreenPreview {
        SettingsScreen(SettingsScreenState.LoggedOut(isInProgress = false), NightModeSetting.FOLLOW_SYSTEM, {}, {})
    }
}

@Preview
@Composable
private fun LoggedOutProgressPreview() {
    ScreenPreview {
        SettingsScreen(SettingsScreenState.LoggedOut(isInProgress = true), NightModeSetting.FOLLOW_SYSTEM, {}, {})
    }
}
