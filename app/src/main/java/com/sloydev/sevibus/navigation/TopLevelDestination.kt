/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sloydev.sevibus.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import com.sloydev.sevibus.R

/**
 * Type for the top level destinations in the application. Each of these destinations
 * can contain one or more screens (based on the window size). Navigation from one screen to the
 * next within a single destination will be handled directly in composables.
 */
enum class TopLevelDestination(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconTextId: Int,
    val titleTextId: Int,
) {
    FOR_YOU(
        route = "/home",
        selectedIcon = Icons.Default.Face,
        unselectedIcon = Icons.Default.Face,
        iconTextId = R.string.navigation_for_you,
        titleTextId = R.string.app_name,
    ),
    LINES(
        route = "/lines",
        selectedIcon = Icons.Default.ShoppingCart,
        unselectedIcon = Icons.Default.ShoppingCart,
        iconTextId = R.string.navigation_lines,
        titleTextId = R.string.navigation_lines,
    ),
    MAP(
        route = "/map",
        selectedIcon = Icons.Default.Email,
        unselectedIcon = Icons.Default.Email,
        iconTextId = R.string.navigation_map,
        titleTextId = R.string.navigation_map,
    ),
    CARDS(
        route = "/cards",
        selectedIcon = Icons.Default.Delete,
        unselectedIcon = Icons.Default.Delete,
        iconTextId = R.string.navigation_cards,
        titleTextId = R.string.navigation_cards,
    ),
}
