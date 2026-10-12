package com.ezora.android.data.remote

import com.ezora.android.core.Brand
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/** Agrega el token, pide JSON e identifica la app y el teléfono en cada llamada. */
@Singleton
class AuthInterceptor @Inject constructor(private val tokenStore: TokenStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
            .header("Accept", "application/json")
            .header("User-Agent", Brand.userAgent)
        tokenStore.token?.let { builder.header("Authorization", "Bearer $it") }
        return chain.proceed(builder.build())
    }
}
