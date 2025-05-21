package com.sloy.sevibus

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.sloy.sevibus.feature.cards.NfcDecoder
import com.sloy.sevibus.infrastructure.nfc.ListenForNfcStateChanges
import com.sloy.sevibus.infrastructure.nfc.NfcReadEvent
import com.sloy.sevibus.infrastructure.nfc.NfcStateManager
import com.sloy.sevibus.navigation.NavigationDestination
import com.sloy.sevibus.navigation.SevNavigator
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class MainActivity : ComponentActivity() {

    val sevNavigator: SevNavigator by inject()
    val nfcStateManager: NfcStateManager by inject()
    private val nfcAdapter: NfcAdapter? by lazy { NfcAdapter.getDefaultAdapter(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onNewIntent(intent)
        enableEdgeToEdge()
        setContent {
            App()
            ListenForNfcStateChanges(nfcStateManager)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if ("android.nfc.action.TECH_DISCOVERED" == intent?.action) {
            val cardId = NfcDecoder.readCard(intent)
            if (cardId != null) {
                sevNavigator.navigate(NavigationDestination.Cards)
                lifecycleScope.launch {
                    nfcStateManager.emitNfcCardRead(cardId)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enableForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        disableForegroundDispatch()
    }

    /**
     * The filters in this function should match those in the AndroidManifest.xml
     */
    private fun enableForegroundDispatch() {
        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val filters = arrayOf(IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))
        val techLists = arrayOf(
            arrayOf(android.nfc.tech.NfcA::class.java.name),
            arrayOf(android.nfc.tech.MifareClassic::class.java.name)
        )

        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, filters, techLists)
    }

    private fun disableForegroundDispatch() {
        nfcAdapter?.disableForegroundDispatch(this)
    }
}
