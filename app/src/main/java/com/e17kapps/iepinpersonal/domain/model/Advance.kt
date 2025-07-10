package com.e17kapps.iepinpersonal.domain.model

data class Advance(
    val id: String = "",
    val employeeId: String = "",
    val employeeName: String = "",
    val amount: Double = 0.0,
    val requestDate: Long = System.currentTimeMillis(),
    val approvedDate: Long? = null,
    val paidDate: Long? = null,
    val reason: String = "",
    val notes: String = "",
    val status: AdvanceStatus = AdvanceStatus.PENDING,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val deductionSchedule: DeductionSchedule? = null,
    val remainingAmount: Double = 0.0,
    val isFullyDeducted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val approvedBy: String = "",
    val createdBy: String = ""
)

data class DeductionSchedule(
    val totalInstallments: Int = 1,
    val installmentAmount: Double = 0.0,
    val remainingInstallments: Int = 0,
    val startDeductionDate: Long = System.currentTimeMillis()
)

enum class AdvanceStatus(val displayName: String) {
    PENDING("Pendiente"),
    APPROVED("Aprobado"),
    PAID("Pagado"),
    REJECTED("Rechazado"),
    DEDUCTING("En Descuento"),
    COMPLETED("Completado")
}