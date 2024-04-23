package com.sloydev.sevibus.infrastructure

import com.sloydev.sevibus.data.repository.StubLineRepository
import com.sloydev.sevibus.data.repository.StubStopRepository
import com.sloydev.sevibus.domain.repository.LineRepository
import com.sloydev.sevibus.domain.repository.StopRepository
import com.sloydev.sevibus.feature.lines.LinesViewModel
import com.sloydev.sevibus.feature.linestops.LineRouteViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object DI {
    val viewModelModule = module {
        viewModel { LinesViewModel(get()) }
        viewModel { parameters -> LineRouteViewModel(parameters.get(), get(), get()) }
    }

    val dataModule = module {
        single<LineRepository> { StubLineRepository() }
        single<StopRepository> { StubStopRepository() }
    }
}