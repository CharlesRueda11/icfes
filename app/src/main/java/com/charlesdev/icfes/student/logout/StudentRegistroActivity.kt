package com.charlesdev.icfes.student.logout

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
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
import com.charlesdev.icfes.student.components.SmartInstitutionField
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class StudentRegistroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IcfesTheme {
                StudentRegistroScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentRegistroScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var institutionData by remember { mutableStateOf(InstitutionData()) }
    var grado by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val nombreFocus = remember { FocusRequester() }
    val emailFocus = remember { FocusRequester() }
    val passFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val grados = listOf("10°", "11°", "Egresado", "Universitario")
    var gradoExpanded by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Registro de Estudiante")
                        Text(
                            "ICFES Saber 11",
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono y título
            Icon(
                Icons.Default.School,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                "Únete a la preparación ICFES",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                "Prepárate con Milton Ochoa y alcanza tu mejor puntaje",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Formulario
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre completo") },
                placeholder = { Text("Ej: Juan Pérez") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(nombreFocus),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { emailFocus.requestFocus() }
                ),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                isError = nombre.isNotEmpty() && nombre.length < 3,
                supportingText = if (nombre.isNotEmpty() && nombre.length < 3) {
                    { Text("El nombre debe tener al menos 3 caracteres") }
                } else null
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                placeholder = { Text("ejemplo@correo.com") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(emailFocus),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passFocus.requestFocus() }
                ),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                isError = email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches(),
                supportingText = if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    { Text("Formato de correo inválido") }
                } else null
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                placeholder = { Text("Mínimo 6 caracteres") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passFocus),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                isError = password.isNotEmpty() && password.length < 6,
                supportingText = if (password.isNotEmpty() && password.length < 6) {
                    { Text("La contraseña debe tener al menos 6 caracteres") }
                } else null
            )

            // 🎯 NUEVO: Campo inteligente para institución
            SmartInstitutionField(
                institutionData = institutionData,
                onInstitutionChange = { institutionData = it },
                modifier = Modifier.fillMaxWidth(),
                isEnabled = !isLoading
            )

            // Dropdown para grado
            ExposedDropdownMenuBox(
                expanded = gradoExpanded,
                onExpandedChange = { gradoExpanded = !gradoExpanded }
            ) {
                OutlinedTextField(
                    value = grado,
                    onValueChange = { },
                    label = { Text("Grado actual") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradoExpanded) }
                )

                ExposedDropdownMenu(
                    expanded = gradoExpanded,
                    onDismissRequest = { gradoExpanded = false }
                ) {
                    grados.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                grado = option
                                gradoExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón de registro
            Button(
                onClick = {
                    when {
                        nombre.isEmpty() -> scope.launch {
                            snackbarHostState.showSnackbar("Ingresa tu nombre completo")
                        }
                        nombre.length < 3 -> scope.launch {
                            snackbarHostState.showSnackbar("El nombre debe tener al menos 3 caracteres")
                        }
                        email.isEmpty() -> scope.launch {
                            snackbarHostState.showSnackbar("Ingresa un correo válido")
                        }
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> scope.launch {
                            snackbarHostState.showSnackbar("Formato de correo inválido")
                        }
                        password.length < 6 -> scope.launch {
                            snackbarHostState.showSnackbar("La contraseña debe tener al menos 6 caracteres")
                        }
                        institutionData.name.isEmpty() -> scope.launch {
                            snackbarHostState.showSnackbar("Ingresa tu institución educativa")
                        }
                        grado.isEmpty() -> scope.launch {
                            snackbarHostState.showSnackbar("Selecciona tu grado actual")
                        }
                        else -> {
                            isLoading = true
                            scope.launch {
                                try {
                                    val auth = FirebaseAuth.getInstance()
                                    val result = auth.createUserWithEmailAndPassword(email, password).await()
                                    val uid = result.user?.uid ?: throw Exception("No se obtuvo UID")

                                    // 🎯 ACTUALIZADO: Guardar datos completos de la institución
                                    val estudianteData = mapOf(
                                        "uid" to uid,
                                        "nombre" to nombre,
                                        "email" to email,
                                        "institucion" to institutionData.name,
                                        "municipio" to institutionData.municipality,
                                        "departamento" to institutionData.department,
                                        "sectorInstitucion" to institutionData.sector,
                                        "nivelInstitucion" to institutionData.level,
                                        "institucionValidada" to institutionData.isValidated,
                                        "grado" to grado,
                                        "tipoUsuario" to "estudiante",
                                        "tiempoRegistro" to System.currentTimeMillis(),
                                        "activo" to true,
                                        // Datos específicos para gamificación ICFES
                                        "nivel" to 1,
                                        "experiencia" to 0,
                                        "racha" to 0,
                                        "puntajeTotal" to 0,
                                        "modulosCompletados" to 0,
                                        "tiempoEstudioTotal" to 0,
                                        "objetivoPuntaje" to 300,
                                        // Metadatos de registro
                                        "versionApp" to "1.0",
                                        "fechaRegistroFormateada" to java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date())
                                    )

                                    val database = FirebaseDatabase.getInstance()
                                    // Guardar en nodo Usuarios para gestión general
                                    database.reference.child("Usuarios").child(uid).setValue(estudianteData).await()
                                    // Guardar en nodo Estudiantes para datos específicos
                                    database.reference.child("Estudiantes").child(uid).setValue(estudianteData).await()

                                    // 🎯 NUEVO: Mensaje personalizado basado en la validación y ubicación
                                    val welcomeMessage = when {
                                        institutionData.isValidated && institutionData.municipality.isNotEmpty() -> {
                                            "¡Registro exitoso! Bienvenido desde ${institutionData.municipality}, ${institutionData.department}"
                                        }
                                        institutionData.department.contains("Santander", ignoreCase = true) -> {
                                            "¡Registro exitoso! Orgulloso de tener otro santandereano preparándose para el ICFES"
                                        }
                                        else -> {
                                            "¡Registro exitoso! Bienvenido a la preparación ICFES"
                                        }
                                    }

                                    snackbarHostState.showSnackbar(welcomeMessage)

                                    // Pequeña pausa para que el usuario vea el mensaje
                                    kotlinx.coroutines.delay(1500)

                                    val intent = Intent(context, StudentMainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)

                                } catch (e: Exception) {
                                    val errorMessage = when {
                                        e.message?.contains("email address is already in use") == true ->
                                            "Este correo ya está registrado"
                                        e.message?.contains("The email address is badly formatted") == true ->
                                            "Formato de correo inválido"
                                        e.message?.contains("network error") == true ->
                                            "Error de conexión. Verifica tu internet"
                                        e.message?.contains("weak-password") == true ->
                                            "La contraseña es muy débil"
                                        else -> "Error al registrar: ${e.message}"
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
                        Text("Registrando...")
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.School, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Comenzar mi preparación ICFES")
                    }
                }
            }

            // Link a login
            TextButton(onClick = {
                context.startActivity(Intent(context, StudentLoginActivity::class.java))
                (context as ComponentActivity).finish()
            }) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }

            // Información adicional si la institución está validada
            AnimatedVisibility(
                visible = institutionData.isValidated && institutionData.sector.isNotEmpty()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Institución verificada",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            "Tu institución ha sido validada automáticamente con datos oficiales",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}