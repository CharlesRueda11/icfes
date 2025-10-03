// duelo/DuelModeRoot.kt - ADAPTADO para avance individual
package com.charlesdev.icfes.student.duelo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.let
import kotlin.text.contains

@Composable
fun DuelModeRoot(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val controller = remember { DuelController(scope) }
    var screen by remember { mutableStateOf<Screen>(Screen.Entry) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isStartingGame by remember { mutableStateOf(false) }

    // ✅ SIMPLIFICADO: Observar cambios para auto-navegar al juego
    LaunchedEffect(screen) {
        if (screen is Screen.LobbyHost || screen is Screen.LobbyJoin) {
            val code = when (screen) {
                is Screen.LobbyHost -> (screen as Screen.LobbyHost).code
                is Screen.LobbyJoin -> (screen as Screen.LobbyJoin).code
                else -> null
            }

            code?.let { matchCode ->
                // ✅ SIMPLIFICADO: Solo observar el estado started
                controller.myMatchFlow?.collect { match ->
                    if (match?.started == true && !isStartingGame) {
                        // Auto-navegar al juego cuando started = true
                        screen = Screen.Game(matchCode)
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            when (val s = screen) {
                is Screen.LobbyHost -> controller.cleanup(s.code)
                is Screen.LobbyJoin -> controller.cleanup(s.code)
                is Screen.Game -> controller.cleanup(s.code)
                else -> {}
            }
        }
    }

    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (val s = screen) {
            is Screen.Entry -> EntryScreen(
                navController = navController,
                controller = controller,
                onNavigate = { newScreen -> screen = newScreen },
                onError = { error -> errorMessage = error }
            )
            is Screen.LobbyHost -> LobbyScreen(
                s.code, true, controller, navController,
                onStart = {
                    scope.launch {
                        isStartingGame = true
                        try {
                            controller.service.startGame(s.code)
                            // ✅ MENSAJE: Específico para avance individual
                            errorMessage = "Iniciando duelo individual... Cada jugador tendrá su timer personal"
                            // El auto-navegación se hará cuando started = true
                        } catch (e: Exception) {
                            errorMessage = "Error iniciando juego: ${e.message}"
                        } finally {
                            isStartingGame = false
                        }
                    }
                },
                onGoGame = {
                    // Navegar manualmente al juego
                    screen = Screen.Game(s.code)
                }
            )
            is Screen.LobbyJoin -> LobbyScreen(
                s.code, false, controller, navController,
                onStart = {},
                onGoGame = {
                    // Navegar manualmente al juego
                    screen = Screen.Game(s.code)
                }
            )
            is Screen.Game -> GameScreen(s.code, controller, navController)
        }

        // ✅ MEJORADO: Mostrar mensajes de estado
        errorMessage?.let { message ->
            LaunchedEffect(message) {
                delay(if (message.contains("Iniciando")) 3000 else 5000) // ✅ Más tiempo para leer el mensaje del duelo individual
                errorMessage = null
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("Iniciando"))
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.contains("Iniciando")) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(12.dp))
                        }
                        Text(
                            text = message,
                            color = if (message.contains("Iniciando"))
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}