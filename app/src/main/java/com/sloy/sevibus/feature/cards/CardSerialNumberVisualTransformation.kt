package com.sloy.sevibus.feature.cards

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class CardSerialNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val transformedText = text.chunked(4).joinToString(" ")
        return TransformedText(AnnotatedString(transformedText), SerialNumberOffsetMapping)
    }

    object SerialNumberOffsetMapping : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            return when (offset) {
                in 0..4 -> offset
                in 5..8 -> offset + 1
                in 9..12 -> offset + 2
                else -> offset + 2
            }
        }

        override fun transformedToOriginal(offset: Int): Int {
            return when (offset) {
                in 0..3 -> offset
                in 4..8 -> offset - 1
                in 9..14 -> offset - 2
                else -> offset - 2
            }
        }

    }


}
