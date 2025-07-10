package com.e17kapps.iepinpersonal.domain.model

data class Discount(
    val id: String = "",
    val employeeId: String = "",
    val employeeName: String = "",
    val amount: Double = 0.0,
    val type: DiscountType = DiscountType.OTHER,
    val reason: String = "",
    val description: String = "",
    val isRecurring: Boolean = false,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val isActive: Boolean = true,
    val appliedInPaymentId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = ""
)

enum class DiscountType(val displayName: String) {
    TARDINESS("Tardanzas"),
    ABSENCE("Faltas"),
    LOAN_PAYMENT("Pago de Préstamo"),
    ADVANCE_DEDUCTION("Descuento por Adelanto"),
    UNIFORM("Uniforme"),
    EQUIPMENT("Equipos"),
    INSURANCE("Seguro"),
    OTHER("Otro")
}