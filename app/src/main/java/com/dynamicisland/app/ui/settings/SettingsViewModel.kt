package com.dynamicisland.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dynamicisland.app.data.model.IslandSettings
import com.dynamicisland.app.data.repository.SettingsRepository
import com.dynamicisland.app.util.AppIconProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository.getInstance(application)
    private val iconProvider = AppIconProvider(application)

    val settings: StateFlow<IslandSettings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = IslandSettings()
    )

    /** Apps lanzables del dispositivo, para que el usuario elija cuáles mostrar. */
    val installedApps: List<Pair<String, String>> by lazy { iconProvider.getLaunchableApps() }

    fun updateCollapsedSize(widthDp: Float, heightDp: Float) = viewModelScope.launch {
        repository.updateCollapsedSize(widthDp, heightDp)
    }

    fun updateExpandedSize(widthDp: Float, heightDp: Float) = viewModelScope.launch {
        repository.updateExpandedSize(widthDp, heightDp)
    }

    fun updateCornerRadius(radiusDp: Float) = viewModelScope.launch {
        repository.updateCornerRadius(radiusDp)
    }

    fun updateTopOffset(offsetDp: Float) = viewModelScope.launch {
        repository.updateTopOffset(offsetDp)
    }

    fun updateAnimationSpeed(speed: Float) = viewModelScope.launch {
        repository.updateAnimationSpeed(speed)
    }

    fun updateAutoDismiss(millis: Long) = viewModelScope.launch {
        repository.updateAutoDismiss(millis)
    }

    fun updateBackgroundAlpha(alpha: Float) = viewModelScope.launch {
        repository.updateBackgroundAlpha(alpha)
    }

    fun toggleApp(packageName: String, enabled: Boolean) = viewModelScope.launch {
        repository.toggleAppEnabled(packageName, enabled)
    }
}
