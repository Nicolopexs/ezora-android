package com.ezora.android.data.repo

import com.ezora.android.data.local.FarmaciaDao
import com.ezora.android.data.local.FarmaciaEntity
import com.ezora.android.data.local.KvDao
import com.ezora.android.data.local.KvEntity
import com.ezora.android.data.local.MedicoDao
import com.ezora.android.data.local.MedicoEntity
import com.ezora.android.data.local.StockDao
import com.ezora.android.data.local.StockEntity
import com.ezora.android.data.remote.ApiResult
import com.ezora.android.data.remote.ApiService
import com.ezora.android.data.remote.CatalogosDto
import com.ezora.android.data.remote.CicloDto
import com.ezora.android.data.remote.HorarioDto
import com.ezora.android.data.remote.JornadaStateDto
import com.ezora.android.data.remote.PlanDto
import com.ezora.android.data.remote.SettingsDto
import com.ezora.android.data.remote.SyncResponse
import com.ezora.android.data.remote.UserDto
import com.ezora.android.data.remote.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/** Descarga todo lo necesario para trabajar sin señal y lo guarda en el teléfono (OFF-01). */
@Singleton
class SyncRepository @Inject constructor(
    private val api: ApiService,
    private val medicoDao: MedicoDao,
    private val farmaciaDao: FarmaciaDao,
    private val stockDao: StockDao,
    private val kv: KvDao,
    private val json: Json,
) {
    val medicosCount: Flow<Int> = medicoDao.observeCount()
    val farmaciasCount: Flow<Int> = farmaciaDao.observeCount()
    val stockCount: Flow<Int> = stockDao.observeCount()
    val lastSyncAt: Flow<String?> = kv.observe(KEY_SYNCED_AT)
    val ciclo: Flow<CicloDto?> = kv.observe(KEY_CICLO).map { raw ->
        raw?.let { runCatching { json.decodeFromString(CicloDto.serializer(), it) }.getOrNull() }
    }
    val jornada: Flow<JornadaStateDto?> = kv.observe(KEY_JORNADA).map { raw ->
        raw?.let { runCatching { json.decodeFromString(JornadaStateDto.serializer(), it) }.getOrNull() }
    }
    val planHoy: Flow<List<PlanDto>> = kv.observe(KEY_PLAN_HOY).map { raw ->
        raw?.let { runCatching { json.decodeFromString(ListSerializer(PlanDto.serializer()), it) }.getOrNull() }
            ?: emptyList()
    }

    suspend fun sync(): ApiResult<SyncResponse> {
        val result = safeApiCall(json) { api.sync() }
        if (result is ApiResult.Ok) save(result.value)
        return result
    }

    private suspend fun save(data: SyncResponse) {
        medicoDao.replaceAll(data.medicos.map { m ->
            MedicoEntity(
                id = m.id,
                nombre = m.nombre.orEmpty(),
                apellido = m.apellido.orEmpty(),
                especialidadId = m.especialidadId,
                especialidad = m.especialidad,
                lineaId = m.lineaId,
                linea = m.linea,
                tipoContacto = m.tipoContacto,
                telefono = m.telefono,
                email = m.email,
                consultorio = m.consultorio,
                direccion = m.direccion,
                provinciaId = m.provinciaId,
                provincia = m.provincia,
                ciudadId = m.ciudadId,
                ciudad = m.ciudad,
                lat = m.lat,
                lng = m.lng,
                cumpleDia = m.cumpleDia,
                cumpleMes = m.cumpleMes,
                aprobado = m.aprobado,
                creadoPorMi = m.creadoPorMi,
                horariosJson = json.encodeToString(ListSerializer(HorarioDto.serializer()), m.horarios),
            )
        })
        farmaciaDao.replaceAll(data.farmacias.map { f ->
            FarmaciaEntity(
                id = f.id,
                nombre = f.nombre.orEmpty(),
                establecimiento = f.establecimiento,
                direccion = f.direccion,
                provinciaId = f.provinciaId,
                provincia = f.provincia,
                ciudadId = f.ciudadId,
                ciudad = f.ciudad,
                telefono = f.telefono,
                lat = f.lat,
                lng = f.lng,
                aprobado = f.aprobado,
                creadoPorMi = f.creadoPorMi,
            )
        })
        stockDao.replaceAll(data.stock.map { s ->
            StockEntity(s.id, s.nombre, s.lote, s.vence, s.cantidad, s.categoria)
        })

        data.user?.let { put(AuthRepository.KEY_USER, json.encodeToString(UserDto.serializer(), it)) }
        put(KEY_SETTINGS, json.encodeToString(SettingsDto.serializer(), data.settings))
        put(KEY_CATALOGOS, json.encodeToString(CatalogosDto.serializer(), data.catalogos))
        put(KEY_PLAN_HOY, json.encodeToString(ListSerializer(PlanDto.serializer()), data.planHoy))
        put(KEY_JORNADA, json.encodeToString(JornadaStateDto.serializer(), data.jornada))
        data.ciclo?.let { put(KEY_CICLO, json.encodeToString(CicloDto.serializer(), it)) } ?: kv.delete(KEY_CICLO)
        data.syncedAt?.let { put(KEY_SYNCED_AT, it) }
    }

    private suspend fun put(key: String, value: String) = kv.put(KvEntity(key, value))

    companion object {
        const val KEY_SETTINGS = "settings"
        const val KEY_CATALOGOS = "catalogos"
        const val KEY_CICLO = "ciclo"
        const val KEY_PLAN_HOY = "plan_hoy"
        const val KEY_JORNADA = "jornada"
        const val KEY_SYNCED_AT = "synced_at"
    }
}
