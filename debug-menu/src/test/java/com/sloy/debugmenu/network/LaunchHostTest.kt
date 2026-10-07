package com.sloy.debugmenu.network

import com.sloy.debugmenu.testing.testContext
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class LaunchHostTest {

    private val dataSource = NetworkDebugModuleDataSource(testContext())

    @Test
    fun `launch host is stored as its origin`() {
        dataSource.applyLaunchHost("http://localhost:8089/")

        expectThat(dataSource.getCurrentState().hostOverride).isEqualTo("http://localhost:8089")
    }

    @Test
    fun `missing launch host keeps the current override`() {
        dataSource.updateState(NetworkDebugModuleState(hostOverride = "https://dev.example.com"))

        dataSource.applyLaunchHost(null)

        expectThat(dataSource.getCurrentState().hostOverride).isEqualTo("https://dev.example.com")
    }

    @Test
    fun `invalid launch host is ignored`() {
        dataSource.applyLaunchHost("localhost:8089")

        expectThat(dataSource.getCurrentState().hostOverride).isNull()
    }

    @Test
    fun `launch host keeps other network settings`() {
        dataSource.updateState(NetworkDebugModuleState(latencyPreset = LatencyPreset.entries.last()))

        dataSource.applyLaunchHost("http://localhost:8089")

        expectThat(dataSource.getCurrentState().latencyPreset).isEqualTo(LatencyPreset.entries.last())
    }
}
