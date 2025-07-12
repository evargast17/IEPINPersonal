package com.e17kapps.iepinpersonal.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.e17kapps.iepinpersonal.domain.model.AuthState
import com.e17kapps.iepinpersonal.ui.components.LoadingButton
import com.e17kapps.iepinpersonal.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.loginUiState.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }

    // 🔥 NUEVO: Validación en tiempo real
    val isEmailValid = remember(uiState.email) {
        uiState.email.isBlank() || android.util.Patterns.EMAIL_ADDRESS.matcher(uiState.email).matches()
    }

    val isPasswordValid = remember(uiState.password) {
        uiState.password.isBlank() || uiState.password.length >= 6
    }

    val isFormValid = remember(uiState.email, uiState.password) {
        uiState.email.isNotBlank() &&
                uiState.password.isNotBlank() &&
                isEmailValid &&
                isPasswordValid
    }


    // Observar el éxito del login
    // 🔥 MEJORADO: Observar estados de auth
    LaunchedEffect(authState) {
        when (authState) {
            is com.e17kapps.iepinpersonal.domain.model.AuthState.Authenticated -> {
                keyboardController?.hide()
                onLoginSuccess()
            }
            is com.e17kapps.iepinpersonal.domain.model.AuthState.Error -> {
                snackbarHostState.showSnackbar((authState as AuthState.Error).message)
            }
            else -> { /* Loading o otros estados */ }
        }
    }

    // 🔥 NUEVO: Limpiar errores cuando el usuario empiece a escribir
    LaunchedEffect(uiState.email, uiState.password) {
        if (uiState.errorMessage != null) {
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColors.GradientStart,
                        AppColors.GradientEnd
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.ime) // Ajuste automático del teclado

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header con logo y título
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                // Logo placeholder
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = AppColors.surface.copy(alpha = 0.2f),
                            shape = MaterialTheme.shapes.large
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👥",
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "IEPIN Personal",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AppColors.onPrimary
                )

                Text(
                    text = "Bienvenido de nuevo",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.onPrimary.copy(alpha = 0.8f)
                )
            }

            // Formulario de login
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AppColors.TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Campo de email
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = viewModel::updateEmail,
                        label = { Text("Correo Electrónico") },
                        placeholder = { Text("ejemplo@correo.com") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true,
                        isError = !isEmailValid && uiState.email.isNotBlank(),
                        supportingText = {
                            if (!isEmailValid && uiState.email.isNotBlank()) {
                                Text(
                                    text = "Ingresa un email válido",
                                    color = AppColors.error
                                )
                            }
                        }
                    )

                    // Campo de contraseña
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = viewModel::updatePassword,
                        label = { Text("Contraseña") },
                        placeholder = { Text("Mínimo 6 caracteres") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = viewModel::togglePasswordVisibility
                            ) {
                                Icon(
                                    imageVector = if (uiState.isPasswordVisible) {
                                        Icons.Default.VisibilityOff
                                    } else {
                                        Icons.Default.Visibility
                                    },
                                    contentDescription = if (uiState.isPasswordVisible) {
                                        "Ocultar contraseña"
                                    } else {
                                        "Mostrar contraseña"
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (uiState.isPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                if (isFormValid) {
                                    viewModel.login()
                                }
                            }
                        ),
                        singleLine = true,
                        isError = !isPasswordValid && uiState.password.isNotBlank(),
                        supportingText = {
                            if (!isPasswordValid && uiState.password.isNotBlank()) {
                                Text(
                                    text = "La contraseña debe tener al menos 6 caracteres",
                                    color = AppColors.error
                                )
                            }
                        }
                    )

                    // Mensaje de error MEJORADO (solo errores específicos de auth)
                    if (uiState.errorMessage != null && authState is com.e17kapps.iepinpersonal.domain.model.AuthState.Error) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = AppColors.error
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚠️",
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = uiState.errorMessage!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.error
                                )
                            }
                        }
                    }

                    // Botón de login MEJORADO
                    LoadingButton(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.login()
                        },
                        modifier = Modifier.fillMaxWidth()
                            .semantics {
                                contentDescription = if (isFormValid) {
                                    "Botón de iniciar sesión habilitado"
                                } else {
                                    "Completa el formulario para iniciar sesión"
                                }
                            },
                        isLoading = uiState.isLoading,
                        enabled = isFormValid && !uiState.isLoading
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!uiState.isLoading) {
                                Text(
                                    text = "🔐",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                            Text(
                                text = if (uiState.isLoading) "Iniciando sesión..." else "Iniciar Sesión",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    // Checkbox "Recordar sesión"
                    var rememberMe by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AppColors.GradientStart
                            )
                        )
                        Text(
                            text = "Recordar sesión",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSecondary,
                            modifier = Modifier.padding(start = 8.dp)
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Mostrar estado de conexión
                        if (uiState.isLoading) {
                            Text(
                                text = "Conectando...",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.GradientStart
                            )
                        }
                    }

                    // Forgot password
                    TextButton(
                        onClick = onNavigateToForgotPassword,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.GradientStart
                        )
                    }

                    // Divider
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = AppColors.DividerLight
                    )

                    // Register button
                    OutlinedButton(
                        onClick = onNavigateToRegister,
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    AppColors.GradientStart,
                                    AppColors.GradientEnd
                                )
                            )
                        )
                    ) {
                        Text(
                            text = "Crear Nueva Cuenta",
                            style = MaterialTheme.typography.labelLarge,
                            color = AppColors.GradientStart
                        )
                    }
                }
            }
        }
        //Snackbar para mostrar mensajes
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

}