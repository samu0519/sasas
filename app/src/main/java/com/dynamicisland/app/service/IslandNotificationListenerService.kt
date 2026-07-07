package com.dynamicisland.app.service

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.content.ContextCompat
import com.dynamicisland.app.data.model.IslandNotification
import com.dynamicisland.app.data.repository.NotificationRepository
import com.dynamicisland.app.data.repository.SettingsRepository
import com.dynamicisland.app.util.AppIconProvider
import com.dynamicisland.app.util.PermissionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Captura las notificaciones publicadas por el sistema, filtra por
 * las apps que el usuario habilitó y republica una versión ligera
 * (IslandNotification) al repositorio, que consume el overlay.
 *
 * No procesa nada pesado aquí: solo mapea y delega.
 */
class IslandNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var iconProvider: AppIconProvider
    private lateinit var settingsRepository: SettingsRepository

    companion object {
        @Volatile private var activeInstance: IslandNotificationListenerService? = null

        /** Cierra la notificación original en el sistema, si el listener está conectado. */
        fun dismiss(key: String) {
            activeInstance?.cancelNotification(key)
        }
    }

    override fun onCreate() {
        super.onCreate()
        iconProvider = AppIconProvider(this)
        settingsRepository = SettingsRepository.getInstance(this)
        activeInstance = this

        // Asegura que el servicio overlay esté activo mientras haya listener.
        // Solo lo arrancamos si el permiso de overlay está realmente concedido:
        // si no lo está, IslandOverlayService lanzaría una BadTokenException al
        // intentar dibujar la ventana, y como el servicio es START_STICKY,
        // Android lo reiniciaría en bucle (esto es lo que puede sentirse como
        // que "se traba el celular" al llegar cada notificación).
        if (PermissionUtils.canDrawOverlays(this)) {
            ContextCompat.startForegroundService(this, Intent(this, IslandOverlayService::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activeInstance === this) activeInstance = null
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationRepository.setListenerConnected(true)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        NotificationRepository.setListenerConnected(false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Ignora notificaciones propias y de "grupo resumen" sin contenido útil.
        if (sbn.packageName == packageName) return
        if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return

        serviceScope.launch {
            val enabledPackages = settingsRepository.settings.first().enabledPackages
            if (sbn.packageName !in enabledPackages) return@launch

            val extras = sbn.notification.extras
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()

            if (title.isBlank() && text.isBlank()) return@launch

            val islandNotification = IslandNotification(
                key = sbn.key,
                packageName = sbn.packageName,
                appName = iconProvider.getAppName(sbn.packageName),
                title = title,
                text = text,
                icon = iconProvider.getIcon(sbn.packageName)
            )

            NotificationRepository.emitNotification(islandNotification)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        serviceScope.launch {
            NotificationRepository.emitRemoved(sbn.key)
        }
    }
}
