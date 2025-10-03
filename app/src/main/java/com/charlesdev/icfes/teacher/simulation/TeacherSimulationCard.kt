package com.charlesdev.icfes.teacher.simulation



import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.charlesdev.icfes.teacher.TeacherData
import com.charlesdev.icfes.teacher.simulation.TeacherSimulationManagerActivity
import com.charlesdev.icfes.teacher.simulation.TeacherSimulationViewModel
import com.charlesdev.icfes.ui.theme.IcfesTheme

/**
 * ===================================
 * 🎯 CARD DE SIMULACROS PREMIUM - DASHBOARD PROFESOR
 * ===================================
 * Componente integrado en TeacherDashboardScreen para acceso rápido
 */

@Composable
fun TeacherSimulationCard(
    teacherId: String,
    teacherName: String
) {
    val context = LocalContext.current
    var showQuickActions by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = {
            val intent = Intent(context, TeacherSimulationManagerActivity::class.java).apply {
                putExtra("teacher_id", teacherId)
                putExtra("teacher_name", teacherName)
            }
            context.startActivity(intent)
        },
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF9C27B0), // Púrpura premium
                            Color(0xFF673AB7)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                // Header con icono premium
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Icono principal
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "🎯 SIMULACROS PREMIUM",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Gestión completa ICFES para tus estudiantes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    // Badge premium
                    Surface(
                        color = Color(0xFFFFD700),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color(0xFF9C27B0)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "PREMIUM",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9C27B0)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Características principales
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FeatureItem(
                        icon = "📚",
                        label = "175",
                        description = "Preguntas"
                    )
                    FeatureItem(
                        icon = "⏰",
                        label = "4.5h",
                        description = "Duración"
                    )
                    FeatureItem(
                        icon = "🎯",
                        label = "5",
                        description = "Módulos"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Estadísticas rápidas (si hay datos)
                SimulationStatsRow(teacherId = teacherId)

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de acción rápida
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón principal
                    Button(
                        onClick = {
                            val intent = Intent(context, TeacherSimulationManagerActivity::class.java)
                            intent.putExtra("teacher_id", teacherId)
                            intent.putExtra("teacher_name", teacherName)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Gestionar",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Botón de creación rápida
                    OutlinedButton(
                        onClick = { showQuickActions = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Crear", color = Color.White)
                    }
                }

                // Acciones rápidas
                if (showQuickActions) {
                    QuickActionsMenu(
                        onDismiss = { showQuickActions = false },
                        teacherId = teacherId,
                        teacherName = teacherName
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureItem(icon: String, label: String, description: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            icon,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun SimulationStatsRow(teacherId: String) {
    val viewModel: TeacherSimulationViewModel = viewModel()
    var stats by remember { mutableStateOf(mapOf<String, Int>()) }

    LaunchedEffect(teacherId) {
        viewModel.simulations.collect { simulations ->
            stats = mapOf(
                "total" to simulations.size,
                "active" to simulations.count { it.isActive },
                "students" to 45 // Estimado, se puede obtener de Firebase
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatBubble(
            value = "${stats["total"] ?: 0}",
            label = "Creados",
            color = Color.White.copy(alpha = 0.2f)
        )
        StatBubble(
            value = "${stats["active"] ?: 0}",
            label = "Activos",
            color = Color.White.copy(alpha = 0.2f)
        )
        StatBubble(
            value = "${stats["students"] ?: 0}",
            label = "Estudiantes",
            color = Color.White.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun StatBubble(value: String, label: String, color: Color) {
    Surface(
        color = color,
        shape = RoundedCornerShape(50)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}

@Composable
fun QuickActionsMenu(
    onDismiss: () -> Unit,
    teacherId: String,
    teacherName: String
) {
    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Default.Create,
                    label = "Crear Nuevo",
                    onClick = {
                        val intent = Intent(context, TeacherSimulationManagerActivity::class.java)
                        intent.putExtra("teacher_id", teacherId)
                        intent.putExtra("teacher_name", teacherName)
                        intent.putExtra("mode", "create")
                        context.startActivity(intent)
                        onDismiss()
                    }
                )

                QuickActionButton(
                    icon = Icons.Default.Analytics,
                    label = "Ver Análisis",
                    onClick = {
                        val intent = Intent(context, TeacherSimulationManagerActivity::class.java)
                        intent.putExtra("teacher_id", teacherId)
                        intent.putExtra("teacher_name", teacherName)
                        intent.putExtra("mode", "analytics")
                        context.startActivity(intent)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = Color.White
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
    }
}

// ===================================
// 🎯 INTEGRACIÓN EN TeacherDashboardScreen.kt
// ===================================

// 📁 Agregar esta función en TeacherDashboardScreen.kt

@Composable
fun TeacherDashboardAddSimulationSection(teacherData: TeacherData) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "🎯 Herramientas Avanzadas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        TeacherSimulationCard(
            teacherId = teacherData.email, // o usar ID real
            teacherName = teacherData.nombre
        )
    }
}