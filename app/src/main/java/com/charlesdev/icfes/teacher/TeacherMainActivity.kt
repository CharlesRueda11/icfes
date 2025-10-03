package com.charlesdev.icfes.teacher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charlesdev.icfes.auth.SplashActivity
import com.charlesdev.icfes.teacher.practice_evaluation.TeacherContentManagerActivity
import com.charlesdev.icfes.teacher.simulation.TeacherDashboardAddSimulationSection
import com.charlesdev.icfes.ui.theme.IcfesTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class TeacherMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            IcfesTheme {
                TeacherDashboardScreen()
            }
        }
    }
}

// 🎯 Data class para profesor
data class TeacherData(
    val nombre: String = "",
    val email: String = "",
    val institucion: String = "",
    val especialidad: String = "",
    val experiencia: String = "",
    val gruposActivos: Int = 0,
    val estudiantesBajoCargo: Int = 0,
    val evaluacionesCreadas: Int = 0,
    val nivelProfesor: Int = 1,
    val reputacion: Int = 100
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen() {
    val context = LocalContext.current
    var teacherData by remember { mutableStateOf(TeacherData()) }
    var isLoading by remember { mutableStateOf(true) }

    // 📊 Cargar datos del profesor desde Firebase
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            try {
                val database = FirebaseDatabase.getInstance()
                val snapshot = database.reference
                    .child("Profesores")
                    .child(currentUser.uid)
                    .get()
                    .await()

                teacherData = TeacherData(
                    nombre = snapshot.child("nombre").getValue(String::class.java) ?: "Profesor",
                    email = snapshot.child("email").getValue(String::class.java) ?: "",
                    institucion = snapshot.child("institucion").getValue(String::class.java) ?: "",
                    especialidad = snapshot.child("especialidad").getValue(String::class.java) ?: "",
                    experiencia = snapshot.child("experiencia").getValue(String::class.java) ?: "",
                    gruposActivos = snapshot.child("gruposActivos").getValue(Int::class.java) ?: 0,
                    estudiantesBajoCargo = snapshot.child("estudiantesBajoCargo").getValue(Int::class.java) ?: 0,
                    evaluacionesCreadas = snapshot.child("evaluacionesCreadas").getValue(Int::class.java) ?: 0,
                    nivelProfesor = snapshot.child("nivelProfesor").getValue(Int::class.java) ?: 1,
                    reputacion = snapshot.child("reputacion").getValue(Int::class.java) ?: 100
                )

                // Actualizar último acceso
                val timestamp = System.currentTimeMillis()
                database.reference.child("Profesores").child(currentUser.uid)
                    .child("ultimoAcceso").setValue(timestamp)
                database.reference.child("Usuarios").child(currentUser.uid)
                    .child("ultimoAcceso").setValue(timestamp)

                isLoading = false
            } catch (e: Exception) {
                teacherData = TeacherData(nombre = "Profesor")
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "ICFES EDUCADORES",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "Gestión de Estudiantes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Perfil */ }) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }
                    IconButton(onClick = { /* Configuración */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                    IconButton(
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            val intent = Intent(context, SplashActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cargando tu panel de profesor...")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { WelcomeHeaderTeacher(teacherData) }

                item { TeacherFeaturesPreview() }
                item { TeacherQuickStats(teacherData) }
            }
        }
    }
}

// 🎨 **ENCABEZADO PERSONALIZADO ESTILO ESTUDIANTE**
@Composable
fun WelcomeHeaderTeacher(teacherData: TeacherData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF7B1FA2), // Morado profesor
                            Color(0xFF3949AB)
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Psychology,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "¡Hola, Prof. ${teacherData.nombre.split(" ").firstOrNull() ?: "Educador"}!",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${teacherData.especialidad} • ${teacherData.institucion}",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            "Nivel ${teacherData.nivelProfesor}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

// 📈 **ESTADÍSTICAS RÁPIDAS DEL PROFESOR**
@Composable
fun TeacherQuickStats(teacherData: TeacherData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCardTeacher(
            modifier = Modifier.weight(1f),
            title = "Grupos Activos",
            value = "${teacherData.gruposActivos}",
            icon = Icons.Default.Groups,
            color = Color(0xFF7B1FA2)
        )

        StatCardTeacher(
            modifier = Modifier.weight(1f),
            title = "Estudiantes",
            value = "${teacherData.estudiantesBajoCargo}",
            icon = Icons.Default.School,
            color = Color(0xFF4CAF50)
        )

        StatCardTeacher(
            modifier = Modifier.weight(1f),
            title = "Evaluaciones",
            value = "${teacherData.evaluacionesCreadas}",
            icon = Icons.Default.Assessment,
            color = Color(0xFFFF9800)
        )
    }
}

@Composable
fun StatCardTeacher(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = color
            )
            Text(
                title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 🚀 **PREVIEW DE FUNCIONES DEL PROFESOR**
// 🚀 **PREVIEW DE FUNCIONES DEL PROFESOR** - ACTUALIZADO
@Composable
fun TeacherFeaturesPreview() {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // 🎯 **TARJETA DE BIENVENIDA ADICIONAL** (mantener igual)
       /* Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "¡Bienvenido a tu panel de gestión educativa!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Aquí podrás crear grupos, asignar evaluaciones y monitorear el progreso de tus estudiantes en tiempo real.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }*/
        // ✅ NUEVA CARD PRINCIPAL - GESTIÓN DE CONTENIDO ICFES
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
            onClick = {
                // TODO: Navegar a TeacherContentManagerActivity
                val intent = Intent(context, TeacherContentManagerActivity::class.java)
                context.startActivity(intent)
            },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono principal
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Contenido
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "🎯 GESTIÓN DE CONTENIDO ICFES",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Administra 35+35 preguntas por módulo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        "Crea contenido personalizado para tus estudiantes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }

                // Flecha
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }


        // ✅ NUEVA CARD - BANCO DE PREGUNTAS DUELO
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
            onClick = {
                val intent = Intent(context, com.charlesdev.icfes.teacher.duelo.TeacherDuelBankActivity::class.java)
                context.startActivity(intent)
            },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono principal
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Casino, // Icono de duelo/juego
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Contenido
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "⚔️ BANCO DE PREGUNTAS DUELO",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        "Gestiona preguntas para duelos ICFES",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        "Crea, edita y publica preguntas interactivas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                    )
                }

                // Flecha
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }


        // ✅ 2. Simulacros ICFES (inmediatamente después)
        TeacherDashboardAddSimulationSection(
            TeacherData(
                nombre = "Profesor",
                email = "profesor@example.com"
            )
        )



        // Separador
        Text(
            "🚀 Próximas Funciones",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        // El resto de las funciones (mantener como estaba)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                val features = listOf(
                    "📊 Creación de grupos personalizados" to Icons.Default.Groups,
                    "📝 Diseño de evaluaciones adaptativas" to Icons.Default.Edit,
                    "📈 Monitoreo en tiempo real de estudiantes" to Icons.Default.TrendingUp,
                    "🏆 Sistema de recompensas y logros" to Icons.Default.EmojiEvents,
                    "📅 Calendario de actividades" to Icons.Default.CalendarToday,
                    "🔔 Notificaciones personalizadas" to Icons.Default.Notifications
                )

                features.forEach { (feature, icon) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            feature,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }


    }
}