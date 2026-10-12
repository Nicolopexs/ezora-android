package com.ezora.android

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.ezora.android.data.remote.TokenStore
import com.ezora.android.work.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class EzoraApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var tokenStore: TokenStore

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Con sesión iniciada, la sincronización periódica sigue aunque la app esté cerrada.
        if (tokenStore.token != null) {
            SyncWorker.schedulePeriodic(this)
        }
    }
}
