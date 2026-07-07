package com.dynamicisland.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dynamicisland.app.data.model.IslandSettings
import com.dynamicisland.app.data.repository.SettingsRepository
import com.dynamicisland.app.util.AppIconProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository.getInstance(application)
    private val iconProvider = AppIconProvider(application)

    val settings: StateFlow<IslandSettings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = IslandSettings()
    )

    /**
     * Apps lanzables del dispositivo, para que el usuario elija cuáles mostrar.
     * Se calcula en un hilo de fondo: recorrer el PackageManager y pedir el
     * nombre de cada app instalada es una operación pesada de IO/CPU que, si
     * se hace en el hilo principal (como hacía antes con `by lazy`), bloquea
     * la pantalla varios segundos en dispositivos con muchas apps.
     */
    private val _installedApps = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val installedApps: StateFlow<List<Pair<String, String>>> = _installedApps.asStateFlow()

    init {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.Default) { iconProvider.getLaunchableApps() }
            _installedApps.value = apps
        }
    }

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
