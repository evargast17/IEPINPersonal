package com.e17kapps.iepinpersonal.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.PersistentCacheSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseConfig @Inject constructor() {

    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance().apply {
            // Usar la nueva API de configuración
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(
                    // Opción 1: Cache persistente (recomendado para apps de producción)
                    PersistentCacheSettings.newBuilder()
                        .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                        .build()

                    // Opción 2: Cache en memoria (descomenta si prefieres esta opción)
                    // MemoryCacheSettings.newBuilder()
                    //     .setGcSettings(MemoryGarbageCollectorSettings.newBuilder()
                    //         .setMemoryCacheThreshold(100 * 1024 * 1024) // 100 MB
                    //         .build())
                    //     .build()
                )
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

        // Cache settings constants
        const val DEFAULT_CACHE_SIZE_MB = 100L * 1024 * 1024 // 100 MB
        const val SMALL_CACHE_SIZE_MB = 40L * 1024 * 1024   // 40 MB

        // Timeout settings
        const val DEFAULT_TIMEOUT_SECONDS = 30L
        const val LONG_TIMEOUT_SECONDS = 60L
    }

    // Función auxiliar para configurar diferentes tipos de cache
    fun configureCache(useUnlimitedCache: Boolean = true, customSizeMB: Long? = null) {
        val cacheSettings = if (useUnlimitedCache) {
            // Cache persistente ilimitado (recomendado para la mayoría de casos)
            PersistentCacheSettings.newBuilder()
                .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
        } else {
            // Cache persistente con tamaño limitado
            val sizeBytes = customSizeMB?.let { it * 1024 * 1024 } ?: DEFAULT_CACHE_SIZE_MB
            PersistentCacheSettings.newBuilder()
                .setSizeBytes(sizeBytes)
                .build()
        }

        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(cacheSettings)
            .build()
    }

    // Función para configurar cache en memoria (para casos específicos)
    fun configureMemoryCache(thresholdMB: Long = 100) {
        val memoryCacheSettings = MemoryCacheSettings.newBuilder()
            .build()

        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(memoryCacheSettings)
            .build()
    }

    // Función para limpiar el cache (útil para debugging o cuando se necesite)
    suspend fun clearCache() {
        try {
            firestore.clearPersistence()
        } catch (e: Exception) {
            // El cache solo se puede limpiar cuando no hay conexiones activas
            android.util.Log.w("FirebaseConfig", "No se pudo limpiar el cache: ${e.message}")
        }
    }

    // Función para obtener información del estado del cache
    fun getCacheInfo(): String {
        return buildString {
            appendLine("=== FIREBASE CACHE INFO ===")
            appendLine("Configuración actual: Cache persistente")
            appendLine("Tamaño: Ilimitado")
            appendLine("Estado: Activo")
        }
    }
}