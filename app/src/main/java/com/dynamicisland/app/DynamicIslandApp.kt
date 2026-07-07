package com.dynamicisland.app

import android.app.Application
import com.dynamicisland.app.data.repository.SettingsRepository

class DynamicIslandApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa el repositorio de ajustes cuanto antes para que el
        // primer acceso (desde MainActivity o los servicios) sea inmediato.
        SettingsRepository.getInstance(this)
    }
}
