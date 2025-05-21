package com.sloy.sevibus.infrastructure

enum class BuildVariant {
    DEBUG,
    RELEASE;

    companion object {
        fun current(): BuildVariant {
            return CurrentBuildVariant
        }

        fun isDebug(): Boolean {
            return current() == DEBUG
        }

        fun isRelease(): Boolean {
            return current() == RELEASE
        }
    }
}
