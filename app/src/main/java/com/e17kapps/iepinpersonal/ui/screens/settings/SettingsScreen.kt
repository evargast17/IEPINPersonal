package com.e17kapps.iepinpersonal.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.e17kapps.iepinpersonal.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var isDarkMode by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showDataDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // TopAppBar
        TopAppBar(
            title = {
                Text(
                    text = "Configuración",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Regresar"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = AppColors.TextPrimary
            )
        )

        // Contenido
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección de Apariencia
            SettingsSection(title = "Apariencia") {
                SettingsSwitchItem(
                    icon = Icons.Default.DarkMode,
                    title = "Modo Oscuro",
                    description = "Cambiar tema de la aplicación",
                    checked = isDarkMode,
                    onCheckedChange = { isDarkMode = it }
                )
            }

            // Sección de Notificaciones
            SettingsSection(title = "Notificaciones") {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Notificaciones Push",
                    description = "Recibir notificaciones de la app",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )

                SettingsClickableItem(
                    icon = Icons.Default.Schedule,
                    title = "Recordatorios",
                    description = "Configurar recordatorios de pagos",
                    onClick = { /* TODO: Implementar */ }
                )
            }

            // Sección de Datos
            SettingsSection(title = "Datos y Privacidad") {
                SettingsClickableItem(
                    icon = Icons.Default.Backup,
                    title = "Respaldo de Datos",
                    description = "Respaldar información en la nube",
                    onClick = { showDataDialog = true }
                )

                SettingsClickableItem(
                    icon = Icons.Default.Download,
                    title = "Exportar Datos",
                    description = "Descargar datos en formato Excel",
                    onClick = { /* TODO: Implementar */ }
                )

                SettingsClickableItem(
                    icon = Icons.Default.Security,
                    title = "Privacidad",
                    description = "Configuración de privacidad",
                    onClick = { /* TODO: Implementar */ }
                )
            }

            // Sección de Soporte
            SettingsSection(title = "Soporte") {
                SettingsClickableItem(
                    icon = Icons.Default.Help,
                    title = "Centro de Ayuda",
                    description = "Preguntas frecuentes y guías",
                    onClick = { /* TODO: Implementar */ }
                )

                SettingsClickableItem(
                    icon = Icons.Default.Email,
                    title = "Contactar Soporte",
                    description = "Enviar consulta o reporte de bug",
                    onClick = { /* TODO: Implementar */ }
                )

                SettingsClickableItem(
                    icon = Icons.Default.Star,
                    title = "Calificar App",
                    description = "Deja tu reseña en la tienda",
                    onClick = { /* TODO: Implementar */ }
                )
            }

            // Sección Acerca de
            SettingsSection(title = "Acerca de") {
                SettingsInfoItem(
                    icon = Icons.Default.Info,
                    title = "Versión",
                    description = "1.0.0"
                )

                SettingsInfoItem(
                    icon = Icons.Default.Update,
                    title = "Última actualización",
                    description = "Julio 2025"
                )

                SettingsClickableItem(
                    icon = Icons.Default.Description,
                    title = "Términos y Condiciones",
                    description = "Leer términos de uso",
                    onClick = { /* TODO: Implementar */ }
                )

                SettingsClickableItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Política de Privacidad",
                    description = "Ver política de privacidad",
                    onClick = { /* TODO: Implementar */ }
                )
            }

            // Espaciado final
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Dialog de respaldo de datos
    if (showDataDialog) {
        AlertDialog(
            onDismissRequest = { showDataDialog = false },
            title = {
                Text("Respaldo de Datos")
            },
            text = {
                Text("Esta función estará disponible en una próxima actualización.")
            },
            confirmButton = {
                TextButton(
                    onClick = { showDataDialog = false }
                ) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(16.dp)
            )

            content()
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.TextSecondary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = AppColors.TextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppColors.GradientStart,
                checkedTrackColor = AppColors.GradientStart.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.TextSecondary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = AppColors.TextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
        }

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = AppColors.TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsInfoItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.TextSecondary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = AppColors.TextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
        }
    }
}