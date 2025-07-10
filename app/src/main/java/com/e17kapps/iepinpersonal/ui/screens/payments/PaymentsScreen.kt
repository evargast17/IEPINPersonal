package com.e17kapps.iepinpersonal.ui.screens.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.e17kapps.iepinpersonal.domain.model.Payment
import com.e17kapps.iepinpersonal.domain.model.PaymentMethod
import com.e17kapps.iepinpersonal.ui.components.EmptyState
import com.e17kapps.iepinpersonal.ui.components.LoadingState
import com.e17kapps.iepinpersonal.ui.components.PaymentCard
import com.e17kapps.iepinpersonal.ui.components.PaymentMethodFilter
import com.e17kapps.iepinpersonal.utils.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    onNavigateToAddPayment: () -> Unit,
    onNavigateToPaymentDetail: (String) -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val payments by viewModel.payments.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf<PaymentMethod?>(null) }
    var showFilters by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getTodayPayments()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Header con estadísticas
            PaymentStatsRow(payments = payments)

            Spacer(modifier = Modifier.height(16.dp))

            // Filtros y controles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pagos Recientes",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF374151)
                )

                Row {
                    OutlinedButton(
                        onClick = { showFilters = !showFilters },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Filtros")
                    }
                }
            }

            // Filtros de método de pago
            if (showFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Filtrar por método de pago",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                PaymentMethodFilter(
                                    method = null,
                                    isSelected = selectedFilter == null,
                                    onClick = {
                                        selectedFilter = null
                                        viewModel.getTodayPayments()
                                    }
                                )
                            }

                            items(PaymentMethod.values()) { method ->
                                PaymentMethodFilter(
                                    method = method,
                                    isSelected = selectedFilter == method,
                                    onClick = {
                                        selectedFilter = method
                                        viewModel.getPaymentsByMethod(method)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de pagos
            when {
                uiState.isLoading && payments.isEmpty() -> {
                    LoadingState(
                        message = "Cargando pagos...",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                payments.isEmpty() && !uiState.isLoading -> {
                    EmptyState(
                        icon = "💰",
                        title = "Sin pagos registrados",
                        message = "Registra el primer pago de sueldo para comenzar",
                        actionText = "Registrar Pago",
                        onAction = onNavigateToAddPayment,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        // Agrupar pagos por fecha
                        val groupedPayments = payments.groupBy { payment ->
                            payment.paymentDate.toDateString("dd/MM/yyyy")
                        }.toSortedMap(compareByDescending {
                            // Ordenar fechas de más reciente a más antigua
                            it
                        })

                        groupedPayments.forEach { (date, paymentsForDate) ->
                            item {
                                Text(
                                    text = getDateLabel(date),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color(0xFF6B7280),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            items(paymentsForDate) { payment ->
                                PaymentCard(
                                    payment = payment,
                                    onClick = { onNavigateToPaymentDetail(payment.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onNavigateToAddPayment,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF667EEA),
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Registrar pago"
            )
        }

        // Loading overlay
        if (uiState.isLoading && payments.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF667EEA)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Cargando...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentStatsRow(
    payments: List<Payment>
) {
    val todayTotal = payments.sumOf { it.amount }
    val completedPayments = payments.filter { it.status == com.e17kapps.iepinpersonal.domain.model.PaymentStatus.COMPLETED }
    val pendingPayments = payments.filter { it.status == com.e17kapps.iepinpersonal.domain.model.PaymentStatus.PENDING }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PaymentStatItem(
                title = "Total Hoy",
                value = formatCurrency(todayTotal),
                color = Color(0xFF10B981)
            )

            PaymentStatItem(
                title = "Completados",
                value = completedPayments.size.toString(),
                color = Color(0xFF3B82F6)
            )

            PaymentStatItem(
                title = "Pendientes",
                value = pendingPayments.size.toString(),
                color = Color(0xFFFECA57)
            )
        }
    }
}

@Composable
private fun PaymentStatItem(
    title: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6B7280)
        )
    }
}

private fun getDateLabel(dateString: String): String {
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    val todayString = today.timeInMillis.toDateString("dd/MM/yyyy")
    val yesterdayString = yesterday.timeInMillis.toDateString("dd/MM/yyyy")

    return when (dateString) {
        todayString -> "Hoy"
        yesterdayString -> "Ayer"
        else -> dateString
    }
}
