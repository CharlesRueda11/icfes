package com.charlesdev.icfes.student.screens



import android.content.Context

import android.content.SharedPreferences
import androidx.compose.foundation.background

import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.lazy.LazyRow

import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.*

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp

import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavHostController
import com.charlesdev.icfes.student.StudentData

import com.charlesdev.icfes.student.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import com.charlesdev.icfes.student.viewmodel.ICFESQuizViewModel

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import java.text.SimpleDateFormat

import java.util.*



// ✅ PANTALLA PRINCIPAL (DASHBOARD ICFES) - REORGANIZADA

@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun ICFESHomeScreen(
    navController: NavHostController,
    onNavigateToQuiz: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("ICFESPrefs", Context.MODE_PRIVATE) }
    val viewModel: ICFESQuizViewModel = viewModel()
    val scope = rememberCoroutineScope()

    // ✅ NUEVO: Estado para datos del estudiante
    var studentData by remember { mutableStateOf<StudentData?>(null) }
    var isLoadingUserData by remember { mutableStateOf(true) }

    // Estado para refrescar datos
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.initializePreferences(context)

        // ✅ CARGAR DATOS DEL USUARIO DESDE FIREBASE
        loadUserDataFromFirebase { userData ->
            studentData = userData
            isLoadingUserData = false

            // ✅ GUARDAR EN SHAREDPREFERENCES PARA FUTURO USO
            userData?.let { data ->
                prefs.edit().apply {
                    putString("student_name", data.nombre)
                    putString("student_email", data.email)
                    putString("student_institution", data.institucion)
                    putString("student_grade", data.grado)
                    putInt("student_level", data.nivel)
                    putInt("student_experience", data.experiencia)
                    putInt("student_streak", data.racha)
                    apply()
                }
            }
        }
    }



    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Column {

                        Text("ICFES Saber 11", fontWeight = FontWeight.Bold)



                    }

                },

                actions = {

                    IconButton(onClick = { /* Navegar a perfil */ }) {

                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")

                    }

                    IconButton(onClick = { /* Navegar a configuración */ }) {

                        Icon(Icons.Default.Settings, contentDescription = "Configuración")

                    }

                }

            )

        }

    ) { padding ->

        LazyColumn(

            modifier = Modifier

                .padding(padding)

                .fillMaxSize(),

            contentPadding = PaddingValues(16.dp),

            verticalArrangement = Arrangement.spacedBy(16.dp)

        ) {

            // 1. ✅ Tarjeta de bienvenida CON NOMBRE REAL
            item {
                if (isLoadingUserData) {
                    // Mostrar loading mientras carga
                    WelcomeCardLoading()
                } else {
                    WelcomeCardWithUserData(
                        studentData = studentData,
                        prefs = prefs,
                        refreshKey = refreshKey
                    )
                }
            }



            // 2. ✅ MÓDULOS INDIVIDUALES (PRIORIDAD MÁXIMA)

            item {

                Text(

                    "🎯 Comienza tu Preparación",

                    style = MaterialTheme.typography.titleLarge,

                    fontWeight = FontWeight.Bold,

                    modifier = Modifier.padding(vertical = 8.dp)

                )

                Text(

                    "Practica cada módulo para recibir feedback personalizado y construir tu confianza",

                    style = MaterialTheme.typography.bodyMedium,

                    color = MaterialTheme.colorScheme.onSurfaceVariant,

                    modifier = Modifier.padding(bottom = 8.dp)

                )

            }



            items(populatedICFESModules) { module ->

                ModuleCardEnhanced(

                    module = module,

                    prefs = prefs,

                    refreshKey = refreshKey,

                    onModuleClick = { /* Navegar a detalle módulo */ },

                    onPracticeClick = { onNavigateToQuiz(module.id, "practice") },

                    onQuizClick = { onNavigateToQuiz(module.id, "timed") }

                )

            }



            // 3. ✅ Progreso general (después de mostrar módulos)

            item {

                OverallProgressCard(

                    prefs = prefs,

                    refreshKey = refreshKey

                )

            }



            // 4. ✅ SIMULACRO COMPLETO (cuando ya tenga práctica)

            item {

                FullSimulationCard(

                    prefs = prefs,

                    refreshKey = refreshKey,

                    onSimulationClick = {
                        navController.navigate("simulation") // ✅ ESTO DEBE NAVEGAR A LA RUTA "simulation"
                    }

                )

            }



            // 5. ✅ Recomendaciones personalizadas

            item {

                RecommendationsCard(

                    prefs = prefs,

                    refreshKey = refreshKey,

                    onRecommendationClick = { moduleId ->

                        onNavigateToQuiz(moduleId, "practice")

                    }

                )

            }



            // 6. ✅ Actividad reciente

            item {

                RecentActivityCard(

                    prefs = prefs,

                    refreshKey = refreshKey

                )

            }



            // 7. ✅ Acciones rápidas (al final)

            item {

                QuickActionsSection(

                    onSimulationClick = { onNavigateToQuiz("simulation", "all") },

                    onProgressClick = { /* Navegar a progreso */ },

                    onAchievementsClick = { /* Navegar a logros */ }

                )

            }

        }

    }

}

// ✅ NUEVA TARJETA DE BIENVENIDA CON DATOS REALES
@Composable
fun WelcomeCardWithUserData(
    studentData: StudentData?,
    prefs: SharedPreferences,
    refreshKey: Int
) {
    // Usar datos de Firebase si están disponibles, sino fallback a SharedPreferences
    val studentName = studentData?.nombre?.split(" ")?.firstOrNull()
        ?: prefs.getString("student_name", "Estudiante")?.split(" ")?.firstOrNull()
        ?: "Estudiante"

    val studyStreak = studentData?.racha
        ?: prefs.getInt("student_streak", 0)

    val currentLevel = studentData?.nivel
        ?: prefs.getInt("student_level", 1)

    val completedModules = prefs.getInt("icfes_completed_modules", 0)

    val institution = studentData?.institucion ?: ""
    val grade = studentData?.grado ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "¡Hola, $studentName!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // ✅ MOSTRAR INSTITUCIÓN Y GRADO SI ESTÁN DISPONIBLES
                    val subtitle = if (institution.isNotEmpty() && grade.isNotEmpty()) {
                        "$grade - $institution"
                    } else {
                        "Nivel $currentLevel • Racha: $studyStreak días"
                    }

                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    // ✅ SEGUNDA LÍNEA CON NIVEL Y RACHA
                    if (institution.isNotEmpty() && grade.isNotEmpty()) {
                        Text(
                            "Nivel $currentLevel • Racha: $studyStreak días",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                // ✅ AVATAR PERSONALIZADO
                Box {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    // ✅ INDICADOR DE NIVEL
                    Card(
                        modifier = Modifier
                            .offset(x = 4.dp, y = (-4).dp)
                            .align(Alignment.TopEnd),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            "$currentLevel",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ MENSAJE MOTIVACIONAL MEJORADO
            val motivationalMessage = when {
                completedModules == 0 -> "🚀 ¡Comienza tu preparación! Practica cada módulo para recibir feedback personalizado."
                completedModules < 3 -> "💪 ¡Vas bien! Continúa practicando para dominar todos los módulos."
                completedModules < 5 -> "🔥 ¡Excelente progreso! Ya casi dominas todos los módulos."
                else -> "🌟 ¡Felicidades! Ahora puedes hacer simulacros completos con confianza."
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                )
            ) {
                Text(
                    motivationalMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ✅ FUNCIÓN PARA CARGAR DATOS DESDE FIREBASE
private fun loadUserDataFromFirebase(onResult: (StudentData?) -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser != null) {
        val database = FirebaseDatabase.getInstance()
        database.reference.child("Usuarios").child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val studentData = StudentData(
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
                        onResult(studentData)
                    } catch (e: Exception) {
                        onResult(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(null)
                }
            })
    } else {
        onResult(null)
    }
}


// ✅ LOADING STATE PARA CUANDO ESTÁ CARGANDO
@Composable
fun WelcomeCardLoading() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(24.dp)
                            .background(
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f),
                                RoundedCornerShape(4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(16.dp)
                            .background(
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }

                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                )
            ) {
                Text(
                    "Cargando tu información...",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}



// ✅ NUEVA TARJETA DE SIMULACRO COMPLETO - DISEÑO COMPLETO Y ATRACTIVO

@Composable
fun FullSimulationCard(
    prefs: SharedPreferences,
    refreshKey: Int,
    onSimulationClick: () -> Unit
) {
    val completedModules = remember(refreshKey) {
        prefs.getInt("icfes_completed_modules", 0)
    }
    val overallScore = remember(refreshKey) {
        prefs.getInt("icfes_overall_score", 0)
    }
    val lastSimulationScore = remember(refreshKey) {
        prefs.getInt("simulation_global_score", 0)
    }
    val totalSimulations = remember(refreshKey) {
        prefs.getInt("total_simulations", 0)
    }

    val isReadyForSimulation = completedModules >= 3
    val hasSimulationExperience = totalSimulations > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isReadyForSimulation)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // ✅ HEADER CON ICONO Y TÍTULO
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            if (isReadyForSimulation)
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isReadyForSimulation) Icons.Default.Timer else Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = if (isReadyForSimulation)
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "🎯 Simulacro Completo ICFES",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isReadyForSimulation)
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        if (isReadyForSimulation)
                            "Experiencia 100% real del examen Saber 11"
                        else
                            "Practica más módulos para desbloquear",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isReadyForSimulation)
                            MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // ✅ BADGE DE ESTADO
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isReadyForSimulation)
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        if (isReadyForSimulation) "DESBLOQUEADO" else "BLOQUEADO",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isReadyForSimulation)
                            MaterialTheme.colorScheme.onTertiary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isReadyForSimulation) {
                // ✅ CONTENIDO CUANDO ESTÁ DESBLOQUEADO
                FullSimulationUnlockedContent(
                    hasSimulationExperience = hasSimulationExperience,
                    lastSimulationScore = lastSimulationScore,
                    totalSimulations = totalSimulations,
                    onSimulationClick = onSimulationClick
                )
            } else {
                // ✅ CONTENIDO CUANDO ESTÁ BLOQUEADO
                FullSimulationLockedContent(
                    completedModules = completedModules,
                    requiredModules = 3
                )
            }
        }
    }
}

// ✅ CONTENIDO CUANDO EL SIMULACRO ESTÁ DESBLOQUEADO
@Composable
fun FullSimulationUnlockedContent(
    hasSimulationExperience: Boolean,
    lastSimulationScore: Int,
    totalSimulations: Int,
    onSimulationClick: () -> Unit
) {
    // ✅ DESCRIPCIÓN DEL SIMULACRO
    Text(
        "¿Qué incluye el simulacro completo?",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    // ✅ ESTRUCTURA DEL SIMULACRO
    val simulationStructure = listOf(
        "📚 Lectura Crítica" to "35 preguntas • 65 minutos",
        "🔢 Matemáticas" to "35 preguntas • 65 minutos",
        "🧪 Ciencias Naturales" to "35 preguntas • 65 minutos",
        "🏛️ Sociales y Ciudadanas" to "35 preguntas • 65 minutos",
        "🇺🇸 Inglés" to "35 preguntas • 60 minutos"
    )

    Column(
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        simulationStructure.forEach { (subject, details) ->
            Row(
                modifier = Modifier.padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "•",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    subject,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }

    // ✅ CARACTERÍSTICAS ESPECIALES
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                "✨ Experiencia 100% Real",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val features = listOf(
                "⏱️ 5 sesiones con breaks de 15 minutos",
                "🎯 175 preguntas totales (como el ICFES real)",
                "📊 Análisis detallado por módulo",
                "🏆 Puntaje oficial escala 0-500",
                "📈 Comparación con promedios nacionales"
            )

            features.forEach { feature ->
                Text(
                    feature,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }

    // ✅ ESTADÍSTICAS PREVIAS (si las tiene)
    if (hasSimulationExperience) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    "📊 Tu Historial",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$lastSimulationScore",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            "Último puntaje",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$totalSimulations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            "Simulacros",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            when {
                                lastSimulationScore >= 350 -> "Alto"
                                lastSimulationScore >= 250 -> "Medio"
                                else -> "Bajo"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                lastSimulationScore >= 350 -> Color(0xFF4CAF50)
                                lastSimulationScore >= 250 -> Color(0xFFFF9800)
                                else -> Color(0xFFF44336)
                            }
                        )
                        Text(
                            "Nivel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }

    // ✅ RECOMENDACIONES ANTES DEL SIMULACRO
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                "💡 Recomendaciones",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2196F3),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val recommendations = listOf(
                "📱 Busca un lugar tranquilo sin distracciones",
                "☕ Ten agua y snacks listos para los breaks",
                "🔋 Asegúrate de tener batería suficiente",
                "⏰ Planifica 4.5 horas completas",
                "🧘 Descansa bien la noche anterior"
            )

            recommendations.forEach { recommendation ->
                Text(
                    recommendation,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }

    // ✅ BOTÓN PRINCIPAL DEL SIMULACRO
    Button(
        onClick = onSimulationClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (hasSimulationExperience) "🚀 Nuevo Simulacro Completo" else "🎯 Mi Primer Simulacro",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "4.5 horas • 175 preguntas • 5 sesiones",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f)
                )
            }
        }
    }

    // ✅ TEXTO DE MOTIVACIÓN
    Text(
        if (hasSimulationExperience)
            "¡Supera tu puntaje anterior y alcanza tus metas!"
        else
            "¡Es hora de poner a prueba todo lo que has aprendido!",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )
}

// ✅ CONTENIDO CUANDO EL SIMULACRO ESTÁ BLOQUEADO
@Composable
fun FullSimulationLockedContent(
    completedModules: Int,
    requiredModules: Int
) {
    // ✅ EXPLICACIÓN DEL BLOQUEO
    Text(
        "¿Por qué está bloqueado?",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Text(
        "El simulacro completo replica la experiencia real del ICFES. Para aprovecharlo al máximo, necesitas dominar primero los módulos individuales.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    // ✅ PROGRESO HACIA EL DESBLOQUEO
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "📈 Tu Progreso",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Módulos completados:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "$completedModules/$requiredModules",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = completedModules.toFloat() / requiredModules,
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Necesitas ${requiredModules - completedModules} módulos más",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // ✅ LO QUE INCLUIRÁ EL SIMULACRO
    Text(
        "¿Qué incluirá cuando se desbloquee?",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    val futureFeatures = listOf(
        "🎯 Experiencia idéntica al ICFES real",
        "⏱️ 5 sesiones cronometradas con breaks",
        "📊 Análisis completo de resultados",
        "🏆 Puntaje oficial escala 0-500",
        "📈 Comparación con promedios nacionales"
    )

    futureFeatures.forEach { feature ->
        Row(
            modifier = Modifier.padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "•",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                feature,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // ✅ BOTÓN BLOQUEADO
    OutlinedButton(
        onClick = { },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = false,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Simulacro Bloqueado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }

    Text(
        "¡Practica más módulos para desbloquearlo!",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )
}

// ✅ TARJETA DE MÓDULO CON BLOQUEO PEDAGÓGICO

@Composable

fun ModuleCardEnhanced(

    module: ICFESModule,

    prefs: SharedPreferences,

    refreshKey: Int,

    onModuleClick: () -> Unit,

    onPracticeClick: () -> Unit,

    onQuizClick: () -> Unit

) {

    val moduleScore = remember(refreshKey) {

        prefs.getInt("icfes_score_${module.id}", 0)

    }

    val modulePercentage = remember(refreshKey) {

        prefs.getFloat("icfes_percentage_${module.id}", 0f)

    }

    val lastActivity = remember(refreshKey) {

        val timestamp = prefs.getLong("icfes_timestamp_${module.id}", 0)

        if (timestamp > 0) {

            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))

        } else "Sin actividad"

    }



    // ✅ NUEVA LÓGICA DE BLOQUEO PEDAGÓGICO

    val isCompleted = moduleScore > 0

    val isGoodScore = moduleScore >= 300

    val hasMinimumPractice = moduleScore >= 50 // Puntaje mínimo para desbloquear evaluación

    val isEvaluationUnlocked = hasMinimumPractice // Solo se desbloquea después de practicar



    Card(

        modifier = Modifier.fillMaxWidth(),

        onClick = onModuleClick,

        colors = CardDefaults.cardColors(

            containerColor = when {

                isGoodScore -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)

                isCompleted -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)

                else -> MaterialTheme.colorScheme.surface

            }

        )

    ) {

        Column(

            modifier = Modifier.padding(16.dp)

        ) {

            // Header del módulo con indicador de estado

            Row(

                modifier = Modifier

                    .fillMaxWidth()

                    .padding(bottom = 4.dp),

                verticalAlignment = Alignment.CenterVertically

            ) {

                // Icono y nombre del módulo

                Box(

                    modifier = Modifier

                        .size(40.dp)

                        .align(Alignment.CenterVertically)

                ) {

                    Icon(

                        Icons.Default.Book,

                        contentDescription = null,

                        modifier = Modifier.size(32.dp).align(Alignment.Center),

                        tint = module.color

                    )

                    // Status Icon (completado)

                    if (isCompleted) {

                        Icon(

                            if (isGoodScore) Icons.Default.CheckCircle else Icons.Default.Circle,

                            contentDescription = null,

                            modifier = Modifier

                                .size(16.dp)

                                .align(Alignment.TopEnd)

                                .offset(x = 4.dp, y = (-4).dp),

                            tint = if (isGoodScore) Color(0xFF4CAF50) else Color(0xFFFF9800)

                        )

                    }

                }

                Spacer(modifier = Modifier.width(10.dp))



                // Nombre, info y badge (ocupa el espacio central)

                Column(

                    modifier = Modifier.weight(1f)

                ) {

                    Row(

                        verticalAlignment = Alignment.CenterVertically,

                        ) {

                        Text(

                            module.name,

                            style = MaterialTheme.typography.titleMedium,

                            fontWeight = FontWeight.Bold,

                            maxLines = 1

                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        when {

                            !isCompleted -> {

                                Card(

                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)

                                ) {

                                    Text(

                                        "NUEVO",

                                        style = MaterialTheme.typography.labelSmall,

                                        color = MaterialTheme.colorScheme.onPrimary,

                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)

                                    )

                                }

                            }

                            isEvaluationUnlocked -> {

                                Card(

                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))

                                ) {

                                    Text(

                                        "LISTO",

                                        style = MaterialTheme.typography.labelSmall,

                                        color = Color.White,

                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)

                                    )

                                }

                            }

                        }

                    }

                    Text(

                        "${module.totalQuestions} preguntas • ${module.timeLimit} min • Feedback con IA",

                        style = MaterialTheme.typography.bodySmall,

                        color = MaterialTheme.colorScheme.onSurfaceVariant

                    )

                }



                // Puntaje (siempre alineado al centro vertical)

                Column(

                    horizontalAlignment = Alignment.End,

                    verticalArrangement = Arrangement.Center,

                    modifier = Modifier.padding(start = 6.dp)

                ) {

                    Text(

                        if (moduleScore > 0) "$moduleScore/500" else "¡Comienza!",

                        style = MaterialTheme.typography.titleSmall,

                        fontWeight = FontWeight.Bold,

                        color = when {

                            moduleScore >= 350 -> Color(0xFF4CAF50)

                            moduleScore >= 250 -> Color(0xFFFF9800)

                            moduleScore > 0 -> Color(0xFFF44336)

                            else -> MaterialTheme.colorScheme.primary

                        }

                    )

                    if (moduleScore > 0) {

                        Text(

                            "${"%.1f".format(modulePercentage)}%",

                            style = MaterialTheme.typography.bodySmall,

                            color = MaterialTheme.colorScheme.onSurfaceVariant

                        )

                    }

                }

            }



            Spacer(modifier = Modifier.height(12.dp))



            // Descripción con énfasis en feedback

            Text(

                "${module.description}\n💡 Recibe explicaciones detalladas y consejos personalizados para cada pregunta.",

                style = MaterialTheme.typography.bodyMedium,

                color = MaterialTheme.colorScheme.onSurfaceVariant

            )



            Spacer(modifier = Modifier.height(12.dp))



            // Barra de progreso

            if (moduleScore > 0) {

                LinearProgressIndicator(

                    progress = modulePercentage / 100f,

                    modifier = Modifier.fillMaxWidth(),

                    trackColor = MaterialTheme.colorScheme.surfaceVariant,

                    color = module.color

                )

                Spacer(modifier = Modifier.height(12.dp))

            }



            // ✅ NUEVA SECCIÓN: PROGRESIÓN PEDAGÓGICA

            if (!isEvaluationUnlocked) {

                Card(

                    colors = CardDefaults.cardColors(

                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

                    ),

                    modifier = Modifier

                        .fillMaxWidth()

                        .padding(bottom = 12.dp)

                ) {

                    Row(

                        modifier = Modifier

                            .fillMaxWidth()

                            .padding(12.dp),

                        verticalAlignment = Alignment.CenterVertically

                    ) {

                        Icon(

                            Icons.Default.TipsAndUpdates,

                            contentDescription = null,

                            tint = MaterialTheme.colorScheme.primary,

                            modifier = Modifier.size(20.dp)

                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {

                            Text(

                                "🎯 Progresión Recomendada",

                                style = MaterialTheme.typography.bodyMedium,

                                fontWeight = FontWeight.Medium

                            )

                            Text(

                                "1. Practica sin tiempo → 2. Evaluación cronometrada → 3. Simulacro completo",

                                style = MaterialTheme.typography.bodySmall,

                                color = MaterialTheme.colorScheme.onSurfaceVariant

                            )

                        }

                    }

                }

            }



            // ✅ BOTONES CON BLOQUEO PEDAGÓGICO

            Column(

                verticalArrangement = Arrangement.spacedBy(8.dp)

            ) {

                // Botón principal - Práctica (SIEMPRE disponible)

                Button(

                    onClick = onPracticeClick,

                    modifier = Modifier.fillMaxWidth(),

                    colors = ButtonDefaults.buttonColors(

                        containerColor = module.color

                    )

                ) {

                    Row(

                        verticalAlignment = Alignment.CenterVertically,

                        horizontalArrangement = Arrangement.Center

                    ) {

                        Icon(

                            Icons.Default.Psychology,

                            contentDescription = null,

                            modifier = Modifier.size(18.dp)

                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(

                            horizontalAlignment = Alignment.CenterHorizontally

                        ) {

                            Text(

                                if (isCompleted) "Seguir Practicando" else "Comenzar Práctica",

                                fontWeight = FontWeight.Medium

                            )

                            Text(

                                "🧠 Sin tiempo • Feedback completo",

                                style = MaterialTheme.typography.bodySmall,

                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)

                            )

                        }

                    }

                }



                // Botón secundario - Evaluación (BLOQUEADO hasta practicar)

                if (isEvaluationUnlocked) {

                    // ✅ DESBLOQUEADO: Botón normal de evaluación

                    OutlinedButton(

                        onClick = onQuizClick,

                        modifier = Modifier.fillMaxWidth(),

                        colors = ButtonDefaults.outlinedButtonColors(

                            contentColor = module.color

                        )

                    ) {

                        Row(

                            verticalAlignment = Alignment.CenterVertically,

                            horizontalArrangement = Arrangement.Center

                        ) {

                            Icon(

                                Icons.Default.Timer,

                                contentDescription = null,

                                modifier = Modifier.size(18.dp)

                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(

                                horizontalAlignment = Alignment.CenterHorizontally

                            ) {

                                Text(

                                    "Evaluación Cronometrada",

                                    fontWeight = FontWeight.Medium

                                )

                                Text(

                                    "⏰ ${module.timeLimit} min • Simula ICFES real",

                                    style = MaterialTheme.typography.bodySmall,

                                    color = MaterialTheme.colorScheme.onSurfaceVariant

                                )

                            }

                        }

                    }

                } else {

                    // ❌ BLOQUEADO: Botón deshabilitado con explicación

                    OutlinedButton(

                        onClick = { },

                        modifier = Modifier.fillMaxWidth(),

                        enabled = false,

                        colors = ButtonDefaults.outlinedButtonColors(

                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant

                        )

                    ) {

                        Row(

                            verticalAlignment = Alignment.CenterVertically,

                            horizontalArrangement = Arrangement.Center

                        ) {

                            Icon(

                                Icons.Default.Lock,

                                contentDescription = null,

                                modifier = Modifier.size(18.dp)

                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(

                                horizontalAlignment = Alignment.CenterHorizontally

                            ) {

                                Text(

                                    "Evaluación Bloqueada",

                                    fontWeight = FontWeight.Medium

                                )

                                Text(

                                    "Practica primero para desbloquear",

                                    style = MaterialTheme.typography.bodySmall,

                                    color = MaterialTheme.colorScheme.onSurfaceVariant

                                )

                            }

                        }

                    }



                    // Indicador de progreso hacia desbloqueo

                    Spacer(modifier = Modifier.height(4.dp))



                    val progressToUnlock = if (moduleScore > 0) (moduleScore / 50f).coerceAtMost(1f) else 0f



                    LinearProgressIndicator(

                        progress = progressToUnlock,

                        modifier = Modifier.fillMaxWidth(),

                        trackColor = MaterialTheme.colorScheme.surfaceVariant,

                        color = module.color.copy(alpha = 0.7f)

                    )



                    Text(

                        if (moduleScore > 0)

                            "Progreso: $moduleScore/50 puntos para desbloquear"

                        else

                            "Practica para comenzar a desbloquear la evaluación",

                        style = MaterialTheme.typography.bodySmall,

                        color = MaterialTheme.colorScheme.onSurfaceVariant,

                        textAlign = TextAlign.Center,

                        modifier = Modifier

                            .fillMaxWidth()

                            .padding(top = 4.dp)

                    )

                }

            }



            // Información adicional con contexto pedagógico

            if (lastActivity != "Sin actividad" || !isEvaluationUnlocked) {

                Spacer(modifier = Modifier.height(12.dp))



                Card(

                    colors = CardDefaults.cardColors(

                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

                    )

                ) {

                    Column(

                        modifier = Modifier.padding(12.dp)

                    ) {

                        if (lastActivity != "Sin actividad") {

                            Text(

                                "📅 Última práctica: $lastActivity",

                                style = MaterialTheme.typography.bodySmall,

                                color = MaterialTheme.colorScheme.onSurfaceVariant

                            )

                        }



                        if (isEvaluationUnlocked && !isGoodScore) {

                            if (lastActivity != "Sin actividad") {

                                Spacer(modifier = Modifier.height(4.dp))

                            }

                            Text(

                                "💡 Consejo: Practica más para mejorar tu puntaje antes del simulacro completo",

                                style = MaterialTheme.typography.bodySmall,

                                color = MaterialTheme.colorScheme.primary,

                                fontWeight = FontWeight.Medium

                            )

                        }



                        if (!isEvaluationUnlocked) {

                            if (lastActivity != "Sin actividad") {

                                Spacer(modifier = Modifier.height(4.dp))

                            }

                            Text(

                                "🎯 Estrategia: Domina la práctica sin presión, luego evalúate con tiempo real",

                                style = MaterialTheme.typography.bodySmall,

                                color = MaterialTheme.colorScheme.primary,

                                fontWeight = FontWeight.Medium

                            )

                        }

                    }

                }

            }

        }

    }

}



// ✅ MANTENER LAS DEMÁS FUNCIONES ORIGINALES

@Composable

fun OverallProgressCard(

    prefs: SharedPreferences,

    refreshKey: Int

) {

    val overallScore = remember(refreshKey) {

        prefs.getInt("icfes_overall_score", 0)

    }

    val completedModules = remember(refreshKey) {

        prefs.getInt("icfes_completed_modules", 0)

    }



    Card(

        modifier = Modifier.fillMaxWidth()

    ) {

        Column(

            modifier = Modifier.padding(20.dp)

        ) {

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceBetween,

                verticalAlignment = Alignment.CenterVertically

            ) {

                Text(

                    "📊 Tu Progreso General",

                    style = MaterialTheme.typography.titleLarge,

                    fontWeight = FontWeight.Bold

                )



                TextButton(onClick = { /* Ver detalles */ }) {

                    Text("Ver detalles")

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(Icons.Default.ArrowForward, contentDescription = null)

                }

            }



            Spacer(modifier = Modifier.height(16.dp))



            // Puntaje general

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceEvenly

            ) {

                Column(

                    horizontalAlignment = Alignment.CenterHorizontally

                ) {

                    Text(

                        if (overallScore > 0) "$overallScore" else "--",

                        style = MaterialTheme.typography.displayMedium,

                        fontWeight = FontWeight.Bold,

                        color = MaterialTheme.colorScheme.primary

                    )

                    Text(

                        "Puntaje promedio",

                        style = MaterialTheme.typography.bodySmall,

                        textAlign = TextAlign.Center

                    )

                }



                Column(

                    horizontalAlignment = Alignment.CenterHorizontally

                ) {

                    Text(

                        "$completedModules/5",

                        style = MaterialTheme.typography.displayMedium,

                        fontWeight = FontWeight.Bold,

                        color = Color(0xFF4CAF50)

                    )

                    Text(

                        "Módulos\ncompletados",

                        style = MaterialTheme.typography.bodySmall,

                        textAlign = TextAlign.Center

                    )

                }

            }



            Spacer(modifier = Modifier.height(16.dp))



            // Barra de progreso

            LinearProgressIndicator(

                progress = completedModules / 5f,

                modifier = Modifier.fillMaxWidth(),

                trackColor = MaterialTheme.colorScheme.surfaceVariant

            )



            Spacer(modifier = Modifier.height(8.dp))



            Text(

                when {

                    completedModules == 0 -> "🚀 ¡Comienza tu preparación!"

                    completedModules < 3 -> "💪 ¡Continúa practicando!"

                    completedModules < 5 -> "🔥 ¡Ya casi terminas!"

                    else -> "🌟 ¡Todos los módulos completados!"

                },

                style = MaterialTheme.typography.bodyMedium,

                textAlign = TextAlign.Center,

                modifier = Modifier.fillMaxWidth()

            )

        }

    }

}



@Composable

fun QuickActionsSection(

    onSimulationClick: () -> Unit,

    onProgressClick: () -> Unit,

    onAchievementsClick: () -> Unit

) {

    Row(

        modifier = Modifier

            .fillMaxWidth()

            .padding(horizontal = 8.dp, vertical = 8.dp),

        horizontalArrangement = Arrangement.SpaceEvenly,

        verticalAlignment = Alignment.CenterVertically

    ) {

        QuickActionCard(

            icon = Icons.Default.Timer,

            text = "Simulacro",

            backgroundColor = MaterialTheme.colorScheme.primary,

            contentColor = Color.White,

            onClick = onSimulationClick

        )

        QuickActionCard(

            icon = Icons.Default.Analytics,

            text = "Progreso",

            backgroundColor = Color(0xFF4CAF50),

            contentColor = Color.White,

            onClick = onProgressClick

        )

        QuickActionCard(

            icon = Icons.Default.EmojiEvents,

            text = "Logros",

            backgroundColor = Color(0xFFFFC107),

            contentColor = Color.Black,

            onClick = onAchievementsClick

        )

    }

}



@Composable

fun QuickActionCard(

    icon: androidx.compose.ui.graphics.vector.ImageVector,

    text: String,

    backgroundColor: Color,

    contentColor: Color,

    onClick: () -> Unit

) {

    Card(

        modifier = Modifier

            .width(110.dp)

            .height(120.dp)

            .clickable { onClick() },

        shape = RoundedCornerShape(20.dp),

        elevation = CardDefaults.cardElevation(8.dp),

        colors = CardDefaults.cardColors(

            containerColor = backgroundColor,

            contentColor = contentColor

        )

    ) {

        Column(

            modifier = Modifier

                .fillMaxSize()

                .padding(10.dp),

            horizontalAlignment = Alignment.CenterHorizontally,

            verticalArrangement = Arrangement.Center

        ) {

            Icon(

                icon,

                contentDescription = text,

                modifier = Modifier

                    .size(40.dp)

                    .padding(bottom = 8.dp),

                tint = contentColor

            )

            Text(

                text,

                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),

                color = contentColor,

                textAlign = TextAlign.Center

            )

        }

    }

}





@Composable

fun RecentActivityCard(

    prefs: SharedPreferences,

    refreshKey: Int

) {

    val recentActivities = remember(refreshKey) {

        populatedICFESModules.mapNotNull { module ->

            val timestamp = prefs.getLong("icfes_timestamp_${module.id}", 0)

            val score = prefs.getInt("icfes_score_${module.id}", 0)

            if (timestamp > 0) {

                Triple(module.name, score, timestamp)

            } else null

        }.sortedByDescending { it.third }.take(3)

    }



    if (recentActivities.isNotEmpty()) {

        Card(

            modifier = Modifier.fillMaxWidth()

        ) {

            Column(

                modifier = Modifier.padding(16.dp)

            ) {

                Row(

                    verticalAlignment = Alignment.CenterVertically,

                    modifier = Modifier.padding(bottom = 12.dp)

                ) {

                    Icon(

                        Icons.Default.History,

                        contentDescription = null,

                        tint = MaterialTheme.colorScheme.primary

                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(

                        "📈 Actividad Reciente",

                        style = MaterialTheme.typography.titleMedium,

                        fontWeight = FontWeight.Bold

                    )

                }



                recentActivities.forEach { (moduleName, score, timestamp) ->

                    Row(

                        modifier = Modifier

                            .fillMaxWidth()

                            .padding(vertical = 4.dp),

                        horizontalArrangement = Arrangement.SpaceBetween,

                        verticalAlignment = Alignment.CenterVertically

                    ) {

                        Column {

                            Text(

                                moduleName,

                                style = MaterialTheme.typography.bodyMedium,

                                fontWeight = FontWeight.Medium

                            )

                            Text(

                                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                                    .format(Date(timestamp)),

                                style = MaterialTheme.typography.bodySmall,

                                color = MaterialTheme.colorScheme.onSurfaceVariant

                            )

                        }



                        Text(

                            "$score/500",

                            style = MaterialTheme.typography.bodyMedium,

                            fontWeight = FontWeight.Bold,

                            color = when {

                                score >= 350 -> Color(0xFF4CAF50)

                                score >= 250 -> Color(0xFFFF9800)

                                else -> Color(0xFFF44336)

                            }

                        )

                    }

                }

            }

        }

    }

}



@Composable

fun RecommendationsCard(

    prefs: SharedPreferences,

    refreshKey: Int,

    onRecommendationClick: (String) -> Unit

) {

    val recommendations = remember(refreshKey) {

        val moduleScores = populatedICFESModules.map { module ->

            module.id to prefs.getFloat("icfes_percentage_${module.id}", 0f)

        }



        // Encontrar módulos con menor rendimiento

        val weakModules = moduleScores.filter { it.second > 0 && it.second < 70 }

            .sortedBy { it.second }

            .take(2)



        // Encontrar módulos sin evaluar

        val unevaluatedModules = moduleScores.filter { it.second == 0f }.take(2)



        val recommendationsList = mutableListOf<Pair<String, String>>()



        weakModules.forEach { (moduleId, percentage) ->

            val moduleName = populatedICFESModules.find { it.id == moduleId }?.name ?: ""

            recommendationsList.add(

                moduleId to "📈 Refuerza $moduleName (${"%.1f".format(percentage)}%)"

            )

        }



        unevaluatedModules.forEach { (moduleId, _) ->

            val moduleName = populatedICFESModules.find { it.id == moduleId }?.name ?: ""

            recommendationsList.add(

                moduleId to "🚀 Comienza con $moduleName"

            )

        }



        if (recommendationsList.isEmpty()) {

            listOf("" to "🌟 ¡Excelente progreso en todos los módulos!")

        } else {

            recommendationsList

        }

    }



    Card(

        modifier = Modifier.fillMaxWidth(),

        colors = CardDefaults.cardColors(

            containerColor = MaterialTheme.colorScheme.secondaryContainer

        )

    ) {

        Column(

            modifier = Modifier.padding(16.dp)

        ) {

            Row(

                verticalAlignment = Alignment.CenterVertically,

                modifier = Modifier.padding(bottom = 12.dp)

            ) {

                Icon(

                    Icons.Default.Lightbulb,

                    contentDescription = null,

                    tint = MaterialTheme.colorScheme.onSecondaryContainer

                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(

                    "💡 Recomendaciones Personalizadas",

                    style = MaterialTheme.typography.titleMedium,

                    fontWeight = FontWeight.Bold

                )

            }



            recommendations.forEach { (moduleId, recommendation) ->

                if (moduleId.isNotEmpty()) {

                    OutlinedButton(

                        onClick = { onRecommendationClick(moduleId) },

                        modifier = Modifier

                            .fillMaxWidth()

                            .padding(vertical = 2.dp),

                        colors = ButtonDefaults.outlinedButtonColors(

                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer

                        )

                    ) {

                        Text(

                            recommendation,

                            style = MaterialTheme.typography.bodyMedium,

                            textAlign = TextAlign.Start,

                            modifier = Modifier.weight(1f)

                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(

                            Icons.Default.ArrowForward,

                            contentDescription = null,

                            modifier = Modifier.size(16.dp)

                        )

                    }

                } else {

                    Text(

                        recommendation,

                        style = MaterialTheme.typography.bodyMedium,

                        textAlign = TextAlign.Center,

                        modifier = Modifier.fillMaxWidth()

                    )

                }

            }

        }

    }

}



// ✅ MANTENER PANTALLAS ADICIONALES SIN CAMBIOS

@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun ICFESModuleSelectionScreen(

    navController: NavHostController,

    onNavigateToQuiz: (String, String) -> Unit

) {

    val context = LocalContext.current

    val prefs = remember { context.getSharedPreferences("ICFESPrefs", Context.MODE_PRIVATE) }



    Scaffold(

        topBar = {

            TopAppBar(

                title = { Text("Seleccionar Módulo") },

                navigationIcon = {

                    IconButton(onClick = { navController.navigateUp() }) {

                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")

                    }

                }

            )

        }

    ) { padding ->

        LazyColumn(

            modifier = Modifier

                .padding(padding)

                .fillMaxSize(),

            contentPadding = PaddingValues(16.dp),

            verticalArrangement = Arrangement.spacedBy(16.dp)

        ) {

            item {

                Card(

                    modifier = Modifier.fillMaxWidth(),

                    colors = CardDefaults.cardColors(

                        containerColor = MaterialTheme.colorScheme.primaryContainer

                    )

                ) {

                    Column(

                        modifier = Modifier.padding(20.dp),

                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {

                        Icon(

                            Icons.Default.School,

                            contentDescription = null,

                            modifier = Modifier.size(48.dp),

                            tint = MaterialTheme.colorScheme.primary

                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(

                            "Elige un módulo para practicar",

                            style = MaterialTheme.typography.headlineSmall,

                            fontWeight = FontWeight.Bold,

                            textAlign = TextAlign.Center

                        )

                        Text(

                            "Selecciona el área que deseas reforzar",

                            style = MaterialTheme.typography.bodyMedium,

                            textAlign = TextAlign.Center,

                            color = MaterialTheme.colorScheme.onPrimaryContainer

                        )

                    }

                }

            }



            items(populatedICFESModules) { module ->

                ModuleSelectionCard(

                    module = module,

                    prefs = prefs,

                    onModuleClick = { onNavigateToQuiz(module.id, "practice") }

                )

            }

        }

    }

}



@Composable

fun ModuleSelectionCard(

    module: ICFESModule,

    prefs: SharedPreferences,

    onModuleClick: () -> Unit

) {

    val moduleScore = prefs.getInt("icfes_score_${module.id}", 0)

    val modulePercentage = prefs.getFloat("icfes_percentage_${module.id}", 0f)



    Card(

        modifier = Modifier.fillMaxWidth(),

        onClick = onModuleClick

    ) {

        Row(

            modifier = Modifier.padding(16.dp),

            verticalAlignment = Alignment.CenterVertically

        ) {

            Icon(

                Icons.Default.Book,

                contentDescription = null,

                modifier = Modifier.size(48.dp),

                tint = module.color

            )



            Spacer(modifier = Modifier.width(16.dp))



            Column(

                modifier = Modifier.weight(1f)

            ) {

                Text(

                    module.name,

                    style = MaterialTheme.typography.titleMedium,

                    fontWeight = FontWeight.Bold

                )

                Text(

                    "${module.totalQuestions} preguntas • ${module.timeLimit} min",

                    style = MaterialTheme.typography.bodySmall,

                    color = MaterialTheme.colorScheme.onSurfaceVariant

                )



                if (moduleScore > 0) {

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(

                        progress = modulePercentage / 100f,

                        modifier = Modifier.fillMaxWidth(),

                        trackColor = MaterialTheme.colorScheme.surfaceVariant,

                        color = module.color

                    )

                }

            }



            Spacer(modifier = Modifier.width(16.dp))



            Column(

                horizontalAlignment = Alignment.End

            ) {

                Text(

                    if (moduleScore > 0) "$moduleScore/500" else "Nueva",

                    style = MaterialTheme.typography.titleSmall,

                    fontWeight = FontWeight.Bold,

                    color = when {

                        moduleScore >= 350 -> Color(0xFF4CAF50)

                        moduleScore >= 250 -> Color(0xFFFF9800)

                        moduleScore > 0 -> Color(0xFFF44336)

                        else -> MaterialTheme.colorScheme.primary

                    }

                )



                Icon(

                    Icons.Default.ArrowForward,

                    contentDescription = null,

                    tint = MaterialTheme.colorScheme.onSurfaceVariant

                )

            }

        }

    }

}