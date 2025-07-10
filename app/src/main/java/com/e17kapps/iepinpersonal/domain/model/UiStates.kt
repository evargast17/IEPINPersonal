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