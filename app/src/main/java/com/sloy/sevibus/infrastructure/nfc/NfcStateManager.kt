package com.sloy.sevibus.infrastructure.nfc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.sloy.sevibus.domain.model.CardId
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class NfcStateManager(private val context: Context) {

    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)

    val state: MutableStateFlow<NfcState> = MutableStateFlow(nfcAdapter.asState())

    private val _eventChannel = Channel<NfcReadEvent>(Channel.BUFFERED)
    val events get() = _eventChannel.receiveAsFlow()

    suspend fun emitNfcCardRead(cardId: CardId) {
        _eventChannel.send(NfcReadEvent(cardId))
    }
}

data class NfcReadEvent(val cardId: CardId, val isHandled: Boolean = false, val timestamp: Long = System.currentTimeMillis())

private fun NfcAdapter?.asState() = when {
    this == null -> NfcState.NOT_AVAILABLE
    !this.isEnabled -> NfcState.DISABLED
    else -> NfcState.ENABLED
}

@Composable
fun Context.ListenForNfcStateChanges(nfc: NfcStateManager) {
    DisposableEffect(Unit) {
        val receiver: BroadcastReceiver = object : BroadcastReceiver() {
            public override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action

                if (action == NfcAdapter.ACTION_ADAPTER_STATE_CHANGED) {
                    val state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF)
                    when (state) {
                        NfcAdapter.STATE_OFF -> {
                            nfc.state.value = NfcState.DISABLED
                        }

                        NfcAdapter.STATE_ON -> {
                            nfc.state.value = NfcState.ENABLED
                        }
                    }
                }
            }
        }
        registerReceiver(receiver, IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED))
        onDispose {
            unregisterReceiver(receiver)
        }
    }
}
