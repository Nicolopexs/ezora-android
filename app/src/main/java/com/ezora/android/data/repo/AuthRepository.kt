package com.ezora.android.data.repo

import com.ezora.android.core.Brand
import com.ezora.android.data.local.AppDatabase
import com.ezora.android.data.local.KvDao
import com.ezora.android.data.local.KvEntity
import com.ezora.android.data.remote.ApiResult
import com.ezora.android.data.remote.ApiService
import com.ezora.android.data.remote.ChangePasswordRequest
import com.ezora.android.data.remote.LoginRequest
import com.ezora.android.data.remote.TermsAcceptRequest
import com.ezora.android.data.remote.TokenStore
import com.ezora.android.data.remote.UserDto
import com.ezora.android.data.remote.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/** Inicio y cierre de sesión, usuario actual, contraseña y términos (AUTH-01 a AUTH-11). */
@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore,
    private val kv: KvDao,
    private val db: AppDatabase,
    private val json: Json,
) {
    val hasSession: Boolean get() = tokenStore.token != null

    suspend fun login(username: String, password: String): ApiResult<UserDto> {
        val result = safeApiCall(json) { api.login(LoginRequest(username.trim(), password, Brand.deviceName)) }
        return when (result) {
            is ApiResult.Ok -> {
                val token = result.value.token
                val user = result.value.user
                if (token.isNullOrBlank() || user == null) {
                    ApiResult.Error(result.value.message ?: "No se pudo iniciar sesión.")
                } else {
                    tokenStore.token = token
                    cacheUser(user)
                    ApiResult.Ok(user)
                }
            }
            is ApiResult.Error -> result
            ApiResult.Offline -> ApiResult.Error("Sin conexión. Para el primer ingreso necesitas internet.")
        }
    }

    /** Usuario guardado en el teléfono (sirve sin señal). */
    suspend fun cachedUser(): UserDto? = kv.get(KEY_USER)?.let {
        runCatching { json.decodeFromString(UserDto.serializer(), it) }.getOrNull()
    }

    /** Pide el usuario al servidor; si el token ya no sirve (401) cierra la sesión local. */
    suspend fun refreshUser(): ApiResult<UserDto> {
        return when (val result = safeApiCall(json) { api.me() }) {
            is ApiResult.Ok -> result.value.user?.let { cacheUser(it); ApiResult.Ok(it) }
                ?: ApiResult.Error("Respuesta inválida del servidor.")
            is ApiResult.Error -> {
                if (result.status == 401) clearLocalSession()
                result
            }
            ApiResult.Offline -> ApiResult.Offline
        }
    }

    suspend fun changePassword(newPassword: String, confirmation: String, current: String?): ApiResult<UserDto> {
        val result = safeApiCall(json) {
            api.changePassword(ChangePasswordRequest(newPassword, confirmation, current?.takeIf { it.isNotBlank() }))
        }
        return when (result) {
            is ApiResult.Ok -> result.value.user?.let { cacheUser(it); ApiResult.Ok(it) }
                ?: ApiResult.Error("Respuesta inválida del servidor.")
            is ApiResult.Error -> result
            ApiResult.Offline -> ApiResult.Error("Sin conexión. Intenta cuando tengas señal.")
        }
    }

    suspend fun acceptTerms(lat: Double?, lng: Double?, accuracy: Double?): ApiResult<UserDto> {
        val result = safeApiCall(json) {
            api.acceptTerms(TermsAcceptRequest(lat, lng, accuracy, Brand.deviceName))
        }
        return when (result) {
            is ApiResult.Ok -> refreshUser()
            is ApiResult.Error -> result
            ApiResult.Offline -> ApiResult.Error("Sin conexión. Aceptar los términos requiere internet.")
        }
    }

    suspend fun logout() {
        if (tokenStore.token != null) {
            safeApiCall(json) { api.logout() }
        }
        clearLocalSession()
    }

    private suspend fun clearLocalSession() {
        tokenStore.token = null
        withContext(Dispatchers.IO) { db.clearAllTables() }
    }

    private suspend fun cacheUser(user: UserDto) {
        kv.put(KvEntity(KEY_USER, json.encodeToString(UserDto.serializer(), user)))
    }

    companion object {
        const val KEY_USER = "user"
    }
}
