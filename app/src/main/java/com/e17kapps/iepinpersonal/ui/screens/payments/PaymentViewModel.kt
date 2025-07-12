package com.e17kapps.iepinpersonal.ui.screens.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.e17kapps.iepinpersonal.domain.model.*
import com.e17kapps.iepinpersonal.domain.repository.EmployeeRepository
import com.e17kapps.iepinpersonal.domain.repository.PaymentRepository
import com.e17kapps.iepinpersonal.utils.isValidAmount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _employees = MutableStateFlow<List<Employee>>(emptyList())
    val employees: StateFlow<List<Employee>> = _employees.asStateFlow()

    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments.asStateFlow()

    private val _selectedPayment = MutableStateFlow<Payment?>(null)
    val selectedPayment: StateFlow<Payment?> = _selectedPayment.asStateFlow()

    // Jobs para manejar cancelaciones
    private var loadEmployeesJob: Job? = null
    private var loadPaymentsJob: Job? = null

    init {
        loadEmployees()
        loadPayments()
    }

    private fun loadEmployees() {
        // Cancelar job anterior si existe
        loadEmployeesJob?.cancel()

        loadEmployeesJob = viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                // Usar la misma lógica que EmployeesScreen - getAllEmployees en lugar de getActiveEmployees
                val result = employeeRepository.getAllEmployees()

                result.onSuccess { employeeList ->
                    // Filtrar solo empleados activos
                    val activeEmployees = employeeList.filter { it.isActive }
                    _employees.value = activeEmployees
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = if (activeEmployees.isEmpty()) {
                            if (employeeList.isEmpty()) {
                                "No hay empleados registrados"
                            } else {
                                "No hay empleados activos disponibles"
                            }
                        } else null
                    )
                }.onFailure { exception ->
                    _employees.value = emptyList()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar empleados: ${exception.message}"
                    )
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _employees.value = emptyList()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    // Alternativa: Usar el Flow como en EmployeesScreen
    private fun observeEmployees() {
        loadEmployeesJob?.cancel()

        loadEmployeesJob = viewModelScope.launch {
            try {
                employeeRepository.getEmployeesFlow()
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar empleados: ${exception.message}"
                        )
                    }
                    .collect { employeeList ->
                        val activeEmployees = employeeList.filter { it.isActive }
                        _employees.value = activeEmployees
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = if (activeEmployees.isEmpty()) {
                                if (employeeList.isEmpty()) {
                                    "No hay empleados registrados"
                                } else {
                                    "No hay empleados activos disponibles"
                                }
                            } else null
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

    private fun loadPayments() {
        // Cancelar job anterior si existe
        loadPaymentsJob?.cancel()

        loadPaymentsJob = viewModelScope.launch {
            try {
                paymentRepository.getPaymentsFlow()
                    .catch { exception ->
                        // Manejar errores en el flow
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al cargar pagos: ${exception.message}"
                        )
                    }
                    .collect { paymentList ->
                        _payments.value = paymentList
                    }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Error al conectar con la base de datos: ${e.message}"
                    )
                }
            }
        }
    }

    fun refreshEmployees() {
        // Usar observeEmployees() para obtener datos en tiempo real como EmployeesScreen
        observeEmployees()
    }

    fun selectEmployee(employee: Employee) {
        if (employee.id.isNotEmpty()) { // Solo si es un empleado válido
            _uiState.value = _uiState.value.copy(
                selectedEmployee = employee,
                amount = employee.baseSalary.toString(),
                errorMessage = null
            )
        } else {
            // Limpiar selección
            _uiState.value = _uiState.value.copy(
                selectedEmployee = null,
                amount = "",
                errorMessage = null
            )
        }
    }

    fun updateAmount(amount: String) {
        // Permitir solo números y punto decimal
        val cleanAmount = amount.filter { it.isDigit() || it == '.' }
        if (cleanAmount.count { it == '.' } <= 1) {
            _uiState.value = _uiState.value.copy(
                amount = cleanAmount,
                errorMessage = null
            )
        }
    }

    fun updatePaymentMethod(method: PaymentMethod) {
        _uiState.value = _uiState.value.copy(
            paymentMethod = method,
            bankDetails = if (method == PaymentMethod.BANK_TRANSFER) BankDetails() else null,
            digitalWalletDetails = if (method == PaymentMethod.YAPE || method == PaymentMethod.PLIN)
                DigitalWalletDetails(walletType = method) else null,
            errorMessage = null
        )
    }

    fun updateBankDetails(
        bankName: String? = null,
        accountNumber: String? = null,
        operationNumber: String? = null
    ) {
        val current = _uiState.value.bankDetails ?: BankDetails()
        _uiState.value = _uiState.value.copy(
            bankDetails = current.copy(
                bankName = bankName ?: current.bankName,
                accountNumber = accountNumber ?: current.accountNumber,
                operationNumber = operationNumber ?: current.operationNumber,
                transferDate = System.currentTimeMillis()
            )
        )
    }

    fun updateDigitalWalletDetails(
        phoneNumber: String? = null,
        operationNumber: String? = null,
        transactionId: String? = null
    ) {
        val current = _uiState.value.digitalWalletDetails ?: DigitalWalletDetails()
        _uiState.value = _uiState.value.copy(
            digitalWalletDetails = current.copy(
                phoneNumber = phoneNumber ?: current.phoneNumber,
                operationNumber = operationNumber ?: current.operationNumber,
                transactionId = transactionId ?: current.transactionId
            )
        )
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun processPayment() {
        val currentState = _uiState.value

        if (!isValidPaymentForm(currentState)) {
            return
        }

        val employee = currentState.selectedEmployee!!
        val amount = currentState.amount.toDoubleOrNull() ?: 0.0

        val currentDate = Calendar.getInstance()
        val paymentPeriod = PaymentPeriod(
            month = currentDate.get(Calendar.MONTH) + 1,
            year = currentDate.get(Calendar.YEAR),
            description = "Sueldo"
        )

        val payment = Payment(
            employeeId = employee.id,
            employeeName = employee.fullName,
            amount = amount,
            paymentPeriod = paymentPeriod,
            paymentMethod = currentState.paymentMethod,
            bankDetails = currentState.bankDetails,
            digitalWalletDetails = currentState.digitalWalletDetails,
            notes = currentState.notes,
        )

        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                paymentRepository.addPayment(payment)
                    .onSuccess { paymentId ->
                        _uiState.value = PaymentUiState(
                            successMessage = "Pago registrado exitosamente"
                        )
                    }
                    .onFailure { exception ->
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Error al procesar el pago"
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

    fun getPaymentsByEmployee(employeeId: String) {
        viewModelScope.launch {
            try {
                paymentRepository.getPaymentsByEmployee(employeeId)
                    .onSuccess { paymentList ->
                        _payments.value = paymentList
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al cargar pagos: ${exception.message}"
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

    fun getPaymentsByMethod(method: PaymentMethod) {
        viewModelScope.launch {
            try {
                paymentRepository.getPaymentsByMethod(method)
                    .onSuccess { paymentList ->
                        _payments.value = paymentList
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al cargar pagos: ${exception.message}"
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

    fun getPaymentsByDateRange(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            try {
                paymentRepository.getPaymentsByDateRange(startDate, endDate)
                    .onSuccess { paymentList ->
                        _payments.value = paymentList
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al cargar pagos: ${exception.message}"
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

    fun getTodayPayments() {
        viewModelScope.launch {
            try {
                paymentRepository.getTodayPayments()
                    .onSuccess { paymentList ->
                        _payments.value = paymentList
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al cargar pagos de hoy: ${exception.message}"
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

    fun selectPayment(payment: Payment) {
        _selectedPayment.value = payment
    }

    fun clearSelection() {
        _selectedPayment.value = null
    }

    fun resetForm() {
        _uiState.value = PaymentUiState()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    private fun isValidPaymentForm(state: PaymentUiState): Boolean {
        when {
            state.selectedEmployee == null -> {
                _uiState.value = state.copy(errorMessage = "Selecciona un empleado")
                return false
            }
            state.amount.isBlank() -> {
                _uiState.value = state.copy(errorMessage = "El monto es requerido")
                return false
            }
            state.amount.toDoubleOrNull() == null -> {
                _uiState.value = state.copy(errorMessage = "Monto no válido")
                return false
            }
            !state.amount.toDouble().isValidAmount() -> {
                _uiState.value = state.copy(errorMessage = "El monto debe ser mayor a 0")
                return false
            }
            state.paymentMethod == PaymentMethod.BANK_TRANSFER && !isValidBankDetails(state.bankDetails) -> {
                return false
            }
            (state.paymentMethod == PaymentMethod.YAPE || state.paymentMethod == PaymentMethod.PLIN) &&
                    !isValidDigitalWalletDetails(state.digitalWalletDetails) -> {
                return false
            }
        }
        return true
    }

    private fun isValidBankDetails(bankDetails: BankDetails?): Boolean {
        if (bankDetails == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Detalles bancarios requeridos")
            return false
        }

        when {
            bankDetails.bankName.isBlank() -> {
                _uiState.value = _uiState.value.copy(errorMessage = "El banco es requerido")
                return false
            }
            bankDetails.operationNumber.isBlank() -> {
                _uiState.value = _uiState.value.copy(errorMessage = "El número de operación es requerido")
                return false
            }
        }
        return true
    }

    private fun isValidDigitalWalletDetails(walletDetails: DigitalWalletDetails?): Boolean {
        if (walletDetails == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Detalles de billetera digital requeridos")
            return false
        }

        when {
            walletDetails.phoneNumber.isBlank() -> {
                _uiState.value = _uiState.value.copy(errorMessage = "El número de teléfono es requerido")
                return false
            }
            walletDetails.operationNumber.isBlank() -> {
                _uiState.value = _uiState.value.copy(errorMessage = "El número de operación es requerido")
                return false
            }
        }
        return true
    }

    // Métodos adicionales para estadísticas de pagos
    fun calculateTotalPayments(payments: List<Payment>): Double {
        return payments.filter { it.status == PaymentStatus.COMPLETED }.sumOf { it.amount }
    }

    fun getPaymentMethodDistribution(payments: List<Payment>): Map<PaymentMethod, Int> {
        return payments.groupBy { it.paymentMethod }.mapValues { it.value.size }
    }

    fun getMonthlyPayments(month: Int, year: Int) {
        viewModelScope.launch {
            try {
                paymentRepository.getPaymentsByMonth(month, year)
                    .onSuccess { paymentList ->
                        _payments.value = paymentList
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al cargar pagos mensuales: ${exception.message}"
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

    override fun onCleared() {
        super.onCleared()
        // Cancelar jobs al limpiar el ViewModel
        loadEmployeesJob?.cancel()
        loadPaymentsJob?.cancel()
    }

    fun getPaymentById(paymentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                paymentRepository.getPayment(paymentId)
                    .onSuccess { payment ->
                        _selectedPayment.value = payment
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    .onFailure { exception ->
                        _selectedPayment.value = null
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar el pago: ${exception.message}"
                        )
                    }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _selectedPayment.value = null
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    fun deletePayment(paymentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                paymentRepository.deletePayment(paymentId)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Pago eliminado exitosamente"
                        )
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Error al eliminar el pago: ${exception.message}"
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

    fun clearSelectedPayment() {
        _selectedPayment.value = null
    }


}