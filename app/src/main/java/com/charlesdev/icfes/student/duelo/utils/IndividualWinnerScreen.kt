package com.charlesdev.icfes.student.duelo.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charlesdev.icfes.student.duelo.Match
import com.charlesdev.icfes.student.duelo.Player
import com.charlesdev.icfes.student.duelo.Team
import com.charlesdev.icfes.student.duelo.audio.rememberDuelSoundManager

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.forEachIndexed
import kotlin.collections.isNotEmpty
import kotlin.collections.maxByOrNull
import kotlin.collections.sortedByDescending

@Composable
fun IndividualWinnerScreen(
    match: Match,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val soundManager = rememberDuelSoundManager(context)

    val teamAScore = match.finalScoreA
    val teamBScore = match.finalScoreB
    val winner = match.winner

    // Determinar equipo ganador
    val winningTeam = when (winner) {
        "A" -> match.teamA
        "B" -> match.teamB
        else -> null
    }

    // Animaciones para la celebración
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }

    // Reproducir sonido de victoria al cargar
    LaunchedEffect(Unit) {
        soundManager.playButtonClick()
        delay(500)

        // Animación de entrada
        launch {
            alpha.animateTo(1f, animationSpec = tween(800))
        }
        launch {
            scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }

        delay(1000)

        // Sonido de victoria para el equipo ganador
        if (winner != "EMPATE") {
            soundManager.playVictory()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFE2E8F0)
                    )
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ✅ ARREGLADO: Espaciado superior adecuado
        item { Spacer(Modifier.height(60.dp)) }

        // ✅ ARREGLADO: Card principal de victoria centrada y sin cortes
        item {
            AnimatedVisibility(
                visible = alpha.value > 0.5f,
                enter = slideInVertically { -100 } + fadeIn()
            ) {
                ImprovedVictoryCard(
                    winner = winner,
                    teamAScore = teamAScore,
                    teamBScore = teamBScore,
                    teamAName = match.teamA.name,
                    teamBName = match.teamB.name,
                    winningTeam = winningTeam,
                    scale = scale.value
                )
            }
        }

        // ✅ ELIMINADO: La segunda card redundante de campeones

        // Comparación detallada de equipos (mejorada)
        item {
            DetailedTeamComparison(
                teamA = match.teamA,
                teamB = match.teamB,
                scoreA = teamAScore,
                scoreB = teamBScore,
                winner = winner
            )
        }

        // Destacados individuales (mantenida)
        item {
            IndividualHighlights(
                allPlayers = match.getAllPlayers(),
                teamA = match.teamA,
                teamB = match.teamB
            )
        }

        // Botones de acción
        item {
            ActionButtonsRow(
                soundManager = soundManager,
                onPlayAgain = onPlayAgain,
                onExit = onExit
            )
        }

        item { Spacer(Modifier.height(20.dp)) }
    }
}

// ✅ NUEVA: Card de victoria mejorada con mejor layout y centrado
@Composable
private fun ImprovedVictoryCard(
    winner: String,
    teamAScore: Int,
    teamBScore: Int,
    teamAName: String,
    teamBName: String,
    winningTeam: Team?,
    scale: Float
) {
    val winnerColor = when (winner) {
        "A" -> Color(0xFF1976D2)
        "B" -> Color(0xFFD32F2F)
        else -> Color(0xFF757575)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(12.dp),
        border = BorderStroke(3.dp, winnerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono de victoria
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        winnerColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = winnerColor
                )
            }

            Spacer(Modifier.height(20.dp))

            // Título del ganador
            Text(
                text = when (winner) {
                    "A" -> "¡${teamAName} GANA!"
                    "B" -> "¡${teamBName} GANA!"
                    else -> "¡EMPATE ÉPICO!"
                },
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = winnerColor,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // Marcador VS centrado
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8FAFC)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Equipo A
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (winner == "A") {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFFFFD700)
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                        Text(
                            teamAName,
                            fontSize = 16.sp,
                            fontWeight = if (winner == "A") FontWeight.Bold else FontWeight.Medium,
                            color = if (winner == "A") Color(0xFF1976D2) else Color(0xFF6B7280)
                        )
                        Text(
                            "$teamAScore",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (winner == "A") Color(0xFF1976D2) else Color(0xFF6B7280)
                        )
                        Text(
                            "puntos",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }

                    // VS
                    Text(
                        "VS",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF6B7280)
                    )

                    // Equipo B
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (winner == "B") {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFFFFD700)
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                        Text(
                            teamBName,
                            fontSize = 16.sp,
                            fontWeight = if (winner == "B") FontWeight.Bold else FontWeight.Medium,
                            color = if (winner == "B") Color(0xFFD32F2F) else Color(0xFF6B7280)
                        )
                        Text(
                            "$teamBScore",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (winner == "B") Color(0xFFD32F2F) else Color(0xFF6B7280)
                        )
                        Text(
                            "puntos",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }

            // Mostrar miembros del equipo ganador (integrado aquí)
            if (winningTeam != null) {
                Spacer(Modifier.height(20.dp))

                Text(
                    "CAMPEONES:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = winnerColor
                )

                Spacer(Modifier.height(12.dp))

                val sortedPlayers = winningTeam.players.sortedByDescending { it.individualScore }
                sortedPlayers.forEachIndexed { index, player ->
                    CompactPlayerRow(
                        player = player,
                        position = index + 1,
                        teamColor = winnerColor,
                        isTopScorer = index == 0
                    )
                    if (index < sortedPlayers.size - 1) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactPlayerRow(
    player: Player,
    position: Int,
    teamColor: Color,
    isTopScorer: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isTopScorer) teamColor.copy(alpha = 0.1f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Posición
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        when (position) {
                            1 -> Color(0xFFFFD700)
                            2 -> Color(0xFFC0C0C0)
                            3 -> Color(0xFFCD7F32)
                            else -> teamColor.copy(alpha = 0.3f)
                        },
                        RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (position <= 3) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                } else {
                    Text(
                        "$position",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Column {
                Text(
                    player.name,
                    fontSize = 14.sp,
                    fontWeight = if (isTopScorer) FontWeight.Bold else FontWeight.Medium,
                    color = Color(0xFF1F2937)
                )
                Text(
                    "${player.getCorrectAnswers()}/20 correctas",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }

        Text(
            "${player.individualScore}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = teamColor
        )
    }
}

// Componentes existentes mantenidos...
@Composable
private fun ActionButtonsRow(
    soundManager: com.charlesdev.icfes.student.duelo.audio.DuelSoundManager,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(
            onClick = {
                soundManager.playButtonClick()
                onExit()
            },
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF374151)
            ),
            border = BorderStroke(2.dp, Color(0xFF9CA3AF))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Salir",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Button(
            onClick = {
                soundManager.playButtonClick()
                onPlayAgain()
            },
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF059669),
                                Color(0xFF10B981)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Nuevo Duelo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailedTeamComparison(
    teamA: Team,
    teamB: Team,
    scoreA: Int,
    scoreB: Int,
    winner: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Comparación Detallada",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TeamSummaryColumn(
                    team = teamA,
                    teamName = teamA.name,
                    teamColor = Color(0xFF1976D2),
                    totalScore = scoreA,
                    isWinner = winner == "A"
                )

                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(120.dp)
                        .background(Color(0xFFE5E7EB))
                )

                TeamSummaryColumn(
                    team = teamB,
                    teamName = teamB.name,
                    teamColor = Color(0xFFD32F2F),
                    totalScore = scoreB,
                    isWinner = winner == "B"
                )
            }
        }
    }
}

@Composable
private fun TeamSummaryColumn(
    team: Team,
    teamName: String,
    teamColor: Color,
    totalScore: Int,
    isWinner: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isWinner) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFFFD700)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                teamName,
                fontWeight = FontWeight.Bold,
                color = teamColor,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            "$totalScore",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = teamColor
        )

        Text(
            "puntos totales",
            fontSize = 12.sp,
            color = Color(0xFF6B7280)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "${team.players.size} jugadores",
            fontSize = 12.sp,
            color = Color(0xFF6B7280)
        )

        val avgScore = if (team.players.isNotEmpty()) totalScore / team.players.size else 0
        Text(
            "Promedio: $avgScore pts",
            fontSize = 12.sp,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
private fun IndividualHighlights(
    allPlayers: List<Player>,
    teamA: Team,
    teamB: Team
) {
    val topScorer = allPlayers.maxByOrNull { it.individualScore }
    val mostCorrect = allPlayers.maxByOrNull { it.getCorrectAnswers() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Destacados Individuales",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(16.dp))

            if (topScorer != null) {
                HighlightCard(
                    title = "Máximo Puntaje",
                    playerName = topScorer.name,
                    value = "${topScorer.individualScore} puntos",
                    icon = Icons.Default.Star,
                    color = Color(0xFFFFD700)
                )
            }

            if (mostCorrect != null && mostCorrect != topScorer) {
                Spacer(Modifier.height(12.dp))
                HighlightCard(
                    title = "Más Respuestas Correctas",
                    playerName = mostCorrect.name,
                    value = "${mostCorrect.getCorrectAnswers()} correctas",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF10B981)
                )
            }
        }
    }
}

@Composable
private fun HighlightCard(
    title: String,
    playerName: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = color
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B7280)
                )
                Text(
                    playerName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }

            Text(
                value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}