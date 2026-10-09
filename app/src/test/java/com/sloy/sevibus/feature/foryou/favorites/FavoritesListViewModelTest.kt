package com.sloy.sevibus.feature.foryou.favorites

import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.FavoriteStop
import com.sloy.sevibus.domain.model.LoggedUser
import com.sloy.sevibus.domain.repository.FavoriteRepository
import com.sloy.sevibus.infrastructure.analytics.Analytics
import com.sloy.sevibus.infrastructure.session.SessionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesListViewModelTest {

    private val user = LoggedUser("id", "name", "email", null)
    private val currentUser = MutableStateFlow<LoggedUser?>(null)
    private val favorites = MutableSharedFlow<List<FavoriteStop>>()
    private val sessionService = mock<SessionService> {
        on { observeCurrentUser() } doReturn currentUser
    }
    private val favoriteRepository = mock<FavoriteRepository> {
        on { observeFavorites() } doReturn favorites
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `shows loading after login until the favorites arrive`() = runTest {
        val viewModel = collectedViewModel()

        currentUser.value = user

        expectThat(viewModel.state.value).isEqualTo(FavoritesListState.Loading)
    }

    @Test
    fun `shows the favorites once they arrive`() = runTest {
        val viewModel = collectedViewModel()

        currentUser.value = user
        favorites.emit(listOf(Stubs.favorites[0]))

        expectThat(viewModel.state.value).isEqualTo(FavoritesListState.Content(listOf(Stubs.favorites[0])))
    }

    @Test
    fun `shows the empty content when the user has no favorites`() = runTest {
        val viewModel = collectedViewModel()

        currentUser.value = user
        favorites.emit(emptyList())

        expectThat(viewModel.state.value).isEqualTo(FavoritesListState.Content(emptyList()))
    }

    private fun TestScope.collectedViewModel(): FavoritesListViewModel {
        val viewModel = FavoritesListViewModel(favoriteRepository, sessionService, mock<Analytics>())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        return viewModel
    }
}
