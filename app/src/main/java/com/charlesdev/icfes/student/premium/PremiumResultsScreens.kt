package com.charlesdev.icfes.student.premium

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import java.util.*
import java.util.concurrent.TimeUnit

// ✅ DIÁLOGO PRINCIPAL DE RESULTADOS PREMIUM (COMO ICFESEvaluationSummaryDialog)
@Composable
fun PremiumEvaluationSummaryDialog(
    viewModel: PremiumQuizViewModel,
    result: PremiumEvaluationResult,
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

        // ✅ ÍCONO PREMIUM DE EVALUACIÓN
        icon = {
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFF7B1FA2)
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

        // ✅ TÍTULO PREMIUM
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "⭐ Evaluación Premium Completada",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF7B1FA2)
                    )
                }
                Text(
                    "${result.moduleName} • Prof. ${result.teacherName}",
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
                    .heightIn(max = 400.dp)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 6.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    // ✅ PUNTAJE PRINCIPAL PREMIUM
                    item {
                        PremiumScoreCard(result = result)
                    }

                    // ✅ COMPARACIÓN CON ICFES (SI EXISTE)
                    result.comparedToICFES?.let { comparison ->
                        item {
                            ICFESComparisonCard(comparison = comparison)
                        }
                    }

                    // ✅ ANÁLISIS PERSONALIZADO
                    item {
                        PremiumAnalysisCard(
                            title = "📝 Análisis Personalizado",
                            content = result.analisisPersonalizado,
                            icon = Icons.Default.Psychology,
                            color = Color(0xFF7B1FA2)
                        )
                    }

                    // ✅ FORTALEZAS PREMIUM
                    if (result.fortalezas.isNotEmpty()) {
                        item {
                            PremiumStrengthsCard(fortalezas = result.fortalezas)
                        }
                    }

                    // ✅ RECOMENDACIONES DEL PROFESOR
                    if (result.recomendacionesProfesor.isNotEmpty()) {
                        item {
                            PremiumTeacherRecommendationsCard(
                                recomendaciones = result.recomendacionesProfesor,
                                teacherName = result.teacherName
                            )
                        }
                    }

                    // ✅ ESTRATEGIAS PREMIUM
                    if (result.estrategiasPremium.isNotEmpty()) {
                        item {
                            PremiumStrategiesCard(estrategias = result.estrategiasPremium)
                        }
                    }
                }
            }
        },

        // ✅ BOTONES - ÁREA FIJA Y MUY VISIBLE
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // ✅ SEPARADOR VISUAL
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )

                    // ✅ BOTÓN DE AUDIO PREMIUM
                    ElevatedButton(
                        onClick = {
                            if (isPlayingAudio) {
                                tts.stop()
                                isPlayingAudio = false
                            } else {
                                val feedbackText = buildString {
                                    append("Análisis premium de tu evaluación de ${result.moduleName}. ")
                                    append("Preparada por el profesor ${result.teacherName}. ")
                                    append("Obtuviste ${result.puntajePremium} puntos premium, ")
                                    append("con ${result.correctAnswers} respuestas correctas de ${result.totalQuestions}. ")
                                    append("Tu nivel es ${result.nivel}. ")
                                    append(result.analisisPersonalizado)

                                    if (result.fortalezas.isNotEmpty()) {
                                        append(" Tus fortalezas incluyen: ")
                                        append(result.fortalezas.joinToString(", "))
                                    }

                                    if (result.recomendacionesProfesor.isNotEmpty()) {
                                        append(" El profesor recomienda: ")
                                        append(result.recomendacionesProfesor.take(3).joinToString(", "))
                                    }
                                }

                                val cleanText = viewModel.cleanPremiumTextForTTS(feedbackText)

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

                                val utteranceId = "premium_evaluation_${System.currentTimeMillis()}"
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
                                if (isPlayingAudio) "🔴 Detener Audio" else "🔊 Escuchar Análisis Premium",
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

                    // ✅ BOTONES DE ACCIÓN PREMIUM
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                tts.stop()
                                isPlayingAudio = false
                                onRetry()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF7B1FA2)
                            ),
                            border = BorderStroke(1.5.dp, Color(0xFF7B1FA2))
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
                                    "Nueva Evaluación",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Button(
                            onClick = {
                                tts.stop()
                                isPlayingAudio = false
                                onFinish()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7B1FA2)
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

// ✅ COMPONENTE PARA PUNTAJE PREMIUM
@Composable
fun PremiumScoreCard(result: PremiumEvaluationResult) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF7B1FA2).copy(alpha = 0.1f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Indicador Premium
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFFFD700)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "PUNTAJE PREMIUM",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7B1FA2)
                )
            }

            Text(
                "${result.puntajePremium}",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )
            Text(
                "de 100 puntos premium",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PremiumStatItem(
                    value = "${result.correctAnswers}/${result.totalQuestions}",
                    label = "Correctas",
                    color = Color(0xFF4CAF50)
                )

                PremiumStatItem(
                    value = "${"%.1f".format(result.percentage)}%",
                    label = "Porcentaje",
                    color = Color(0xFF2196F3)
                )

                PremiumStatItem(
                    value = result.nivel,
                    label = "Nivel Premium",
                    color = when (result.nivel) {
                        "Excelente" -> Color(0xFF4CAF50)
                        "Muy Bueno" -> Color(0xFF2196F3)
                        "Bueno" -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val hours = TimeUnit.MILLISECONDS.toHours(result.timeSpent)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(result.timeSpent) % 60
            Text(
                "Tiempo: ${if (hours > 0) "${hours}h " else ""}${minutes}m",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun PremiumStatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

// ✅ CONTINÚA EN PARTE 2...
// ✅ CONTINUACIÓN DE PARTE 1 - COMPONENTES ESPECÍFICOS PREMIUM

// ✅ COMPARACIÓN CON ICFES BÁSICO
@Composable
fun ICFESComparisonCard(comparison: PremiumICFESComparison) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when {
                comparison.improvement > 10 -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                comparison.improvement > 0 -> Color(0xFFFF9800).copy(alpha = 0.1f)
                else -> Color(0xFFF44336).copy(alpha = 0.1f)
            }
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
                    Icons.Default.CompareArrows,
                    contentDescription = null,
                    tint = Color(0xFF7B1FA2),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "📊 Comparación Premium vs ICFES Básico",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7B1FA2)
                )
            }

            // Comparación visual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ICFES Básico
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "ICFES Básico",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            "${comparison.icfesScore}/500",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Flecha y mejora
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        if (comparison.improvement > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = when {
                            comparison.improvement > 10 -> Color(0xFF4CAF50)
                            comparison.improvement > 0 -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }
                    )
                    Text(
                        "${if (comparison.improvement > 0) "+" else ""}${comparison.improvement}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            comparison.improvement > 10 -> Color(0xFF4CAF50)
                            comparison.improvement > 0 -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }
                    )
                }

                // Premium
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Premium",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF7B1FA2),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF7B1FA2).copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            "${comparison.premiumScore}/100",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp),
                            color = Color(0xFF7B1FA2)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recomendación
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    comparison.recommendation,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ✅ ANÁLISIS PERSONALIZADO PREMIUM
@Composable
fun PremiumAnalysisCard(
    title: String,
    content: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = Color(0xFF7B1FA2)
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
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
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
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

// ✅ FORTALEZAS PREMIUM
@Composable
fun PremiumStrengthsCard(fortalezas: List<String>) {
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
                    "💪 Fortalezas Identificadas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = Color(0xFFFFD700),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "PREMIUM",
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7B1FA2)
                    )
                }
            }

            fortalezas.forEachIndexed { index, fortaleza ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        fortaleza,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (index < fortalezas.size - 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

// ✅ RECOMENDACIONES DEL PROFESOR
@Composable
fun PremiumTeacherRecommendationsCard(
    recomendaciones: List<String>,
    teacherName: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
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
                    Icons.Default.School,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "🎓 Recomendaciones del Profesor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                    Text(
                        "Prof. $teacherName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            recomendaciones.forEachIndexed { index, recomendacion ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Surface(
                        color = Color(0xFF2196F3),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        recomendacion,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (index < recomendaciones.size - 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

// ✅ ESTRATEGIAS PREMIUM
@Composable
fun PremiumStrategiesCard(estrategias: List<String>) {
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
                    Icons.Default.Psychology,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "🧠 Estrategias Premium",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = Color(0xFFFFD700),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "EXCLUSIVO",
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7B1FA2)
                    )
                }
            }

            estrategias.forEach { estrategia ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        estrategia,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

// ✅ DIÁLOGO DE CARGA PARA EVALUACIÓN PREMIUM
@Composable
fun PremiumEvaluationLoadingDialog() {
    AlertDialog(
        onDismissRequest = { /* No permitir cerrar mientras se genera */ },
        icon = {
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp,
                    color = Color(0xFF7B1FA2)
                )
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF7B1FA2)
                )
            }
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "🔮 Generando Análisis Premium",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7B1FA2)
                )
                Text(
                    "con Inteligencia Artificial",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "La IA está analizando tu evaluación premium...",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Personalizando feedback con el enfoque del profesor",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Indicadores de progreso premium
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PremiumLoadingStep("Análisis", true)
                    PremiumLoadingStep("Personalización", true)
                    PremiumLoadingStep("Comparación", false)
                }
            }
        },
        confirmButton = { /* Vacío para no mostrar botón */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )
}

@Composable
fun PremiumLoadingStep(
    label: String,
    isActive: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    if (isActive) Color(0xFF7B1FA2) else Color(0xFF7B1FA2).copy(alpha = 0.3f),
                    RoundedCornerShape(50)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isActive) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 1.dp,
                    color = Color(0xFF7B1FA2)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) Color(0xFF7B1FA2) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ✅ CONTINÚA EN PARTE 3...