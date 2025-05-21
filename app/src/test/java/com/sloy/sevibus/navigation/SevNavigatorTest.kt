package com.sloy.sevibus.navigation

import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.StopId
import org.junit.Test
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isFalse
import strikt.assertions.isTrue


class SevNavigatorTest {

    val navigator = SevNavigator()

    @Test
    fun `should start on initial destination, and isLastDestination true`() {
        expect {
            that(navigator.current()).isA<NavigationDestination.ForYou>()
            that(navigator.isLastDestination.value).isTrue()
        }
    }

    @Test
    fun `should navigate and come back to previous`() {
        navigator.navigate(NavigationDestination.Lines)
        navigator.navigate(NavigationDestination.LineStops(LINE_ID))

        expect {
            that(navigator.current()).isA<NavigationDestination.LineStops>()
            that(navigator.peekPrevious()).isA<NavigationDestination.Lines>()
        }

        navigator.navigateBack()
        expect {
            that(navigator.current()).isA<NavigationDestination.Lines>()
            that(navigator.peekPrevious()).isA<NavigationDestination.ForYou>()
        }

        navigator.navigateBack()
        expect {
            that(navigator.current()).isA<NavigationDestination.ForYou>()
            that(navigator.isLastDestination.value).isTrue()
        }
    }

    @Test
    fun `should not keep multiple LineStops destinations in back stack`() {
        navigator.navigate(NavigationDestination.LineStops(LINE_ID, routeId = "1"))
        navigator.navigate(NavigationDestination.LineStops(LINE_ID, routeId = "2"))

        expectThat(navigator.peekPrevious()).isA<NavigationDestination.ForYou>()
    }

    @Test
    fun `should not keep multiple StopDetail destinations in back stack`() {
        navigator.navigate(NavigationDestination.StopDetail(STOP_ID))
        navigator.navigate(NavigationDestination.StopDetail(STOP_ID_2))

        expectThat(navigator.peekPrevious()).isA<NavigationDestination.ForYou>()
    }

    @Test
    fun `should skip Search from the back stack`() {
        navigator.navigate(NavigationDestination.Search)
        expect {
            that(navigator.current()).isA<NavigationDestination.Search>()
            that(navigator.peekPrevious()).isA<NavigationDestination.ForYou>()
            that(navigator.isLastDestination.value).isFalse()
        }

        navigator.navigate(NavigationDestination.StopDetail(STOP_ID))
        expect {
            that(navigator.current()).isA<NavigationDestination.StopDetail>()
            that(navigator.peekPrevious()).isA<NavigationDestination.ForYou>()
            that(navigator.isLastDestination.value).isFalse()
        }
    }

    @Test
    fun `should not keep same destination in back stack after Search`() {
        // Abro búsqueda despúes de una parada
        navigator.navigate(NavigationDestination.StopDetail(STOP_ID))
        navigator.navigate(NavigationDestination.Search)
        expect {
            // Debería estar en la búsqueda y la anterior ser la parada
            that(navigator.current()).isA<NavigationDestination.Search>()
            that(navigator.peekPrevious()).isA<NavigationDestination.StopDetail>()
        }

        // Elijo una parada
        navigator.navigate(NavigationDestination.StopDetail(STOP_ID_2))
        expect{
            // La anterior debería ser ForYou porque hemos sobreescrito la parada anterior
            that(navigator.peekPrevious()).isA<NavigationDestination.ForYou>()
        }
    }
}

private const val LINE_ID: LineId = 1
private const val STOP_ID: StopId = 11
private const val STOP_ID_2: StopId = 12
