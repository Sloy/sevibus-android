package com.sloy.sevibus.infrastructure.extensions


/**
 * Splits a phrase with multiple words into 2 lines of text, so that both lines have a similar length.
 * If the phrase is too short only 1 line is returned.
 */
fun String.splitPhrase(): List<String> {
    val phrase = this
    val words = phrase.split(" ")
    if (words.size <= 1 || phrase.length <= 8) {
        return listOf(phrase)
    }

    var bestSplitIndex = -1
    var minLengthDifference = Int.MAX_VALUE

    for (i in 1 until words.size) {
        val line1 = words.subList(0, i).joinToString(" ")
        val line2 = words.subList(i, words.size).joinToString(" ")
        val lengthDifference = kotlin.math.abs(line1.length - line2.length)

        if (lengthDifference < minLengthDifference) {
            minLengthDifference = lengthDifference
            bestSplitIndex = i
        }
    }

    return if (bestSplitIndex == -1) {
        listOf(phrase)
    } else {
        listOf(
            words.subList(0, bestSplitIndex).joinToString(" "),
            words.subList(bestSplitIndex, words.size).joinToString(" ")
        )
    }
}
