package com.charlesdev.icfes.student.premium

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.concurrent.TimeUnit

import androidx.compose.animation.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.collectAsState

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.*

// ✅ PANTALLA PRINCIPAL PREMIUM CON INTEGRACIÓN COMPLETA DE DIÁLOGOS
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumQuizScreen(
    moduleId: String,
    teacherId: String,
    sessionType: String,
    moduleName: String,
    teacherName: String
) {
    val viewModel: PremiumQuizViewModel = viewModel()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadPremiumQuestions(
            moduleId = moduleId,
            teacherId = teacherId,
            sessionTypeString = sessionType,
            teacherName = teacherName,
            moduleName = moduleName
        )
    }

    // ✅ STATES PARA TIEMPO
    val timeState by viewModel.timeState.collectAsState()
    val currentQuestion = viewModel.getCurrentQuestion()

    Scaffold(
        topBar = {
            PremiumQuizTopBar(
                moduleName = moduleName,
                teacherName = teacherName,
                currentQuestion = viewModel.currentQuestionIndex + 1,
                totalQuestions = viewModel.currentQuestions.size,
                sessionType = sessionType,
                timeRemaining = timeState,
                isEvaluationMode = viewModel.isEvaluationMode,
                onBackClick = {
                    (context as ComponentActivity).finish()
                }
            )
        }
    ) { paddingValues ->

        // ✅ MANEJO DE ESTADOS
        when {
            viewModel.isLoading -> {
                PremiumLoadingScreen(paddingValues)
            }
            viewModel.hasError -> {
                PremiumErrorScreen(
                    message = viewModel.errorMessage,
                    onRetry = {
                        viewModel.loadPremiumQuestions(
                            moduleId, teacherId, sessionType, teacherName, moduleName
                        )
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            viewModel.showResults -> {
                PremiumQuizResults(
                    viewModel = viewModel,
                    sessionType = sessionType,
                    moduleId = moduleId,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                PremiumQuizContent(
                    viewModel = viewModel,
                    sessionType = sessionType,
                    paddingValues = paddingValues
                )
            }
        }
    }

    // ✅ DIÁLOGOS PREMIUM

    // Diálogo de feedback inmediato (solo práctica)
    if (viewModel.showFeedback && sessionType == "practica") {
        PremiumFeedbackDialog(
            viewModel = viewModel,
            teacherName = teacherName,
            onDismiss = { viewModel.closeFeedback() }
        )
    }

    // ✅ NUEVO: Diálogo de carga para evaluación premium
    if (viewModel.isGeneratingPremiumFeedback) {
        PremiumEvaluationLoadingDialog()
    }

    // ✅ NUEVO: Diálogo de resultados de evaluación premium
    if (viewModel.showPremiumEvaluationSummary && viewModel.premiumEvaluationResult != null) {
        PremiumEvaluationSummaryDialog(
            viewModel = viewModel,
            result = viewModel.premiumEvaluationResult!!,
            onDismiss = { viewModel.closePremiumEvaluationSummary() },
            onRetry = { viewModel.retryPremiumEvaluation() },
            onFinish = {
                viewModel.closePremiumEvaluationSummary()
                (context as ComponentActivity).finish()
            }
        )
    }
}

// ✅ TOP BAR PREMIUM MEJORADO
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumQuizTopBar(
    moduleName: String,
    teacherName: String,
    currentQuestion: Int,
    totalQuestions: Int,
    sessionType: String,
    timeRemaining: Long,
    isEvaluationMode: Boolean = false,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$moduleName Premium",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Badge Premium
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFD700)
                        )
                    ) {
                        Text(
                            "⭐ PREMIUM",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7B1FA2)
                        )
                    }

                    // Badge Evaluación
                    if (isEvaluationMode) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            )
                        ) {
                            Text(
                                "EVALUACIÓN",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Text(
                    "Pregunta $currentQuestion de $totalQuestions • Prof. $teacherName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
        },
        actions = {
            if (isEvaluationMode && timeRemaining > 0) {
                PremiumTimerChip(timeRemaining = timeRemaining)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (isEvaluationMode)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            else
                Color(0xFF7B1FA2).copy(alpha = 0.1f)
        )
    )
}

// ✅ TIMER CHIP PREMIUM
@Composable
fun PremiumTimerChip(timeRemaining: Long) {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeRemaining)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeRemaining) % 60

    val timerColor = when {
        timeRemaining > 300000 -> Color(0xFF4CAF50) // Verde > 5 min
        timeRemaining > 60000 -> Color(0xFFFF9800)  // Naranja > 1 min
        else -> Color(0xFFF44336) // Rojo < 1 min
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = timerColor.copy(alpha = 0.1f)
        ),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Timer,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = timerColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                String.format("%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = timerColor
            )
        }
    }
}

// ✅ PANTALLAS DE ESTADO
@Composable
fun PremiumLoadingScreen(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = Color(0xFF7B1FA2),
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Cargando contenido premium...",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFFFD700)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Preparado por tu profesor",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PremiumErrorScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Error al cargar contenido premium",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7B1FA2)
            )
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reintentar")
        }
    }
}
// ✅ PARTE 2: CONTENIDO PRINCIPAL DEL QUIZ Y COMPONENTES

// ✅ CONTENIDO PRINCIPAL PREMIUM
@Composable
fun PremiumQuizContent(
    viewModel: PremiumQuizViewModel,
    sessionType: String,
    paddingValues: PaddingValues
) {
    val currentQuestion = viewModel.getCurrentQuestion()

    if (currentQuestion == null) {
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFF7B1FA2))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    if (viewModel.isEvaluationMode)
                        "Preparando evaluación premium..."
                    else
                        "Cargando preguntas premium..."
                )
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = if (viewModel.hasAnsweredCurrentQuestion) 200.dp else 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Información de pregunta premium
            item {
                PremiumQuestionInfo(
                    question = currentQuestion,
                    isEvaluationMode = viewModel.isEvaluationMode,
                    teacherName = viewModel.currentTeacherName
                )
            }

            // 2. Contexto (si existe)
            if (!currentQuestion.context.isNullOrEmpty()) {
                item {
                    PremiumContextCard(context = currentQuestion.context)
                }
            }

            // 3. Pregunta principal
            item {
                PremiumQuestionCard(question = currentQuestion)
            }

            // 4. Opciones de respuesta
            item {
                PremiumAnswerOptions(
                    question = currentQuestion,
                    viewModel = viewModel
                )
            }

            // 5. Hint card (solo práctica)
            if (sessionType == "practica" && !viewModel.isEvaluationMode) {
                item {
                    PremiumHintCard(teacherName = viewModel.currentTeacherName)
                }
            }

            // 6. Botón submit
            item {
                PremiumSubmitButton(
                    viewModel = viewModel,
                    question = currentQuestion
                )
            }
        }

        // 7. Barra inferior de navegación
        if (viewModel.hasAnsweredCurrentQuestion &&
            (!viewModel.isEvaluationMode || !viewModel.evaluationCompleted)) {
            PremiumQuizBottomBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                viewModel = viewModel,
                currentQuestion = currentQuestion,
                sessionType = sessionType,
                totalQuestions = viewModel.currentQuestions.size
            )
        }
    }
}

// ✅ 1. INFORMACIÓN DE PREGUNTA PREMIUM
@Composable
fun PremiumQuestionInfo(
    question: PremiumQuestion,
    isEvaluationMode: Boolean = false,
    teacherName: String = ""
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isEvaluationMode)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                Color(0xFF7B1FA2).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF7B1FA2)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    question.competency,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Badge Premium
                Surface(
                    color = Color(0xFFFFD700),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = Color(0xFF7B1FA2)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            "PREMIUM",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7B1FA2)
                        )
                    }
                }

                // Badge Evaluación
                if (isEvaluationMode) {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "EVAL",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Dificultad
                Surface(
                    color = when (question.difficulty) {
                        "FACIL" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        "MEDIO" -> Color(0xFFFF9800).copy(alpha = 0.2f)
                        "DIFICIL" -> Color(0xFFF44336).copy(alpha = 0.2f)
                        else -> Color(0xFFFF9800).copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        question.difficulty,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (question.difficulty) {
                            "FACIL" -> Color(0xFF4CAF50)
                            "MEDIO" -> Color(0xFFFF9800)
                            "DIFICIL" -> Color(0xFFF44336)
                            else -> Color(0xFFFF9800)
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ✅ 2. CONTEXTO PREMIUM (EXPANDIBLE)
@Composable
fun PremiumContextCard(context: String) {
    var isExpanded by remember { mutableStateOf(false) }
    val maxLines = if (isExpanded) Int.MAX_VALUE else 5

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF7B1FA2).copy(alpha = 0.1f)
        ),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    Icons.Default.Article,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF7B1FA2)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "📄 Texto base premium",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7B1FA2)
                )
                Spacer(modifier = Modifier.weight(1f))

                if (context.length > 300) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Ver menos" else "Ver más",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFF7B1FA2)
                    )
                }
            }

            Text(
                context,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis
            )

            if (!isExpanded && context.length > 300) {
                Text(
                    "Toca para ver más...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF7B1FA2),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// ✅ 3. PREGUNTA PRINCIPAL PREMIUM
@Composable
fun PremiumQuestionCard(question: PremiumQuestion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF7B1FA2).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF7B1FA2)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "⭐ Pregunta Premium",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7B1FA2)
                )
            }

            Text(
                question.question,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.headlineSmall.lineHeight * 1.2
            )
        }
    }
}

// ✅ 4. OPCIONES DE RESPUESTA PREMIUM
@Composable
fun PremiumAnswerOptions(
    question: PremiumQuestion,
    viewModel: PremiumQuizViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.RadioButtonChecked,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF7B1FA2)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Selecciona tu respuesta:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            question.options.forEachIndexed { index, option ->
                val optionLetter = option.first().toString() // A, B, C, D

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (viewModel.userAnswer.equals(optionLetter, ignoreCase = true))
                            Color(0xFF7B1FA2).copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    onClick = {
                        if (!viewModel.hasAnsweredCurrentQuestion) {
                            viewModel.userAnswer = optionLetter
                        }
                    },
                    border = if (viewModel.userAnswer.equals(optionLetter, ignoreCase = true))
                        BorderStroke(2.dp, Color(0xFF7B1FA2))
                    else
                        null
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        RadioButton(
                            selected = viewModel.userAnswer.equals(optionLetter, ignoreCase = true),
                            onClick = {
                                if (!viewModel.hasAnsweredCurrentQuestion) {
                                    viewModel.userAnswer = optionLetter
                                }
                            },
                            enabled = !viewModel.hasAnsweredCurrentQuestion,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF7B1FA2)
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            option,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// ✅ 5. HINT CARD PREMIUM (SOLO PRÁCTICA)
@Composable
fun PremiumHintCard(teacherName: String = "") {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFD700).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Psychology,
                contentDescription = null,
                tint = Color(0xFF7B1FA2),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    "💡 Modo práctica premium activado",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7B1FA2)
                )
                Text(
                    "Recibirás feedback detallado personalizado por $teacherName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ✅ 6. BOTÓN SUBMIT PREMIUM
@Composable
fun PremiumSubmitButton(
    viewModel: PremiumQuizViewModel,
    question: PremiumQuestion
) {
    AnimatedVisibility(
        visible = !viewModel.hasAnsweredCurrentQuestion,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Button(
            onClick = { viewModel.submitAnswer() },
            enabled = viewModel.userAnswer.isNotEmpty() && !viewModel.isValidatingWithAI,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (viewModel.isEvaluationMode)
                    MaterialTheme.colorScheme.error
                else
                    Color(0xFF7B1FA2)
            )
        ) {
            if (viewModel.isValidatingWithAI) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Generando feedback premium...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        if (viewModel.isEvaluationMode) Icons.Default.CheckCircle else Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (viewModel.isEvaluationMode) "Confirmar Respuesta Premium" else "Enviar Respuesta Premium",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
// ✅ PARTE 3: NAVEGACIÓN, DIÁLOGOS Y RESULTADOS

// ✅ 7. BARRA INFERIOR DE NAVEGACIÓN PREMIUM
@Composable
fun PremiumQuizBottomBar(
    modifier: Modifier = Modifier,
    viewModel: PremiumQuizViewModel,
    currentQuestion: PremiumQuestion,
    sessionType: String,
    totalQuestions: Int
) {
    val progressState by viewModel.progressState.collectAsState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .padding(bottom = 16.dp)
        ) {
            // Información diferenciada por modo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF7B1FA2)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Progreso: ${viewModel.currentQuestionIndex + 1}/$totalQuestions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (viewModel.isEvaluationMode) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${viewModel.currentQuestionIndex + 1} completadas",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF7B1FA2)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Puntaje: ${viewModel.score}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7B1FA2)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { (viewModel.currentQuestionIndex + 1).toFloat() / totalQuestions },
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = if (viewModel.isEvaluationMode)
                    MaterialTheme.colorScheme.error
                else
                    Color(0xFF7B1FA2)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Botones con texto diferenciado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.previousQuestion() },
                    enabled = viewModel.currentQuestionIndex > 0,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF7B1FA2)
                    )
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Anterior",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = { viewModel.nextQuestion() },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.isEvaluationMode)
                            MaterialTheme.colorScheme.error
                        else
                            Color(0xFF7B1FA2)
                    )
                ) {
                    Text(
                        if (viewModel.currentQuestionIndex < totalQuestions - 1) {
                            if (viewModel.isEvaluationMode) "Continuar" else "Siguiente"
                        } else {
                            if (viewModel.isEvaluationMode) "Finalizar Evaluación" else "Finalizar"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        if (viewModel.currentQuestionIndex < totalQuestions - 1)
                            Icons.Default.ArrowForward
                        else
                            Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Advertencia en evaluación
            if (viewModel.isEvaluationMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Modo evaluación premium: Sin feedback hasta el final",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ✅ REEMPLAZAR EL DIÁLOGO DE FEEDBACK PREMIUM EN PremiumQuizScreen.kt



// ✅ DIÁLOGO DE FEEDBACK PREMIUM CON TTS
@Composable
fun PremiumFeedbackDialog(
    viewModel: PremiumQuizViewModel,
    teacherName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val tts = remember { TextToSpeech(context, null) }

    // ✅ ESTADO PARA CONTROLAR LA REPRODUCCIÓN DE AUDIO
    var isPlayingAudio by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        tts.language = Locale("es", "ES")
        tts.setSpeechRate(0.8f)
        tts.setPitch(1.0f)
    }

    // ✅ DETENER AUDIO AL CERRAR EL DIÁLOGO
    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
            isPlayingAudio = false
        }
    }

    AlertDialog(
        onDismissRequest = {
            tts.stop()
            isPlayingAudio = false
            onDismiss()
        },
        icon = {
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (viewModel.isAnswerCorrect) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (viewModel.isAnswerCorrect) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(48.dp)
                )
                // Badge premium
                Surface(
                    modifier = Modifier
                        .size(20.dp)
                        .offset(x = 16.dp, y = (-16).dp),
                    color = Color(0xFFFFD700),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.padding(2.dp),
                        tint = Color(0xFF7B1FA2)
                    )
                }
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    viewModel.feedbackTitle,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = if (viewModel.isAnswerCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = "Feedback Premium",
                    tint = Color(0xFF7B1FA2),
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                // ✅ FEEDBACK PRINCIPAL PREMIUM
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF7B1FA2).copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                "⭐ Análisis Premium:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7B1FA2)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Prof. $teacherName",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            viewModel.feedbackMessage,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // ✅ CONSEJO SI EXISTE
                if (viewModel.feedbackTip.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFD700).copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "💡 Estrategia Premium:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7B1FA2),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                viewModel.feedbackTip,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // ✅ PERSONALIZACIÓN DEL PROFESOR SI EXISTE
                if (viewModel.teacherPersonalization.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFF7B1FA2)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "👨‍🏫 Comentario del Profesor:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7B1FA2)
                                )
                            }
                            Text(
                                viewModel.teacherPersonalization,
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // ✅ SEPARADOR VISUAL
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )

                    // ✅ BOTÓN DE AUDIO PREMIUM CON TTS
                    ElevatedButton(
                        onClick = {
                            if (isPlayingAudio) {
                                tts.stop()
                                isPlayingAudio = false
                            } else {
                                // ✅ CREAR TEXTO PREMIUM PARA TTS
                                val feedbackText = buildString {
                                    if (viewModel.isAnswerCorrect) {
                                        append("Respuesta correcta. ")
                                    } else {
                                        append("Respuesta incorreta. ")
                                    }

                                    append("Análisis premium del profesor $teacherName: ")
                                    append(viewModel.feedbackMessage)

                                    if (viewModel.feedbackTip.isNotEmpty()) {
                                        append(". Estrategia recomendada: ")
                                        append(viewModel.feedbackTip)
                                    }

                                    if (viewModel.teacherPersonalization.isNotEmpty()) {
                                        append(". Comentario personalizado: ")
                                        append(viewModel.teacherPersonalization)
                                    }
                                }

                                // ✅ LIMPIAR TEXTO PARA TTS
                                val cleanText = viewModel.cleanPremiumFeedbackForTTS(
                                    feedbackMessage = viewModel.feedbackMessage,
                                    feedbackTip = viewModel.feedbackTip,
                                    teacherPersonalization = viewModel.teacherPersonalization
                                )

                                tts.setSpeechRate(0.8f)
                                tts.setPitch(1.0f)

                                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                                    override fun onStart(utteranceId: String?) {
                                        isPlayingAudio = true
                                    }

                                    override fun onDone(utteranceId: String?) {
                                        isPlayingAudio = false
                                    }

                                    override fun onError(utteranceId: String?) {
                                        isPlayingAudio = false
                                    }
                                })

                                val utteranceId = "premium_feedback_${System.currentTimeMillis()}"
                                val params = Bundle().apply {
                                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
                                }
                                tts.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = if (isPlayingAudio)
                                Color(0xFFF44336).copy(alpha = 0.1f)
                            else
                                Color(0xFF7B1FA2).copy(alpha = 0.1f),
                            contentColor = if (isPlayingAudio)
                                Color(0xFFF44336)
                            else
                                Color(0xFF7B1FA2)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                if (isPlayingAudio) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                if (isPlayingAudio) "🔴 Detener Audio" else "🔊 Escuchar Feedback Premium",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            if (isPlayingAudio) {
                                Spacer(modifier = Modifier.width(8.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFFF44336)
                                )
                            }
                        }
                    }

                    // ✅ BOTÓN CONTINUAR
                    Button(
                        onClick = {
                            tts.stop()
                            isPlayingAudio = false
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.isAnswerCorrect)
                                Color(0xFF4CAF50) else Color(0xFF7B1FA2)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Continuar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        ),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(16.dp)
    )
}

// ✅ 9. PANTALLA DE RESULTADOS PREMIUM
@Composable
fun PremiumQuizResults(
    viewModel: PremiumQuizViewModel,
    sessionType: String,
    moduleId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF7B1FA2).copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFF7B1FA2)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        if (sessionType == "evaluacion") "Evaluación Premium Completada" else "Práctica Premium Completada",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF7B1FA2)
                    )
                    Text(
                        "Has terminado el contenido premium de ${viewModel.currentModuleName}",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Preparado por Prof. ${viewModel.currentTeacherName}",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Puntaje Premium
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFD700).copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color(0xFFFFD700)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "PUNTAJE PREMIUM",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7B1FA2)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "${viewModel.score}",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7B1FA2)
                            )
                            Text(
                                "de ${viewModel.currentQuestions.size * 10} puntos posibles",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.retryQuiz() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF7B1FA2)
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reintentar")
                }

                Button(
                    onClick = { (context as ComponentActivity).finish() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7B1FA2)
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Finalizar")
                }
            }
        }

        // Información adicional premium
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "🎯 Contenido Premium Completado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• Has completado contenido exclusivo creado por tu profesor\n" +
                                "• Este material está adaptado específicamente para tu institución\n" +
                                "• Combínalo con el contenido básico para una preparación completa",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// ✅ 10. FUNCIONES AUXILIARES DE INTEGRACIÓN

/**
 * ✅ FUNCIÓN PARA ACTUALIZAR SHAREDPREFERENCES PREMIUM
 */
fun savePremiumProgressToPrefs(
    context: Context,
    moduleId: String,
    sessionType: String,
    score: Int,
    percentage: Float,
    teacherId: String
) {
    val prefs = context.getSharedPreferences("ICFESPrefs", Context.MODE_PRIVATE)
    val key = if (sessionType == "evaluacion") {
        "premium_eval_${moduleId}"
    } else {
        "premium_practice_${moduleId}"
    }

    prefs.edit().apply {
        putInt(key, score)
        putFloat("${key}_percentage", percentage)
        putString("${key}_teacher", teacherId)
        putLong("${key}_timestamp", System.currentTimeMillis())
        apply()
    }
}

/**
 * ✅ FUNCIÓN PARA OBTENER PROGRESO PREMIUM
 */
fun getPremiumProgressFromPrefs(
    context: Context,
    moduleId: String,
    sessionType: String
): Triple<Int, Float, Long> {
    val prefs = context.getSharedPreferences("ICFESPrefs", Context.MODE_PRIVATE)
    val key = if (sessionType == "evaluacion") {
        "premium_eval_${moduleId}"
    } else {
        "premium_practice_${moduleId}"
    }

    val score = prefs.getInt(key, 0)
    val percentage = prefs.getFloat("${key}_percentage", 0f)
    val timestamp = prefs.getLong("${key}_timestamp", 0L)

    return Triple(score, percentage, timestamp)
}

/**
 * ✅ FUNCIÓN PARA VERIFICAR SI LA EVALUACIÓN ESTÁ DESBLOQUEADA
 */
fun isEvaluationUnlocked(context: Context, moduleId: String): Boolean {
    val (practiceScore, _, _) = getPremiumProgressFromPrefs(context, moduleId, "practica")
    return practiceScore >= 50 // Requiere al menos 50 puntos en práctica
}

/**
 * ✅ FUNCIÓN PARA OBTENER ESTADÍSTICAS COMPLETAS PREMIUM
 */
fun getPremiumModuleStats(context: Context, moduleId: String): Map<String, Any> {
    val (practiceScore, practicePercentage, practiceTimestamp) =
        getPremiumProgressFromPrefs(context, moduleId, "practica")
    val (evalScore, evalPercentage, evalTimestamp) =
        getPremiumProgressFromPrefs(context, moduleId, "evaluacion")

    return mapOf(
        "practice_score" to practiceScore,
        "practice_percentage" to practicePercentage,
        "practice_timestamp" to practiceTimestamp,
        "evaluation_score" to evalScore,
        "evaluation_percentage" to evalPercentage,
        "evaluation_timestamp" to evalTimestamp,
        "is_evaluation_unlocked" to isEvaluationUnlocked(context, moduleId)
    )
}