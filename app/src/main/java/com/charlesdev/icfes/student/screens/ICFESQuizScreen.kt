package com.charlesdev.icfes.student.screens



import android.os.Bundle
import android.provider.Settings.Global.putString
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
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

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.ui.unit.dp

import androidx.compose.ui.window.DialogProperties

import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavHostController

import com.charlesdev.icfes.student.data.*

import com.charlesdev.icfes.student.viewmodel.ICFESQuizViewModel

import com.charlesdev.icfes.student.evaluation.ICFESEvaluationResult

import java.util.*

import java.util.concurrent.TimeUnit



@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun ICFESQuizScreen(

    navController: NavHostController,

    moduleId: String,

    sessionType: String = "practice",

    onQuizComplete: () -> Unit = {}

) {

    val context = LocalContext.current

    val viewModel: ICFESQuizViewModel = viewModel()

    val tts = remember { TextToSpeech(context, null) }


    // ✅ DETECTAR SI ES SIMULACRO COMPLETO - AGREGAR ESTA LÍNEA
    val isFullSimulation = moduleId == "simulation" || moduleId == "simulacro_completo"


    // Convertir string a enum

    val sessionTypeEnum = when (sessionType) {
        "practice" -> SessionType.PRACTICE
        "timed" -> SessionType.TIMED_QUIZ
        "simulation" -> SessionType.SIMULATION
        "review" -> SessionType.REVIEW
        else -> if (isFullSimulation) SessionType.SIMULATION else SessionType.PRACTICE // ✅ AGREGAR ESTA LÍNEA
    }



    // Obtener el módulo actual

    val currentModule = remember {
        if (isFullSimulation) {
            null // Se configurará en el ViewModel
        } else {
            populatedICFESModules.find { it.id == moduleId }
        }
    }



    LaunchedEffect(Unit) {
        tts.language = Locale("es", "ES")
        tts.setSpeechRate(0.9f)
        tts.setPitch(1.0f)
        // ✅ MODIFICAR ESTA LÍNEA - PASAR EL moduleId CORRECTO
        val finalModuleId = if (isFullSimulation) "simulacro_completo" else moduleId
        viewModel.initializeSession(context, finalModuleId, sessionTypeEnum)
    }



    DisposableEffect(Unit) {

        onDispose {

            tts.stop()

            tts.shutdown()

        }

    }



    // ✅ MOSTRAR DIÁLOGO DE CARGA PARA EVALUACIÓN

    if (viewModel.isGeneratingEvaluationFeedback) {

        ICFESEvaluationLoadingDialog()

    }



    // ✅ MOSTRAR RESUMEN DE SIMULACRO SI ESTÁ DISPONIBLE
    viewModel.simulationResult?.let { result ->
        if (viewModel.showSimulationSummary) {
            ICFESSimulationSummaryDialog(
                result = result,
                onDismiss = { viewModel.closeSimulationSummary() },
                onRetry = { viewModel.retrySimulation() },
                onFinish = {
                    viewModel.closeSimulationSummary()
                    onQuizComplete()
                    navController.popBackStack()
                }
            )
            return
        }
    }

// ✅ MOSTRAR RESUMEN DE EVALUACIÓN SI ESTÁ DISPONIBLE (YA EXISTENTE)
    viewModel.evaluationResult?.let { result ->
        if (viewModel.showEvaluationSummary) {
            ICFESEvaluationSummaryDialog(
                viewModel = viewModel,
                result = result,
                onDismiss = { viewModel.closeEvaluationSummary() },
                onRetry = { viewModel.retryEvaluation() },
                onFinish = {
                    viewModel.closeEvaluationSummary()
                    onQuizComplete()
                    navController.popBackStack()
                }
            )
            return
        }
    }



    // ✅ MOSTRAR RESULTADOS BÁSICOS SI COMPLETÓ (SOLO PARA PRÁCTICA)

    if (viewModel.evaluationCompleted && !viewModel.isEvaluationMode) {

        ICFESResultsScreen(

            viewModel = viewModel,

            module = currentModule!!,

            sessionType = sessionTypeEnum,

            onFinish = {

                onQuizComplete()

                navController.popBackStack()

            },

            onRetry = { viewModel.resetQuiz() }

        )

        return

    }



    val currentQuestion = viewModel.getCurrentQuestion()

    val timeState by viewModel.timeState.collectAsState()



    if (currentQuestion == null) {

        // Pantalla de carga

        Box(

            modifier = Modifier.fillMaxSize(),

            contentAlignment = Alignment.Center

        ) {

            Column(

                horizontalAlignment = Alignment.CenterHorizontally

            ) {

                CircularProgressIndicator()

                Spacer(modifier = Modifier.height(16.dp))

                Text(

                    if (viewModel.isEvaluationMode)

                        "Preparando evaluación..."

                    else

                        "Cargando preguntas..."

                )

            }

        }

        return

    }



    // ✅ PANTALLA PRINCIPAL

    Scaffold(

        topBar = {

            ICFESQuizTopBar(
                module = currentModule ?: viewModel.currentModule, // ✅ USAR EL MÓDULO DEL VIEWMODEL SI ES NULL
                currentQuestion = viewModel.currentQuestionIndex + 1,
                totalQuestions = viewModel.currentQuestions.size,
                sessionType = sessionTypeEnum,
                timeRemaining = timeState,
                isEvaluationMode = viewModel.isEvaluationMode,
                onBackClick = { navController.popBackStack() }
            )
        }

    ) { padding ->

        Box(

            modifier = Modifier

                .fillMaxSize()

                .padding(padding)

        ) {

            LazyColumn(

                modifier = Modifier.fillMaxSize(),

                contentPadding = PaddingValues(

                    start = 16.dp,

                    end = 16.dp,

                    top = 16.dp,

                    bottom = if (viewModel.hasAnsweredCurrentQuestion) 180.dp else 16.dp

                ),

                verticalArrangement = Arrangement.spacedBy(16.dp)

            ) {

                item {

                    ICFESQuestionInfo(

                        question = currentQuestion,

                        isEvaluationMode = viewModel.isEvaluationMode

                    )

                }



                if (!currentQuestion.context.isNullOrEmpty()) {

                    item {

                        ICFESContextCard(context = currentQuestion.context)

                    }

                }



                item {

                    ICFESQuestionCard(question = currentQuestion)

                }



                item {

                    ICFESAnswerOptions(

                        question = currentQuestion,

                        viewModel = viewModel

                    )

                }



                // ✅ HINT SOLO EN PRÁCTICA

                if (currentQuestion.type == ICFESQuestionType.READING_COMPREHENSION && !viewModel.isEvaluationMode) {

                    item {

                        ICFESHintCard()

                    }

                }



                item {

                    ICFESSubmitButton(

                        viewModel = viewModel,

                        question = currentQuestion

                    )

                }



                if (viewModel.hasAnsweredCurrentQuestion) {

                    item {

                        Spacer(modifier = Modifier.height(120.dp))

                    }

                }

            }



            // ✅ BARRA INFERIOR - SOLO SI HA RESPONDIDO Y NO ES EVALUACIÓN CON FEEDBACK PENDIENTE

            if (viewModel.hasAnsweredCurrentQuestion &&

                (!viewModel.isEvaluationMode || !viewModel.evaluationCompleted)) {

                ICFESQuizBottomBar(

                    modifier = Modifier.align(Alignment.BottomCenter),

                    viewModel = viewModel,

                    currentQuestion = currentQuestion,

                    sessionType = sessionTypeEnum,

                    totalQuestions = viewModel.currentQuestions.size

                )

            }

        }

    }



    // ✅ DIÁLOGO DE FEEDBACK SOLO EN PRÁCTICA

    if (viewModel.showFeedback && !viewModel.isEvaluationMode) {

        ICFESFeedbackDialog(

            viewModel = viewModel,

            tts = tts,

            onDismiss = { viewModel.closeFeedback() }

        )

    }

}

// ✅ DIÁLOGO DE RESUMEN DEL SIMULACRO COMPLETO
@Composable
fun ICFESSimulationSummaryDialog(
    result: ICFESSimulationResult,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onFinish: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Simulacro Completo ICFES",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    "¡Felicidades por completar el simulacro!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .heightIn(max = 600.dp)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    // Puntaje global
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "${result.globalScore}",
                                    style = MaterialTheme.typography.displayLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Puntaje Global ICFES",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "${result.correctAnswers}/${result.totalQuestions}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Correctas",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "${"%.1f".format(result.percentage)}%",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Porcentaje",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            result.nivel,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = when (result.nivel) {
                                                "Superior" -> Color(0xFF4CAF50)
                                                "Alto" -> Color(0xFF2196F3)
                                                "Medio" -> Color(0xFFFF9800)
                                                else -> Color(0xFFF44336)
                                            }
                                        )
                                        Text(
                                            "Nivel",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                val hours = TimeUnit.MILLISECONDS.toHours(result.timeSpent)
                                val minutes = TimeUnit.MILLISECONDS.toMinutes(result.timeSpent) % 60
                                Text(
                                    "Tiempo: ${hours}h ${minutes}m",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Puntajes por módulo
                    item {
                        Text(
                            "Resultados por Módulo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(result.moduleScores.entries.toList()) { (moduleId, moduleScore) ->
                        ModuleScoreCard(moduleScore = moduleScore)
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nuevo Simulacro")
                }

                Button(
                    onClick = onFinish,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Finalizar")
                }
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(16.dp)
    )
}

// ✅ COMPONENTE PARA MOSTRAR PUNTAJE DE CADA MÓDULO
@Composable
fun ModuleScoreCard(moduleScore: ModuleSimulationScore) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    moduleScore.moduleName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${moduleScore.correctAnswers}/${moduleScore.totalQuestions} correctas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "${moduleScore.puntajeICFES}/500",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        moduleScore.puntajeICFES >= 350 -> Color(0xFF4CAF50)
                        moduleScore.puntajeICFES >= 250 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                )
                Text(
                    "${"%.1f".format(moduleScore.percentage)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


// ✅ BARRA SUPERIOR MODIFICADA

// ✅ REEMPLAZA ESTA FUNCIÓN EN ICFESQuizScreen.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ICFESQuizTopBar(
    module: ICFESModule?,
    currentQuestion: Int,
    totalQuestions: Int,
    sessionType: SessionType,
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
                        module?.name ?: "Simulacro Completo ICFES",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (isEvaluationMode) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            )
                        ) {
                            Text(
                                if (module == null) "SIMULACRO" else "EVALUACIÓN",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                Text(
                    "Pregunta $currentQuestion de $totalQuestions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
        },
        // ✅ ESTO ES LO QUE FALTABA - MOSTRAR EL CRONÓMETRO
        actions = {
            if (isEvaluationMode && timeRemaining > 0) {
                TimerChip(timeRemaining = timeRemaining)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (isEvaluationMode)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            else
                (module?.color ?: Color(0xFF9C27B0)).copy(alpha = 0.1f)
        )
    )
}


@Composable

fun TimerChip(timeRemaining: Long) {

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



// ✅ INFORMACIÓN DE PREGUNTA MODIFICADA

@Composable

fun ICFESQuestionInfo(

    question: ICFESQuestion,

    isEvaluationMode: Boolean = false

) {

    Card(

        modifier = Modifier.fillMaxWidth(),

        colors = CardDefaults.cardColors(

            containerColor = if (isEvaluationMode)

                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)

            else

                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

        )

    ) {

        Row(

            modifier = Modifier.padding(12.dp),

            horizontalArrangement = Arrangement.SpaceBetween,

            verticalAlignment = Alignment.CenterVertically

        ) {

            Row(

                verticalAlignment = Alignment.CenterVertically

            ) {

                Icon(

                    Icons.Default.Category,

                    contentDescription = null,

                    modifier = Modifier.size(16.dp),

                    tint = MaterialTheme.colorScheme.primary

                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(

                    question.competency,

                    style = MaterialTheme.typography.bodySmall,

                    fontWeight = FontWeight.Medium

                )

            }



            Row(

                verticalAlignment = Alignment.CenterVertically,

                horizontalArrangement = Arrangement.spacedBy(8.dp)

            ) {

                // ✅ INDICADOR DE EVALUACIÓN

                if (isEvaluationMode) {

                    Card(

                        colors = CardDefaults.cardColors(

                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)

                        )

                    ) {

                        Row(

                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),

                            verticalAlignment = Alignment.CenterVertically

                        ) {

                            Icon(

                                Icons.Default.Assignment,

                                contentDescription = null,

                                modifier = Modifier.size(12.dp),

                                tint = MaterialTheme.colorScheme.error

                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(

                                "EVAL",

                                style = MaterialTheme.typography.labelSmall,

                                fontWeight = FontWeight.Bold,

                                color = MaterialTheme.colorScheme.error

                            )

                        }

                    }

                }



                // Dificultad

                Card(

                    colors = CardDefaults.cardColors(

                        containerColor = when (question.difficulty) {

                            Difficulty.FACIL -> Color(0xFF4CAF50).copy(alpha = 0.1f)

                            Difficulty.MEDIO -> Color(0xFFFF9800).copy(alpha = 0.1f)

                            Difficulty.DIFICIL -> Color(0xFFF44336).copy(alpha = 0.1f)

                        }

                    )

                ) {

                    Text(

                        question.difficulty.displayName,

                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),

                        style = MaterialTheme.typography.bodySmall,

                        color = when (question.difficulty) {

                            Difficulty.FACIL -> Color(0xFF4CAF50)

                            Difficulty.MEDIO -> Color(0xFFFF9800)

                            Difficulty.DIFICIL -> Color(0xFFF44336)

                        },

                        fontWeight = FontWeight.Bold

                    )

                }

            }

        }

    }

}



@Composable

fun ICFESContextCard(context: String) {

    var isExpanded by remember { mutableStateOf(false) }

    val maxLines = if (isExpanded) Int.MAX_VALUE else 5



    Card(

        modifier = Modifier.fillMaxWidth(),

        colors = CardDefaults.cardColors(

            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)

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

                    tint = MaterialTheme.colorScheme.primary

                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(

                    "Texto base",

                    style = MaterialTheme.typography.titleSmall,

                    fontWeight = FontWeight.Bold

                )

                Spacer(modifier = Modifier.weight(1f))



                if (context.length > 300) {

                    Icon(

                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,

                        contentDescription = if (isExpanded) "Ver menos" else "Ver más",

                        modifier = Modifier.size(24.dp),

                        tint = MaterialTheme.colorScheme.primary

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

                    color = MaterialTheme.colorScheme.primary,

                    modifier = Modifier.padding(top = 4.dp)

                )

            }

        }

    }

}



@Composable

fun ICFESQuestionCard(question: ICFESQuestion) {

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

                question.question,

                style = MaterialTheme.typography.headlineSmall,

                fontWeight = FontWeight.Medium,

                color = MaterialTheme.colorScheme.onPrimaryContainer,

                lineHeight = MaterialTheme.typography.headlineSmall.lineHeight * 1.2

            )

        }

    }

}



@Composable

fun ICFESAnswerOptions(

    question: ICFESQuestion,

    viewModel: ICFESQuizViewModel

) {

    Card(

        modifier = Modifier.fillMaxWidth()

    ) {

        Column(

            modifier = Modifier.padding(16.dp)

        ) {

            Text(

                "Selecciona tu respuesta:",

                style = MaterialTheme.typography.titleMedium,

                fontWeight = FontWeight.Medium,

                modifier = Modifier.padding(bottom = 16.dp)

            )



            question.options.forEachIndexed { index, option ->

                val optionLetter = option.first().toString() // A, B, C, D



                Card(

                    modifier = Modifier

                        .fillMaxWidth()

                        .padding(vertical = 4.dp),

                    colors = CardDefaults.cardColors(

                        containerColor = if (viewModel.userAnswer.equals(optionLetter, ignoreCase = true))

                            MaterialTheme.colorScheme.secondaryContainer

                        else

                            MaterialTheme.colorScheme.surface

                    ),

                    onClick = {

                        if (!viewModel.hasAnsweredCurrentQuestion) {

                            viewModel.userAnswer = optionLetter

                        }

                    }

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

                            enabled = !viewModel.hasAnsweredCurrentQuestion

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



// ✅ HINT CARD MODIFICADA

@Composable

fun ICFESHintCard() {

    Card(

        modifier = Modifier.fillMaxWidth(),

        colors = CardDefaults.cardColors(

            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)

        )

    ) {

        Row(

            modifier = Modifier.padding(12.dp),

            verticalAlignment = Alignment.CenterVertically

        ) {

            Icon(

                Icons.Default.Psychology,

                contentDescription = null,

                tint = MaterialTheme.colorScheme.primary,

                modifier = Modifier.size(20.dp)

            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(

                "En modo práctica recibes feedback inmediato con IA especializada en ICFES",

                style = MaterialTheme.typography.bodySmall,

                color = MaterialTheme.colorScheme.onTertiaryContainer

            )

        }

    }

}



// ✅ BOTÓN DE ENVIAR MODIFICADO

@Composable

fun ICFESSubmitButton(

    viewModel: ICFESQuizViewModel,

    question: ICFESQuestion

) {

    AnimatedVisibility(

        visible = !viewModel.hasAnsweredCurrentQuestion,

        enter = slideInVertically() + fadeIn(),

        exit = slideOutVertically() + fadeOut()

    ) {

        Button(

            onClick = { viewModel.submitAnswer(question) },

            enabled = viewModel.userAnswer.isNotEmpty() && !viewModel.isValidatingWithAI,

            modifier = Modifier.fillMaxWidth(),

            shape = RoundedCornerShape(12.dp),

            colors = ButtonDefaults.buttonColors(

                containerColor = if (viewModel.isEvaluationMode)

                    MaterialTheme.colorScheme.error

                else

                    MaterialTheme.colorScheme.primary

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

                        Icons.Default.SmartToy,

                        contentDescription = null,

                        tint = MaterialTheme.colorScheme.onPrimary,

                        modifier = Modifier.size(18.dp)

                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(

                        "Generando feedback ICFES...",

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

                        if (viewModel.isEvaluationMode) "Confirmar Respuesta" else "Enviar Respuesta",

                        style = MaterialTheme.typography.bodyMedium,

                        fontWeight = FontWeight.Medium

                    )

                }

            }

        }

    }

}



// ✅ BARRA INFERIOR MODIFICADA

@Composable

fun ICFESQuizBottomBar(

    modifier: Modifier = Modifier,

    viewModel: ICFESQuizViewModel,

    currentQuestion: ICFESQuestion,

    sessionType: SessionType,

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

            // ✅ INFORMACIÓN DIFERENCIADA POR MODO

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceBetween,

                verticalAlignment = Alignment.CenterVertically

            ) {

                Text(

                    "Progreso: ${viewModel.currentQuestionIndex + 1}/$totalQuestions",

                    style = MaterialTheme.typography.bodySmall,

                    color = MaterialTheme.colorScheme.onSurfaceVariant

                )



                if (viewModel.isEvaluationMode) {

                    Text(

                        "${viewModel.currentQuestionIndex + 1} completadas",

                        style = MaterialTheme.typography.bodySmall,

                        fontWeight = FontWeight.Bold,

                        color = MaterialTheme.colorScheme.error

                    )

                } else {

                    Text(

                        "Puntaje: ${viewModel.score}",

                        style = MaterialTheme.typography.bodySmall,

                        fontWeight = FontWeight.Bold,

                        color = MaterialTheme.colorScheme.primary

                    )

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

                    MaterialTheme.colorScheme.primary

            )



            Spacer(modifier = Modifier.height(20.dp))



            // ✅ BOTONES CON TEXTO DIFERENCIADO

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.spacedBy(16.dp)

            ) {

                OutlinedButton(

                    onClick = { viewModel.previousQuestion() },

                    enabled = viewModel.currentQuestionIndex > 0,

                    modifier = Modifier.weight(1f),

                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp)

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

                            MaterialTheme.colorScheme.primary

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



            // ✅ ADVERTENCIA EN EVALUACIÓN

            if (viewModel.isEvaluationMode) {

                Spacer(modifier = Modifier.height(8.dp))

                Text(

                    "⚠️ Modo evaluación: Sin feedback hasta el final",

                    style = MaterialTheme.typography.bodySmall,

                    color = MaterialTheme.colorScheme.error,

                    textAlign = TextAlign.Center,

                    modifier = Modifier.fillMaxWidth()

                )

            }

        }

    }

}



@Composable
fun ICFESFeedbackDialog(
    viewModel: ICFESQuizViewModel,
    tts: TextToSpeech,
    onDismiss: () -> Unit
) {
    // ✅ ESTADO PARA CONTROLAR LA REPRODUCCIÓN DE AUDIO
    var isPlayingAudio by remember { mutableStateOf(false) }

    // ✅ DETENER AUDIO AL CERRAR EL DIÁLOGO
    DisposableEffect(Unit) {
        onDispose {
            tts.stop() // Detener inmediatamente al cerrar
            isPlayingAudio = false
        }
    }

    AlertDialog(
        onDismissRequest = {
            tts.stop() // ✅ DETENER AUDIO AL CERRAR
            isPlayingAudio = false
            onDismiss()
        },

        // ✅ ÍCONO EN LA PARTE SUPERIOR
        icon = {
            Icon(
                if (viewModel.isAnswerCorrect) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (viewModel.isAnswerCorrect) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.size(48.dp)
            )
        },

        // ✅ TÍTULO CON IA INDICATOR
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
                    Icons.Default.SmartToy,
                    contentDescription = "Feedback IA",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        },

        // ✅ CONTENIDO SCROLLEABLE
        text = {
            Box(
                modifier = Modifier
                    .heightIn(max = 400.dp) // Altura máxima para scroll
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState()) // ✅ SCROLL SOLO EN EL CONTENIDO
                        .padding(vertical = 8.dp)
                ) {
                    // ✅ FEEDBACK PRINCIPAL
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                                    "Análisis ICFES:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
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
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    "Estrategia ICFES:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    viewModel.feedbackTip,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        },

        // ✅ BOTONES - ÁREA FIJA (NO SCROLLEABLE)
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ✅ BOTÓN DE AUDIO - SIEMPRE VISIBLE, NO HACE SCROLL
                OutlinedButton(
                    onClick = {
                        if (isPlayingAudio) {
                            // ✅ DETENER AUDIO SI ESTÁ REPRODUCIENDO
                            tts.stop()
                            isPlayingAudio = false
                        } else {
                            // ✅ REPRODUCIR AUDIO
                            val cleanText = viewModel.cleanICFESFeedbackForTTS(
                                viewModel.feedbackMessage,
                                viewModel.feedbackTip
                            )

                            // ✅ CONFIGURAR TTS
                            tts.setSpeechRate(0.8f)
                            tts.setPitch(1.0f)

                            // ✅ LISTENER PARA CONTROLAR ESTADO
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

                            val utteranceId = "feedback_${System.currentTimeMillis()}"
                            val params = Bundle().apply {
                                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
                            }
                            tts.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isPlayingAudio) Color(0xFFF44336) else MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(
                        2.dp,
                        if (isPlayingAudio) Color(0xFFF44336) else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            if (isPlayingAudio) Icons.Default.Stop else Icons.Default.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                if (isPlayingAudio) "🔴 Detener Audio" else "🔊 Escuchar Análisis",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            if (isPlayingAudio) {
                                Text(
                                    "Reproduciendo...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFF44336).copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // ✅ INDICADOR VISUAL DE REPRODUCCIÓN
                        if (isPlayingAudio) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFFF44336)
                            )
                        } else {
                            // Espacio para mantener alineación
                            Spacer(modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // ✅ BOTÓN CONTINUAR - DESTACADO Y PRINCIPAL
                Button(
                    onClick = {
                        tts.stop() // ✅ DETENER AUDIO AL CONTINUAR
                        isPlayingAudio = false
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp), // Botón más alto para destacar
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.isAnswerCorrect)
                            Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
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
        },

        // ✅ PROPIEDADES DEL DIÁLOGO
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true, // ✅ PERMITIR CERRAR CON BACK
            dismissOnClickOutside = false // ✅ NO CERRAR TOCANDO AFUERA ACCIDENTALMENTE
        ),
        modifier = Modifier
            .fillMaxWidth(0.92f) // Ligeramente más ancho para el botón de audio
            .padding(16.dp)
    )
}



@Composable

fun ICFESResultsScreen(

    viewModel: ICFESQuizViewModel,

    module: ICFESModule,

    sessionType: SessionType,

    onFinish: () -> Unit,

    onRetry: () -> Unit

) {

    LazyColumn(

        modifier = Modifier.fillMaxSize(),

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

                        Icons.Default.EmojiEvents,

                        contentDescription = null,

                        modifier = Modifier.size(64.dp),

                        tint = MaterialTheme.colorScheme.primary

                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(

                        "Evaluación Completada",

                        style = MaterialTheme.typography.headlineMedium,

                        fontWeight = FontWeight.Bold,

                        textAlign = TextAlign.Center

                    )

                    Text(

                        "Has terminado el módulo de ${module.name}",

                        style = MaterialTheme.typography.bodyMedium,

                        textAlign = TextAlign.Center,

                        color = MaterialTheme.colorScheme.onPrimaryContainer

                    )



                    Spacer(modifier = Modifier.height(24.dp))



                    // Puntaje

                    Text(

                        "${viewModel.score}",

                        style = MaterialTheme.typography.displayLarge,

                        fontWeight = FontWeight.Bold,

                        color = MaterialTheme.colorScheme.primary

                    )

                    Text(

                        "Puntos obtenidos",

                        style = MaterialTheme.typography.bodyMedium

                    )

                }

            }

        }



        item {

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.spacedBy(8.dp)

            ) {

                OutlinedButton(

                    onClick = onRetry,

                    modifier = Modifier.weight(1f)

                ) {

                    Text("Reintentar")

                }



                Button(

                    onClick = onFinish,

                    modifier = Modifier.weight(1f)

                ) {

                    Text("Finalizar")

                }

            }

        }

    }

}



// ✅ PANTALLA DE CARGA PARA GENERACIÓN DE FEEDBACK

@Composable

fun ICFESEvaluationLoadingDialog() {

    AlertDialog(

        onDismissRequest = { /* No permitir cerrar mientras se genera */ },

        icon = {

            CircularProgressIndicator(

                modifier = Modifier.size(48.dp),

                strokeWidth = 4.dp

            )

        },

        title = {

            Text(

                "Generando Análisis ICFES",

                textAlign = TextAlign.Center,

                modifier = Modifier.fillMaxWidth()

            )

        },

        text = {

            Column(

                horizontalAlignment = Alignment.CenterHorizontally,

                modifier = Modifier.fillMaxWidth()

            ) {

                Row(

                    verticalAlignment = Alignment.CenterVertically,

                    horizontalArrangement = Arrangement.Center

                ) {

                    Icon(

                        Icons.Default.SmartToy,

                        contentDescription = null,

                        tint = MaterialTheme.colorScheme.primary,

                        modifier = Modifier.size(24.dp)

                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(

                        "La IA está analizando tu evaluación...",

                        style = MaterialTheme.typography.bodyMedium,

                        textAlign = TextAlign.Center

                    )

                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(

                    "Esto puede tomar unos segundos",

                    style = MaterialTheme.typography.bodySmall,

                    color = MaterialTheme.colorScheme.onSurfaceVariant,

                    textAlign = TextAlign.Center

                )

            }

        },

        confirmButton = { /* Vacío para no mostrar botón */ },

        properties = DialogProperties(

            dismissOnBackPress = false,

            dismissOnClickOutside = false

        )

    )

}



// ✅ DIÁLOGO DE RESUMEN DE EVALUACIÓN

@Composable
fun ICFESEvaluationSummaryDialog(
    viewModel: ICFESQuizViewModel,
    result: ICFESEvaluationResult,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onFinish: () -> Unit
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
            tts.stop() // Detener inmediatamente al cerrar
            tts.shutdown()
            isPlayingAudio = false
        }
    }

    AlertDialog(
        onDismissRequest = {
            tts.stop() // ✅ DETENER AUDIO AL CERRAR
            isPlayingAudio = false
            onDismiss()
        },

        // ✅ ÍCONO DE EVALUACIÓN
        icon = {
            Icon(
                Icons.Default.Assessment,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },

        // ✅ TÍTULO DE EVALUACIÓN
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Evaluación Completada",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    result.moduleName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },

        // ✅ CONTENIDO SCROLLEABLE CON ALTURA LIMITADA
        text = {
            Box(
                modifier = Modifier
                    .heightIn(max = 400.dp) // ✅ REDUCIR ALTURA MÁXIMA para que botones sean visibles
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp), // ✅ Menos espaciado
                    modifier = Modifier.padding(vertical = 6.dp),
                    contentPadding = PaddingValues(bottom = 8.dp) // ✅ Padding al final
                ) {
                    // ✅ PUNTAJE PRINCIPAL
                    item {
                        ICFESScoreCard(result = result)
                    }

                    // ✅ ANÁLISIS GENERAL
                    item {
                        ICFESAnalysisCard(
                            title = "Análisis General",
                            content = result.analisisGeneral,
                            icon = Icons.Default.Psychology
                        )
                    }

                    // ✅ FORTALEZAS
                    if (result.fortalezas.isNotEmpty()) {
                        item {
                            ICFESStrengthsCard(fortalezas = result.fortalezas)
                        }
                    }

                    // ✅ ÁREAS DE MEJORA
                    if (result.debilidades.isNotEmpty()) {
                        item {
                            ICFESWeaknessesCard(debilidades = result.debilidades)
                        }
                    }

                    // ✅ RECOMENDACIONES
                    if (result.recomendaciones.isNotEmpty()) {
                        item {
                            ICFESRecommendationsCard(recomendaciones = result.recomendaciones)
                        }
                    }

                    // ✅ ESTRATEGIAS DE MEJORA
                    if (result.estrategias.isNotEmpty()) {
                        item {
                            ICFESStrategiesCard(estrategias = result.estrategias)
                        }
                    }
                }
            }
        },

        // ✅ BOTONES - ÁREA FIJA Y MUY VISIBLE
        confirmButton = {
            // ✅ CONTENEDOR CON FONDO PARA DESTACAR LOS BOTONES
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp), // ✅ Más padding para destacar
                    verticalArrangement = Arrangement.spacedBy(16.dp) // ✅ Más espaciado
                ) {

                    // ✅ SEPARADOR VISUAL (si HorizontalDivider no está disponible, usa Divider)
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    // ✅ ALTERNATIVA: Divider() si HorizontalDivider no está disponible

                    // ✅ BOTÓN DE AUDIO - MÁS COMPACTO PERO DESTACADO
                    ElevatedButton(
                        onClick = {
                            if (isPlayingAudio) {
                                // ✅ DETENER AUDIO SI ESTÁ REPRODUCIENDO
                                tts.stop()
                                isPlayingAudio = false
                            } else {
                                // ✅ REPRODUCIR AUDIO DE EVALUACIÓN
                                val feedbackText = buildString {
                                    append("Análisis de tu evaluación de ${result.moduleName}. ")
                                    append("Obtuviste ${result.puntajeICFES} puntos, ")
                                    append("con ${result.correctAnswers} respuestas correctas de ${result.totalQuestions}. ")
                                    append("Tu nivel es ${result.nivel}. ")
                                    append(result.analisisGeneral)

                                    if (result.fortalezas.isNotEmpty()) {
                                        append(" Tus fortalezas incluyen: ")
                                        append(result.fortalezas.joinToString(", "))
                                    }

                                    if (result.recomendaciones.isNotEmpty()) {
                                        append(" Te recomendamos: ")
                                        append(result.recomendaciones.take(3).joinToString(", "))
                                    }
                                }

                                val cleanText = viewModel.cleanTextForTTS(feedbackText)

                                // ✅ CONFIGURAR TTS
                                tts.setSpeechRate(0.8f)
                                tts.setPitch(1.0f)

                                // ✅ LISTENER PARA CONTROLAR ESTADO
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

                                val utteranceId = "evaluation_${System.currentTimeMillis()}"
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
                                MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (isPlayingAudio)
                                Color(0xFFF44336)
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(
                            defaultElevation = if (isPlayingAudio) 0.dp else 4.dp
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
                                if (isPlayingAudio) "🔴 Detener Audio" else "🔊 Escuchar Análisis ",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            // ✅ INDICADOR VISUAL DE REPRODUCCIÓN
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

                    // ✅ BOTONES DE ACCIÓN - MEJOR ESPACIADO
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Botón Reintentar
                        OutlinedButton(
                            onClick = {
                                tts.stop() // ✅ DETENER AUDIO AL REINTENTAR
                                isPlayingAudio = false
                                onRetry()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp), // ✅ Altura ajustada
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp) // ✅ Padding personalizado
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp) // ✅ Ícono más pequeño
                                )
                                Spacer(modifier = Modifier.width(8.dp)) // ✅ Más espacio
                                Text(
                                    "Reintentar",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Botón Finalizar - Principal y destacado
                        Button(
                            onClick = {
                                tts.stop() // ✅ DETENER AUDIO AL FINALIZAR
                                isPlayingAudio = false
                                onFinish()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp), // ✅ Altura ajustada
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp // ✅ Elevación reducida
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp) // ✅ Padding personalizado
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp) // ✅ Ícono más pequeño
                                )
                                Spacer(modifier = Modifier.width(8.dp)) // ✅ Más espacio
                                Text(
                                    "Finalizar",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },

        // ✅ PROPIEDADES DEL DIÁLOGO
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true, // ✅ PERMITIR CERRAR CON BACK
            dismissOnClickOutside = false // ✅ NO CERRAR TOCANDO AFUERA ACCIDENTALMENTE
        ),
        modifier = Modifier
            .fillMaxWidth(0.92f) // Ligeramente menos ancho para mejor proporción
            .padding(16.dp)
    )
}






// ✅ COMPONENTES PARA EL RESUMEN DE EVALUACIÓN

@Composable

fun ICFESScoreCard(result: ICFESEvaluationResult) {

    Card(

        colors = CardDefaults.cardColors(

            containerColor = MaterialTheme.colorScheme.primaryContainer

        ),

        modifier = Modifier.fillMaxWidth()

    ) {

        Column(

            modifier = Modifier.padding(20.dp),

            horizontalAlignment = Alignment.CenterHorizontally

        ) {

            Text(

                "${result.puntajeICFES}",

                style = MaterialTheme.typography.displayLarge,

                fontWeight = FontWeight.Bold,

                color = MaterialTheme.colorScheme.primary

            )

            Text(

                "Puntaje ICFES",

                style = MaterialTheme.typography.bodyMedium,

                color = MaterialTheme.colorScheme.onPrimaryContainer

            )



            Spacer(modifier = Modifier.height(16.dp))



            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceEvenly

            ) {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Text(

                        "${result.correctAnswers}/${result.totalQuestions}",

                        style = MaterialTheme.typography.titleMedium,

                        fontWeight = FontWeight.Bold

                    )

                    Text(

                        "Correctas",

                        style = MaterialTheme.typography.bodySmall,

                        color = MaterialTheme.colorScheme.onPrimaryContainer

                    )

                }



                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Text(

                        "${"%.1f".format(result.percentage)}%",

                        style = MaterialTheme.typography.titleMedium,

                        fontWeight = FontWeight.Bold

                    )

                    Text(

                        "Porcentaje",

                        style = MaterialTheme.typography.bodySmall,

                        color = MaterialTheme.colorScheme.onPrimaryContainer

                    )

                }



                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Text(

                        result.nivel,

                        style = MaterialTheme.typography.titleMedium,

                        fontWeight = FontWeight.Bold,

                        color = when (result.nivel) {

                            "Alto" -> Color(0xFF4CAF50)

                            "Medio" -> Color(0xFFFF9800)

                            else -> Color(0xFFF44336)

                        }

                    )

                    Text(

                        "Nivel",

                        style = MaterialTheme.typography.bodySmall,

                        color = MaterialTheme.colorScheme.onPrimaryContainer

                    )

                }

            }



            Spacer(modifier = Modifier.height(12.dp))



            val minutes = TimeUnit.MILLISECONDS.toMinutes(result.timeSpent)

            val seconds = TimeUnit.MILLISECONDS.toSeconds(result.timeSpent) % 60

            Text(

                "Tiempo: ${minutes}m ${seconds}s",

                style = MaterialTheme.typography.bodySmall,

                color = MaterialTheme.colorScheme.onPrimaryContainer

            )

        }

    }

}



@Composable

fun ICFESAnalysisCard(

    title: String,

    content: String,

    icon: androidx.compose.ui.graphics.vector.ImageVector

) {

    Card(

        colors = CardDefaults.cardColors(

            containerColor = MaterialTheme.colorScheme.surfaceVariant

        ),

        modifier = Modifier.fillMaxWidth()

    ) {

        Column(

            modifier = Modifier.padding(16.dp)

        ) {

            Row(

                verticalAlignment = Alignment.CenterVertically,

                modifier = Modifier.padding(bottom = 8.dp)

            ) {

                Icon(

                    icon,

                    contentDescription = null,

                    tint = MaterialTheme.colorScheme.primary,

                    modifier = Modifier.size(20.dp)

                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(

                    title,

                    style = MaterialTheme.typography.titleMedium,

                    fontWeight = FontWeight.Bold

                )

            }

            Text(

                content,

                style = MaterialTheme.typography.bodyMedium,

                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2

            )

        }

    }

}



@Composable

fun ICFESStrengthsCard(fortalezas: List<String>) {

    Card(

        colors = CardDefaults.cardColors(

            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)

        ),

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

                    Icons.Default.CheckCircle,

                    contentDescription = null,

                    tint = Color(0xFF4CAF50),

                    modifier = Modifier.size(20.dp)

                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(

                    "Fortalezas Identificadas",

                    style = MaterialTheme.typography.titleMedium,

                    fontWeight = FontWeight.Bold,

                    color = Color(0xFF4CAF50)

                )

            }



            fortalezas.forEach { fortaleza ->

                Row(

                    verticalAlignment = Alignment.Top,

                    modifier = Modifier.padding(vertical = 2.dp)

                ) {

                    Icon(

                        Icons.Default.Star,

                        contentDescription = null,

                        tint = Color(0xFF4CAF50),

                        modifier = Modifier.size(16.dp).padding(top = 2.dp)

                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(

                        fortaleza,

                        style = MaterialTheme.typography.bodyMedium,

                        modifier = Modifier.weight(1f)

                    )

                }

            }

        }

    }

}



@Composable

fun ICFESWeaknessesCard(debilidades: List<String>) {

    Card(

        colors = CardDefaults.cardColors(

            containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)

        ),

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

                    Icons.Default.TrendingUp,

                    contentDescription = null,

                    tint = Color(0xFFFF9800),

                    modifier = Modifier.size(20.dp)

                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(

                    "Áreas de Mejora",

                    style = MaterialTheme.typography.titleMedium,

                    fontWeight = FontWeight.Bold,

                    color = Color(0xFFFF9800)

                )

            }



            debilidades.forEach { debilidad ->

                Row(

                    verticalAlignment = Alignment.Top,

                    modifier = Modifier.padding(vertical = 2.dp)

                ) {

                    Icon(

                        Icons.Default.Flag,

                        contentDescription = null,

                        tint = Color(0xFFFF9800),

                        modifier = Modifier.size(16.dp).padding(top = 2.dp)

                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(

                        debilidad,

                        style = MaterialTheme.typography.bodyMedium,

                        modifier = Modifier.weight(1f)

                    )

                }

            }

        }

    }

}



@Composable

fun ICFESRecommendationsCard(recomendaciones: List<String>) {

    Card(

        colors = CardDefaults.cardColors(

            containerColor = MaterialTheme.colorScheme.secondaryContainer

        ),

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

                    Icons.Default.Lightbulb,

                    contentDescription = null,

                    tint = MaterialTheme.colorScheme.secondary,

                    modifier = Modifier.size(20.dp)

                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(

                    "Recomendaciones de Estudio",

                    style = MaterialTheme.typography.titleMedium,

                    fontWeight = FontWeight.Bold,

                    color = MaterialTheme.colorScheme.secondary

                )

            }



            recomendaciones.forEachIndexed { index, recomendacion ->

                Row(

                    verticalAlignment = Alignment.Top,

                    modifier = Modifier.padding(vertical = 2.dp)

                ) {

                    Text(

                        "${index + 1}.",

                        style = MaterialTheme.typography.bodyMedium,

                        fontWeight = FontWeight.Bold,

                        color = MaterialTheme.colorScheme.secondary,

                        modifier = Modifier.padding(top = 2.dp)

                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(

                        recomendacion,

                        style = MaterialTheme.typography.bodyMedium,

                        modifier = Modifier.weight(1f)

                    )

                }

            }

        }

    }

}



@Composable

fun ICFESStrategiesCard(estrategias: List<String>) {

    Card(

        colors = CardDefaults.cardColors(

            containerColor = MaterialTheme.colorScheme.tertiaryContainer

        ),

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

                    Icons.Default.Psychology,

                    contentDescription = null,

                    tint = MaterialTheme.colorScheme.tertiary,

                    modifier = Modifier.size(20.dp)

                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(

                    "Estrategias para el Examen",

                    style = MaterialTheme.typography.titleMedium,

                    fontWeight = FontWeight.Bold,

                    color = MaterialTheme.colorScheme.tertiary

                )

            }



            estrategias.forEach { estrategia ->

                Row(

                    verticalAlignment = Alignment.Top,

                    modifier = Modifier.padding(vertical = 2.dp)

                ) {

                    Icon(

                        Icons.Default.PlayArrow,

                        contentDescription = null,

                        tint = MaterialTheme.colorScheme.tertiary,

                        modifier = Modifier.size(16.dp).padding(top = 2.dp)

                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(

                        estrategia,

                        style = MaterialTheme.typography.bodyMedium,

                        modifier = Modifier.weight(1f)

                    )

                }

            }

        }

    }

}



@Composable

fun ICFESAudioButton(

    result: ICFESEvaluationResult,

    tts: TextToSpeech,

    viewModel: ICFESQuizViewModel

) {

    OutlinedButton(

        onClick = {

            val feedbackText = buildString {

                append("Análisis de tu evaluación de ${result.moduleName}. ")

                append("Obtuviste ${result.puntajeICFES} puntos, ")

                append("con ${result.correctAnswers} respuestas correctas de ${result.totalQuestions}. ")

                append("Tu nivel es ${result.nivel}. ")

                append(result.analisisGeneral)



                if (result.fortalezas.isNotEmpty()) {

                    append(" Tus fortalezas incluyen: ")

                    append(result.fortalezas.joinToString(", "))

                }



                if (result.recomendaciones.isNotEmpty()) {

                    append(" Te recomendamos: ")

                    append(result.recomendaciones.take(3).joinToString(", "))

                }

            }



            val cleanText = viewModel.cleanTextForTTS(feedbackText)

            tts.setSpeechRate(0.8f)

            tts.setPitch(1.0f)

            tts.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, null)

        },

        modifier = Modifier.fillMaxWidth()

    ) {

        Icon(Icons.Default.VolumeUp, contentDescription = null)

        Spacer(modifier = Modifier.width(8.dp))

        Text("Escuchar Análisis Completo")

    }

}