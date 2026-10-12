package com.ezora.android.core

import android.os.Build
import com.ezora.android.BuildConfig

/** Configuración del sabor (cliente) con el que se compiló la app: cassara o ezora. */
object Brand {
    const val CASSARA = "cassara"
    const val EZORA = "ezora"

    val id: String = BuildConfig.BRAND
    val apiBaseUrl: String = BuildConfig.API_BASE_URL
    val termsUrl: String = BuildConfig.TERMS_URL
    val isCassara: Boolean get() = id == CASSARA

    val displayName: String get() = if (isCassara) "Cassará" else "EZORA"

    val userAgent: String
        get() = "EzoraAndroid/${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}; $id; Android ${Build.VERSION.RELEASE}; ${Build.MANUFACTURER} ${Build.MODEL})"

    val deviceName: String
        get() = "${Build.MANUFACTURER} ${Build.MODEL} · Android ${Build.VERSION.RELEASE}".take(120)
}
