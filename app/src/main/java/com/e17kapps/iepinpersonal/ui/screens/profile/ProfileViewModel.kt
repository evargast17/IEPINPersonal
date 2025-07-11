package com.e17kapps.iepinpersonal.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.e17kapps.iepinpersonal.domain.model.User
import com.e17kapps.iepinpersonal.domain.repository.AuthRepository
import com.e17kapps.iepinpersonal.domain.model.ProfileUiState
import com.e17kapps.iepinpersonal.utils.toDateString
import com.e17kapps.iepinpersonal.utils.toDateTimeString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        observeUser()
    }

    private fun observeUser() {
        viewModelScope.launch {
            authRepository.getCurrentUserFlow().collect { user ->
                _currentUser.value = user
            }
        }
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                _currentUser.value = user
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al cargar perfil: ${e.message}"
                )
            }
        }
    }

    fun updateProfile(name: String) {
        val user = _currentUser.value ?: return

        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "El nombre no puede estar vacío"
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            val updatedUser = user.copy(name = name.trim())

            authRepository.updateUserProfile(updatedUser)
                .onSuccess {
                    _currentUser.value = updatedUser
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Perfil actualizado exitosamente"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error al actualizar perfil"
                    )
                }
        }
    }

    fun logout() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            authRepository.logout()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        logoutSuccess = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error al cerrar sesión"
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun getUserInitials(): String {
        val user = _currentUser.value
        return if (user != null && user.name.isNotBlank()) {
            user.name.split(" ")
                .take(2)
                .map { it.first().uppercase() }
                .joinToString("")
        } else {
            "U"
        }
    }

    fun getUserDisplayName(): String {
        return _currentUser.value?.name?.ifBlank { "Usuario" } ?: "Usuario"
    }

    fun getUserEmail(): String {
        return _currentUser.value?.email ?: ""
    }

    fun getUserRole(): String {
        return _currentUser.value?.role?.displayName ?: ""
    }

    fun isCurrentUserAdmin(): Boolean {
        return _currentUser.value?.role == com.e17kapps.iepinpersonal.domain.model.UserRole.ADMIN
    }

    fun getAccountCreationDate(): String {
        val user = _currentUser.value ?: return ""
        val createdAt = user.createdAt
        return if (createdAt > 0) {
            createdAt.toDateString("dd/MM/yyyy")
        } else {
            "Fecha no disponible"
        }
    }

    fun getLastUpdateDate(): String {
        val user = _currentUser.value ?: return ""
        val updatedAt = user.updatedAt
        return if (updatedAt > 0) {
            updatedAt.toDateTimeString("dd/MM/yyyy HH:mm")
        } else {
            "Nunca actualizado"
        }
    }

    fun refreshProfile() {
        loadUserProfile()
    }

    fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "El nombre no puede estar vacío"
            name.length < 2 -> "El nombre debe tener al menos 2 caracteres"
            name.length > 50 -> "El nombre no puede tener más de 50 caracteres"
            !name.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) ->
                "El nombre solo puede contener letras y espacios"
            else -> null
        }
    }

    fun canEditProfile(): Boolean {
        // En el futuro, aquí se puede agregar lógica para verificar permisos
        return _currentUser.value != null && !_uiState.value.isLoading
    }
}