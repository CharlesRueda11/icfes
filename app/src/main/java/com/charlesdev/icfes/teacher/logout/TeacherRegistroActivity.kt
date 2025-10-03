package com.charlesdev.icfes.teacher.logout

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
import com.charlesdev.icfes.teacher.TeacherMainActivity
import com.charlesdev.icfes.ui.theme.IcfesTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class TeacherRegistroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IcfesTheme {
                TeacherRegistroScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherRegistroScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var institucion by remember { mutableStateOf("") }
    var especialidad by remember { mutableStateOf("") }
    var experiencia by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    val nombreFocus = remember { FocusRequester() }
    val emailFocus = remember { FocusRequester() }
    val passFocus = remember { FocusRequester() }
    val institucionFocus = remember { FocusRequester() }
    val especialidadFocus = remember { FocusRequester() }
    val experienciaFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val especialidades = listOf(
        "Matemáticas", "Física", "Química", "Biología",
        "Lectura Crítica", "Inglés", "Sociales", "Filosofía", "Todas las áreas"
    )
    var especialidadExpanded by remember { mutableStateOf(false) }

    val experiencias = listOf(
        "Menos de 1 año", "1-3 años", "3-5 años",
        "5-10 años", "Más de 10 años"
    )
    var experienciaExpanded by remember { mutableStateOf(false) }

    // Diálogo de términos y condiciones
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            icon = { Icon(Icons.Default.Description, contentDescription = null) },
            title = { Text("Términos y Condiciones") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "Al registrarte como educador en ICFES APP, aceptas:\n\n" +
                                "1. Usar la plataforma únicamente para fines educativos\n" +
                                "2. Respetar la privacidad de los estudiantes\n" +
                                "3. Mantener actualizada tu información académica\n" +
                                "4. No compartir credenciales de acceso\n" +
                                "5. Cumplir con las normativas educativas vigentes"
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Entendido")
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
                        Text("Registro de Profesor")
                        Text(
                            "ICFES Educadores",
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
                Icons.Default.Psychology,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                "Únete como educador ICFES",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                "Gestiona grupos, crea evaluaciones y monitorea el progreso estudiantil",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Formulario mejorado
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    if (it.length > 50) nombre = it.take(50)
                },
                label = { Text("Nombre completo *") },
                placeholder = { Text("Ej: María González Pérez") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(nombreFocus),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { emailFocus.requestFocus() }
                ),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                isError = nombre.isNotEmpty() && (nombre.length < 3 || nombre.length > 50),
                supportingText = {
                    Text("${nombre.length}/50 caracteres")
                }
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim() },
                label = { Text("Correo electrónico *") },
                placeholder = { Text("profesor@institucion.edu.co") },
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
                } else {
                    { Text("Usa tu correo institucional si es posible") }
                }
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña *") },
                placeholder = { Text("Mínimo 6 caracteres") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passFocus),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { institucionFocus.requestFocus() }
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
                supportingText = {
                    if (password.isNotEmpty() && password.length < 6) {
                        Text("Mínimo 6 caracteres")
                    } else {
                        Text("Seguridad: ${getPasswordStrength(password)}")
                    }
                }
            )

            OutlinedTextField(
                value = institucion,
                onValueChange = {
                    institucion = it
                    if (it.length > 100) institucion = it.take(100)
                },
                label = { Text("Institución educativa *") },
                placeholder = { Text("Ej: Colegio Nacional San José") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(institucionFocus),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.clearFocus() }
                ),
                leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                isError = institucion.isNotEmpty() && institucion.length < 3,
                supportingText = {
                    Text("${institucion.length}/100 caracteres")
                }
            )

            // Dropdown para especialidad
            ExposedDropdownMenuBox(
                expanded = especialidadExpanded,
                onExpandedChange = { especialidadExpanded = !especialidadExpanded }
            ) {
                OutlinedTextField(
                    value = especialidad,
                    onValueChange = { },
                    label = { Text("Área de especialidad *") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    leadingIcon = { Icon(Icons.Default.Subject, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = especialidadExpanded) }
                )

                ExposedDropdownMenu(
                    expanded = especialidadExpanded,
                    onDismissRequest = { especialidadExpanded = false }
                ) {
                    especialidades.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                especialidad = option
                                especialidadExpanded = false
                            }
                        )
                    }
                }
            }

            // Dropdown para experiencia
            ExposedDropdownMenuBox(
                expanded = experienciaExpanded,
                onExpandedChange = { experienciaExpanded = !experienciaExpanded }
            ) {
                OutlinedTextField(
                    value = experiencia,
                    onValueChange = { },
                    label = { Text("Años de experiencia *") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    leadingIcon = { Icon(Icons.Default.WorkHistory, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = experienciaExpanded) }
                )

                ExposedDropdownMenu(
                    expanded = experienciaExpanded,
                    onDismissRequest = { experienciaExpanded = false }
                ) {
                    experiencias.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                experiencia = option
                                experienciaExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Términos y condiciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showTermsDialog = true }) {
                    Text("Ver términos y condiciones")
                }
            }

            // Botón de registro mejorado
            Button(
                onClick = {
                    when {
                        nombre.isEmpty() || nombre.length < 3 -> scope.launch {
                            snackbarHostState.showSnackbar("Ingresa tu nombre completo (mínimo 3 caracteres)")
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
                        institucion.isEmpty() || institucion.length < 3 -> scope.launch {
                            snackbarHostState.showSnackbar("Ingresa tu institución educativa")
                        }
                        especialidad.isEmpty() -> scope.launch {
                            snackbarHostState.showSnackbar("Selecciona tu área de especialidad")
                        }
                        experiencia.isEmpty() -> scope.launch {
                            snackbarHostState.showSnackbar("Selecciona tus años de experiencia")
                        }
                        else -> {
                            isLoading = true
                            scope.launch {
                                try {
                                    // Verificar si el email ya existe
                                    if (verificarProfesorExistente(email)) {
                                        snackbarHostState.showSnackbar("Este correo ya está registrado como profesor")
                                        isLoading = false
                                        return@launch
                                    }

                                    val auth = FirebaseAuth.getInstance()
                                    val result = auth.createUserWithEmailAndPassword(email, password).await()
                                    val uid = result.user?.uid ?: throw Exception("No se obtuvo UID")

                                    // Datos separados por nodos
                                    val fechaActual = System.currentTimeMillis()
                                    val fechaFormateada = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                        .format(Date(fechaActual))

                                    // Datos mínimos para Usuarios
                                    val usuarioData = mapOf(
                                        "uid" to uid,
                                        "tipoUsuario" to "profesor",
                                        "activo" to true,
                                        "ultimoAcceso" to fechaActual,
                                        "email" to email
                                    )

                                    // Datos completos para Profesores
                                    val profesorData = mapOf(
                                        "uid" to uid,
                                        "nombre" to nombre.trim(),
                                        "email" to email.trim(),
                                        "institucion" to institucion.trim(),
                                        "especialidad" to especialidad,
                                        "experiencia" to experiencia,
                                        "fechaRegistro" to fechaActual,
                                        "fechaRegistroFormateada" to fechaFormateada,

                                        // Métricas iniciales
                                        "gruposActivos" to 0,
                                        "estudiantesBajoCargo" to 0,
                                        "evaluacionesCreadas" to 0,
                                        "nivelProfesor" to 1,
                                        "reputacion" to 100,

                                        // Configuración
                                        "notificacionesActivas" to true,
                                        "temaOscuro" to false,
                                        "idioma" to "es",

                                        // Seguridad
                                        "verificado" to false,
                                        "intentosFallidos" to 0,
                                        "ultimoCambioPassword" to fechaActual,

                                        // Metadatos
                                        "versionApp" to "1.0",
                                        "dispositivo" to "android"
                                    )

                                    val database = FirebaseDatabase.getInstance()

                                    // Guardar en nodo Usuarios (solo datos básicos)
                                    database.reference.child("Usuarios").child(uid)
                                        .setValue(usuarioData).await()

                                    // Guardar en nodo Profesores (datos completos)
                                    database.reference.child("Profesores").child(uid)
                                        .setValue(profesorData).await()

                                    // Enviar verificación de email
                                    auth.currentUser?.sendEmailVerification()?.await()

                                    snackbarHostState.showSnackbar("¡Registro exitoso! Verifica tu correo para activar tu cuenta")

                                    // Pequeña pausa para que el usuario vea el mensaje
                                    kotlinx.coroutines.delay(2000)

                                    val intent = Intent(context, TeacherMainActivity::class.java)
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
                        Icon(Icons.Default.Psychology, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crear mi cuenta de educador")
                    }
                }
            }

            // Link a login
            TextButton(onClick = {
                context.startActivity(Intent(context, TeacherLoginActivity::class.java))
                (context as ComponentActivity).finish()
            }) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}

// Función auxiliar para verificar existencia
private suspend fun verificarProfesorExistente(email: String): Boolean {
    val database = FirebaseDatabase.getInstance()
    val profesoresRef = database.getReference("Profesores")

    return try {
        val snapshot = profesoresRef.orderByChild("email").equalTo(email).get().await()
        snapshot.exists()
    } catch (e: Exception) {
        false
    }
}

// Función auxiliar para fortaleza de contraseña
private fun getPasswordStrength(password: String): String {
    return when {
        password.length < 6 -> "Débil"
        password.length < 8 -> "Media"
        password.any { it.isDigit() } && password.any { it.isLetter() } -> "Fuerte"
        else -> "Media"
    }
}