package com.sloydev.sevibus.domain.repository

import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.LineId

interface LineRepository {
    suspend fun obtainLines(): List<Line>
    suspend fun obtainLine(line: LineId): Line
}