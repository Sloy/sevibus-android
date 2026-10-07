package com.sloy.sevibus.data.repository

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class CardIdApiPathTest {

    @Test
    fun `keeps leading zeros up to 12 digits`() {
        expectThat(0L.toApiPath()).isEqualTo("000000000000")
        expectThat(12345L.toApiPath()).isEqualTo("000000012345")
    }

    @Test
    fun `does not alter a full serial`() {
        expectThat(123456789012L.toApiPath()).isEqualTo("123456789012")
    }
}
