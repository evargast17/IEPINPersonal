package com.e17kapps.iepinpersonal.domain.model

data class Employee(
    val id: String = "",
    val dni: String = "",
    val name: String = "",
    val lastName: String = "",
    val position: String = "",
    val baseSalary: Double = 0.0,
    val phone: String = "",
    val address: String = "",
    val email: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val bankAccount: String = "",
    val emergencyContact: EmergencyContact? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = ""
) {
    val fullName: String
        get() = "$name $lastName"

    val monthlyNet: Double
        get() = baseSalary // Aquí se pueden aplicar descuentos base
}

data class EmergencyContact(
    val name: String = "",
    val phone: String = "",
    val relationship: String = ""
)