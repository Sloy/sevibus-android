package com.sloy.sevibus.feature.foryou.favorites.edit

import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.CustomIcon
import com.sloy.sevibus.infrastructure.analytics.events.Events
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class FavoritesDiffTest {

    private val first = Stubs.favorites[0]
    private val second = Stubs.favorites[1]
    private val third = Stubs.favorites[2]
    private val original = listOf(first, second, third)

    @Test
    fun `nothing changed`() {
        expectThat(favoritesDiff(original, original)).isEqualTo(saved())
    }

    @Test
    fun `counts renamed favorites`() {
        val updated = listOf(first.copy(customName = "Trabajo"), second.copy(customName = null), third)

        expectThat(favoritesDiff(original, updated)).isEqualTo(saved(renamed = 2))
    }

    @Test
    fun `counts favorites with a new icon`() {
        val updated = listOf(first, second.copy(customIcon = CustomIcon.School), third)

        expectThat(favoritesDiff(original, updated)).isEqualTo(saved(iconChanged = 1))
    }

    @Test
    fun `counts favorites with different selected lines`() {
        val updated = listOf(first.copy(selectedLineIds = setOf(1)), second, third.copy(selectedLineIds = emptySet()))

        expectThat(favoritesDiff(original, updated)).isEqualTo(saved(linesChanged = 2))
    }

    @Test
    fun `counts deleted favorites without considering it a reorder`() {
        val updated = listOf(first, third)

        expectThat(favoritesDiff(original, updated)).isEqualTo(saved(deleted = 1))
    }

    @Test
    fun `detects reordered favorites`() {
        val updated = listOf(second, first, third)

        expectThat(favoritesDiff(original, updated)).isEqualTo(saved(reordered = true))
    }

    @Test
    fun `combines every change`() {
        val updated = listOf(third.copy(customName = "Gimnasio", customIcon = CustomIcon.Health), first)

        expectThat(favoritesDiff(original, updated)).isEqualTo(saved(renamed = 1, iconChanged = 1, deleted = 1, reordered = true))
    }

    private fun saved(renamed: Int = 0, iconChanged: Int = 0, deleted: Int = 0, linesChanged: Int = 0, reordered: Boolean = false) =
        Events.EditFavoritesSaved(renamed, iconChanged, deleted, linesChanged, reordered)
}
