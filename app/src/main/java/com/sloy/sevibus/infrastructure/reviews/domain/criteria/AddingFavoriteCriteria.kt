package com.sloy.sevibus.infrastructure.reviews.domain.criteria

import com.sloy.sevibus.domain.repository.FavoriteRepository
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.events.Clicks
import com.sloy.sevibus.infrastructure.reviews.domain.HappyMomentCriteria
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * "Adding favorite" happy moment criteria implementation.
 *
 * The conditions are:
 * - The user adds a new favorite stop
 * - It's not their first favorite (they already have at least one favorite)
 *
 * Trigger the happy moment when:
 * - AddFavoriteClicked event is dispatched and the user already has favorites
 * - This is a momentary trigger that resets after triggering
 */
class AddingFavoriteCriteria(
    private val favoriteRepository: FavoriteRepository
) : HappyMomentCriteria {

    override val name = "Adding favorite"

    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _happyMoment = MutableStateFlow(false)

    override fun observeHappyMoment(): StateFlow<Boolean> {
        return _happyMoment.asStateFlow()
    }

    override fun dispatch(event: SevEvent) {
        when (event) {
            is Clicks.AddFavoriteClicked -> {
                backgroundScope.launch {
                    // Check if user already has favorites (before adding this one)
                    val currentFavorites = favoriteRepository.obtainFavorites()

                    // Trigger happy moment if this is not their first favorite
                    if (currentFavorites.isNotEmpty()) {
                        _happyMoment.value = true
                    }
                }
            }

            else -> { /* Ignore other events */
            }
        }
    }
}