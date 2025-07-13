package com.e17kapps.iepinpersonal.ui.screens.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.e17kapps.iepinpersonal.domain.model.User
import com.e17kapps.iepinpersonal.domain.model.UserRole
import com.e17kapps.iepinpersonal.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserManagementUiState())
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private var loadUsersJob: Job? = null

    init {
        loadUsers()
    }

    fun loadUsers() {
        loadUsersJob?.cancel()
        loadUsersJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                userRepository.getUsersFlow()
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar usuarios: ${exception.message}"
                        )
                    }
                    .collect { userList ->
                        _users.value = userList
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    fun createUser(
        email: String,
        displayName: String,
        password: String,
        role: UserRole,
        department: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val newUser = User(
                email = email.trim(),
                displayName = displayName.trim(),
                role = role,
                updatedAt = 0
            )

            try {
                userRepository.createUser(newUser, password)
                    .onSuccess { userId ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Usuario creado exitosamente"
                        )
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Error al crear usuario: ${exception.message}"
                        )
                    }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateUserRole(userId: String, newRole: UserRole) {
        viewModelScope.launch {
            try {
                userRepository.updateUserRole(userId, newRole)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Rol actualizado exitosamente"
                        )
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al actualizar rol: ${exception.message}"
                        )
                    }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    fun toggleUserStatus(userId: String, isActive: Boolean) {
        viewModelScope.launch {
            try {
                if (isActive) {
                    userRepository.activateUser(userId)
                } else {
                    userRepository.deactivateUser(userId)
                }
                    .onSuccess {
                        val action = if (isActive) "activado" else "desactivado"
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Usuario $action exitosamente"
                        )
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al cambiar estado: ${exception.message}"
                        )
                    }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    fun getUsersByRole(role: UserRole) {
        viewModelScope.launch {
            try {
                userRepository.getUsersByRole(role)
                    .onSuccess { userList ->
                        _users.value = userList
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al filtrar usuarios: ${exception.message}"
                        )
                    }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    override fun onCleared() {
        super.onCleared()
        loadUsersJob?.cancel()
    }
}

data class UserManagementUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)