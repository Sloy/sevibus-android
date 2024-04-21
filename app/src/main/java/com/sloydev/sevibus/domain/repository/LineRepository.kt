package com.sloydev.sevibus.domain.repository

import com.sloydev.sevibus.domain.model.Line

interface LineRepository {
    suspend fun obtainLines(): Result<List<Line>>
}