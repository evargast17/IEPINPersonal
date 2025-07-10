package com.e17kapps.iepinpersonal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.e17kapps.iepinpersonal.domain.model.Payment
import com.e17kapps.iepinpersonal.domain.model.PaymentMethod
import com.e17kapps.iepinpersonal.domain.model.PaymentStatus
import com.e17kapps.iepinpersonal.utils.formatCurrency
import com.e17kapps.iepinpersonal.utils.toDateTimeString


@Composable
fun PaymentCard(
    payment: Payment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header con empleado y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = payment.employeeName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF374151),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = payment.paymentDate.toDateTimeString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                }

                PaymentStatusChip(status = payment.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Información del pago
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatCurrency(payment.amount),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = getStatusColor(payment.status)
                    )

                    if (payment.netAmount != payment.amount) {
                        Text(
                            text = "Neto: ${formatCurrency(payment.netAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                PaymentMethodChip(method = payment.paymentMethod)
            }

            // Detalles adicionales si existen
            if (payment.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = payment.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Mostrar descuentos o adelantos si existen
            if (payment.totalDiscounts > 0 || payment.totalAdvances > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (payment.totalDiscounts > 0) {
                        Text(
                            text = "Desc: ${formatCurrency(payment.totalDiscounts)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF6B6B),
                            modifier = Modifier
                                .background(
                                    Color(0xFFFF6B6B).copy(alpha = 0.1f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (payment.totalAdvances > 0) {
                        Text(
                            text = "Adel: ${formatCurrency(payment.totalAdvances)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFECA57),
                            modifier = Modifier
                                .background(
                                    Color(0xFFFECA57).copy(alpha = 0.1f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentStatusChip(
    status: PaymentStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, text) = when (status) {
        PaymentStatus.COMPLETED -> Triple(
            Color(0xFF10B981).copy(alpha = 0.1f),
            Color(0xFF10B981),
            "Completado"
        )
        PaymentStatus.PENDING -> Triple(
            Color(0xFFFECA57).copy(alpha = 0.1f),
            Color(0xFFFECA57),
            "Pendiente"
        )
        PaymentStatus.CANCELLED -> Triple(
            Color(0xFF6B7280).copy(alpha = 0.1f),
            Color(0xFF6B7280),
            "Cancelado"
        )
        PaymentStatus.FAILED -> Triple(
            Color(0xFFFF6B6B).copy(alpha = 0.1f),
            Color(0xFFFF6B6B),
            "Fallido"
        )
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Medium
        ),
        color = textColor,
        modifier = modifier
            .background(
                backgroundColor,
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
fun PaymentMethodChip(
    method: PaymentMethod,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, icon) = when (method) {
        PaymentMethod.CASH -> Triple(
            Color(0xFF059669).copy(alpha = 0.1f),
            Color(0xFF059669),
            "💵"
        )
        PaymentMethod.BANK_TRANSFER -> Triple(
            Color(0xFF3B82F6).copy(alpha = 0.1f),
            Color(0xFF3B82F6),
            "🏦"
        )
        PaymentMethod.YAPE -> Triple(
            Color(0xFF00A651).copy(alpha = 0.1f),
            Color(0xFF00A651),
            "📱"
        )
        PaymentMethod.PLIN -> Triple(
            Color(0xFF1E3A8A).copy(alpha = 0.1f),
            Color(0xFF1E3A8A),
            "💳"
        )
        PaymentMethod.OTHER_DIGITAL -> Triple(
            Color(0xFF6B7280).copy(alpha = 0.1f),
            Color(0xFF6B7280),
            "📲"
        )
    }

    Row(
        modifier = modifier
            .background(
                backgroundColor,
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = method.displayName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = textColor
        )
    }
}

@Composable
fun PaymentMethodFilter(
    method: PaymentMethod?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, text) = if (method == null) {
        Triple(
            if (isSelected) Color(0xFF667EEA) else Color.Transparent,
            if (isSelected) Color.White else Color(0xFF6B7280),
            "Todos"
        )
    } else {
        val baseColor = when (method) {
            PaymentMethod.CASH -> Color(0xFF059669)
            PaymentMethod.BANK_TRANSFER -> Color(0xFF3B82F6)
            PaymentMethod.YAPE -> Color(0xFF00A651)
            PaymentMethod.PLIN -> Color(0xFF1E3A8A)
            PaymentMethod.OTHER_DIGITAL -> Color(0xFF6B7280)
        }

        Triple(
            if (isSelected) baseColor else Color.Transparent,
            if (isSelected) Color.White else baseColor,
            method.displayName
        )
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        ),
        color = textColor,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .background(backgroundColor)
            .then(
                if (!isSelected) {
                    Modifier.background(
                        Color(0xFFE5E7EB),
                        RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

private fun getStatusColor(status: PaymentStatus): Color {
    return when (status) {
        PaymentStatus.COMPLETED -> Color(0xFF10B981)
        PaymentStatus.PENDING -> Color(0xFFFECA57)
        PaymentStatus.CANCELLED -> Color(0xFF6B7280)
        PaymentStatus.FAILED -> Color(0xFFFF6B6B)
    }
}

