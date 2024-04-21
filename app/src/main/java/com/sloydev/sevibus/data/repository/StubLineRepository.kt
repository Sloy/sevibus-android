package com.sloydev.sevibus.data.repository

import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.Stubs.delayNetwork
import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.repository.LineRepository

class StubLineRepository : LineRepository {
    override suspend fun obtainLines(): Result<List<Line>> {
        delayNetwork()
        return Result.success(Stubs.lines.sortedBy { Stubs.lineGroups.indexOf(it.group) })
    }
}