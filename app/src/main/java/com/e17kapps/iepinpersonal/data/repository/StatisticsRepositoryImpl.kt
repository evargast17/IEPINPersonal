package com.e17kapps.iepinpersonal.data.repository

import com.e17kapps.iepinpersonal.data.remote.FirebaseConfig
import com.e17kapps.iepinpersonal.domain.model.*
import com.e17kapps.iepinpersonal.domain.repository.*
import com.e17kapps.iepinpersonal.utils.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepositoryImpl @Inject constructor(
    private val firebaseConfig: FirebaseConfig,
    private val paymentRepository: PaymentRepository,
    private val employeeRepository: EmployeeRepository
) : StatisticsRepository {

    private val firestore: FirebaseFirestore = firebaseConfig.firestore
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Cache en memoria para evitar consultas repetidas
    private val _cachedStatistics = MutableStateFlow<DashboardStatistics?>(null)
    private val cachedStatistics: StateFlow<DashboardStatistics?> = _cachedStatistics.asStateFlow()

    private var lastCacheUpdate: Long = 0
    private val cacheValidityDuration = 5 * 60 * 1000L // 5 minutos

    override suspend fun getDashboardStatistics(): Result<DashboardStatistics> {
        return try {
            // Verificar si tenemos datos en cache válidos
            val currentTime = System.currentTimeMillis()
            val cachedData = _cachedStatistics.value

            if (cachedData != null && (currentTime - lastCacheUpdate) < cacheValidityDuration) {
                return Result.success(cachedData)
            }

            val currentDate = Calendar.getInstance()
            val currentMonth = currentDate.get(Calendar.MONTH) + 1
            val currentYear = currentDate.get(Calendar.YEAR)

            // Ejecutar consultas en paralelo para mejor performance
            val pendingAmountDeferred = repositoryScope.async { calculatePendingPayments().getOrElse { 0.0 } }
            val monthlyPaymentsDeferred = repositoryScope.async { calculateMonthlyPayments(currentMonth, currentYear).getOrElse { 0.0 } }
            val totalEmployeesDeferred = repositoryScope.async { getTotalEmployeesCount().getOrElse { 0 } }
            val todayPaymentsDeferred = repositoryScope.async { getTodayPaymentsCount().getOrElse { 0 } }

            // Esperar todos los resultados
            val pendingAmount = pendingAmountDeferred.await()
            val monthlyPayments = monthlyPaymentsDeferred.await()
            val totalEmployees = totalEmployeesDeferred.await()
            val todayPayments = todayPaymentsDeferred.await()

            // Comparación mensual (también en paralelo)
            val previousMonth = if (currentMonth == 1) 12 else currentMonth - 1
            val previousYear = if (currentMonth == 1) currentYear - 1 else currentYear
            val previousMonthPayments = calculateMonthlyPayments(previousMonth, previousYear).getOrElse { 0.0 }

            val percentageChange = if (previousMonthPayments > 0) {
                ((monthlyPayments - previousMonthPayments) / previousMonthPayments) * 100
            } else if (monthlyPayments > 0) {
                100.0
            } else {
                0.0
            }

            val monthlyComparison = MonthlyComparison(
                currentMonth = MonthlyStats(
                    month = currentMonth,
                    year = currentYear,
                    totalPayments = monthlyPayments
                ),
                previousMonth = MonthlyStats(
                    month = previousMonth,
                    year = previousYear,
                    totalPayments = previousMonthPayments
                ),
                percentageChange = percentageChange
            )

            // Actividad reciente simplificada para mejor rendimiento
            val recentActivity = getRecentActivityOptimized().getOrElse { emptyList() }

            // Distribución de métodos de pago optimizada
            val paymentMethodDistribution = getPaymentMethodDistributionOptimized(currentMonth, currentYear)
                .getOrElse { emptyList() }

            val statistics = DashboardStatistics(
                totalPendingAmount = pendingAmount,
                currentMonthPayments = monthlyPayments,
                totalEmployees = totalEmployees,
                todayPayments = todayPayments,
                recentActivity = recentActivity,
                monthlyComparison = monthlyComparison,
                paymentMethodDistribution = paymentMethodDistribution
            )

            // Actualizar cache
            _cachedStatistics.value = statistics
            lastCacheUpdate = currentTime

            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDashboardStatisticsFlow(): Flow<DashboardStatistics> = callbackFlow {
        try {
            // Enviar datos en cache inmediatamente si están disponibles
            _cachedStatistics.value?.let { cachedData ->
                trySend(cachedData)
            }

            // Combinar flows de forma más eficiente
            val employeesFlow = employeeRepository.getEmployeesFlow()
                .distinctUntilChanged { old, new -> old.size == new.size }

            val paymentsFlow = paymentRepository.getPaymentsFlow()
                .distinctUntilChanged { old, new -> old.size == new.size }

            val combinedFlow = combine(employeesFlow, paymentsFlow) { employees, payments ->
                calculateStatisticsFromDataOptimized(employees, payments)
            }

            // Enviar estadísticas iniciales rápidamente
            val quickStats = getQuickStatistics()
            trySend(quickStats)

            // Observar cambios en tiempo real
            repositoryScope.launch {
                combinedFlow.collect { stats ->
                    // Actualizar cache
                    _cachedStatistics.value = stats
                    lastCacheUpdate = System.currentTimeMillis()
                    trySend(stats)
                }
            }

        } catch (e: Exception) {
            // Enviar datos básicos en caso de error
            trySend(DashboardStatistics())
        }

        awaitClose {
            // Cleanup automático
        }
    }

    override suspend fun calculatePendingPayments(): Result<Double> {
        return try {
            // Cálculo optimizado usando consultas directas limitadas
            val allEmployees = employeeRepository.getAllEmployees().getOrElse { emptyList() }
            val activeEmployees = allEmployees.filter { it.isActive }

            if (activeEmployees.isEmpty()) {
                return Result.success(0.0)
            }

            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)

            var totalPending = 0.0

            // Procesar empleados en lotes para mejor rendimiento
            val batchSize = 10
            for (employeeBatch in activeEmployees.chunked(batchSize)) {
                employeeBatch.forEach { employee ->
                    val monthlyPayments = paymentRepository.getPaymentsByEmployee(employee.id)
                        .getOrElse { emptyList() }
                        .filter { payment ->
                            val paymentCalendar = Calendar.getInstance().apply { timeInMillis = payment.paymentDate }
                            paymentCalendar.get(Calendar.MONTH) + 1 == currentMonth &&
                                    paymentCalendar.get(Calendar.YEAR) == currentYear &&
                                    payment.status == PaymentStatus.COMPLETED
                        }

                    if (monthlyPayments.isEmpty()) {
                        totalPending += employee.baseSalary
                    }
                }
            }

            Result.success(totalPending)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun calculateMonthlyPayments(month: Int, year: Int): Result<Double> {
        return try {
            // Usar el método del repository de pagos que ya está optimizado
            paymentRepository.calculateMonthlyTotal(month, year)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTotalEmployeesCount(): Result<Int> {
        return try {
            val allEmployees = employeeRepository.getAllEmployees().getOrElse { emptyList() }
            val activeEmployees = allEmployees.filter { it.isActive }
            Result.success(activeEmployees.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTodayPaymentsCount(): Result<Int> {
        return try {
            val todayPayments = paymentRepository.getTodayPayments().getOrElse { emptyList() }
            Result.success(todayPayments.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEmployeeStatistics(employeeId: String): Result<EmployeeStatistics> {
        return try {
            val employee = employeeRepository.getEmployee(employeeId).getOrThrow()
            val payments = paymentRepository.getPaymentsByEmployee(employeeId).getOrElse { emptyList() }

            val totalPayments = payments.filter { it.status == PaymentStatus.COMPLETED }.sumOf { it.amount }
            val totalDiscounts = payments.sumOf { it.totalDiscounts }
            val totalAdvances = payments.sumOf { it.totalAdvances }
            val lastPaymentDate = payments.maxOfOrNull { it.paymentDate }

            val statistics = EmployeeStatistics(
                employeeId = employeeId,
                employeeName = employee.name + " " + employee.lastName,
                totalPayments = totalPayments,
                totalDiscounts = totalDiscounts,
                totalAdvances = totalAdvances,
                lastPaymentDate = lastPaymentDate,
                averageMonthlyPayment = if (payments.isNotEmpty()) totalPayments / payments.size else 0.0,
                paymentHistory = payments.takeLast(10) // Solo los últimos 10 para rendimiento
            )

            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMonthlyStats(month: Int, year: Int): Result<MonthlyStats> {
        return try {
            val totalPayments = calculateMonthlyPayments(month, year).getOrElse { 0.0 }

            // Obtener count de pagos del mes de forma optimizada
            val payments = paymentRepository.getPaymentsByMonth(month, year).getOrElse { emptyList() }
            val completedPayments = payments.filter { it.status == PaymentStatus.COMPLETED }

            val stats = MonthlyStats(
                month = month,
                year = year,
                totalPayments = totalPayments,
                paymentCount = completedPayments.size
            )

            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDashboardStats(): Result<Unit> {
        return try {
            // Forzar actualización del cache
            clearCache()
            getDashboardStatistics()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Métodos privados optimizados
    private suspend fun getRecentActivityOptimized(): Result<List<ActivityItem>> {
        return try {
            // Obtener solo los últimos 5 pagos para mejor rendimiento
            val recentPayments = paymentRepository.getTodayPayments().getOrElse { emptyList() }
                .sortedByDescending { it.paymentDate }
                .take(5)

            val recentActivity = recentPayments.map { payment ->
                ActivityItem(
                    id = payment.id,
                    type = ActivityType.PAYMENT,
                    description = "Pago a ${payment.employeeName}",
                    amount = payment.amount,
                    employeeName = payment.employeeName,
                    timestamp = payment.paymentDate,
                    icon = "💰"
                )
            }

            Result.success(recentActivity)
        } catch (e: Exception) {
            Result.success(emptyList()) // Fallar silenciosamente para no bloquear el dashboard
        }
    }

    private suspend fun getPaymentMethodDistributionOptimized(month: Int, year: Int): Result<List<PaymentMethodStats>> {
        return try {
            val payments = paymentRepository.getPaymentsByMonth(month, year).getOrElse { emptyList() }
            val completedPayments = payments.filter { it.status == PaymentStatus.COMPLETED }

            if (completedPayments.isEmpty()) {
                return Result.success(emptyList())
            }

            val totalAmount = completedPayments.sumOf { it.amount }

            val distribution = PaymentMethod.entries.mapNotNull { method ->
                val methodPayments = completedPayments.filter { it.paymentMethod == method }
                if (methodPayments.isNotEmpty()) {
                    val methodAmount = methodPayments.sumOf { it.amount }
                    val percentage = if (totalAmount > 0) (methodAmount / totalAmount) * 100 else 0.0

                    PaymentMethodStats(
                        method = method,
                        count = methodPayments.size,
                        totalAmount = methodAmount,
                        percentage = percentage
                    )
                } else null
            }

            Result.success(distribution)
        } catch (e: Exception) {
            Result.success(emptyList()) // Fallar silenciosamente
        }
    }

    // Método optimizado para cálculos rápidos desde datos en memoria
    private fun calculateStatisticsFromDataOptimized(employees: List<Employee>, payments: List<Payment>): DashboardStatistics {
        val currentDate = Calendar.getInstance()
        val currentMonth = currentDate.get(Calendar.MONTH) + 1
        val currentYear = currentDate.get(Calendar.YEAR)

        val activeEmployees = employees.filter { it.isActive }
        val totalEmployees = activeEmployees.size

        // Cálculos optimizados usando operaciones de colección más eficientes
        val todayStart = getStartOfDay()
        val todayEnd = getEndOfDay()
        val todayPayments = payments.count { payment ->
            payment.paymentDate in todayStart..todayEnd
        }

        val monthStart = getStartOfMonth(currentMonth, currentYear)
        val monthEnd = getEndOfMonth(currentMonth, currentYear)

        val monthlyPaymentsList = payments.filter { payment ->
            payment.paymentDate in monthStart..monthEnd &&
                    payment.status == PaymentStatus.COMPLETED
        }

        val monthlyPayments = monthlyPaymentsList.sumOf { it.amount }

        // Cálculo de pendientes optimizado
        val employeesWithPayments = monthlyPaymentsList.map { it.employeeId }.toSet()
        val pendingAmount = activeEmployees
            .filterNot { it.id in employeesWithPayments }
            .sumOf { it.baseSalary }

        // Comparación mensual simplificada
        val previousMonth = if (currentMonth == 1) 12 else currentMonth - 1
        val previousYear = if (currentMonth == 1) currentYear - 1 else currentYear
        val prevMonthStart = getStartOfMonth(previousMonth, previousYear)
        val prevMonthEnd = getEndOfMonth(previousMonth, previousYear)

        val previousMonthPayments = payments.filter { payment ->
            payment.paymentDate in prevMonthStart..prevMonthEnd &&
                    payment.status == PaymentStatus.COMPLETED
        }.sumOf { it.amount }

        val percentageChange = if (previousMonthPayments > 0) {
            ((monthlyPayments - previousMonthPayments) / previousMonthPayments) * 100
        } else if (monthlyPayments > 0) {
            100.0
        } else {
            0.0
        }

        val monthlyComparison = MonthlyComparison(
            currentMonth = MonthlyStats(
                month = currentMonth,
                year = currentYear,
                totalPayments = monthlyPayments,
                paymentCount = monthlyPaymentsList.size
            ),
            previousMonth = MonthlyStats(
                month = previousMonth,
                year = previousYear,
                totalPayments = previousMonthPayments
            ),
            percentageChange = percentageChange
        )

        return DashboardStatistics(
            totalPendingAmount = pendingAmount,
            currentMonthPayments = monthlyPayments,
            totalEmployees = totalEmployees,
            todayPayments = todayPayments,
            recentActivity = emptyList(), // Se cargan bajo demanda
            monthlyComparison = monthlyComparison,
            paymentMethodDistribution = emptyList() // Se cargan bajo demanda
        )
    }

    // Estadísticas rápidas para carga inicial
    private suspend fun getQuickStatistics(): DashboardStatistics {
        return try {
            // Solo datos básicos para carga rápida
            val totalEmployees = getTotalEmployeesCount().getOrElse { 0 }
            val todayPayments = getTodayPaymentsCount().getOrElse { 0 }

            DashboardStatistics(
                totalEmployees = totalEmployees,
                todayPayments = todayPayments,
                // Los demás valores se actualizarán con el flow
                totalPendingAmount = 0.0,
                currentMonthPayments = 0.0,
                recentActivity = emptyList(),
                monthlyComparison = MonthlyComparison(),
                paymentMethodDistribution = emptyList()
            )
        } catch (e: Exception) {
            DashboardStatistics()
        }
    }

    // Limpiar cache manualmente cuando sea necesario
    private fun clearCache() {
        _cachedStatistics.value = null
        lastCacheUpdate = 0
    }
}