package me.cugarete.investowl.core.database

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.cugarete.investowl.core.model.AuthState
import me.cugarete.investowl.core.model.UserRead
import java.util.Base64

private val Context.dataStore by preferencesDataStore(name = "investowl_secure_session")

class SessionManager(private val context: Context) {

    private val KEY_ENCRYPTED_USER = stringPreferencesKey("encrypted_user_json")
    private val KEY_AUTH_TOKEN = stringPreferencesKey("encrypted_token")

    private var aead: Aead? = null

    init {
        try {
            AeadConfig.register()
            val keysetHandle = AndroidKeysetManager.Builder()
                .withSharedPref(context, "investowl_tink_keyset", "investowl_master_pref")
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri("android-keystore://investowl_tink_master_key")
                .build()
                .keysetHandle
            aead = keysetHandle.getPrimitive(Aead::class.java)
        } catch (e: Exception) {
            // Fallback gracefully for test environments
            aead = null
        }
    }

    private fun encrypt(data: String): String {
        val aeadInstance = aead ?: return Base64.getEncoder().encodeToString(data.toByteArray(Charsets.UTF_8))
        return try {
            val ciphertext = aeadInstance.encrypt(data.toByteArray(Charsets.UTF_8), null)
            Base64.getEncoder().encodeToString(ciphertext)
        } catch (e: Exception) {
            Base64.getEncoder().encodeToString(data.toByteArray(Charsets.UTF_8))
        }
    }

    private fun decrypt(encryptedBase64: String): String? {
        if (encryptedBase64.isEmpty()) return null
        return try {
            val bytes = Base64.getDecoder().decode(encryptedBase64)
            val aeadInstance = aead
            if (aeadInstance != null) {
                String(aeadInstance.decrypt(bytes, null), Charsets.UTF_8)
            } else {
                String(bytes, Charsets.UTF_8)
            }
        } catch (e: Exception) {
            null
        }
    }

    val userFlow: Flow<UserRead?> = context.dataStore.data.map { prefs ->
        val rawEncrypted = prefs[KEY_ENCRYPTED_USER] ?: return@map null
        val decryptedJson = decrypt(rawEncrypted) ?: return@map null
        try {
            kotlinx.serialization.json.Json.decodeFromString<UserRead>(decryptedJson)
        } catch (e: Exception) {
            null
        }
    }

    val authStateFlow: Flow<AuthState> = userFlow.map { user ->
        when {
            user == null -> AuthState.GUEST
            user.role == "admin" -> AuthState.ADMIN
            user.isApproved -> AuthState.ACTIVE_USER
            else -> AuthState.PENDING_APPROVAL
        }
    }

    suspend fun saveSession(user: UserRead, token: String? = null) {
        val json = kotlinx.serialization.json.Json.encodeToString(UserRead.serializer(), user)
        val encryptedJson = encrypt(json)
        val encryptedToken = token?.let { encrypt(it) }

        context.dataStore.edit { prefs ->
            prefs[KEY_ENCRYPTED_USER] = encryptedJson
            if (encryptedToken != null) {
                prefs[KEY_AUTH_TOKEN] = encryptedToken
            }
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ENCRYPTED_USER)
            prefs.remove(KEY_AUTH_TOKEN)
        }
    }
}
