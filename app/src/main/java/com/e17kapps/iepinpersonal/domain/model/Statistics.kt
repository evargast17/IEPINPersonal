package com.e17kapps.iepinpersonal.domain.model

data class DashboardStatistics(
    val totalPendingAmount: Double = 0.0,
    val currentMonthPayments: Double = 0.0,
    val totalEmployees: Int = 0,
    val todayPayments: Int = 0,
    val recentActivity: List<ActivityItem> = emptyList(),
    val monthlyComparison: MonthlyComparison = MonthlyComparison(),
    val paymentMethodDistribution: List<PaymentMethodStats> = emptyList()
)

data class MonthlyComparison(
    val currentMonth: MonthlyStats = MonthlyStats(),
    val previousMonth: MonthlyStats = MonthlyStats(),
    val percentageChange: Double = 0.0
)

data class MonthlyStats(
    val month: Int = 0,
    val year: Int = 0,
    val totalPayments: Double = 0.0,
    val totalDiscounts: Double = 0.0,
    val totalAdvances: Double = 0.0,
    val paymentCount: Int = 0,
    val averagePayment: Double = 0.0
)

data class PaymentMethodStats(
    val method: PaymentMethod = PaymentMethod.CASH,
    val count: Int = 0,
    val totalAmount: Double = 0.0,
    val percentage: Double = 0.0
)

data class ActivityItem(
    val id: String = "",
    val type: ActivityType = ActivityType.PAYMENT,
    val title: String = "",
    val description: String = "",
    val amount: Double? = null,
    val employeeName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val icon: String = ""
)

enum class ActivityType(val displayName: String, val icon: String) {
    PAYMENT("Pago Realizado", "💰"),
    EMPLOYEE_ADDED("Empleado Agregado", "👥"),
    DISCOUNT_APPLIED("Descuento Aplicado", "📉"),
    ADVANCE_REQUESTED("Adelanto Solicitado", "💳"),
    ADVANCE_APPROVED("Adelanto Aprobado", "✅"),
    EMPLOYEE_UPDATED("Empleado Actualizado", "✏️")
}

data class EmployeeStatistics(
    val employeeId: String = "",
    val employeeName: String = "",
    val totalPayments: Double = 0.0,
    val totalDiscounts: Double = 0.0,
    val totalAdvances: Double = 0.0,
    val lastPaymentDate: Long = 0,
    val pendingAmount: Double = 0.0,
    val paymentHistory: List<MonthlyPayment> = emptyList(),
    val advanceHistory: List<Advance> = emptyList(),
    val discountHistory: List<Discount> = emptyList()
)

data class MonthlyPayment(
    val month: Int = 0,
    val year: Int = 0,
    val amount: Double = 0.0,
    val paymentDate: Long = 0,
    val status: PaymentStatus = PaymentStatus.COMPLETED
)