package com.sloy.sevibus.domain.repository

import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.LineId

class FakeLineRepository : LineRepository {

    private val lines = mutableListOf<Line>()

    override suspend fun obtainLines(): List<Line> {
        return lines
    }

    override suspend fun obtainLines(ids: List<LineId>): List<Line> {
        return lines.filter { it.id in ids }
    }

    override suspend fun searchLines(term: String): List<Line> {
        return lines
    }

    override suspend fun obtainLine(line: LineId): Line {
        return lines.first { it.id == line }
    }

    fun setLines(lines: List<Line>) {
        this.lines.clear()
        this.lines.addAll(lines)
    }
}
