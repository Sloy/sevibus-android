package com.sloy.sevibus.feature.cards

import com.sloy.sevibus.domain.model.CardId

object CardSerialNumberUtils {

    fun calculateVisibleSerialNumber(cardId: CardId): String {
        return calculate(cardId.toString())
    }

    /**
     * Calculates the control digits for a card id.
     * Ensures the input is padded to 10 characters, extracts TNS bytes, computes control digits,
     * and reinserts them into the correct position.
     */
    fun calculate(input: String): String {
        val paddedInput = input.padStart(10, '0')
        val extractedBytes = extractTNSBytes(paddedInput)
        val controlDigits = calculateControlDigits(
            paddedInput, extractedBytes[0], extractedBytes[1], extractedBytes[2], extractedBytes[3]
        )
        return paddedInput.substring(0, 5) + controlDigits + paddedInput.substring(5, 10)
    }

    /**
     * Computes the control digits based on extracted binary values from the input string.
     * Uses XOR operations and bit shifting for calculation.
     */
    private fun calculateControlDigits(
        input: String, b1: String, b2: String, b3: String, b4: String
    ): String {
        val binToDecB1 = binToDec(b1)
        val xorValue = ((binToDec(b2).toInt() xor binToDecB1.toInt())
                xor binToDec(b3).toInt()) xor binToDec(b4).toInt()
        val shiftedBits = shiftControlBits(decToBin(xorValue.toLong(), 8), input.toLong() % 7)
        var part1 = binToDec(shiftedBits.substring(0, 4))
        if (part1 > 9) part1 -= 7
        var part2 = binToDec(shiftedBits.substring(4, 8))
        if (part2 > 9) part2 -= 7
        return "$part1$part2"
    }

    /**
     * Extracts 4 groups of 8-bit binary strings from the 32-bit binary representation of the input.
     */
    private fun extractTNSBytes(input: String): Array<String> {
        val binary = decToBin(input.toLong(), 32)
        return arrayOf(
            binary.substring(0, 8), binary.substring(8, 16),
            binary.substring(16, 24), binary.substring(24, 32)
        )
    }

    /**
     * Converts a decimal number to a binary string of specified bit length.
     */
    private fun decToBin(number: Long, bits: Int): String {
        return number.toString(2).padStart(bits, '0')
    }

    /**
     * Converts a binary string to a decimal number.
     */
    private fun binToDec(binary: String): Long {
        return binary.toLong(2)
    }

    /**
     * Shifts the bits in the control binary string based on the shift value.
     * Ensures correct reordering of bits in an 8-bit string.
     */
    private fun shiftControlBits(binary: String, shift: Long): String {
        val result = CharArray(8)
        for (i in 1..8) {
            var newIndex = i - shift.toInt()
            if (newIndex < 0) newIndex += 8 else if (newIndex == 0) newIndex = 8
            result[newIndex - 1] = binary[i - 1]
        }
        return String(result)
    }
}
