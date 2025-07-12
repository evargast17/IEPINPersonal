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
    private val employeeRepository: EmployeeRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _dashboardStats = MutableStateFlow<UiState<DashboardStatistics>>(UiState.Loading)
    val dashboardStats: StateFlow<UiState<DashboardStatistics>> = _dashboardStats.asStateFlow()

    private val _employeeStats = MutableStateFlow<UiState<EmployeeStatistics>>(UiState.Empty)
    val employeeStats: StateFlow<UiState<EmployeeStatistics>> = _employeeStats.asStateFlow()

    private val _monthlyStats = MutableStateFlow<UiState<MonthlyStats>>(UiState.Empty)
    val monthlyStats: StateFlow<UiState<MonthlyStats>> = _monthlyStats.asStateFlow()

    private val _employees = MutableStateFlow<List<Employee>>(emptyList())
    val employees: StateFlow<List<Employee>> = _employees.asStateFlow()

    private val _recentPayments = MutableStateFlow<List<Payment>>(emptyList())
    val recentPayments: StateFlow<List<Payment>> = _recentPayments.asStateFlow()

    private val _selectedEmployee = MutableStateFlow<Employee?>(null)
    val selectedEmployee: StateFlow<Employee?> = _selectedEmployee.asStateFlow()

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Estados adicionales para la UI - AHORA USANDO UiStates.kt
    private val _selectedTimeframe = MutableStateFlow(StatisticsTimeframe.MONTH)
    val selectedTimeframe: StateFlow<StatisticsTimeframe> = _selectedTimeframe.asStateFlow()

    private val _exportStatus = MutableStateFlow<ExportStatus>(ExportStatus.Idle)
    val exportStatus: StateFlow<ExportStatus> = _exportStatus.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            loadDashboardStatistics()
            loadEmployees()
            loadRecentPayments()
        }
    }

    private fun loadDashboardStatistics() {
        _dashboardStats.value = UiState.Loading

        viewModelScope.launch {
            try {
                statisticsRepository.getDashboardStatistics()
                    .onSuccess { stats ->
                        _dashboardStats.value = UiState.Success(stats)
                        _errorMessage.value = null
                    }
                    .onFailure { exception ->
                        _dashboardStats.value = UiState.Error(exception.message ?: "Error al cargar estadísticas")
                        _errorMessage.value = exception.message
                    }
            } catch (e: Exception) {
                _dashboardStats.value = UiState.Error(e.message ?: "Error inesperado")
                _errorMessage.value = e.message
            }
        }
    }

    private fun loadEmployees() {
        viewModelScope.launch {
            try {
                employeeRepository.getEmployeesFlow().collect { employees ->
                    _employees.value = employees.filter { it.isActive }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar empleados: ${e.message}"
            }
        }
    }

    private fun loadRecentPayments() {
        viewModelScope.launch {
            try {
                paymentRepository.getPaymentsFlow().collect { payments ->
                    _recentPayments.value = payments
                        .filter { it.status == PaymentStatus.COMPLETED }
                        .sortedByDescending { it.paymentDate }
                        .take(10)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar pagos recientes: ${e.message}"
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
            try {
                statisticsRepository.getEmployeeStatistics(employeeId)
                    .onSuccess { stats ->
                        _employeeStats.value = UiState.Success(stats)
                    }
                    .onFailure { exception ->
                        _employeeStats.value = UiState.Error(
                            exception.message ?: "Error al cargar estadísticas del empleado"
                        )
                    }
            } catch (e: Exception) {
                _employeeStats.value = UiState.Error(e.message ?: "Error inesperado")
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
            try {
                statisticsRepository.getMonthlyStats(month, year)
                    .onSuccess { stats ->
                        _monthlyStats.value = UiState.Success(stats)
                    }
                    .onFailure { exception ->
                        _monthlyStats.value = UiState.Error(
                            exception.message ?: "Error al cargar estadísticas mensuales"
                        )
                    }
            } catch (e: Exception) {
                _monthlyStats.value = UiState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun selectTimeframe(timeframe: StatisticsTimeframe) {
        _selectedTimeframe.value = timeframe
        // Recargar estadísticas según el nuevo timeframe
        refreshStatistics()
    }

    fun refreshStatistics() {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
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
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error inesperado"
                _isLoading.value = false
            }
        }
    }

    fun exportData(format: String) {
        _exportStatus.value = ExportStatus.Loading

        viewModelScope.launch {
            try {
                when (format.lowercase()) {
                    "pdf" -> exportToPdf()
                    "xlsx" -> exportToExcel()
                    "csv" -> exportToCsv()
                    else -> {
                        _exportStatus.value = ExportStatus.Error("Formato no soportado")
                    }
                }
            } catch (e: Exception) {
                _exportStatus.value = ExportStatus.Error(e.message ?: "Error al exportar")
            }
        }
    }

    private suspend fun exportToPdf() {
        // TODO: Implementar exportación a PDF
        _exportStatus.value = ExportStatus.Success("Reporte exportado a PDF")
    }

    private suspend fun exportToExcel() {
        // TODO: Implementar exportación a Excel
        _exportStatus.value = ExportStatus.Success("Datos exportados a Excel")
    }

    private suspend fun exportToCsv() {
        // TODO: Implementar exportación a CSV
        _exportStatus.value = ExportStatus.Success("Datos exportados a CSV")
    }

    fun clearError() {
        _errorMessage.value = null
        _exportStatus.value = ExportStatus.Idle
    }

    // Funciones de análisis y cálculos
    fun getCurrentMonthComparison(): String {
        val currentStats = (_dashboardStats.value as? UiState.Success)?.data?.monthlyComparison
        return if (currentStats != null) {
            val change = currentStats.percentageChange
            when {
                change > 0 -> "+${String.format("%.1f", change)}% vs mes anterior"
                change < 0 -> "${String.format("%.1f", change)}% vs mes anterior"
                else -> "Sin cambios vs mes anterior"
            }
        } else {
            "Datos no disponibles"
        }
    }

    fun getTopPaymentMethods(): List<PaymentMethodStats> {
        return (_dashboardStats.value as? UiState.Success)?.data?.paymentMethodDistribution
            ?: emptyList()
    }

    fun getRecentActivitySummary(): String {
        val recentActivity = (_dashboardStats.value as? UiState.Success)?.data?.recentActivity ?: emptyList()
        return when {
            recentActivity.isEmpty() -> "Sin actividad reciente"
            recentActivity.size == 1 -> "1 actividad reciente"
            else -> "${recentActivity.size} actividades recientes"
        }
    }

    // Funciones para análisis avanzado
    fun getEmployeePerformanceRanking(): List<Employee> {
        return _employees.value.sortedByDescending { employee ->
            // Ordenar por algún criterio de rendimiento
            // Por ahora, por salario base
            employee.baseSalary
        }
    }

    fun getMonthlyTrend(months: Int = 6): List<MonthlyStats> {
        // TODO: Implementar tendencia de múltiples meses
        return emptyList()
    }

    fun exportStatisticsText(): String {
        val stats = (_dashboardStats.value as? UiState.Success)?.data
        return if (stats != null) {
            buildString {
                appendLine("=== ESTADÍSTICAS IEPIN PERSONAL ===")
                appendLine("Fecha: ${Date()}")
                appendLine()
                appendLine("RESUMEN GENERAL:")
                appendLine("Total empleados: ${stats.totalEmployees}")
                appendLine("Pendiente por pagar: ${stats.totalPendingAmount}")
                appendLine("Pagos del mes: ${stats.currentMonthPayments}")
                appendLine("Pagos hoy: ${stats.todayPayments}")
                appendLine()
                appendLine("COMPARACIÓN MENSUAL:")
                appendLine("Cambio vs mes anterior: ${String.format("%.1f", stats.monthlyComparison.percentageChange)}%")
                appendLine()
                appendLine("MÉTODOS DE PAGO:")
                stats.paymentMethodDistribution.forEach { method ->
                    appendLine("${method.method.displayName}: ${String.format("%.1f", method.percentage)}% (${method.count} pagos)")
                }
                appendLine()
                appendLine("ACTIVIDAD RECIENTE:")
                stats.recentActivity.take(5).forEach { activity ->
                    appendLine("- ${activity.title}: ${activity.description}")
                }
            }
        } else {
            "No hay datos disponibles para exportar"
        }
    }

    // Funciones para filtros y búsquedas
    fun filterEmployeesByPerformance(criteria: PerformanceCriteria): List<Employee> {
        return when (criteria) {
            PerformanceCriteria.HIGH_SALARY -> _employees.value.filter { it.baseSalary > 2000.0 }
            PerformanceCriteria.RECENT_HIRES -> _employees.value.filter {
                val threeMonthsAgo = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -3)
                }.timeInMillis
                it.startDate > threeMonthsAgo
            }
            PerformanceCriteria.ACTIVE -> _employees.value.filter { it.isActive }
            PerformanceCriteria.ALL -> _employees.value
        }
    }

    fun getPaymentTrendForEmployee(employeeId: String, months: Int = 6): List<Double> {
        // TODO: Implementar tendencia de pagos por empleado
        return emptyList()
    }

    fun getStatisticsSummary(): StatisticsSummary {
        val stats = (_dashboardStats.value as? UiState.Success)?.data
        return if (stats != null) {
            StatisticsSummary(
                totalEmployees = stats.totalEmployees,
                totalPaymentsThisMonth = stats.currentMonthPayments,
                totalPending = stats.totalPendingAmount,
                paymentsToday = stats.todayPayments,
                monthlyGrowth = stats.monthlyComparison.percentageChange,
                averagePayment = if (stats.totalEmployees > 0)
                    stats.currentMonthPayments / stats.totalEmployees else 0.0,
                topPaymentMethod = stats.paymentMethodDistribution.maxByOrNull { it.totalAmount }?.method?.displayName ?: "N/A"
            )
        } else {
            StatisticsSummary()
        }
    }
}