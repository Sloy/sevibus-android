package com.sloydev.sevibus.infrastructure

import androidx.room.Room
import com.sloydev.sevibus.data.api.SevibusApi
import com.sloydev.sevibus.data.database.SevibusDatabase
import com.sloydev.sevibus.data.database.TussamDao
import com.sloydev.sevibus.data.repository.RemoteAndLocalLineRepository
import com.sloydev.sevibus.data.repository.RemoteAndLocalPathRepository
import com.sloydev.sevibus.data.repository.RemoteAndLocalStopRepository
import com.sloydev.sevibus.data.repository.RemoteBusRepository
import com.sloydev.sevibus.data.repository.StubFavoriteStopsRepository
import com.sloydev.sevibus.domain.repository.BusRepository
import com.sloydev.sevibus.domain.repository.FavoriteStopsRepository
import com.sloydev.sevibus.domain.repository.LineRepository
import com.sloydev.sevibus.domain.repository.PathRepository
import com.sloydev.sevibus.domain.repository.StopRepository
import com.sloydev.sevibus.feature.foryou.favorites.FavoritesViewModel
import com.sloydev.sevibus.feature.lines.LinesViewModel
import com.sloydev.sevibus.feature.linestops.LineRouteViewModel
import com.sloydev.sevibus.feature.map.MapViewModel
import com.sloydev.sevibus.feature.search.LineSelectorViewModel
import com.sloydev.sevibus.feature.search.SearchViewModel
import com.sloydev.sevibus.feature.stopdetail.StopDetailViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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
        viewModel { MapViewModel(get(), get(), get()) }
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
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
            OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
        }

        single<SevibusApi> {
            val retrofit = Retrofit.Builder()
                .client(get())
                .baseUrl("https://api-vd4mgiw7ma-uc.a.run.app/api/")
                //.baseUrl("https://sevibus.app/api/")
                .addConverterFactory(
                    Json.asConverterFactory("application/json; charset=UTF8".toMediaType())
                )
                .build()

            retrofit.create(SevibusApi::class.java)
        }
    }

}