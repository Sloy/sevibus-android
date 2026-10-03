package com.sloy.debugmenu.overlay

import android.content.Context
import androidx.core.content.edit

internal data class StoredPosition(val edge: Edge, val yFraction: Float)

internal class FloatingButtonPositionStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)

    fun read(): StoredPosition? {
        val edge = prefs.getString(KEY_EDGE, null)
            ?.let { name -> Edge.entries.firstOrNull { it.name == name } }
            ?: return null
        val yFraction = prefs.getFloat(KEY_Y_FRACTION, -1f).takeIf { it in 0f..1f } ?: return null
        return StoredPosition(edge, yFraction)
    }

    fun write(position: StoredPosition) {
        prefs.edit {
            putString(KEY_EDGE, position.edge.name)
            putFloat(KEY_Y_FRACTION, position.yFraction)
        }
    }
}

private const val PREFERENCES_FILE = "debug_menu_floating_button"
private const val KEY_EDGE = "edge"
private const val KEY_Y_FRACTION = "y_fraction"
