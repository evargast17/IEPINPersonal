package com.e17kapps.iepinpersonal.domain.repository

import com.e17kapps.iepinpersonal.domain.model.Discount
import com.e17kapps.iepinpersonal.domain.model.DiscountType
import kotlinx.coroutines.flow.Flow

interface DiscountRepository {
    suspend fun addDiscount(discount: Discount): Result<String>
    suspend fun updateDiscount(discount: Discount): Result<Unit>
    suspend fun deleteDiscount(discountId: String): Result<Unit>
    suspend fun getDiscount(discountId: String): Result<Discount>
    suspend fun getDiscountsByEmployee(employeeId: String): Result<List<Discount>>
    suspend fun getActiveDiscounts(): Result<List<Discount>>
    suspend fun getDiscountsByType(type: DiscountType): Result<List<Discount>>
    suspend fun getDiscountsByDateRange(startDate: Long, endDate: Long): Result<List<Discount>>
    suspend fun deactivateDiscount(discountId: String): Result<Unit>
    suspend fun activateDiscount(discountId: String): Result<Unit>
    fun getDiscountsFlow(): Flow<List<Discount>>
    suspend fun getRecurringDiscounts(): Result<List<Discount>>
    suspend fun getExpiredDiscounts(): Result<List<Discount>>
    suspend fun calculateTotalDiscounts(startDate: Long, endDate: Long): Result<Double>
    suspend fun calculateEmployeeDiscounts(employeeId: String, startDate: Long, endDate: Long): Result<Double>
}