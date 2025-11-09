package com.finedu.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object PrefKeys {
        val ID_TOKEN = stringPreferencesKey("id_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val UID = stringPreferencesKey("uid")
        val EMAIL = stringPreferencesKey("email")
        val NAME = stringPreferencesKey("name")
    }

    /**
     * Llamado por LoginViewModel. Ahora es una 'suspend fun'.
     * Guarda los datos de la sesión EN EL DISCO.
     */
    suspend fun saveSession(sessionData: UserSessionData) {
        dataStore.edit { preferences ->
            preferences[PrefKeys.ID_TOKEN] = sessionData.idToken
            preferences[PrefKeys.REFRESH_TOKEN] = sessionData.refreshToken
            preferences[PrefKeys.UID] = sessionData.uid
            preferences[PrefKeys.EMAIL] = sessionData.email
            preferences[PrefKeys.NAME] = sessionData.name
        }
    }

    /**
     * Llamado por ProfileViewModel.
     * Lee el Flow de datos DEL DISCO.
     */
    fun getStoredSession(): Flow<UserSessionData?> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                // Leemos cada valor usando las llaves
                val idToken = preferences[PrefKeys.ID_TOKEN]
                val refreshToken = preferences[PrefKeys.REFRESH_TOKEN]
                val uid = preferences[PrefKeys.UID]
                val email = preferences[PrefKeys.EMAIL]
                val name = preferences[PrefKeys.NAME]
                if (idToken == null || uid == null || email == null || name == null || refreshToken == null) {
                    null
                } else {
                    // Si todo está, creamos el objeto de sesión
                    UserSessionData(
                        idToken = idToken,
                        refreshToken = refreshToken,
                        uid = uid,
                        email = email,
                        name = name
                    )
                }
            }
    }

    /**
     * Llamado en el Logout. Ahora es una 'suspend fun'.
     * Borra todas las preferencias de sesión guardadas.
     */
    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}