package com.e17kapps.iepinpersonal.ui.screens.personal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.e17kapps.iepinpersonal.domain.model.Employee
import com.e17kapps.iepinpersonal.domain.model.EmployeeListUiState
import com.e17kapps.iepinpersonal.domain.model.EmergencyContact
import com.e17kapps.iepinpersonal.domain.model.UiState
import com.e17kapps.iepinpersonal.domain.repository.EmployeeRepository
import com.e17kapps.iepinpersonal.utils.isValidDNI
import com.e17kapps.iepinpersonal.utils.isValidEmail
import com.e17kapps.iepinpersonal.utils.isValidPhone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmployeeViewModel @Inject constructor(
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmployeeListUiState())
    val uiState: StateFlow<EmployeeListUiState> = _uiState.asStateFlow()

    private val _selectedEmployee = MutableStateFlow<Employee?>(null)
    val selectedEmployee: StateFlow<Employee?> = _selectedEmployee.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Form state for add/edit employee
    private val _employeeForm = MutableStateFlow(Employee())
    val employeeForm: StateFlow<Employee> = _employeeForm.asStateFlow()

    init {
        loadEmployees()
        observeEmployees()
    }

    private fun loadEmployees() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            employeeRepository.getAllEmployees()
                .onSuccess { employees ->
                    _uiState.value = _uiState.value.copy(
                        employees = employees,
                        filteredEmployees = employees,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error al cargar empleados"
                    )
                }
        }
    }

    private fun observeEmployees() {
        viewModelScope.launch {
            employeeRepository.getEmployeesFlow().collect { employees ->
                _uiState.value = _uiState.value.copy(
                    employees = employees,
                    filteredEmployees = if (_uiState.value.searchQuery.isBlank()) {
                        employees
                    } else {
                        filterEmployees(employees, _uiState.value.searchQuery)
                    },
                    isLoading = false
                )
            }
        }
    }

    fun searchEmployees(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)

        val filteredEmployees = if (query.isBlank()) {
            _uiState.value.employees
        } else {
            filterEmployees(_uiState.value.employees, query)
        }

        _uiState.value = _uiState.value.copy(filteredEmployees = filteredEmployees)
    }

    private fun filterEmployees(employees: List<Employee>, query: String): List<Employee> {
        return employees.filter { employee ->
            employee.fullName.contains(query, ignoreCase = true) ||
                    employee.dni.contains(query, ignoreCase = true) ||
                    employee.position.contains(query, ignoreCase = true) ||
                    employee.phone.contains(query, ignoreCase = true)
        }
    }

    fun selectEmployee(employee: Employee) {
        _selectedEmployee.value = employee
        _employeeForm.value = employee
    }

    fun clearSelection() {
        _selectedEmployee.value = null
        _employeeForm.value = Employee()
    }

    // Form update methods
    fun updateEmployeeForm(
        dni: String? = null,
        name: String? = null,
        lastName: String? = null,
        position: String? = null,
        baseSalary: Double? = null,
        phone: String? = null,
        address: String? = null,
        email: String? = null,
        bankAccount: String? = null,
        notes: String? = null,
        emergencyContact: EmergencyContact? = null
    ) {
        val current = _employeeForm.value
        _employeeForm.value = current.copy(
            dni = dni ?: current.dni,
            name = name ?: current.name,
            lastName = lastName ?: current.lastName,
            position = position ?: current.position,
            baseSalary = baseSalary ?: current.baseSalary,
            phone = phone ?: current.phone,
            address = address ?: current.address,
            email = email ?: current.email,
            bankAccount = bankAccount ?: current.bankAccount,
            notes = notes ?: current.notes,
            emergencyContact = emergencyContact ?: current.emergencyContact
        )
    }

    fun addEmployee() {
        val employee = _employeeForm.value

        if (!isValidEmployeeForm(employee)) {
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            employeeRepository.addEmployee(employee)
                .onSuccess { employeeId ->
                    _successMessage.value = "Empleado agregado exitosamente"
                    _employeeForm.value = Employee() // Reset form
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Error al agregar empleado"
                    _isLoading.value = false
                }
        }
    }

    fun updateEmployee() {
        val employee = _employeeForm.value

        if (!isValidEmployeeForm(employee)) {
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            employeeRepository.updateEmployee(employee)
                .onSuccess {
                    _successMessage.value = "Empleado actualizado exitosamente"
                    _selectedEmployee.value = employee
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Error al actualizar empleado"
                    _isLoading.value = false
                }
        }
    }

    fun deleteEmployee(employeeId: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            employeeRepository.deleteEmployee(employeeId)
                .onSuccess {
                    _successMessage.value = "Empleado eliminado exitosamente"
                    clearSelection()
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Error al eliminar empleado"
                    _isLoading.value = false
                }
        }
    }

    fun deactivateEmployee(employeeId: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            employeeRepository.deactivateEmployee(employeeId)
                .onSuccess {
                    _successMessage.value = "Empleado desactivado"
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Error al desactivar empleado"
                    _isLoading.value = false
                }
        }
    }

    fun reactivateEmployee(employeeId: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            employeeRepository.reactivateEmployee(employeeId)
                .onSuccess {
                    _successMessage.value = "Empleado reactivado"
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Error al reactivar empleado"
                    _isLoading.value = false
                }
        }
    }

    fun refreshEmployees() {
        loadEmployees()
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    private fun isValidEmployeeForm(employee: Employee): Boolean {
        when {
            employee.dni.isBlank() -> {
                _errorMessage.value = "El DNI es requerido"
                return false
            }
            !employee.dni.isValidDNI() -> {
                _errorMessage.value = "DNI no válido (8 dígitos)"
                return false
            }
            employee.name.isBlank() -> {
                _errorMessage.value = "El nombre es requerido"
                return false
            }
            employee.lastName.isBlank() -> {
                _errorMessage.value = "El apellido es requerido"
                return false
            }
            employee.position.isBlank() -> {
                _errorMessage.value = "El cargo es requerido"
                return false
            }
            employee.baseSalary <= 0 -> {
                _errorMessage.value = "El salario debe ser mayor a 0"
                return false
            }
            employee.phone.isNotBlank() && !employee.phone.isValidPhone() -> {
                _errorMessage.value = "Teléfono no válido"
                return false
            }
            employee.email.isNotBlank() && !employee.email.isValidEmail() -> {
                _errorMessage.value = "Email no válido"
                return false
            }
        }
        return true
    }
}