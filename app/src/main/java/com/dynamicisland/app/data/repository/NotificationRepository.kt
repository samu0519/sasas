package com.dynamicisland.app.data.repository

import com.dynamicisland.app.data.model.IslandNotification
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton en memoria (proceso único, servicio y UI comparten el mismo proceso)
 * que conecta el NotificationListenerService con el ViewModel de la isla,
 * sin necesidad de un bus de eventos pesado.
 */
object NotificationRepository {

    private val _incoming = MutableSharedFlow<IslandNotification>(extraBufferCapacity = 4)
    val incoming: SharedFlow<IslandNotification> = _incoming.asSharedFlow()

    private val _removedKey = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val removedKey: SharedFlow<String> = _removedKey.asSharedFlow()

    private val _listenerConnected = MutableStateFlow(false)
    val listenerConnected: StateFlow<Boolean> = _listenerConnected.asStateFlow()

    suspend fun emitNotification(notification: IslandNotification) {
        _incoming.emit(notification)
    }

    suspend fun emitRemoved(key: String) {
        _removedKey.emit(key)
    }

    fun setListenerConnected(connected: Boolean) {
        _listenerConnected.value = connected
    }
}
