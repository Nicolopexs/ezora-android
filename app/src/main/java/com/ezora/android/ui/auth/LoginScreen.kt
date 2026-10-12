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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezora.android.BuildConfig
import com.ezora.android.core.Brand
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
class LoginViewModel @Inject constructor(private val auth: AuthRepository) : ViewModel() {
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun submit(onSuccess: (UserDto) -> Unit) {
        if (loading) return
        if (username.isBlank() || password.isBlank()) {
            error = "Ingresa tu usuario y contraseña."
            return
        }
        loading = true
        error = null
        viewModelScope.launch {
            when (val r = auth.login(username, password)) {
                is ApiResult.Ok -> onSuccess(r.value)
                is ApiResult.Error -> error = r.message
                ApiResult.Offline -> error = "Sin conexión."
            }
            loading = false
        }
    }
}

/** AUTH-01: ingreso con usuario o correo. */
@Composable
fun LoginScreen(onLoggedIn: (UserDto) -> Unit, vm: LoginViewModel = hiltViewModel()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Spacer(Modifier.height(32.dp))
        BrandMark()
        Text(Brand.displayName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Ingresa con tu usuario de visitador",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = vm.username,
            onValueChange = { vm.username = it },
            label = { Text("Usuario o correo") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        PasswordField(
            value = vm.password,
            onValueChange = { vm.password = it },
            label = "Contraseña",
            imeAction = ImeAction.Done,
            onDone = { vm.submit(onLoggedIn) },
        )
        ErrorBanner(vm.error)
        PrimaryButton("Ingresar", onClick = { vm.submit(onLoggedIn) }, loading = vm.loading)

        Spacer(Modifier.height(24.dp))
        Text(
            "Versión ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
