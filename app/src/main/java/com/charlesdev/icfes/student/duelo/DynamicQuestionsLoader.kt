package com.charlesdev.icfes.student.duelo

import android.util.Log
import com.charlesdev.icfes.teacher.duelo.repository.DuelBankRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DynamicQuestionsLoader {

    private val _questionsCache = MutableStateFlow<List<Question>>(emptyList())
    val questionsCache: StateFlow<List<Question>> = _questionsCache

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val duelBankRepository = DuelBankRepository()

    companion object {
        private const val TAG = "DynamicQuestionsLoader"
        private const val TOTAL_QUESTIONS_PER_MATCH = 20
    }

    suspend fun loadQuestionsForMatch(): Result<List<Question>> {
        return try {
            _isLoading.value = true
            Log.d(TAG, "Cargando preguntas ICFES...")

            // ✅ SIMPLE: Intentar cargar desde Firebase del profesor actual
            val currentUser = FirebaseAuth.getInstance().currentUser
            val questions = if (currentUser != null) {
                Log.d(TAG, "Cargando preguntas desde Firebase del profesor...")
                loadQuestionsFromFirebase(currentUser.uid)
            } else {
                Log.w(TAG, "No hay usuario autenticado, usando preguntas locales")
                loadLocalQuestions()
            }

            // Validar y filtrar
            val validQuestions = questions
                .filter { it.isValid() }
                .distinctBy { it.text.trim().lowercase() }

            Log.d(TAG, "Disponibles ${validQuestions.size} preguntas válidas")

            // Seleccionar 20 preguntas
            val selectedQuestions = validQuestions.take(TOTAL_QUESTIONS_PER_MATCH)

            // Guardar en caché
            _questionsCache.value = selectedQuestions

            Log.d(TAG, "✅ Cargadas ${selectedQuestions.size} preguntas ICFES")

            // Log de debugging
            selectedQuestions.take(3).forEachIndexed { index, question ->
                Log.d(TAG, "Pregunta ${index + 1}: ${question.text.take(50)}... | Correcta: ${question.correctAnswer}")
            }

            Result.success(selectedQuestions)

        } catch (e: Exception) {
            Log.e(TAG, "Error cargando preguntas", e)

            // Fallback a preguntas locales
            val fallbackQuestions = loadLocalQuestions()
                .filter { it.isValid() }
                .take(TOTAL_QUESTIONS_PER_MATCH)

            _questionsCache.value = fallbackQuestions
            Result.success(fallbackQuestions)

        } finally {
            _isLoading.value = false
        }
    }

    // Cargar preguntas desde Firebase
    private suspend fun loadQuestionsFromFirebase(profesorId: String): List<Question> {
        return try {
            val result = duelBankRepository.getAllActiveQuestions(profesorId)

            result.fold(
                onSuccess = { questions ->
                    Log.d(TAG, "Firebase: ${questions.size} preguntas obtenidas")
                    if (questions.isNotEmpty()) {
                        questions
                    } else {
                        Log.w(TAG, "Firebase vacío, usando preguntas locales")
                        loadLocalQuestions()
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error en Firebase", exception)
                    loadLocalQuestions()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en Firebase", e)
            loadLocalQuestions()
        }
    }

    // Fallback a preguntas locales
    private fun loadLocalQuestions(): List<Question> {
        Log.d(TAG, "Usando ${SampleQuestionsICFES.list.size} preguntas locales")
        return SampleQuestionsICFES.list
    }

    fun clearCache() {
        _questionsCache.value = emptyList()
        Log.d(TAG, "Cache limpiado")
    }

    fun getQuestionByIndex(index: Int): Question? {
        val questions = _questionsCache.value
        val question = questions.getOrNull(index)

        if (question != null && !question.isValid()) {
            Log.e(TAG, "Pregunta $index inválida: ${question.getDebugInfo()}")
            return null
        }

        return question
    }

    fun getTotalQuestions(): Int = TOTAL_QUESTIONS_PER_MATCH

    fun isLastQuestion(index: Int): Boolean = index >= (TOTAL_QUESTIONS_PER_MATCH - 1)
}