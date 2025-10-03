package com.charlesdev.icfes.student

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.charlesdev.icfes.student.screens.*
import com.charlesdev.icfes.student.simulation.ICFESSimulationScreen
import com.charlesdev.icfes.ui.theme.IcfesTheme

class StudentICFESActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IcfesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ICFESStudentApp()
                }
            }
        }
    }
}

@Composable
fun ICFESStudentApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // 🏠 Pantalla principal (Dashboard ICFES)
        composable("home") {
            ICFESHomeScreen(
                navController = navController,
                onNavigateToQuiz = { moduleId, sessionType ->
                    navController.navigate("quiz/$moduleId/$sessionType")
                }
            )
        }

        // 📚 Selección de módulos
        composable("modules") {
            ICFESModuleSelectionScreen(
                navController = navController,
                onNavigateToQuiz = { moduleId, sessionType ->
                    navController.navigate("quiz/$moduleId/$sessionType")
                }
            )
        }

        // 📝 Quiz por módulo y tipo de sesión (módulos individuales)
        composable("quiz/{moduleId}/{sessionType}") { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
            val sessionType = backStackEntry.arguments?.getString("sessionType") ?: "practice"

            ICFESQuizScreen(
                navController = navController,
                moduleId = moduleId,
                sessionType = sessionType,
                onQuizComplete = {
                    // Navegación para módulos individuales
                    navController.navigate("modules") {
                        popUpTo("quiz/$moduleId/$sessionType") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // 🎯 SIMULACRO COMPLETO ICFES - NUEVA EXPERIENCIA INDEPENDIENTE
        composable("simulation") {
            ICFESSimulationScreen(
                navController = navController,
                onSimulationComplete = {
                    // Volver al home después del simulacro completo
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        // 📊 Progreso detallado
        composable("progress") {
            ICFESProgressScreen(navController = navController)
        }

        // 🏆 Logros y badges
        composable("achievements") {
            ICFESAchievementsScreen(navController = navController)
        }

        // 👤 Perfil del estudiante
        composable("profile") {
            ICFESProfileScreen(navController = navController)
        }

        // ⚙️ Configuración
        composable("settings") {
            ICFESSettingsScreen(navController = navController)
        }

        // 📖 Detalles de un módulo específico
        composable("module/{moduleId}") { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
            ICFESModuleDetailScreen(
                navController = navController,
                moduleId = moduleId
            )
        }

        // 🔍 RUTAS ADICIONALES PARA SIMULACRO (Opcionales - para futuras funcionalidades)

        // Historial de simulacros
        composable("simulation_history") {
            ICFESSimulationHistoryScreen(navController = navController)
        }

        // Análisis detallado de un simulacro específico
        composable("simulation_analysis/{simulationId}") { backStackEntry ->
            val simulationId = backStackEntry.arguments?.getString("simulationId") ?: ""
            ICFESSimulationAnalysisScreen(
                navController = navController,
                simulationId = simulationId
            )
        }
    }
}

// ✅ PANTALLAS EXISTENTES (mantener como están)

@Composable
fun ICFESProgressScreen(navController: NavHostController) {
    ICFESPlaceholderScreen(
        navController = navController,
        title = "📊 Progreso Detallado",
        description = "Análisis completo de tu rendimiento en cada módulo ICFES",
        icon = "📈"
    )
}

@Composable
fun ICFESAchievementsScreen(navController: NavHostController) {
    ICFESPlaceholderScreen(
        navController = navController,
        title = "🏆 Logros y Badges",
        description = "Desbloquea logros mientras mejoras tus habilidades ICFES",
        icon = "🌟"
    )
}

@Composable
fun ICFESProfileScreen(navController: NavHostController) {
    ICFESPlaceholderScreen(
        navController = navController,
        title = "👤 Perfil de Estudiante",
        description = "Gestiona tu información personal y objetivos ICFES",
        icon = "🎓"
    )
}

@Composable
fun ICFESSettingsScreen(navController: NavHostController) {
    ICFESPlaceholderScreen(
        navController = navController,
        title = "⚙️ Configuración",
        description = "Personaliza tu experiencia de aprendizaje",
        icon = "🔧"
    )
}

@Composable
fun ICFESModuleDetailScreen(navController: NavHostController, moduleId: String) {
    ICFESPlaceholderScreen(
        navController = navController,
        title = "📖 Detalle del Módulo",
        description = "Información detallada sobre el módulo: $moduleId",
        icon = "📚"
    )
}

// ✅ NUEVAS PANTALLAS PLACEHOLDER PARA SIMULACRO (Futuras funcionalidades)

@Composable
fun ICFESSimulationHistoryScreen(navController: NavHostController) {
    ICFESPlaceholderScreen(
        navController = navController,
        title = "📋 Historial de Simulacros",
        description = "Revisa todos tus simulacros anteriores y compara tu progreso",
        icon = "📊"
    )
}

@Composable
fun ICFESSimulationAnalysisScreen(navController: NavHostController, simulationId: String) {
    ICFESPlaceholderScreen(
        navController = navController,
        title = "🔍 Análisis Detallado",
        description = "Análisis profundo del simulacro: $simulationId",
        icon = "📈"
    )
}

// ✅ PANTALLA PLACEHOLDER REUTILIZABLE (mantener igual)

@Composable
fun ICFESPlaceholderScreen(
    navController: NavHostController,
    title: String,
    description: String,
    icon: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "🚀 Próximamente disponible",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(
            onClick = { navController.navigateUp() }
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Volver")
        }
    }
}