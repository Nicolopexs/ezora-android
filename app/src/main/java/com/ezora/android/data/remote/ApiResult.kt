package com.ezora.android.data.remote

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import retrofit2.Response
import java.io.IOException

/** Resultado de una llamada: éxito, error del servidor con su mensaje, o sin señal. */
sealed interface ApiResult<out T> {
    data class Ok<T>(val value: T) : ApiResult<T>
    data class Error(val message: String, val code: String? = null, val status: Int = 0) : ApiResult<Nothing>
    data object Offline : ApiResult<Nothing>
}

suspend fun <T> safeApiCall(json: Json, call: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = call()
        val body = response.body()
        if (response.isSuccessful && body != null) {
            ApiResult.Ok(body)
        } else {
            val raw = runCatching { response.errorBody()?.string() }.getOrNull().orEmpty()
            val parsed = runCatching { json.decodeFromString(ApiError.serializer(), raw) }.getOrNull()
            ApiResult.Error(
                message = parsed?.message?.takeIf { it.isNotBlank() } ?: defaultMessage(response.code()),
                code = parsed?.code,
                status = response.code(),
            )
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        ApiResult.Offline
    } catch (e: Exception) {
        ApiResult.Error("Ocurrió un error inesperado. Intenta de nuevo.")
    }
}

private fun defaultMessage(status: Int): String = when (status) {
    401 -> "Tu sesión expiró. Ingresa de nuevo."
    403 -> "No tienes permiso para esta acción."
    404 -> "No se encontró la información solicitada."
    422 -> "Revisa los datos ingresados."
    423 -> "Debes aceptar los términos y condiciones."
    428 -> "Debes cambiar tu contraseña para continuar."
    429 -> "Demasiados intentos. Espera un minuto."
    in 500..599 -> "El servidor no responde. Intenta en unos minutos."
    else -> "No se pudo completar la acción."
}
