package com.ezora.android.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** API v1 del servidor del cliente (Brand.apiBaseUrl). */
interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<SimpleResponse>

    @GET("me")
    suspend fun me(): Response<MeResponse>

    @POST("me/password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): Response<MeResponse>

    @POST("terms/accept")
    suspend fun acceptTerms(@Body body: TermsAcceptRequest): Response<SimpleResponse>

    @GET("sync")
    suspend fun sync(): Response<SyncResponse>
}
