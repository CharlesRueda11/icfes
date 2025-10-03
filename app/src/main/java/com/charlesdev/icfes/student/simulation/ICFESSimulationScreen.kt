package com.charlesdev.icfes.student.simulation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.charlesdev.icfes.student.data.*
import com.charlesdev.icfes.student.viewmodel.ICFESSimulationViewModel
import java.util.concurrent.TimeUnit

/**
 * ===================================
 * 📁 PANTALLA PRINCIPAL DEL SIMULACRO COMPLETO ICFES
 * ===================================
 * Experiencia completa: Instrucciones → 5 Sesiones → Breaks → Resultados finales
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ICFESSimulationScreen(
    navController: NavHostController,
    onSimulationComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: ICFESSimulationViewModel = viewModel()

    // Estados del flujo
    val sessionTimeRemaining by viewModel.sessionTimeRemaining.collectAsState()
    val breakTimeRemaining by viewModel.breakTimeRemaining.collectAsState()
    val totalTimeSpent by viewModel.totalTimeSpent.collectAsState()

    // Inicializar simulacro
    LaunchedEffect(Unit) {
        viewModel.initializeSimulation(context)
    }

    // ✅ MOSTRAR PANTALLA SEGÚN EL ESTADO ACTUAL
    when {
        // 1. Pantalla de instrucciones
        viewModel.showInstructions -> {
            ICFESInstructionsScreen(
                simulation = viewModel.currentSimulation,
                onStart = { viewModel.startSimulation() },
                onExit = { navController.navigateUp() }
            )
        }

        // 2. Pantalla de break entre sesiones
        viewModel.showBreakScreen -> {
            ICFESBreakScreen(
                currentSession = viewModel.simulationState.currentSession,
                totalSessions = viewModel.currentSimulation?.sessions?.size ?: 5,
                breakTimeRemaining = breakTimeRemaining,
                onFinishBreak = { viewModel.finishBreak() },
                onPause = { viewModel.pauseSimulation() }
            )
        }

        // 3. Resultados finales
        viewModel.showFinalResults -> {
            ICFESSimulationResultsScreen(
                results = viewModel.finalResults,
                isGenerating = viewModel.isGeneratingResults,
                onRetry = { viewModel.resetSimulation() },
                onFinish = {
                    onSimulationComplete()
                    navController.navigateUp()
                }
            )
        }

        // 4. Sesión activa del simulacro
        else -> {
            ICFESActiveSessionScreen(
                viewModel = viewModel,
                sessionTimeRemaining = sessionTimeRemaining,
                totalTimeSpent = totalTimeSpent,
                onExit = { navController.navigateUp() }
            )
        }
    }

    // ✅ MANEJO DE ERRORES
    viewModel.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Mostrar snackbar o dialog de error
        }
    }
}

// ✅ PANTALLA DE INSTRUCCIONES
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ICFESInstructionsScreen(
    simulation: ICFESSimulation?,
    onStart: () -> Unit,
    onExit: () -> Unit
) {
    val instructions = simulation?.instructions ?: SimulationInstructions.getDefaultInstructions()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simulacro Completo ICFES", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Salir")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF9C27B0).copy(alpha = 0.1f)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header principal
            item {
                ICFESSimulationHeader()
            }

            // Estructura del simulacro
            item {
                ICFESSimulationStructure(simulation?.sessions ?: emptyList())
            }

            // Instrucciones generales
            item {
                ICFESInstructionSection(
                    title = "📋 Instrucciones Generales",
                    instructions = instructions.generalInstructions,
                    color = Color(0xFF2196F3)
                )
            }

            // Instrucciones de tiempo
            item {
                ICFESInstructionSection(
                    title = "⏰ Manejo del Tiempo",
                    instructions = instructions.timingInstructions,
                    color = Color(0xFFFF9800)
                )
            }

            // Instrucciones técnicas
            item {
                ICFESInstructionSection(
                    title = "💻 Aspectos Técnicos",
                    instructions = instructions.technicalInstructions,
                    color = Color(0xFF4CAF50)
                )
            }

            // Recomendaciones de comportamiento
            item {
                ICFESInstructionSection(
                    title = "🎯 Recomendaciones",
                    instructions = instructions.behaviorInstructions,
                    color = Color(0xFF9C27B0)
                )
            }

            // Botones de acción
            item {
                ICFESStartButtons(
                    onStart = onStart,
                    onExit = onExit
                )
            }
        }
    }
}

// ✅ HEADER DEL SIMULACRO
@Composable
fun ICFESSimulationHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF9C27B0).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Assignment,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF9C27B0)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "🎯 Simulacro Completo ICFES Saber 11",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF9C27B0)
            )

            Text(
                "Experiencia 100% real del examen oficial",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SimulationStatCard("175", "Preguntas", "📝")
                SimulationStatCard("4.5", "Horas", "⏰")
                SimulationStatCard("5", "Sesiones", "📚")
            }
        }
    }
}

@Composable
fun SimulationStatCard(value: String, label: String, icon: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            icon,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF9C27B0)
        )
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ✅ ESTRUCTURA DEL SIMULACRO
@Composable
fun ICFESSimulationStructure(sessions: List<SimulationSession>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "📊 Estructura del Simulacro",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            sessions.forEachIndexed { index, session ->
                SimulationSessionCard(
                    sessionNumber = index + 1,
                    session = session,
                    isLast = index == sessions.size - 1
                )
            }
        }
    }
}

@Composable
fun SimulationSessionCard(
    sessionNumber: Int,
    session: SimulationSession,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indicador de sesión
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(session.color).copy(alpha = 0.2f)
            ),
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$sessionNumber",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(session.color)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Información de la sesión
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                "${session.icon} ${session.moduleName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "${session.questions.size} preguntas • ${session.duration / (60 * 1000)} minutos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Tiempo
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                "${session.duration / (60 * 1000)} min",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(session.color)
            )
        }
    }

    // Break indicator (except for last session)
    if (!isLast) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    "☕ Break: 15 minutos",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ✅ SECCIÓN DE INSTRUCCIONES
@Composable
fun ICFESInstructionSection(
    title: String,
    instructions: List<String>,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            instructions.forEach { instruction ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = color,
                        modifier = Modifier.padding(end = 8.dp, top = 2.dp)
                    )
                    Text(
                        instruction,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                    )
                }
            }
        }
    }
}

// ✅ BOTONES DE INICIO
@Composable
fun ICFESStartButtons(
    onStart: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Botón principal
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9C27B0)
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
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "🚀 Comenzar Simulacro",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Una vez iniciado, no podrás pausar las sesiones",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Advertencia importante
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "⚠️ Asegúrate de tener 4.5 horas disponibles y un ambiente sin distracciones antes de comenzar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFF9800),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Botón secundario
        OutlinedButton(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Volver al Menú Principal")
        }
    }
}

// ✅ SESIÓN ACTIVA DEL SIMULACRO
@Composable
fun ICFESActiveSessionScreen(
    viewModel: ICFESSimulationViewModel,
    sessionTimeRemaining: Long,
    totalTimeSpent: Long,
    onExit: () -> Unit
) {
    val currentSession = viewModel.getCurrentSession()
    val currentQuestion = viewModel.currentQuestion

    Scaffold(
        topBar = {
            ICFESSessionTopBar(
                session = currentSession,
                sessionNumber = viewModel.simulationState.currentSession + 1,
                totalSessions = viewModel.currentSimulation?.sessions?.size ?: 5,
                currentQuestion = viewModel.currentQuestionIndex + 1,
                totalQuestions = currentSession?.questions?.size ?: 0,
                timeRemaining = sessionTimeRemaining,
                totalTimeSpent = totalTimeSpent,
                onExit = onExit
            )
        }
    ) { padding ->
        if (currentQuestion != null && currentSession != null) {
            ICFESQuestionContent(
                viewModel = viewModel,
                question = currentQuestion,
                session = currentSession,
                modifier = Modifier.padding(padding)
            )
        } else {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cargando pregunta...")
                }
            }
        }
    }
}

// ✅ BARRA SUPERIOR DE LA SESIÓN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ICFESSessionTopBar(
    session: SimulationSession?,
    sessionNumber: Int,
    totalSessions: Int,
    currentQuestion: Int,
    totalQuestions: Int,
    timeRemaining: Long,
    totalTimeSpent: Long,
    onExit: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    "${session?.icon ?: "📝"} ${session?.moduleName ?: "Simulacro ICFES"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Sesión $sessionNumber/$totalSessions • Pregunta $currentQuestion/$totalQuestions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onExit) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Salir")
            }
        },
        actions = {
            // Timer de la sesión
            ICFESTimerChip(
                timeRemaining = timeRemaining,
                label = "Sesión",
                isWarning = timeRemaining < 5 * 60 * 1000L // 5 minutos
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Tiempo total gastado
            ICFESTimerChip(
                timeRemaining = totalTimeSpent,
                label = "Total",
                isElapsed = true
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = session?.let { Color(it.color).copy(alpha = 0.1f) }
                ?: MaterialTheme.colorScheme.surface
        )
    )
}

// ✅ CHIP DE TIMER
@Composable
fun ICFESTimerChip(
    timeRemaining: Long,
    label: String,
    isWarning: Boolean = false,
    isElapsed: Boolean = false
) {
    val color = when {
        isElapsed -> Color(0xFF2196F3)
        isWarning -> Color(0xFFF44336)
        timeRemaining > 10 * 60 * 1000L -> Color(0xFF4CAF50)
        else -> Color(0xFFFF9800)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                formatTime(timeRemaining),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

// ✅ CONTENIDO DE LA PREGUNTA
@Composable
fun ICFESQuestionContent(
    viewModel: ICFESSimulationViewModel,
    question: ICFESQuestion,
    session: SimulationSession,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = if (viewModel.hasAnsweredCurrentQuestion) 180.dp else 16.dp // ✅ AJUSTE DINÁMICO
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información de la pregunta
            item {
                ICFESQuestionInfo(
                    question = question,
                    sessionColor = Color(session.color)
                )
            }

            // Contexto si existe
            if (!question.context.isNullOrEmpty()) {
                item {
                    ICFESQuestionContext(context = question.context)
                }
            }

            // Pregunta principal
            item {
                ICFESQuestionCard(
                    question = question,
                    sessionColor = Color(session.color)
                )
            }

            // Opciones de respuesta
            item {
                ICFESAnswerOptions(
                    question = question,
                    userAnswer = viewModel.userAnswer,
                    hasAnswered = viewModel.hasAnsweredCurrentQuestion,
                    onAnswerSelected = { viewModel.selectAnswer(it) }
                )
            }

            // ✅ BOTÓN DE ENVIAR RESPUESTA - SIEMPRE DENTRO DEL SCROLL
            if (!viewModel.hasAnsweredCurrentQuestion) {
                item {
                    ICFESSubmitAnswerButton(
                        enabled = viewModel.userAnswer.isNotEmpty(),
                        onSubmit = { viewModel.submitAnswer() },
                        sessionColor = Color(session.color)
                    )
                }
            }

            // ✅ SPACER ADICIONAL CUANDO HA RESPONDIDO PARA QUE LOS BOTONES NO TAPEN CONTENIDO
            if (viewModel.hasAnsweredCurrentQuestion) {
                item {
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }

        // ✅ BOTONES DE NAVEGACIÓN - SOLO SI HA RESPONDIDO
        if (viewModel.hasAnsweredCurrentQuestion) {
            ICFESNavigationButtons(
                modifier = Modifier.align(Alignment.BottomCenter),
                viewModel = viewModel,
                sessionColor = Color(session.color)
            )
        }
    }
}

// ✅ INFORMACIÓN DE LA PREGUNTA
@Composable
fun ICFESQuestionInfo(
    question: ICFESQuestion,
    sessionColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = sessionColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Category,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = sessionColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    question.competency,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicador de simulacro
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        "SIMULACRO",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF44336)
                    )
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

// ✅ CONTEXTO DE LA PREGUNTA
@Composable
fun ICFESQuestionContext(context: String) {
    var isExpanded by remember { mutableStateOf(false) }
    val maxLines = if (isExpanded) Int.MAX_VALUE else 6

    Card(
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
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Default.Article,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "📖 Texto Base",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                if (context.length > 400) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Ver menos" else "Ver más",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                context,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3,
                maxLines = maxLines
            )

            if (!isExpanded && context.length > 400) {
                Text(
                    "Toca para leer completo...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

// ✅ TARJETA DE LA PREGUNTA
@Composable
fun ICFESQuestionCard(
    question: ICFESQuestion,
    sessionColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = sessionColor.copy(alpha = 0.1f)
        )
    ) {
        Text(
            question.question,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            lineHeight = MaterialTheme.typography.headlineSmall.lineHeight * 1.2,
            modifier = Modifier.padding(20.dp)
        )
    }
}

// ✅ OPCIONES DE RESPUESTA
@Composable
fun ICFESAnswerOptions(
    question: ICFESQuestion,
    userAnswer: String,
    hasAnswered: Boolean,
    onAnswerSelected: (String) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Selecciona tu respuesta:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            question.options.forEach { option ->
                val optionLetter = option.first().toString()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (userAnswer == optionLetter)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    onClick = {
                        if (!hasAnswered) {
                            onAnswerSelected(optionLetter)
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        RadioButton(
                            selected = userAnswer == optionLetter,
                            onClick = {
                                if (!hasAnswered) {
                                    onAnswerSelected(optionLetter)
                                }
                            },
                            enabled = !hasAnswered
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

// ✅ BOTÓN DE ENVIAR RESPUESTA
@Composable
fun ICFESSubmitAnswerButton(
    enabled: Boolean,
    onSubmit: () -> Unit,
    sessionColor: Color
) {
    Button(
        onClick = onSubmit,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = sessionColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                Icons.Default.Send,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Confirmar Respuesta",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ✅ BOTONES DE NAVEGACIÓN
@Composable
fun ICFESNavigationButtons(
    modifier: Modifier = Modifier,
    viewModel: ICFESSimulationViewModel,
    sessionColor: Color
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Progreso
            val sessionProgress = viewModel.getSessionProgress()
            val globalProgress = viewModel.getProgress()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Progreso sesión: ${(sessionProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Global: ${(globalProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = sessionProgress,
                modifier = Modifier.fillMaxWidth(),
                color = sessionColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.previousQuestion() },
                    enabled = viewModel.canGoToPreviousQuestion(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Anterior")
                }

                Button(
                    onClick = { viewModel.nextQuestion() },
                    enabled = viewModel.canGoToNextQuestion(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = sessionColor
                    )
                ) {
                    val isLastQuestion = viewModel.currentQuestionIndex ==
                            (viewModel.getCurrentSession()?.questions?.size ?: 1) - 1
                    val isLastSession = viewModel.simulationState.currentSession ==
                            (viewModel.currentSimulation?.sessions?.size ?: 1) - 1

                    Text(
                        when {
                            isLastQuestion && isLastSession -> "Finalizar"
                            isLastQuestion -> "Siguiente Sesión"
                            else -> "Siguiente"
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        if (isLastQuestion) Icons.Default.Check else Icons.Default.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

// ✅ FUNCIONES DE UTILIDAD
fun formatTime(timeMs: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(timeMs)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMs) % 60

    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
        else -> String.format("%02d:%02d", minutes, seconds)
    }
}