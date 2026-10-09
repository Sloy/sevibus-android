package com.sloy.debugmenu.events.overlay

import com.sloy.debugmenu.events.CapturedEvent
import com.sloy.debugmenu.events.EventType
import com.sloy.debugmenu.events.chronological

internal object StackSpec {
    const val LIFETIME_MILLIS = 5_000L
    const val HOLD_MILLIS = 2_000L
    const val FADE_TO = 0.5f
    const val EXIT_MILLIS = 450L
    const val COALESCE_WINDOW_MILLIS = 3_000L
    const val BURST_WINDOW_MILLIS = 150L
    const val STAGGER_MILLIS = 45L
    const val CHIP_HEIGHT = 28f
    const val GAP_BURST = 3f
    const val GAP = 9f
    const val TOP_RESERVE = 160f
    const val OLDER_PILL_OFFSET = 34f
    private val FOLD_ALPHAS = floatArrayOf(0.40f, 0.28f, 0.16f)
    private val FOLD_SCALES = floatArrayOf(0.94f, 0.91f, 0.88f)

    fun foldAlpha(index: Int): Float = FOLD_ALPHAS.getOrElse(index) { 0f }
    fun foldScale(index: Int): Float = FOLD_SCALES[minOf(index, 2)]
    fun foldOffset(index: Int): Float = 5f + minOf(index, 2) * 3f
}

internal enum class ChipPhase { IN, OUT }

internal data class StackChip(
    val key: String,
    val name: String,
    val type: EventType,
    val count: Int,
    val phase: ChipPhase,
    val y: Float,
    val scale: Float,
    val alpha: Float,
    val timerFraction: Float,
    val foldIndex: Int?,
)

internal data class StackFrame(val chips: List<StackChip>, val olderCount: Int, val olderY: Float)

private class Run(val key: String, val name: String, val type: EventType, val firstAt: Long, var lastAt: Long, var count: Int = 1)

internal fun stackFrame(
    events: List<CapturedEvent>,
    nowMillis: Long,
    maxHeight: Float,
    lifetimeMillis: Long = StackSpec.LIFETIME_MILLIS,
): StackFrame {
    val runs = buildRuns(events, nowMillis)
    val shown = runs.filter { nowMillis - it.lastAt <= lifetimeMillis + StackSpec.EXIT_MILLIS }.asReversed()

    val placed = ArrayList<StackChip>(shown.size)
    var y = 0f
    var topY = 0f
    var folded = 0
    shown.forEachIndexed { index, run ->
        if (index > 0) {
            val newer = shown[index - 1]
            val isBurst = newer.firstAt - run.lastAt <= StackSpec.BURST_WINDOW_MILLIS
            y -= StackSpec.CHIP_HEIGHT + if (isBurst) StackSpec.GAP_BURST else StackSpec.GAP
        }
        val foldIndex = if (folded > 0 || -y > maxHeight - StackSpec.CHIP_HEIGHT) folded++ else null
        if (foldIndex == null) topY = y
        val age = nowMillis - run.lastAt
        val phase = if (age <= lifetimeMillis) ChipPhase.IN else ChipPhase.OUT
        placed += StackChip(
            key = run.key,
            name = run.name,
            type = run.type,
            count = run.count,
            phase = phase,
            y = y,
            scale = 1f,
            alpha = if (phase == ChipPhase.OUT) 0f else stackAlpha(age, lifetimeMillis),
            timerFraction = (1f - age.toFloat() / lifetimeMillis).coerceIn(0f, 1f),
            foldIndex = foldIndex,
        )
    }
    val chips = placed.map { chip ->
        val foldIndex = chip.foldIndex ?: return@map chip
        chip.copy(
            y = topY - StackSpec.foldOffset(foldIndex),
            scale = StackSpec.foldScale(foldIndex),
            alpha = if (chip.phase == ChipPhase.OUT) 0f else StackSpec.foldAlpha(foldIndex),
        )
    }
    return StackFrame(chips, folded, topY - StackSpec.OLDER_PILL_OFFSET)
}

private fun buildRuns(events: List<CapturedEvent>, nowMillis: Long): List<Run> {
    val runs = mutableListOf<Run>()
    var previousEnterAt: Long? = null
    for (event in events.chronological()) {
        val enterAt = previousEnterAt?.let { maxOf(event.timestampMillis, it + StackSpec.STAGGER_MILLIS) } ?: event.timestampMillis
        if (enterAt > nowMillis) break
        previousEnterAt = enterAt
        val newest = runs.lastOrNull()
        if (newest != null && newest.name == event.name && enterAt - newest.lastAt < StackSpec.COALESCE_WINDOW_MILLIS) {
            newest.count++
            newest.lastAt = enterAt
        } else {
            runs += Run(event.id, event.name, event.type, enterAt, enterAt)
        }
    }
    return runs
}

internal fun stackAlpha(ageMillis: Long, lifetimeMillis: Long): Float {
    val hold = minOf(StackSpec.HOLD_MILLIS, lifetimeMillis - 500)
    if (ageMillis <= hold) return 1f
    val progress = ((ageMillis - hold).toFloat() / (lifetimeMillis - hold)).coerceAtMost(1f)
    return 1f - (1f - StackSpec.FADE_TO) * progress
}
