package com.sloy.sevibus.infrastructure

import androidx.room.Room
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.LocationSource
import com.sloy.sevibus.data.api.SevibusApi
import com.sloy.sevibus.data.database.SevibusDatabase
import com.sloy.sevibus.data.database.TussamDao
import com.sloy.sevibus.data.repository.RemoteAndLocalLineRepository
import com.sloy.sevibus.data.repository.RemoteAndLocalPathRepository
import com.sloy.sevibus.data.repository.RemoteAndLocalStopRepository
import com.sloy.sevibus.data.repository.RemoteBusRepository
import com.sloy.sevibus.data.repository.StubFavoriteStopsRepository
import com.sloy.sevibus.domain.repository.BusRepository
import com.sloy.sevibus.domain.repository.FavoriteStopsRepository
import com.sloy.sevibus.domain.repository.LineRepository
import com.sloy.sevibus.domain.repository.PathRepository
import com.sloy.sevibus.domain.repository.StopRepository
import com.sloy.sevibus.feature.foryou.favorites.FavoritesViewModel
import com.sloy.sevibus.feature.lines.LinesViewModel
import com.sloy.sevibus.feature.linestops.LineRouteViewModel
import com.sloy.sevibus.feature.map.MapViewModel
import com.sloy.sevibus.feature.search.LineSelectorViewModel
import com.sloy.sevibus.feature.search.SearchViewModel
import com.sloy.sevibus.feature.stopdetail.StopDetailViewModel
import com.sloy.sevibus.infrastructure.location.FusedLocationService
import com.sloy.sevibus.infrastructure.location.LocationService
import com.sloy.sevibus.infrastructure.location.LocationServiceSource
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory


object DI {
    val viewModelModule = module {
        viewModel { LinesViewModel(get()) }
        viewModel { parameters -> LineRouteViewModel(parameters.get(), get(), get()) }
        viewModel { parameters -> StopDetailViewModel(parameters.get(), get(), get()) }
        viewModel { FavoritesViewModel(get(), get()) }
        viewModel { SearchViewModel(get(), get()) }
        viewModel { MapViewModel(get(), get(), get(), get()) }
        viewModel { LineSelectorViewModel(get()) }
    }

    val dataModule = module {
        //single<LineRepository> { StubLineRepository() }
        single<LineRepository> { RemoteAndLocalLineRepository(get(), get()) }
//        single<StopRepository> { StubStopRepository() }
        single<StopRepository> { RemoteAndLocalStopRepository(get(), get()) }
//        single<BusRepository> { FakeBusRepository(get()) }
        single<BusRepository> { RemoteBusRepository(get(), get()) }
        single<FavoriteStopsRepository> { StubFavoriteStopsRepository() }
        //single<PathRepository> { StubPathRepository() }
        single<PathRepository> { RemoteAndLocalPathRepository(get(), get()) }

        single<SevibusDatabase> {
            Room.databaseBuilder(
                androidContext(),
                SevibusDatabase::class.java,
                "sevibus"
            ).fallbackToDestructiveMigration()
                .build()
        }
        single<TussamDao> { get<SevibusDatabase>().tussamDao() }

        single<OkHttpClient> {
            val interceptors: List<Interceptor> = get()
            OkHttpClient.Builder()
                .addInterceptors(interceptors)
                .build()
        }

        single<SevibusApi> {
            val retrofit = Retrofit.Builder()
                .client(get())
                .baseUrl("https://ldzu622p0l.execute-api.eu-south-2.amazonaws.com/prod/api/")
                //.baseUrl("https://sevibus.app/api/")
                .addConverterFactory(
                    Json.asConverterFactory("application/json; charset=UTF8".toMediaType())
                )
                .build()
            retrofit.create(SevibusApi::class.java)
        }
    }

    val infrastructureModule = module {
        single<FusedLocationService> {
            FusedLocationService(
                androidContext(),
                LocationServices.getFusedLocationProviderClient(androidContext())
            )
        }
        single<LocationService> { get<FusedLocationService>() }
        single<LocationSource> { LocationServiceSource(get()) }
    }

    private fun OkHttpClient.Builder.addInterceptors(interceptors: List<Interceptor>): OkHttpClient.Builder {
        interceptors.forEach {
            addInterceptor(it)
        }
        return this
    }

}
