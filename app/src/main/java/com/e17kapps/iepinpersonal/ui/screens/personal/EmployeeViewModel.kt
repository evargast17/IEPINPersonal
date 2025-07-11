package com.e17kapps.iepinpersonal.ui.screens.personal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.e17kapps.iepinpersonal.domain.model.Employee
import com.e17kapps.iepinpersonal.domain.model.EmployeeListUiState
import com.e17kapps.iepinpersonal.domain.model.EmergencyContact
import com.e17kapps.iepinpersonal.domain.repository.EmployeeRepository
import com.e17kapps.iepinpersonal.utils.formatCurrency
import com.e17kapps.iepinpersonal.utils.toDateString
import com.e17kapps.iepinpersonal.utils.toDateTimeString
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
            // Mantener el ID original y actualizar timestamp
            val updatedEmployee = employee.copy(
                updatedAt = System.currentTimeMillis()
            )

            employeeRepository.updateEmployee(updatedEmployee)
                .onSuccess {
                    _successMessage.value = "Empleado actualizado exitosamente"
                    _selectedEmployee.value = updatedEmployee

                    // Actualizar también en la lista de empleados
                    val currentEmployees = _uiState.value.employees.toMutableList()
                    val index = currentEmployees.indexOfFirst { it.id == updatedEmployee.id }
                    if (index != -1) {
                        currentEmployees[index] = updatedEmployee
                        _uiState.value = _uiState.value.copy(
                            employees = currentEmployees,
                            filteredEmployees = if (_uiState.value.searchQuery.isBlank()) {
                                currentEmployees
                            } else {
                                filterEmployees(currentEmployees, _uiState.value.searchQuery)
                            }
                        )
                    }

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

    // Método para validar formulario mejorado
    private fun isValidEmployeeForm(employee: Employee): Boolean {
        // Limpiar mensaje de error previo
        _errorMessage.value = null

        when {
            employee.dni.isBlank() -> {
                _errorMessage.value = "El DNI es requerido"
                return false
            }
            !employee.dni.isValidDNI() -> {
                _errorMessage.value = "DNI no válido (debe tener 8 dígitos)"
                return false
            }
            employee.name.isBlank() -> {
                _errorMessage.value = "El nombre es requerido"
                return false
            }
            employee.name.length < 2 -> {
                _errorMessage.value = "El nombre debe tener al menos 2 caracteres"
                return false
            }
            employee.lastName.isBlank() -> {
                _errorMessage.value = "El apellido es requerido"
                return false
            }
            employee.lastName.length < 2 -> {
                _errorMessage.value = "El apellido debe tener al menos 2 caracteres"
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
            employee.baseSalary > 99999.99 -> {
                _errorMessage.value = "El salario no puede exceder S/ 99,999.99"
                return false
            }
            employee.phone.isBlank() -> {
                _errorMessage.value = "El teléfono es requerido"
                return false
            }
            !employee.phone.isValidPhone() -> {
                _errorMessage.value = "Teléfono no válido (mínimo 9 dígitos)"
                return false
            }
            employee.address.isBlank() -> {
                _errorMessage.value = "La dirección es requerida"
                return false
            }
            employee.email.isNotBlank() && !employee.email.isValidEmail() -> {
                _errorMessage.value = "Email no válido"
                return false
            }
            // Validar DNI único (excluyendo el empleado actual en caso de edición)
            isDniDuplicated(employee.dni, employee.id) -> {
                _errorMessage.value = "Ya existe un empleado con este DNI"
                return false
            }
            // Validar contacto de emergencia si se proporcionó
            employee.emergencyContact != null && !isValidEmergencyContact(employee.emergencyContact) -> {
                return false
            }
        }
        return true
    }

    // Método para validar contacto de emergencia
    private fun isValidEmergencyContact(contact: EmergencyContact): Boolean {
        when {
            contact.name.isNotBlank() && contact.name.length < 2 -> {
                _errorMessage.value = "El nombre del contacto de emergencia debe tener al menos 2 caracteres"
                return false
            }
            contact.phone.isNotBlank() && !contact.phone.isValidPhone() -> {
                _errorMessage.value = "Teléfono del contacto de emergencia no válido"
                return false
            }
            contact.relationship.isNotBlank() && contact.relationship.length < 2 -> {
                _errorMessage.value = "El parentesco debe tener al menos 2 caracteres"
                return false
            }
            // Si se proporciona algún campo, todos los campos principales deben estar completos
            (contact.name.isNotBlank() || contact.phone.isNotBlank() || contact.relationship.isNotBlank()) &&
                    (contact.name.isBlank() || contact.phone.isBlank() || contact.relationship.isBlank()) -> {
                _errorMessage.value = "Si agrega contacto de emergencia, complete nombre, teléfono y parentesco"
                return false
            }
        }
        return true
    }

    // Método para verificar DNI duplicado
    private fun isDniDuplicated(dni: String, currentEmployeeId: String): Boolean {
        return _uiState.value.employees.any { employee ->
            employee.dni == dni && employee.id != currentEmployeeId
        }
    }

    // Método para cargar un empleado específico por ID
    suspend fun loadEmployeeById(employeeId: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            employeeRepository.getEmployee(employeeId)
                .onSuccess { employee ->
                    _selectedEmployee.value = employee
                    _employeeForm.value = employee
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Error al cargar empleado"
                    _isLoading.value = false
                }
        }
    }

    // Método para obtener empleado de la lista en memoria (más rápido)
    fun selectEmployeeById(employeeId: String) {
        val employee = _uiState.value.employees.find { it.id == employeeId }
        if (employee != null) {
            selectEmployee(employee)
        } else {
            // Si no está en memoria, cargar desde repositorio
            viewModelScope.launch {
                loadEmployeeById(employeeId)
            }
        }
    }

    // Método para activar/desactivar empleado con confirmación
    fun toggleEmployeeStatus(employeeId: String, newStatus: Boolean) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = if (newStatus) {
                employeeRepository.reactivateEmployee(employeeId)
            } else {
                employeeRepository.deactivateEmployee(employeeId)
            }

            result.onSuccess {
                _successMessage.value = if (newStatus) {
                    "Empleado reactivado exitosamente"
                } else {
                    "Empleado desactivado exitosamente"
                }

                // Actualizar el empleado seleccionado si es el mismo
                if (_selectedEmployee.value?.id == employeeId) {
                    _selectedEmployee.value = _selectedEmployee.value?.copy(
                        isActive = newStatus,
                        updatedAt = System.currentTimeMillis()
                    )
                    _employeeForm.value = _employeeForm.value.copy(
                        isActive = newStatus,
                        updatedAt = System.currentTimeMillis()
                    )
                }

                _isLoading.value = false

                // Refrescar la lista para mostrar cambios
                refreshEmployees()
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Error al cambiar estado del empleado"
                _isLoading.value = false
            }
        }
    }

    // Método para exportar datos de empleados
    fun exportEmployeesData(format: ExportFormat = ExportFormat.CSV): String {
        val employees = _uiState.value.employees

        return when (format) {
            ExportFormat.CSV -> {
                buildString {
                    // Headers
                    appendLine("DNI,Nombre,Apellidos,Cargo,Salario,Teléfono,Email,Dirección,Estado,Fecha Ingreso")

                    // Data
                    employees.forEach { employee ->
                        appendLine(
                            "${employee.dni}," +
                                    "\"${employee.name}\"," +
                                    "\"${employee.lastName}\"," +
                                    "\"${employee.position}\"," +
                                    "${employee.baseSalary}," +
                                    "${employee.phone}," +
                                    "\"${employee.email}\"," +
                                    "\"${employee.address}\"," +
                                    "${if (employee.isActive) "Activo" else "Inactivo"}," +
                                    "${employee.startDate.toDateString()}"
                        )
                    }
                }
            }
            ExportFormat.TXT -> {
                buildString {
                    appendLine("=".repeat(80))
                    appendLine("LISTADO DE EMPLEADOS")
                    appendLine("Generado: ${System.currentTimeMillis().toDateTimeString()}")
                    appendLine("=".repeat(80))
                    appendLine()

                    employees.forEach { employee ->
                        appendLine("DNI: ${employee.dni}")
                        appendLine("Nombre: ${employee.fullName}")
                        appendLine("Cargo: ${employee.position}")
                        appendLine("Salario: ${formatCurrency(employee.baseSalary)}")
                        appendLine("Teléfono: ${employee.phone}")
                        if (employee.email.isNotBlank()) {
                            appendLine("Email: ${employee.email}")
                        }
                        appendLine("Dirección: ${employee.address}")
                        appendLine("Estado: ${if (employee.isActive) "Activo" else "Inactivo"}")
                        appendLine("Fecha Ingreso: ${employee.startDate.toDateString()}")
                        if (employee.emergencyContact != null) {
                            appendLine("Contacto Emergencia: ${employee.emergencyContact.name} - ${employee.emergencyContact.phone}")
                        }
                        appendLine("-".repeat(40))
                    }

                    appendLine()
                    appendLine("Total empleados: ${employees.size}")
                    appendLine("Empleados activos: ${employees.count { it.isActive }}")
                    appendLine("Nómina total: ${formatCurrency(employees.filter { it.isActive }.sumOf { it.baseSalary })}")
                }
            }
        }
    }

    // Enum para formatos de exportación
    enum class ExportFormat {
        CSV, TXT
    }

    // Método para buscar empleados con filtros avanzados
    fun searchEmployeesAdvanced(
        query: String = "",
        isActive: Boolean? = null,
        minSalary: Double? = null,
        maxSalary: Double? = null,
        position: String? = null
    ) {
        val allEmployees = _uiState.value.employees

        val filtered = allEmployees.filter { employee ->
            // Filtro de texto
            val matchesQuery = if (query.isBlank()) true else {
                employee.fullName.contains(query, ignoreCase = true) ||
                        employee.dni.contains(query) ||
                        employee.position.contains(query, ignoreCase = true) ||
                        employee.phone.contains(query)
            }

            // Filtro de estado
            val matchesStatus = isActive?.let { employee.isActive == it } ?: true

            // Filtro de salario
            val matchesSalary = when {
                minSalary != null && maxSalary != null ->
                    employee.baseSalary >= minSalary && employee.baseSalary <= maxSalary
                minSalary != null -> employee.baseSalary >= minSalary
                maxSalary != null -> employee.baseSalary <= maxSalary
                else -> true
            }

            // Filtro de cargo
            val matchesPosition = position?.let {
                employee.position.contains(it, ignoreCase = true)
            } ?: true

            matchesQuery && matchesStatus && matchesSalary && matchesPosition
        }

        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredEmployees = filtered
        )
    }

    // Método para obtener empleados por cargo
    fun getEmployeesByPosition(): Map<String, List<Employee>> {
        return _uiState.value.employees
            .filter { it.isActive }
            .groupBy { it.position }
            .toSortedMap()
    }

    // Método para obtener estadísticas salariales
    fun getSalaryStatistics(): SalaryStatistics {
        val activeEmployees = _uiState.value.employees.filter { it.isActive }
        val salaries = activeEmployees.map { it.baseSalary }

        return if (salaries.isNotEmpty()) {
            SalaryStatistics(
                minSalary = salaries.minOrNull() ?: 0.0,
                maxSalary = salaries.maxOrNull() ?: 0.0,
                averageSalary = salaries.average(),
                medianSalary = salaries.sorted().let { sorted ->
                    val size = sorted.size
                    if (size % 2 == 0) {
                        (sorted[size / 2 - 1] + sorted[size / 2]) / 2
                    } else {
                        sorted[size / 2]
                    }
                },
                totalPayroll = salaries.sum(),
                employeeCount = salaries.size
            )
        } else {
            SalaryStatistics()
        }
    }

    // Clase de datos para estadísticas salariales
    data class SalaryStatistics(
        val minSalary: Double = 0.0,
        val maxSalary: Double = 0.0,
        val averageSalary: Double = 0.0,
        val medianSalary: Double = 0.0,
        val totalPayroll: Double = 0.0,
        val employeeCount: Int = 0
    )

    // Método para limpiar formulario
    fun clearForm() {
        _employeeForm.value = Employee()
        _selectedEmployee.value = null
        _errorMessage.value = null
        _successMessage.value = null
    }

    // Método para validar antes de navegar
    fun validateAndNavigate(action: () -> Unit) {
        if (_isLoading.value) {
            _errorMessage.value = "Operación en progreso, espere un momento"
            return
        }

        // Limpiar mensajes previos
        clearMessages()

        action()
    }

    // Método para obtener empleados ordenados
    fun getSortedEmployees(sortBy: EmployeeSortBy): List<Employee> {
        val employees = if (_uiState.value.searchQuery.isBlank()) {
            _uiState.value.employees
        } else {
            _uiState.value.filteredEmployees
        }

        return when (sortBy) {
            EmployeeSortBy.NAME_ASC -> employees.sortedBy { it.fullName }
            EmployeeSortBy.NAME_DESC -> employees.sortedByDescending { it.fullName }
            EmployeeSortBy.SALARY_ASC -> employees.sortedBy { it.baseSalary }
            EmployeeSortBy.SALARY_DESC -> employees.sortedByDescending { it.baseSalary }
            EmployeeSortBy.START_DATE_ASC -> employees.sortedBy { it.startDate }
            EmployeeSortBy.START_DATE_DESC -> employees.sortedByDescending { it.startDate }
            EmployeeSortBy.POSITION_ASC -> employees.sortedBy { it.position }
            EmployeeSortBy.POSITION_DESC -> employees.sortedByDescending { it.position }
            EmployeeSortBy.STATUS -> employees.sortedByDescending { it.isActive }
        }
    }

    // Enum para opciones de ordenamiento
    enum class EmployeeSortBy(val displayName: String) {
        NAME_ASC("Nombre A-Z"),
        NAME_DESC("Nombre Z-A"),
        SALARY_ASC("Salario menor a mayor"),
        SALARY_DESC("Salario mayor a menor"),
        START_DATE_ASC("Más antiguos primero"),
        START_DATE_DESC("Más recientes primero"),
        POSITION_ASC("Cargo A-Z"),
        POSITION_DESC("Cargo Z-A"),
        STATUS("Activos primero")
    }

    // Método para aplicar ordenamiento
    fun applySorting(sortBy: EmployeeSortBy) {
        val sortedEmployees = getSortedEmployees(sortBy)
        _uiState.value = _uiState.value.copy(
            filteredEmployees = sortedEmployees
        )
    }

    // Funciones de extensión necesarias para las validaciones
    private fun String.isValidDNI(): Boolean {
        return this.length == 8 && this.all { it.isDigit() }
    }

    private fun String.isValidPhone(): Boolean {
        return this.length >= 9 && this.all { it.isDigit() || it == '+' || it == '-' || it == ' ' }
    }

    private fun String.isValidEmail(): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }



}