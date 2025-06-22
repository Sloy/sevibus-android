package com.sloy.sevibus.infrastructure

import android.content.Context
import androidx.room.Room
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.LocationSource
import com.sloy.sevibus.data.api.AdminApi
import com.sloy.sevibus.data.api.FirebaseAuthHeaderInterceptor
import com.sloy.sevibus.data.api.SevibusApi
import com.sloy.sevibus.data.api.SevibusUserApi
import com.sloy.sevibus.data.cache.ArrivalsMemoryCache
import com.sloy.sevibus.data.cache.BusesMemoryCache
import com.sloy.sevibus.data.database.SevibusDao
import com.sloy.sevibus.data.database.SevibusDatabase
import com.sloy.sevibus.data.database.TussamDao
import com.sloy.sevibus.data.repository.RemoteAndLocalCardsRepository
import com.sloy.sevibus.data.repository.RemoteAndLocalFavoriteRepository
import com.sloy.sevibus.data.repository.RemoteAndLocalLineRepository
import com.sloy.sevibus.data.repository.RemoteAndLocalPathRepository
import com.sloy.sevibus.data.repository.RemoteAndLocalRouteRepository
import com.sloy.sevibus.data.repository.RemoteAndLocalStopRepository
import com.sloy.sevibus.data.repository.RemoteBusRepository
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.repository.BusRepository
import com.sloy.sevibus.domain.repository.CardsRepository
import com.sloy.sevibus.domain.repository.FavoriteRepository
import com.sloy.sevibus.domain.repository.LineRepository
import com.sloy.sevibus.domain.repository.PathRepository
import com.sloy.sevibus.domain.repository.RouteRepository
import com.sloy.sevibus.domain.repository.StopRepository
import com.sloy.sevibus.domain.usecase.ObtainNearbyStops
import com.sloy.sevibus.feature.cards.CardViewModel
import com.sloy.sevibus.feature.foryou.favorites.FavoriteItemViewModel
import com.sloy.sevibus.feature.foryou.favorites.FavoritesListViewModel
import com.sloy.sevibus.feature.foryou.favorites.edit.EditFavoritesViewModel
import com.sloy.sevibus.feature.foryou.nearby.NearbyItemViewModel
import com.sloy.sevibus.feature.foryou.nearby.NearbyViewModel
import com.sloy.sevibus.feature.lines.LinesViewModel
import com.sloy.sevibus.feature.linestops.LineRouteViewModel
import com.sloy.sevibus.feature.login.SettingsViewModel
import com.sloy.sevibus.feature.map.MapViewModel
import com.sloy.sevibus.feature.map.states.OnLineSelectedState
import com.sloy.sevibus.feature.map.states.OnLinesSectionSelected
import com.sloy.sevibus.feature.map.states.OnStopAndLineSelected
import com.sloy.sevibus.feature.map.states.OnStopSelectedState
import com.sloy.sevibus.feature.search.LineSelectorViewModel
import com.sloy.sevibus.feature.search.SearchViewModel
import com.sloy.sevibus.feature.stopdetail.StopDetailViewModel
import com.sloy.sevibus.infrastructure.config.ApiConfigurationManager
import com.sloy.sevibus.infrastructure.config.DynamicApiUrlInterceptor
import com.sloy.sevibus.infrastructure.config.RemoteConfigService
import com.sloy.sevibus.infrastructure.location.FusedLocationService
import com.sloy.sevibus.infrastructure.location.LocationService
import com.sloy.sevibus.infrastructure.location.LocationServiceSource
import com.sloy.sevibus.infrastructure.nfc.NfcStateManager
import com.sloy.sevibus.infrastructure.nightmode.NightModeDataSource
import com.sloy.sevibus.infrastructure.session.FirebaseAuthService
import com.sloy.sevibus.infrastructure.session.GoogleAuthService
import com.sloy.sevibus.infrastructure.session.SessionService
import com.sloy.sevibus.navigation.SevNavigator
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File


object DI {
    val viewModelModule = module {
        viewModel { LinesViewModel(get()) }
        viewModel { parameters -> LineRouteViewModel(parameters.get(), parameters.getOrNull<RouteId>(), get(), get()) }
        viewModel { parameters -> StopDetailViewModel(parameters.get(), get(), get(), get(), get(), get()) }
        viewModel { FavoritesListViewModel(get(), get(), get()) }
        viewModel { NearbyViewModel(get(), get(), get()) }
        viewModel { parameters -> FavoriteItemViewModel(parameters.get(), get(), get()) }
        viewModel { parameters -> NearbyItemViewModel(parameters.get(), get()) }
        viewModel { EditFavoritesViewModel(get(), get()) }
        viewModel { SearchViewModel(get(), get(), get()) }
        viewModel { MapViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { LineSelectorViewModel(get()) }
        viewModel { SettingsViewModel(get(), get(), get()) }
        viewModel { CardViewModel(get(), get()) }
    }

    val dataModule = module {
        //single<LineRepository> { StubLineRepository() }
        single<LineRepository> { RemoteAndLocalLineRepository(get(), get(), get()) }
//        single<StopRepository> { StubStopRepository() }
        single<StopRepository> { RemoteAndLocalStopRepository(get(), get(), get()) }
//        single<BusRepository> { FakeBusRepository(get()) }
        single<BusRepository> { RemoteBusRepository(get(), get(), get(), get(), get()) }
        //single<FavoriteStopsRepository> { StubFavoriteStopsRepository() }
        single<FavoriteRepository> { RemoteAndLocalFavoriteRepository(get(), get(), get(), get()) }
        //single<PathRepository> { StubPathRepository() }
        single<PathRepository> { RemoteAndLocalPathRepository(get(), get(), get()) }
        single<RouteRepository> { RemoteAndLocalRouteRepository(get(), get()) }
        single<CardsRepository> { RemoteAndLocalCardsRepository(get(), get(), get(), get(), get()) }
        single<NightModeDataSource> { NightModeDataSource(androidContext()) }

        single<SevibusDatabase> {
            Room.databaseBuilder(
                androidContext(),
                SevibusDatabase::class.java,
                "sevibus"
            ).fallbackToDestructiveMigration()
                .build()
        }
        single<TussamDao> { get<SevibusDatabase>().tussamDao() }
        single<SevibusDao> { get<SevibusDatabase>().sevibusDao() }
        single<ArrivalsMemoryCache> { ArrivalsMemoryCache() }
        single<BusesMemoryCache> { BusesMemoryCache() }

        single<OkHttpClient> {
            val interceptors: List<Interceptor> = get()
            OkHttpClient.Builder()
                .cache(okHttpCache(androidContext()))
                .addInterceptor(DynamicApiUrlInterceptor(get()))
                .addInterceptors(interceptors)
                .build()
        }

        val json = Json { ignoreUnknownKeys = true }
        single<SevibusApi> {
            val retrofit = Retrofit.Builder()
                .client(get())
                .baseUrl("${DynamicApiUrlInterceptor.BASE_URL_PLACEHOLDER}api/")
                .addConverterFactory(
                    json.asConverterFactory("application/json; charset=UTF-8".toMediaType())
                )
                .build()
            retrofit.create(SevibusApi::class.java)
        }
        single<SevibusUserApi> {
            val retrofit = Retrofit.Builder()
                .client(get<OkHttpClient>().newBuilder().apply {
                    interceptors().add(0, FirebaseAuthHeaderInterceptor())
                }.build())
                .baseUrl("${DynamicApiUrlInterceptor.BASE_URL_PLACEHOLDER}api/")
                .addConverterFactory(
                    json.asConverterFactory("application/json; charset=UTF-8".toMediaType())
                )
                .build()
            retrofit.create(SevibusUserApi::class.java)
        }
        single<AdminApi> {
            val retrofit = Retrofit.Builder()
                .client(get<OkHttpClient>().newBuilder().apply {
                    interceptors().add(0, FirebaseAuthHeaderInterceptor())
                }.build())
                .baseUrl("https://base.url/admin/")
                .addConverterFactory(
                    json.asConverterFactory("application/json; charset=UTF-8".toMediaType())
                )
                .build()
            retrofit.create(AdminApi::class.java)
        }
    }

    private fun okHttpCache(context: Context): Cache {
        val cacheSize = 50L * 1024 * 1024 // 50 MB
        val cacheDir = File(context.cacheDir, "http_cache")
        val cache = Cache(cacheDir, cacheSize)
        return cache
    }

    val infrastructureModule = module {
        single<ApiConfigurationManager> { ApiConfigurationManager() }
        single<RemoteConfigService> { RemoteConfigService(get()) }
        single<FusedLocationService> {
            FusedLocationService(
                androidContext(),
                LocationServices.getFusedLocationProviderClient(androidContext())
            )
        }
        single<LocationService> { get<FusedLocationService>() }
        single<LocationSource> { LocationServiceSource(get()) }
        single<FirebaseAuthService> { FirebaseAuthService() }
        single<SessionService> { SessionService(get(), GoogleAuthService(), get()) }
        single<SevNavigator> { SevNavigator() }
        single<OnStopSelectedState> { OnStopSelectedState(get(), get(), get(), get()) }
        single<OnLineSelectedState> { OnLineSelectedState(get(), get(), get(), get(), get()) }
        single<OnStopAndLineSelected> { OnStopAndLineSelected(get(), get(), get()) }
        single<OnLinesSectionSelected> { OnLinesSectionSelected(get(), get(), get(), get()) }
        single<ObtainNearbyStops> { ObtainNearbyStops(get()) }
        single<NfcStateManager> { NfcStateManager(androidContext()) }
    }

    private fun OkHttpClient.Builder.addInterceptors(interceptors: List<Interceptor>): OkHttpClient.Builder {
        interceptors.forEach {
            addInterceptor(it)
        }
        return this
    }

}
