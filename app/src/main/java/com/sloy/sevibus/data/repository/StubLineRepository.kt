package com.sloy.sevibus.data.repository

import com.sloy.sevibus.Stubs
import com.sloy.sevibus.Stubs.delayNetwork
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.repository.LineRepository

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