package com.charlesdev.icfes.teacher.duelo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charlesdev.icfes.student.duelo.Difficulty
import com.charlesdev.icfes.student.duelo.DuelTopic
import com.charlesdev.icfes.teacher.duelo.viewmodel.DuelBankViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionEditorScreen(
    viewModel: DuelBankViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    questionToEdit: com.charlesdev.icfes.student.duelo.Question? = null
) {
    var text by remember { mutableStateOf(questionToEdit?.text ?: "") }
    var optionA by remember { mutableStateOf(questionToEdit?.optionA ?: "") }
    var optionB by remember { mutableStateOf(questionToEdit?.optionB ?: "") }
    var optionC by remember { mutableStateOf(questionToEdit?.optionC ?: "") }
    var optionD by remember { mutableStateOf(questionToEdit?.optionD ?: "") }
    var correctAnswer by remember { mutableStateOf(questionToEdit?.correctAnswer ?: 'A') }
    var difficulty by remember { mutableStateOf(questionToEdit?.difficulty ?: Difficulty.EASY) }
    var topic by remember { mutableStateOf(questionToEdit?.topic ?: DuelTopic.OTROS) }
    var hint by remember { mutableStateOf(questionToEdit?.hint ?: "") }
    var saveAsDraft by remember { mutableStateOf(true) }

    var showDifficultyMenu by remember { mutableStateOf(false) }
    var showTopicMenu by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is DuelBankViewModel.UiState.Success) {
            onSaved()
            viewModel.clearUiState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (questionToEdit != null) "Editar Pregunta" else "Nueva Pregunta",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Texto de la pregunta
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Texto de la pregunta") },
                    placeholder = { Text("¿Cuánto es 2+2?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    isError = text.isBlank()
                )

                // Opciones A, B, C, D
                Text(
                    "Opciones de respuesta:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                OptionField(
                    letter = 'A',
                    value = optionA,
                    onValueChange = { optionA = it },
                    isCorrect = correctAnswer == 'A',
                    onSelectAsCorrect = { correctAnswer = 'A' }
                )

                OptionField(
                    letter = 'B',
                    value = optionB,
                    onValueChange = { optionB = it },
                    isCorrect = correctAnswer == 'B',
                    onSelectAsCorrect = { correctAnswer = 'B' }
                )

                OptionField(
                    letter = 'C',
                    value = optionC,
                    onValueChange = { optionC = it },
                    isCorrect = correctAnswer == 'C',
                    onSelectAsCorrect = { correctAnswer = 'C' }
                )

                OptionField(
                    letter = 'D',
                    value = optionD,
                    onValueChange = { optionD = it },
                    isCorrect = correctAnswer == 'D',
                    onSelectAsCorrect = { correctAnswer = 'D' }
                )

                HorizontalDivider()

                // Dificultad
                ExposedDropdownMenuBox(
                    expanded = showDifficultyMenu,
                    onExpandedChange = { showDifficultyMenu = it }
                ) {
                    OutlinedTextField(
                        value = difficulty.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dificultad") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDifficultyMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = showDifficultyMenu,
                        onDismissRequest = { showDifficultyMenu = false }
                    ) {
                        Difficulty.entries.forEach { diff ->
                            DropdownMenuItem(
                                text = { Text(diff.name) },
                                onClick = {
                                    difficulty = diff
                                    showDifficultyMenu = false
                                }
                            )
                        }
                    }
                }

                // Tema
                ExposedDropdownMenuBox(
                    expanded = showTopicMenu,
                    onExpandedChange = { showTopicMenu = it }
                ) {
                    OutlinedTextField(
                        value = topic.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tema") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTopicMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = showTopicMenu,
                        onDismissRequest = { showTopicMenu = false }
                    ) {
                        DuelTopic.entries.forEach { topicItem ->
                            DropdownMenuItem(
                                text = { Text(topicItem.name) },
                                onClick = {
                                    topic = topicItem
                                    showTopicMenu = false
                                }
                            )
                        }
                    }
                }

                // Hint (opcional)
                OutlinedTextField(
                    value = hint,
                    onValueChange = { hint = it },
                    label = { Text("Pista (opcional)") },
                    placeholder = { Text("Ayuda adicional para el estudiante") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                HorizontalDivider()

                // Guardar como borrador o publicar
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                                if (saveAsDraft) "Guardar como borrador" else "Publicar directamente",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (saveAsDraft) "Podrás revisarla antes de publicar" else "Los estudiantes la verán inmediatamente",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = !saveAsDraft,
                            onCheckedChange = { saveAsDraft = !it }
                        )
                    }
                }

                Spacer(Modifier.height(80.dp))
            }

            // Botones flotantes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        viewModel.saveQuestion(
                            text = text,
                            optionA = optionA,
                            optionB = optionB,
                            optionC = optionC,
                            optionD = optionD,
                            correctAnswer = correctAnswer,
                            difficulty = difficulty,
                            topic = topic,
                            hint = hint,
                            isDraft = saveAsDraft,
                            questionId = questionToEdit?.id
                        )
                    },
                    enabled = text.isNotBlank() &&
                            optionA.isNotBlank() &&
                            optionB.isNotBlank() &&
                            optionC.isNotBlank() &&
                            optionD.isNotBlank() &&
                            uiState !is DuelBankViewModel.UiState.Loading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState is DuelBankViewModel.UiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (saveAsDraft) "Guardar" else "Publicar")
                    }
                }
            }
        }

        // Snackbar para errores
        if (uiState is DuelBankViewModel.UiState.Error) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearUiState() }) {
                        Text("OK")
                    }
                }
            ) {
                Text((uiState as DuelBankViewModel.UiState.Error).message)
            }
        }
    }
}

@Composable
private fun OptionField(
    letter: Char,
    value: String,
    onValueChange: (String) -> Unit,
    isCorrect: Boolean,
    onSelectAsCorrect: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadioButton(
            selected = isCorrect,
            onClick = onSelectAsCorrect
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Opción $letter") },
            placeholder = { Text("Escribe la opción $letter") },
            modifier = Modifier.weight(1f),
            isError = value.isBlank(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                focusedLabelColor = if (isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
            )
        )
    }
}