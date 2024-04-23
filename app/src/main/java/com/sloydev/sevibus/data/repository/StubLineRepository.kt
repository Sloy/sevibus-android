package com.sloydev.sevibus.data.repository

import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.Stubs.delayNetwork
import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.LineId
import com.sloydev.sevibus.domain.repository.LineRepository

class StubLineRepository : LineRepository {
    override suspend fun obtainLines(): List<Line> {
        delayNetwork()
        return Stubs.lines.sortedBy { Stubs.lineGroups.indexOf(it.group) }
    }

    override suspend fun obtainLine(line: LineId): Line {
        delayNetwork()
        return Stubs.lines.first { it.id == line }
    }
}