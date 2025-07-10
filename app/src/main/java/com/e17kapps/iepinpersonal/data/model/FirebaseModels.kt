package com.e17kapps.iepinpersonal.data.model

import com.e17kapps.iepinpersonal.domain.model.*

// Firebase User Document
data class UserDocument(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "USER",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): User = User(
        id = id,
        email = email,
        name = name,
        role = UserRole.valueOf(role),
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(user: User): UserDocument = UserDocument(
            id = user.id,
            email = user.email,
            name = user.name,
            role = user.role.name,
            isActive = user.isActive,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }
}

// Firebase Employee Document
data class EmployeeDocument(
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
    val emergencyContact: Map<String, String>? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = ""
) {
    fun toDomain(): Employee = Employee(
        id = id,
        dni = dni,
        name = name,
        lastName = lastName,
        position = position,
        baseSalary = baseSalary,
        phone = phone,
        address = address,
        email = email,
        startDate = startDate,
        isActive = isActive,
        bankAccount = bankAccount,
        emergencyContact = emergencyContact?.let {
            EmergencyContact(
                name = it["name"] ?: "",
                phone = it["phone"] ?: "",
                relationship = it["relationship"] ?: ""
            )
        },
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
        createdBy = createdBy
    )

    companion object {
        fun fromDomain(employee: Employee): EmployeeDocument = EmployeeDocument(
            id = employee.id,
            dni = employee.dni,
            name = employee.name,
            lastName = employee.lastName,
            position = employee.position,
            baseSalary = employee.baseSalary,
            phone = employee.phone,
            address = employee.address,
            email = employee.email,
            startDate = employee.startDate,
            isActive = employee.isActive,
            bankAccount = employee.bankAccount,
            emergencyContact = employee.emergencyContact?.let {
                mapOf(
                    "name" to it.name,
                    "phone" to it.phone,
                    "relationship" to it.relationship
                )
            },
            notes = employee.notes,
            createdAt = employee.createdAt,
            updatedAt = employee.updatedAt,
            createdBy = employee.createdBy
        )
    }
}

// Firebase Payment Document
data class PaymentDocument(
    val id: String = "",
    val employeeId: String = "",
    val employeeName: String = "",
    val amount: Double = 0.0,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentPeriod: Map<String, Any> = emptyMap(),
    val paymentMethod: String = "CASH",
    val bankDetails: Map<String, Any>? = null,
    val digitalWalletDetails: Map<String, Any>? = null,
    val discounts: List<Map<String, Any>> = emptyList(),
    val advances: List<Map<String, Any>> = emptyList(),
    val notes: String = "",
    val status: String = "COMPLETED",
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = ""
) {
    companion object {
        fun fromDomain(payment: Payment): PaymentDocument = PaymentDocument(
            id = payment.id,
            employeeId = payment.employeeId,
            employeeName = payment.employeeName,
            amount = payment.amount,
            paymentDate = payment.paymentDate,
            paymentPeriod = mapOf(
                "month" to payment.paymentPeriod.month,
                "year" to payment.paymentPeriod.year,
                "description" to payment.paymentPeriod.description
            ),
            paymentMethod = payment.paymentMethod.name,
            bankDetails = payment.bankDetails?.let {
                mapOf(
                    "bankName" to it.bankName,
                    "accountNumber" to it.accountNumber,
                    "operationNumber" to it.operationNumber,
                    "transferDate" to it.transferDate
                )
            },
            digitalWalletDetails = payment.digitalWalletDetails?.let {
                mapOf(
                    "walletType" to it.walletType.name,
                    "phoneNumber" to it.phoneNumber,
                    "operationNumber" to it.operationNumber,
                    "transactionId" to it.transactionId
                )
            },
            notes = payment.notes,
            status = payment.status.name,
            createdAt = payment.createdAt,
            createdBy = payment.createdBy
        )
    }
}