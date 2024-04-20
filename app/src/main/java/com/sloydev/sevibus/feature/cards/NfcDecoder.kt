package com.sloydev.sevibus.feature.cards

import android.content.Intent
import android.nfc.Tag

object NfcDecoder {

    fun readCard(intent: Intent): Long? {
        val tag = intent.getParcelableExtra("android.nfc.extra.TAG") as Tag? ?: return null
        return getCardId(tag)
    }

    private fun getCardId(tag: Tag): Long {
        val hexString = tag.id.toHexString()
        val id = reverseHexBytes(hexString)
        return id.toLong(16)
    }

    private fun reverseHexBytes(hexString: String): String {
        val stringBuilder = StringBuilder()
        var index = 6
        while (index >= 0) {
            stringBuilder.append(hexString.substring(index, index + 2))
            index -= 2
        }
        return stringBuilder.toString()
    }

    private fun ByteArray.toHexString(): String {
        val hexChars = "0123456789ABCDEF".toCharArray()
        val result = StringBuilder()

        for (byte in this) {
            val intValue = byte.toInt() and 0xFF
            val highNibble = intValue ushr 4
            val lowNibble = intValue and 0x0F
            result.append(hexChars[highNibble])
            result.append(hexChars[lowNibble])
        }

        return result.toString()
    }

}