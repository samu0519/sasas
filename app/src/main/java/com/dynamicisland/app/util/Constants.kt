package com.dynamicisland.app.util

object Constants {
    const val DATASTORE_NAME = "dynamic_island_settings"

    const val ACTION_SHOW_NOTIFICATION = "com.dynamicisland.app.action.SHOW_NOTIFICATION"
    const val ACTION_REMOVE_NOTIFICATION = "com.dynamicisland.app.action.REMOVE_NOTIFICATION"
    const val ACTION_START_OVERLAY = "com.dynamicisland.app.action.START_OVERLAY"
    const val ACTION_STOP_OVERLAY = "com.dynamicisland.app.action.STOP_OVERLAY"

    const val OVERLAY_NOTIFICATION_CHANNEL_ID = "dynamic_island_service_channel"
    const val OVERLAY_NOTIFICATION_ID = 1001

    // Límites razonables para los sliders de personalización
    const val MIN_CORNER_RADIUS = 8f
    const val MAX_CORNER_RADIUS = 40f
    const val MIN_TOP_OFFSET = 0f
    const val MAX_TOP_OFFSET = 60f
    const val MIN_ALPHA = 0.5f
    const val MAX_ALPHA = 1f
}
