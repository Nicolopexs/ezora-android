package com.ezora.android.ui.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ezora.android.ui.components.PrimaryButton
import com.ezora.android.ui.components.SectionCard

/** Estado de los permisos que necesita el rastreo de jornada. */
data class PermissionSnapshot(
    val fine: Boolean,
    val background: Boolean,
    val notifications: Boolean,
) {
    val allGranted: Boolean get() = fine && background && notifications

    companion object {
        fun read(context: Context): PermissionSnapshot {
            fun has(p: String) = ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED
            val fine = has(Manifest.permission.ACCESS_FINE_LOCATION)
            val background = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                has(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else fine
            val notifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                has(Manifest.permission.POST_NOTIFICATIONS)
            } else true
            return PermissionSnapshot(fine, background, notifications)
        }
    }
}

/**
 * GPS-01 / INI-02: sin "Permitir todo el tiempo" no se entra a la app, porque el recorrido
 * se perdería con la pantalla apagada. Se vuelve a revisar cada vez que el visitador regresa de Ajustes.
 */
@Composable
fun LocationGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var snapshot by remember { mutableStateOf(PermissionSnapshot.read(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) snapshot = PermissionSnapshot.read(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (snapshot.allGranted) {
        content()
    } else {
        PermissionScreen(snapshot, onChanged = { snapshot = PermissionSnapshot.read(context) })
    }
}

@Composable
private fun PermissionScreen(snapshot: PermissionSnapshot, onChanged: () -> Unit) {
    val context = LocalContext.current
    var deniedOnce by remember { mutableStateOf(false) }

    val foregroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        if (result.values.any { !it }) deniedOnce = true
        onChanged()
    }
    val backgroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (!granted) deniedOnce = true
        onChanged()
    }
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { onChanged() }

    fun openAppSettings() {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    val (buttonText, action) = when {
        !snapshot.fine -> "Permitir ubicación" to {
            if (deniedOnce) openAppSettings() else foregroundLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            )
        }
        !snapshot.background -> "Elegir \"Permitir todo el tiempo\"" to {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !deniedOnce) {
                backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else openAppSettings()
        }
        else -> "Permitir notificaciones" to {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Text("Activa los permisos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "La app registra tu recorrido solo durante la jornada. Para que no se pierda con la pantalla apagada, " +
                "la ubicación debe estar en \"Permitir todo el tiempo\".",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                PermissionRow(Icons.Outlined.LocationOn, "Ubicación precisa", snapshot.fine)
                PermissionRow(Icons.Outlined.LocationOn, "Permitir todo el tiempo", snapshot.background)
                PermissionRow(Icons.Outlined.Notifications, "Notificaciones (aviso de jornada activa)", snapshot.notifications)
            }
        }
        if (snapshot.fine && !snapshot.background) {
            Text(
                "En la siguiente pantalla toca Permisos › Ubicación › Permitir todo el tiempo, y regresa a la app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        PrimaryButton(buttonText, onClick = action)
        if (deniedOnce) {
            TextButton(onClick = ::openAppSettings, modifier = Modifier.fillMaxWidth()) { Text("Abrir ajustes de la app") }
        }
    }
}

@Composable
private fun PermissionRow(icon: ImageVector, label: String, granted: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Icon(
            if (granted) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = if (granted) "Concedido" else "Pendiente",
            tint = if (granted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(22.dp),
        )
    }
}
