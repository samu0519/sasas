package com.dynamicisland.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onRequestOverlayPermission: () -> Unit,
    onRequestNotificationAccess: () -> Unit,
    onRequestBatteryExemption: () -> Unit,
    overlayGranted: Boolean,
    notificationAccessGranted: Boolean,
    batteryExempted: Boolean
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Dynamic Island") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                SectionTitle("Permisos")
            }
            item {
                PermissionRow(
                    label = "Superposición de pantalla",
                    granted = overlayGranted,
                    onClick = onRequestOverlayPermission
                )
            }
            item {
                PermissionRow(
                    label = "Acceso a notificaciones",
                    granted = notificationAccessGranted,
                    onClick = onRequestNotificationAccess
                )
            }
            item {
                PermissionRow(
                    label = "Ignorar optimización de batería",
                    granted = batteryExempted,
                    onClick = onRequestBatteryExemption
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { SectionTitle("Apariencia") }

            item {
                LabeledSlider(
                    label = "Ancho colapsado",
                    value = settings.collapsedWidthDp,
                    range = 80f..200f,
                    unit = "dp",
                    onValueChange = { viewModel.updateCollapsedSize(it, settings.collapsedHeightDp) }
                )
            }
            item {
                LabeledSlider(
                    label = "Alto colapsado",
                    value = settings.collapsedHeightDp,
                    range = 24f..56f,
                    unit = "dp",
                    onValueChange = { viewModel.updateCollapsedSize(settings.collapsedWidthDp, it) }
                )
            }
            item {
                LabeledSlider(
                    label = "Ancho expandido",
                    value = settings.expandedWidthDp,
                    range = 220f..420f,
                    unit = "dp",
                    onValueChange = { viewModel.updateExpandedSize(it, settings.expandedHeightDp) }
                )
            }
            item {
                LabeledSlider(
                    label = "Alto expandido",
                    value = settings.expandedHeightDp,
                    range = 72f..160f,
                    unit = "dp",
                    onValueChange = { viewModel.updateExpandedSize(settings.expandedWidthDp, it) }
                )
            }
            item {
                LabeledSlider(
                    label = "Radio de bordes",
                    value = settings.cornerRadiusDp,
                    range = 8f..40f,
                    unit = "dp",
                    onValueChange = { viewModel.updateCornerRadius(it) }
                )
            }
            item {
                LabeledSlider(
                    label = "Posición vertical",
                    value = settings.topOffsetDp,
                    range = 0f..60f,
                    unit = "dp",
                    onValueChange = { viewModel.updateTopOffset(it) }
                )
            }
            item {
                LabeledSlider(
                    label = "Transparencia del fondo",
                    value = settings.backgroundAlpha,
                    range = 0.5f..1f,
                    unit = "",
                    onValueChange = { viewModel.updateBackgroundAlpha(it) }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { SectionTitle("Animación y tiempos") }

            item {
                LabeledSlider(
                    label = "Velocidad de animación",
                    value = settings.animationSpeed,
                    range = 0.5f..2f,
                    unit = "x",
                    onValueChange = { viewModel.updateAnimationSpeed(it) }
                )
            }
            item {
                LabeledSlider(
                    label = "Duración visible",
                    value = settings.autoDismissMillis / 1000f,
                    range = 1f..10f,
                    unit = "s",
                    onValueChange = { viewModel.updateAutoDismiss((it * 1000).toLong()) }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { SectionTitle("Apps que muestran notificaciones") }

            items(viewModel.installedApps) { (packageName, appName) ->
                AppToggleRow(
                    appName = appName,
                    checked = packageName in settings.enabledPackages,
                    onCheckedChange = { viewModel.toggleApp(packageName, it) }
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun PermissionRow(label: String, granted: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = granted, onCheckedChange = { if (!granted) onClick() })
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = "$label: ${"%.1f".format(value)}$unit",
            style = MaterialTheme.typography.bodyMedium
        )
        Slider(value = value, onValueChange = onValueChange, valueRange = range)
    }
}

@Composable
private fun AppToggleRow(appName: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = appName, style = MaterialTheme.typography.bodyLarge)
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
    Divider()
}
