package com.charlesdev.icfes.teacher.practice_evaluation



import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.sp
import com.charlesdev.icfes.ui.theme.IcfesTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class ModuleEditorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val moduleId = intent.getStringExtra("module_id") ?: ""

        setContent {
            IcfesTheme {
                ModuleEditorScreen(moduleId = moduleId)
            }
        }
    }
}

// 📝 Data classes para preguntas
data class TeacherQuestion(
    val id: String = "",
    val type: String = "practica", // "practica" o "evaluacion"
    val question: String = "",
    val context: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctAnswer: String = "",
    val competency: String = "",
    val difficulty: String = "MEDIO",
    val timeEstimated: Int = 120,
    val explanation: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleEditorScreen(moduleId: String) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var module by remember { mutableStateOf<ICFESModuleContent?>(null) }
    var practiceQuestions by remember { mutableStateOf<List<TeacherQuestion>>(emptyList()) }
    var evaluationQuestions by remember { mutableStateOf<List<TeacherQuestion>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) }

    // 📊 Cargar datos del módulo
    LaunchedEffect(moduleId) {
        loadModuleData(moduleId) { moduleData, practice, evaluation ->
            module = moduleData
            practiceQuestions = practice
            evaluationQuestions = evaluation
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            module?.name ?: "Cargando...",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (module != null) "${module!!.emoji} Gestión de preguntas" else "",
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
                    containerColor = module?.color?.copy(alpha = 0.1f) ?: MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    // TODO: Navegar a QuestionCreatorActivity
                    val intent = Intent(context, QuestionCreatorActivity::class.java)
                    intent.putExtra("module_id", moduleId)
                    intent.putExtra("question_type", if (selectedTab == 0) "practica" else "evaluacion")
                    context.startActivity(intent)
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nueva Pregunta") },
                containerColor = module?.color ?: MaterialTheme.colorScheme.primary
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            LoadingModuleScreen(paddingValues)
        } else {
            module?.let { moduleData ->
                ModuleEditorContent(
                    paddingValues = paddingValues,
                    module = moduleData,
                    practiceQuestions = practiceQuestions,
                    evaluationQuestions = evaluationQuestions,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        }
    }
}

@Composable
fun LoadingModuleScreen(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cargando preguntas del módulo...")
        }
    }
}

@Composable
fun ModuleEditorContent(
    paddingValues: PaddingValues,
    module: ICFESModuleContent,
    practiceQuestions: List<TeacherQuestion>,
    evaluationQuestions: List<TeacherQuestion>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // 📊 Estadísticas del módulo
        ModuleStatsCard(
            module = module,
            practiceCount = practiceQuestions.size,
            evaluationCount = evaluationQuestions.size
        )

        // 📋 Tabs para práctica vs evaluación
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = module.color.copy(alpha = 0.1f),
            contentColor = module.color
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📚 Práctica")
                        Text(
                            "${practiceQuestions.size}/35",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⏱️ Evaluación")
                        Text(
                            "${evaluationQuestions.size}/35",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )
        }

        // 📝 Lista de preguntas
        val currentQuestions = if (selectedTab == 0) practiceQuestions else evaluationQuestions

        if (currentQuestions.isEmpty()) {
            EmptyQuestionsScreen(
                questionType = if (selectedTab == 0) "práctica" else "evaluación",
                module = module
            )
        } else {
            QuestionsList(
                questions = currentQuestions,
                module = module
            )
        }
    }
}

@Composable
fun ModuleStatsCard(
    module: ICFESModuleContent,
    practiceCount: Int,
    evaluationCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = module.color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji del módulo
            Text(
                module.emoji,
                fontSize = 40.sp,
                modifier = Modifier.padding(end = 16.dp)
            )

            // Estadísticas
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    module.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = module.color
                )
                Text(
                    module.description,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatChip(
                        label = "Práctica",
                        value = "$practiceCount/35",
                        color = Color(0xFF2196F3),
                        progress = practiceCount / 35f
                    )
                    StatChip(
                        label = "Evaluación",
                        value = "$evaluationCount/35",
                        color = Color(0xFFFF9800),
                        progress = evaluationCount / 35f
                    )
                }
            }
        }
    }
}

@Composable
fun StatChip(
    label: String,
    value: String,
    color: Color,
    progress: Float
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun EmptyQuestionsScreen(
    questionType: String,
    module: ICFESModuleContent
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                module.emoji,
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Sin preguntas de $questionType",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Crea tu primera pregunta de $questionType para ${module.name.lowercase()}",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = module.color.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "💡 Diferencias:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (questionType == "práctica") {
                        Text("• Feedback inmediato con IA", style = MaterialTheme.typography.bodySmall)
                        Text("• Sin límite de tiempo", style = MaterialTheme.typography.bodySmall)
                        Text("• Enfoque en aprendizaje", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text("• Sin feedback hasta el final", style = MaterialTheme.typography.bodySmall)
                        Text("• Tiempo cronometrado", style = MaterialTheme.typography.bodySmall)
                        Text("• Simula examen real", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionsList(
    questions: List<TeacherQuestion>,
    module: ICFESModuleContent
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(questions) { question ->
            QuestionCard(
                question = question,
                module = module,
                onClick = {
                    // TODO: Editar pregunta
                }
            )
        }
    }
}

@Composable
fun QuestionCard(
    question: TeacherQuestion,
    module: ICFESModuleContent,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    question.question.take(60) + if (question.question.length > 60) "..." else "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = module.color.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        question.correctAnswer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = module.color
                    )
                }
            }

            if (question.context.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "📄 Con texto base",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    question.competency,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${question.timeEstimated}s • ${question.difficulty}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// 📊 Funciones auxiliares
suspend fun loadModuleData(
    moduleId: String,
    onResult: (ICFESModuleContent?, List<TeacherQuestion>, List<TeacherQuestion>) -> Unit
) {
    try {
        val module = getICFESModules().find { it.id == moduleId }
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null && module != null) {
            val database = FirebaseDatabase.getInstance()
            val moduleRef = database.reference
                .child("ContenidoDocente")
                .child("profesores")
                .child(currentUser.uid)
                .child("modulos")
                .child(moduleId)

            val practiceSnapshot = moduleRef.child("practica").get().await()
            val evaluationSnapshot = moduleRef.child("evaluacion").get().await()

            val practiceQuestions = mutableListOf<TeacherQuestion>()
            val evaluationQuestions = mutableListOf<TeacherQuestion>()

            // Convertir datos de Firebase a TeacherQuestion
            practiceSnapshot.children.forEach { child ->
                val question = child.getValue(TeacherQuestion::class.java)
                question?.let { practiceQuestions.add(it) }
            }

            evaluationSnapshot.children.forEach { child ->
                val question = child.getValue(TeacherQuestion::class.java)
                question?.let { evaluationQuestions.add(it) }
            }

            onResult(module, practiceQuestions, evaluationQuestions)
        } else {
            onResult(null, emptyList(), emptyList())
        }
    } catch (e: Exception) {
        onResult(null, emptyList(), emptyList())
    }
}