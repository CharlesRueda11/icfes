package com.charlesdev.icfes.teacher.duelo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charlesdev.icfes.student.duelo.Difficulty
import com.charlesdev.icfes.student.duelo.DuelTopic
import com.charlesdev.icfes.student.duelo.Question
import com.charlesdev.icfes.teacher.duelo.repository.DuelBankRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DuelBankViewModel : ViewModel() {

    private val repository = DuelBankRepository()

    // Estados
    private val _activeQuestions = MutableStateFlow<List<Question>>(emptyList())
    val activeQuestions: StateFlow<List<Question>> = _activeQuestions.asStateFlow()

    private val _draftQuestions = MutableStateFlow<List<Question>>(emptyList())
    val draftQuestions: StateFlow<List<Question>> = _draftQuestions.asStateFlow()

    private val _metadata = MutableStateFlow<Map<String, Any>>(emptyMap())
    val metadata: StateFlow<Map<String, Any>> = _metadata.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "DuelBankViewModel"
    }

    init {
        loadQuestions()
        loadMetadata()
    }

    // ========== CARGAR DATOS ==========

    private fun loadQuestions() {
        viewModelScope.launch {
            // Activas
            repository.getActiveQuestionsFlow()
                .catch { e ->
                    Log.e(TAG, "Error cargando activas", e)
                    _uiState.value = UiState.Error("Error cargando preguntas activas")
                }
                .collect { questions ->
                    _activeQuestions.value = questions.sortedByDescending { it.createdAt }
                }
        }

        viewModelScope.launch {
            // Borradores
            repository.getDraftQuestionsFlow()
                .catch { e ->
                    Log.e(TAG, "Error cargando borradores", e)
                }
                .collect { questions ->
                    _draftQuestions.value = questions.sortedByDescending { it.createdAt }
                }
        }
    }

    private fun loadMetadata() {
        viewModelScope.launch {
            repository.getMetadata().fold(
                onSuccess = { _metadata.value = it },
                onFailure = { Log.e(TAG, "Error cargando metadata", it) }
            )
        }
    }

    // ========== CRUD PREGUNTAS ==========

    fun saveQuestion(
        text: String,
        optionA: String,
        optionB: String,
        optionC: String,
        optionD: String,
        correctAnswer: Char,
        difficulty: Difficulty,
        topic: DuelTopic,
        hint: String = "",
        isDraft: Boolean = true,
        questionId: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val question = Question(
                id = questionId ?: repository.generateQuestionId(),
                text = text.trim(),
                optionA = optionA.trim(),
                optionB = optionB.trim(),
                optionC = optionC.trim(),
                optionD = optionD.trim(),
                correctAnswer = correctAnswer.uppercaseChar(),
                difficulty = difficulty,
                topic = topic,
                hint = hint.trim(),
                createdAt = if (questionId == null) System.currentTimeMillis() else 0,
                lastModified = System.currentTimeMillis(),
                isActive = !isDraft
            )

            repository.saveQuestion(question, isDraft).fold(
                onSuccess = {
                    _uiState.value = UiState.Success(
                        if (isDraft) "Pregunta guardada como borrador"
                        else "Pregunta publicada correctamente"
                    )
                    loadMetadata()
                },
                onFailure = {
                    _uiState.value = UiState.Error("Error: ${it.message}")
                }
            )
        }
    }

    fun publishQuestion(questionId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            repository.publishQuestion(questionId).fold(
                onSuccess = {
                    _uiState.value = UiState.Success("Pregunta publicada")
                    loadMetadata()
                },
                onFailure = {
                    _uiState.value = UiState.Error("Error publicando: ${it.message}")
                }
            )
        }
    }

    fun unpublishQuestion(questionId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            repository.unpublishQuestion(questionId).fold(
                onSuccess = {
                    _uiState.value = UiState.Success("Pregunta movida a borradores")
                    loadMetadata()
                },
                onFailure = {
                    _uiState.value = UiState.Error("Error: ${it.message}")
                }
            )
        }
    }

    fun deleteQuestion(questionId: String, isDraft: Boolean) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            repository.deleteQuestion(questionId, isDraft).fold(
                onSuccess = {
                    _uiState.value = UiState.Success("Pregunta eliminada")
                    loadMetadata()
                },
                onFailure = {
                    _uiState.value = UiState.Error("Error eliminando: ${it.message}")
                }
            )
        }
    }

    // ========== HELPERS ==========

    fun clearUiState() {
        _uiState.value = UiState.Idle
    }

    fun getTotalActive(): Int = _activeQuestions.value.size
    fun getTotalDrafts(): Int = _draftQuestions.value.size

    fun getQuestionsByDifficulty(difficulty: Difficulty): List<Question> {
        return _activeQuestions.value.filter { it.difficulty == difficulty }
    }

    fun getQuestionsByTopic(topic: DuelTopic): List<Question> {
        return _activeQuestions.value.filter { it.topic == topic }
    }

    // ========== ESTADOS UI ==========

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }
}