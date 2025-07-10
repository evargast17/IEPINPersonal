package com.e17kapps.iepinpersonal.domain.model

data class Payment(
    val id: String = "",
    val employeeId: String = "",
    val employeeName: String = "",
    val amount: Double = 0.0,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentPeriod: PaymentPeriod = PaymentPeriod(),
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val bankDetails: BankDetails? = null,
    val digitalWalletDetails: DigitalWalletDetails? = null,
    val discounts: List<Discount> = emptyList(),
    val advances: List<Advance> = emptyList(),
    val notes: String = "",
    val status: PaymentStatus = PaymentStatus.COMPLETED,
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = ""
) {
    val totalDiscounts: Double
        get() = discounts.sumOf { it.amount }

    val totalAdvances: Double
        get() = advances.sumOf { it.amount }

    val netAmount: Double
        get() = amount - totalDiscounts - totalAdvances
}

data class PaymentPeriod(
    val month: Int = 0,
    val year: Int = 0,
    val description: String = ""
) {
    fun getDisplayText(): String = "$description $month/$year"
}

enum class PaymentMethod(val displayName: String) {
    CASH("Efectivo"),
    BANK_TRANSFER("Transferencia Bancaria"),
    YAPE("YAPE"),
    PLIN("PLIN"),
    OTHER_DIGITAL("Otra Billetera Digital")
}

data class BankDetails(
    val bankName: String = "",
    val accountNumber: String = "",
    val operationNumber: String = "",
    val transferDate: Long = System.currentTimeMillis()
)

data class DigitalWalletDetails(
    val walletType: PaymentMethod = PaymentMethod.YAPE,
    val phoneNumber: String = "",
    val operationNumber: String = "",
    val transactionId: String = ""
)

enum class PaymentStatus(val displayName: String) {
    PENDING("Pendiente"),
    COMPLETED("Completado"),
    CANCELLED("Cancelado"),
    FAILED("Fallido")
}