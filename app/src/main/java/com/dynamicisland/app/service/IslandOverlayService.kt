package com.dynamicisland.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.dynamicisland.app.R
import com.dynamicisland.app.data.repository.SettingsRepository
import com.dynamicisland.app.ui.island.IslandComposable
import com.dynamicisland.app.ui.island.IslandOverlayHost
import com.dynamicisland.app.ui.island.IslandViewModel
import com.dynamicisland.app.util.Constants
import com.dynamicisland.app.util.PermissionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class IslandOverlayService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private lateinit var windowManager: WindowManager
    private lateinit var overlayHost: IslandOverlayHost
    private lateinit var islandViewModel: IslandViewModel
    private var composeView: ComposeView? = null
    private var currentLayoutParams: WindowManager.LayoutParams? = null

    private var setupFailed = false

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        startForeground(Constants.OVERLAY_NOTIFICATION_ID, buildForegroundNotification())

        // Si el permiso de overlay no está realmente concedido (puede pasar en
        // HyperOS/MIUI incluso cuando Settings.canDrawOverlays ya decía que sí,
        // o si el usuario lo revocó manualmente), no intentamos dibujar la
        // ventana: eso lanzaría una excepción y, al ser START_STICKY, el
        // sistema reiniciaría el servicio una y otra vez, degradando todo el
        // teléfono. En su lugar, paramos el servicio de forma controlada.
        if (!PermissionUtils.canDrawOverlays(this)) {
            setupFailed = true
            stopSelf()
            return
        }

        val settingsRepository = SettingsRepository.getInstance(this)
        islandViewModel = IslandViewModel(applicationContext, serviceScope, settingsRepository)

        overlayHost = IslandOverlayHost().also { it.onCreate() }
        addOverlayView()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (setupFailed) return START_NOT_STICKY
        // START_STICKY: si el sistema mata el proceso por memoria, HyperOS
        // lo relanza automáticamente para que la isla siga disponible.
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        removeOverlayView()
        if (::overlayHost.isInitialized) {
            overlayHost.onDestroy()
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun addOverlayView() {
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 12 // se recalcula dinámicamente según settings más abajo
        }
        currentLayoutParams = params

        val view = ComposeView(this)
        view.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        view.setViewTreeLifecycleOwner(overlayHost)
        view.setViewTreeViewModelStoreOwner(overlayHost)
        view.setViewTreeSavedStateRegistryOwner(overlayHost)

        view.setContent {
            val state by islandViewModel.state.collectAsState()
            val settings by islandViewModel.settings.collectAsState()
            val density = LocalDensity.current

            // Ajusta el offset vertical de la ventana según configuración del usuario.
            LaunchedEffect(settings.topOffsetDp) {
                val offsetPx = with(density) { settings.topOffsetDp.dp.roundToPx() }
                currentLayoutParams?.let { lp ->
                    lp.y = offsetPx
                    runCatching { windowManager.updateViewLayout(view, lp) }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                IslandComposable(
                    state = state,
                    settings = settings,
                    onTap = islandViewModel::onNotificationTapped,
                    onDismiss = islandViewModel::onDismissTapped
                )
            }
        }

        composeView = view
        try {
            windowManager.addView(view, params)
        } catch (e: Exception) {
            // Algunos fabricantes (HyperOS/MIUI incluidos) pueden denegar la
            // ventana overlay pese a que el permiso parezca concedido. Evitamos
            // que esto tumbe el servicio y provoque un bucle de reinicios.
            composeView = null
            setupFailed = true
            stopSelf()
        }
    }

    private fun removeOverlayView() {
        composeView?.let {
            runCatching { windowManager.removeView(it) }
        }
        composeView = null
    }

    private fun buildForegroundNotification(): android.app.Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.OVERLAY_NOTIFICATION_CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, Constants.OVERLAY_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Isla activa")
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()
    }
}
