package com.e17kapps.iepinpersonal.domain.repository

import com.e17kapps.iepinpersonal.domain.model.Payment
import com.e17kapps.iepinpersonal.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    suspend fun addPayment(payment: Payment): Result<String>
    suspend fun updatePayment(payment: Payment): Result<Unit>
    suspend fun deletePayment(paymentId: String): Result<Unit>
    suspend fun getPayment(paymentId: String): Result<Payment>
    suspend fun getPaymentsByEmployee(employeeId: String): Result<List<Payment>>
    suspend fun getPaymentsByDateRange(startDate: Long, endDate: Long): Result<List<Payment>>
    suspend fun getPaymentsByMonth(month: Int, year: Int): Result<List<Payment>>
    suspend fun getPaymentsByMethod(method: PaymentMethod): Result<List<Payment>>
    fun getPaymentsFlow(): Flow<List<Payment>>
    suspend fun getTodayPayments(): Result<List<Payment>>
    suspend fun getPendingPayments(): Result<List<Payment>>
    suspend fun calculateTotalPayments(startDate: Long, endDate: Long): Result<Double>
    suspend fun calculateMonthlyTotal(month: Int, year: Int): Result<Double>
}