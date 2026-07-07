package com.dynamicisland.app.ui.island

import com.dynamicisland.app.data.model.IslandNotification

sealed class IslandState {
    /** Isla oculta por completo (sin notificaciones activas). */
    data object Hidden : IslandState()

    /** Cápsula pequeña visible, sin contenido expandido. */
    data object Collapsed : IslandState()

    /** Isla expandida mostrando una notificación concreta. */
    data class Expanded(val notification: IslandNotification) : IslandState()
}
