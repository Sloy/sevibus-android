package com.sloy.sevibus.data.repository

import com.sloy.sevibus.data.api.SevibusUserApi
import com.sloy.sevibus.data.database.SevibusDao
import com.sloy.sevibus.data.database.fromEntity
import com.sloy.sevibus.data.database.toDto
import com.sloy.sevibus.data.database.toEntity
import com.sloy.sevibus.domain.model.FavoriteStop
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.domain.repository.FavoriteRepository
import com.sloy.sevibus.domain.repository.StopRepository
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.session.SessionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class RemoteAndLocalFavoriteRepository(
    private val dao: SevibusDao,
    private val api: SevibusUserApi,
    private val stopRepository: StopRepository,
    private val sessionService: SessionService,
) : FavoriteRepository {

    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()
    private val syncedUserId = MutableStateFlow<String?>(null)

    init {
        sessionService.observeCurrentUser()
            .distinctUntilChangedBy { it != null }
            .onEach { user ->
                syncedUserId.value = null
                if (user != null) {
                    runCatching { syncWithServer() }
                        .onFailure { SevLogger.logW(it, "Error syncing favorites") }
                    syncedUserId.value = user.id
                } else {
                    dao.deleteAllFavorites()
                }
            }
            .launchIn(backgroundScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeFavorites(): Flow<List<FavoriteStop>> {
        return sessionService.observeCurrentUser()
            .flatMapLatest { user ->
                if (user == null) {
                    flowOf(emptyList())
                } else {
                    combine(dao.observeFavorites(), syncedUserId) { entities, syncedUserId ->
                        entities.takeIf { it.isNotEmpty() || syncedUserId == user.id }
                    }
                        .filterNotNull()
                        .map { entities ->
                            entities.map { entity ->
                                entity.fromEntity(stopRepository.obtainStop(entity.stopId))
                            }
                        }
                }
            }
    }

    override suspend fun obtainFavorites(): List<FavoriteStop> = withContext(Dispatchers.Default) {
        mutex.withLock {
            if (!sessionService.isLogged()) {
                return@withContext emptyList()
            }
            dao.getFavorites()
                .map { entity ->
                    entity.fromEntity(stopRepository.obtainStop(entity.stopId))
                }

        }
    }

    override suspend fun isFavorite(stop: StopId): Boolean {
        if (!sessionService.isLogged()) {
            return false
        }
        return obtainFavorites().any { it.stop.code == stop }
    }

    override suspend fun addFavorite(stop: FavoriteStop): Unit = withContext(Dispatchers.Default) {
        if (!sessionService.isLogged()) {
            error("Can't add favorite if user is not logged in")
        }
        val lastOrder = dao.getFavorites().sortedBy { it.order }.lastOrNull()?.order ?: -1
        val entity = stop.toEntity(order = lastOrder + 1)
        dao.putFavorite(entity)
        backgroundScope.launch {
            runCatching { api.addFavorite(entity.stopId, entity.toDto()) }
                .onFailure { SevLogger.logW(it) }
        }
    }

    override suspend fun removeFavorite(stop: StopId): Unit = withContext(Dispatchers.Default) {
        if (!sessionService.isLogged()) {
            error("Can't remove favorite if user is not logged in")
        }
        dao.deleteFavorite(stop)
        backgroundScope.launch {
            runCatching { api.deleteFavorite(stop) }
                .onFailure { SevLogger.logW(it) }
        }
    }

    override suspend fun replaceFavorites(favorites: List<FavoriteStop>): Unit = withContext(Dispatchers.Default) {
        if (!sessionService.isLogged()) {
            error("Can't replace favorites if user is not logged in")
        }
        val entities = favorites.mapIndexed { i, favorite -> favorite.toEntity(i) }
        dao.replaceAllFavorites(entities)
        backgroundScope.launch {
            runCatching { api.replaceFavorites(entities.map { it.toDto() }) }
                .onFailure { SevLogger.logW(it) }
        }
    }

    private suspend fun syncWithServer() = withContext(Dispatchers.Default) {
        mutex.withLock {
            val localFavorites = dao.getFavorites()
            val remoteFavorites = api.obtainFavorites()

            val localOnlyFavorites = localFavorites.filter { localFavorite ->
                remoteFavorites.none { it.stopId == localFavorite.stopId }
            }

            dao.replaceAllFavorites(remoteFavorites.map { it.toEntity() } + localOnlyFavorites)

            localOnlyFavorites.forEach { local ->
                api.addFavorite(local.stopId, local.toDto())
            }
        }
    }
}
