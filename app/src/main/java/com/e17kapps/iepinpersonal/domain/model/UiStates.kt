package com.e17kapps.iepinpersonal.domain.model

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordVisible: Boolean = false
)

data class EmployeeListUiState(
    val employees: List<Employee> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val filteredEmployees: List<Employee> = emptyList()
)

data class PaymentUiState(
    val selectedEmployee: Employee? = null,
    val amount: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val bankDetails: BankDetails? = null,
    val digitalWalletDetails: DigitalWalletDetails? = null,
    val notes: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

data class DashboardUiState(
    val statistics: DashboardStatistics = DashboardStatistics(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

// Nuevos estados para búsqueda de empleados
data class EmployeeSearchUiState(
    val query: String = "",
    val results: List<Employee> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val errorMessage: String? = null
)

data class PaymentFormUiState(
    val isEmployeeSelected: Boolean = false,
    val selectedEmployee: Employee? = null,
    val employeeSearchQuery: String = "",
    val employeeSearchResults: List<Employee> = emptyList(),
    val isSearchingEmployees: Boolean = false,
    val showEmployeeResults: Boolean = false,
    val amount: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val showPaymentMethodDropdown: Boolean = false,
    val bankDetails: BankDetails? = null,
    val digitalWalletDetails: DigitalWalletDetails? = null,
    val notes: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isFormValid: Boolean = false
) {
    val canProcessPayment: Boolean
        get() = selectedEmployee != null &&
                amount.isNotBlank() &&
                amount.toDoubleOrNull() != null &&
                amount.toDoubleOrNull()!! > 0 &&
                !isLoading &&
                isValidPaymentMethodDetails()

    private fun isValidPaymentMethodDetails(): Boolean {
        return when (paymentMethod) {
            PaymentMethod.CASH -> true
            PaymentMethod.BANK_TRANSFER -> {
                bankDetails?.let { details ->
                    details.bankName.isNotBlank() && details.operationNumber.isNotBlank()
                } ?: false
            }
            PaymentMethod.YAPE, PaymentMethod.PLIN -> {
                digitalWalletDetails?.let { details ->
                    details.phoneNumber.isNotBlank() && details.operationNumber.isNotBlank()
                } ?: false
            }
            PaymentMethod.OTHER_DIGITAL -> {
                digitalWalletDetails?.let { details ->
                    details.phoneNumber.isNotBlank() && details.operationNumber.isNotBlank()
                } ?: false
            }
        }
    }
}

// Estados para validación de formularios
data class FormValidationState(
    val isValid: Boolean = false,
    val errors: Map<String, String> = emptyMap(),
    val warnings: Map<String, String> = emptyMap()
)

// Estados para operaciones async
sealed class AsyncOperation<out T> {
    object Idle : AsyncOperation<Nothing>()
    object Loading : AsyncOperation<Nothing>()
    data class Success<T>(val data: T) : AsyncOperation<T>()
    data class Error(val exception: Throwable, val message: String = exception.message ?: "Error desconocido") : AsyncOperation<Nothing>()
}

// Estados específicos para búsqueda
data class SearchState<T>(
    val query: String = "",
    val results: List<T> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val isEmpty: Boolean = false,
    val errorMessage: String? = null,
    val totalResults: Int = 0,
    val hasMore: Boolean = false
) {
    val showResults: Boolean
        get() = hasSearched && query.isNotBlank()

    val showEmptyState: Boolean
        get() = hasSearched && results.isEmpty() && !isSearching && errorMessage == null

    val showError: Boolean
        get() = errorMessage != null
}

// Estados para paginación
data class PaginationState(
    val currentPage: Int = 0,
    val pageSize: Int = 20,
    val totalItems: Int = 0,
    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false
) {
    val totalPages: Int
        get() = if (totalItems == 0) 0 else (totalItems + pageSize - 1) / pageSize

    val canLoadMore: Boolean
        get() = hasMore && !isLoadingMore
}

// Estados para filtros
data class FilterState(
    val isActive: Boolean? = null,
    val position: String? = null,
    val salaryRange: Pair<Double, Double>? = null,
    val dateRange: Pair<Long, Long>? = null,
    val paymentMethod: PaymentMethod? = null,
    val sortBy: SortOption = SortOption.NAME_ASC
)

enum class SortOption(val displayName: String) {
    NAME_ASC("Nombre A-Z"),
    NAME_DESC("Nombre Z-A"),
    SALARY_ASC("Salario Menor"),
    SALARY_DESC("Salario Mayor"),
    DATE_ASC("Más Antiguos"),
    DATE_DESC("Más Recientes")
}

// Estados para operaciones en lote
data class BatchOperationState(
    val selectedItems: Set<String> = emptySet(),
    val isInSelectionMode: Boolean = false,
    val operation: BatchOperation? = null,
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val errorMessage: String? = null
) {
    val selectedCount: Int
        get() = selectedItems.size

    val hasSelection: Boolean
        get() = selectedItems.isNotEmpty()
}

enum class BatchOperation(val displayName: String) {
    DELETE("Eliminar"),
    DEACTIVATE("Desactivar"),
    ACTIVATE("Activar"),
    EXPORT("Exportar"),
    BULK_PAYMENT("Pago Masivo")
}

// Profile

data class ProfileUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val logoutSuccess: Boolean = false,
    val isEditingProfile: Boolean = false,
    val profileUpdateSuccess: Boolean = false
)

data class SettingsUiState(
    val isDarkModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val autoBackupEnabled: Boolean = false,
    val lastBackupDate: Long? = null,
    val appVersion: String = "1.0.0",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

data class UserPreferences(
    val theme: AppTheme = AppTheme.SYSTEM,
    val language: AppLanguage = AppLanguage.SPANISH,
    val notifications: NotificationSettings = NotificationSettings(),
    val currency: String = "PEN",
    val dateFormat: String = "dd/MM/yyyy",
    val timeFormat: String = "HH:mm"
)

enum class AppTheme(val displayName: String) {
    LIGHT("Claro"),
    DARK("Oscuro"),
    SYSTEM("Sistema")
}

enum class AppLanguage(val displayName: String, val code: String) {
    SPANISH("Español", "es"),
    ENGLISH("English", "en")
}

data class NotificationSettings(
    val enabled: Boolean = true,
    val paymentReminders: Boolean = true,
    val employeeUpdates: Boolean = true,
    val systemNotifications: Boolean = true,
    val emailNotifications: Boolean = false,
    val reminderTime: String = "09:00"
)

data class DataManagementState(
    val isBackingUp: Boolean = false,
    val isExporting: Boolean = false,
    val lastBackupDate: Long? = null,
    val backupProgress: Float = 0f,
    val exportProgress: Float = 0f,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

data class SecuritySettings(
    val requirePinOnStartup: Boolean = false,
    val biometricEnabled: Boolean = false,
    val autoLockTime: Int = 5, // minutos
    val dataEncryptionEnabled: Boolean = true
)

data class ProfileValidationState(
    val nameError: String? = null,
    val emailError: String? = null,
    val isNameValid: Boolean = true,
    val isEmailValid: Boolean = true,
    val canSaveProfile: Boolean = false
) {
    val hasErrors: Boolean
        get() = nameError != null || emailError != null

    val isFormValid: Boolean
        get() = isNameValid && isEmailValid && !hasErrors
}

data class UserAnalytics(
    val totalEmployeesCreated: Int = 0,
    val totalPaymentsProcessed: Int = 0,
    val totalAmountProcessed: Double = 0.0,
    val accountAge: Long = 0,
    val lastLoginDate: Long = 0,
    val loginCount: Int = 0,
    val favoritePaymentMethod: String? = null,
    val averageMonthlyPayments: Double = 0.0
)

data class AdvancedSettings(
    val debugModeEnabled: Boolean = false,
    val analyticsEnabled: Boolean = true,
    val crashReportingEnabled: Boolean = true,
    val betaFeaturesEnabled: Boolean = false,
    val developerOptionsVisible: Boolean = false
)

sealed class SettingsError(val message: String) {
    object BackupFailed : SettingsError("Error al realizar respaldo")
    object ExportFailed : SettingsError("Error al exportar datos")
    object PreferencesSaveFailed : SettingsError("Error al guardar configuración")
    object NetworkError : SettingsError("Error de conexión")
    data class UnknownError(val details: String) : SettingsError(details)
}

sealed class SettingsResult<out T> {
    data class Success<T>(val data: T) : SettingsResult<T>()
    data class Error(val error: SettingsError) : SettingsResult<Nothing>()
    object Loading : SettingsResult<Nothing>()
}

data class OnboardingState(
    val isFirstLaunch: Boolean = false,
    val hasSeenProfileTutorial: Boolean = false,
    val hasSeenPaymentTutorial: Boolean = false,
    val hasSeenEmployeeTutorial: Boolean = false,
    val currentTutorialStep: Int = 0,
    val isTutorialVisible: Boolean = false
)

data class FeedbackState(
    val hasRatedApp: Boolean = false,
    val feedbackGiven: Boolean = false,
    val lastFeedbackDate: Long = 0,
    val appUsageCount: Int = 0,
    val shouldShowRatingPrompt: Boolean = false
)

// ============================================================================
// ESTADOS ESPECÍFICOS PARA ESTADÍSTICAS
// ============================================================================

// Estados para timeframes de estadísticas
enum class StatisticsTimeframe(val displayName: String, val days: Int) {
    WEEK("Esta Semana", 7),
    MONTH("Este Mes", 30),
    QUARTER("Este Trimestre", 90),
    YEAR("Este Año", 365),
    ALL_TIME("Todo el Tiempo", -1)
}

// Estados para exportación de datos
sealed class ExportStatus {
    object Idle : ExportStatus()
    object Loading : ExportStatus()
    data class Success(val message: String) : ExportStatus()
    data class Error(val message: String) : ExportStatus()
}

// Estados para criterios de rendimiento de empleados
enum class PerformanceCriteria {
    ALL,
    HIGH_SALARY,
    RECENT_HIRES,
    ACTIVE
}

// Estado para resumen de estadísticas
data class StatisticsSummary(
    val totalEmployees: Int = 0,
    val totalPaymentsThisMonth: Double = 0.0,
    val totalPending: Double = 0.0,
    val paymentsToday: Int = 0,
    val monthlyGrowth: Double = 0.0,
    val averagePayment: Double = 0.0,
    val topPaymentMethod: String = "N/A"
)

// Estados para análisis de estadísticas
data class StatisticsAnalysisState(
    val isAnalyzing: Boolean = false,
    val selectedTimeframe: StatisticsTimeframe = StatisticsTimeframe.MONTH,
    val selectedEmployee: Employee? = null,
    val filterCriteria: PerformanceCriteria = PerformanceCriteria.ALL,
    val analysisResults: List<String> = emptyList(),
    val errorMessage: String? = null
)

// Estados para filtros de estadísticas
data class StatisticsFilterState(
    val dateRange: Pair<Long, Long>? = null,
    val employeeIds: Set<String> = emptySet(),
    val paymentMethods: Set<PaymentMethod> = emptySet(),
    val minimumAmount: Double? = null,
    val maximumAmount: Double? = null,
    val includeDiscounts: Boolean = true,
    val includeAdvances: Boolean = true
)

// Estados para comparaciones temporales
data class TemporalComparisonState(
    val currentPeriod: StatisticsTimeframe = StatisticsTimeframe.MONTH,
    val comparisonPeriod: StatisticsTimeframe = StatisticsTimeframe.MONTH,
    val currentData: DashboardStatistics = DashboardStatistics(),
    val comparisonData: DashboardStatistics = DashboardStatistics(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val hasValidComparison: Boolean
        get() = !isLoading && errorMessage == null
}