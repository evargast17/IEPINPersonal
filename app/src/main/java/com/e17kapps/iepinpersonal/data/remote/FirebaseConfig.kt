package com.e17kapps.iepinpersonal.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseConfig @Inject constructor() {

    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance().apply {
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
        }
    }

    companion object {
        // Collection names
        const val USERS_COLLECTION = "users"
        const val EMPLOYEES_COLLECTION = "employees"
        const val PAYMENTS_COLLECTION = "payments"
        const val DISCOUNTS_COLLECTION = "discounts"
        const val ADVANCES_COLLECTION = "advances"
        const val STATISTICS_COLLECTION = "statistics"

        // Subcollections
        const val EMPLOYEE_PAYMENTS_SUBCOLLECTION = "payments"
        const val EMPLOYEE_DISCOUNTS_SUBCOLLECTION = "discounts"
        const val EMPLOYEE_ADVANCES_SUBCOLLECTION = "advances"

        // Document IDs
        const val DASHBOARD_STATS_DOC = "dashboard"
        const val MONTHLY_STATS_DOC = "monthly"
    }
}