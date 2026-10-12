package com.ezora.android.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezora.android.data.remote.ApiResult
import com.ezora.android.data.remote.UserDto
import com.ezora.android.data.repo.AuthRepository
import com.ezora.android.ui.components.BrandMark
import com.ezora.android.ui.components.ErrorBanner
import com.ezora.android.ui.components.PasswordField
import com.ezora.android.ui.components.PrimaryButton
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(private val auth: AuthRepository) : ViewModel() {
    var password by mutableStateOf("")
    var confirmation by mutableStateOf("")
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun submit(onDone: (UserDto) -> Unit) {
        if (loading) return
        error = when {
            password.length < 8 -> "La contraseña debe tener al menos 8 caracteres."
            password != confirmation -> "Las contraseñas no coinciden."
            else -> null
        }
        if (error != null) return
        loading = true
        viewModelScope.launch {
            when (val r = auth.changePassword(password, confirmation, current = null)) {
                is ApiResult.Ok -> onDone(r.value)
                is ApiResult.Error -> error = r.message
                ApiResult.Offline -> error = "Sin conexión."
            }
            loading = false
        }
    }
}

/** AUTH-05: cambio de contraseña obligatorio en el primer ingreso. */
@Composable
fun ForcePasswordScreen(
    onChanged: (UserDto) -> Unit,
    onLogout: () -> Unit,
    vm: ChangePasswordViewModel = hiltViewModel(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        BrandMark(56)
        Text("Crea tu nueva contraseña", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "Por seguridad debes cambiar la contraseña temporal antes de continuar. Usa al menos 8 caracteres.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        PasswordField(vm.password, { vm.password = it }, "Nueva contraseña")
        PasswordField(
            vm.confirmation, { vm.confirmation = it }, "Repite la contraseña",
            imeAction = ImeAction.Done, onDone = { vm.submit(onChanged) },
        )
        ErrorBanner(vm.error)
        PrimaryButton("Guardar y continuar", onClick = { vm.submit(onChanged) }, loading = vm.loading)
        TextButton(onClick = onLogout) { Text("Cerrar sesión") }
    }
}
