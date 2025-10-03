// duelo/LobbyScreen.kt - CON SONIDOS INTEGRADOS (MANTIENE TODAS LAS FUNCIONALIDADES)
package com.charlesdev.icfes.student.duelo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.charlesdev.icfes.student.duelo.audio.rememberDuelSoundManager
import com.charlesdev.icfes.student.duelo.utils.TeamSummaryWithAvatars
import com.charlesdev.icfes.ui.theme.UISColors
import com.charlesdev.icfes.student.duelo.utils.EnhancedPlayerItem

import kotlin.collections.forEach
import kotlin.collections.plus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    code: String,
    asHost: Boolean,
    controller: DuelController,
    navController: NavHostController,
    onStart: () -> Unit,
    onGoGame: () -> Unit
) {
    val match by controller.myMatchFlow!!.collectAsState(initial = null)

    // ✅ NUEVO: Sistema de sonidos integrado
    val context = LocalContext.current
    val soundManager = rememberDuelSoundManager(context)

    // ✅ NUEVO: Control de sonidos para evitar duplicados
    var lastPlayerCount by remember { mutableStateOf(0) }
    var lastBalancedState by remember { mutableStateOf(false) }

    // Estados existentes (MANTENER EXACTAMENTE IGUAL)
    var rulesExpanded by remember { mutableStateOf(false) }
    val infinite = rememberInfiniteTransition(label = "glow")
    val glow by infinite.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAnim"
    )

    // Lógica de validación (MANTENER EXACTAMENTE IGUAL)
    val teamACount = match?.teamA?.players?.size ?: 0
    val teamBCount = match?.teamB?.players?.size ?: 0
    val allPlayers = (match?.teamA?.players ?: emptyList()) + (match?.teamB?.players ?: emptyList())

    val isBalanced = teamACount == teamBCount && teamACount > 0
    val canStart = isBalanced && teamACount <= 4 // Máximo 4vs4
    val balanceMessage = when {
        teamACount == 0 && teamBCount == 0 -> "Esperando jugadores..."
        teamACount == 0 -> "Equipo A necesita al menos 1 jugador"
        teamBCount == 0 -> "Equipo B necesita al menos 1 jugador"
        teamACount != teamBCount -> "Los equipos deben tener el mismo número de jugadores (${teamACount}vs${teamBCount})"
        teamACount > 4 -> "Máximo 4 jugadores por equipo"
        isBalanced -> "✅ Equipos balanceados: ${teamACount}vs${teamBCount} - ¡Listos para duelo individual!"
        else -> "Configurando equipos..."
    }

    // ✅ NUEVO: Detectar cuando se une un jugador
    LaunchedEffect(allPlayers.size) {
        val currentPlayerCount = allPlayers.size
        if (currentPlayerCount > lastPlayerCount && lastPlayerCount > 0) {
            soundManager.playPlayerJoin()
        }
        lastPlayerCount = currentPlayerCount
    }

    // ✅ NUEVO: Detectar cuando se logra balance de equipos
    LaunchedEffect(isBalanced) {
        if (isBalanced && !lastBalancedState && teamACount > 0) {
            // Sonido especial cuando se logra el balance
            soundManager.playNotification()
        }
        lastBalancedState = isBalanced
    }

    // Scaffold (MANTENER EXACTAMENTE IGUAL con sonidos en navegación)
    Scaffold(
        topBar = {
            Card(
                modifier = Modifier.fillMaxWidth()
                    .semantics {
                        contentDescription = "Lobby del Duelo Individual - Universidad Industrial de Santander"
                    },
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    UISColors.Primary,
                                    UISColors.PrimaryVariant
                                )
                            )
                        )
                        .padding(top = 40.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                soundManager.playButtonClick() // ✅ NUEVO: Sonido
                                navController.navigateUp()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📚 Lobby — Duelo ICFES",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Avance individual • Timers personales • Preparación ICFES",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Individual",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(UISColors.Background)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Código de sala (MANTENER EXACTAMENTE IGUAL)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    border = BorderStroke(1.dp, UISColors.Primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.QrCode,
                                contentDescription = null,
                                tint = UISColors.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "🔗 Código de sala",
                                style = MaterialTheme.typography.titleMedium,
                                color = UISColors.Primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = UISColors.Background),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                code,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = UISColors.Primary,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        if (match?.pin != null) {
                            Spacer(Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = UISColors.AccentGreen1.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = UISColors.Primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "PIN: ${match!!.pin}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = UISColors.Primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }



            // Estado de balanceado (MANTENER EXACTAMENTE IGUAL)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (canStart)
                            UISColors.Primary.copy(alpha = 0.1f)
                        else
                            UISColors.AccentGreen2.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    border = BorderStroke(
                        2.dp,
                        if (canStart) UISColors.Primary else UISColors.AccentGreen2
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (canStart) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (canStart) UISColors.Primary else UISColors.AccentGreen2,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            balanceMessage,
                            fontWeight = FontWeight.Medium,
                            color = if (canStart) UISColors.Primary else UISColors.AccentGreen2,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Título de equipos (MANTENER EXACTAMENTE IGUAL)
            item {
                Text(
                    "🏆 Equipos:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = UISColors.Primary
                )
            }

            // Resumen de equipos con avatares (MANTENER EXACTAMENTE IGUAL)
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TeamSummaryWithAvatars(
                        team = match?.teamA ?: Team(),
                        teamName = match?.teamA?.name ?: "Equipo A",
                        teamLetter = "A",
                        teamColor = Color(0xFF1976D2),
                        isBalanced = canStart,
                        modifier = Modifier.weight(1f)
                    )

                    TeamSummaryWithAvatars(
                        team = match?.teamB ?: Team(),
                        teamName = match?.teamB?.name ?: "Equipo B",
                        teamLetter = "B",
                        teamColor = Color(0xFFD32F2F),
                        isBalanced = canStart,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Lista detallada de jugadores (MANTENER EXACTAMENTE IGUAL)
            item {
                Text(
                    "👥 Jugadores conectados:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = UISColors.Primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    border = BorderStroke(1.dp, UISColors.Primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        if (allPlayers.isEmpty()) {
                            Text(
                                "• Sin jugadores conectados",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF666666),
                                fontStyle = FontStyle.Italic
                            )
                        } else {
                            if (teamACount > 0) {
                                Text(
                                    "Equipo A (${teamACount}):",
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1976D2),
                                    fontSize = 14.sp
                                )

                                match?.teamA?.players?.forEach { player ->
                                    EnhancedPlayerItem(
                                        player = player,
                                        team = "A",
                                        teamColor = Color(0xFF1976D2),
                                        isMe = player.id == controller.myPlayer?.id
                                    )
                                }

                                if (teamBCount > 0) Spacer(Modifier.height(8.dp))
                            }

                            if (teamBCount > 0) {
                                Text(
                                    "Equipo B (${teamBCount}):",
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFD32F2F),
                                    fontSize = 14.sp
                                )

                                match?.teamB?.players?.forEach { player ->
                                    EnhancedPlayerItem(
                                        player = player,
                                        team = "B",
                                        teamColor = Color(0xFFD32F2F),
                                        isMe = player.id == controller.myPlayer?.id
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Botones de acción (CON SONIDOS INTEGRADOS)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    border = BorderStroke(1.dp, UISColors.Primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = UISColors.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "🎮 Acciones del Duelo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = UISColors.Primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (asHost) {
                                Button(
                                    onClick = {
                                        soundManager.playButtonClick() // ✅ NUEVO: Sonido
                                        onStart()
                                    },
                                    enabled = canStart,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                brush = if (canStart) {
                                                    Brush.horizontalGradient(
                                                        colors = listOf(
                                                            UISColors.Primary,
                                                            UISColors.PrimaryVariant
                                                        )
                                                    )
                                                } else {
                                                    Brush.horizontalGradient(colors = listOf(
                                                        Color.Gray,
                                                        Color.Gray
                                                    )
                                                    )
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = Color.White
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                if (canStart) "Iniciar duelo individual ${teamACount}v${teamBCount}" else "Esperando equipos",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                            }

                            Button(
                                onClick = {
                                    soundManager.playButtonClick() // ✅ NUEVO: Sonido
                                    onGoGame()
                                },
                                enabled = match?.started == true,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            brush = if (match?.started == true) {
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        Color(0xFFFF5722),
                                                        Color(0xFFE64A19)
                                                    )
                                                )
                                            } else {
                                                Brush.horizontalGradient(colors = listOf(
                                                    Color.Gray,
                                                    Color.Gray
                                                )
                                                )
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        "Entrar",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Reglas (CON SONIDOS EN EXPANSIÓN)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            soundManager.playButtonClick() // ✅ NUEVO: Sonido al expandir
                            rulesExpanded = !rulesExpanded
                        },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    border = BorderStroke(1.dp, UISColors.Primary.copy(alpha = 0.2f))
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Rule,
                                    contentDescription = null,
                                    tint = UISColors.Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "📋 Reglas del duelo individual",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = UISColors.Primary
                                )
                            }
                            Icon(
                                if (rulesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (rulesExpanded) "Contraer" else "Expandir",
                                tint = UISColors.Primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        AnimatedVisibility(visible = rulesExpanded) {
                            Column {
                                Spacer(Modifier.height(16.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = UISColors.Background),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        "• 20 preguntas de resistencia de materiales\n" +
                                                "• Cada jugador tiene timer individual de 25s por pregunta\n" +
                                                "• Avanzas automáticamente al responder (sin esperar)\n" +
                                                "• Feedback inmediato: ves si acertaste al momento\n" +
                                                "• 10 puntos por respuesta correcta\n" +
                                                "• Si abandonas, tu progreso se congela\n" +
                                                "• Gana el equipo con más puntos totales\n" +
                                                "• Equipos balanceados: 1v1, 2v2, 3v3 o 4v4",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF424242),
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
