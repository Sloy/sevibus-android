package com.sloydev.sevibus.infrastructure

import com.sloydev.sevibus.data.repository.StubLineRepository
import com.sloydev.sevibus.domain.repository.LineRepository
import com.sloydev.sevibus.feature.lines.LinesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object DI {
    val viewModelModule = module {
        viewModel { LinesViewModel(get()) }
    }

    val dataModule = module {
        single<LineRepository> { StubLineRepository() }
    }
}