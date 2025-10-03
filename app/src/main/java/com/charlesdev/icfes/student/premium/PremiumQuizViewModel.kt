package com.charlesdev.icfes.student.premium

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charlesdev.icfes.teacher.practice_evaluation.TeacherQuestion
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Data class simplificada para las preguntas premium (MANTENER)
data class PremiumQuestion(
    val id: String,
    val question: String,
    val context: String?,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String,
    val competency: String,
    val difficulty: String,
    val explanation: String?,
    val timeEstimated: Int
) {
    val options: List<String>
        get() = listOf(
            "A) $optionA",
            "B) $optionB",
            "C) $optionC",
            "D) $optionD"
        )
}

enum class PremiumSessionType {
    PRACTICE,
    TIMED_QUIZ,
    EVALUATION
}

class PremiumQuizViewModel : ViewModel() {

    // ✅ NUEVA INTEGRACIÓN: MANAGERS DE IA
    private val premiumAIManager = PremiumAIManager()
    private val premiumEvaluationManager = PremiumEvaluationManager()

    // ✅ VARIABLES BÁSICAS (MANTENER EXISTENTES)
    var currentQuestions by mutableStateOf<List<PremiumQuestion>>(emptyList())
    var isLoading by mutableStateOf(true)
    var hasError by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var currentQuestionIndex by mutableStateOf(0)
    var userAnswer by mutableStateOf("")
    var score by mutableStateOf(0)
    var showResults by mutableStateOf(false)

    // ✅ VARIABLES PARA IGUALAR ICFES (MANTENER)
    var timeRemaining by mutableStateOf(0L)
        private set
    var isTimerActive by mutableStateOf(false)
        private set
    var isEvaluationMode by mutableStateOf(false)
        private set
    var evaluationCompleted by mutableStateOf(false)
        private set

    // Estados de navegación y respuestas
    var hasAnsweredCurrentQuestion by mutableStateOf(false)
        private set
    var sessionType by mutableStateOf(PremiumSessionType.PRACTICE)
        private set

    // ✅ NUEVAS VARIABLES PARA IA PREMIUM
    // ✅ NUEVAS VARIABLES PARA IA PREMIUM (ACTUALIZAR)
    var showFeedback by mutableStateOf(false)
        private set
    var isValidatingWithAI by mutableStateOf(false)
        private set
    var feedbackTitle by mutableStateOf("")
        private set
    var feedbackMessage by mutableStateOf("")
        private set
    var feedbackTip by mutableStateOf("")
        private set
    var teacherPersonalization by mutableStateOf("") // ✅ NUEVA VARIABLE
        private set
    var isAnswerCorrect by mutableStateOf(false)
        private set

    // ✅ NUEVAS VARIABLES PARA EVALUACIONES PREMIUM
    var showPremiumEvaluationSummary by mutableStateOf(false)
        private set
    var premiumEvaluationResult by mutableStateOf<PremiumEvaluationResult?>(null)
        private set
    var isGeneratingPremiumFeedback by mutableStateOf(false)
        private set

    // StateFlows para progreso como ICFES (MANTENER)
    private val _progressState = MutableStateFlow(0)
    val progressState: StateFlow<Int> = _progressState.asStateFlow()

    private val _timeState = MutableStateFlow(0L)
    val timeState: StateFlow<Long> = _timeState.asStateFlow()

    // Variables de sesión (MANTENER)
    private var sessionStartTime = 0L
    private var totalQuestionsAnswered = 0
    private val answeredQuestions = mutableSetOf<Int>()

    // Variables para datos del profesor (MANTENER)
    var currentTeacherId by mutableStateOf("")
        private set
    var currentTeacherName by mutableStateOf("")
        private set
    var currentModuleId by mutableStateOf("")
        private set
    var currentModuleName by mutableStateOf("")
        private set

    // ✅ FUNCIÓN PRINCIPAL DE CARGA (ACTUALIZADA CON IA)
    fun loadPremiumQuestions(
        moduleId: String,
        teacherId: String,
        sessionTypeString: String,
        teacherName: String = "",
        moduleName: String = ""
    ) {
        viewModelScope.launch {
            try {
                isLoading = true
                hasError = false

                // Guardar datos de sesión
                currentModuleId = moduleId
                currentTeacherId = teacherId
                currentTeacherName = teacherName
                currentModuleName = moduleName
                sessionStartTime = System.currentTimeMillis()

                // Determinar tipo de sesión
                sessionType = when (sessionTypeString) {
                    "practica" -> PremiumSessionType.PRACTICE
                    "evaluacion" -> PremiumSessionType.EVALUATION
                    else -> PremiumSessionType.PRACTICE
                }

                isEvaluationMode = sessionType == PremiumSessionType.EVALUATION

                // ✅ INICIALIZAR MANAGERS DE IA
                if (isEvaluationMode) {
                    premiumEvaluationManager.startPremiumEvaluation(
                        moduleId = moduleId,
                        moduleName = moduleName,
                        teacherId = teacherId,
                        teacherName = teacherName,
                        institution = "Institución", // TODO: Obtener de datos reales
                        sessionType = PremiumSessionType.EVALUATION
                    )
                } else {
                    premiumAIManager.startPremiumSession(
                        teacherId = teacherId,
                        teacherName = teacherName,
                        institution = "Institución", // TODO: Obtener de datos reales
                        sessionType = PremiumSessionType.PRACTICE
                    )
                }

                // Cargar preguntas desde Firebase (MANTENER LÓGICA EXISTENTE)
                val db = FirebaseDatabase.getInstance()
                val snapshot = db.reference
                    .child("ContenidoDocente")
                    .child("profesores")
                    .child(teacherId)
                    .child("modulos")
                    .child(moduleId)
                    .child(sessionTypeString)
                    .get()
                    .await()

                if (!snapshot.exists()) {
                    throw Exception("No se encontraron preguntas para este módulo")
                }

                val questions = mutableListOf<PremiumQuestion>()
                snapshot.children.forEach { child ->
                    val teacherQuestion = child.getValue(TeacherQuestion::class.java)
                    teacherQuestion?.let { tq ->
                        questions.add(
                            PremiumQuestion(
                                id = tq.id,
                                question = tq.question,
                                context = tq.context.takeIf { it.isNotBlank() },
                                optionA = tq.optionA,
                                optionB = tq.optionB,
                                optionC = tq.optionC,
                                optionD = tq.optionD,
                                correctAnswer = tq.correctAnswer,
                                competency = tq.competency,
                                difficulty = tq.difficulty,
                                explanation = tq.explanation.takeIf { it.isNotBlank() },
                                timeEstimated = tq.timeEstimated
                            )
                        )
                    }
                }

                if (questions.isEmpty()) {
                    throw Exception("No se encontraron preguntas válidas")
                }

                currentQuestions = questions.shuffled()

                // ✅ CONFIGURAR TIMER SI ES EVALUACIÓN
                if (isEvaluationMode) {
                    setupTimer()
                }

                resetQuizStates()
                isLoading = false

            } catch (e: Exception) {
                errorMessage = e.message ?: "Error desconocido"
                hasError = true
                isLoading = false
            }
        }
    }

    // ✅ CONFIGURAR TIMER COMO ICFES (MANTENER)
    private fun setupTimer() {
        val totalEstimatedSeconds = currentQuestions.sumOf { it.timeEstimated }
        timeRemaining = totalEstimatedSeconds * 1000L
        startTimer()
    }

    private fun startTimer() {
        isTimerActive = true
        viewModelScope.launch {
            while (timeRemaining > 0 && isTimerActive && !evaluationCompleted) {
                kotlinx.coroutines.delay(1000)
                timeRemaining -= 1000
                _timeState.value = timeRemaining
            }

            if (timeRemaining <= 0 && !evaluationCompleted) {
                finishEvaluation()
            }
        }
    }

    private fun stopTimer() {
        isTimerActive = false
    }

    // ✅ FUNCIÓN DE ENVÍO DE RESPUESTA (ACTUALIZADA CON IA)
    fun submitAnswer() {
        if (hasAnsweredCurrentQuestion) return

        val currentQuestion = getCurrentQuestion() ?: return
        val isCorrectBasic = userAnswer.equals(currentQuestion.correctAnswer, ignoreCase = true)

        if (isEvaluationMode) {
            // ✅ MODO EVALUACIÓN: USAR PREMIUM EVALUATION MANAGER
            premiumEvaluationManager.submitPremiumEvaluationAnswer(
                questionIndex = currentQuestionIndex,
                question = currentQuestion,
                userAnswer = userAnswer
            )

            hasAnsweredCurrentQuestion = true
            showFeedback = false // Sin feedback hasta el final

            // Actualizar progreso básico
            totalQuestionsAnswered++
            updateProgressInRealTime()

        } else {
            // ✅ MODO PRÁCTICA: USAR PREMIUM AI MANAGER PARA FEEDBACK INMEDIATO
            isValidatingWithAI = true

            viewModelScope.launch {
                try {
                    // ✅ LLAMAR A IA PREMIUM PARA FEEDBACK INMEDIATO
                    val premiumFeedback = premiumAIManager.generateImmediateFeedback(
                        question = currentQuestion,
                        userAnswer = userAnswer,
                        isCorrect = isCorrectBasic
                    )

                    // ✅ APLICAR FEEDBACK PREMIUM
                    isAnswerCorrect = premiumFeedback.isCorrect
                    feedbackTitle = premiumFeedback.title
                    feedbackMessage = premiumFeedback.message
                    feedbackTip = premiumFeedback.tip
                    teacherPersonalization = premiumFeedback.teacherPersonalization

                    // ✅ AGREGAR TOQUE PERSONALIZADO DEL PROFESOR
                    if (premiumFeedback.teacherPersonalization.isNotEmpty()) {
                        feedbackMessage += "\n\n💡 ${premiumFeedback.teacherPersonalization}"
                    }

                    if (isAnswerCorrect) {
                        score += 10
                    }

                    showFeedback = true
                    hasAnsweredCurrentQuestion = true

                    // Actualizar progreso
                    totalQuestionsAnswered++
                    updateProgressInRealTime()

                } catch (e: Exception) {
                    // ✅ FALLBACK SIN IA EN CASO DE ERROR
                    isAnswerCorrect = isCorrectBasic
                    feedbackTitle = if (isCorrectBasic) "¡Correcto!" else "Incorrecto"
                    feedbackMessage = currentQuestion.explanation ?:
                            "La respuesta correcta es ${currentQuestion.correctAnswer}"
                    feedbackTip = "Consulta el material de $currentTeacherName sobre ${currentQuestion.competency}."
                    teacherPersonalization = "Este contenido fue diseñado especialmente por tu profesor $currentTeacherName." // ✅ NUEVA LÍNEA

                    if (isCorrectBasic) {
                        score += 10
                    }

                    showFeedback = true
                    hasAnsweredCurrentQuestion = true
                    totalQuestionsAnswered++
                    updateProgressInRealTime()

                } finally {
                    isValidatingWithAI = false
                }
            }
        }
    }

    // ✅ RESTO DE FUNCIONES (MANTENER COMO ESTÁN)
    fun nextQuestion() {
        if (currentQuestionIndex < currentQuestions.size - 1) {
            currentQuestionIndex++
            userAnswer = ""
            showFeedback = false
            hasAnsweredCurrentQuestion = answeredQuestions.contains(currentQuestionIndex)
        } else {
            finishEvaluation()
        }
    }

    fun previousQuestion() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--
            userAnswer = ""
            showFeedback = false
            hasAnsweredCurrentQuestion = answeredQuestions.contains(currentQuestionIndex)
        }
    }

    fun closeFeedback() {
        showFeedback = false
    }

    fun getCurrentQuestion(): PremiumQuestion? {
        return if (currentQuestionIndex < currentQuestions.size) {
            currentQuestions[currentQuestionIndex]
        } else null
    }

    private fun updateProgressInRealTime() {
        if (totalQuestionsAnswered > 0) {
            val currentProgress = (score.toFloat() / (totalQuestionsAnswered * 10) * 100).toInt()
            _progressState.value = currentProgress
        }
    }

    private fun resetQuizStates() {
        currentQuestionIndex = 0
        userAnswer = ""
        showFeedback = false
        score = 0
        evaluationCompleted = false
        totalQuestionsAnswered = 0
        hasAnsweredCurrentQuestion = false
        answeredQuestions.clear()
        _progressState.value = 0
        isValidatingWithAI = false
        showResults = false
        // ✅ RESET PREMIUM ESPECÍFICO
        showPremiumEvaluationSummary = false
        premiumEvaluationResult = null
        isGeneratingPremiumFeedback = false
        // ✅ RESET FEEDBACK VARIABLES
        feedbackTitle = ""
        feedbackMessage = ""
        feedbackTip = ""
        teacherPersonalization = "" // ✅ NUEVA LÍNEA
        isAnswerCorrect = false
    }

    fun retryQuiz() {
        resetQuizStates()
        if (isEvaluationMode) {
            setupTimer()
        }
    }

    // ✅ CONTINUACIÓN DE PARTE 1 - FUNCIÓN DE FINALIZACIÓN CON IA PREMIUM

    /**
     * ✅ FINALIZAR EVALUACIÓN (ACTUALIZADA CON IA PREMIUM)
     */
    private fun finishEvaluation() {
        evaluationCompleted = true
        stopTimer()

        if (isEvaluationMode) {
            // ✅ MODO EVALUACIÓN: GENERAR FEEDBACK CONSOLIDADO CON IA
            isGeneratingPremiumFeedback = true
            viewModelScope.launch {
                try {
                    val result = premiumEvaluationManager.generateConsolidatedPremiumFeedback()

                    // ✅ AGREGAR COMPARACIÓN CON ICFES SI HAY DATOS PREVIOS
                    val updatedResult = addICFESComparison(result)

                    premiumEvaluationResult = updatedResult
                    showPremiumEvaluationSummary = true

                    // ✅ GUARDAR PROGRESO PREMIUM
                    savePremiumProgress(updatedResult)

                } catch (e: Exception) {
                    // ✅ FALLBACK PARA EVALUACIÓN
                    premiumEvaluationResult = generateBasicPremiumResult()
                    showPremiumEvaluationSummary = true
                } finally {
                    isGeneratingPremiumFeedback = false
                }
            }
        } else {
            // ✅ MODO PRÁCTICA: MOSTRAR RESULTADOS SIMPLES
            showResults = true

            // ✅ GUARDAR PROGRESO DE PRÁCTICA
            savePremiumPracticeProgress()
        }
    }

    /**
     * ✅ AGREGAR COMPARACIÓN CON ICFES BÁSICO
     */
    private fun addICFESComparison(result: PremiumEvaluationResult): PremiumEvaluationResult {
        // TODO: Obtener puntaje ICFES del módulo desde SharedPreferences
        // val prefs = context.getSharedPreferences("ICFESPrefs", Context.MODE_PRIVATE)
        // val icfesScore = prefs.getInt("icfes_score_${currentModuleId}", 0)

        // Por ahora, ejemplo con datos simulados
        val icfesScore = 250 // Ejemplo: puntaje ICFES básico

        val comparison = createPremiumICFESComparison(
            icfesScore = icfesScore,
            premiumScore = result.puntajePremium,
            moduleId = currentModuleId
        )

        return result.copy(comparedToICFES = comparison)
    }

    /**
     * ✅ GUARDAR PROGRESO PREMIUM EN SHAREDPREFERENCES
     */
    private fun savePremiumProgress(result: PremiumEvaluationResult) {
        // TODO: Implementar guardado en SharedPreferences
        // Similar a como se hace en ICFESQuizViewModel pero con prefijo "premium_"

        // Ejemplo de estructura:
        // prefs.edit().apply {
        //     putInt("premium_eval_score_${currentModuleId}", result.puntajePremium)
        //     putFloat("premium_eval_percentage_${currentModuleId}", result.percentage)
        //     putString("premium_eval_level_${currentModuleId}", result.nivel)
        //     putString("premium_teacher_${currentModuleId}", currentTeacherId)
        //     putLong("premium_eval_timestamp_${currentModuleId}", System.currentTimeMillis())
        //     apply()
        // }
    }

    /**
     * ✅ GUARDAR PROGRESO DE PRÁCTICA PREMIUM
     */
    private fun savePremiumPracticeProgress() {
        // TODO: Implementar guardado de práctica
        // Ejemplo:
        // prefs.edit().apply {
        //     putInt("premium_practice_${currentModuleId}", score)
        //     putLong("premium_practice_timestamp_${currentModuleId}", System.currentTimeMillis())
        //     apply()
        // }
    }

    /**
     * ✅ GENERAR RESULTADO BÁSICO SIN IA (FALLBACK)
     */
    private fun generateBasicPremiumResult(): PremiumEvaluationResult {
        val totalQuestions = currentQuestions.size
        val correctAnswers = score / 10 // Cada respuesta correcta vale 10 puntos
        val percentage = if (totalQuestions > 0) (correctAnswers.toFloat() / totalQuestions * 100) else 0f

        val nivel = when {
            percentage >= 90 -> "Excelente"
            percentage >= 80 -> "Muy Bueno"
            percentage >= 70 -> "Bueno"
            else -> "Necesita Mejorar"
        }

        return PremiumEvaluationResult(
            moduleId = currentModuleId,
            moduleName = currentModuleName,
            teacherId = currentTeacherId,
            teacherName = currentTeacherName,
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            percentage = percentage,
            timeSpent = System.currentTimeMillis() - sessionStartTime,
            puntajePremium = percentage.toInt().coerceIn(0, 100),
            nivel = nivel,
            fortalezas = listOf("Completaste la evaluación premium"),
            debilidades = listOf("Revisa las preguntas incorrectas"),
            recomendacionesProfesor = listOf(
                "Consulta con $currentTeacherName sobre áreas de mejora",
                "Practica más con el contenido premium"
            ),
            estrategiasPremium = listOf(
                "Utiliza la metodología de $currentTeacherName",
                "Combina contenido premium con ICFES básico"
            ),
            analisisPersonalizado = "Evaluación completada con contenido premium de $currentTeacherName. " +
                    "Este material está adaptado específicamente para tu institución.",
            competencyScores = emptyMap(),
            difficultyScores = emptyMap(),
            evaluationDate = System.currentTimeMillis(),
            comparedToICFES = null
        )
    }

    // ✅ FUNCIONES PARA MANEJO DE RESULTADOS PREMIUM

    /**
     * ✅ CERRAR RESUMEN DE EVALUACIÓN PREMIUM
     */
    fun closePremiumEvaluationSummary() {
        showPremiumEvaluationSummary = false
    }

    /**
     * ✅ REINTENTAR EVALUACIÓN PREMIUM
     */
    fun retryPremiumEvaluation() {
        premiumEvaluationManager.resetEvaluation()
        showPremiumEvaluationSummary = false
        premiumEvaluationResult = null
        resetQuizStates()

        // Reinicializar con nuevas preguntas
        loadPremiumQuestions(
            currentModuleId,
            currentTeacherId,
            if (isEvaluationMode) "evaluacion" else "practica",
            currentTeacherName,
            currentModuleName
        )
    }

    /**
     * ✅ OBTENER ESTADÍSTICAS PREMIUM
     */
    fun getPremiumStats(): Map<String, Any> {
        // TODO: Implementar con SharedPreferences reales
        return mapOf(
            "practice_score" to score,
            "total_questions" to currentQuestions.size,
            "correct_answers" to (score / 10),
            "teacher_name" to currentTeacherName,
            "module_name" to currentModuleName
        )
    }

    /**
     * ✅ VERIFICAR SI TIENE EXPERIENCIA PREMIUM PREVIA
     */
    fun hasPremiumExperience(): Boolean {
        // TODO: Verificar en SharedPreferences
        return false // Por ahora false, se implementará con SharedPreferences
    }

    /**
     * ✅ OBTENER MEJOR PUNTAJE PREMIUM
     */
    fun getBestPremiumScore(): Int {
        // TODO: Obtener de SharedPreferences
        return 0 // Se implementará con SharedPreferences
    }



    /**
     * ✅ LIMPIAR TEXTO PARA TTS (PREMIUM)
     */
    fun cleanPremiumTextForTTS(text: String): String {
        return text
            // Remover emojis específicos premium
            .replace("⭐", "premium")
            .replace("💎", "diamante")
            .replace("🎯", "objetivo")
            .replace("💡", "idea")
            .replace("👨‍🏫", "profesor")
            .replace("🔊", "audio")
            .replace("🔴", "detener")
            // Remover indicadores premium
            .replace("PREMIUM", "premium")
            .replace("Prof.", "Profesor")
            // Aplicar limpieza básica
            .replace(Regex("[\uD83C-\uDBFF\uDC00-\uDFFF]+"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
            .let { cleanedText ->
                if (cleanedText.isNotEmpty() && !cleanedText.endsWith(".") &&
                    !cleanedText.endsWith("!") && !cleanedText.endsWith("?")) {
                    "$cleanedText."
                } else {
                    cleanedText
                }
            }
    }

    /**
     * ✅ FUNCIÓN ESPECÍFICA PARA LIMPIAR FEEDBACK PREMIUM PARA TTS
     */
    fun cleanPremiumFeedbackForTTS(
        feedbackMessage: String,
        feedbackTip: String,
        teacherPersonalization: String = ""
    ): String {
        val cleanedMessage = cleanPremiumTextForTTS(feedbackMessage)
        val cleanedTip = if (feedbackTip.isNotEmpty()) {
            "Estrategia premium: ${cleanPremiumTextForTTS(feedbackTip)}"
        } else ""
        val cleanedPersonalization = if (teacherPersonalization.isNotEmpty()) {
            "Comentario del profesor: ${cleanPremiumTextForTTS(teacherPersonalization)}"
        } else ""

        return buildString {
            append(cleanedMessage)
            if (cleanedTip.isNotEmpty()) {
                append(". $cleanedTip")
            }
            if (cleanedPersonalization.isNotEmpty()) {
                append(". $cleanedPersonalization")
            }
        }.trim()
    }

    // ✅ FUNCIÓN PARA EXPORTAR DATOS PREMIUM
    fun exportPremiumProgressData(): String {
        // TODO: Implementar exportación de datos premium
        return buildString {
            append("=== REPORTE PREMIUM ICFES ===\n\n")
            append("Profesor: $currentTeacherName\n")
            append("Módulo: $currentModuleName\n")
            append("Tipo de sesión: ${if (isEvaluationMode) "Evaluación" else "Práctica"}\n")
            append("Puntaje actual: $score\n")
            append("Preguntas completadas: $totalQuestionsAnswered/${currentQuestions.size}\n")
            append("Tiempo de sesión: ${(System.currentTimeMillis() - sessionStartTime) / 60000} minutos\n\n")

            premiumEvaluationResult?.let { result ->
                append("=== RESULTADO DE EVALUACIÓN PREMIUM ===\n")
                append("Puntaje Premium: ${result.puntajePremium}/100\n")
                append("Nivel: ${result.nivel}\n")
                append("Porcentaje: ${"%.1f".format(result.percentage)}%\n")
                append("Respuestas correctas: ${result.correctAnswers}/${result.totalQuestions}\n\n")

                if (result.fortalezas.isNotEmpty()) {
                    append("FORTALEZAS:\n")
                    result.fortalezas.forEach { fortaleza ->
                        append("• $fortaleza\n")
                    }
                    append("\n")
                }

                if (result.recomendacionesProfesor.isNotEmpty()) {
                    append("RECOMENDACIONES DEL PROFESOR:\n")
                    result.recomendacionesProfesor.forEach { recomendacion ->
                        append("• $recomendacion\n")
                    }
                }

                result.comparedToICFES?.let { comparison ->
                    append("\n=== COMPARACIÓN CON ICFES BÁSICO ===\n")
                    append("ICFES Básico: ${comparison.icfesScore}/500\n")
                    append("Premium: ${comparison.premiumScore}/100\n")
                    append("Mejora: ${comparison.improvement} puntos\n")
                    append("Recomendación: ${comparison.recommendation}\n")
                }
            }
        }
    }

    // ✅ FUNCIÓN DE LIMPIEZA FINAL
    override fun onCleared() {
        super.onCleared()
        stopTimer()
        premiumAIManager.resetPremiumSession()
        premiumEvaluationManager.resetEvaluation()
    }

}