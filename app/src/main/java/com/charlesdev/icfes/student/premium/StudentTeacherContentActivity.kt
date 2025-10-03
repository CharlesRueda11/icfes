// ===================================
// 🎯 SISTEMA PREMIUM COMPLETO Y FUNCIONAL
// ===================================

package com.charlesdev.icfes.student.premium

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.charlesdev.icfes.ui.theme.IcfesTheme
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

// ===================================
// 🎯 DATA CLASS SIMPLE
// ===================================
data class PremiumModuleData(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val color: Color,
    val practiceCount: Int,
    val evaluationCount: Int
)

class StudentTeacherContentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val teacherId = intent.getStringExtra("teacher_id") ?: ""
        val teacherName = intent.getStringExtra("teacher_name") ?: "Profesor"

        setContent {
            IcfesTheme {
                CleanPremiumContentScreen(
                    teacherId = teacherId,
                    teacherName = teacherName
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanPremiumContentScreen(
    teacherId: String,
    teacherName: String
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var modules by remember { mutableStateOf<List<PremiumModuleData>>(emptyList()) }

    // ✅ CARGAR PROGRESO DEL ESTUDIANTE PARA BLOQUEOS
    val prefs = remember { context.getSharedPreferences("ICFESPrefs", Context.MODE_PRIVATE) }

    LaunchedEffect(teacherId) {
        loadPremiumModules(teacherId) { modulesList ->
            modules = modulesList
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Contenido Premium",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Prof. $teacherName",
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
                actions = {
                    Surface(
                        color = Color(0xFFFFD700),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "⭐ PREMIUM",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7B1FA2)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF7B1FA2).copy(alpha = 0.1f)
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF7B1FA2))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cargando contenido premium...")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    CompactPremiumHeader(teacherName, modules)
                }

                items(modules) { module ->
                    CleanModuleCard(
                        module = module,
                        teacherId = teacherId, // ✅ PASAR teacherId
                        prefs = prefs,
                        onPracticeClick = {
                            // ✅ NAVEGAR A PRÁCTICA PREMIUM
                            val intent = Intent(context, PremiumQuizActivity::class.java).apply {
                                putExtra("module_id", module.id)
                                putExtra("teacher_id", teacherId)
                                putExtra("session_type", "practica")
                                putExtra("module_name", module.name)
                                putExtra("teacher_name", teacherName)
                            }
                            context.startActivity(intent)
                        },
                        onEvaluationClick = {
                            // ✅ NAVEGAR A EVALUACIÓN PREMIUM
                            val intent = Intent(context, PremiumQuizActivity::class.java).apply {
                                putExtra("module_id", module.id)
                                putExtra("teacher_id", teacherId)
                                putExtra("session_type", "evaluacion")
                                putExtra("module_name", module.name)
                                putExtra("teacher_name", teacherName)
                            }
                            context.startActivity(intent)
                        }
                    )
                }

                item {
                    SimpleFooter()
                }
            }
        }
    }
}

@Composable
fun CompactPremiumHeader(teacherName: String, modules: List<PremiumModuleData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF7B1FA2).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF7B1FA2)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "🎓 Contenido Premium Exclusivo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7B1FA2)
                    )
                    Text(
                        "Preparado por $teacherName para tu institución",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CompactStat(
                    value = "${modules.size}",
                    label = "Módulos",
                    color = Color(0xFF7B1FA2)
                )
                CompactStat(
                    value = "${modules.sumOf { it.practiceCount }}",
                    label = "Práctica",
                    color = Color(0xFF2196F3)
                )
                CompactStat(
                    value = "${modules.sumOf { it.evaluationCount }}",
                    label = "Evaluación",
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
fun CompactStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
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
fun CleanModuleCard(
    module: PremiumModuleData,
    teacherId: String, // ✅ AGREGAR teacherId
    prefs: SharedPreferences,
    onPracticeClick: () -> Unit,
    onEvaluationClick: () -> Unit
) {
    val practiceScore = prefs.getInt("premium_practice_${module.id}", 0)
    val hasMinimumPractice = practiceScore >= 50
    val isEvaluationUnlocked = hasMinimumPractice

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = module.color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    module.emoji,
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        module.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = module.color
                    )
                    Text(
                        module.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (practiceScore > 0) {
                    Surface(
                        color = if (isEvaluationUnlocked) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            if (isEvaluationUnlocked) "LISTO" else "EN PROGRESO",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ModuleTypeCard(
                    modifier = Modifier.weight(1f),
                    title = "📚 Práctica",
                    count = module.practiceCount,
                    status = if (practiceScore > 0) "Puntuación: $practiceScore" else "Sin comenzar",
                    color = Color(0xFF2196F3),
                    isUnlocked = true
                )

                ModuleTypeCard(
                    modifier = Modifier.weight(1f),
                    title = "⏱️ Evaluación",
                    count = module.evaluationCount,
                    status = if (isEvaluationUnlocked) "Desbloqueado" else "Practica primero",
                    color = Color(0xFFFF9800),
                    isUnlocked = isEvaluationUnlocked
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ✅ BOTÓN PRÁCTICA FUNCIONAL
                Button(
                    onClick = onPracticeClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = module.color
                    )
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Comenzar Práctica Premium")
                }

                // ✅ BOTÓN EVALUACIÓN CON LÓGICA
                if (isEvaluationUnlocked) {
                    OutlinedButton(
                        onClick = onEvaluationClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = module.color
                        )
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Evaluación Cronometrada")
                    }
                } else {
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Evaluación Bloqueada")
                    }

                    val progressToUnlock = if (practiceScore > 0) (practiceScore / 50f).coerceAtMost(1f) else 0f

                    LinearProgressIndicator(
                        progress = progressToUnlock,
                        modifier = Modifier.fillMaxWidth(),
                        color = module.color.copy(alpha = 0.7f)
                    )

                    Text(
                        if (practiceScore > 0)
                            "Progreso: $practiceScore/50 puntos para desbloquear"
                        else
                            "Practica para desbloquear la evaluación",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ModuleTypeCard(
    modifier: Modifier = Modifier,
    title: String,
    count: Int,
    status: String,
    color: Color,
    isUnlocked: Boolean
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = if (isUnlocked) 0.1f else 0.05f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                "$count preguntas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                status,
                style = MaterialTheme.typography.bodySmall,
                color = if (isUnlocked) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SimpleFooter() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "💡 Contenido Premium Verificado",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Adaptado específicamente para tu institución y el ICFES 2025",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ===================================
// 🔧 FUNCIONES AUXILIARES
// ===================================

suspend fun loadPremiumModules(
    teacherId: String,
    onResult: (List<PremiumModuleData>) -> Unit
) {
    try {
        val database = FirebaseDatabase.getInstance()
        val snapshot = database.reference
            .child("ContenidoDocente")
            .child("profesores")
            .child(teacherId)
            .child("modulos")
            .get()
            .await()

        val modulesList = mutableListOf<PremiumModuleData>()

        snapshot.children.forEach { moduleSnapshot ->
            val moduleId = moduleSnapshot.key ?: return@forEach
            val practiceCount = moduleSnapshot.child("practica").childrenCount.toInt()
            val evaluationCount = moduleSnapshot.child("evaluacion").childrenCount.toInt()

            if (practiceCount > 0 || evaluationCount > 0) {
                val moduleInfo = getCleanModuleInfo(moduleId)
                modulesList.add(
                    PremiumModuleData(
                        id = moduleId,
                        name = moduleInfo.name,
                        emoji = moduleInfo.emoji,
                        description = moduleInfo.description,
                        color = moduleInfo.color,
                        practiceCount = practiceCount,
                        evaluationCount = evaluationCount
                    )
                )
            }
        }

        onResult(modulesList)
    } catch (e: Exception) {
        onResult(emptyList())
    }
}

fun getCleanModuleInfo(moduleId: String): PremiumModuleData {
    return when (moduleId) {
        "lectura_critica" -> PremiumModuleData(
            id = moduleId,
            name = "Lectura Crítica",
            emoji = "📖",
            description = "Comprensión e interpretación textual",
            color = Color(0xFF2196F3),
            practiceCount = 0,
            evaluationCount = 0
        )
        "matematicas" -> PremiumModuleData(
            id = moduleId,
            name = "Matemáticas",
            emoji = "🔢",
            description = "Razonamiento cuantitativo",
            color = Color(0xFFFF9800),
            practiceCount = 0,
            evaluationCount = 0
        )
        "ciencias_naturales" -> PremiumModuleData(
            id = moduleId,
            name = "Ciencias Naturales",
            emoji = "🧪",
            description = "Uso del conocimiento científico",
            color = Color(0xFF4CAF50),
            practiceCount = 0,
            evaluationCount = 0
        )
        "sociales_ciudadanas" -> PremiumModuleData(
            id = moduleId,
            name = "Sociales y Ciudadanas",
            emoji = "🏛️",
            description = "Pensamiento social y ciudadano",
            color = Color(0xFF9C27B0),
            practiceCount = 0,
            evaluationCount = 0
        )
        "ingles" -> PremiumModuleData(
            id = moduleId,
            name = "Inglés",
            emoji = "🇺🇸",
            description = "Comunicación en lengua inglesa",
            color = Color(0xFFF44336),
            practiceCount = 0,
            evaluationCount = 0
        )
        else -> PremiumModuleData(
            id = moduleId,
            name = "Módulo Premium",
            emoji = "⭐",
            description = "Contenido especializado",
            color = Color(0xFF607D8B),
            practiceCount = 0,
            evaluationCount = 0
        )
    }
}
