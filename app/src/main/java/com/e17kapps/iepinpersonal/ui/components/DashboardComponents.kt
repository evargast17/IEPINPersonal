package com.e17kapps.iepinpersonal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.e17kapps.iepinpersonal.domain.model.ActivityItem
import com.e17kapps.iepinpersonal.ui.theme.AppColors
import com.e17kapps.iepinpersonal.utils.formatCurrency
import com.e17kapps.iepinpersonal.utils.toDateTimeString

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: String,
    color: Color,
    change: Double? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )

                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = AppColors.TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = color.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 16.sp
                    )
                }
            }

            if (change != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val changeText = if (change >= 0) "+${String.format("%.1f", change)}%" else "${String.format("%.1f", change)}%"
                    val changeColor = if (change >= 0) Color(0xFF10B981) else Color(0xFFFF6B6B)
                    val changeIcon = if (change >= 0) "📈" else "📉"

                    Text(
                        text = changeIcon,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = changeText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = changeColor
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                color.copy(alpha = 0.2f),
                                color.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ActivityItemCard(
    activity: ActivityItem,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de actividad
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = getActivityColor(activity.type).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activity.icon,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información de la actividad
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = AppColors.TextPrimary
                )

                if (activity.description.isNotBlank()) {
                    Text(
                        text = activity.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }

                Text(
                    text = activity.timestamp.toDateTimeString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextHint
                )
            }

            // Monto si está disponible
            if (activity.amount != null) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formatCurrency(activity.amount),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF10B981) // SuccessGreen
                    )
                }
            }
        }
    }
}

private fun getActivityColor(type: com.e17kapps.iepinpersonal.domain.model.ActivityType): Color {
    return when (type) {
        com.e17kapps.iepinpersonal.domain.model.ActivityType.PAYMENT -> Color(0xFF10B981) // SuccessGreen
        com.e17kapps.iepinpersonal.domain.model.ActivityType.EMPLOYEE_ADDED -> Color(0xFF3B82F6) // InfoBlue
        com.e17kapps.iepinpersonal.domain.model.ActivityType.DISCOUNT_APPLIED -> Color(0xFFFECA57) // WarningOrange
        com.e17kapps.iepinpersonal.domain.model.ActivityType.ADVANCE_REQUESTED -> Color(0xFF4ECDC4) // SecondaryTeal
        com.e17kapps.iepinpersonal.domain.model.ActivityType.ADVANCE_APPROVED -> Color(0xFF10B981) // SuccessGreen
        com.e17kapps.iepinpersonal.domain.model.ActivityType.EMPLOYEE_UPDATED -> Color(0xFF3B82F6) // InfoBlue
    }
}

