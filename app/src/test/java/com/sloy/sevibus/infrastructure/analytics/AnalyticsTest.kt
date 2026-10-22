package com.sloy.sevibus.infrastructure.analytics

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

class AnalyticsTest {

    private val tracked = mutableListOf<String>()
    private val tracker = object : Tracker {
        override fun track(event: SevEvent) {
            tracked += event.name
        }
    }

    @Test
    fun `trackers receive events in call order when the enabled check suspends`() = runTest {
        var checks = 0
        val settings = mock<AnalyticsSettingsDataSource> {
            onBlocking { isAnalyticsEnabled() } doSuspendableAnswer {
                delay(if (checks++ == 0) 100 else 0)
                true
            }
        }
        val analytics = Analytics(listOf(tracker), settings, backgroundScope)

        analytics.track(TestEvent("First"))
        analytics.track(TestEvent("Second"))
        advanceTimeBy(200)
        runCurrent()

        expectThat(tracked).containsExactly("First", "Second")
    }

    @Test
    fun `nothing is tracked when analytics is disabled`() = runTest {
        val settings = mock<AnalyticsSettingsDataSource> {
            onBlocking { isAnalyticsEnabled() } doSuspendableAnswer { false }
        }
        val analytics = Analytics(listOf(tracker), settings, backgroundScope)

        analytics.track(TestEvent("First"))
        advanceTimeBy(200)
        runCurrent()

        expectThat(tracked).isEmpty()
    }

    private class TestEvent(name: String) : SevEvent(name)
}
