package com.e17kapps.iepinpersonal.domain.repository

import com.e17kapps.iepinpersonal.domain.model.DashboardStatistics
import com.e17kapps.iepinpersonal.domain.model.EmployeeStatistics
import com.e17kapps.iepinpersonal.domain.model.MonthlyStats
import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    suspend fun getDashboardStatistics(): Result<DashboardStatistics>
    fun getDashboardStatisticsFlow(): Flow<DashboardStatistics>
    suspend fun getEmployeeStatistics(employeeId: String): Result<EmployeeStatistics>
    suspend fun getMonthlyStats(month: Int, year: Int): Result<MonthlyStats>
    suspend fun updateDashboardStats(): Result<Unit>
    suspend fun calculatePendingPayments(): Result<Double>
    suspend fun calculateMonthlyPayments(month: Int, year: Int): Result<Double>
    suspend fun getTotalEmployeesCount(): Result<Int>
    suspend fun getTodayPaymentsCount(): Result<Int>
}