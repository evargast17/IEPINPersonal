package com.e17kapps.iepinpersonal.ui.screens.discounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.e17kapps.iepinpersonal.domain.model.*
import com.e17kapps.iepinpersonal.domain.repository.DiscountRepository
import com.e17kapps.iepinpersonal.domain.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscountViewModel @Inject constructor(
    private val discountRepository: DiscountRepository,
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscountUiState())
    val uiState: StateFlow<DiscountUiState> = _uiState.asStateFlow()

    private val _discounts = MutableStateFlow<List<Discount>>(emptyList())
    val discounts: StateFlow<List<Discount>> = _discounts.asStateFlow()

    private val _employees = MutableStateFlow<List<Employee>>(emptyList())
    val employees: StateFlow<List<Employee>> = _employees.asStateFlow()

    private val _selectedDiscount = MutableStateFlow<Discount?>(null)
    val selectedDiscount: StateFlow<Discount?> = _selectedDiscount.asStateFlow()

    // Jobs para manejar cancelaciones
    private var loadDiscountsJob: Job? = null
    private var loadEmployeesJob: Job? = null

    init {
        loadDiscounts()
        loadEmployees()
    }

    // =========================================================================
    // MÉTODOS PARA CARGAR DATOS
    // =========================================================================

    fun loadDiscounts() {
        loadDiscountsJob?.cancel()
        loadDiscountsJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                discountRepository.getDiscountsFlow()
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar descuentos: ${exception.message}"
                        )
                    }
                    .collect { discountList ->
                        _discounts.value = discountList
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

    fun loadEmployees() {
        loadEmployeesJob?.cancel()
        loadEmployeesJob = viewModelScope.launch {
            try {
                employeeRepository.getAllEmployees()
                    .onSuccess { employeeList ->
                        _employees.value = employeeList.filter { it.isActive }
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al cargar empleados: ${exception.message}"
                        )
                    }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Error inesperado al cargar empleados: ${e.message}"
                    )
                }
            }
        }
    }

    // =========================================================================
    // MÉTODOS PARA AGREGAR DESCUENTO
    // =========================================================================

    fun selectEmployee(employee: Employee) {
        _uiState.value = _uiState.value.copy(
            selectedEmployee = employee,
            employeeSearchQuery = employee.fullName
        )
    }

    fun updateEmployeeSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(employeeSearchQuery = query)
        if (query.isEmpty()) {
            _uiState.value = _uiState.value.copy(selectedEmployee = null)
        }
    }

    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun updateDiscountType(type: DiscountType) {
        _uiState.value = _uiState.value.copy(discountType = type)
    }

    fun updateReason(reason: String) {
        _uiState.value = _uiState.value.copy(reason = reason)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateIsRecurring(isRecurring: Boolean) {
        _uiState.value = _uiState.value.copy(isRecurring = isRecurring)
    }

    fun updateStartDate(startDate: Long) {
        _uiState.value = _uiState.value.copy(startDate = startDate)
    }

    fun updateEndDate(endDate: Long?) {
        _uiState.value = _uiState.value.copy(endDate = endDate)
    }

    fun addDiscount() {
        val currentState = _uiState.value

        if (!isValidDiscountForm(currentState)) {
            return
        }

        val employee = currentState.selectedEmployee!!
        val amount = currentState.amount.toDoubleOrNull() ?: 0.0

        val discount = Discount(
            employeeId = employee.id,
            employeeName = employee.fullName,
            amount = amount,
            type = currentState.discountType,
            reason = currentState.reason,
            description = currentState.description,
            isRecurring = currentState.isRecurring,
            startDate = currentState.startDate,
            endDate = currentState.endDate,
            isActive = true
        )

        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                discountRepository.addDiscount(discount)
                    .onSuccess { discountId ->
                        _uiState.value = DiscountUiState(
                            successMessage = "Descuento agregado exitosamente"
                        )
                    }
                    .onFailure { exception ->
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Error al agregar el descuento"
                        )
                    }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    // =========================================================================
    // MÉTODOS PARA GESTIÓN DE DESCUENTOS
    // =========================================================================

    fun getDiscountById(discountId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                discountRepository.getDiscount(discountId)
                    .onSuccess { discount ->
                        _selectedDiscount.value = discount
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    .onFailure { exception ->
                        _selectedDiscount.value = null
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar el descuento: ${exception.message}"
                        )
                    }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _selectedDiscount.value = null
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    fun toggleDiscountStatus(discountId: String, isActive: Boolean) {
        viewModelScope.launch {
            try {
                if (isActive) {
                    discountRepository.activateDiscount(discountId)
                } else {
                    discountRepository.deactivateDiscount(discountId)
                }
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            successMessage = if (isActive) "Descuento activado" else "Descuento desactivado"
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

    fun deleteDiscount(discountId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                discountRepository.deleteDiscount(discountId)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Descuento eliminado exitosamente"
                        )
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Error al eliminar el descuento: ${exception.message}"
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

    // =========================================================================
    // MÉTODOS DE FILTRADO
    // =========================================================================

    fun filterByEmployee(employeeId: String?) {
        viewModelScope.launch {
            try {
                if (employeeId != null) {
                    discountRepository.getDiscountsByEmployee(employeeId)
                        .onSuccess { discountList ->
                            _discounts.value = discountList
                        }
                        .onFailure { exception ->
                            _uiState.value = _uiState.value.copy(
                                errorMessage = "Error al filtrar descuentos: ${exception.message}"
                            )
                        }
                } else {
                    loadDiscounts()
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

    fun filterByType(type: DiscountType?) {
        viewModelScope.launch {
            try {
                if (type != null) {
                    discountRepository.getDiscountsByType(type)
                        .onSuccess { discountList ->
                            _discounts.value = discountList
                        }
                        .onFailure { exception ->
                            _uiState.value = _uiState.value.copy(
                                errorMessage = "Error al filtrar descuentos: ${exception.message}"
                            )
                        }
                } else {
                    loadDiscounts()
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

    fun getActiveDiscounts() {
        viewModelScope.launch {
            try {
                discountRepository.getActiveDiscounts()
                    .onSuccess { discountList ->
                        _discounts.value = discountList
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al cargar descuentos activos: ${exception.message}"
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

    // =========================================================================
    // MÉTODOS DE UTILIDAD
    // =========================================================================

    fun resetForm() {
        _uiState.value = DiscountUiState()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun clearSelectedDiscount() {
        _selectedDiscount.value = null
    }

    private fun isValidDiscountForm(state: DiscountUiState): Boolean {
        when {
            state.selectedEmployee == null -> {
                _uiState.value = state.copy(errorMessage = "Selecciona un empleado")
                return false
            }
            state.amount.isBlank() -> {
                _uiState.value = state.copy(errorMessage = "Ingresa el monto del descuento")
                return false
            }
            state.amount.toDoubleOrNull() == null || state.amount.toDoubleOrNull()!! <= 0 -> {
                _uiState.value = state.copy(errorMessage = "Ingresa un monto válido")
                return false
            }
            state.reason.isBlank() -> {
                _uiState.value = state.copy(errorMessage = "Ingresa la razón del descuento")
                return false
            }
            state.endDate != null && state.endDate < state.startDate -> {
                _uiState.value = state.copy(errorMessage = "La fecha de fin debe ser posterior a la fecha de inicio")
                return false
            }
        }
        return true
    }

    override fun onCleared() {
        super.onCleared()
        loadDiscountsJob?.cancel()
        loadEmployeesJob?.cancel()
    }
}

// ============================================================================
// UI STATE PARA DESCUENTOS
// ============================================================================

data class DiscountUiState(
    val selectedEmployee: Employee? = null,
    val employeeSearchQuery: String = "",
    val amount: String = "",
    val discountType: DiscountType = DiscountType.OTHER,
    val reason: String = "",
    val description: String = "",
    val isRecurring: Boolean = false,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)