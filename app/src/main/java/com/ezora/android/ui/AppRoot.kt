package com.ezora.android.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ezora.android.ui.auth.ForcePasswordScreen
import com.ezora.android.ui.auth.LoginScreen
import com.ezora.android.ui.auth.TermsScreen
import com.ezora.android.ui.main.MainScaffold
import com.ezora.android.ui.permissions.LocationGate

/**
 * Puerta de entrada: decide qué pantalla ve el visitador.
 * Orden igual que la web: login → cambio de clave → términos → permisos de ubicación → app.
 */
@Composable
fun AppRoot(session: SessionViewModel = hiltViewModel()) {
    val state by session.state.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        AnimatedContent(
            targetState = state,
            contentKey = { it::class },
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "session",
        ) { s ->
            when (s) {
                SessionState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                SessionState.LoggedOut -> LoginScreen(onLoggedIn = session::onUserUpdated)
                is SessionState.NeedsPassword -> ForcePasswordScreen(
                    onChanged = session::onUserUpdated,
                    onLogout = session::logout,
                )
                is SessionState.NeedsTerms -> TermsScreen(
                    onAccepted = session::onUserUpdated,
                    onLogout = session::logout,
                )
                is SessionState.Ready -> LocationGate {
                    MainScaffold(user = s.user, onLogout = session::logout)
                }
            }
        }
    }
}
