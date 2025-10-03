package com.charlesdev.icfes.student.simulation

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.BorderStroke
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
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * ===================================
 * 📁 PANTALLAS DE BREAK Y RESULTADOS DEL SIMULACRO
 * ===================================
 * Completa la experiencia del simulacro ICFES
 */

// ✅ PANTALLA DE BREAK ENTRE SESIONES
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ICFESBreakScreen(
    currentSession: Int,
    totalSessions: Int,
    breakTimeRemaining: Long,
    onFinishBreak: () -> Unit,
    onPause: () -> Unit
) {
    val nextSessionNumber = currentSession + 2 // +1 para índice, +1 para siguiente
    val completedSessions = currentSession + 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Break - Sesión $completedSessions de $totalSessions completada",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header del break
            item {
                ICFESBreakHeader(
                    completedSessions = completedSessions,
                    totalSessions = totalSessions,
                    timeRemaining = breakTimeRemaining
                )
            }

            // Progreso del simulacro
            item {
                ICFESSimulationProgress(
                    completedSessions = completedSessions,
                    totalSessions = totalSessions
                )
            }

            // Información de la siguiente sesión
            if (nextSessionNumber <= totalSessions) {
                item {
                    ICFESNextSessionInfo(nextSessionNumber = nextSessionNumber)
                }
            }

            // Recomendaciones para el break
            item {
                ICFESBreakRecommendations()
            }

            // Motivación
            item {
                ICFESBreakMotivation(
                    completedSessions = completedSessions,
                    totalSessions = totalSessions
                )
            }

            // Botones de acción
            item {
                ICFESBreakActions(
                    timeRemaining = breakTimeRemaining,
                    onFinishBreak = onFinishBreak,
                    onPause = onPause,
                    canFinishEarly = breakTimeRemaining > 5 * 60 * 1000L // 5 minutos
                )
            }
        }
    }
}

// ✅ HEADER DEL BREAK
@Composable
fun ICFESBreakHeader(
    completedSessions: Int,
    totalSessions: Int,
    timeRemaining: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono de break
            Icon(
                Icons.Default.Coffee,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "☕ Tiempo de Descanso",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )

            Text(
                "¡Excelente trabajo! Has completado $completedSessions de $totalSessions sesiones",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Timer del break
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Tiempo restante:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        formatBreakTime(timeRemaining),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

// ✅ PROGRESO DEL SIMULACRO
@Composable
fun ICFESSimulationProgress(
    completedSessions: Int,
    totalSessions: Int
) {
    Card {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "📊 Progreso del Simulacro",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val progress = completedSessions.toFloat() / totalSessions

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Sesiones completadas:",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "$completedSessions/$totalSessions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF4CAF50),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "${((progress * 100).toInt())}% del simulacro completado",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ✅ INFORMACIÓN DE LA SIGUIENTE SESIÓN
@Composable
fun ICFESNextSessionInfo(nextSessionNumber: Int) {
    val nextSessionInfo = when (nextSessionNumber) {
        1 -> Triple("📚 Lectura Crítica", "Comprensión e interpretación de textos", Color(0xFF2196F3))
        2 -> Triple("🔢 Matemáticas", "Razonamiento cuantitativo", Color(0xFFFF9800))
        3 -> Triple("🧪 Ciencias Naturales", "Conocimiento científico", Color(0xFF4CAF50))
        4 -> Triple("🏛️ Sociales y Ciudadanas", "Pensamiento social", Color(0xFF9C27B0))
        5 -> Triple("🇺🇸 Inglés", "Comunicación en inglés", Color(0xFFF44336))
        else -> Triple("🎯 Simulacro", "Módulo siguiente", Color(0xFF607D8B))
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = nextSessionInfo.third.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "🎯 Siguiente Sesión",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = nextSessionInfo.third,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = nextSessionInfo.third.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$nextSessionNumber",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = nextSessionInfo.third
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        nextSessionInfo.first,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        nextSessionInfo.second,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (nextSessionNumber == 5) "60 minutos • 35 preguntas" else "65 minutos • 35 preguntas",
                        style = MaterialTheme.typography.bodySmall,
                        color = nextSessionInfo.third
                    )
                }
            }
        }
    }
}

// ✅ RECOMENDACIONES PARA EL BREAK
@Composable
fun ICFESBreakRecommendations() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "💡 Aprovecha el Descanso",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2196F3),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val recommendations = listOf(
                "💧 Hidrata tu cuerpo con agua",
                "🍎 Come un snack ligero si tienes hambre",
                "🚶 Camina un poco o estira las piernas",
                "👀 Descansa la vista mirando a la distancia",
                "🧘 Respira profundo y relájate",
                "📱 Evita usar redes sociales o contenido distractor",
                "📚 NO estudies durante el break - descansa la mente"
            )

            recommendations.forEach { recommendation ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.padding(end = 8.dp, top = 2.dp)
                    )
                    Text(
                        recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ✅ MOTIVACIÓN DURANTE EL BREAK
@Composable
fun ICFESBreakMotivation(
    completedSessions: Int,
    totalSessions: Int
) {
    val motivationalMessage = when (completedSessions) {
        1 -> "🚀 ¡Excelente inicio! Has superado la primera sesión con éxito."
        2 -> "💪 ¡Vas por la mitad! Tu constancia está dando frutos."
        3 -> "🔥 ¡Más de la mitad completado! La meta está cada vez más cerca."
        4 -> "🌟 ¡Solo falta una sesión! Estás a punto de lograr algo grande."
        else -> "🎯 ¡Cada sesión completada te acerca más a tu meta!"
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "✨ Mensaje Motivacional",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                motivationalMessage,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Recuerda: cada pregunta respondida es un paso hacia tu futuro académico.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

// ✅ ACCIONES DEL BREAK
@Composable
fun ICFESBreakActions(
    timeRemaining: Long,
    onFinishBreak: () -> Unit,
    onPause: () -> Unit,
    canFinishEarly: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Botón principal - Continuar
        if (timeRemaining <= 0) {
            Button(
                onClick = onFinishBreak,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Continuar con Siguiente Sesión",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else if (canFinishEarly) {
            // Opción de terminar break temprano
            OutlinedButton(
                onClick = onFinishBreak,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF4CAF50)
                )
            ) {
                Icon(
                    Icons.Default.FastForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Terminar Descanso Ahora")
            }
        }

        // Botón de pausa (opcional)
        OutlinedButton(
            onClick = onPause,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
                Icons.Default.Pause,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pausar Break")
        }

        // Información importante
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "El break terminará automáticamente cuando se acabe el tiempo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

// ✅ PANTALLA DE RESULTADOS FINALES
// ===================================
// 🔊 MODIFICAR ICFESSimulationResultsScreen
// ===================================
// En ICFESSimulationBreakAndResult.kt - AGREGAR TTS

@Composable
fun ICFESSimulationResultsScreen(
    results: ICFESSimulationCompleteResult?,
    isGenerating: Boolean,
    onRetry: () -> Unit,
    onFinish: () -> Unit
) {
    // ✅ AGREGAR TTS STATE
    val context = LocalContext.current
    val tts = remember { TextToSpeech(context, null) }
    var isPlayingAudio by remember { mutableStateOf(false) }

    // ✅ CONFIGURAR TTS
    LaunchedEffect(Unit) {
        tts.language = Locale("es", "CO")
        tts.setSpeechRate(0.8f)
        tts.setPitch(1.0f)
    }

    // ✅ CLEANUP TTS
    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
            isPlayingAudio = false
        }
    }

    // ✅ USAR SCAFFOLD CON BOTTOM BAR FIJO
    Scaffold(
        bottomBar = {
            // Solo mostrar barra inferior si hay resultados completados
            if (!isGenerating && results != null) {
                ICFESResultsActionsWithTTS(
                    results = results,
                    tts = tts,
                    isPlayingAudio = isPlayingAudio,
                    onAudioStateChange = { isPlayingAudio = it },
                    onRetry = onRetry,
                    onFinish = onFinish
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp // Espacio para bottom bar
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isGenerating) {
                item {
                    ICFESGeneratingResultsCard()
                }
            } else if (results != null) {
                // Header de felicitaciones
                item {
                    ICFESResultsHeader(results)
                }

                // Puntaje global
                item {
                    ICFESGlobalScoreCard(results)
                }

                // Resultados por sesión
                item {
                    ICFESSessionResultsCard(results.sessionResults)
                }

                // Comparación nacional
                item {
                    ICFESNationalComparisonCard(results.comparison)
                }

                // Análisis detallado
                item {
                    ICFESDetailedAnalysisCard(results.detailedAnalysis)
                }

                // Plan de estudio
                item {
                    ICFESStudyPlanCard(results.detailedAnalysis.studyPlan)
                }

                // ✅ AGREGAR INFORMACIÓN DEL CERTIFICADO SI APLICA
                if (results.certificateEligible) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "🏆 ¡Felicidades! Tu excelente puntaje te hace elegible para certificado de participación.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // ✅ MENSAJE FINAL MOTIVACIONAL
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            "🎯 Usa los controles de audio de abajo para escuchar tu análisis completo mientras revisas los resultados detallados.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            } else {
                item {
                    ICFESErrorResultsCard(onRetry)
                }
            }
        }
    }
}

// ===================================
// 🎯 NUEVO COMPONENTE - Botones con TTS
// ===================================

@Composable
fun ICFESResultsActionsWithTTS(
    results: ICFESSimulationCompleteResult,
    tts: TextToSpeech,
    isPlayingAudio: Boolean,
    onAudioStateChange: (Boolean) -> Unit,
    onRetry: () -> Unit,
    onFinish: () -> Unit
) {
    // ✅ CONTENEDOR PRINCIPAL CON FONDO DESTACADO
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ✅ SEPARADOR VISUAL
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            // ✅ BOTÓN DE AUDIO - IGUAL AL DE EVALUACIONES
            ElevatedButton(
                onClick = {
                    if (isPlayingAudio) {
                        tts.stop()
                        onAudioStateChange(false)
                    } else {
                        val audioText = generateSimulationResultsAudio(results)

                        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                onAudioStateChange(true)
                            }
                            override fun onDone(utteranceId: String?) {
                                onAudioStateChange(false)
                            }
                            override fun onError(utteranceId: String?) {
                                onAudioStateChange(false)
                            }
                        })

                        val utteranceId = "simulation_results_${System.currentTimeMillis()}"
                        val params = Bundle().apply {
                            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
                        }
                        tts.speak(audioText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
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
                        if (isPlayingAudio) "🔴 Detener Audio" else "🔊 Escuchar Análisis Completo",
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

            // ✅ BOTONES FINALES EN LA MISMA LÍNEA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón Nuevo Simulacro
                OutlinedButton(
                    onClick = {
                        tts.stop()
                        onAudioStateChange(false)
                        onRetry()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Nuevo Simulacro",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Botón Finalizar - Principal
                Button(
                    onClick = {
                        tts.stop()
                        onAudioStateChange(false)
                        onFinish()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Finalizar",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ✅ INFORMACIÓN DEL CERTIFICADO (si aplica)
            if (results.certificateEligible) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "🏆 ¡Felicidades! Tu puntaje te hace elegible para certificado de participación.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ===================================
// 🎙️ FUNCIÓN PARA GENERAR AUDIO COMPLETO
// ===================================

fun generateSimulationResultsAudio(results: ICFESSimulationCompleteResult): String {
    return buildString {
        append("Felicidades por completar tu simulacro completo ICFES de 4 horas y 30 minutos. ")
        append("Obtuviste un puntaje global de ${results.globalScore} sobre 500 puntos, ")
        append("equivalente al ${results.globalPercentage.toInt()} por ciento de respuestas correctas. ")
        append("Tu nivel general es ${results.globalLevel}. ")

        // Percentil nacional
        append("Te ubicas en el percentil ${results.nationalPercentile}, ")
        append("lo que significa que tu rendimiento es ${results.comparison.comparison.lowercase()}. ")

        // Resultados por módulo
        append("Resultados por módulo: ")
        results.sessionResults.forEach { session ->
            val nivel = when {
                session.percentage >= 80 -> "excelente"
                session.percentage >= 70 -> "muy bueno"
                session.percentage >= 60 -> "bueno"
                else -> "necesita mejora"
            }
            append("${session.moduleName}: ${session.icfesScore} puntos, nivel ${nivel}. ")
        }

        // Fortalezas principales
        if (results.detailedAnalysis.strengths.isNotEmpty()) {
            append("Tus principales fortalezas identificadas son: ")
            append(results.detailedAnalysis.strengths.take(3).joinToString(", "))
            append(". ")
        }

        // Áreas de mejora
        if (results.detailedAnalysis.improvementAreas.isNotEmpty()) {
            append("Las áreas que necesitan mayor atención son: ")
            append(results.detailedAnalysis.improvementAreas.take(3).joinToString(", "))
            append(". ")
        }

        // Plan de estudio
        if (results.detailedAnalysis.studyPlan.isNotEmpty()) {
            val priority = results.detailedAnalysis.studyPlan.find { it.priority == StudyPriority.HIGH }
            priority?.let {
                append("Tu prioridad de estudio más importante es ${it.moduleName}, ")
                append("con ${it.suggestedHours} horas recomendadas. ")
            }
        }

        // Mensaje motivacional
        append(results.detailedAnalysis.motivationalMessage)

        append(" Continúa preparándote con dedicación para alcanzar tus metas en el examen ICFES oficial.")
    }.let { text ->
        // ✅ LIMPIAR TEXTO PARA TTS (sin emojis)
        text.replace(Regex("[\uD83C-\uDBFF\uDC00-\uDFFF]+"), "")
            .replace("🏆", "")
            .replace("📊", "")
            .replace("🇨🇴", "")
            .replace("🎯", "")
            .replace("📚", "")
            .replace("🔢", "")
            .replace("🧪", "")
            .replace("🏛️", "")
            .replace("🇺🇸", "")
            .replace("🔴", "")
            .replace("🔊", "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}

// ===================================
// 🗑️ REEMPLAZAR LA FUNCIÓN ORIGINAL
// ===================================

// ❌ ELIMINAR esta función original de ICFESResultsActions
// ✅ Ya no se necesita porque usamos ICFESResultsActionsWithTTS

// ✅ CARD DE GENERANDO RESULTADOS
@Composable
fun ICFESGeneratingResultsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                strokeWidth = 6.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "🤖 Generando Análisis Completo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "La IA está analizando tu simulacro completo para generar recomendaciones personalizadas...",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
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
                    "Esto puede tomar hasta 30 segundos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ✅ HEADER DE RESULTADOS
@Composable
fun ICFESResultsHeader(results: ICFESSimulationCompleteResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "🎉 ¡Simulacro Completado!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )

            Text(
                "Has terminado exitosamente el simulacro completo ICFES",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            val hours = TimeUnit.MILLISECONDS.toHours(results.totalTimeSpent)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(results.totalTimeSpent) % 60

            Text(
                "Tiempo total: ${hours}h ${minutes}m",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ✅ PUNTAJE GLOBAL
@Composable
fun ICFESGlobalScoreCard(results: ICFESSimulationCompleteResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "🎯 Puntaje Global ICFES",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                "${results.globalScore}",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                "/ 500 puntos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${results.sessionResults.sumOf { it.correctAnswers }}/${results.sessionResults.sumOf { it.totalQuestions }}",
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
                        "${"%.1f".format(results.globalPercentage)}%",
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
                        results.globalLevel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (results.globalLevel) {
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

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${results.nationalPercentile}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9C27B0)
                    )
                    Text(
                        "Percentil",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// ✅ RESULTADOS POR SESIÓN
@Composable
fun ICFESSessionResultsCard(sessionResults: List<SimulationSessionResult>) {
    Card {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "📊 Resultados por Módulo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            sessionResults.forEach { result ->
                SessionResultItem(result)
                if (result != sessionResults.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SessionResultItem(result: SimulationSessionResult) {
    val moduleColor = when (result.moduleId) {
        "lectura_critica" -> Color(0xFF2196F3)
        "matematicas" -> Color(0xFFFF9800)
        "ciencias_naturales" -> Color(0xFF4CAF50)
        "sociales_ciudadanas" -> Color(0xFF9C27B0)
        "ingles" -> Color(0xFFF44336)
        else -> Color(0xFF607D8B)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = moduleColor.copy(alpha = 0.1f)
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
                    result.moduleName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${result.correctAnswers}/${result.totalQuestions} correctas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "${result.icfesScore}/500",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = moduleColor
                )
                Text(
                    "${"%.1f".format(result.percentage)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ✅ COMPARACIÓN NACIONAL
@Composable
fun ICFESNationalComparisonCard(comparison: ICFESNationalComparison) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "🇨🇴 Comparación Nacional",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2196F3),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                comparison.comparison,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ComparisonItem("Nacional", "${comparison.nationalAverage}")
                ComparisonItem("Santander", "${comparison.regionAverage}")
                ComparisonItem("Tu Percentil", "${comparison.percentilePosition}%")
            }
        }
    }
}

@Composable
fun ComparisonItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2196F3)
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ✅ ANÁLISIS DETALLADO
@Composable
fun ICFESDetailedAnalysisCard(analysis: SimulationAnalysis) {
    Card {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "🧠 Análisis Personalizado",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Fortalezas
            if (analysis.strengths.isNotEmpty()) {
                AnalysisSection(
                    title = "✅ Fortalezas",
                    items = analysis.strengths,
                    color = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Debilidades
            if (analysis.weaknesses.isNotEmpty()) {
                AnalysisSection(
                    title = "📈 Áreas de Mejora",
                    items = analysis.weaknesses,
                    color = Color(0xFFFF9800)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Mensaje motivacional
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    analysis.motivationalMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AnalysisSection(
    title: String,
    items: List<String>,
    color: Color
) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    items.forEach { item ->
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
                item,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ✅ PLAN DE ESTUDIO
@Composable
fun ICFESStudyPlanCard(studyPlan: List<StudyRecommendation>) {
    if (studyPlan.isNotEmpty()) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF9C27B0).copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    "📚 Plan de Estudio Personalizado",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                studyPlan.forEach { recommendation ->
                    StudyRecommendationItem(recommendation)
                    if (recommendation != studyPlan.last()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StudyRecommendationItem(recommendation: StudyRecommendation) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = recommendation.priority.color.let { Color(it) }.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    recommendation.moduleName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(recommendation.priority.color)
                    )
                ) {
                    Text(
                        recommendation.priority.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Text(
                "📖 ${recommendation.suggestedHours} horas sugeridas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                "🎯 ${recommendation.practiceType}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ✅ ACCIONES FINALES
@Composable
fun ICFESResultsActions(
    onRetry: () -> Unit,
    onFinish: () -> Unit,
    certificateEligible: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Botón principal
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Finalizar Simulacro",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Botón secundario
        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Nuevo Simulacro Completo")
        }

        // Información del certificado
        if (certificateEligible) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "🏆 ¡Felicidades! Tu puntaje te hace elegible para certificado de participación.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ✅ ERROR EN RESULTADOS
@Composable
fun ICFESErrorResultsCard(onRetry: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFFF44336)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Error al generar resultados",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF44336)
            )

            Text(
                "Hubo un problema al procesar tu simulacro",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF44336)
                )
            ) {
                Text("Reintentar")
            }
        }
    }
}

// ✅ FUNCIÓN DE UTILIDAD PARA FORMATEAR TIEMPO DEL BREAK
fun formatBreakTime(timeMs: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMs) % 60
    return String.format("%02d:%02d", minutes, seconds)
}