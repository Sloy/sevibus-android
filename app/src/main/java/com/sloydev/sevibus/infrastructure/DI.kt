package com.sloydev.sevibus.infrastructure

import androidx.room.Room
import com.sloydev.sevibus.data.api.SevibusApi
import com.sloydev.sevibus.data.database.SevibusDatabase
import com.sloydev.sevibus.data.database.TussamDao
import com.sloydev.sevibus.data.repository.RemoteAndLocalLineRepository
import com.sloydev.sevibus.data.repository.StubStopRepository
import com.sloydev.sevibus.domain.repository.LineRepository
import com.sloydev.sevibus.domain.repository.StopRepository
import com.sloydev.sevibus.feature.lines.LinesViewModel
import com.sloydev.sevibus.feature.linestops.LineRouteViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object DI {
    val viewModelModule = module {
        viewModel { LinesViewModel(get()) }
        viewModel { parameters -> LineRouteViewModel(parameters.get(), get(), get()) }
    }

    val dataModule = module {
        //single<LineRepository> { StubLineRepository() }
        single<LineRepository> { RemoteAndLocalLineRepository(get(), get()) }
        single<StopRepository> { StubStopRepository() }

        single<SevibusDatabase> {
            Room.databaseBuilder(
                androidContext(),
                SevibusDatabase::class.java,
                "sevibus"
            ).build()
        }
        single<TussamDao> { get<SevibusDatabase>().tussamDao() }

        single<SevibusApi> {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://sevibus.app/api/")
                .addConverterFactory(
                    Json.asConverterFactory("application/json; charset=UTF8".toMediaType())
                )
                .build()

            retrofit.create(SevibusApi::class.java)
        }
    }

}