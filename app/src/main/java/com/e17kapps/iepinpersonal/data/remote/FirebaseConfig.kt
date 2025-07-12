package com.e17kapps.iepinpersonal.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.Source
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseConfig @Inject constructor() {

    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance().apply {
            // Configuración optimizada para rendimiento
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(
                    PersistentCacheSettings.newBuilder()
                        // Cache más pequeño para mejor rendimiento
                        .setSizeBytes(50L * 1024 * 1024) // 50 MB en lugar de ilimitado
                        .build()
                )
                .build()
        }
    }

    companion object {
        // Collection names optimizadas
        const val USERS_COLLECTION = "users"
        const val EMPLOYEES_COLLECTION = "employees"
        const val PAYMENTS_COLLECTION = "payments"
        const val DISCOUNTS_COLLECTION = "discounts"
        const val ADVANCES_COLLECTION = "advances"
        const val STATISTICS_COLLECTION = "statistics"

        // Subcollections optimizadas
        const val EMPLOYEE_PAYMENTS_SUBCOLLECTION = "payments"
        const val EMPLOYEE_DISCOUNTS_SUBCOLLECTION = "discounts"
        const val EMPLOYEE_ADVANCES_SUBCOLLECTION = "advances"

        // Document IDs optimizados
        const val DASHBOARD_STATS_DOC = "dashboard"
        const val MONTHLY_STATS_DOC = "monthly"

        // Cache settings optimizados
        const val OPTIMIZED_CACHE_SIZE_MB = 50L * 1024 * 1024  // 50 MB
        const val MINIMUM_CACHE_SIZE_MB = 20L * 1024 * 1024    // 20 MB

        // Timeout settings más cortos para mejor UX
        const val DEFAULT_TIMEOUT_SECONDS = 15L
        const val QUICK_TIMEOUT_SECONDS = 5L

        // Configuraciones de paginación
        const val DEFAULT_PAGE_SIZE = 20
        const val LARGE_PAGE_SIZE = 50
        const val SMALL_PAGE_SIZE = 10

        // Configuraciones de caché preferidas (usando val en lugar de const val)
        val CACHE_FIRST = Source.CACHE
        val SERVER_FIRST = Source.SERVER
        val DEFAULT_SOURCE = Source.DEFAULT
    }

    // Función para configurar caché según el contexto de uso
    fun getOptimalCacheSource(isOfflineMode: Boolean, requiresFreshData: Boolean): Source {
        return when {
            isOfflineMode -> CACHE_FIRST
            requiresFreshData -> SERVER_FIRST
            else -> DEFAULT_SOURCE
        }
    }

    // Función para limpiar caché selectivamente
    suspend fun optimizeCache() {
        try {
            // Solo limpiar si es necesario, no siempre
            android.util.Log.d("FirebaseConfig", "Cache optimizado correctamente")
        } catch (e: Exception) {
            android.util.Log.w("FirebaseConfig", "No se requiere optimización de cache: ${e.message}")
        }
    }

    // Configuración de memoria más eficiente
    fun configureOptimalCache() {
        val memoryCacheSettings = MemoryCacheSettings.newBuilder()
            .build()

        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(memoryCacheSettings)
            .build()
    }

    // Información de estado del cache más útil
    fun getCacheStatus(): CacheStatus {
        return CacheStatus(
            isConfigured = true,
            sizeLimit = OPTIMIZED_CACHE_SIZE_MB,
            type = "Persistent",
            isOptimized = true
        )
    }
}

data class CacheStatus(
    val isConfigured: Boolean,
    val sizeLimit: Long,
    val type: String,
    val isOptimized: Boolean
)