package com.sloy.debugmenu.network

import com.sloy.debugmenu.testing.RecordingOverlayLogger
import com.sloy.debugmenu.testing.testContext
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue

class NetworkDebugModuleViewModelTest {

    private val dataSource = NetworkDebugModuleDataSource(testContext())
    private val overlayLogger = RecordingOverlayLogger()
    private val presets = listOf(
        HostPreset("Prod", "https://prod.example.com"),
        HostPreset("Dev", "https://dev.example.com"),
    )
    private val viewModel = NetworkDebugModuleViewModel(dataSource, overlayLogger, presets)

    @Test
    fun `disabling http overlay clears http items`() {
        viewModel.onHttpOverlayToggled(false)

        expectThat(dataSource.getCurrentState().isHttpOverlayEnabled).isFalse()
        expectThat(overlayLogger.clearedTypes).containsExactly(HttpOverlayLoggerItem::class)
    }

    @Test
    fun `valid custom host is stored as its origin`() {
        val applied = viewModel.onCustomHostApplied("  http://192.168.1.10:8080/  ")

        expectThat(applied).isTrue()
        expectThat(dataSource.getCurrentState().hostOverride).isEqualTo("http://192.168.1.10:8080")
    }

    @Test
    fun `invalid custom host is rejected`() {
        val applied = viewModel.onCustomHostApplied("192.168.1.10")

        expectThat(applied).isFalse()
        expectThat(dataSource.getCurrentState().hostOverride).isNull()
    }

    @Test
    fun `host selection index maps default presets and custom`() {
        expectThat(hostSelectionIndex(null, presets)).isEqualTo(0)
        expectThat(hostSelectionIndex("https://prod.example.com", presets)).isEqualTo(0)
        expectThat(hostSelectionIndex("https://dev.example.com/", presets)).isEqualTo(1)
        expectThat(hostSelectionIndex("http://192.168.1.10:8080", presets)).isEqualTo(presets.size)
    }

    @Test
    fun `selecting the first preset stores no override`() {
        viewModel.onHostSelected("https://dev.example.com")
        viewModel.onHostSelected("https://prod.example.com/")

        expectThat(dataSource.getCurrentState().hostOverride).isNull()
    }

    @Test
    fun `selecting a non default preset stores its url`() {
        viewModel.onHostSelected("https://dev.example.com")

        expectThat(dataSource.getCurrentState().hostOverride).isEqualTo("https://dev.example.com")
    }
}
