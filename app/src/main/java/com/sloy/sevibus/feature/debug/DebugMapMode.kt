package com.sloy.sevibus.feature.debug

/**
 * How the home map is rendered, set by the debug launch arguments.
 */
enum class DebugMapMode {
    /** The regular map. */
    Full,

    /**
     * The map never enters the composition.
     * Drawing the stop markers keeps an emulator busy, which makes UI tests slow and flaky.
     */
    Disabled;

    companion object {
        fun fromArgument(value: String?): DebugMapMode? = when (value) {
            "full" -> Full
            "disabled" -> Disabled
            else -> null
        }
    }
}
