package com.e17kapps.iepinpersonal.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.e17kapps.iepinpersonal.data.repository.UserRepositoryImpl
import com.e17kapps.iepinpersonal.domain.manager.RoleManager
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
    private val authRepository: AuthRepository,
    private val userRepository: UserRepositoryImpl,
    private val roleManager: RoleManager
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
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    _currentUser.value = user
                    userRepository.setCurrentUser(user.uid)
                    roleManager.loadCurrentUser()
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUserFlow().collect { user ->
                _currentUser.value = user
                if (user != null) {
                    userRepository.setCurrentUser(user.uid)
                    roleManager.loadCurrentUser()
                    _authState.value = AuthState.Authenticated
                } else {
                    userRepository.setCurrentUser(null)
                    roleManager.clearCurrentUser()
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
            authRepository.login(currentState.email.trim(), currentState.password)
                .onSuccess { user ->
                    _currentUser.value = user
                    userRepository.setCurrentUser(user.uid)
                    roleManager.loadCurrentUser()
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

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
                .onSuccess {
                    _currentUser.value = null
                    userRepository.setCurrentUser(null)
                    roleManager.clearCurrentUser()
                    _authState.value = AuthState.Unauthenticated
                    _loginUiState.value = LoginUiState() // Reset form
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Error al cerrar sesión")
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
        return when {
            state.email.isBlank() -> {
                _loginUiState.value = state.copy(
                    errorMessage = "El correo electrónico es requerido"
                )
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> {
                _loginUiState.value = state.copy(
                    errorMessage = "Formato de correo electrónico inválido"
                )
                false
            }
            state.password.isBlank() -> {
                _loginUiState.value = state.copy(
                    errorMessage = "La contraseña es requerida"
                )
                false
            }
            state.password.length < 6 -> {
                _loginUiState.value = state.copy(
                    errorMessage = "La contraseña debe tener al menos 6 caracteres"
                )
                false
            }
            else -> true
        }
    }
}