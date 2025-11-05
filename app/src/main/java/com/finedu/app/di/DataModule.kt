package com.finedu.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Este es el nombre de tu archivo de sesión en el teléfono
private const val SESSION_PREFERENCES = "session_prefs"

// Creamos una extensión para acceder al DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = SESSION_PREFERENCES
)

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.dataStore
    }
}