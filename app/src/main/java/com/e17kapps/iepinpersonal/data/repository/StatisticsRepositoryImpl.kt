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
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
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

    override suspend fun getDashboardStatistics(): Result<DashboardStatistics> {
        return try {
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

            // Actividad reciente simplificada
            val recentActivity = getRecentActivitySimplified().getOrElse { emptyList() }

            // Distribución de métodos de pago
            val paymentMethodDistribution = getPaymentMethodDistribution(currentMonth, currentYear)
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

            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDashboardStatisticsFlow(): Flow<DashboardStatistics> = callbackFlow {
        try {
            // Combinar los flows de empleados y pagos para actualizar automáticamente
            val employeesFlow = employeeRepository.getEmployeesFlow()
            val paymentsFlow = paymentRepository.getPaymentsFlow()

            val combinedFlow = combine(employeesFlow, paymentsFlow) { employees, payments ->
                calculateStatisticsFromData(employees, payments)
            }

            // Enviar estadísticas iniciales rápidamente
            val quickStats = getQuickStatistics()
            trySend(quickStats)

            // Observar cambios en tiempo real
            repositoryScope.launch {
                combinedFlow.collect { stats ->
                    trySend(stats)
                }
            }

        } catch (e: Exception) {
            trySend(DashboardStatistics())
        }

        awaitClose {
            // Cleanup si es necesario
        }
    }

    // Método optimizado para cálculos rápidos desde datos en memoria
    private fun calculateStatisticsFromData(employees: List<Employee>, payments: List<Payment>): DashboardStatistics {
        val currentDate = Calendar.getInstance()
        val currentMonth = currentDate.get(Calendar.MONTH) + 1
        val currentYear = currentDate.get(Calendar.YEAR)

        val activeEmployees = employees.filter { it.isActive }
        val totalEmployees = activeEmployees.size

        val todayStart = getStartOfDay()
        val todayEnd = getEndOfDay()
        val todayPayments = payments.count { payment ->
            payment.paymentDate >= todayStart && payment.paymentDate <= todayEnd
        }

        val monthStart = getStartOfMonth(currentMonth, currentYear)
        val monthEnd = getEndOfMonth(currentMonth, currentYear)
        val monthlyPaymentsList = payments.filter { payment ->
            payment.paymentDate >= monthStart &&
                    payment.paymentDate <= monthEnd &&
                    payment.status == PaymentStatus.COMPLETED
        }
        val monthlyPayments = monthlyPaymentsList.sumOf { it.amount }

        // Calcular pagos pendientes de forma eficiente
        var pendingAmount = 0.0
        activeEmployees.forEach { employee ->
            val hasPaymentThisMonth = monthlyPaymentsList.any { payment ->
                payment.employeeId == employee.id
            }
            if (!hasPaymentThisMonth) {
                pendingAmount += employee.baseSalary
            }
        }

        // Comparación mensual
        val previousMonth = if (currentMonth == 1) 12 else currentMonth - 1
        val previousYear = if (currentMonth == 1) currentYear - 1 else currentYear
        val prevMonthStart = getStartOfMonth(previousMonth, previousYear)
        val prevMonthEnd = getEndOfMonth(previousMonth, previousYear)

        val previousMonthPayments = payments.filter { payment ->
            payment.paymentDate >= prevMonthStart &&
                    payment.paymentDate <= prevMonthEnd &&
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
                totalPayments = monthlyPayments
            ),
            previousMonth = MonthlyStats(
                month = previousMonth,
                year = previousYear,
                totalPayments = previousMonthPayments
            ),
            percentageChange = percentageChange
        )

        // Distribución de métodos de pago (simplificada)
        val totalMonthlyAmount = monthlyPaymentsList.sumOf { it.amount }
        val paymentMethodDistribution = if (totalMonthlyAmount > 0) {
            PaymentMethod.entries.mapNotNull { method ->
                val methodPayments = monthlyPaymentsList.filter { it.paymentMethod == method }
                if (methodPayments.isNotEmpty()) {
                    val methodAmount = methodPayments.sumOf { it.amount }
                    val percentage = (methodAmount / totalMonthlyAmount) * 100

                    PaymentMethodStats(
                        method = method,
                        count = methodPayments.size,
                        totalAmount = methodAmount,
                        percentage = percentage
                    )
                } else null
            }
        } else emptyList()

        // Actividad reciente (simplificada - solo últimos 3 pagos)
        val recentActivity = payments
            .sortedByDescending { it.paymentDate }
            .take(3)
            .map { payment ->
                ActivityItem(
                    id = payment.id,
                    type = ActivityType.PAYMENT,
                    title = "Pago realizado",
                    description = "Pago a ${payment.employeeName}",
                    amount = payment.amount,
                    employeeName = payment.employeeName,
                    timestamp = payment.paymentDate,
                    icon = "💰"
                )
            }

        return DashboardStatistics(
            totalPendingAmount = pendingAmount,
            currentMonthPayments = monthlyPayments,
            totalEmployees = totalEmployees,
            todayPayments = todayPayments,
            recentActivity = recentActivity,
            monthlyComparison = monthlyComparison,
            paymentMethodDistribution = paymentMethodDistribution
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

    override suspend fun getEmployeeStatistics(employeeId: String): Result<EmployeeStatistics> {
        return try {
            val employee = employeeRepository.getEmployee(employeeId).getOrThrow()
            val payments = paymentRepository.getPaymentsByEmployee(employeeId).getOrElse { emptyList() }

            val totalPayments = payments.filter { it.status == PaymentStatus.COMPLETED }.sumOf { it.amount }
            val totalDiscounts = payments.sumOf { it.totalDiscounts }
            val totalAdvances = payments.sumOf { it.totalAdvances }
            val lastPaymentDate = payments.maxOfOrNull { it.paymentDate } ?: 0L

            // Calcular monto pendiente
            val pendingAmount = if (payments.isNotEmpty()) {
                val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val monthlyPayments = payments.filter { payment ->
                    val paymentCalendar = Calendar.getInstance().apply { timeInMillis = payment.paymentDate }
                    paymentCalendar.get(Calendar.MONTH) + 1 == currentMonth &&
                            paymentCalendar.get(Calendar.YEAR) == currentYear
                }
                if (monthlyPayments.isEmpty()) employee.baseSalary else 0.0
            } else {
                employee.baseSalary
            }

            val paymentHistory = payments.map { payment ->
                val calendar = Calendar.getInstance().apply { timeInMillis = payment.paymentDate }
                MonthlyPayment(
                    month = calendar.get(Calendar.MONTH) + 1,
                    year = calendar.get(Calendar.YEAR),
                    amount = payment.amount,
                    paymentDate = payment.paymentDate,
                    status = payment.status
                )
            }

            val statistics = EmployeeStatistics(
                employeeId = employeeId,
                employeeName = employee.fullName,
                totalPayments = totalPayments,
                totalDiscounts = totalDiscounts,
                totalAdvances = totalAdvances,
                lastPaymentDate = lastPaymentDate,
                pendingAmount = pendingAmount,
                paymentHistory = paymentHistory
            )

            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMonthlyStats(month: Int, year: Int): Result<MonthlyStats> {
        return try {
            val payments = paymentRepository.getPaymentsByMonth(month, year).getOrElse { emptyList() }
            val completedPayments = payments.filter { it.status == PaymentStatus.COMPLETED }

            val totalPayments = completedPayments.sumOf { it.amount }
            val totalDiscounts = completedPayments.sumOf { it.totalDiscounts }
            val totalAdvances = completedPayments.sumOf { it.totalAdvances }
            val paymentCount = completedPayments.size
            val averagePayment = if (paymentCount > 0) totalPayments / paymentCount else 0.0

            val monthlyStats = MonthlyStats(
                month = month,
                year = year,
                totalPayments = totalPayments,
                totalDiscounts = totalDiscounts,
                totalAdvances = totalAdvances,
                paymentCount = paymentCount,
                averagePayment = averagePayment
            )

            Result.success(monthlyStats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDashboardStats(): Result<Unit> {
        return try {
            val stats = getDashboardStatistics().getOrThrow()

            // Guardar estadísticas en Firestore para cache
            firestore.collection(FirebaseConfig.STATISTICS_COLLECTION)
                .document(FirebaseConfig.DASHBOARD_STATS_DOC)
                .set(mapOf(
                    "totalPendingAmount" to stats.totalPendingAmount,
                    "currentMonthPayments" to stats.currentMonthPayments,
                    "totalEmployees" to stats.totalEmployees,
                    "todayPayments" to stats.todayPayments,
                    "lastUpdated" to System.currentTimeMillis()
                ))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun calculatePendingPayments(): Result<Double> {
        return try {
            val allEmployees = employeeRepository.getAllEmployees().getOrElse { emptyList() }
            val activeEmployees = allEmployees.filter { it.isActive }

            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)

            var totalPending = 0.0

            activeEmployees.forEach { employee ->
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

            Result.success(totalPending)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun calculateMonthlyPayments(month: Int, year: Int): Result<Double> {
        return paymentRepository.calculateMonthlyTotal(month, year)
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

    private suspend fun getRecentActivitySimplified(): Result<List<ActivityItem>> {
        return try {
            // Simplificado para mejor performance
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getPaymentMethodDistribution(month: Int, year: Int): Result<List<PaymentMethodStats>> {
        return try {
            val payments = paymentRepository.getPaymentsByMonth(month, year).getOrElse { emptyList() }
            val completedPayments = payments.filter { it.status == PaymentStatus.COMPLETED }

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
            Result.failure(e)
        }
    }
}