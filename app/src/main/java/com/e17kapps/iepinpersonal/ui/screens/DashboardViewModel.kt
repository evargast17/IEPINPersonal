package com.e17kapps.iepinpersonal.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.e17kapps.iepinpersonal.domain.model.DashboardStatistics
import com.e17kapps.iepinpersonal.domain.model.DashboardUiState
import com.e17kapps.iepinpersonal.domain.model.UiState
import com.e17kapps.iepinpersonal.domain.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _statisticsState = MutableStateFlow<UiState<DashboardStatistics>>(UiState.Loading)
    val statisticsState: StateFlow<UiState<DashboardStatistics>> = _statisticsState.asStateFlow()

    init {
        loadDashboardData()
        observeStatistics()
    }

    private fun loadDashboardData() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        _statisticsState.value = UiState.Loading

        viewModelScope.launch {
            try {
                // Cargar datos rápidamente sin bloquear
                statisticsRepository.getDashboardStatistics()
                    .onSuccess { statistics ->
                        _statisticsState.value = UiState.Success(statistics)
                        _uiState.value = _uiState.value.copy(
                            statistics = statistics,
                            isLoading = false,
                            lastUpdated = System.currentTimeMillis()
                        )
                    }
                    .onFailure { exception ->
                        _statisticsState.value = UiState.Error(exception.message ?: "Error al cargar datos")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Error al cargar datos del dashboard"
                        )
                    }
            } catch (e: Exception) {
                _statisticsState.value = UiState.Error(e.message ?: "Error inesperado")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error inesperado"
                )
            }
        }
    }

    private fun observeStatistics() {
        viewModelScope.launch {
            try {
                statisticsRepository.getDashboardStatisticsFlow()
                    .catch { exception ->
                        _statisticsState.value = UiState.Error(exception.message ?: "Error en flow")
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al actualizar datos: ${exception.message}"
                        )
                    }
                    .collect { statistics ->
                        _statisticsState.value = UiState.Success(statistics)
                        _uiState.value = _uiState.value.copy(
                            statistics = statistics,
                            isLoading = false,
                            lastUpdated = System.currentTimeMillis(),
                            errorMessage = null
                        )
                    }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _statisticsState.value = UiState.Error(e.message ?: "Error inesperado")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    fun refreshData() {
        loadDashboardData()
    }

    fun updateStatistics() {
        viewModelScope.launch {
            try {
                statisticsRepository.updateDashboardStats()
                    .onSuccess {
                        // Las estadísticas se actualizarán automáticamente a través del Flow
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al actualizar estadísticas: ${exception.message}"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // Función para obtener estadísticas específicas
    fun getPendingPayments(): Double {
        return _uiState.value.statistics.totalPendingAmount
    }

    fun getCurrentMonthPayments(): Double {
        return _uiState.value.statistics.currentMonthPayments
    }

    fun getTotalEmployees(): Int {
        return _uiState.value.statistics.totalEmployees
    }

    fun getTodayPayments(): Int {
        return _uiState.value.statistics.todayPayments
    }

    fun getMonthlyGrowth(): Double {
        return _uiState.value.statistics.monthlyComparison.percentageChange
    }

    fun isDataFresh(): Boolean {
        val lastUpdate = _uiState.value.lastUpdated
        val currentTime = System.currentTimeMillis()
        val fiveMinutesInMillis = 5 * 60 * 1000
        return (currentTime - lastUpdate) < fiveMinutesInMillis
    }

    fun getDebugInfo(): String {
        val currentState = _statisticsState.value
        val currentStats = _uiState.value.statistics
        return buildString {
            appendLine("=== DASHBOARD DEBUG INFO ===")
            appendLine("Estado actual: ${currentState::class.simpleName}")
            appendLine("Total empleados: ${currentStats.totalEmployees}")
            appendLine("Pagos este mes: ${currentStats.currentMonthPayments}")
            appendLine("Pendientes: ${currentStats.totalPendingAmount}")
            appendLine("Pagos hoy: ${currentStats.todayPayments}")
            appendLine("Última actualización: ${_uiState.value.lastUpdated}")
            appendLine("¿Datos frescos?: ${isDataFresh()}")
            appendLine("============================")
        }
    }

    fun forceRefresh() {
        // Método para forzar actualización completa
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        _statisticsState.value = UiState.Loading
        loadDashboardData()
    }
}