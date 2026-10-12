package com.ezora.android.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.ezora.android.data.remote.ApiResult
import com.ezora.android.data.remote.UserDto
import com.ezora.android.data.repo.SyncRepository
import com.ezora.android.ui.components.ErrorBanner
import com.ezora.android.ui.components.SectionCard
import com.ezora.android.ui.components.StatusPill
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repo: SyncRepository) : ViewModel() {
    val medicos = repo.medicosCount
    val farmacias = repo.farmaciasCount
    val stock = repo.stockCount
    val lastSync = repo.lastSyncAt
    val ciclo = repo.ciclo
    val jornada = repo.jornada
    val planHoy = repo.planHoy

    var syncing by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    fun sync() {
        if (syncing) return
        syncing = true
        message = null
        viewModelScope.launch {
            message = when (val r = repo.sync()) {
                is ApiResult.Ok -> null
                is ApiResult.Error -> r.message
                ApiResult.Offline -> "Sin señal: trabajas con los datos guardados en el teléfono."
            }
            syncing = false
        }
    }
}

/** INI-01: resumen del día. La jornada y el GPS nativo llegan en la fase 1. */
@Composable
fun HomeScreen(user: UserDto, modifier: Modifier = Modifier, vm: HomeViewModel = hiltViewModel()) {
    val medicos by vm.medicos.collectAsStateWithLifecycle(0)
    val farmacias by vm.farmacias.collectAsStateWithLifecycle(0)
    val stock by vm.stock.collectAsStateWithLifecycle(0)
    val lastSync by vm.lastSync.collectAsStateWithLifecycle(null)
    val ciclo by vm.ciclo.collectAsStateWithLifecycle(null)
    val jornada by vm.jornada.collectAsStateWithLifecycle(null)
    val plan by vm.planHoy.collectAsStateWithLifecycle(emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(greeting() + ", " + (user.name ?: "visitador"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        ciclo?.let { c ->
            Text(
                listOfNotNull(c.nombre, rangeText(c.fechaInicio, c.fechaFin)).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Jornada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                val j = jornada
                when {
                    j == null -> StatusPill("Sin datos", MaterialTheme.colorScheme.outline)
                    j.jornada?.lunchActive == true -> StatusPill("En almuerzo", Color(0xFFF59E0B))
                    j.active -> StatusPill("Activa", MaterialTheme.colorScheme.tertiary)
                    j.hasFinishedToday -> StatusPill("Finalizada", MaterialTheme.colorScheme.primary)
                    else -> StatusPill("Sin iniciar", MaterialTheme.colorScheme.outline)
                }
            }
            Text(
                "Iniciar jornada, almuerzo y GPS nativo: próxima entrega.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        SectionCard {
            Text("Hoy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                if (plan.isEmpty()) "No tienes visitas planificadas para hoy." else "${plan.size} visitas planificadas para hoy.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        SectionCard {
            Text("Guardado en el teléfono", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Stat("Médicos", medicos, Modifier.weight(1f))
                Stat("Farmacias", farmacias, Modifier.weight(1f))
                Stat("Productos", stock, Modifier.weight(1f))
            }
            Text(
                "Última actualización: " + (lastSync?.let(::formatSync) ?: "nunca"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp),
            )
            OutlinedButton(onClick = vm::sync, enabled = !vm.syncing, modifier = Modifier.padding(top = 8.dp)) {
                Icon(Icons.Outlined.Sync, contentDescription = null)
                Text(if (vm.syncing) "  Actualizando…" else "  Actualizar datos")
            }
        }
        ErrorBanner(vm.message)
    }
}

@Composable
private fun Stat(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun greeting(): String {
    val h = LocalTime.now().hour
    return when {
        h < 12 -> "Buenos días"
        h < 19 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}

private fun rangeText(from: String?, to: String?): String? {
    if (from.isNullOrBlank() || to.isNullOrBlank()) return null
    return "${from.take(10)} al ${to.take(10)}"
}

private fun formatSync(raw: String): String = runCatching {
    OffsetDateTime.parse(raw).atZoneSameInstant(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))
}.getOrDefault(raw)
