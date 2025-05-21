package com.sloy.sevibus.infrastructure.extensions

import android.view.View
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat

fun View.performHapticGestureStart(){
    ViewCompat.performHapticFeedback(this, HapticFeedbackConstantsCompat.GESTURE_START)
}

fun View.performHapticClockTick(){
    ViewCompat.performHapticFeedback(this, HapticFeedbackConstantsCompat.CLOCK_TICK)
}

fun View.performHapticSegmentTick(){
    ViewCompat.performHapticFeedback(this, HapticFeedbackConstantsCompat.SEGMENT_TICK)
}

fun View.performHapticReject(){
    ViewCompat.performHapticFeedback(this, HapticFeedbackConstantsCompat.REJECT)
}
