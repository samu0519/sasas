package com.dynamicisland.app.ui.island

import android.content.Context
import android.content.Intent
import com.dynamicisland.app.data.model.IslandNotification
import com.dynamicisland.app.data.model.IslandSettings
import com.dynamicisland.app.data.repository.NotificationRepository
import com.dynamicisland.app.data.repository.SettingsRepository
import com.dynamicisland.app.service.IslandNotificationListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.ArrayDeque

/**
 * ViewModel plano (no AndroidX ViewModel) porque vive dentro del
 * Foreground Service del overlay, que no tiene un ViewModelStoreOwner
 * asociado como lo tendría una Activity. Sigue el patrón MVVM:
 * expone estado inmutable observable y recibe acciones desde la UI.
 */
class IslandViewModel(
    private val appContext: Context,
    private val scope: CoroutineScope,
    private val settingsRepository: SettingsRepository
) {
    private val _state = MutableStateFlow<IslandState>(IslandState.Hidden)
    val state: StateFlow<IslandState> = _state.asStateFlow()

    private val _settings = MutableStateFlow(IslandSettings())
    val settings: StateFlow<IslandSettings> = _settings.asStateFlow()

    private val pendingQueue = ArrayDeque<IslandNotification>()
    private var autoCollapseJob: Job? = null
    private var currentKey: String? = null

    init {
        scope.launch {
            settingsRepository.settings.collect { _settings.value = it }
        }
        scope.launch {
            NotificationRepository.incoming.collect { notification ->
                enqueue(notification)
            }
        }
        scope.launch {
            NotificationRepository.removedKey.collect { key ->
                if (key == currentKey) {
                    collapseAndAdvance()
                } else {
                    pendingQueue.removeAll { it.key == key }
                }
            }
        }
    }

    private fun enqueue(notification: IslandNotification) {
        pendingQueue.addLast(notification)
        if (_state.value !is IslandState.Expanded) {
            showNext()
        }
    }

    private fun showNext() {
        val next = pendingQueue.pollFirst() ?: run {
            _state.value = IslandState.Collapsed
            scope.launch {
                delay(1500L)
                if (_state.value == IslandState.Collapsed) {
                    _state.value = IslandState.Hidden
                }
            }
            return
        }
        currentKey = next.key
        _state.value = IslandState.Expanded(next)
        scheduleAutoCollapse()
    }

    private fun scheduleAutoCollapse() {
        autoCollapseJob?.cancel()
        val durationMillis = (_settings.value.autoDismissMillis / _settings.value.animationSpeed.coerceAtLeast(0.1f)).toLong()
        autoCollapseJob = scope.launch {
            delay(durationMillis)
            collapseAndAdvance()
        }
    }

    private fun collapseAndAdvance() {
        autoCollapseJob?.cancel()
        currentKey = null
        showNext()
    }

    /** El usuario tocó la isla expandida: abre la app de la notificación. */
    fun onNotificationTapped() {
        val expanded = _state.value as? IslandState.Expanded ?: return
        val launchIntent = appContext.packageManager
            .getLaunchIntentForPackage(expanded.notification.packageName)
            ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        launchIntent?.let { appContext.startActivity(it) }
        collapseAndAdvance()
    }

    /** El usuario pulsó la X: cierra la notificación original y avanza. */
    fun onDismissTapped() {
        val expanded = _state.value as? IslandState.Expanded ?: return
        IslandNotificationListenerService.dismiss(expanded.notification.key)
        collapseAndAdvance()
    }

    /** Toca la cápsula pequeña: no hay nada que expandir manualmente por diseño (solo eventos). */
    fun onCollapsedTapped() {
        // Reservado por si en el futuro se añade contenido persistente al tocar.
    }
}
