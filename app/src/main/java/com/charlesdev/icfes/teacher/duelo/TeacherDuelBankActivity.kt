package com.charlesdev.icfes.teacher.duelo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charlesdev.icfes.student.duelo.Question
import com.charlesdev.icfes.teacher.duelo.ui.QuestionEditorScreen
import com.charlesdev.icfes.teacher.duelo.viewmodel.DuelBankViewModel
import com.charlesdev.icfes.ui.theme.IcfesTheme

class TeacherDuelBankActivity : ComponentActivity() {

    private val viewModel: DuelBankViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            IcfesTheme {
                DuelBankScreen(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuelBankScreen(
    viewModel: DuelBankViewModel,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showEditor by remember { mutableStateOf(false) }
    var questionToEdit by remember { mutableStateOf<Question?>(null) }

    val activeQuestions by viewModel.activeQuestions.collectAsState()
    val draftQuestions by viewModel.draftQuestions.collectAsState()
    val metadata by viewModel.metadata.collectAsState()

    if (showEditor) {
        QuestionEditorScreen(
            viewModel = viewModel,
            questionToEdit = questionToEdit,
            onSaved = {
                showEditor = false
                questionToEdit = null
            },
            onCancel = {
                showEditor = false
                questionToEdit = null
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Banco de Preguntas ICFES",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Gestión de duelos",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        questionToEdit = null
                        showEditor = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar pregunta")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                // Stats cards
                StatsRow(
                    totalActive = metadata["totalActivas"] as? Long ?: 0L,
                    totalDrafts = metadata["totalBorradores"] as? Long ?: 0L
                )

                // Tabs
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text("Activas (${activeQuestions.size})")
                        },
                        icon = {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text("Borradores (${draftQuestions.size})")
                        },
                        icon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )
                }

                // Content
                when (selectedTab) {
                    0 -> QuestionsListTab(
                        questions = activeQuestions,
                        isDraft = false,
                        viewModel = viewModel,
                        onEdit = { question ->
                            questionToEdit = question
                            showEditor = true
                        }
                    )
                    1 -> QuestionsListTab(
                        questions = draftQuestions,
                        isDraft = true,
                        viewModel = viewModel,
                        onEdit = { question ->
                            questionToEdit = question
                            showEditor = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsRow(
    totalActive: Long,
    totalDrafts: Long
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Activas",
            value = totalActive.toString(),
            icon = Icons.Default.CheckCircle,
            color = Color(0xFF4CAF50)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Borradores",
            value = totalDrafts.toString(),
            icon = Icons.Default.Edit,
            color = Color(0xFFFF9800)
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun QuestionsListTab(
    questions: List<Question>,
    isDraft: Boolean,
    viewModel: DuelBankViewModel,
    onEdit: (Question) -> Unit
) {
    if (questions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    if (isDraft) Icons.Default.Edit else Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    if (isDraft) "No hay borradores" else "No hay preguntas publicadas",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Usa el botón + para crear una",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(questions) { question ->
                QuestionCard(
                    question = question,
                    isDraft = isDraft,
                    onEdit = { onEdit(question) },
                    onPublish = { viewModel.publishQuestion(question.id) },
                    onUnpublish = { viewModel.unpublishQuestion(question.id) },
                    onDelete = { viewModel.deleteQuestion(question.id, isDraft) }
                )
            }
        }
    }
}

@Composable
private fun QuestionCard(
    question: Question,
    isDraft: Boolean,
    onEdit: () -> Unit,
    onPublish: () -> Unit,
    onUnpublish: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar pregunta") },
            text = { Text("¿Estás seguro? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Eliminar", color = Color(0xFFD32F2F))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = when (question.difficulty) {
                                    com.charlesdev.icfes.student.duelo.Difficulty.EASY -> Color(0xFF4CAF50)
                                    com.charlesdev.icfes.student.duelo.Difficulty.MEDIUM -> Color(0xFFFF9800)
                                    com.charlesdev.icfes.student.duelo.Difficulty.HARD -> Color(0xFFD32F2F)
                                },
                                shape = CircleShape
                            )
                    )
                    Text(
                        question.difficulty.name,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("•", fontSize = 12.sp)
                    Text(
                        question.topic.name,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            onEdit()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )

                    if (isDraft) {
                        DropdownMenuItem(
                            text = { Text("Publicar") },
                            onClick = {
                                onPublish()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, null) }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Mover a borradores") },
                            onClick = {
                                onUnpublish()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                    }

                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Eliminar", color = Color(0xFFD32F2F)) },
                        onClick = {
                            showDeleteDialog = true
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFD32F2F)) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                question.text,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(12.dp))

            // Opciones
            listOf(
                'A' to question.optionA,
                'B' to question.optionB,
                'C' to question.optionC,
                'D' to question.optionD
            ).forEach { (letter, option) ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = if (letter == question.correctAnswer)
                                    Color(0xFF4CAF50).copy(alpha = 0.2f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            letter.toString(),
                            fontWeight = if (letter == question.correctAnswer) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (letter == question.correctAnswer) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        option,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}