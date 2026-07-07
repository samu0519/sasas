package com.dynamicisland.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dynamicisland.app.service.IslandOverlayService
import com.dynamicisland.app.ui.settings.SettingsScreen
import com.dynamicisland.app.ui.theme.DynamicIslandTheme
import com.dynamicisland.app.util.PermissionUtils

class MainActivity : ComponentActivity() {

    private var overlayGranted by mutableStateOf(false)
    private var notificationAccessGranted by mutableStateOf(false)
    private var batteryExempted by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DynamicIslandTheme {
                SettingsScreen(
                    overlayGranted = overlayGranted,
                    notificationAccessGranted = notificationAccessGranted,
                    batteryExempted = batteryExempted,
                    onRequestOverlayPermission = {
                        startActivity(PermissionUtils.requestOverlayPermissionIntent(this))
                    },
                    onRequestNotificationAccess = {
                        startActivity(PermissionUtils.requestNotificationListenerIntent())
                    },
                    onRequestBatteryExemption = {
                        startActivity(PermissionUtils.requestIgnoreBatteryOptimizationsIntent(this))
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionState()
        maybeStartOverlayService()
    }

    private fun refreshPermissionState() {
        overlayGranted = PermissionUtils.canDrawOverlays(this)
        notificationAccessGranted = PermissionUtils.isNotificationListenerEnabled(this)
        batteryExempted = PermissionUtils.isIgnoringBatteryOptimizations(this)
    }

    /** Arranca el servicio en cuanto tenemos overlay + acceso a notificaciones. */
    private fun maybeStartOverlayService() {
        if (overlayGranted && notificationAccessGranted) {
            val intent = Intent(this, IslandOverlayService::class.java)
            startForegroundService(intent)
        }
    }
}
