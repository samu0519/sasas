package com.dynamicisland.app.data.model

import android.graphics.drawable.Drawable

/**
 * Representa una notificación capturada por el NotificationListener,
 * ya reducida a lo mínimo que la isla necesita mostrar.
 */
data class IslandNotification(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val icon: Drawable?,
    val timestamp: Long = System.currentTimeMillis()
)
