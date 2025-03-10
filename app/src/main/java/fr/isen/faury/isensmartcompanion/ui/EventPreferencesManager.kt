package fr.isen.faury.isensmartcompanion.ui

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EventPreferencesManager(private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "event_preferences")

    companion object {
        fun getNotificationKey(eventId: String) = booleanPreferencesKey("notification_${eventId}")
    }

    suspend fun setEventNotificationPreference(eventId: Long, isNotificationEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[getNotificationKey(eventId.toString())] = isNotificationEnabled
        }
    }

    fun getEventNotificationPreference(eventId: String): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[getNotificationKey(eventId)] ?: false
        }
    }
}