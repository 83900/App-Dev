package com.wanderlog.app.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val KEY_LOGGED_IN = booleanPreferencesKey("is_logged_in")

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_LOGGED_IN] ?: false
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_LOGGED_IN] = loggedIn
        }
    }
}

