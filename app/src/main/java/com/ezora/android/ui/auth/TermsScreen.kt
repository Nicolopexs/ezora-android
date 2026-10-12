package com.ezora.android.ui.auth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ezora.android.core.Brand
import com.ezora.android.data.remote.ApiResult
import com.ezora.android.data.remote.UserDto
import com.ezora.android.data.repo.AuthRepository
import com.ezora.android.ui.components.ErrorBanner
import com.ezora.android.ui.components.PrimaryButton
import com.ezora.android.ui.components.SectionCard
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class TermsViewModel @Inject constructor(
    private val app: Application,
    private val auth: AuthRepository,
) : AndroidViewModel(app) {
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun accept(onDone: (UserDto) -> Unit) {
        if (loading) return
        loading = true
        error = null
        viewModelScope.launch {
            // Igual que la web: se adjunta la ubicación si ya hay permiso; si no, se acepta sin ella.
            val loc = lastLocation()
            when (val r = auth.acceptTerms(loc?.latitude, loc?.longitude, loc?.accuracy?.toDouble())) {
                is ApiResult.Ok -> onDone(r.value)
                is ApiResult.Error -> error = r.message
                ApiResult.Offline -> error = "Sin conexión."
            }
            loading = false
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun lastLocation(): android.location.Location? {
        val granted = ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) return null
        return runCatching {
            withTimeoutOrNull(3_000) { LocationServices.getFusedLocationProviderClient(app).lastLocation.await() }
        }.getOrNull()
    }
}

/** AUTH-07: términos, protección de datos y uso de geolocalización (mismo texto que la web). */
@Composable
fun TermsScreen(
    onAccepted: (UserDto) -> Unit,
    onLogout: () -> Unit,
    vm: TermsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Términos y condiciones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        SectionCard {
            Text(
                "Protección de datos y uso de geolocalización en jornada laboral",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            val body = MaterialTheme.typography.bodyMedium
            val muted = MaterialTheme.colorScheme.onSurfaceVariant
            Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Para continuar usando la app debes aceptar los términos y condiciones y la política de protección de datos. " +
                        "Esta plataforma utiliza geolocalización durante la jornada laboral para registrar recorridos, validar visitas, " +
                        "generar reportes operativos y respaldar la trazabilidad de actividades de campo.",
                    style = body, color = muted,
                )
                Text(
                    "Al aceptar, confirmas que conoces y autorizas este tratamiento de datos dentro del marco de tu actividad laboral. " +
                        "También confirmas que es tu responsabilidad mantener activa la geolocalización al iniciar jornada, desactivarla " +
                        "al finalizar jornada y gestionar correctamente su estado durante el inicio y fin de almuerzo, según el flujo de la app.",
                    style = body, color = muted,
                )
                Text(
                    "Estos datos se usarán con fines de control operativo, seguridad, auditoría interna y cumplimiento de procesos " +
                        "comerciales. Para más detalle sobre el tratamiento de datos, tiempos de conservación y derechos del titular, " +
                        "revisa el documento completo.",
                    style = body, color = muted,
                )
            }
        }
        ErrorBanner(vm.error)
        OutlinedButton(
            onClick = {
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Brand.termsUrl))) }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Leer más") }
        PrimaryButton("Aceptar y continuar", onClick = { vm.accept(onAccepted) }, loading = vm.loading)
        TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
    }
}
