package com.charlesdev.icfes.teacher.practice_evaluation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charlesdev.icfes.ui.theme.IcfesTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class TeacherContentManagerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            IcfesTheme {
                TeacherContentManagerScreen()
            }
        }
    }
}

// 📊 Data classes para gestión de contenido
data class ICFESModuleContent(
    val id: String,
    val name: String,
    val emoji: String,
    val color: Color,
    val practiceQuestions: Int = 0,  // Preguntas de práctica creadas
    val evaluationQuestions: Int = 0, // Preguntas de evaluación creadas
    val totalRequired: Int = 35,      // Total requerido por tipo
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherContentManagerScreen() {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var modules by remember { mutableStateOf(getICFESModules()) }

    // 📊 Cargar progreso desde Firebase
    LaunchedEffect(Unit) {
        loadModuleProgress { updatedModules ->
            modules = updatedModules
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Gestión de Contenido ICFES",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Administra tus preguntas personalizadas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as ComponentActivity).finish()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            LoadingScreen(paddingValues)
        } else {
            ContentManagerDashboard(
                paddingValues = paddingValues,
                modules = modules
            )
        }
    }
}

@Composable
fun LoadingScreen(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cargando contenido de módulos...")
        }
    }
}

@Composable
fun ContentManagerDashboard(
    paddingValues: PaddingValues,
    modules: List<ICFESModuleContent>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 📊 Header con estadísticas generales
        item {
            OverallProgressCard(modules)
        }

        // 🎯 Título de módulos
        item {
            Text(
                "📚 Módulos ICFES",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // 📋 Lista de módulos
        items(modules) { module ->
            ModuleContentCard(
                module = module,
                onClick = {
                    // TODO: Navegar a editor específico del módulo
                    // val intent = Intent(context, ModuleEditorActivity::class.java)
                    // intent.putExtra("module_id", module.id)
                    // context.startActivity(intent)
                }
            )
        }

        // 🚀 Información adicional
        item {
            InfoCard()
        }
    }
}

@Composable
fun OverallProgressCard(modules: List<ICFESModuleContent>) {
    val totalPracticeCreated = modules.sumOf { it.practiceQuestions }
    val totalEvaluationCreated = modules.sumOf { it.evaluationQuestions }
    val totalRequired = modules.size * 35 // 5 módulos × 35 preguntas
    val completedModules = modules.count {
        it.practiceQuestions >= it.totalRequired && it.evaluationQuestions >= it.totalRequired
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "📊 Progreso General",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Estadísticas en grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    title = "Módulos\nCompletos",
                    value = "$completedModules/5",
                    color = Color(0xFF4CAF50)
                )
                StatItem(
                    title = "Práctica\nCreadas",
                    value = "$totalPracticeCreated/$totalRequired",
                    color = Color(0xFF2196F3)
                )
                StatItem(
                    title = "Evaluación\nCreadas",
                    value = "$totalEvaluationCreated/$totalRequired",
                    color = Color(0xFFFF9800)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de progreso general
            val overallProgress = (totalPracticeCreated + totalEvaluationCreated).toFloat() / (totalRequired * 2)

            LinearProgressIndicator(
                progress = overallProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                "${(overallProgress * 100).toInt()}% del contenido total completado",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun StatItem(title: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            title,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun ModuleContentCard(
    module: ICFESModuleContent,
    onClick: () -> Unit
) {
    val context = LocalContext.current  // ✅ MOVER AQUÍ - DENTRO DEL COMPOSABLE
    val practiceProgress = module.practiceQuestions.toFloat() / module.totalRequired
    val evaluationProgress = module.evaluationQuestions.toFloat() / module.totalRequired
    val isComplete = practiceProgress >= 1f && evaluationProgress >= 1f

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            // ✅ USAR EL CONTEXTO OBTENIDO ARRIBA
            val intent = Intent(context, ModuleEditorActivity::class.java)
            intent.putExtra("module_id", module.id)
            context.startActivity(intent)
        },
        colors = CardDefaults.cardColors(
            containerColor = if (isComplete)
                module.color.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isComplete) 8.dp else 4.dp
        )
    ) {
        // ... resto del código igual
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header del módulo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji e icono de estado
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        module.emoji,
                        fontSize = 32.sp
                    )
                    if (isComplete) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Completo",
                            modifier = Modifier
                                .size(16.dp)
                                .offset(x = 12.dp, y = (-12).dp),
                            tint = Color(0xFF4CAF50)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        module.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        module.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Estado general
                if (isComplete) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            "COMPLETO",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "Gestionar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progreso de práctica y evaluación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Práctica
                ProgressSection(
                    modifier = Modifier.weight(1f),
                    title = "📚 Práctica",
                    current = module.practiceQuestions,
                    total = module.totalRequired,
                    progress = practiceProgress,
                    color = Color(0xFF2196F3)
                )

                // Evaluación
                ProgressSection(
                    modifier = Modifier.weight(1f),
                    title = "⏱️ Evaluación",
                    current = module.evaluationQuestions,
                    total = module.totalRequired,
                    progress = evaluationProgress,
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
fun ProgressSection(
    modifier: Modifier = Modifier,
    title: String,
    current: Int,
    total: Int,
    progress: Float,
    color: Color
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                "$current/$total",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )

        Text(
            "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "💡 Información",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "• Cada módulo requiere 35 preguntas de práctica y 35 de evaluación\n" +
                        "• Las preguntas de práctica incluyen feedback inmediato\n" +
                        "• Las preguntas de evaluación son para simulacros cronometrados\n" +
                        "• Tus estudiantes accederán a este contenido personalizado",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// 📊 Funciones auxiliares
fun getICFESModules(): List<ICFESModuleContent> {
    return listOf(
        ICFESModuleContent(
            id = "lectura_critica",
            name = "Lectura Crítica",
            emoji = "📖",
            color = Color(0xFF2196F3),
            description = "Comprensión e interpretación textual"
        ),
        ICFESModuleContent(
            id = "matematicas",
            name = "Matemáticas",
            emoji = "🔢",
            color = Color(0xFFFF9800),
            description = "Razonamiento cuantitativo"
        ),
        ICFESModuleContent(
            id = "ciencias_naturales",
            name = "Ciencias Naturales",
            emoji = "🧪",
            color = Color(0xFF4CAF50),
            description = "Uso del conocimiento científico"
        ),
        ICFESModuleContent(
            id = "sociales_ciudadanas",
            name = "Sociales y Ciudadanas",
            emoji = "🏛️",
            color = Color(0xFF9C27B0),
            description = "Pensamiento social y ciudadano"
        ),
        ICFESModuleContent(
            id = "ingles",
            name = "Inglés",
            emoji = "🇺🇸",
            color = Color(0xFFF44336),
            description = "Comunicación en lengua inglesa"
        )
    )
}

suspend fun loadModuleProgress(onResult: (List<ICFESModuleContent>) -> Unit) {
    try {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val database = FirebaseDatabase.getInstance()
            val snapshot = database.reference
                .child("ContenidoDocente")
                .child("profesores")
                .child(currentUser.uid)
                .child("modulos")
                .get()
                .await()

            val modules = getICFESModules().map { module ->
                val moduleData = snapshot.child(module.id)
                val practiceCount = moduleData.child("practica").childrenCount.toInt()
                val evaluationCount = moduleData.child("evaluacion").childrenCount.toInt()

                module.copy(
                    practiceQuestions = practiceCount,
                    evaluationQuestions = evaluationCount
                )
            }

            onResult(modules)
        } else {
            onResult(getICFESModules())
        }
    } catch (e: Exception) {
        // En caso de error, devolver módulos vacíos
        onResult(getICFESModules())
    }
}