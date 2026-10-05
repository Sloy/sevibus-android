package com.sloy.sevibus.infrastructure.session

import android.content.Context
import java.io.File
import java.security.KeyStore

/**
 * Firebase Auth persists the signed-in user encrypted with a Tink keyset, which is in turn wrapped by an
 * Android Keystore key. Keystore keys are never backed up, but the shared preferences holding the keyset and
 * the user are. After restoring a backup on a new device, Firebase finds a keyset it can't decrypt and is
 * unable to load or persist the session, so the user is logged out on every cold start.
 *
 * This removes the orphan Firebase Auth preferences so Firebase can create a fresh keyset.
 * It must run before Firebase Auth is initialized.
 *
 * Relies on Firebase Auth internal file and key names, so it does nothing if they change.
 */
object FirebaseAuthStorageRepair {

    private const val CRYPTO_PREFS_PREFIX = "com.google.firebase.auth.api.crypto."
    private const val STORE_PREFS_PREFIX = "com.google.firebase.auth.api.Store."
    private const val KEY_ALIAS_PREFIX = "firebear_main_key_id_for_storage_crypto."

    /**
     * @return the persistence keys whose storage was repaired, empty if nothing was done.
     */
    fun repairIfNeeded(context: Context): List<String> {
        val prefsDir = File(context.dataDir, "shared_prefs")
        val persistenceKeys = prefsDir.list().orEmpty()
            .filter { it.startsWith(CRYPTO_PREFS_PREFIX) && it.endsWith(".xml") }
            .map { it.removePrefix(CRYPTO_PREFS_PREFIX).removeSuffix(".xml") }
        if (persistenceKeys.isEmpty()) return emptyList()

        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return persistenceKeys
            .filterNot { keyStore.containsAlias(KEY_ALIAS_PREFIX + it) }
            .onEach {
                context.deleteSharedPreferences(CRYPTO_PREFS_PREFIX + it)
                context.deleteSharedPreferences(STORE_PREFS_PREFIX + it)
            }
    }
}
