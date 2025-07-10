package com.e17kapps.iepinpersonal.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.e17kapps.iepinpersonal.domain.model.*
import com.e17kapps.iepinpersonal.domain.repository.EmployeeRepository
import com.e17kapps.iepinpersonal.domain.repository.PaymentRepository
import com.e17kapps.iepinpersonal.domain.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val paymentRepository: PaymentRepository,
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    private val _dashboardStats = MutableStateFlow<UiState<DashboardStatistics>>(UiState.Loading)
    val dashboardStats: StateFlow<UiState<DashboardStatistics>> = _dashboardStats.asStateFlow()

    private val _selectedEmployee = MutableStateFlow<Employee?>(null)
    val selectedEmployee: StateFlow<Employee?> = _selectedEmployee.asStateFlow()

    private val _employeeStats = MutableStateFlow<UiState<EmployeeStatistics>>(UiState.Empty)
    val employeeStats: StateFlow<UiState<EmployeeStatistics>> = _employeeStats.asStateFlow()

    private val _monthlyStats = MutableStateFlow<UiState<MonthlyStats>>(UiState.Empty)
    val monthlyStats: StateFlow<UiState<MonthlyStats>> = _monthlyStats.asStateFlow()

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _paymentMethodStats = MutableStateFlow<List<PaymentMethodStats>>(emptyList())
    val paymentMethodStats: StateFlow<List<PaymentMethodStats>> = _paymentMethodStats.asStateFlow()

    private val _recentPayments = MutableStateFlow<List<Payment>>(emptyList())
    val recentPayments: StateFlow<List<Payment>> = _recentPayments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadDashboardStatistics()
        loadRecentPayments()
    }

    private fun loadDashboardStatistics() {
        _dashboardStats.value = UiState.Loading

        viewModelScope.launch {
            statisticsRepository.getDashboardStatistics()
                .onSuccess { stats ->
                    _dashboardStats.value = UiState.Success(stats)
                    _paymentMethodStats.value = stats.paymentMethodDistribution
                }
                .onFailure { exception ->
                    _dashboardStats.value = UiState.Error(exception.message ?: "Error al cargar estadísticas")
                }
        }
    }

    private fun loadRecentPayments() {
        viewModelScope.launch {
            paymentRepository.getPaymentsFlow().collect { payments ->
                _recentPayments.value = payments.take(10) // Últimos 10 pagos
            }
        }
    }

    fun selectEmployee(employee: Employee) {
        _selectedEmployee.value = employee
        loadEmployeeStatistics(employee.id)
    }

    fun clearEmployeeSelection() {
        _selectedEmployee.value = null
        _employeeStats.value = UiState.Empty
    }

    private fun loadEmployeeStatistics(employeeId: String) {
        _employeeStats.value = UiState.Loading

        viewModelScope.launch {
            statisticsRepository.getEmployeeStatistics(employeeId)
                .onSuccess { stats ->
                    _employeeStats.value = UiState.Success(stats)
                }
                .onFailure { exception ->
                    _employeeStats.value = UiState.Error(exception.message ?: "Error al cargar estadísticas del empleado")
                }
        }
    }

    fun selectMonth(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
        loadMonthlyStatistics(month, year)
    }

    private fun loadMonthlyStatistics(month: Int, year: Int) {
        _monthlyStats.value = UiState.Loading

        viewModelScope.launch {
            statisticsRepository.getMonthlyStats(month, year)
                .onSuccess { stats ->
                    _monthlyStats.value = UiState.Success(stats)
                }
                .onFailure { exception ->
                    _monthlyStats.value = UiState.Error(exception.message ?: "Error al cargar estadísticas mensuales")
                }
        }
    }

    fun refreshStatistics() {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            statisticsRepository.updateDashboardStats()
                .onSuccess {
                    loadDashboardStatistics()
                    _selectedEmployee.value?.let { employee ->
                        loadEmployeeStatistics(employee.id)
                    }
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Error al actualizar estadísticas"
                    _isLoading.value = false
                }
        }
    }

    fun getCurrentMonthComparison(): String {
        val currentStats = (_dashboardStats.value as? UiState.Success)?.data?.monthlyComparison
        return when {
            currentStats == null -> "Sin datos"
            currentStats.percentageChange > 0 -> "+${String.format("%.1f", currentStats.percentageChange)}%"
            currentStats.percentageChange < 0 -> "${String.format("%.1f", currentStats.percentageChange)}%"
            else -> "0%"
        }
    }

    fun getTopPaymentMethod(): PaymentMethod? {
        return _paymentMethodStats.value.maxByOrNull { it.count }?.method
    }

    fun calculatePendingPayments(): Double {
        return (_dashboardStats.value as? UiState.Success)?.data?.totalPendingAmount ?: 0.0
    }

    fun calculateAveragePayment(): Double {
        val payments = _recentPayments.value.filter { it.status == PaymentStatus.COMPLETED }
        return if (payments.isNotEmpty()) {
            payments.sumOf { it.amount } / payments.size
        } else {
            0.0
        }
    }

    fun getPaymentsByMethod(method: PaymentMethod) {
        _isLoading.value = true

        viewModelScope.launch {
            paymentRepository.getPaymentsByMethod(method)
                .onSuccess { payments ->
                    _recentPayments.value = payments
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _errorMessage.value = "Error al cargar pagos por método: ${exception.message}"
                    _isLoading.value = false
                }
        }
    }

    fun getPaymentsByDateRange(startDate: Long, endDate: Long) {
        _isLoading.value = true

        viewModelScope.launch {
            paymentRepository.getPaymentsByDateRange(startDate, endDate)
                .onSuccess { payments ->
                    _recentPayments.value = payments
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _errorMessage.value = "Error al cargar pagos por rango de fecha: ${exception.message}"
                    _isLoading.value = false
                }
        }
    }

    fun exportMonthlyReport(month: Int, year: Int): String {
        val monthlyStats = (_monthlyStats.value as? UiState.Success)?.data
        val payments = _recentPayments.value.filter { payment ->
            val calendar = Calendar.getInstance().apply { timeInMillis = payment.paymentDate }
            calendar.get(Calendar.MONTH) + 1 == month && calendar.get(Calendar.YEAR) == year
        }

        return buildString {
            appendLine("REPORTE MENSUAL - $month/$year")
            appendLine("=".repeat(40))
            appendLine()
            appendLine("RESUMEN:")
            appendLine("Total de pagos: S/ ${monthlyStats?.totalPayments ?: 0.0}")
            appendLine("Número de pagos: ${monthlyStats?.paymentCount ?: 0}")
            appendLine("Promedio por pago: S/ ${monthlyStats?.averagePayment ?: 0.0}")
            appendLine("Total descuentos: S/ ${monthlyStats?.totalDiscounts ?: 0.0}")
            appendLine("Total adelantos: S/ ${monthlyStats?.totalAdvances ?: 0.0}")
            appendLine()
            appendLine("DETALLE DE PAGOS:")
            payments.forEach { payment ->
                appendLine("${payment.employeeName} - S/ ${payment.amount} - ${payment.paymentMethod.displayName}")
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // Función para generar datos para gráficos
    fun getChartData(): List<Pair<String, Double>> {
        val stats = (_dashboardStats.value as? UiState.Success)?.data
        return stats?.paymentMethodDistribution?.map {
            it.method.displayName to it.totalAmount
        } ?: emptyList()
    }

    fun getMonthlyTrend(): List<Pair<String, Double>> {
        // Aquí podrías implementar la lógica para obtener la tendencia mensual
        // Por ahora retornamos datos de ejemplo
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

        return months.mapIndexed { index, month ->
            month to (index + 1) * 1000.0 // Datos de ejemplo
        }
    }
}