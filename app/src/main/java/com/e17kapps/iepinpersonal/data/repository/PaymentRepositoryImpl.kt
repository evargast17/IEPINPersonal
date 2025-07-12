package com.e17kapps.iepinpersonal.data.repository

import com.e17kapps.iepinpersonal.data.model.PaymentDocument
import com.e17kapps.iepinpersonal.data.remote.FirebaseConfig
import com.e17kapps.iepinpersonal.domain.model.*
import com.e17kapps.iepinpersonal.domain.repository.PaymentRepository
import com.e17kapps.iepinpersonal.utils.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val firebaseConfig: FirebaseConfig
) : PaymentRepository {

    private val firestore: FirebaseFirestore = firebaseConfig.firestore
    private val paymentsCollection = firestore.collection(FirebaseConfig.PAYMENTS_COLLECTION)

    override suspend fun addPayment(payment: Payment): Result<String> {
        return try {
            val documentRef = paymentsCollection.document()
            val paymentWithId = payment.copy(
                id = documentRef.id,
            )

            val paymentDocument = PaymentDocument.fromDomain(paymentWithId)
            documentRef.set(paymentDocument).await()

            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePayment(payment: Payment): Result<Unit> {
        return try {
            val paymentDocument = PaymentDocument.fromDomain(payment)

            paymentsCollection.document(payment.id)
                .set(paymentDocument)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePayment(paymentId: String): Result<Unit> {
        return try {
            paymentsCollection.document(paymentId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPayment(paymentId: String): Result<Payment> {
        return try {
            val document = paymentsCollection.document(paymentId)
                .get()
                .await()

            if (document.exists()) {
                val paymentDocument = document.toObject(PaymentDocument::class.java)
                if (paymentDocument != null) {
                    val payment = convertToPayment(paymentDocument)
                    Result.success(payment)
                } else {
                    Result.failure(Exception("Error al parsear datos del pago"))
                }
            } else {
                Result.failure(Exception("Pago no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPaymentsByEmployee(employeeId: String): Result<List<Payment>> {
        return try {
            val querySnapshot = paymentsCollection
                .whereEqualTo("employeeId", employeeId)
                .orderBy("paymentDate", Query.Direction.DESCENDING)
                .get()
                .await()

            val payments = querySnapshot.documents.mapNotNull { document ->
                document.toObject(PaymentDocument::class.java)?.let { convertToPayment(it) }
            }

            Result.success(payments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPaymentsByDateRange(startDate: Long, endDate: Long): Result<List<Payment>> {
        return try {
            val querySnapshot = paymentsCollection
                .whereGreaterThanOrEqualTo("paymentDate", startDate)
                .whereLessThanOrEqualTo("paymentDate", endDate)
                .orderBy("paymentDate", Query.Direction.DESCENDING)
                .get()
                .await()

            val payments = querySnapshot.documents.mapNotNull { document ->
                document.toObject(PaymentDocument::class.java)?.let { convertToPayment(it) }
            }

            Result.success(payments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPaymentsByMonth(month: Int, year: Int): Result<List<Payment>> {
        val startDate = getStartOfMonth(month, year)
        val endDate = getEndOfMonth(month, year)
        return getPaymentsByDateRange(startDate, endDate)
    }

    override suspend fun getPaymentsByMethod(method: PaymentMethod): Result<List<Payment>> {
        return try {
            val querySnapshot = paymentsCollection
                .whereEqualTo("paymentMethod", method.name)
                .orderBy("paymentDate", Query.Direction.DESCENDING)
                .get()
                .await()

            val payments = querySnapshot.documents.mapNotNull { document ->
                document.toObject(PaymentDocument::class.java)?.let { convertToPayment(it) }
            }

            Result.success(payments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getPaymentsFlow(): Flow<List<Payment>> = callbackFlow {
        val listener = paymentsCollection
            .orderBy("paymentDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val payments = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(PaymentDocument::class.java)?.let { convertToPayment(it) }
                } ?: emptyList()

                trySend(payments)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getTodayPayments(): Result<List<Payment>> {
        val startOfDay = getStartOfDay()
        val endOfDay = getEndOfDay()
        return getPaymentsByDateRange(startOfDay, endOfDay)
    }

    override suspend fun getPendingPayments(): Result<List<Payment>> {
        return try {
            val querySnapshot = paymentsCollection
                .whereEqualTo("status", PaymentStatus.PENDING.name)
                .orderBy("paymentDate", Query.Direction.ASCENDING)
                .get()
                .await()

            val payments = querySnapshot.documents.mapNotNull { document ->
                document.toObject(PaymentDocument::class.java)?.let { convertToPayment(it) }
            }

            Result.success(payments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun calculateTotalPayments(startDate: Long, endDate: Long): Result<Double> {
        return try {
            val paymentsResult = getPaymentsByDateRange(startDate, endDate)
            val payments = paymentsResult.getOrThrow()

            val total = payments
                .filter { it.status == PaymentStatus.COMPLETED }
                .sumOf { it.amount }

            Result.success(total)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun calculateMonthlyTotal(month: Int, year: Int): Result<Double> {
        val startDate = getStartOfMonth(month, year)
        val endDate = getEndOfMonth(month, year)
        return calculateTotalPayments(startDate, endDate)
    }

    private fun convertToPayment(doc: PaymentDocument): Payment {
        return Payment(
            id = doc.id,
            employeeId = doc.employeeId,
            employeeName = doc.employeeName,
            amount = doc.amount,
            paymentDate = doc.paymentDate,
            paymentPeriod = PaymentPeriod(
                month = (doc.paymentPeriod["month"] as? Long)?.toInt() ?: 0,
                year = (doc.paymentPeriod["year"] as? Long)?.toInt() ?: 0,
                description = doc.paymentPeriod["description"] as? String ?: ""
            ),
            paymentMethod = try {
                PaymentMethod.valueOf(doc.paymentMethod)
            } catch (e: Exception) {
                PaymentMethod.CASH
            },
            bankDetails = doc.bankDetails?.let { details ->
                BankDetails(
                    bankName = details["bankName"] as? String ?: "",
                    accountNumber = details["accountNumber"] as? String ?: "",
                    operationNumber = details["operationNumber"] as? String ?: "",
                    transferDate = (details["transferDate"] as? Long) ?: System.currentTimeMillis()
                )
            },
            digitalWalletDetails = doc.digitalWalletDetails?.let { details ->
                DigitalWalletDetails(
                    walletType = try {
                        PaymentMethod.valueOf(details["walletType"] as? String ?: "YAPE")
                    } catch (e: Exception) {
                        PaymentMethod.YAPE
                    },
                    phoneNumber = details["phoneNumber"] as? String ?: "",
                    operationNumber = details["operationNumber"] as? String ?: "",
                    transactionId = details["transactionId"] as? String ?: ""
                )
            },
            // Convertir los descuentos de Map a lista de Discount
            discounts = doc.discounts.mapNotNull { discountMap ->
                try {
                    Discount(
                        id = discountMap["id"] as? String ?: "",
                        employeeId = discountMap["employeeId"] as? String ?: "",
                        employeeName = discountMap["employeeName"] as? String ?: "",
                        amount = (discountMap["amount"] as? Number)?.toDouble() ?: 0.0,
                        type = try {
                            DiscountType.valueOf(discountMap["type"] as? String ?: "OTHER")
                        } catch (e: Exception) {
                            DiscountType.OTHER
                        },
                        reason = discountMap["reason"] as? String ?: "",
                        description = discountMap["description"] as? String ?: "",
                        isRecurring = discountMap["isRecurring"] as? Boolean ?: false,
                        startDate = (discountMap["startDate"] as? Long) ?: System.currentTimeMillis(),
                        endDate = discountMap["endDate"] as? Long,
                        isActive = discountMap["isActive"] as? Boolean ?: true,
                        appliedInPaymentId = discountMap["appliedInPaymentId"] as? String,
                        createdAt = (discountMap["createdAt"] as? Long) ?: System.currentTimeMillis(),
                        createdBy = discountMap["createdBy"] as? String ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            },
            // Convertir los adelantos de Map a lista de Advance
            advances = doc.advances.mapNotNull { advanceMap ->
                try {
                    Advance(
                        id = advanceMap["id"] as? String ?: "",
                        employeeId = advanceMap["employeeId"] as? String ?: "",
                        employeeName = advanceMap["employeeName"] as? String ?: "",
                        amount = (advanceMap["amount"] as? Number)?.toDouble() ?: 0.0,
                        requestDate = (advanceMap["requestDate"] as? Long) ?: System.currentTimeMillis(),
                        approvedDate = advanceMap["approvedDate"] as? Long,
                        paidDate = advanceMap["paidDate"] as? Long,
                        reason = advanceMap["reason"] as? String ?: "",
                        notes = advanceMap["notes"] as? String ?: "",
                        status = try {
                            AdvanceStatus.valueOf(advanceMap["status"] as? String ?: "PENDING")
                        } catch (e: Exception) {
                            AdvanceStatus.PENDING
                        },
                        paymentMethod = try {
                            PaymentMethod.valueOf(advanceMap["paymentMethod"] as? String ?: "CASH")
                        } catch (e: Exception) {
                            PaymentMethod.CASH
                        },
                        deductionSchedule = (advanceMap["deductionSchedule"] as? Map<*, *>)?.let { schedule ->
                            DeductionSchedule(
                                totalInstallments = (schedule["totalInstallments"] as? Long)?.toInt() ?: 1,
                                installmentAmount = (schedule["installmentAmount"] as? Number)?.toDouble() ?: 0.0,
                                remainingInstallments = (schedule["remainingInstallments"] as? Long)?.toInt() ?: 0,
                                startDeductionDate = (schedule["startDeductionDate"] as? Long) ?: System.currentTimeMillis()
                            )
                        },
                        remainingAmount = (advanceMap["remainingAmount"] as? Number)?.toDouble() ?: 0.0,
                        isFullyDeducted = advanceMap["isFullyDeducted"] as? Boolean ?: false,
                        createdAt = (advanceMap["createdAt"] as? Long) ?: System.currentTimeMillis(),
                        approvedBy = advanceMap["approvedBy"] as? String ?: "",
                        createdBy = advanceMap["createdBy"] as? String ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            },
            notes = doc.notes,
            status = try {
                PaymentStatus.valueOf(doc.status)
            } catch (e: Exception) {
                PaymentStatus.COMPLETED
            },
            createdAt = doc.createdAt,
            createdBy = doc.createdBy

        )
    }
}