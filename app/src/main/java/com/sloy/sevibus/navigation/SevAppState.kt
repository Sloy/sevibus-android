package com.sloy.sevibus.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sloy.sevibus.domain.model.LoggedUser
import com.sloy.sevibus.infrastructure.session.SessionService
import org.koin.compose.koinInject

@Composable
fun rememberSevAppState(
    sessionService: SessionService = koinInject(),
    sevNavigator: SevNavigator = koinInject()
): SevAppState {
    return remember { SevAppState(sessionService, sevNavigator) }
}

@Stable
class SevAppState(
    val sessionService: SessionService,
    val sevNavigator: SevNavigator
) {

    val currentUser: LoggedUser?
        @Composable get() {
            return sessionService.observeCurrentUser().collectAsStateWithLifecycle(null).value
        }

    fun navigate(destination: NavigationDestination) {
        sevNavigator.navigate(destination)

    }
}
