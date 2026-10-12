package com.ezora.android.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/* Contrato de la API v1 (documento maestro, sección "API necesaria"). Todo con valores por defecto:
   si el servidor agrega o quita campos, la app no se rompe. */

@Serializable
data class ApiError(
    val message: String? = null,
    val code: String? = null,
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
    @SerialName("device_name") val deviceName: String,
)

@Serializable
data class LoginResponse(
    val ok: Boolean = false,
    val token: String? = null,
    val user: UserDto? = null,
    val message: String? = null,
)

@Serializable
data class MeResponse(
    val ok: Boolean = false,
    val user: UserDto? = null,
    val message: String? = null,
)

@Serializable
data class SimpleResponse(
    val ok: Boolean = false,
    val message: String? = null,
    val code: String? = null,
)

@Serializable
data class ChangePasswordRequest(
    val password: String,
    @SerialName("password_confirmation") val passwordConfirmation: String,
    @SerialName("current_password") val currentPassword: String? = null,
)

@Serializable
data class TermsAcceptRequest(
    val lat: Double? = null,
    val lng: Double? = null,
    val accuracy: Double? = null,
    val device: String? = null,
)

@Serializable
data class UserDto(
    val id: Long = 0,
    val name: String? = null,
    val apellido: String? = null,
    val username: String? = null,
    val email: String? = null,
    val telefono: String? = null,
    @SerialName("cedula_ruc") val cedulaRuc: String? = null,
    @SerialName("foto_url") val fotoUrl: String? = null,
    val area: String? = null,
    val role: String? = null,
    @SerialName("tracking_enabled") val trackingEnabled: Boolean = false,
    @SerialName("weekend_tracking_enabled") val weekendTrackingEnabled: Boolean = false,
    @SerialName("must_change_password") val mustChangePassword: Boolean = false,
    val terms: TermsDto = TermsDto(),
    val permissions: PermissionsDto = PermissionsDto(),
) {
    val fullName: String get() = listOfNotNull(name, apellido).joinToString(" ").trim()
}

@Serializable
data class TermsDto(
    val version: String = "",
    val accepted: Boolean = true,
)

@Serializable
data class PermissionsDto(
    @SerialName("can_delete_visitas") val canDeleteVisitas: Boolean = false,
)

@Serializable
data class SyncResponse(
    val ok: Boolean = false,
    @SerialName("synced_at") val syncedAt: String? = null,
    val user: UserDto? = null,
    val settings: SettingsDto = SettingsDto(),
    val ciclo: CicloDto? = null,
    val catalogos: CatalogosDto = CatalogosDto(),
    val medicos: List<MedicoDto> = emptyList(),
    val farmacias: List<FarmaciaDto> = emptyList(),
    val stock: List<StockDto> = emptyList(),
    @SerialName("plan_hoy") val planHoy: List<PlanDto> = emptyList(),
    val jornada: JornadaStateDto = JornadaStateDto(),
)

@Serializable
data class SettingsDto(
    @SerialName("enforce_daily_plan") val enforceDailyPlan: Boolean = false,
    @SerialName("require_directory_approval") val requireDirectoryApproval: Boolean = true,
    @SerialName("alert_distance_meters") val alertDistanceMeters: Double = 200.0,
)

@Serializable
data class CicloDto(
    val id: Long = 0,
    val nombre: String? = null,
    @SerialName("fecha_inicio") val fechaInicio: String? = null,
    @SerialName("fecha_fin") val fechaFin: String? = null,
    val semanas: Int = 5,
    @SerialName("dias_habiles") val diasHabiles: List<String> = emptyList(),
)

@Serializable
data class CatalogoItemDto(
    val id: Long = 0,
    val nombre: String = "",
    @SerialName("provincia_id") val provinciaId: Long? = null,
)

@Serializable
data class CatalogosDto(
    val especialidades: List<CatalogoItemDto> = emptyList(),
    val lineas: List<CatalogoItemDto> = emptyList(),
    val provincias: List<CatalogoItemDto> = emptyList(),
    val ciudades: List<CatalogoItemDto> = emptyList(),
    @SerialName("tipos_gasto") val tiposGasto: List<String> = emptyList(),
)

@Serializable
data class HorarioDto(
    val dia: String? = null,
    @SerialName("hora_inicio") val horaInicio: String? = null,
    @SerialName("hora_fin") val horaFin: String? = null,
)

@Serializable
data class MedicoDto(
    val id: Long = 0,
    val nombre: String? = null,
    val apellido: String? = null,
    @SerialName("cedula_ruc") val cedulaRuc: String? = null,
    @SerialName("especialidad_id") val especialidadId: Long? = null,
    val especialidad: String? = null,
    @SerialName("linea_id") val lineaId: Long? = null,
    val linea: String? = null,
    @SerialName("tipo_contacto") val tipoContacto: String = "normal",
    val telefono: String? = null,
    val email: String? = null,
    val consultorio: String? = null,
    val direccion: String? = null,
    val direccion2: String? = null,
    val direccion3: String? = null,
    @SerialName("provincia_id") val provinciaId: Long? = null,
    val provincia: String? = null,
    @SerialName("ciudad_id") val ciudadId: Long? = null,
    val ciudad: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    @SerialName("cumple_dia") val cumpleDia: Int? = null,
    @SerialName("cumple_mes") val cumpleMes: Int? = null,
    val aprobado: Boolean = true,
    @SerialName("creado_por_mi") val creadoPorMi: Boolean = false,
    val horarios: List<HorarioDto> = emptyList(),
)

@Serializable
data class FarmaciaDto(
    val id: Long = 0,
    val nombre: String? = null,
    val establecimiento: String? = null,
    val direccion: String? = null,
    val direccion2: String? = null,
    val direccion3: String? = null,
    @SerialName("provincia_id") val provinciaId: Long? = null,
    val provincia: String? = null,
    @SerialName("ciudad_id") val ciudadId: Long? = null,
    val ciudad: String? = null,
    val telefono: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val aprobado: Boolean = true,
    @SerialName("creado_por_mi") val creadoPorMi: Boolean = false,
)

@Serializable
data class StockDto(
    val id: Long = 0,
    val nombre: String = "",
    val lote: String = "",
    val vence: String? = null,
    val cantidad: Int? = null,
    val categoria: String = "otros",
)

@Serializable
data class PlanDto(
    val id: Long = 0,
    val tipo: String = "medico",
    @SerialName("medico_id") val medicoId: Long? = null,
    @SerialName("farmacia_id") val farmaciaId: Long? = null,
    val fecha: String? = null,
    @SerialName("hora_inicio") val horaInicio: String? = null,
    @SerialName("hora_fin") val horaFin: String? = null,
    @SerialName("no_products_planned") val noProductsPlanned: Boolean = false,
)

@Serializable
data class JornadaStateDto(
    val active: Boolean = false,
    @SerialName("has_finished_today") val hasFinishedToday: Boolean = false,
    val jornada: JornadaDto? = null,
)

@Serializable
data class JornadaDto(
    val id: Long = 0,
    @SerialName("session_id") val sessionId: String? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("lunch_active") val lunchActive: Boolean = false,
    @SerialName("lunch_accumulated_seconds") val lunchAccumulatedSeconds: Long = 0,
)
