package com.charlesdev.icfes.student.logout

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.charlesdev.icfes.student.StudentMainActivity
import com.charlesdev.icfes.ui.theme.IcfesTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class StudentLoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IcfesTheme {
                StudentLoginScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLoginScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var recoveryEmail by remember { mutableStateOf("") }

    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Diálogo de recuperación de contraseña
    if (showRecoveryDialog) {
        AlertDialog(
            onDismissRequest = { showRecoveryDialog = false },
            icon = { Icon(Icons.Default.Email, contentDescription = null) },
            title = { Text("Recuperar Contraseña") },
            text = {
                Column {
                    Text("Ingresa tu correo electrónico para recibir un enlace de recuperación:")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = recoveryEmail,
                        onValueChange = { recoveryEmail = it },
                        label = { Text("Correo electrónico") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (recoveryEmail.isNotEmpty() &&
                            android.util.Patterns.EMAIL_ADDRESS.matcher(recoveryEmail).matches()) {
                            scope.launch {
                                try {
                                    FirebaseAuth.getInstance().sendPasswordResetEmail(recoveryEmail).await()
                                    snackbarHostState.showSnackbar("Correo de recuperación enviado")
                                    showRecoveryDialog = false
                                    recoveryEmail = ""
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Error: ${e.message}")
                                }
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Ingresa un correo válido")
                            }
                        }
                    }
                ) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRecoveryDialog = false
                    recoveryEmail = ""
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Iniciar Sesión")
                        Text(
                            "ICFES Estudiantes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono y título
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "Estudiante ICFES",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "¡Bienvenido de vuelta!",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "Continúa tu preparación ICFES",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Formulario
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                placeholder = { Text("ejemplo@correo.com") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(emailFocusRequester),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                isError = email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                placeholder = { Text("Tu contraseña") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                }
            )

            // Botón de login
            Button(
                onClick = {
                    when {
                        email.isEmpty() -> scope.launch {
                            snackbarHostState.showSnackbar("Ingresa tu correo electrónico")
                        }
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> scope.launch {
                            snackbarHostState.showSnackbar("Formato de correo inválido")
                        }
                        password.isEmpty() -> scope.launch {
                            snackbarHostState.showSnackbar("Ingresa tu contraseña")
                        }
                        else -> {
                            isLoading = true
                            scope.launch {
                                try {
                                    val authResult = FirebaseAuth.getInstance()
                                        .signInWithEmailAndPassword(email, password)
                                        .await()

                                    val uid = authResult.user?.uid ?: throw Exception("Usuario no autenticado")
                                    val database = FirebaseDatabase.getInstance()
                                    val userSnapshot = database.reference.child("Usuarios").child(uid).get().await()
                                    val tipoUsuario = userSnapshot.child("tipoUsuario").getValue(String::class.java)
                                    val activo = userSnapshot.child("activo").getValue(Boolean::class.java) ?: false

                                    if (tipoUsuario == "estudiante" && activo) {
                                        // Actualizar última actividad
                                        database.reference.child("Usuarios").child(uid)
                                            .child("ultimaActividad").setValue(System.currentTimeMillis())

                                        snackbarHostState.showSnackbar("¡Bienvenido! Continuemos tu preparación")
                                        val intent = Intent(context, StudentMainActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        context.startActivity(intent)
                                    } else if (tipoUsuario != "estudiante") {
                                        FirebaseAuth.getInstance().signOut()
                                        snackbarHostState.showSnackbar("Este correo está registrado como profesor. Usa la opción correspondiente.")
                                    } else {
                                        FirebaseAuth.getInstance().signOut()
                                        snackbarHostState.showSnackbar("Tu cuenta está inactiva. Contacta al administrador.")
                                    }

                                } catch (e: Exception) {
                                    val errorMessage = when {
                                        e.message?.contains("password is invalid") == true ||
                                                e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ->
                                            "Correo o contraseña incorrectos"
                                        e.message?.contains("user not found") == true ->
                                            "Usuario no encontrado"
                                        e.message?.contains("network error") == true ->
                                            "Error de conexión. Verifica tu internet"
                                        e.message?.contains("too many requests") == true ->
                                            "Demasiados intentos. Espera un momento"
                                        else -> "Error: ${e.message}"
                                    }
                                    snackbarHostState.showSnackbar(errorMessage)
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Iniciando sesión...")
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Iniciar sesión")
                    }
                }
            }

            // Links adicionales
            TextButton(
                onClick = {
                    recoveryEmail = email
                    showRecoveryDialog = true
                }
            ) {
                Icon(Icons.Default.Help, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("¿Olvidaste tu contraseña?")
            }

            TextButton(onClick = {
                context.startActivity(Intent(context, StudentRegistroActivity::class.java))
                (context as ComponentActivity).finish()
            }) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("¿No tienes cuenta? Regístrate")
            }
        }
    }
}