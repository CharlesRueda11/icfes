package com.charlesdev.icfes.teacher.simulation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.charlesdev.icfes.teacher.practice_evaluation.TeacherQuestion
import com.charlesdev.icfes.ui.theme.IcfesTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ===================================
// 🎯 ACTIVITY PRINCIPAL - GESTIÓN DE SIMULACROS
// ===================================

class TeacherSimulationManagerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val teacherId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        setContent {
            IcfesTheme {
                TeacherSimulationManagerScreen(teacherId = teacherId)
            }
        }
    }
}

// ===================================
// 🎯 SCREEN PRINCIPAL
// ===================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherSimulationManagerScreen(teacherId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: TeacherSimulationViewModel = viewModel()

    // Estados
    var simulations by remember { mutableStateOf<List<TeacherSimulation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCreateDialog by remember { mutableStateOf(false) }

    // Cargar simulacros del profesor
    LaunchedEffect(teacherId) {
        viewModel.loadTeacherSimulations(teacherId)
        viewModel.simulations.collect { simList ->
            simulations = simList
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Gestión de Simulacros Premium",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Crea y gestiona simulacros personalizados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as ComponentActivity).finish()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF9C27B0).copy(alpha = 0.1f)
                ),
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Crear nuevo")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Crear Simulacro") },
                containerColor = Color(0xFF9C27B0)
            )
        }
    ) { paddingValues ->

        when {
            isLoading -> {
                LoadingSimulationScreen(paddingValues)
            }
            simulations.isEmpty() -> {
                EmptySimulationScreen(
                    paddingValues = paddingValues,
                    onCreateClick = { showCreateDialog = true }
                )
            }
            else -> {
                SimulationListContent(
                    simulations = simulations,
                    paddingValues = paddingValues,
                    onEditClick = { simulation ->
                        // Navegar a edición
                    },
                    onDeleteClick = { simulation ->
                        scope.launch {
                            viewModel.deleteSimulation(simulation)
                        }
                    },
                    onAnalyticsClick = { simulation ->
                        // Ver análisis
                    }
                )
            }
        }

        // Dialogo de creación
        if (showCreateDialog) {
            CreateSimulationDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { title, description, moduleConfigs ->
                    scope.launch {
                        viewModel.createSimulation(title, description, TeacherSimulationConfig(), moduleConfigs)
                        showCreateDialog = false
                    }
                }
            )
        }
    }
}

// ===================================
// 🎯 COMPONENTES UI
// ===================================

@Composable
fun LoadingSimulationScreen(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFF9C27B0))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cargando simulacros...")
        }
    }
}

@Composable
fun EmptySimulationScreen(
    paddingValues: PaddingValues,
    onCreateClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Assessment,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF9C27B0)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No tienes simulacros creados",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Crea tu primer simulacro personalizado para tus estudiantes",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onCreateClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0)
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear Simulacro")
            }
        }
    }
}

@Composable
fun SimulationListContent(
    simulations: List<TeacherSimulation>,
    paddingValues: PaddingValues,
    onEditClick: (TeacherSimulation) -> Unit,
    onDeleteClick: (TeacherSimulation) -> Unit,
    onAnalyticsClick: (TeacherSimulation) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header informativo
        item {
            SimulationStatsHeader(simulations)
        }

        // Lista de simulacros
        items(simulations) { simulation ->
            SimulationCard(
                simulation = simulation,
                onEditClick = { onEditClick(simulation) },
                onDeleteClick = { onDeleteClick(simulation) },
                onAnalyticsClick = { onAnalyticsClick(simulation) }
            )
        }
    }
}

@Composable
fun SimulationStatsHeader(simulations: List<TeacherSimulation>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF9C27B0).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "📊 Resumen de Simulacros",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = "${simulations.size}",
                    label = "Simulacros",
                    color = Color(0xFF9C27B0)
                )
                StatItem(
                    value = "${simulations.sumOf { it.totalQuestions }}",
                    label = "Preguntas Totales",
                    color = Color(0xFF2196F3)
                )
                StatItem(
                    value = "${simulations.count { it.isActive }}",
                    label = "Activos",
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SimulationCard(
    simulation: TeacherSimulation,
    onEditClick: (TeacherSimulation) -> Unit,
    onDeleteClick: (TeacherSimulation) -> Unit,
    onAnalyticsClick: (TeacherSimulation) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (simulation.isActive)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        simulation.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        simulation.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Creado: ${formatDate(simulation.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = if (simulation.isActive) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        if (simulation.isActive) "ACTIVO" else "INACTIVO",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Detalles del simulacro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DetailItem("${simulation.totalQuestions}", "Preguntas")
                DetailItem("${simulation.sessions.size}", "Módulos")
                DetailItem("${simulation.totalDuration / (60 * 60 * 1000)}h", "Duración")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onAnalyticsClick(simulation) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Análisis")
                }

                OutlinedButton(
                    onClick = { onEditClick(simulation) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Editar")
                }

                OutlinedButton(
                    onClick = { onDeleteClick(simulation) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFF44336)
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Eliminar")
                }
            }
        }
    }
}

@Composable
fun DetailItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ===================================
// 🎯 DIALOGO DE CREACIÓN
// ===================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSimulationDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, List<ModuleConfiguration>) -> Unit
) {
    var title by remember { mutableStateOf("Simulacro Personalizado") }
    var description by remember { mutableStateOf("Contenido adaptado para tu institución") }
    var selectedModules by remember { mutableStateOf(listOf("lectura_critica", "matematicas", "ciencias_naturales", "sociales_ciudadanas", "ingles")) }
    var timeLimit by remember { mutableStateOf(65) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "🎯 Crear Simulacro Premium",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título del simulacro") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }

                item {
                    Text(
                        "Módulos incluidos:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    ModuleSelection(
                        selectedModules = selectedModules,
                        onSelectionChange = { selectedModules = it }
                    )
                }

                item {
                    Slider(
                        value = timeLimit.toFloat(),
                        onValueChange = { timeLimit = it.toInt() },
                        valueRange = 45f..90f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Tiempo por módulo: ${timeLimit} minutos",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val moduleConfigs = selectedModules.map { moduleId ->
                        ModuleConfiguration(
                            moduleId = moduleId,
                            moduleName = getModuleName(moduleId),
                            questionCount = 35, // Configurable
                            timeLimit = 65
                        )
                    }
                    onCreate(title, description, moduleConfigs)
                }
            ) {
                Text("Crear Simulacro")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

fun getModuleName(moduleId: String): String {
    return when (moduleId) {
        "lectura_critica" -> "Lectura Crítica"
        "matematicas" -> "Matemáticas"
        "ciencias_naturales" -> "Ciencias Naturales"
        "sociales_ciudadanas" -> "Sociales y Ciudadanas"
        "ingles" -> "Inglés"
        else -> moduleId
    }
}

@Composable
fun ModuleSelection(
    selectedModules: List<String>,
    onSelectionChange: (List<String>) -> Unit
) {
    val modules = listOf(
        "lectura_critica" to "📖 Lectura Crítica",
        "matematicas" to "🔢 Matemáticas",
        "ciencias_naturales" to "🧪 Ciencias Naturales",
        "sociales_ciudadanas" to "🏛️ Sociales y Ciudadanas",
        "ingles" to "🇺🇸 Inglés"
    )

    modules.forEach { (id, name) ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Checkbox(
                checked = id in selectedModules,
                onCheckedChange = { checked ->
                    val newList = if (checked) {
                        selectedModules + id
                    } else {
                        selectedModules - id
                    }
                    onSelectionChange(newList.sorted())
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(name)
        }
    }
}

// ===================================
// 🎯 FUNCIONES AUXILIARES
// ===================================

fun formatDate(timestamp: Long): String {
    return java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        .format(java.util.Date(timestamp))
}

