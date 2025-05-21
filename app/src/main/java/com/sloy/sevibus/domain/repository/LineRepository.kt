package com.sloy.sevibus.domain.repository

import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.LineId

interface LineRepository {
    suspend fun obtainLines(): List<Line>
    suspend fun obtainLine(line: LineId): Line
    suspend fun obtainLines(ids: List<LineId>): List<Line>
    suspend fun searchLines(query: String): List<Line>
}
