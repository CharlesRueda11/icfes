package com.charlesdev.icfes.teacher.practice_evaluation



import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charlesdev.icfes.ui.theme.IcfesTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class QuestionCreatorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val moduleId = intent.getStringExtra("module_id") ?: ""
        val questionType = intent.getStringExtra("question_type") ?: "practica"
        val questionId = intent.getStringExtra("question_id") // Para editar pregunta existente

        setContent {
            IcfesTheme {
                QuestionCreatorScreen(
                    moduleId = moduleId,
                    questionType = questionType,
                    questionId = questionId
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionCreatorScreen(
    moduleId: String,
    questionType: String,
    questionId: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(questionId != null) }
    var isSaving by remember { mutableStateOf(false) }

    // 📝 Estados del formulario
    var questionText by remember { mutableStateOf("") }
    var contextText by remember { mutableStateOf("") }
    var optionA by remember { mutableStateOf("") }
    var optionB by remember { mutableStateOf("") }
    var optionC by remember { mutableStateOf("") }
    var optionD by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableStateOf("A") }
    var competency by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("MEDIO") }
    var timeEstimated by remember { mutableStateOf("120") }
    var explanation by remember { mutableStateOf("") }

    // 🎯 Obtener información del módulo
    val module = remember { getICFESModules().find { it.id == moduleId } }
    val isEditMode = questionId != null

    // 📊 Cargar pregunta si estamos editando
    LaunchedEffect(questionId) {
        if (questionId != null) {
            loadQuestion(moduleId, questionType, questionId) { question ->
                question?.let {
                    questionText = it.question
                    contextText = it.context
                    optionA = it.optionA
                    optionB = it.optionB
                    optionC = it.optionC
                    optionD = it.optionD
                    correctAnswer = it.correctAnswer
                    competency = it.competency
                    difficulty = it.difficulty
                    timeEstimated = it.timeEstimated.toString()
                    explanation = it.explanation
                }
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (isEditMode) "Editar Pregunta" else "Nueva Pregunta",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${module?.emoji ?: ""} ${module?.name ?: ""} • ${if (questionType == "practica") "Práctica" else "Evaluación"}",
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
        }
    ) { paddingValues ->
        if (isLoading) {
            LoadingQuestionScreen(paddingValues)
        } else {
            QuestionForm(
                paddingValues = paddingValues,
                module = module,
                questionType = questionType,
                questionText = questionText,
                onQuestionTextChange = { questionText = it },
                contextText = contextText,
                onContextTextChange = { contextText = it },
                optionA = optionA,
                onOptionAChange = { optionA = it },
                optionB = optionB,
                onOptionBChange = { optionB = it },
                optionC = optionC,
                onOptionCChange = { optionC = it },
                optionD = optionD,
                onOptionDChange = { optionD = it },
                correctAnswer = correctAnswer,
                onCorrectAnswerChange = { correctAnswer = it },
                competency = competency,
                onCompetencyChange = { competency = it },
                difficulty = difficulty,
                onDifficultyChange = { difficulty = it },
                timeEstimated = timeEstimated,
                onTimeEstimatedChange = { timeEstimated = it },
                explanation = explanation,
                onExplanationChange = { explanation = it },
                isSaving = isSaving,
                onSave = {
                    scope.launch {
                        isSaving = true
                        try {
                            val question = TeacherQuestion(
                                id = questionId ?: generateQuestionId(),
                                type = questionType,
                                question = questionText,
                                context = contextText,
                                optionA = optionA,
                                optionB = optionB,
                                optionC = optionC,
                                optionD = optionD,
                                correctAnswer = correctAnswer,
                                competency = competency,
                                difficulty = difficulty,
                                timeEstimated = timeEstimated.toIntOrNull() ?: 120,
                                explanation = explanation
                            )

                            val success = saveQuestion(moduleId, questionType, question)
                            if (success) {
                                (context as ComponentActivity).finish()
                            }
                        } catch (e: Exception) {
                            // Manejar error
                        } finally {
                            isSaving = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun LoadingQuestionScreen(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cargando pregunta...")
        }
    }
}

@Composable
fun QuestionForm(
    paddingValues: PaddingValues,
    module: ICFESModuleContent?,
    questionType: String,
    questionText: String,
    onQuestionTextChange: (String) -> Unit,
    contextText: String,
    onContextTextChange: (String) -> Unit,
    optionA: String,
    onOptionAChange: (String) -> Unit,
    optionB: String,
    onOptionBChange: (String) -> Unit,
    optionC: String,
    onOptionCChange: (String) -> Unit,
    optionD: String,
    onOptionDChange: (String) -> Unit,
    correctAnswer: String,
    onCorrectAnswerChange: (String) -> Unit,
    competency: String,
    onCompetencyChange: (String) -> Unit,
    difficulty: String,
    onDifficultyChange: (String) -> Unit,
    timeEstimated: String,
    onTimeEstimatedChange: (String) -> Unit,
    explanation: String,
    onExplanationChange: (String) -> Unit,
    isSaving: Boolean,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 🎯 Información del tipo de pregunta
        QuestionTypeInfo(questionType, module)

        // 📄 Texto base (contexto) - Opcional
        ContextSection(
            contextText = contextText,
            onContextTextChange = onContextTextChange
        )

        // ❓ Pregunta principal
        QuestionSection(
            questionText = questionText,
            onQuestionTextChange = onQuestionTextChange
        )

        // 🔘 Opciones de respuesta
        OptionsSection(
            optionA = optionA,
            onOptionAChange = onOptionAChange,
            optionB = optionB,
            onOptionBChange = onOptionBChange,
            optionC = optionC,
            onOptionCChange = onOptionCChange,
            optionD = optionD,
            onOptionDChange = onOptionDChange,
            correctAnswer = correctAnswer,
            onCorrectAnswerChange = onCorrectAnswerChange
        )

        // ⚙️ Configuración de la pregunta
        QuestionConfigSection(
            competency = competency,
            onCompetencyChange = onCompetencyChange,
            difficulty = difficulty,
            onDifficultyChange = onDifficultyChange,
            timeEstimated = timeEstimated,
            onTimeEstimatedChange = onTimeEstimatedChange,
            module = module
        )

        // 📚 Explicación (solo para práctica)
        if (questionType == "practica") {
            ExplanationSection(
                explanation = explanation,
                onExplanationChange = onExplanationChange
            )
        }

        // 💾 Botón guardar
        SaveButton(
            isSaving = isSaving,
            onSave = onSave,
            module = module,
            isFormValid = questionText.isNotEmpty() &&
                    optionA.isNotEmpty() &&
                    optionB.isNotEmpty() &&
                    optionC.isNotEmpty() &&
                    optionD.isNotEmpty() &&
                    competency.isNotEmpty()
        )

        Spacer(modifier = Modifier.height(80.dp)) // Espacio adicional al final
    }
}

@Composable
fun QuestionTypeInfo(questionType: String, module: ICFESModuleContent?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (questionType == "practica")
                Color(0xFF2196F3).copy(alpha = 0.1f)
            else
                Color(0xFFFF9800).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (questionType == "practica") "📚" else "⏱️",
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    if (questionType == "practica") "Pregunta de Práctica" else "Pregunta de Evaluación",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (questionType == "practica")
                        "• Incluye feedback inmediato\n• Sin límite de tiempo\n• Enfoque en aprendizaje"
                    else
                        "• Sin feedback hasta el final\n• Tiempo cronometrado\n• Simula examen real",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ContextSection(
    contextText: String,
    onContextTextChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "📄 Texto Base (Opcional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Para preguntas de comprensión lectora o que requieran contexto",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = contextText,
                onValueChange = onContextTextChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Escribe el texto base aquí...") },
                minLines = 3,
                maxLines = 8
            )
        }
    }
}

@Composable
fun QuestionSection(
    questionText: String,
    onQuestionTextChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "❓ Pregunta Principal *",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = questionText,
                onValueChange = onQuestionTextChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("¿Cuál es la pregunta?") },
                minLines = 2,
                maxLines = 6,
                isError = questionText.isEmpty()
            )
            if (questionText.isEmpty()) {
                Text(
                    "* Campo obligatorio",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun OptionsSection(
    optionA: String,
    onOptionAChange: (String) -> Unit,
    optionB: String,
    onOptionBChange: (String) -> Unit,
    optionC: String,
    onOptionCChange: (String) -> Unit,
    optionD: String,
    onOptionDChange: (String) -> Unit,
    correctAnswer: String,
    onCorrectAnswerChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "🔘 Opciones de Respuesta *",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val options = listOf(
                "A" to Pair(optionA, onOptionAChange),
                "B" to Pair(optionB, onOptionBChange),
                "C" to Pair(optionC, onOptionCChange),
                "D" to Pair(optionD, onOptionDChange)
            )

            options.forEach { (letter, option) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = correctAnswer == letter,
                        onClick = { onCorrectAnswerChange(letter) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = option.first,
                        onValueChange = option.second,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Opción $letter") },
                        label = { Text("$letter)") },
                        isError = option.first.isEmpty(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (correctAnswer == letter) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                            focusedLabelColor = if (correctAnswer == letter) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "✓ Selecciona la respuesta correcta marcando el círculo",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
fun QuestionConfigSection(
    competency: String,
    onCompetencyChange: (String) -> Unit,
    difficulty: String,
    onDifficultyChange: (String) -> Unit,
    timeEstimated: String,
    onTimeEstimatedChange: (String) -> Unit,
    module: ICFESModuleContent?
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "⚙️ Configuración",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Competencia
            OutlinedTextField(
                value = competency,
                onValueChange = onCompetencyChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Competencia ICFES *") },
                placeholder = { Text("ej: Comprensión inferencial") },
                isError = competency.isEmpty()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dificultad
            Text(
                "Dificultad:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("FACIL", "MEDIO", "DIFICIL").forEach { level ->
                    FilterChip(
                        onClick = { onDifficultyChange(level) },
                        label = { Text(level) },
                        selected = difficulty == level,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tiempo estimado
            OutlinedTextField(
                value = timeEstimated,
                onValueChange = onTimeEstimatedChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tiempo estimado (segundos)") },
                placeholder = { Text("120") }
            )
        }
    }
}

@Composable
fun ExplanationSection(
    explanation: String,
    onExplanationChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "📚 Explicación para Práctica",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Esta explicación se mostrará cuando el estudiante responda",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = explanation,
                onValueChange = onExplanationChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Explica por qué esta es la respuesta correcta...") },
                minLines = 3,
                maxLines = 6
            )
        }
    }
}

@Composable
fun SaveButton(
    isSaving: Boolean,
    onSave: () -> Unit,
    module: ICFESModuleContent?,
    isFormValid: Boolean
) {
    Button(
        onClick = onSave,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = !isSaving && isFormValid,
        colors = ButtonDefaults.buttonColors(
            containerColor = module?.color ?: MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Guardando...")
        } else {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "💾 Guardar Pregunta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 📊 Funciones auxiliares
fun generateQuestionId(): String {
    return "Q_${System.currentTimeMillis()}_${(1000..9999).random()}"
}

suspend fun loadQuestion(
    moduleId: String,
    questionType: String,
    questionId: String,
    onResult: (TeacherQuestion?) -> Unit
) {
    try {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val database = FirebaseDatabase.getInstance()
            val snapshot = database.reference
                .child("ContenidoDocente")
                .child("profesores")
                .child(currentUser.uid)
                .child("modulos")
                .child(moduleId)
                .child(questionType)
                .child(questionId)
                .get()
                .await()

            val question = snapshot.getValue(TeacherQuestion::class.java)
            onResult(question)
        } else {
            onResult(null)
        }
    } catch (e: Exception) {
        onResult(null)
    }
}

suspend fun saveQuestion(
    moduleId: String,
    questionType: String,
    question: TeacherQuestion
): Boolean {
    return try {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val database = FirebaseDatabase.getInstance()
            database.reference
                .child("ContenidoDocente")
                .child("profesores")
                .child(currentUser.uid)
                .child("modulos")
                .child(moduleId)
                .child(questionType)
                .child(question.id)
                .setValue(question)
                .await()
            true
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }
}