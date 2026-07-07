package com.dynamicisland.app.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dynamicisland.app.data.model.IslandSettings
import com.dynamicisland.app.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = Constants.DATASTORE_NAME)

/**
 * Única fuente de verdad persistida para IslandSettings.
 * Expone un Flow reactivo: cualquier cambio se propaga automáticamente
 * a quien esté observando (ViewModel del overlay y de ajustes).
 */
class SettingsDataStore(private val context: Context) {

    private object Keys {
        val COLLAPSED_WIDTH = floatPreferencesKey("collapsed_width")
        val COLLAPSED_HEIGHT = floatPreferencesKey("collapsed_height")
        val EXPANDED_WIDTH = floatPreferencesKey("expanded_width")
        val EXPANDED_HEIGHT = floatPreferencesKey("expanded_height")
        val CORNER_RADIUS = floatPreferencesKey("corner_radius")
        val TOP_OFFSET = floatPreferencesKey("top_offset")
        val ANIMATION_SPEED = floatPreferencesKey("animation_speed")
        val AUTO_DISMISS = longPreferencesKey("auto_dismiss")
        val BACKGROUND_ALPHA = floatPreferencesKey("background_alpha")
        val ENABLED_PACKAGES = stringSetPreferencesKey("enabled_packages")
    }

    val settingsFlow: Flow<IslandSettings> = context.dataStore.data.map { prefs ->
        val defaults = IslandSettings()
        IslandSettings(
            collapsedWidthDp = prefs[Keys.COLLAPSED_WIDTH] ?: defaults.collapsedWidthDp,
            collapsedHeightDp = prefs[Keys.COLLAPSED_HEIGHT] ?: defaults.collapsedHeightDp,
            expandedWidthDp = prefs[Keys.EXPANDED_WIDTH] ?: defaults.expandedWidthDp,
            expandedHeightDp = prefs[Keys.EXPANDED_HEIGHT] ?: defaults.expandedHeightDp,
            cornerRadiusDp = prefs[Keys.CORNER_RADIUS] ?: defaults.cornerRadiusDp,
            topOffsetDp = prefs[Keys.TOP_OFFSET] ?: defaults.topOffsetDp,
            animationSpeed = prefs[Keys.ANIMATION_SPEED] ?: defaults.animationSpeed,
            autoDismissMillis = prefs[Keys.AUTO_DISMISS] ?: defaults.autoDismissMillis,
            backgroundAlpha = prefs[Keys.BACKGROUND_ALPHA] ?: defaults.backgroundAlpha,
            enabledPackages = prefs[Keys.ENABLED_PACKAGES] ?: defaults.enabledPackages
        )
    }

    suspend fun update(transform: (IslandSettings) -> IslandSettings) {
        context.dataStore.edit { prefs ->
            val current = IslandSettings(
                collapsedWidthDp = prefs[Keys.COLLAPSED_WIDTH] ?: IslandSettings().collapsedWidthDp,
                collapsedHeightDp = prefs[Keys.COLLAPSED_HEIGHT] ?: IslandSettings().collapsedHeightDp,
                expandedWidthDp = prefs[Keys.EXPANDED_WIDTH] ?: IslandSettings().expandedWidthDp,
                expandedHeightDp = prefs[Keys.EXPANDED_HEIGHT] ?: IslandSettings().expandedHeightDp,
                cornerRadiusDp = prefs[Keys.CORNER_RADIUS] ?: IslandSettings().cornerRadiusDp,
                topOffsetDp = prefs[Keys.TOP_OFFSET] ?: IslandSettings().topOffsetDp,
                animationSpeed = prefs[Keys.ANIMATION_SPEED] ?: IslandSettings().animationSpeed,
                autoDismissMillis = prefs[Keys.AUTO_DISMISS] ?: IslandSettings().autoDismissMillis,
                backgroundAlpha = prefs[Keys.BACKGROUND_ALPHA] ?: IslandSettings().backgroundAlpha,
                enabledPackages = prefs[Keys.ENABLED_PACKAGES] ?: IslandSettings().enabledPackages
            )
            val updated = transform(current)
            prefs[Keys.COLLAPSED_WIDTH] = updated.collapsedWidthDp
            prefs[Keys.COLLAPSED_HEIGHT] = updated.collapsedHeightDp
            prefs[Keys.EXPANDED_WIDTH] = updated.expandedWidthDp
            prefs[Keys.EXPANDED_HEIGHT] = updated.expandedHeightDp
            prefs[Keys.CORNER_RADIUS] = updated.cornerRadiusDp
            prefs[Keys.TOP_OFFSET] = updated.topOffsetDp
            prefs[Keys.ANIMATION_SPEED] = updated.animationSpeed
            prefs[Keys.AUTO_DISMISS] = updated.autoDismissMillis
            prefs[Keys.BACKGROUND_ALPHA] = updated.backgroundAlpha
            prefs[Keys.ENABLED_PACKAGES] = updated.enabledPackages
        }
    }
}
