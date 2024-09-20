package com.sloy.sevibus

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sloy.sevibus.feature.cards.NfcDecoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onNewIntent(intent)
        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if ("android.nfc.action.TECH_DISCOVERED" == intent?.action) {
            val cardId = NfcDecoder.readCard(intent)
            Toast.makeText(this, cardId.toString(), Toast.LENGTH_LONG).show()
        }
    }
}
