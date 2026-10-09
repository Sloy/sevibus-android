package com.sloy.sevibus.data.repository

import com.sloy.sevibus.Stubs
import com.sloy.sevibus.data.api.SevibusUserApi
import com.sloy.sevibus.data.api.model.FavoriteStopDto
import com.sloy.sevibus.data.database.FakeSevibusDao
import com.sloy.sevibus.data.database.FavoriteStopEntity
import com.sloy.sevibus.domain.model.LoggedUser
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.domain.repository.StopRepository
import com.sloy.sevibus.infrastructure.session.SessionService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.map
import kotlin.time.Duration.Companion.seconds

class RemoteAndLocalFavoriteRepositoryTest {

    private val user = LoggedUser("id", "name", "email", null)
    private val currentUser = MutableStateFlow<LoggedUser?>(null)
    private val sessionService = mock<SessionService> {
        on { observeCurrentUser() } doAnswer { currentUser }
        on { isLogged() } doAnswer { currentUser.value != null }
    }
    private val dao = FakeSevibusDao()
    private var remoteFavorites = CompletableDeferred<List<FavoriteStopDto>>()
    private val api = mock<SevibusUserApi> {
        onBlocking { obtainFavorites() } doSuspendableAnswer { remoteFavorites.await() }
    }
    private val stopRepository = mock<StopRepository> {
        onBlocking { obtainStop(any()) } doSuspendableAnswer { Stubs.stops[0].copy(code = it.getArgument<StopId>(0)) }
    }

    private val repository = RemoteAndLocalFavoriteRepository(dao, api, stopRepository, sessionService)

    @Test
    fun `waits for the initial sync before emitting when there are no local favorites`() = runTest {
        currentUser.value = user

        val firstEmission = firstEmissionAsync()
        remoteFavorites.complete(listOf(FavoriteStopDto(stopId = 1)))

        expectThat(firstEmission.await()).map { it.stop.code }.containsExactly(1)
    }

    @Test
    fun `emits no favorites after the initial sync when the server has none`() = runTest {
        currentUser.value = user

        val firstEmission = firstEmissionAsync()
        remoteFavorites.complete(emptyList())

        expectThat(firstEmission.await()).isEmpty()
    }

    @Test
    fun `emits local favorites without waiting for the sync`() = runTest {
        dao.favorites.value = listOf(FavoriteStopEntity(stopId = 2))
        currentUser.value = user

        expectThat(firstEmissionAsync().await()).map { it.stop.code }.containsExactly(2)
    }

    @Test
    fun `emits local favorites when the sync fails`() = runTest {
        currentUser.value = user

        val firstEmission = firstEmissionAsync()
        remoteFavorites.completeExceptionally(IllegalStateException("Server error"))

        expectThat(firstEmission.await()).isEmpty()
    }

    @Test
    fun `syncs again on the next login after a failed sync`() = runTest {
        currentUser.value = user
        remoteFavorites.completeExceptionally(IllegalStateException("Server error"))
        firstEmissionAsync().await()
        dao.favorites.value = listOf(FavoriteStopEntity(stopId = 2))

        currentUser.value = null
        awaitFirst(dao.favorites) { it.isEmpty() }
        remoteFavorites = CompletableDeferred(listOf(FavoriteStopDto(stopId = 3)))
        currentUser.value = user

        val favorites = awaitFirst(repository.observeFavorites()) { it.isNotEmpty() }
        expectThat(favorites).map { it.stop.code }.containsExactly(3)
    }

    private fun TestScope.firstEmissionAsync() = async { awaitFirst(repository.observeFavorites()) { true } }

    private suspend fun <T> awaitFirst(flow: Flow<T>, predicate: (T) -> Boolean): T = withContext(Dispatchers.Default) {
        withTimeout(5.seconds) { flow.first(predicate) }
    }
}
