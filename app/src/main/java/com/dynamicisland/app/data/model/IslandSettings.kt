package com.dynamicisland.app.data.model

/**
 * Configuración persistida en DataStore. Todos los valores tienen
 * un default sensato para que la app funcione bien "out of the box".
 */
data class IslandSettings(
    val collapsedWidthDp: Float = 126f,
    val collapsedHeightDp: Float = 36f,
    val expandedWidthDp: Float = 340f,
    val expandedHeightDp: Float = 108f,
    val cornerRadiusDp: Float = 24f,
    val topOffsetDp: Float = 12f,
    val animationSpeed: Float = 1f,        // multiplicador: 0.5 = más lento, 2 = más rápido
    val autoDismissMillis: Long = 4000L,   // duración visible antes de auto-colapsar
    val backgroundAlpha: Float = 0.95f,    // transparencia del fondo (0..1)
    val enabledPackages: Set<String> = emptySet() // apps cuyas notificaciones se muestran
)
