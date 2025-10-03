package com.charlesdev.icfes.student

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charlesdev.icfes.auth.SplashActivity
import com.charlesdev.icfes.student.duelo.DuelICFESActivity
import com.charlesdev.icfes.student.premium.StudentTeacherContentActivity
import com.charlesdev.icfes.ui.theme.IcfesTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ✅ DATA CLASSES ORIGINALES (sin cambios)
data class StudentData(
    val nombre: String = "",
    val email: String = "",
    val institucion: String = "",
    val grado: String = "",
    val nivel: Int = 1,
    val experiencia: Int = 0,
    val racha: Int = 0,
    val puntajeTotal: Int = 0,
    val modulosCompletados: Int = 0,
    val tiempoEstudioTotal: Int = 0,
    val objetivoPuntaje: Int = 300
)

data class ModuloICFES(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val icono: ImageVector,
    val progreso: Int = 0,
    val totalEjercicios: Int = 100,
    val color: Color
)

// ✅ NUEVAS DATA CLASSES PARA FUNCIONALIDADES AVANZADAS
data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val progress: Float,
    val timeLeft: String,
    val color: Color
)

data class TopStudent(
    val name: String,
    val score: Int
)

class StudentMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IcfesTheme {
                StudentMainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentMainScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var studentData by remember { mutableStateOf(StudentData()) }
    var isLoading by remember { mutableStateOf(true) }


    // ✅ DESCOMENTAR: Estado para contenido docente
    var teacherContent by remember { mutableStateOf<TeacherContentInfo?>(null) }
    var isLoadingTeacherContent by remember { mutableStateOf(true) }

    // ✅ MISMA LÓGICA DE CARGA DE FIREBASE (sin cambios)
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            try {
                val database = FirebaseDatabase.getInstance()
                val snapshot = database.reference.child("Usuarios").child(currentUser.uid).get().await()

                studentData = StudentData(
                    nombre = snapshot.child("nombre").getValue(String::class.java) ?: "",
                    email = snapshot.child("email").getValue(String::class.java) ?: "",
                    institucion = snapshot.child("institucion").getValue(String::class.java) ?: "",
                    grado = snapshot.child("grado").getValue(String::class.java) ?: "",
                    nivel = snapshot.child("nivel").getValue(Int::class.java) ?: 1,
                    experiencia = snapshot.child("experiencia").getValue(Int::class.java) ?: 0,
                    racha = snapshot.child("racha").getValue(Int::class.java) ?: 0,
                    puntajeTotal = snapshot.child("puntajeTotal").getValue(Int::class.java) ?: 0,
                    modulosCompletados = snapshot.child("modulosCompletados").getValue(Int::class.java) ?: 0,
                    tiempoEstudioTotal = snapshot.child("tiempoEstudioTotal").getValue(Int::class.java) ?: 0,
                    objetivoPuntaje = snapshot.child("objetivoPuntaje").getValue(Int::class.java) ?: 300
                )

                // ✅ ESTO ES LO QUE TE FALTA - AGREGAR AQUÍ:
                loadTeacherContentForStudent(studentData.institucion) { content ->
                    println("🔍 DEBUG - Institución estudiante: ${studentData.institucion}")
                    println("🔍 DEBUG - Contenido encontrado: $content")
                    teacherContent = content
                    isLoadingTeacherContent = false
                }

                isLoading = false
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Error al cargar datos: ${e.message}")
                }
                isLoading = false
                // ✅ TAMBIÉN AGREGAR ESTO EN EL CATCH:
                isLoadingTeacherContent = false
            }
        } else {
            // ✅ Y TAMBIÉN AQUÍ:
            isLoadingTeacherContent = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "ICFES APP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "Preparación Saber 11",
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
                    Text("Cargando tu progreso...")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ✅ SECCIONES ORIGINALES (mantener igual)
                WelcomeHeader(studentData)
                //QuickStats(studentData)
                //ProgressSection(studentData)
                ICFESAdvancedCard()

                // ✅ NUEVO: Contenido del profesor (solo si está disponible)
                if (!isLoadingTeacherContent && teacherContent != null) {
                    TeacherContentCard(
                        teacherContent = teacherContent!!,
                        studentData = studentData
                    )
                }

                ICFESAdvancedCardDuelo()

                // 🚀 NUEVAS SECCIONES (Opción A - Control Center Completo)

                // 1. COMUNIDAD Y RANKING
                CommunitySection(studentData)

                // 2. DESAFÍOS Y METAS
                ChallengesSection(studentData)



                // 4. CALENDARIO DE ESTUDIO
                StudyCalendarSection(studentData)

                // 5. NOTICIAS Y ANUNCIOS
                NewsSection()

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ✅ FUNCIONES ORIGINALES (sin cambios) - WelcomeHeader, QuickStats, ProgressSection


@Composable
fun ICFESAdvancedCardDuelo() {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            // Navegación al Duelo ICFES
            val intent = Intent(context, DuelICFESActivity::class.java)
            context.startActivity(intent)
        },
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E88E5),  // Azul
                            Color(0xFF00ACC1),  // Cyan
                            Color(0xFF26A69A)   // Teal
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icono principal con animación
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "🎯 DUELO ICFES",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            "Competencia en tiempo real",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Badge "NUEVO"
                    Surface(
                        color = Color(0xFFFF5722),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "NUEVO",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Descripción
                Text(
                    "Desafía a otros estudiantes en duelos de conocimiento ICFES",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.95f),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Características del duelo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DuelFeature(
                        icon = Icons.Default.People,
                        text = "1v1 hasta 4v4",
                        description = "Equipos balanceados"
                    )
                    DuelFeature(
                        icon = Icons.Default.Timer,
                        text = "25s por pregunta",
                        description = "Ritmo dinámico"
                    )
                    DuelFeature(
                        icon = Icons.Default.Quiz,
                        text = "20 preguntas",
                        description = "Todas las áreas"
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botón de acción
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFF1E88E5)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Iniciar Duelo ICFES",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E88E5)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DuelFeature(
    icon: ImageVector,
    text: String,
    description: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.White.copy(alpha = 0.9f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WelcomeHeader(studentData: StudentData) {
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
                            Color(0xFF1E88E5),
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
                        Icons.Default.School,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "¡Hola, ${studentData.nombre.split(" ").firstOrNull() ?: "Estudiante"}!",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${studentData.grado} - ${studentData.institucion}",
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
                            "Nivel ${studentData.nivel}",
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

@Composable
fun QuickStats(studentData: StudentData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Experiencia",
            value = "${studentData.experiencia}",
            icon = Icons.Default.Star,
            color = Color(0xFFFFB300)
        )

        StatCard(
            modifier = Modifier.weight(1f),
            title = "Racha",
            value = "${studentData.racha} días",
            icon = Icons.Default.LocalFireDepartment,
            color = Color(0xFFFF5722)
        )

        StatCard(
            modifier = Modifier.weight(1f),
            title = "Puntaje",
            value = "${studentData.puntajeTotal}",
            icon = Icons.Default.EmojiEvents,
            color = Color(0xFF4CAF50)
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
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

@Composable
fun ProgressSection(studentData: StudentData) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Tu Progreso hacia ${studentData.objetivoPuntaje} puntos",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            val progress = (studentData.puntajeTotal.toFloat() / studentData.objetivoPuntaje).coerceAtMost(1f)

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${studentData.puntajeTotal} puntos",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Objetivo: ${studentData.objetivoPuntaje}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// 🏆 NUEVAS SECCIONES AVANZADAS (OPCIÓN A)

@Composable
fun CommunitySection(studentData: StudentData) {
    Text(
        "👥 Comunidad ICFES",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Tu Posición Nacional",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Entre ${getEstimatedUsers()} estudiantes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    "#${calculateRanking(studentData)}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "🔥 Top de tu región (${getRegion()})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            getTopStudents().forEachIndexed { index, student ->
                RankingItem(
                    position = index + 1,
                    name = student.name,
                    score = student.score,
                    isCurrentUser = student.name == studentData.nombre.split(" ").first()
                )
                if (index < 2) Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Abrir ranking completo */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Leaderboard, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver Ranking")
                }

                Button(
                    onClick = { /* Abrir grupos de estudio */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(Icons.Default.Group, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Grupos")
                }
            }
        }
    }
}

@Composable
fun RankingItem(position: Int, name: String, score: Int, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val medal = when (position) {
            1 -> "🥇"
            2 -> "🥈"
            3 -> "🥉"
            else -> "$position°"
        }

        Text(
            medal,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(32.dp)
        )

        Text(
            if (isCurrentUser) "Tú" else name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrentUser) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Text(
            "$score pts",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )
    }
}

@Composable
fun ChallengesSection(studentData: StudentData) {
    Text(
        "🎯 Desafíos Activos",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(getActiveChallenges()) { challenge ->
            ChallengeCard(challenge, studentData)
        }
    }
}

@Composable
fun ChallengeCard(challenge: Challenge, studentData: StudentData) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = challenge.color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    challenge.emoji,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        challenge.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        challenge.timeLeft,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                challenge.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = challenge.progress,
                    modifier = Modifier.weight(1f),
                    color = challenge.color
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "${(challenge.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = challenge.color
                )
            }
        }
    }
}



@Composable
fun StudyCalendarSection(studentData: StudentData) {
    val context = LocalContext.current

    Text(
        "📅 Tu Plan de Estudio",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Sesión de Hoy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Matemáticas: Ecuaciones Cuadráticas",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "⏰ 45 min • 📍 3:00 PM",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        // Iniciar sesión planificada
                        val intent = Intent(context, StudentICFESActivity::class.java)
                        intent.putExtra("module_id", "matematicas")
                        intent.putExtra("session_type", "practice")
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Iniciar")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Próximos días:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val upcomingSessions = listOf(
                "Mañana: Lectura Crítica - Textos argumentativos",
                "Domingo: Ciencias - Repaso de Física",
                "Lunes: Simulacro semanal completo"
            )

            upcomingSessions.forEach { session ->
                Text(
                    "• $session",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun NewsSection() {
    Text(
        "📰 Noticias ICFES",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val news = listOf(
                "🔥 Nuevas fechas de inscripción ICFES 2025" to "Hace 2 días",
                "📊 Estadísticas nacionales primer periodo" to "Hace 1 semana",
                "🎯 Tips de último momento para el examen" to "Hace 2 semanas"
            )

            news.forEach { (title, time) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            time,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

// ✅ FUNCIONES AUXILIARES
fun getActiveChallenges(): List<Challenge> {
    return listOf(
        Challenge(
            id = "streak_7",
            title = "Racha de Oro",
            description = "Estudia 7 días consecutivos",
            emoji = "🔥",
            progress = 0.6f,
            timeLeft = "2 días restantes",
            color = Color(0xFFFF5722)
        ),
        Challenge(
            id = "math_master",
            title = "Maestro Matemático",
            description = "85% en 5 evaluaciones de matemáticas",
            emoji = "🧮",
            progress = 0.4f,
            timeLeft = "Esta semana",
            color = Color(0xFF1E88E5)
        ),
        Challenge(
            id = "speed_reader",
            title = "Lector Veloz",
            description = "Completa 10 textos en menos de 3 min",
            emoji = "⚡",
            progress = 0.8f,
            timeLeft = "1 día restante",
            color = Color(0xFF9C27B0)
        )
    )
}

fun getTopStudents(): List<TopStudent> {
    return listOf(
        TopStudent("Ana García", 1250),
        TopStudent("Carlos López", 1180),
        TopStudent("María Rodríguez", 1150)
    )
}

fun calculateRanking(studentData: StudentData): Int {
    return when {
        studentData.puntajeTotal >= 1200 -> 150
        studentData.puntajeTotal >= 800 -> 500
        studentData.puntajeTotal >= 400 -> 1200
        else -> 2500
    }
}

fun getEstimatedUsers(): String = "15,000"
fun getRegion(): String = "Santander"

// ✅ NUEVAS DATA CLASSES PARA CONTENIDO DOCENTE
data class TeacherContentInfo(
    val teacherName: String,
    val institution: String,
    val lastUpdated: String,
    val availableModules: List<String>,
    val totalQuestions: Int,
    val teacherId: String
)


@Composable
fun TeacherContentCard(
    teacherContent: TeacherContentInfo,
    studentData: StudentData
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            val intent = Intent(context, StudentTeacherContentActivity::class.java)
            intent.putExtra("teacher_id", teacherContent.teacherId)
            intent.putExtra("teacher_name", teacherContent.teacherName)
            context.startActivity(intent)
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // ✅ Elevación normal
    ) {
        // ✅ GRADIENTE DIRECTO SIN CARD ANIDADA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF7B1FA2),
                            Color(0xFF3949AB)
                        )
                    )
                )
                .padding(20.dp) // ✅ Padding normal
        ) {
            Column {
                // ✅ HEADER SIMPLIFICADO
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ✅ ICONO SIMPLE PERO LLAMATIVO
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "🎓 CONTENIDO PREMIUM",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Prof. ${teacherContent.teacherName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            "📍 ${teacherContent.institution}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    // ✅ BADGE PREMIUM SIMPLE
                    Surface(
                        color = Color(0xFFFFD700),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF7B1FA2)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "PREMIUM",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7B1FA2)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ COMPARACIÓN DIRECTA SIN CARD ANIDADA
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        "🚀 Upgrade vs ICFES Laboratorio",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // ✅ COMPARACIONES LIMPIAS
                    SimpleComparisonItem("📊 Preguntas", "15 básicas", "35 premium")
                    SimpleComparisonItem("📝 Modalidades", "Solo práctica", "Práctica + Evaluación")
                    SimpleComparisonItem("🎯 Contenido", "Genérico", "Personalizado")
                    SimpleComparisonItem("🤖 Feedback", "Estándar", "IA Especializada")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ ESTADÍSTICAS SIMPLES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SimpleStat(
                        value = "${teacherContent.totalQuestions}",
                        label = "Preguntas"
                    )
                    SimpleStat(
                        value = "${teacherContent.availableModules.size}",
                        label = "Módulos"
                    )
                    SimpleStat(
                        value = "2025",
                        label = "ICFES"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ BOTÓN DE ACCESO SIMPLE
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFF7B1FA2)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Acceder a Contenido Premium",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7B1FA2)
                        )
                    }
                }
            }
        }
    }
}

// ✅ COMPONENTES AUXILIARES SIMPLES
@Composable
fun SimpleComparisonItem(
    feature: String,
    basic: String,
    premium: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            feature,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.weight(1f)
        )

        Text(
            "→",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )

        Text(
            premium,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun SimpleStat(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

// ✅ NUEVO: Componente para comparación


// ✅ NUEVO: Componente para estadísticas premium
@Composable
fun PremiumStatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ===================================
// 2. 🔧 MEJORAR QuestionCreatorActivity.kt
// ===================================



fun calculateQuestionQuality(
    questionText: String,
    options: List<String>,
    explanation: String,
    questionType: String
): Int {
    var score = 0

    // Longitud apropiada de pregunta
    if (questionText.length in 50..300) score += 20

    // Opciones balanceadas
    val avgOptionLength = options.map { it.length }.average()
    if (avgOptionLength > 10) score += 20

    // Variabilidad en opciones
    val lengthVariance = options.map { it.length }.maxOrNull()?.minus(options.map { it.length }.minOrNull() ?: 0) ?: 0
    if (lengthVariance < 50) score += 15

    // Explicación completa (solo para práctica)
    if (questionType == "practica") {
        if (explanation.length > 50) score += 25
    } else {
        score += 25 // No requiere explicación en evaluación
    }

    // Complejidad apropiada
    if (questionText.contains("analiza") ||
        questionText.contains("interpreta") ||
        questionText.contains("deduce")) score += 20

    return score
}

// ===================================
// 3. 🎨 MEJORAR StudentTeacherContentActivity.kt
// ===================================





// ✅ FUNCIONES AUXILIARES
suspend fun loadTeacherContentForStudent(
    studentInstitution: String,
    onResult: (TeacherContentInfo?) -> Unit
) {
    try {
        println("🔍 STEP 1 - Buscando profesores para institución: '$studentInstitution'")

        if (studentInstitution.isEmpty()) {
            println("❌ STEP 1 - Institución vacía")
            onResult(null)
            return
        }

        val database = FirebaseDatabase.getInstance()

        // Buscar profesores de la misma institución
        val teachersSnapshot = database.reference

            .child("Profesores")
            .get()
            .await()
            /*.child("Profesores")
            .orderByChild("institucion")
            .equalTo(studentInstitution)
            .get()
            .await()*/

        println("🔍 STEP 2 - Profesores encontrados: ${teachersSnapshot.childrenCount}")

        if (!teachersSnapshot.exists()) {
            println("❌ STEP 2 - No se encontraron profesores")
            onResult(null)
            return
        }

        // Buscar el primer profesor que tenga contenido
        for (teacherSnapshot in teachersSnapshot.children) {
            val teacherId = teacherSnapshot.key ?: continue
            val teacherName = teacherSnapshot.child("nombre").getValue(String::class.java) ?: "Profesor"

            println("🔍 STEP 3 - Revisando profesor: $teacherName (ID: $teacherId)")
            println("🔍 STEP 3 - Institución del profesor: '${teacherSnapshot.child("institucion").getValue(String::class.java)}'")

            // Verificar si tiene contenido
            val contentSnapshot = database.reference
                .child("ContenidoDocente")
                .child("profesores")
                .child(teacherId)
                .child("modulos")
                .get()
                .await()

            println("🔍 STEP 4 - ¿Existe contenido para $teacherId? ${contentSnapshot.exists()}")

            if (contentSnapshot.exists()) {
                println("🔍 STEP 5 - Módulos encontrados: ${contentSnapshot.childrenCount}")

                val availableModules = mutableListOf<String>()
                var totalQuestions = 0

                contentSnapshot.children.forEach { moduleSnapshot ->
                    val moduleId = moduleSnapshot.key ?: return@forEach
                    val practiceCount = moduleSnapshot.child("practica").childrenCount.toInt()
                    val evaluationCount = moduleSnapshot.child("evaluacion").childrenCount.toInt()

                    println("🔍 STEP 6 - Módulo $moduleId: $practiceCount práctica + $evaluationCount evaluación")

                    if (practiceCount > 0 || evaluationCount > 0) {
                        availableModules.add(moduleId)
                        totalQuestions += practiceCount + evaluationCount
                    }
                }

                if (availableModules.isNotEmpty()) {
                    println("🔍 STEP 7 - ¡Contenido encontrado! Módulos: $availableModules, Total: $totalQuestions")

                    val teacherContent = TeacherContentInfo(
                        teacherName = teacherName,
                        institution = studentInstitution,
                        lastUpdated = "Actualizado recientemente",
                        availableModules = availableModules,
                        totalQuestions = totalQuestions,
                        teacherId = teacherId
                    )

                    onResult(teacherContent)
                    return
                } else {
                    println("❌ STEP 7 - No hay módulos con contenido")
                }
            } else {
                println("❌ STEP 4 - No existe nodo de contenido")
            }
        }

        // No se encontró contenido
        println("❌ FINAL - No se encontró contenido después de revisar todos los profesores")
        onResult(null)

    } catch (e: Exception) {
        println("❌ ERROR - Excepción: ${e.message}")
        println("❌ ERROR - Stack trace: ${e.printStackTrace()}")
        onResult(null)
    }
}

fun getModuleInfo(moduleId: String): Pair<String, String> {
    return when (moduleId) {
        "lectura_critica" -> "📖" to "Lectura"
        "matematicas" -> "🔢" to "Matemáticas"
        "ciencias_naturales" -> "🧪" to "Ciencias"
        "sociales_ciudadanas" -> "🏛️" to "Sociales"
        "ingles" -> "🇺🇸" to "Inglés"
        else -> "📚" to "Módulo"
    }
}