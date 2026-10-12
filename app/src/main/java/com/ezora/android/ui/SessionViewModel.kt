package com.ezora.android.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ezora.android.data.remote.ApiResult
import com.ezora.android.data.remote.UserDto
import com.ezora.android.data.repo.AuthRepository
import com.ezora.android.data.repo.SyncRepository
import com.ezora.android.work.SyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** En qué paso está el visitador: sin sesión, cambio de clave obligatorio, términos o listo. */
sealed interface SessionState {
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data class NeedsPassword(val user: UserDto) : SessionState
    data class NeedsTerms(val user: UserDto) : SessionState
    data class Ready(val user: UserDto) : SessionState
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val app: Application,
    private val auth: AuthRepository,
    private val sync: SyncRepository,
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    init {
        viewModelScope.launch { restore() }
    }

    /** Arranca con lo guardado en el teléfono (funciona sin señal) y luego confirma con el servidor. */
    private suspend fun restore() {
        if (!auth.hasSession) {
            _state.value = SessionState.LoggedOut
            return
        }
        val cached = auth.cachedUser()
        _state.value = cached?.let(::stateFor) ?: SessionState.Loading
        when (val r = auth.refreshUser()) {
            is ApiResult.Ok -> _state.value = stateFor(r.value)
            is ApiResult.Error -> if (r.status == 401) onLoggedOut()
                else if (cached == null) _state.value = SessionState.LoggedOut
            ApiResult.Offline -> if (cached == null) _state.value = SessionState.LoggedOut
        }
    }

    fun onUserUpdated(user: UserDto) {
        val next = stateFor(user)
        val wasReady = _state.value is SessionState.Ready
        _state.value = next
        if (next is SessionState.Ready && !wasReady) {
            SyncWorker.schedulePeriodic(app)
            viewModelScope.launch { sync.sync() }
        }
    }

    fun logout() {
        viewModelScope.launch {
            SyncWorker.cancel(app)
            auth.logout()
            _state.value = SessionState.LoggedOut
        }
    }

    private fun onLoggedOut() {
        SyncWorker.cancel(app)
        _state.value = SessionState.LoggedOut
    }

    private fun stateFor(user: UserDto): SessionState = when {
        user.mustChangePassword -> SessionState.NeedsPassword(user)
        !user.terms.accepted -> SessionState.NeedsTerms(user)
        else -> SessionState.Ready(user)
    }
}
