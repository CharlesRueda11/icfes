// duelo/GameScreen.kt - ACTUALIZADO CON SISTEMA DE LETRAS

package com.charlesdev.icfes.student.duelo

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import com.charlesdev.icfes.student.duelo.audio.rememberDuelSoundManager
import com.charlesdev.icfes.student.duelo.utils.IndividualWinnerScreen
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    code: String,
    controller: DuelController,
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val match by controller.myMatchFlow!!.collectAsState(initial = null)
    val myPlayerId = controller.myPlayer?.id ?: ""

    val context = LocalContext.current
    val soundManager = rememberDuelSoundManager(context)

    var lastQuestionIndex by remember { mutableStateOf(-1) }
    var lastTimeLeft by remember { mutableStateOf(-1) }
    var gameStartSoundPlayed by remember { mutableStateOf(false) }

    val questionsLoader = remember { controller.getQuestionsLoader() }
    val questions by questionsLoader.questionsCache.collectAsState()
    val isLoadingQuestions by questionsLoader.isLoading.collectAsState()

    var lastAnswerWasCorrect by remember { mutableStateOf<Boolean?>(null) }
    var showAnswerFeedback by remember { mutableStateOf(false) }

    var impact by remember { mutableStateOf(0f) }
    val shake = remember { Animatable(0f) }
    val successScale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        if (questions.isEmpty()) {
            questionsLoader.loadQuestionsForMatch()
        }
    }

    LaunchedEffect(match?.started) {
        if (match?.started == true && !gameStartSoundPlayed) {
            soundManager.playGameStart()
            gameStartSoundPlayed = true
        }
    }

    val myPlayer = match?.findPlayer(myPlayerId)
    val isMyTurn = myPlayer != null && !myPlayer.hasFinished && !myPlayer.hasAnsweredCurrentQuestion()

    LaunchedEffect(myPlayer?.currentQuestionIndex) {
        val currentIndex = myPlayer?.currentQuestionIndex ?: 0
        if (currentIndex != lastQuestionIndex && currentIndex > lastQuestionIndex && lastQuestionIndex >= 0) {
            soundManager.playQuestionAppear()
            lastQuestionIndex = currentIndex
        } else if (lastQuestionIndex == -1) {
            lastQuestionIndex = currentIndex
        }
    }

    LaunchedEffect(match?.finished) {
        if (match?.finished == true) {
            val myTeamSide = match!!.getPlayerTeamSide(myPlayerId)
            val playerWon = match!!.winner == myTeamSide?.name

            if (playerWon) {
                soundManager.playVictory()
            }
        }
    }

    LaunchedEffect(match?.started, myPlayer?.currentQuestionIndex, myPlayer?.hasFinished) {
        if (match?.started == true && myPlayer != null && !myPlayer.hasFinished) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    var isActive = true
                    while (isActive && !myPlayer.hasFinished) {
                        delay(1000)

                        val currentMatch = controller.myMatchFlow?.value
                        val currentPlayer = currentMatch?.findPlayer(myPlayerId)

                        if (currentMatch == null || currentPlayer == null || currentPlayer.hasFinished) {
                            Log.d("GameScreen", "Stopping individual timer: player finished or match null")
                            isActive = false
                            break
                        }

                        if (!currentPlayer.hasAnsweredCurrentQuestion()) {
                            val timeLeft = currentPlayer.timeLeftOnCurrentQuestion

                            if (timeLeft != lastTimeLeft) {
                                soundManager.playTimerSequence(timeLeft)
                                lastTimeLeft = timeLeft
                            }

                            if (timeLeft > 0) {
                                val teamSide = currentMatch.getPlayerTeamSide(myPlayerId)
                                val teamPath = if (teamSide == TeamSide.A) "teamA" else "teamB"
                                val team = if (teamSide == TeamSide.A) currentMatch.teamA else currentMatch.teamB

                                val updatedPlayers = team.players.map { player ->
                                    if (player.id == myPlayerId) {
                                        player.copy(timeLeftOnCurrentQuestion = player.timeLeftOnCurrentQuestion - 1)
                                    } else player
                                }

                                val updates = mapOf("$teamPath/players" to updatedPlayers)
                                FirebaseDatabase.getInstance()
                                    .getReference("duel_matches")
                                    .child(code)
                                    .updateChildren(updates)
                            } else {
                                Log.d("GameScreen", "Time's up for player $myPlayerId, forcing next question")
                                controller.service.forcePlayerNextQuestion(code, myPlayerId)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GameScreen", "Individual timer error: ${e.message}")
                }
            }
        }
    }

    LaunchedEffect(myPlayer?.currentQuestionIndex) {
        showAnswerFeedback = false
        lastAnswerWasCorrect = null
        lastTimeLeft = -1
    }

    DisposableEffect(code) {
        onDispose {
            Log.d("GameScreen", "GameScreen disposed, cleaning up")
        }
    }

    // ✅ CAMBIO: Obtener pregunta con validación
    val myCurrentQuestion = remember(myPlayer?.currentQuestionIndex, questions.isNotEmpty()) {
        val currentIndex = myPlayer?.currentQuestionIndex ?: 0
        val question = if (currentIndex < questions.size) {
            questionsLoader.getQuestionByIndex(currentIndex)
        } else {
            SampleQuestionsICFES.list.getOrNull(currentIndex)
        }

        // Validar que la pregunta esté bien formada
        if (question != null && !question.isValid()) {
            Log.e("GameScreen", "Invalid question at index $currentIndex: ${question.getDebugInfo()}")
            null
        } else {
            question
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Duelo ICFES",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (myPlayer != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "${myPlayer.currentQuestionIndex + 1}/20",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        soundManager.playButtonClick()
                        navController.navigateUp()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (myPlayer != null && !myPlayer.hasFinished) {
                        IndividualTimerBadge(
                            timeLeft = myPlayer.timeLeftOnCurrentQuestion,
                            hasAnswered = myPlayer.hasAnsweredCurrentQuestion()
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        if (match?.finished == true) {
            IndividualWinnerScreen(
                match = match!!,
                onPlayAgain = {
                    soundManager.playButtonClick()
                    navController.navigateUp()
                },
                onExit = {
                    soundManager.playButtonClick()
                    navController.popBackStack()
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                if (match != null) {
                    OverallProgressCard(
                        match = match!!,
                        myPlayerId = myPlayerId,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (myPlayer != null) {
                    MyPersonalStatusCard(
                        player = myPlayer,
                        totalPlayers = match?.getTotalPlayers() ?: 0,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                AnimatedVisibility(
                    visible = showAnswerFeedback,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit = slideOutVertically { -it } + fadeOut()
                ) {
                    AnswerFeedbackCard(
                        wasCorrect = lastAnswerWasCorrect == true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (myPlayer?.hasFinished == true) {
                    WaitingForOthersCard(
                        myScore = myPlayer.individualScore,
                        pendingPlayers = match?.getAllPlayers()?.filter { !it.hasFinished } ?: emptyList(),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (isLoadingQuestions) {
                    LoadingQuestionsCard()
                } else if (myCurrentQuestion != null) {
                    // ✅ CAMBIO PRINCIPAL: Usar el nuevo componente con letras
                    CurrentQuestionCardWithLetters(
                        question = myCurrentQuestion,
                        isEnabled = isMyTurn,
                        onAnswerSelected = { selectedLetter -> // ✅ Ahora recibe Char
                            if (isMyTurn) {
                                scope.launch {
                                    try {
                                        Log.d("GameScreen", "Player selected letter: $selectedLetter")

                                        // ✅ USAR EL NUEVO MÉTODO CON LETRAS
                                        val wasCorrect = controller.service.submitAnswerByLetter(code, selectedLetter)

                                        soundManager.playAnswerFeedback(wasCorrect)

                                        lastAnswerWasCorrect = wasCorrect
                                        showAnswerFeedback = true

                                        if (wasCorrect) {
                                            impact = 1f
                                            launch {
                                                successScale.animateTo(1.1f, tween(150))
                                                successScale.animateTo(1f, tween(150))
                                            }
                                        } else {
                                            launch {
                                                shake.animateTo(1f, tween(60))
                                                shake.animateTo(-1f, tween(60))
                                                shake.animateTo(1f, tween(60))
                                                shake.animateTo(0f, tween(60))
                                            }
                                        }

                                        delay(200)
                                        impact = 0f

                                        delay(2000)
                                        showAnswerFeedback = false

                                    } catch (e: Exception) {
                                        Log.e("GameScreen", "Error submitting answer: ${e.message}")
                                    }
                                }
                            }
                        },
                        shakeOffset = shake.value,
                        successScale = successScale.value,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ErrorCard("No hay pregunta disponible")
                }

                if (match != null) {
                    EventLogCard(
                        events = match!!.log,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ✅ MANTENER IGUAL: IndividualTimerBadge
@Composable
private fun IndividualTimerBadge(timeLeft: Int, hasAnswered: Boolean) {
    val color by animateColorAsState(
        targetValue = when {
            hasAnswered -> Color(0xFF4CAF50)
            timeLeft <= 5 -> Color(0xFFFF4D4F)
            timeLeft <= 10 -> Color(0xFFFFA600)
            else -> MaterialTheme.colorScheme.primary
        },
        label = "timer_color"
    )

    Surface(
        color = color,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                if (hasAnswered) Icons.Default.CheckCircle else Icons.Default.Timer,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Color.White
            )
            Text(
                if (hasAnswered) "✓" else "${timeLeft}s",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

// ✅ MANTENER IGUAL: OverallProgressCard, TeamProgressColumn, PlayerProgressRow, MyPersonalStatusCard, AnswerFeedbackCard, WaitingForOthersCard
@Composable
private fun OverallProgressCard(
    match: Match,
    myPlayerId: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Progreso General",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TeamProgressColumn(
                    team = match.teamA,
                    teamName = "Equipo A",
                    teamColor = Color(0xFF1976D2),
                    myPlayerId = myPlayerId,
                    modifier = Modifier.weight(1f)
                )

                TeamProgressColumn(
                    team = match.teamB,
                    teamName = "Equipo B",
                    teamColor = Color(0xFFD32F2F),
                    myPlayerId = myPlayerId,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TeamProgressColumn(
    team: Team,
    teamName: String,
    teamColor: Color,
    myPlayerId: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            teamName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = teamColor
        )

        team.players.forEach { player ->
            PlayerProgressRow(
                player = player,
                isMe = player.id == myPlayerId,
                teamColor = teamColor
            )
        }
    }
}

@Composable
private fun PlayerProgressRow(
    player: Player,
    isMe: Boolean,
    teamColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isMe) teamColor.copy(alpha = 0.1f) else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isMe) "${player.name} (Tú)" else player.name,
            fontSize = 10.sp,
            fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal,
            color = if (isMe) teamColor else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${player.currentQuestionIndex + 1}/20",
                fontSize = 10.sp,
                color = teamColor
            )

            if (player.hasFinished) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Terminado",
                    modifier = Modifier.size(12.dp),
                    tint = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
private fun MyPersonalStatusCard(
    player: Player,
    totalPlayers: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Mi Estado",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Pregunta ${player.currentQuestionIndex + 1}/20",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${player.individualScore} pts",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "${player.getCorrectAnswers()} correctas",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun AnswerFeedbackCard(
    wasCorrect: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (wasCorrect) Color(0xFF4CAF50) else Color(0xFFFF5722)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (wasCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (wasCorrect) "¡CORRECTO! +10 puntos" else "Incorrecto. +0 puntos",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun WaitingForOthersCard(
    myScore: Int,
    pendingPlayers: List<Player>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "¡Has terminado!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                "Puntuación final: $myScore puntos",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(Modifier.height(8.dp))

            if (pendingPlayers.isNotEmpty()) {
                Text(
                    "Esperando a ${pendingPlayers.size} jugadores...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )

                Text(
                    pendingPlayers.joinToString(", ") { it.name },
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// ✅ NUEVO COMPONENTE: Pregunta con letras A, B, C, D
@Composable
private fun CurrentQuestionCardWithLetters(
    question: Question,
    isEnabled: Boolean,
    onAnswerSelected: (Char) -> Unit, // ✅ Recibe Char
    shakeOffset: Float,
    successScale: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.graphicsLayer {
            scaleX = successScale
            scaleY = successScale
        },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                question.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
                color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(12.dp))

            // ✅ NUEVO: Opciones con letras A, B, C, D
            val letters = listOf('A', 'B', 'C', 'D')
            val options = listOf(question.optionA, question.optionB, question.optionC, question.optionD)

            options.forEachIndexed { index, option ->
                val letter = letters[index]
                AnswerOptionWithLetter(
                    letter = letter,
                    text = option,
                    enabled = isEnabled,
                    onClick = { onAnswerSelected(letter) }, // ✅ ENVIAR LA LETRA
                    shakeOffset = shakeOffset,
                    modifier = Modifier.fillMaxWidth()
                )

                if (index < options.size - 1) {
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// ✅ NUEVO COMPONENTE: Opción de respuesta con letra
@Composable
private fun AnswerOptionWithLetter(
    letter: Char,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    shakeOffset: Float,
    modifier: Modifier = Modifier
) {
    val offsetX = shakeOffset * 8f

    Card(
        modifier = modifier
            .graphicsLayer { translationX = offsetX }
            .clickable(enabled = enabled) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (enabled)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ NUEVO: Círculo con la letra
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = if (enabled)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            // Texto de la opción
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ✅ MANTENER IGUAL: Cards auxiliares
@Composable
private fun LoadingQuestionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
                Text("Cargando preguntas...")
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(8.dp))
            Text(
                message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EventLogCard(
    events: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Eventos",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (events.isEmpty()) {
                Text(
                    "Sin eventos...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            } else {
                LazyColumn(Modifier.heightIn(max = 120.dp)) {
                    items(events.takeLast(5).reversed()) { event ->
                        Text(
                            event,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}