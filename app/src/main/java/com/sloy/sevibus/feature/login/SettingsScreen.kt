package com.sloy.sevibus.feature.login

import android.content.res.Resources
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Http
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Work
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
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.LoggedUser
import com.sloy.sevibus.infrastructure.BuildVariant
import com.sloy.sevibus.infrastructure.extensions.koinInjectOnUI
import com.sloy.sevibus.infrastructure.nightmode.NightModeSelectorBottomSheet
import com.sloy.sevibus.infrastructure.nightmode.NightModeSetting
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
    val healthCheckState by viewModel.healthCheckState.collectAsState()
    val nightModeState by viewModel.currentNightModeState.collectAsState()
    val analyticsEnabled by viewModel.currentAnalyticsState.collectAsState()
    SettingsScreen(
        state,
        healthCheckState,
        nightModeState,
        analyticsEnabled,
        onLoginClick = { viewModel.onLoginClick(context) },
        onLogoutClick = { viewModel.onLogoutClick(context) },
        onNightModeChange = { viewModel.onNightModeChange(it) },
        onAnalyticsChange = { viewModel.onAnalyticsChange(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsScreenState,
    healthCheckState: HealthCheckState,
    currentNightMode: NightModeSetting,
    analyticsEnabled: Boolean,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onNightModeChange: (NightModeSetting) -> Unit = {},
    onAnalyticsChange: (Boolean) -> Unit = {}
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
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close_screen))
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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            activity?.splashScreen?.setSplashScreenTheme(
                                when (mode) {
                                    NightModeSetting.LIGHT -> R.style.Theme_SeviBus4
                                    NightModeSetting.DARK -> R.style.Theme_SeviBus4_Dark
                                    NightModeSetting.FOLLOW_SYSTEM -> Resources.ID_NULL
                                }
                            )
                        }
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
                SettingsSection(stringResource(R.string.settings_your_profile)) {
                    Card(Modifier.animateContentSize()) {
                        when (state) {
                            is SettingsScreenState.LoggedOut -> AccountContentLoggedOut(state, onLoginClick = onLoginClick)
                            is SettingsScreenState.LoggedIn -> AccountContentLoggedIn(state, onLogoutClick = onLogoutClick)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                SettingsSection(stringResource(R.string.settings_appearance)) {
                    SettingsItem(
                        title = stringResource(R.string.settings_dark_mode),
                        subtitle = stringResource(currentNightMode.titleRes),
                        leadingIcon = Icons.Outlined.DarkMode,
                        onClick = { showBottomSheet = true },
                        endIcon = Icons.Default.ChevronRight
                    )
                }

                Spacer(Modifier.height(16.dp))
                SettingsSection(stringResource(R.string.settings_services)) {
                    SettingsItem(
                        title = stringResource(R.string.settings_analytics),
                        subtitle = stringResource(R.string.settings_analytics_description),
                        leadingIcon = Icons.Outlined.Analytics,
                        onClick = { onAnalyticsChange(!analyticsEnabled) },
                        endComponent = {
                            Switch(
                                checked = analyticsEnabled,
                                onCheckedChange = onAnalyticsChange,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    val uriHandler = LocalUriHandler.current
                    SettingsItem(
                        title = stringResource(R.string.settings_give_feedback),
                        subtitle = stringResource(R.string.settings_feedback_description),
                        leadingIcon = Icons.AutoMirrored.Outlined.ContactSupport,
                        onClick = { uriHandler.openUri("https://docs.google.com/forms/d/e/1FAIpQLSeSvAtEva0oKiPm-kQgIazXWqa2bjjgf-Y3fngVrm6SZSC6WA/viewform?usp=dialog") },
                        endIcon = Icons.Default.ChevronRight
                    )
                }

                if (BuildVariant.isDebug()) {
                    Spacer(Modifier.height(32.dp))
                    SettingsSection(stringResource(R.string.settings_debug_menu)) {
                        val coroutineScope = rememberCoroutineScope()
                        val firebaseService = koinInjectOnUI<FirebaseAuthService>()
                        SettingsItem(
                            title = stringResource(R.string.settings_firebase_logout),
                            subtitle = stringResource(R.string.settings_firebase_logout_description),
                            leadingIcon = Icons.Outlined.LocalFireDepartment,
                            onClick = { coroutineScope.launch { firebaseService?.signOut() } },
                        )
                    }
                }

                if (healthCheckState !is HealthCheckState.NotAvailable) {
                    Spacer(Modifier.height(32.dp))
                    SettingsSection(stringResource(R.string.settings_server_health_check)) {
                        if (healthCheckState is HealthCheckState.Error) {
                            SettingsItem(stringResource(R.string.common_error), healthCheckState.message, Icons.Outlined.Warning)
                        }
                        if (healthCheckState is HealthCheckState.Success) {
                            with(healthCheckState.data) {
                                host?.let {
                                    SettingsItem(stringResource(R.string.settings_host), it, Icons.Outlined.Http)
                                }
                                environment?.let {
                                    SettingsItem(stringResource(R.string.settings_environment), it, Icons.Outlined.Work)
                                }
                                database?.let {
                                    SettingsItem(stringResource(R.string.settings_database), it, Icons.Outlined.AccountTree)
                                }
                                provider?.let {
                                    SettingsItem(stringResource(R.string.settings_provider), it, Icons.Outlined.Cloud)
                                }
                                version?.let {
                                    SettingsItem(stringResource(R.string.settings_version), it, Icons.Outlined.Timeline)
                                }
                                clientVersion?.let {
                                    SettingsItem(stringResource(R.string.settings_client_version), it, Icons.Outlined.PhoneAndroid)
                                }
                                ip?.let {
                                    SettingsItem(stringResource(R.string.settings_ip), it, Icons.Outlined.Map)
                                }
                                timestamp?.let {
                                    SettingsItem(stringResource(R.string.settings_timestamp), it, Icons.Outlined.AccessTime)
                                }
                                uptime?.let {
                                    SettingsItem(
                                        stringResource(R.string.settings_uptime),
                                        stringResource(R.string.settings_uptime_seconds, it),
                                        Icons.Outlined.Timer
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
                Footer()
            }

        }
    )
}

@Composable
private fun ColumnScope.Footer() {
    Text(
        stringResource(R.string.settings_app_disclaimer),
        color = SevTheme.colorScheme.onSurfaceVariant,
        style = SevTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
    )
    HorizontalDivider(Modifier.padding(vertical = 24.dp))

    Image(
        painter = painterResource(id = R.drawable.illustration_bus),
        contentDescription = stringResource(R.string.cd_stop_heart),
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .padding(top = 32.dp, bottom = 16.dp)
            .height(100.dp)
            .align(Alignment.CenterHorizontally)
    )
    Text(
        buildAnnotatedString {
            append(stringResource(R.string.settings_developed_by) + " ")

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
                append(stringResource(R.string.settings_rafa_vazquez))
            }

            append(" " + stringResource(R.string.settings_in_sevilla_designed_by) + " ")
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
                append(stringResource(R.string.settings_alex_bailon))
            }
            append(" " + stringResource(R.string.settings_costa_brava))
            toAnnotatedString()
        },
        style = SevTheme.typography.bodySmall,
        color = SevTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )

    Text(
        stringResource(R.string.common_version_text, version()),
        color = SevTheme.colorScheme.onSurfaceVariant,
        style = SevTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    )
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
            stringResource(R.string.settings_login_to_save_favorites),
            style = SevTheme.typography.bodyExtraSmall,
            color = SevTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        SurfaceButton(stringResource(R.string.settings_login_with_google), icon = {
            if (state.isInProgress) {
                CircularProgressIndicator(Modifier.size(18.dp))
            } else {
                Icon(
                    tint = Color.Unspecified,
                    painter = painterResource(id = com.google.android.gms.base.R.drawable.googleg_standard_color_18),
                    contentDescription = stringResource(R.string.cd_google_logo),
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
                contentDescription = stringResource(R.string.cd_profile_image),
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
            stringResource(R.string.settings_profile_description),
            style = SevTheme.typography.bodyExtraSmall,
            color = SevTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        SurfaceButton(stringResource(R.string.settings_logout), onClick = onLogoutClick, modifier = Modifier.fillMaxWidth())
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun LoggedInPreview() {
    SevTheme {
        SettingsScreen(
            SettingsScreenState.LoggedIn(LoggedUser("x", "Bonifacio Ramírez Alcántara", "pepe@gmail.com", null)),
            HealthCheckState.Success(Stubs.healthCheck),
            NightModeSetting.FOLLOW_SYSTEM,
            true,
            {},
            {})
    }
}

@Preview
@Composable
private fun LoggedOutPreview() {
    ScreenPreview {
        SettingsScreen(
            SettingsScreenState.LoggedOut(isInProgress = false),
            HealthCheckState.Success(Stubs.healthCheck),
            NightModeSetting.FOLLOW_SYSTEM,
            true,
            {},
            {})
    }
}

@Preview
@Composable
private fun LoggedOutProgressPreview() {
    ScreenPreview {
        SettingsScreen(
            SettingsScreenState.LoggedOut(isInProgress = true),
            HealthCheckState.Success(Stubs.healthCheck),
            NightModeSetting.FOLLOW_SYSTEM,
            true,
            {},
            {})
    }
}
