package com.e17kapps.iepinpersonal.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.e17kapps.iepinpersonal.domain.model.AuthState
import com.e17kapps.iepinpersonal.domain.model.LoginUiState
import com.e17kapps.iepinpersonal.domain.model.User
import com.e17kapps.iepinpersonal.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        checkAuthState()
        observeCurrentUser()
    }

    private fun checkAuthState() {
        if (authRepository.isUserLoggedIn()) {
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUserFlow().collect { user ->
                _currentUser.value = user
                if (user != null) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    fun updateEmail(email: String) {
        _loginUiState.value = _loginUiState.value.copy(
            email = email,
            errorMessage = null
        )
    }

    fun updatePassword(password: String) {
        _loginUiState.value = _loginUiState.value.copy(
            password = password,
            errorMessage = null
        )
    }

    fun togglePasswordVisibility() {
        _loginUiState.value = _loginUiState.value.copy(
            isPasswordVisible = !_loginUiState.value.isPasswordVisible
        )
    }

    fun login() {
        val currentState = _loginUiState.value

        if (!isValidLoginForm(currentState)) {
            return
        }

        _loginUiState.value = currentState.copy(isLoading = true, errorMessage = null)
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            authRepository.login(currentState.email, currentState.password)
                .onSuccess { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated
                    _loginUiState.value = LoginUiState() // Reset form
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Error de autenticación")
                    _loginUiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error de autenticación"
                    )
                }
        }
    }

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (!isValidRegisterForm(name, email, password, confirmPassword)) {
            return
        }

        _loginUiState.value = _loginUiState.value.copy(isLoading = true, errorMessage = null)
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            authRepository.register(email, password, name)
                .onSuccess { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated
                    _loginUiState.value = LoginUiState() // Reset form
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Error al crear cuenta")
                    _loginUiState.value = _loginUiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error al crear cuenta"
                    )
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
                .onSuccess {
                    _currentUser.value = null
                    _authState.value = AuthState.Unauthenticated
                    _loginUiState.value = LoginUiState() // Reset form
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Error al cerrar sesión")
                }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _loginUiState.value = _loginUiState.value.copy(
                errorMessage = "Ingresa tu correo electrónico"
            )
            return
        }

        viewModelScope.launch {
            authRepository.resetPassword(email)
                .onSuccess {
                    _loginUiState.value = _loginUiState.value.copy(
                        errorMessage = "Se envió un correo para restablecer tu contraseña"
                    )
                }
                .onFailure { exception ->
                    _loginUiState.value = _loginUiState.value.copy(
                        errorMessage = exception.message ?: "Error al enviar correo"
                    )
                }
        }
    }

    fun clearError() {
        _loginUiState.value = _loginUiState.value.copy(errorMessage = null)
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    private fun isValidLoginForm(state: LoginUiState): Boolean {
        when {
            state.email.isBlank() -> {
                _loginUiState.value = state.copy(errorMessage = "El correo es requerido")
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> {
                _loginUiState.value = state.copy(errorMessage = "Correo electrónico no válido")
                return false
            }
            state.password.isBlank() -> {
                _loginUiState.value = state.copy(errorMessage = "La contraseña es requerida")
                return false
            }
            state.password.length < 6 -> {
                _loginUiState.value = state.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres")
                return false
            }
        }
        return true
    }

    private fun isValidRegisterForm(name: String, email: String, password: String, confirmPassword: String): Boolean {
        when {
            name.isBlank() -> {
                _loginUiState.value = _loginUiState.value.copy(errorMessage = "El nombre es requerido")
                return false
            }
            email.isBlank() -> {
                _loginUiState.value = _loginUiState.value.copy(errorMessage = "El correo es requerido")
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _loginUiState.value = _loginUiState.value.copy(errorMessage = "Correo electrónico no válido")
                return false
            }
            password.isBlank() -> {
                _loginUiState.value = _loginUiState.value.copy(errorMessage = "La contraseña es requerida")
                return false
            }
            password.length < 6 -> {
                _loginUiState.value = _loginUiState.value.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres")
                return false
            }
            password != confirmPassword -> {
                _loginUiState.value = _loginUiState.value.copy(errorMessage = "Las contraseñas no coinciden")
                return false
            }
        }
        return true
    }
}