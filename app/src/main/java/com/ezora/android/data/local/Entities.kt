package com.ezora.android.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicos")
data class MedicoEntity(
    @PrimaryKey val id: Long,
    val nombre: String,
    val apellido: String,
    val especialidadId: Long?,
    val especialidad: String?,
    val lineaId: Long?,
    val linea: String?,
    val tipoContacto: String,
    val telefono: String?,
    val email: String?,
    val consultorio: String?,
    val direccion: String?,
    val provinciaId: Long?,
    val provincia: String?,
    val ciudadId: Long?,
    val ciudad: String?,
    val lat: Double?,
    val lng: Double?,
    val cumpleDia: Int?,
    val cumpleMes: Int?,
    val aprobado: Boolean,
    val creadoPorMi: Boolean,
    /** Horarios de atención (lista JSON tal como llega del servidor). */
    val horariosJson: String,
)

@Entity(tableName = "farmacias")
data class FarmaciaEntity(
    @PrimaryKey val id: Long,
    val nombre: String,
    val establecimiento: String?,
    val direccion: String?,
    val provinciaId: Long?,
    val provincia: String?,
    val ciudadId: Long?,
    val ciudad: String?,
    val telefono: String?,
    val lat: Double?,
    val lng: Double?,
    val aprobado: Boolean,
    val creadoPorMi: Boolean,
)

@Entity(tableName = "stock")
data class StockEntity(
    @PrimaryKey val id: Long,
    val nombre: String,
    val lote: String,
    val vence: String?,
    val cantidad: Int?,
    val categoria: String,
)

/** Datos sueltos que llegan en la sincronización (usuario, ciclo, catálogos, ajustes) como JSON. */
@Entity(tableName = "kv")
data class KvEntity(
    @PrimaryKey @ColumnInfo(name = "k") val key: String,
    @ColumnInfo(name = "v") val value: String,
)
