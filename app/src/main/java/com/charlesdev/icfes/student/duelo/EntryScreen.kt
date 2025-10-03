package com.charlesdev.icfes.student.duelo

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.charlesdev.icfes.student.duelo.audio.rememberDuelSoundManager

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.let
import kotlin.text.ifBlank
import kotlin.text.isBlank
import kotlin.text.isNotBlank
import kotlin.text.take
import kotlin.text.uppercase

@Composable
fun EntryScreen(
    navController: NavHostController,
    controller: DuelController,
    onNavigate: (Screen) -> Unit,
    onError: (String) -> Unit = {}
) {
    val currentUser = controller.myPlayer
    val scope = rememberCoroutineScope()

    // ✅ NUEVO: Sistema de sonidos integrado
    val context = LocalContext.current
    val soundManager = rememberDuelSoundManager(context)

    var teamName by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var joinCode by remember { mutableStateOf("") }
    var joinPin by remember { mutableStateOf("") }
    var playerName by remember { mutableStateOf(currentUser?.name ?: "") }
    var side by remember { mutableStateOf(TeamSide.A) }

    var isCreating by remember { mutableStateOf(false) }
    var isJoining by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // ✅ TopBar con diseño UIS profesional (VERDE - mantenido)
            Card(
                modifier = Modifier.fillMaxWidth()
                    .semantics {
                        contentDescription = "Duelo de Tensiones - Universidad Industrial de Santander"
                    },
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF2E7D32), // Verde UIS oscuro
                                    Color(0xFF67B93E), // Verde UIS principal
                                    Color(0xFF8BC34A)  // Verde UIS claro
                                )
                            )
                        )
                        .padding(top = 40.dp)
                        .padding(horizontal = 20.dp, vertical = 18.dp)
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
                                .size(48.dp)
                                .background(
                                    Color.White.copy(alpha = 0.25f),
                                    CircleShape
                                )
                                .semantics {
                                    contentDescription = "Volver al menú principal"
                                }
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    contentDescription = "Duelo de Tensiones - Equipos equilibrados"
                                }
                        ) {
                            Text(
                                text = "📚 DUELO ICFES",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            )
                            /* Text(
                                 text = "Equipos equilibrados • Resistencia de materiales • UIS",
                                 color = Color.White.copy(alpha = 0.95f),
                                 fontSize = 14.sp,
                                 fontWeight = FontWeight.Medium
                             )*/
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    Color.White.copy(alpha = 0.25f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Engineering,
                                contentDescription = "Ingeniería",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ✅ MOVIDO: Mecánicas expandibles estilo Competition (VERDE UIS - mantenido) - AHORA ARRIBA
            GameRulesCardStyled(soundManager)

            // ✅ ARREGLADO: Cards con colores correctos (AZUL para A, ROJO para B)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ✅ Crear duelo (Equipo A - AZUL)
                UISTeamCard(
                    modifier = Modifier.weight(1f),
                    title = "CREAR DUELO",
                    subtitle = "Lidera el Equipo A",
                    teamColor = Color(0xFF1976D2), // AZUL para Equipo A
                    teamName = "EQUIPO A",
                    description = "Serás el capitán automáticamente",
                    icon = Icons.Default.Group,
                    isLoading = isCreating,
                    enabled = !isCreating && currentUser != null,
                    content = {
                        OutlinedTextField(
                            value = teamName,
                            onValueChange = { teamName = it },
                            label = { Text("Nombre de tu equipo", fontWeight = FontWeight.Medium) },
                            placeholder = { Text("Equipo A") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isCreating,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1976D2), // AZUL
                                focusedLabelColor = Color(0xFF1976D2),
                                focusedTextColor = Color(0xFF1F2937)
                            )
                        )

                        OutlinedTextField(
                            value = pin,
                            onValueChange = { pin = it },
                            label = { Text("PIN (opcional)", fontWeight = FontWeight.Medium) },
                            placeholder = { Text("Para duelo privado") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = !isCreating,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1976D2), // AZUL
                                focusedLabelColor = Color(0xFF1976D2),
                                focusedTextColor = Color(0xFF1F2937)
                            )
                        )
                    },
                    onAction = {
                        if (currentUser == null) {
                            onError("No hay usuario autenticado")
                            return@UISTeamCard
                        }

                        scope.launch {
                            isCreating = true
                            try {
                                soundManager.playButtonClick() // ✅ NUEVO: Sonido

                                Log.d("EntryScreen", "Creating match with team: ${teamName.ifBlank { "Equipo A" }}")
                                controller.mySide = TeamSide.A

                                val firebaseUser = FirebaseAuth.getInstance().currentUser
                                val playerWithPhoto = controller.myPlayer ?: Player(
                                    id = firebaseUser?.uid ?: "",
                                    name = firebaseUser?.displayName ?: firebaseUser?.email ?: "Usuario",
                                    email = firebaseUser?.email ?: ""
                                )

                                val match = controller.service.createMatch(
                                    teamName.ifBlank { "Equipo A" },
                                    pin.ifBlank { null },
                                    playerWithPhoto
                                )

                                controller.bind(match.code)
                                onNavigate(Screen.LobbyHost(match.code))
                            } catch (e: Exception) {
                                Log.e("EntryScreen", "Error creating match", e)
                                onError("Error al crear duelo: ${e.message}")
                            } finally {
                                isCreating = false
                            }
                        }
                    },
                    buttonText = if (isCreating) "Creando duelo..." else "Crear como Equipo A",
                    currentTeamColor = Color(0xFF1976D2), // AZUL
                    errorMessage = if (currentUser == null) "Debes estar autenticado para crear duelos" else null
                )

                // ✅ Unirse a duelo (DINÁMICO - azul o rojo según selección)
                UISTeamCard(
                    modifier = Modifier.weight(1f),
                    title = "UNIRSE AL DUELO",
                    subtitle = "Elige tu bando",
                    teamColor = if (side == TeamSide.A) Color(0xFF1976D2) else Color(0xFFD32F2F), // DINÁMICO
                    teamName = "EQUIPO ${side.name}",
                    description = "Selección final de bando",
                    icon = Icons.Default.PersonAdd,
                    isLoading = isJoining,
                    enabled = !isJoining && joinCode.isNotBlank() && playerName.isNotBlank(),
                    content = {
                        OutlinedTextField(
                            value = joinCode,
                            onValueChange = { joinCode = it.uppercase().take(6) },
                            label = { Text("Código de duelo", fontWeight = FontWeight.Medium) },
                            placeholder = { Text("MAHLB5") },
                            enabled = !isJoining,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (side == TeamSide.A) Color(0xFF1976D2) else Color(0xFFD32F2F),
                                focusedLabelColor = if (side == TeamSide.A) Color(0xFF1976D2) else Color(0xFFD32F2F),
                                focusedTextColor = Color(0xFF1F2937)
                            )
                        )

                        OutlinedTextField(
                            value = joinPin,
                            onValueChange = { joinPin = it },
                            label = { Text("PIN (si aplica)", fontWeight = FontWeight.Medium) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = !isJoining,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (side == TeamSide.A) Color(0xFF1976D2) else Color(0xFFD32F2F),
                                focusedLabelColor = if (side == TeamSide.A) Color(0xFF1976D2) else Color(0xFFD32F2F),
                                focusedTextColor = Color(0xFF1F2937)
                            )
                        )

                        OutlinedTextField(
                            value = playerName,
                            onValueChange = { playerName = it },
                            label = { Text("Tu nombre", fontWeight = FontWeight.Medium) },
                            enabled = !isJoining,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (side == TeamSide.A) Color(0xFF1976D2) else Color(0xFFD32F2F),
                                focusedLabelColor = if (side == TeamSide.A) Color(0xFF1976D2) else Color(0xFFD32F2F),
                                focusedTextColor = Color(0xFF1F2937)
                            )
                        )

                        // ✅ Selección de equipo con colores correctos
                        Column {
                            Text(
                                "Elige tu bando:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1F2937)
                            )
                            Spacer(Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = side == TeamSide.A,
                                    onClick = {
                                        soundManager.playButtonClick() // ✅ NUEVO: Sonido
                                        side = TeamSide.A
                                    },
                                    label = { Text("EQUIPO A", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp) },
                                    enabled = !isJoining,
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF1976D2).copy(alpha = 0.15f), // AZUL
                                        selectedLabelColor = Color(0xFF1976D2)
                                    )
                                )

                                FilterChip(
                                    selected = side == TeamSide.B,
                                    onClick = {
                                        soundManager.playButtonClick() // ✅ NUEVO: Sonido
                                        side = TeamSide.B
                                    },
                                    label = { Text("EQUIPO B", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp) },
                                    enabled = !isJoining,
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFD32F2F).copy(alpha = 0.15f), // ROJO
                                        selectedLabelColor = Color(0xFFD32F2F)
                                    )
                                )
                            }

                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Esta selección es definitiva",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    onAction = {
                        if (joinCode.isBlank()) {
                            onError("Ingresa el código del duelo")
                            return@UISTeamCard
                        }

                        if (playerName.isBlank()) {
                            onError("Ingresa tu nombre")
                            return@UISTeamCard
                        }

                        scope.launch {
                            isJoining = true
                            try {
                                soundManager.playButtonClick() // ✅ NUEVO: Sonido

                                Log.d("EntryScreen", "Joining match: $joinCode as team $side")

                                val firebaseUser = FirebaseAuth.getInstance().currentUser
                                val playerWithPhoto = controller.myPlayer?.copy(name = playerName) ?: Player(
                                    id = firebaseUser?.uid ?: "",
                                    name = playerName,
                                    email = firebaseUser?.email ?: ""
                                )

                                controller.myPlayer = playerWithPhoto
                                controller.mySide = side
                                controller.bind(joinCode)

                                controller.service.joinMatch(
                                    joinCode,
                                    joinPin.ifBlank { null },
                                    playerWithPhoto,
                                    side
                                )

                                onNavigate(Screen.LobbyJoin(joinCode))
                            } catch (e: IllegalStateException) {
                                onError("El duelo no existe o ha expirado")
                            } catch (e: IllegalArgumentException) {
                                onError("PIN incorrecto")
                            } catch (e: Exception) {
                                onError("Error al unirse: ${e.message}")
                            } finally {
                                isJoining = false
                            }
                        }
                    },
                    buttonText = if (isJoining) "Uniéndose..." else "Unirme a Equipo ${side.name}",
                    currentTeamColor = if (side == TeamSide.A) Color(0xFF1976D2) else Color(0xFFD32F2F) // DINÁMICO
                )
            }
        }
    }
}

@Composable
fun UISTeamCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    teamColor: Color,
    teamName: String,
    description: String,
    icon: ImageVector,
    isLoading: Boolean,
    enabled: Boolean,
    content: @Composable ColumnScope.() -> Unit,
    onAction: () -> Unit,
    buttonText: String,
    currentTeamColor: Color = teamColor,
    errorMessage: String? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(12.dp),
        border = BorderStroke(2.dp, currentTeamColor.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con gradiente mejorado
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    currentTeamColor,
                                    currentTeamColor.copy(alpha = 0.85f)
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    Color.White.copy(alpha = 0.25f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 16.sp
                        )

                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.95f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Indicador de equipo mejorado
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = currentTeamColor.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, currentTeamColor.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = teamName,
                        fontWeight = FontWeight.ExtraBold,
                        color = currentTeamColor,
                        fontSize = 16.sp
                    )
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = currentTeamColor.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Contenido del formulario
            content()

            // Botón de acción mejorado
            Button(
                onClick = onAction,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (enabled) 6.dp else 0.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = if (enabled) {
                                Brush.linearGradient(
                                    colors = listOf(
                                        currentTeamColor,
                                        currentTeamColor.copy(alpha = 0.8f)
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF94A3B8), Color(0xFF64748B))
                                )
                            },
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = buttonText,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Mensaje de error si existe
            errorMessage?.let { message ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFEF2F2)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.2f))
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFDC2626),
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ✅ Componente expandible con sonidos integrados
@Composable
fun GameRulesCardStyled(soundManager: com.charlesdev.icfes.student.duelo.audio.DuelSoundManager) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.2f)) // VERDE UIS
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header expandible
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        soundManager.playButtonClick() // ✅ NUEVO: Sonido al expandir
                        isExpanded = !isExpanded
                    }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color(0xFF2E7D32).copy(alpha = 0.1f), // VERDE UIS
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32), // VERDE UIS
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Reglas del Duelo de Tensiones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32) // VERDE UIS
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Ocultar" else "Mostrar",
                    tint = Color(0xFF2E7D32), // VERDE UIS
                    modifier = Modifier.size(20.dp)
                )
            }

            // Contenido expandible
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    DuelRule(
                        icon = Icons.Default.Groups,
                        title = "Modos equilibrados",
                        description = "1v1, 2v2, 3v3 o 4v4 - Equipos siempre balanceados"
                    )
                    DuelRule(
                        icon = Icons.Default.Group,
                        title = "Formación de equipos",
                        description = "Host lidera Equipo A, otros eligen A o B"
                    )
                    DuelRule(
                        icon = Icons.Default.Timer,
                        title = "Timer individual 25s",
                        description = "Cada jugador avanza a su ritmo"
                    )
                    DuelRule(
                        icon = Icons.Default.Speed,
                        title = "Feedback instantáneo",
                        description = "Sabes si acertaste al momento"
                    )
                    DuelRule(
                        icon = Icons.Default.Engineering,
                        title = "20 preguntas ICFES",
                        description = "10 puntos por respuesta correcta",
                        isLast = true
                    )
                }
            }
        }
    }
}

@Composable
fun DuelRule(
    icon: ImageVector,
    title: String,
    description: String,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFF2E7D32).copy(alpha = 0.1f), CircleShape), // VERDE UIS
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF2E7D32), // VERDE UIS
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32) // VERDE UIS
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF2E7D32).copy(alpha = 0.7f) // VERDE UIS
            )
        }
    }

    if (!isLast) {
        Spacer(modifier = Modifier.height(12.dp))
    }
}

// Enum para las pantallas del juego
sealed class Screen {
    data object Entry : Screen()
    data class LobbyHost(val code: String) : Screen()
    data class LobbyJoin(val code: String) : Screen()
    data class Game(val code: String) : Screen()
}