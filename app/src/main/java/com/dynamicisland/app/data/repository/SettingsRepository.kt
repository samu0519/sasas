package com.dynamicisland.app.data.repository

import android.content.Context
import com.dynamicisland.app.data.datastore.SettingsDataStore
import com.dynamicisland.app.data.model.IslandSettings
import kotlinx.coroutines.flow.Flow

class SettingsRepository private constructor(context: Context) {

    private val dataStore = SettingsDataStore(context.applicationContext)

    val settings: Flow<IslandSettings> = dataStore.settingsFlow

    suspend fun updateCollapsedSize(widthDp: Float, heightDp: Float) {
        dataStore.update { it.copy(collapsedWidthDp = widthDp, collapsedHeightDp = heightDp) }
    }

    suspend fun updateExpandedSize(widthDp: Float, heightDp: Float) {
        dataStore.update { it.copy(expandedWidthDp = widthDp, expandedHeightDp = heightDp) }
    }

    suspend fun updateCornerRadius(radiusDp: Float) {
        dataStore.update { it.copy(cornerRadiusDp = radiusDp) }
    }

    suspend fun updateTopOffset(offsetDp: Float) {
        dataStore.update { it.copy(topOffsetDp = offsetDp) }
    }

    suspend fun updateAnimationSpeed(speed: Float) {
        dataStore.update { it.copy(animationSpeed = speed) }
    }

    suspend fun updateAutoDismiss(millis: Long) {
        dataStore.update { it.copy(autoDismissMillis = millis) }
    }

    suspend fun updateBackgroundAlpha(alpha: Float) {
        dataStore.update { it.copy(backgroundAlpha = alpha) }
    }

    suspend fun toggleAppEnabled(packageName: String, enabled: Boolean) {
        dataStore.update { current ->
            val newSet = current.enabledPackages.toMutableSet()
            if (enabled) newSet.add(packageName) else newSet.remove(packageName)
            current.copy(enabledPackages = newSet)
        }
    }

    companion object {
        @Volatile private var instance: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository =
            instance ?: synchronized(this) {
                instance ?: SettingsRepository(context).also { instance = it }
            }
    }
}
