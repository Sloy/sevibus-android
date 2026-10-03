package com.sloy.debugmenu.overlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

@OptIn(ExperimentalCoroutinesApi::class)
class OverlayLoggerImplTest {

    private val testScope = TestScope()
    private val logger = OverlayLoggerImpl(scope = testScope, autoHideDelayMillis = 1_000)

    @Test
    fun `put appends new items`() {
        logger.put(FakeItem("a"))
        logger.put(FakeItem("b"))

        expectThat(logger.items.value).containsExactly(FakeItem("a"), FakeItem("b"))
    }

    @Test
    fun `put replaces item with the same id in place`() {
        logger.put(FakeItem("a", label = "pending"))
        logger.put(FakeItem("b"))
        logger.put(FakeItem("a", label = "done"))

        expectThat(logger.items.value).containsExactly(FakeItem("a", label = "done"), FakeItem("b"))
    }

    @Test
    fun `auto hide items are removed after the delay`() = testScope.runTest {
        logger.put(FakeItem("a", autoHide = true))

        advanceTimeBy(999)
        runCurrent()
        expectThat(logger.items.value).containsExactly(FakeItem("a", autoHide = true))

        advanceTimeBy(2)
        runCurrent()
        expectThat(logger.items.value).isEmpty()
    }

    @Test
    fun `items without auto hide stay`() = testScope.runTest {
        logger.put(FakeItem("a"))

        advanceTimeBy(10_000)
        runCurrent()

        expectThat(logger.items.value).containsExactly(FakeItem("a"))
    }

    @Test
    fun `replaced item is not removed by the timer of its previous version`() = testScope.runTest {
        logger.put(FakeItem("a", label = "first", autoHide = true))
        advanceTimeBy(500)
        logger.put(FakeItem("a", label = "second", autoHide = false))

        advanceTimeBy(1_000)
        runCurrent()

        expectThat(logger.items.value).containsExactly(FakeItem("a", label = "second", autoHide = false))
    }

    @Test
    fun `clear removes only items of the given type`() {
        logger.put(FakeItem("a"))
        logger.put(OtherItem("b"))

        logger.clear(FakeItem::class)

        expectThat(logger.items.value).containsExactly(OtherItem("b"))
    }

    private data class FakeItem(
        override val id: String,
        val label: String = "",
        override val autoHide: Boolean = false,
    ) : OverlayLoggerItem {
        @Composable
        override fun Content(modifier: Modifier) = Unit
    }

    private data class OtherItem(override val id: String) : OverlayLoggerItem {
        override val autoHide: Boolean = false

        @Composable
        override fun Content(modifier: Modifier) = Unit
    }
}
