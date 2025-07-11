package com.e17kapps.iepinpersonal.utils

import com.e17kapps.iepinpersonal.domain.model.Employee
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.*

// ============================================================================
// FIREBASE EXTENSIONS (Código original de FirebaseExtensions.kt)
// ============================================================================

// Extension para convertir Timestamp a Long
fun Timestamp.toLong(): Long = this.seconds * 1000

// Extension para convertir Long a Timestamp
fun Long.toTimestamp(): Timestamp = Timestamp(Date(this))

// Extension para Query como Flow
fun <T> Query.asFlow(mapper: (QueryDocumentSnapshot) -> T?): Flow<List<T>> = callbackFlow {
    val listener = addSnapshotListener { snapshot, error ->
        if (error != null) {
            trySend(emptyList())
            return@addSnapshotListener
        }

        val items = snapshot?.documents?.mapNotNull { document ->
            try {
                mapper(document as QueryDocumentSnapshot)
            } catch (e: Exception) {
                null
            }
        } ?: emptyList()

        trySend(items)
    }

    awaitClose { listener.remove() }
}

// Extension para DocumentReference como Flow
fun <T> DocumentReference.asFlow(mapper: (DocumentSnapshot) -> T?): Flow<T?> = callbackFlow {
    val listener = addSnapshotListener { snapshot, error ->
        if (error != null) {
            trySend(null)
            return@addSnapshotListener
        }

        val item = snapshot?.let { document ->
            try {
                if (document.exists()) mapper(document) else null
            } catch (e: Exception) {
                null
            }
        }

        trySend(item)
    }

    awaitClose { listener.remove() }
}

// Función para manejar errores de Firebase
inline fun <T> handleFirebaseError(action: () -> T): Result<T> {
    return try {
        Result.success(action())
    } catch (e: FirebaseFirestoreException) {
        when (e.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                Result.failure(Exception("Sin permisos para realizar esta operación"))
            }
            FirebaseFirestoreException.Code.NOT_FOUND -> {
                Result.failure(Exception("Documento no encontrado"))
            }
            FirebaseFirestoreException.Code.ALREADY_EXISTS -> {
                Result.failure(Exception("El documento ya existe"))
            }
            FirebaseFirestoreException.Code.UNAVAILABLE -> {
                Result.failure(Exception("Servicio no disponible. Verifica tu conexión"))
            }
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> {
                Result.failure(Exception("Tiempo de espera agotado"))
            }
            else -> {
                Result.failure(Exception("Error de base de datos: ${e.message}"))
            }
        }
    } catch (e: Exception) {
        Result.failure(Exception("Error inesperado: ${e.message}"))
    }
}

// Función para realizar operaciones en lote
suspend fun FirebaseFirestore.batchOperation(
    operations: suspend (WriteBatch) -> Unit
): Result<Unit> {
    return try {
        val batch = batch()
        operations(batch)
        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// ============================================================================
// DATE EXTENSIONS
// ============================================================================

// Extensiones para fechas
fun Long.toDateString(pattern: String = "dd/MM/yyyy"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toDateTimeString(pattern: String = "dd/MM/yyyy HH:mm"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

fun Date.toDateString(pattern: String): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(this)
}

// Función para obtener el inicio del mes
fun getStartOfMonth(month: Int, year: Int): Long {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1) // Calendar.MONTH es 0-based
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

// Función para obtener el fin del mes
fun getEndOfMonth(month: Int, year: Int): Long {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1) // Calendar.MONTH es 0-based
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return calendar.timeInMillis
}

// Función para obtener el inicio del día
fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

// Función para obtener el fin del día
fun getEndOfDay(timestamp: Long = System.currentTimeMillis()): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return calendar.timeInMillis
}

// ============================================================================
// VALIDATION EXTENSIONS
// ============================================================================

// Extensión para validar campos requeridos
fun String?.isValidEmail(): Boolean {
    return !isNullOrBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String?.isValidPhone(): Boolean {
    return !isNullOrBlank() && length >= 9 && all { it.isDigit() || it == '+' || it == '-' || it == ' ' }
}

fun String?.isValidDNI(): Boolean {
    return !isNullOrBlank() && length == 8 && all { it.isDigit() }
}

fun Double.isValidAmount(): Boolean {
    return this > 0.0 && this <= 999999.99
}

// ============================================================================
// SEARCH UTILITIES (Nuevo contenido integrado)
// ============================================================================

/**
 * Utilidades para búsqueda y filtrado de empleados
 */
object SearchUtils {

    /**
     * Normaliza un texto removiendo acentos y convertiendo a minúsculas
     */
    fun normalizeText(text: String): String {
        return Normalizer.normalize(text.lowercase().trim(), Normalizer.Form.NFD)
            .replace(Regex("[\\p{InCombiningDiacriticalMarks}]"), "")
    }

    /**
     * Busca empleados con diferentes criterios y relevancia
     */
    fun searchEmployees(
        employees: List<Employee>,
        query: String,
        maxResults: Int = 50
    ): List<Employee> {
        if (query.length < 3) return emptyList()

        val normalizedQuery = normalizeText(query)
        val queryWords = normalizedQuery.split(" ").filter { it.length >= 2 }

        return employees.filter { employee ->
            matchesEmployee(employee, normalizedQuery, queryWords)
        }.sortedWith(
            getRelevanceComparator(normalizedQuery, queryWords)
        ).take(maxResults)
    }

    /**
     * Verifica si un empleado coincide con la búsqueda
     */
    private fun matchesEmployee(
        employee: Employee,
        normalizedQuery: String,
        queryWords: List<String>
    ): Boolean {
        if (!employee.isActive) return false

        val fullName = normalizeText(employee.fullName)
        val firstName = normalizeText(employee.name)
        val lastName = normalizeText(employee.lastName)
        val dni = employee.dni
        val position = normalizeText(employee.position)

        return when {
            // Coincidencia exacta de DNI
            dni == normalizedQuery -> true

            // DNI que comience con la query
            dni.startsWith(normalizedQuery) -> true

            // Contiene DNI parcial
            normalizedQuery.all { it.isDigit() } && dni.contains(normalizedQuery) -> true

            // Nombre completo contiene la query
            fullName.contains(normalizedQuery) -> true

            // Nombre comienza con la query
            firstName.startsWith(normalizedQuery) -> true

            // Apellido comienza con la query
            lastName.startsWith(normalizedQuery) -> true

            // Cargo contiene la query
            position.contains(normalizedQuery) -> true

            // Búsqueda por palabras individuales
            queryWords.isNotEmpty() && queryWords.any { word ->
                firstName.contains(word) || lastName.contains(word) || position.contains(word)
            } -> true

            // Búsqueda fuzzy para nombres
            calculateSimilarity(fullName, normalizedQuery) > 0.6 -> true

            else -> false
        }
    }

    /**
     * Comparador que ordena por relevancia
     */
    private fun getRelevanceComparator(
        normalizedQuery: String,
        queryWords: List<String>
    ): Comparator<Employee> {
        return compareBy<Employee> { employee ->
            getRelevanceScore(employee, normalizedQuery, queryWords)
        }.thenBy { it.name }
    }

    /**
     * Calcula el score de relevancia (menor es mejor)
     */
    private fun getRelevanceScore(
        employee: Employee,
        normalizedQuery: String,
        queryWords: List<String>
    ): Int {
        val fullName = normalizeText(employee.fullName)
        val firstName = normalizeText(employee.name)
        val lastName = normalizeText(employee.lastName)
        val dni = employee.dni
        val position = normalizeText(employee.position)

        return when {
            // Coincidencia exacta de DNI (máxima relevancia)
            dni == normalizedQuery -> 0

            // DNI comienza con query
            dni.startsWith(normalizedQuery) -> 1

            // Nombre exacto
            firstName == normalizedQuery || lastName == normalizedQuery -> 2

            // Nombre completo exacto
            fullName == normalizedQuery -> 3

            // Nombre comienza con query
            firstName.startsWith(normalizedQuery) -> 4

            // Apellido comienza con query
            lastName.startsWith(normalizedQuery) -> 5

            // Nombre completo comienza con query
            fullName.startsWith(normalizedQuery) -> 6

            // Cargo exacto
            position == normalizedQuery -> 7

            // Cargo comienza con query
            position.startsWith(normalizedQuery) -> 8

            // Contiene en nombre
            firstName.contains(normalizedQuery) || lastName.contains(normalizedQuery) -> 9

            // Contiene en nombre completo
            fullName.contains(normalizedQuery) -> 10

            // Contiene en cargo
            position.contains(normalizedQuery) -> 11

            // Búsqueda por palabras
            queryWords.any { word ->
                firstName.startsWith(word) || lastName.startsWith(word)
            } -> 12

            // Búsqueda fuzzy
            else -> 15
        }
    }

    /**
     * Calcula la similaridad entre dos strings usando algoritmo de Levenshtein
     */
    private fun calculateSimilarity(s1: String, s2: String): Double {
        val distance = levenshteinDistance(s1, s2)
        val maxLength = maxOf(s1.length, s2.length)
        return if (maxLength == 0) 1.0 else 1.0 - (distance.toDouble() / maxLength)
    }

    /**
     * Calcula la distancia de Levenshtein entre dos strings
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) {
            for (j in 0..s2.length) {
                when {
                    i == 0 -> dp[i][j] = j
                    j == 0 -> dp[i][j] = i
                    s1[i - 1] == s2[j - 1] -> dp[i][j] = dp[i - 1][j - 1]
                    else -> dp[i][j] = 1 + minOf(
                        dp[i - 1][j],    // deletion
                        dp[i][j - 1],    // insertion
                        dp[i - 1][j - 1] // substitution
                    )
                }
            }
        }

        return dp[s1.length][s2.length]
    }

    /**
     * Filtros avanzados para empleados
     */
    fun filterEmployees(
        employees: List<Employee>,
        isActive: Boolean? = null,
        position: String? = null,
        salaryRange: Pair<Double, Double>? = null
    ): List<Employee> {
        return employees.filter { employee ->
            when {
                isActive != null && employee.isActive != isActive -> false
                position != null && !normalizeText(employee.position).contains(normalizeText(position)) -> false
                salaryRange != null && (employee.baseSalary < salaryRange.first || employee.baseSalary > salaryRange.second) -> false
                else -> true
            }
        }
    }

    /**
     * Agrupa empleados por criterio
     */
    fun groupEmployees(
        employees: List<Employee>,
        groupBy: EmployeeGroupBy
    ): Map<String, List<Employee>> {
        return when (groupBy) {
            EmployeeGroupBy.POSITION -> employees.groupBy { it.position }
            EmployeeGroupBy.SALARY_RANGE -> employees.groupBy { getSalaryRange(it.baseSalary) }
            EmployeeGroupBy.FIRST_LETTER -> employees.groupBy { it.name.first().uppercase() }
            EmployeeGroupBy.STATUS -> employees.groupBy { if (it.isActive) "Activos" else "Inactivos" }
        }
    }

    private fun getSalaryRange(salary: Double): String {
        return when {
            salary < 1000 -> "Menos de S/ 1,000"
            salary < 2000 -> "S/ 1,000 - S/ 2,000"
            salary < 3000 -> "S/ 2,000 - S/ 3,000"
            salary < 5000 -> "S/ 3,000 - S/ 5,000"
            else -> "Más de S/ 5,000"
        }
    }

    /**
     * Validaciones para búsqueda
     */
    fun isValidSearchQuery(query: String): Boolean {
        val trimmed = query.trim()
        return trimmed.length >= 3 && trimmed.length <= 100
    }

    fun getSuggestions(query: String, employees: List<Employee>): List<String> {
        val normalizedQuery = normalizeText(query)
        val suggestions = mutableSetOf<String>()

        employees.forEach { employee ->
            val words = listOf(
                employee.name,
                employee.lastName,
                employee.position,
                employee.dni
            )

            words.forEach { word ->
                val normalizedWord = normalizeText(word)
                if (normalizedWord.startsWith(normalizedQuery) && normalizedWord != normalizedQuery) {
                    suggestions.add(word)
                }
            }
        }

        return suggestions.take(5).toList()
    }
}

enum class EmployeeGroupBy {
    POSITION,
    SALARY_RANGE,
    FIRST_LETTER,
    STATUS
}

/**
 * Clase para cachear resultados de búsqueda
 */
class SearchCache<T>(private val maxSize: Int = 50) {
    private val cache = LinkedHashMap<String, List<T>>(maxSize + 1, 0.75f, true)

    fun get(key: String): List<T>? = cache[key]

    fun put(key: String, value: List<T>) {
        if (cache.size >= maxSize) {
            val firstKey = cache.keys.first()
            cache.remove(firstKey)
        }
        cache[key] = value
    }

    fun clear() = cache.clear()

    fun size() = cache.size
}

/**
 * Debouncer para evitar búsquedas excesivas
 */
class SearchDebouncer(private val delayMs: Long = 300) {
    private var lastSearchTime = 0L

    fun shouldSearch(): Boolean {
        val currentTime = System.currentTimeMillis()
        return if (currentTime - lastSearchTime >= delayMs) {
            lastSearchTime = currentTime
            true
        } else {
            false
        }
    }
}

// ============================================================================
// CURRENCY FORMATTING UTILITIES
// ============================================================================

/**
 * Formatea un monto como moneda peruana
 */
fun formatCurrency(amount: Double): String {
    val peruLocale = java.util.Locale.Builder()
        .setLanguage("es")
        .setRegion("PE")
        .build()
    val formatter = java.text.NumberFormat.getCurrencyInstance(peruLocale)
    return formatter.format(amount).replace("PEN", "S/")
}

/**
 * Extension function para Double
 */
fun Double.toCurrency(): String = formatCurrency(this)

/**
 * Parsea un string de moneda a Double
 */
fun String.parseCurrency(): Double? {
    return try {
        this.replace("S/", "")
            .replace(",", "")
            .trim()
            .toDoubleOrNull()
    } catch (e: Exception) {
        null
    }
}

/**
 * Valida si un string es un monto válido
 */
fun String.isValidCurrencyAmount(): Boolean {
    val amount = this.parseCurrency()
    return amount != null && amount > 0.0 && amount <= 999999.99
}

/**
 * Formatea número con separadores de miles
 */
fun formatNumber(number: Long): String {
    val peruLocale = java.util.Locale.Builder()
        .setLanguage("es")
        .setRegion("PE")
        .build()
    val formatter = java.text.NumberFormat.getNumberInstance(peruLocale)
    return formatter.format(number)
}

fun formatNumber(number: Int): String = formatNumber(number.toLong())

/**
 * Formatea porcentaje
 */
fun formatPercentage(value: Double, decimals: Int = 1): String {
    return "%.${decimals}f%%".format(value)
}

// ============================================================================
// CONSTANTES Y CONFIGURACIÓN
// ============================================================================

object FirestoreConstants {
    const val BATCH_SIZE = 500
    const val MAX_RETRIES = 3
    const val RETRY_DELAY_MS = 1000L
}

object AppConstants {
    const val MIN_SEARCH_CHARACTERS = 3
    const val MAX_SEARCH_RESULTS = 50
    const val SEARCH_DEBOUNCE_MS = 300L
    const val DEFAULT_CURRENCY_FORMAT = "es_PE"

    // Configuración de moneda
    const val CURRENCY_SYMBOL = "S/"
    const val MAX_AMOUNT = 999999.99
    const val MIN_AMOUNT = 0.01
}

// Agregar estas extensiones al final del archivo Extensions.kt existente

// ============================================================================
// PROFILE & SETTINGS EXTENSIONS
// ============================================================================

/**
 * Extensiones para validación de perfil de usuario
 */
fun validateProfileName(name: String): String? {
    return when {
        name.isBlank() -> "El nombre no puede estar vacío"
        name.length < 2 -> "El nombre debe tener al menos 2 caracteres"
        name.length > 50 -> "El nombre no puede tener más de 50 caracteres"
        !name.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) ->
            "El nombre solo puede contener letras y espacios"
        else -> null
    }
}

fun validateProfileEmail(email: String): String? {
    return when {
        email.isBlank() -> "El email no puede estar vacío"
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
            "Formato de email no válido"
        else -> null
    }
}

/**
 * Extensiones para formateo de tiempo relativo
 */
fun Long.toRelativeTimeString(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val months = days / 30
    val years = days / 365

    return when {
        years > 0 -> if (years == 1L) "hace 1 año" else "hace $years años"
        months > 0 -> if (months == 1L) "hace 1 mes" else "hace $months meses"
        days > 0 -> if (days == 1L) "hace 1 día" else "hace $days días"
        hours > 0 -> if (hours == 1L) "hace 1 hora" else "hace $hours horas"
        minutes > 0 -> if (minutes == 1L) "hace 1 minuto" else "hace $minutes minutos"
        else -> "hace unos segundos"
    }
}

/**
 * Extensiones para el modelo User existente
 */
fun com.e17kapps.iepinpersonal.domain.model.User.getInitials(): String {
    return if (name.isNotBlank()) {
        name.split(" ")
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
    } else {
        "U"
    }
}

fun com.e17kapps.iepinpersonal.domain.model.User.getDisplayName(): String {
    return name.ifBlank { "Usuario" }
}

fun com.e17kapps.iepinpersonal.domain.model.User.isAdmin(): Boolean {
    return role == com.e17kapps.iepinpersonal.domain.model.UserRole.ADMIN
}

fun com.e17kapps.iepinpersonal.domain.model.User.getAccountAge(): String {
    return createdAt.toRelativeTimeString()
}

fun com.e17kapps.iepinpersonal.domain.model.User.getLastUpdateTime(): String {
    return updatedAt.toRelativeTimeString()
}

/**
 * Utilidades para configuración de la aplicación
 */
object SettingsConstants {
    const val PREF_THEME = "app_theme"
    const val PREF_LANGUAGE = "app_language"
    const val PREF_NOTIFICATIONS = "notifications_enabled"
    const val PREF_AUTO_BACKUP = "auto_backup_enabled"
    const val PREF_BIOMETRIC = "biometric_enabled"
    const val PREF_PIN_REQUIRED = "pin_required"
    const val PREF_AUTO_LOCK_TIME = "auto_lock_time"
    const val PREF_CURRENCY = "currency"
    const val PREF_DATE_FORMAT = "date_format"
    const val PREF_TIME_FORMAT = "time_format"

    // Valores por defecto
    const val DEFAULT_AUTO_LOCK_TIME = 5
    const val DEFAULT_CURRENCY = "PEN"
    const val DEFAULT_DATE_FORMAT = "dd/MM/yyyy"
    const val DEFAULT_TIME_FORMAT = "HH:mm"
    const val DEFAULT_LANGUAGE = "es"
}

/**
 * Extensiones para manejo de versiones y información de la app
 */
object AppInfo {
    const val VERSION_NAME = "1.0.0"
    const val VERSION_CODE = 1
    const val BUILD_DATE = "Julio 2025"
    const val DEVELOPER = "E17K Apps"
    const val SUPPORT_EMAIL = "soporte@e17kapps.com"
    const val PRIVACY_URL = "https://e17kapps.com/privacy"
    const val TERMS_URL = "https://e17kapps.com/terms"
    const val HELP_URL = "https://e17kapps.com/help"
}

/**
 * Extensiones para validación de configuración
 */
fun String.isValidTheme(): Boolean {
    return this in listOf("LIGHT", "DARK", "SYSTEM")
}

fun String.isValidLanguage(): Boolean {
    return this in listOf("es", "en")
}

fun Int.isValidAutoLockTime(): Boolean {
    return this in 1..60 // Entre 1 y 60 minutos
}

fun String.isValidCurrency(): Boolean {
    return this in listOf("PEN", "USD", "EUR") // Monedas soportadas
}

/**
 * Utilidades para debugging y desarrollo
 */
object ProfileDebugUtils {
    fun logUserInfo(user: com.e17kapps.iepinpersonal.domain.model.User?) {
        if (user != null) {
            android.util.Log.d("ProfileDebug", "Usuario: ${user.name} (${user.email})")
            android.util.Log.d("ProfileDebug", "Rol: ${user.role.displayName}")
            android.util.Log.d("ProfileDebug", "Activo: ${user.isActive}")
            android.util.Log.d("ProfileDebug", "Creado: ${user.createdAt.toDateTimeString()}")
        } else {
            android.util.Log.w("ProfileDebug", "Usuario nulo")
        }
    }
}

/**
 * Extensiones adicionales para formateo
 */
fun String.toTitleCase(): String {
    return split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
    }
}

fun String.removeExtraSpaces(): String {
    return trim().replace(Regex("\\s+"), " ")
}

/**
 * Utilidades para validación de configuración avanzada
 */
object ConfigValidationUtils {

    fun isValidNotificationTime(time: String): Boolean {
        return try {
            val parts = time.split(":")
            if (parts.size != 2) return false
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            hour in 0..23 && minute in 0..59
        } catch (e: Exception) {
            false
        }
    }

    fun formatNotificationTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }

    fun parseNotificationTime(time: String): Pair<Int, Int>? {
        return try {
            val parts = time.split(":")
            if (parts.size == 2) {
                Pair(parts[0].toInt(), parts[1].toInt())
            } else null
        } catch (e: Exception) {
            null
        }
    }
}