package com.charlesdev.icfes.student.viewmodel



import android.content.Context

import android.content.SharedPreferences

import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.setValue

import androidx.lifecycle.ViewModel

import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.MutableStateFlow

import kotlinx.coroutines.flow.StateFlow

import kotlinx.coroutines.flow.asStateFlow

import kotlinx.coroutines.launch

import kotlinx.coroutines.withContext

import kotlinx.coroutines.Dispatchers

import com.google.ai.client.generativeai.GenerativeModel

import org.json.JSONObject



import android.app.Activity

import android.content.Intent

import android.speech.RecognizerIntent
import androidx.compose.ui.graphics.Color
import com.charlesdev.icfes.R

import com.charlesdev.icfes.data.Data



import com.charlesdev.icfes.student.data.*

import com.charlesdev.icfes.student.evaluation.ICFESEvaluationManager

import com.charlesdev.icfes.student.evaluation.ICFESEvaluationResult

import kotlin.math.roundToInt



class ICFESQuizViewModel : ViewModel() {



    // ✅ INTEGRACIÓN CON SISTEMA DE EVALUACIÓN

    private val evaluationManager = ICFESEvaluationManager()



    // ✅ NUEVAS VARIABLES PARA EVALUACIÓN

    var isEvaluationMode by mutableStateOf(false)

        private set

    var showEvaluationSummary by mutableStateOf(false)

        private set

    var evaluationResult by mutableStateOf<ICFESEvaluationResult?>(null)

        private set

    var isGeneratingEvaluationFeedback by mutableStateOf(false)

        private set



    // ✅ VARIABLES BÁSICAS DEL QUIZ

    var currentQuestionIndex by mutableStateOf(0)

        private set

    var currentModuleId by mutableStateOf("")

        private set

    var currentAreaId by mutableStateOf("")

        private set

    var userAnswer by mutableStateOf("")

    var showFeedback by mutableStateOf(false)

    var feedbackTitle by mutableStateOf("")

    var feedbackMessage by mutableStateOf("")

    var feedbackTip by mutableStateOf("")

    var isAnswerCorrect by mutableStateOf(false)

    var score by mutableStateOf(0)

        private set

    var evaluationCompleted by mutableStateOf(false)

        private set

    var hasAnsweredCurrentQuestion by mutableStateOf(false)

        private set

    var isValidatingWithAI by mutableStateOf(false)

        private set



    // ✅ VARIABLES ESPECÍFICAS ICFES

    var sessionType by mutableStateOf(SessionType.PRACTICE)

        private set

    var timeRemaining by mutableStateOf(0L) // milisegundos

        private set

    var isTimerActive by mutableStateOf(false)

        private set

    var currentModule by mutableStateOf<ICFESModule?>(null)

        private set

    var currentQuestions by mutableStateOf<List<ICFESQuestion>>(emptyList())

        private set

    var studentProfile by mutableStateOf<StudentProfile?>(null)

        private set


    // ✅ VARIABLES PARA SIMULACRO COMPLETO - AGREGAR ESTAS LÍNEAS
    var simulationResult by mutableStateOf<ICFESSimulationResult?>(null)
        private set
    var showSimulationSummary by mutableStateOf(false)
        private set




    // ✅ VARIABLES DE PERSISTENCIA

    private var sharedPrefs: SharedPreferences? = null

    private var totalQuestionsAnswered = 0

    private val answeredQuestions = mutableSetOf<Int>()

    private val questionResponses = mutableListOf<ICFESQuestionResponse>()



    // ✅ STATEFLOWS PARA PROGRESO

    private val _progressState = MutableStateFlow(0)

    val progressState: StateFlow<Int> = _progressState.asStateFlow()



    private val _timeState = MutableStateFlow(0L)

    val timeState: StateFlow<Long> = _timeState.asStateFlow()



    // ✅ ANALYTICS Y APRENDIZAJE

    private var sessionStartTime = 0L

    private var questionStartTime = 0L

    private val questionTimes = mutableListOf<Long>()

    private val errorPatterns = mutableMapOf<String, Int>()

    private var retryCount by mutableStateOf(0)



    // ✅ MODELO DE IA

    private val generativeModel: GenerativeModel = GenerativeModel(

        modelName = "gemini-2.0-flash",

        apiKey = Data.apikey

    )



    // ✅ VARIABLES PARA VOZ

    var isListening by mutableStateOf(false)

        private set

    var voiceInputStatus by mutableStateOf("")



    // ✅ DATA CLASS PARA RESPUESTAS

    data class ICFESQuestionResponse(

        val questionId: String,

        val userAnswer: String,

        val correctAnswer: String,

        val isCorrect: Boolean,

        val timeSpent: Long,

        val difficulty: Difficulty,

        val competency: String,

        val areaId: String

    )



    // ✅ FUNCIONES PARA LIMPIAR TEXTO TTS

    /**

     * Limpia el texto para que sea apropiado para TextToSpeech

     * Remueve emoticones, caracteres especiales y formato markdown

     */

    fun cleanTextForTTS(text: String): String {

        return text

            // Remover emoticones más comunes usando regex

            .replace(Regex("[\uD83C-\uDBFF\uDC00-\uDFFF]+"), "")

            // Remover emoticones específicos que pueden quedar

            .replace("💡", "")

            .replace("🌟", "")

            .replace("📚", "")

            .replace("👍", "")

            .replace("📖", "")

            .replace("🎯", "")

            .replace("🔍", "")

            .replace("⚡", "")

            .replace("🔊", "")

            .replace("✅", "")

            .replace("❌", "")

            .replace("📝", "")

            .replace("🎉", "")

            .replace("💪", "")

            .replace("🚀", "")

            .replace("🎙️", "")

            .replace("🎵", "")

            .replace("📊", "")

            .replace("⭐", "")

            .replace("🏆", "")

            // Remover símbolos unicode adicionales

            .replace(Regex("[\u2600-\u27BF]"), "") // Símbolos varios

            .replace(Regex("[\u2B05-\u2B07]"), "") // Flechas

            .replace(Regex("[\u2934-\u2935]"), "") // Flechas curvas

            // Remover markdown y caracteres de formato

            .replace("**", "")

            .replace("*", "")

            .replace("__", "")

            .replace("_", "")

            .replace("#", "")

            .replace("`", "")

            .replace("~~", "")

            // Reemplazar caracteres especiales con texto equivalente

            .replace("→", " hacia ")

            .replace("←", " desde ")

            .replace("↑", " arriba ")

            .replace("↓", " abajo ")

            .replace("•", ". ")

            .replace("·", ". ")

            .replace("–", " ")

            .replace("—", " ")

            .replace("&", " y ")

            .replace("+", " más ")

            .replace("%", " por ciento")

            .replace("°", " grados")

            .replace("²", " al cuadrado")

            .replace("³", " al cubo")

            .replace("×", " por ")

            .replace("÷", " dividido ")

            // Reemplazar abreviaciones comunes

            .replace("IA", "Inteligencia Artificial")

            .replace("AI", "Inteligencia Artificial")

            .replace("vs", "versus")

            .replace("etc", "etcétera")

            // Limpiar múltiples espacios y saltos de línea

            .replace(Regex("\\s+"), " ")

            .replace(Regex("\\n+"), ". ")

            // Limpiar puntuación duplicada

            .replace(Regex("\\.{2,}"), ".")

            .replace(Regex(",{2,}"), ",")

            .replace("...", ".")

            // Remover espacios al inicio y final

            .trim()

            // Asegurar que termine con punto para una pausa natural

            .let { cleanedText ->

                if (cleanedText.isNotEmpty() && !cleanedText.endsWith(".") && !cleanedText.endsWith("!") && !cleanedText.endsWith("?")) {

                    "$cleanedText."

                } else {

                    cleanedText

                }

            }

    }



    /**

     * Función específica para limpiar feedback de ICFES para TTS

     */

    fun cleanICFESFeedbackForTTS(feedbackMessage: String, feedbackTip: String): String {

        val cleanedMessage = cleanTextForTTS(feedbackMessage)

        val cleanedTip = if (feedbackTip.isNotEmpty()) {

            "Estrategia: ${cleanTextForTTS(feedbackTip)}"

        } else ""



        return buildString {

            append(cleanedMessage)

            if (cleanedTip.isNotEmpty()) {

                append(". $cleanedTip")

            }

        }.trim()

    }



    // ✅ FUNCIÓN DE INICIALIZACIÓN MODIFICADA

    // ✅ FUNCIÓN DE INICIALIZACIÓN MODIFICADA
    fun initializeSession(
        context: Context,
        moduleId: String,
        sessionType: SessionType = SessionType.PRACTICE,
        areaId: String? = null
    ) {
        initializePreferences(context)
        sessionStartTime = System.currentTimeMillis()
        questionStartTime = System.currentTimeMillis()

        this.sessionType = sessionType
        this.currentModuleId = moduleId
        this.currentAreaId = areaId ?: ""

        // ✅ DETERMINAR SI ES EVALUACIÓN
        isEvaluationMode = sessionType == SessionType.SIMULATION ||
                sessionType == SessionType.TIMED_QUIZ

        // ✅ MANEJAR SIMULACRO COMPLETO
        if (moduleId == "simulation" || moduleId == "simulacro_completo") {
            setupFullSimulation()
        } else if (isEvaluationMode) {
            // ✅ USAR SISTEMA DE EVALUACIÓN PARA MÓDULOS INDIVIDUALES
            currentQuestions = evaluationManager.startEvaluation(moduleId, sessionType, 20)
            currentModule = populatedICFESModules.find { it.id == moduleId }
        } else {
            // ✅ USAR SISTEMA DE PRÁCTICA (COMO ANTES)
            loadModule(moduleId, areaId)
        }

        // ✅ Configurar timer si es necesario (solo para módulos individuales)
        if (sessionType == SessionType.SIMULATION || sessionType == SessionType.TIMED_QUIZ) {
            if (moduleId != "simulation" && moduleId != "simulacro_completo") {
                setupTimer() // Solo para módulos individuales
            }
            // Para simulacro completo, el timer ya se configuró en setupFullSimulation()
        }

        loadStudentProfile()
        resetQuiz()
    }



    fun initializePreferences(context: Context) {

        if (sharedPrefs == null) {

            sharedPrefs = context.getSharedPreferences("ICFESPrefs", Context.MODE_PRIVATE)

        }

    }



    // ✅ CARGA DE MÓDULOS Y PREGUNTAS (PARA PRÁCTICA)

    private fun loadModule(moduleId: String, areaId: String?) {

        currentModule = populatedICFESModules.find { it.id == moduleId }



        currentQuestions = if (areaId.isNullOrEmpty()) {

            // Todas las preguntas del módulo

            currentModule?.areas?.flatMap { it.questions } ?: emptyList()

        } else {

            // Solo preguntas de un área específica

            currentModule?.areas?.find { it.id == areaId }?.questions ?: emptyList()

        }



        // Mezclar preguntas si es simulacro

        if (sessionType == SessionType.SIMULATION) {

            currentQuestions = currentQuestions.shuffled()

        }

    }



    // ✅ CONFIGURACIÓN DEL TIMER

    private fun setupTimer() {

        currentModule?.let { module ->

            timeRemaining = module.timeLimit * 60 * 1000L // convertir a milisegundos

            startTimer()

        }

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

                // Tiempo agotado

                finishEvaluation()

            }

        }

    }



    private fun stopTimer() {

        isTimerActive = false

    }



    // ✅ CARGA DE PERFIL DEL ESTUDIANTE

    private fun loadStudentProfile() {

        sharedPrefs?.let { prefs ->

            val profileId = prefs.getString("student_profile_id", null)

            if (profileId != null) {

                studentProfile = StudentProfile(

                    id = profileId,

                    name = prefs.getString("student_name", "Estudiante") ?: "Estudiante",

                    email = prefs.getString("student_email", "") ?: "",

                    institution = prefs.getString("student_institution", "") ?: "",

                    grade = prefs.getString("student_grade", "") ?: "",

                    targetScore = prefs.getInt("student_target_score", 300),

                    currentLevel = prefs.getInt("student_level", 1),

                    experience = prefs.getInt("student_experience", 0),

                    studyStreak = prefs.getInt("student_streak", 0),

                    totalStudyTime = prefs.getLong("student_total_time", 0),

                    lastActivity = prefs.getLong(

                        "student_last_activity",

                        System.currentTimeMillis()

                    )

                )

            }

        }

    }



    // ✅ FUNCIÓN DE VALIDACIÓN CON IA - ADAPTADA PARA ICFES

    private suspend fun validateAnswerWithAI(

        userAnswer: String,

        question: ICFESQuestion,

        isCorrect: Boolean

    ): Pair<Boolean, Triple<String, String, String>> = withContext(Dispatchers.IO) {

        try {

            val promptTemplate = createICFESPrompt(userAnswer, question, isCorrect)



            val response = generativeModel.generateContent(promptTemplate)

            val responseText = response.text ?: throw Exception("Respuesta vacía")



            val jsonText = responseText.trim()

                .removePrefix("```json")

                .removeSuffix("```")

                .trim()



            val jsonResponse = JSONObject(jsonText)

            val aiCorrect = jsonResponse.getBoolean("es_correcto")

            val feedback = jsonResponse.getString("feedback")

            val tip = jsonResponse.optString("consejo", "")

            val title =

                jsonResponse.optString("titulo", if (aiCorrect) "¡Correcto!" else "Incorrecto")



            Pair(aiCorrect, Triple(title, feedback, tip))



        } catch (e: Exception) {

            // Fallback para ICFES

            val fallbackFeedback = createICFESFallback(question, userAnswer, isCorrect)

            Pair(isCorrect, fallbackFeedback)

        }

    }



    // ✅ PROMPT ESPECÍFICO PARA ICFES

    private fun createICFESPrompt(

        userAnswer: String,

        question: ICFESQuestion,

        isCorrect: Boolean

    ): String {

        val modeContext = if (isEvaluationMode) {

            "Esta es una pregunta de evaluación del examen ICFES. Proporciona feedback educativo conciso pero completo."

        } else {

            "Esta es una pregunta de práctica del examen ICFES. Proporciona feedback educativo detallado para el aprendizaje."

        }



        val moduleContext = when (currentModuleId) {

            "lectura_critica" -> "pregunta de comprensión lectora del examen ICFES"

            "matematicas" -> "problema matemático del examen ICFES"

            "ciencias_naturales" -> "pregunta de ciencias naturales del examen ICFES"

            "sociales_ciudadanas" -> "pregunta de sociales y ciudadanas del examen ICFES"

            "ingles" -> "pregunta de inglés del examen ICFES"

            else -> "pregunta del examen ICFES"

        }



        return """

        Eres un tutor experto en preparación para el examen ICFES (Saber 11) de Colombia.

        

        CONTEXTO: $modeContext

        TIPO: Esta es una $moduleContext

        COMPETENCIA: ${question.competency}

        DIFICULTAD: ${question.difficulty.displayName}

        

        ${if (question.context != null) "TEXTO BASE:\n${question.context}\n" else ""}

        

        PREGUNTA: ${question.question}

        

        OPCIONES:

        ${question.options.joinToString("\n")}

        

        RESPUESTA DEL ESTUDIANTE: ${userAnswer}

        RESPUESTA CORRECTA: ${question.correctAnswer}

        RESULTADO: ${if (isCorrect) "CORRECTO" else "INCORRECTO"}

        

        INSTRUCCIONES:

        - Proporciona feedback educativo específico para la preparación del ICFES

        - Si es CORRECTO: Refuerza la estrategia usada y conecta con otros temas del ICFES

        - Si es INCORRECTO: Explica el error, da la respuesta correcta y sugiere estrategias

        - Usa lenguaje motivador apropiado para estudiantes de grado 11

        - Incluye consejos específicos para el examen ICFES

        - ${if (isEvaluationMode) "Máximo 80 palabras" else "Máximo 120 palabras"} en el feedback principal

        - NO uses emoticones en tu respuesta

        

        Responde en JSON:

        {

            "es_correcto": ${isCorrect},

            "titulo": "Título motivador apropiado sin emoticones",

            "feedback": "Explicación educativa específica para ICFES sin emoticones",

            "consejo": "Estrategia específica para este tipo de preguntas sin emoticones"

        }

        """.trimIndent()

    }



    // ✅ FALLBACK PARA ICFES

    private fun createICFESFallback(

        question: ICFESQuestion,

        userAnswer: String,

        isCorrect: Boolean

    ): Triple<String, String, String> {

        return if (isCorrect) {

            Triple(

                "Correcto",

                "Excelente trabajo. ${question.explanation}",

                "Continúa practicando este tipo de preguntas para fortalecer tus habilidades ICFES."

            )

        } else {

            val incorrectFeedback = question.feedback.incorrect[userAnswer]

                ?: "La respuesta correcta es ${question.correctAnswer}."

            Triple(

                "Incorrecto",

                "$incorrectFeedback\n\nExplicación: ${question.explanation}",

                question.feedback.tip

            )

        }

    }



    // ✅ FUNCIÓN DE ENVÍO DE RESPUESTA MODIFICADA

    fun submitAnswer(currentQuestion: ICFESQuestion) {

        if (hasAnsweredCurrentQuestion) return



        if (isEvaluationMode) {

            // ✅ MODO EVALUACIÓN: Sin feedback inmediato

            evaluationManager.submitEvaluationAnswer(

                questionIndex = currentQuestionIndex,

                question = currentQuestion,

                userAnswer = userAnswer

            )



            hasAnsweredCurrentQuestion = true

            showFeedback = false // ✅ NO mostrar feedback inmediato



            // Actualizar progreso básico

            totalQuestionsAnswered++

            updateProgressInRealTime()



            // Preparar siguiente pregunta

            questionStartTime = System.currentTimeMillis()



        } else {

            // ✅ MODO PRÁCTICA: Con feedback inmediato (MANTENER COMO ESTÁ)

            val responseTime = System.currentTimeMillis() - questionStartTime

            questionTimes.add(responseTime)



            val isCorrectBasic = userAnswer.equals(currentQuestion.correctAnswer, ignoreCase = true)



            questionResponses.add(

                ICFESQuestionResponse(

                    questionId = currentQuestion.id,

                    userAnswer = userAnswer,

                    correctAnswer = currentQuestion.correctAnswer,

                    isCorrect = isCorrectBasic,

                    timeSpent = responseTime,

                    difficulty = currentQuestion.difficulty,

                    competency = currentQuestion.competency,

                    areaId = currentAreaId

                )

            )



            // Validar con IA en práctica

            isValidatingWithAI = true

            viewModelScope.launch {

                try {

                    val (aiCorrect, feedbackTriple) = validateAnswerWithAI(

                        userAnswer = userAnswer,

                        question = currentQuestion,

                        isCorrect = isCorrectBasic

                    )



                    isAnswerCorrect = aiCorrect

                    feedbackTitle = feedbackTriple.first

                    feedbackMessage = feedbackTriple.second

                    feedbackTip = feedbackTriple.third



                    // Bonus por velocidad

                    if (isAnswerCorrect && responseTime < currentQuestion.timeEstimated * 1000) {

                        feedbackMessage += "\n\nExcelente velocidad de respuesta!"

                    }



                    if (isAnswerCorrect) {

                        score += currentQuestion.difficulty.points

                        updateExperience(currentQuestion.difficulty.points * 10)

                    }



                } catch (e: Exception) {

                    isAnswerCorrect = isCorrectBasic

                    val fallback = createICFESFallback(currentQuestion, userAnswer, isCorrectBasic)

                    feedbackTitle = fallback.first

                    feedbackMessage = fallback.second

                    feedbackTip = fallback.third

                } finally {

                    isValidatingWithAI = false

                    completeFeedbackProcess(currentQuestion)

                }

            }

        }

    }



    // ✅ COMPLETAR PROCESO DE FEEDBACK

    private fun completeFeedbackProcess(currentQuestion: ICFESQuestion) {

        // Registrar patrones de error

        if (!isAnswerCorrect) {

            val errorKey = "${currentQuestion.competency}_${currentQuestion.difficulty}"

            errorPatterns[errorKey] = errorPatterns.getOrDefault(errorKey, 0) + 1

        }



        showFeedback = true

        totalQuestionsAnswered++

        answeredQuestions.add(currentQuestionIndex)

        hasAnsweredCurrentQuestion = true



        // Actualizar progreso

        updateProgressInRealTime()



        // Verificar logros

        checkAchievements()



        // Preparar siguiente pregunta

        questionStartTime = System.currentTimeMillis()

    }



    // ✅ ACTUALIZACIÓN DE PROGRESO

    private fun updateProgressInRealTime() {

        if (totalQuestionsAnswered > 0) {

            val currentProgress = (score.toFloat() / (totalQuestionsAnswered * 2) * 100).toInt()

            _progressState.value = currentProgress

        }

    }



    // ✅ SISTEMA DE EXPERIENCIA

    private fun updateExperience(points: Int) {

        studentProfile?.let { profile ->

            val newExperience = profile.experience + points

            val newLevel = calculateLevel(newExperience)



            studentProfile = profile.copy(

                experience = newExperience,

                currentLevel = newLevel

            )



            // Guardar en preferencias

            sharedPrefs?.edit()?.apply {

                putInt("student_experience", newExperience)

                putInt("student_level", newLevel)

                apply()

            }

        }

    }



    private fun calculateLevel(experience: Int): Int {

        return (experience / 1000) + 1

    }



    // ✅ VERIFICACIÓN DE LOGROS

    private fun checkAchievements() {

        // Verificar primer quiz

        if (totalQuestionsAnswered == 1) {

            unlockAchievement("first_quiz")

        }



        // Verificar puntaje perfecto

        if (score == totalQuestionsAnswered * 3) { // 3 puntos por pregunta difícil

            unlockAchievement("perfect_score")

        }



        // Verificar velocidad

        if (questionTimes.size >= 10) {

            val avgTime = questionTimes.takeLast(10).average()

            if (avgTime < 30000) { // menos de 30 segundos promedio

                unlockAchievement("speed_demon")

            }

        }

    }



    private fun unlockAchievement(achievementId: String) {

        sharedPrefs?.edit()?.apply {

            putBoolean("achievement_$achievementId", true)

            putLong("achievement_${achievementId}_time", System.currentTimeMillis())

            apply()

        }

    }



    // ✅ NAVEGACIÓN ENTRE PREGUNTAS MODIFICADA

    fun nextQuestion() {

        if (currentQuestionIndex < currentQuestions.size - 1) {

            currentQuestionIndex++

            userAnswer = ""

            showFeedback = false



            if (isEvaluationMode) {

                // En evaluación, no verificar respuestas previas de la misma forma

                hasAnsweredCurrentQuestion = false

            } else {

                hasAnsweredCurrentQuestion = answeredQuestions.contains(currentQuestionIndex)

            }



            voiceInputStatus = ""

            questionStartTime = System.currentTimeMillis()

        } else {

            finishEvaluation()

        }

    }



    fun previousQuestion() {

        if (currentQuestionIndex > 0) {

            currentQuestionIndex--

            userAnswer = ""

            showFeedback = false



            if (isEvaluationMode) {

                hasAnsweredCurrentQuestion = false

            } else {

                hasAnsweredCurrentQuestion = answeredQuestions.contains(currentQuestionIndex)

            }



            voiceInputStatus = ""

        }

    }



    // ✅ FINALIZACIÓN DE EVALUACIÓN MODIFICADA

    private fun finishEvaluation() {
        evaluationCompleted = true
        stopTimer()

        // ✅ DETECTAR SI ES SIMULACRO COMPLETO
        if (currentModuleId == "simulacro_completo") {
            // SIMULACRO COMPLETO
            val result = calculateFullSimulationScore()
            simulationResult = result
            showSimulationSummary = true

            // Guardar progreso del simulacro
            saveSimulationProgress(result)

        } else if (isEvaluationMode) {
            // EVALUACIÓN DE MÓDULO INDIVIDUAL
            isGeneratingEvaluationFeedback = true
            viewModelScope.launch {
                try {
                    val result = evaluationManager.generateConsolidatedFeedback()
                    evaluationResult = result
                    showEvaluationSummary = true

                    // Guardar progreso de evaluación
                    saveEvaluationProgress(result)

                } catch (e: Exception) {
                    // Error en generación de feedback - usar fallback
                    evaluationResult = generateBasicEvaluationResult()
                    showEvaluationSummary = true
                } finally {
                    isGeneratingEvaluationFeedback = false
                }
            }
        } else {
            // PRÁCTICA NORMAL
            val finalScore = calculateICFESScore()
            saveProgress(finalScore)
        }

        updateStudyStreak()
    }

    // ✅ GUARDAR PROGRESO DEL SIMULACRO COMPLETO
    private fun saveSimulationProgress(result: ICFESSimulationResult) {
        sharedPrefs?.let { prefs ->
            val editor = prefs.edit()

            // Guardar resultado del simulacro completo
            editor.putInt("simulation_global_score", result.globalScore)
            editor.putFloat("simulation_percentage", result.percentage)
            editor.putString("simulation_nivel", result.nivel)
            editor.putLong("simulation_timestamp", result.completedAt)
            editor.putLong("simulation_time_spent", result.timeSpent)
            editor.putInt("simulation_total_questions", result.totalQuestions)
            editor.putInt("simulation_correct_answers", result.correctAnswers)

            // Guardar puntajes por módulo del simulacro
            result.moduleScores.forEach { (moduleId, moduleScore) ->
                editor.putInt("simulation_${moduleId}_score", moduleScore.puntajeICFES)
                editor.putFloat("simulation_${moduleId}_percentage", moduleScore.percentage)
                editor.putInt("simulation_${moduleId}_correct", moduleScore.correctAnswers)
                editor.putInt("simulation_${moduleId}_total", moduleScore.totalQuestions)
            }

            // Actualizar contador de simulacros completados
            val totalSimulations = prefs.getInt("total_simulations", 0)
            editor.putInt("total_simulations", totalSimulations + 1)

            // Experiencia por completar simulacro completo
            updateExperience(300) // Bonus grande por simulacro completo

            editor.apply()
        }
    }

    // ✅ FUNCIONES PARA CONSULTAR DATOS DEL SIMULACRO
    fun getSimulationStats(): Map<String, Any> {
        return sharedPrefs?.let { prefs ->
            val stats = mutableMapOf<String, Any>()

            stats["global_score"] = prefs.getInt("simulation_global_score", 0)
            stats["percentage"] = prefs.getFloat("simulation_percentage", 0f)
            stats["nivel"] = prefs.getString("simulation_nivel", "No evaluado") ?: "No evaluado"
            stats["total_simulations"] = prefs.getInt("total_simulations", 0)

            // Puntajes por módulo
            populatedICFESModules.forEach { module ->
                val moduleScore = prefs.getInt("simulation_${module.id}_score", 0)
                val modulePercentage = prefs.getFloat("simulation_${module.id}_percentage", 0f)

                stats["simulation_${module.id}"] = mapOf(
                    "score" to moduleScore,
                    "percentage" to modulePercentage
                )
            }

            stats
        } ?: emptyMap()
    }

    fun hasSimulationExperience(): Boolean {
        return sharedPrefs?.getInt("total_simulations", 0) ?: 0 > 0
    }

    fun getLastSimulationScore(): Int {
        return sharedPrefs?.getInt("simulation_global_score", 0) ?: 0
    }


    // ✅ FUNCIONES PARA MANEJAR RESUMEN DEL SIMULACRO
    fun closeSimulationSummary() {
        showSimulationSummary = false
    }

    fun retrySimulation() {
        simulationResult = null
        showSimulationSummary = false
        resetQuiz()

        // Reinicializar simulacro con nuevas preguntas
        setupFullSimulation()
    }




    // ✅ GUARDAR PROGRESO DE EVALUACIÓN Y ACTUALIZAR PROGRESO GENERAL

    private fun saveEvaluationProgress(result: ICFESEvaluationResult) {

        sharedPrefs?.let { prefs ->

            val editor = prefs.edit()



            // Guardar mejor puntaje de evaluación

            val currentBest = prefs.getInt("eval_score_${currentModuleId}", 0)

            if (result.puntajeICFES > currentBest) {

                editor.putInt("eval_score_${currentModuleId}", result.puntajeICFES)

                editor.putFloat("eval_percentage_${currentModuleId}", result.percentage)

                editor.putString("eval_level_${currentModuleId}", result.nivel)

                editor.putLong("eval_timestamp_${currentModuleId}", System.currentTimeMillis())

            }



            // Actualizar estadísticas generales

            val totalEvaluations = prefs.getInt("total_evaluations", 0)

            editor.putInt("total_evaluations", totalEvaluations + 1)



            // Guardar progreso general (progreso global y módulo) si el puntaje de evaluación es mejor que el de práctica

            val moduleKey = "icfes_score_${currentModuleId}"

            val currentBestScore = prefs.getInt(moduleKey, 0)

            if (result.puntajeICFES > currentBestScore) {

                editor.putInt(moduleKey, result.puntajeICFES)

                editor.putFloat("icfes_percentage_${currentModuleId}", result.percentage)

                editor.putInt("icfes_total_questions_${currentModuleId}", result.totalQuestions)

                editor.putInt("icfes_raw_score_${currentModuleId}", result.correctAnswers)

                editor.putInt("icfes_percentile_${currentModuleId}", when {

                    result.puntajeICFES >= 400 -> 95

                    result.puntajeICFES >= 350 -> 80

                    result.puntajeICFES >= 300 -> 60

                    result.puntajeICFES >= 250 -> 40

                    else -> 20

                })

                editor.putLong("icfes_timestamp_${currentModuleId}", System.currentTimeMillis())

                editor.putLong("icfes_time_spent_${currentModuleId}", result.timeSpent)

                // ¡Actualiza progreso general!

                updateOverallProgress(editor, prefs)

            }



            // Experiencia por completar evaluación

            updateExperience(100) // Bonus por completar evaluación



            editor.apply()

        }

    }





    // ✅ FUNCIÓN FALLBACK PARA EVALUACIÓN

    private fun generateBasicEvaluationResult(): ICFESEvaluationResult {

        val correctCount = currentQuestions.size // Placeholder - calcular real

        val percentage = (correctCount.toFloat() / currentQuestions.size * 100)

        val puntajeICFES = (percentage * 5).toInt().coerceIn(0, 500)



        return ICFESEvaluationResult(

            moduleId = currentModuleId,

            moduleName = currentModule?.name ?: "",

            totalQuestions = currentQuestions.size,

            correctAnswers = correctCount,

            percentage = percentage,

            timeSpent = System.currentTimeMillis() - sessionStartTime,

            puntajeICFES = puntajeICFES,

            nivel = when {

                percentage >= 80 -> "Alto"

                percentage >= 60 -> "Medio"

                else -> "Bajo"

            },

            fortalezas = listOf("Completaste la evaluación"),

            debilidades = listOf("Revisa las preguntas incorrectas"),

            recomendaciones = listOf("Continúa practicando", "Estudia los conceptos básicos"),

            estrategias = listOf("Administra mejor el tiempo", "Lee con cuidado"),

            analisisGeneral = "Evaluación completada satisfactoriamente.",

            competencyScores = emptyMap(),

            difficultyScores = emptyMap(),

            evaluationDate = System.currentTimeMillis()

        )

    }



    // ✅ FUNCIÓN PARA CERRAR RESUMEN DE EVALUACIÓN

    fun closeEvaluationSummary() {

        showEvaluationSummary = false

    }



    // ✅ FUNCIÓN PARA REINTENTAR EVALUACIÓN

    fun retryEvaluation() {

        evaluationManager.resetEvaluation()

        showEvaluationSummary = false

        evaluationResult = null

        resetQuiz()



        // Reinicializar con nuevas preguntas

        currentQuestions = evaluationManager.startEvaluation(currentModuleId, sessionType, 20)

        setupTimer()

    }



    // ✅ CÁLCULO DE PUNTAJE ICFES (SOLO PARA PRÁCTICA)

    private fun calculateICFESScore(): ICFESScore {

        val totalQuestions = currentQuestions.size

        val correctAnswers = questionResponses.count { it.isCorrect }

        val percentage =

            if (totalQuestions > 0) (correctAnswers.toFloat() / totalQuestions * 100) else 0f



        // Convertir a escala ICFES (0-500)

        val globalScore = (percentage * 5).roundToInt()



        // Calcular percentil (simulado)

        val percentile = when {

            globalScore >= 400 -> 95

            globalScore >= 350 -> 80

            globalScore >= 300 -> 60

            globalScore >= 250 -> 40

            else -> 20

        }



        // Calcular puntajes por área

        val areaScores = calculateAreaScores()



        // Identificar fortalezas y debilidades

        val (strengths, weaknesses) = analyzePerformance()



        return ICFESScore(

            moduleId = currentModuleId,

            globalScore = globalScore,

            rawScore = correctAnswers,

            totalQuestions = totalQuestions,

            percentage = percentage,

            percentile = percentile,

            areaScores = areaScores,

            timeSpent = System.currentTimeMillis() - sessionStartTime,

            efficiency = if (questionTimes.isNotEmpty()) score.toFloat() / (questionTimes.average() / 60000) else 0f,

            strengths = strengths,

            weaknesses = weaknesses,

            recommendations = generateRecommendations(percentage, weaknesses)

        )

    }



    // ✅ CÁLCULO DE PUNTAJES POR ÁREA

    private fun calculateAreaScores(): Map<String, AreaScore> {

        val areaScores = mutableMapOf<String, AreaScore>()



        currentModule?.areas?.forEach { area ->

            val areaResponses = questionResponses.filter { it.areaId == area.id }

            val correctCount = areaResponses.count { it.isCorrect }

            val totalCount = areaResponses.size

            val percentage = if (totalCount > 0) (correctCount.toFloat() / totalCount * 100) else 0f



            val difficultyBreakdown = mutableMapOf<Difficulty, Int>()

            Difficulty.values().forEach { difficulty ->

                difficultyBreakdown[difficulty] = areaResponses.count {

                    it.difficulty == difficulty && it.isCorrect

                }

            }



            areaScores[area.id] = AreaScore(

                areaId = area.id,

                score = correctCount,

                total = totalCount,

                percentage = percentage,

                timeSpent = areaResponses.sumOf { it.timeSpent },

                difficulty = difficultyBreakdown

            )

        }



        return areaScores

    }



    // ✅ ANÁLISIS DE RENDIMIENTO

    private fun analyzePerformance(): Pair<List<String>, List<String>> {

        val strengths = mutableListOf<String>()

        val weaknesses = mutableListOf<String>()



        // Analizar por competencias

        val competencyPerformance = questionResponses.groupBy { it.competency }

        competencyPerformance.forEach { (competency, responses) ->

            val correctRate = responses.count { it.isCorrect }.toFloat() / responses.size

            if (correctRate >= 0.8) {

                strengths.add(competency)

            } else if (correctRate < 0.6) {

                weaknesses.add(competency)

            }

        }



        return Pair(strengths, weaknesses)

    }



    // ✅ GENERACIÓN DE RECOMENDACIONES

    private fun generateRecommendations(percentage: Float, weaknesses: List<String>): List<String> {

        val recommendations = mutableListOf<String>()



        when {

            percentage >= 80 -> {

                recommendations.add("Excelente nivel. Continúa practicando para mantener tu rendimiento.")

                recommendations.add("Enfócate en simulacros completos para familiarizarte con el formato.")

            }

            percentage >= 60 -> {

                recommendations.add("Buen nivel. Refuerza las áreas más débiles.")

                recommendations.add("Practica con tiempo limitado para mejorar tu velocidad.")

            }

            else -> {

                recommendations.add("Necesitas reforzar conceptos fundamentales.")

                recommendations.add("Practica más preguntas de nivel fácil y medio.")

            }

        }



        // Recomendaciones específicas por debilidades

        weaknesses.forEach { weakness ->

            recommendations.add("Estudia más sobre: $weakness")

        }



        return recommendations

    }



    // ✅ GUARDAR PROGRESO (SOLO PARA PRÁCTICA)

    private fun saveProgress(icfesScore: ICFESScore) {

        sharedPrefs?.let { prefs ->

            val editor = prefs.edit()



            // Guardar puntaje del módulo

            val moduleKey = "icfes_score_${currentModuleId}"

            val currentBestScore = prefs.getInt(moduleKey, 0)



            if (icfesScore.globalScore > currentBestScore) {

                editor.putInt(moduleKey, icfesScore.globalScore)

                editor.putInt("icfes_raw_score_${currentModuleId}", icfesScore.rawScore)

                editor.putInt("icfes_total_questions_${currentModuleId}", icfesScore.totalQuestions)

                editor.putFloat("icfes_percentage_${currentModuleId}", icfesScore.percentage)

                editor.putInt("icfes_percentile_${currentModuleId}", icfesScore.percentile)

                editor.putLong("icfes_timestamp_${currentModuleId}", System.currentTimeMillis())

                editor.putLong("icfes_time_spent_${currentModuleId}", icfesScore.timeSpent)

            }



            // Actualizar progreso general

            updateOverallProgress(editor, prefs)



            editor.apply()

        }

    }



    // ✅ ACTUALIZAR PROGRESO GENERAL

    private fun updateOverallProgress(editor: SharedPreferences.Editor, prefs: SharedPreferences) {

        val moduleScores = populatedICFESModules.map { module ->

            prefs.getInt("icfes_score_${module.id}", 0)

        }



        val completedModules = moduleScores.count { it > 0 }

        val averageScore = if (completedModules > 0) moduleScores.sum() / completedModules else 0



        editor.putInt("icfes_overall_score", averageScore)

        editor.putInt("icfes_completed_modules", completedModules)

    }



    // ✅ ACTUALIZAR RACHA DE ESTUDIO

    private fun updateStudyStreak() {

        sharedPrefs?.let { prefs ->

            val editor = prefs.edit()

            val today = System.currentTimeMillis() / (1000 * 60 * 60 * 24)

            val lastStudyDay = prefs.getLong("last_study_day", 0)



            if (today - lastStudyDay == 1L) {

                // Día consecutivo

                val currentStreak = prefs.getInt("student_streak", 0)

                editor.putInt("student_streak", currentStreak + 1)

            } else if (today - lastStudyDay > 1L) {

                // Se rompió la racha

                editor.putInt("student_streak", 1)

            }



            editor.putLong("last_study_day", today)

            editor.putLong("student_last_activity", System.currentTimeMillis())

            editor.apply()

        }

    }



    // ✅ FUNCIÓN PARA CALCULAR PUNTAJE DE SIMULACRO COMPLETO
    private fun calculateFullSimulationScore(): ICFESSimulationResult {
        val totalQuestions = currentQuestions.size
        val correctAnswers = if (isEvaluationMode) {
            // En evaluación, calcular desde evaluationManager
            evaluationManager.evaluationAnswers.values.count { answer ->
                answer.userAnswer.equals(answer.question.correctAnswer, ignoreCase = true)
            }
        } else {
            // En práctica, desde questionResponses
            questionResponses.count { it.isCorrect }
        }

        val percentage = if (totalQuestions > 0) (correctAnswers.toFloat() / totalQuestions * 100) else 0f

        // Calcular puntajes por módulo
        val moduleScores = calculateModuleScores()

        // Puntaje global ICFES (promedio de módulos)
        val globalScore = moduleScores.values.map { it.puntajeICFES }.average().toInt()

        return ICFESSimulationResult(
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            percentage = percentage,
            globalScore = globalScore,
            moduleScores = moduleScores,
            timeSpent = System.currentTimeMillis() - sessionStartTime,
            nivel = when {
                percentage >= 80 -> "Superior"
                percentage >= 70 -> "Alto"
                percentage >= 60 -> "Medio"
                else -> "Bajo"
            },
            completedAt = System.currentTimeMillis()
        )
    }

    // ✅ FUNCIÓN PARA CALCULAR PUNTAJES POR MÓDULO EN SIMULACRO
    private fun calculateModuleScores(): Map<String, ModuleSimulationScore> {
        val moduleScores = mutableMapOf<String, ModuleSimulationScore>()

        // Agrupar preguntas por módulo original
        val questionsByModule = currentQuestions.groupBy { question ->
            when {
                question.competency.contains("Comprensión", ignoreCase = true) ||
                        question.competency.contains("Pensamiento crítico", ignoreCase = true) -> "lectura_critica"

                question.competency.contains("Álgebra", ignoreCase = true) ||
                        question.competency.contains("Geometría", ignoreCase = true) ||
                        question.competency.contains("Estadística", ignoreCase = true) -> "matematicas"

                question.competency.contains("Biología", ignoreCase = true) ||
                        question.competency.contains("Física", ignoreCase = true) ||
                        question.competency.contains("Química", ignoreCase = true) -> "ciencias_naturales"

                question.competency.contains("Historia", ignoreCase = true) ||
                        question.competency.contains("Geografía", ignoreCase = true) ||
                        question.competency.contains("Constitución", ignoreCase = true) -> "sociales_ciudadanas"

                question.competency.contains("Reading", ignoreCase = true) ||
                        question.competency.contains("Use of English", ignoreCase = true) -> "ingles"

                else -> "otros"
            }
        }

        questionsByModule.forEach { (moduleId, questions) ->
            if (moduleId != "otros") {
                val correctInModule = if (isEvaluationMode) {
                    evaluationManager.evaluationAnswers.values.count { answer ->
                        questions.any { it.id == answer.questionId } &&
                                answer.userAnswer.equals(answer.question.correctAnswer, ignoreCase = true)
                    }
                } else {
                    questionResponses.count { response ->
                        questions.any { it.id == response.questionId } && response.isCorrect
                    }
                }

                val totalInModule = questions.size
                val percentageInModule = if (totalInModule > 0) (correctInModule.toFloat() / totalInModule * 100) else 0f
                val puntajeICFES = (percentageInModule * 5).toInt().coerceIn(0, 500)

                val moduleName = populatedICFESModules.find { it.id == moduleId }?.name ?: moduleId

                moduleScores[moduleId] = ModuleSimulationScore(
                    moduleId = moduleId,
                    moduleName = moduleName,
                    totalQuestions = totalInModule,
                    correctAnswers = correctInModule,
                    percentage = percentageInModule,
                    puntajeICFES = puntajeICFES
                )
            }
        }

        return moduleScores
    }



    // ✅ FUNCIONES DE VOZ

    fun createVoiceIntent(): Intent {

        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {

            putExtra(

                RecognizerIntent.EXTRA_LANGUAGE_MODEL,

                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM

            )

            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")

            putExtra(RecognizerIntent.EXTRA_PROMPT, "Explica tu respuesta...")

            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

        }

    }



    fun handleVoiceResult(resultCode: Int, data: Intent?) {

        isListening = false

        if (resultCode == Activity.RESULT_OK) {

            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

            if (!results.isNullOrEmpty()) {

                userAnswer = results[0]

                voiceInputStatus = "Transcripción completada"

            } else {

                voiceInputStatus = "No se pudo transcribir"

            }

        } else {

            voiceInputStatus = "Error de reconocimiento"

        }

    }



    fun startListening() {

        isListening = true

        voiceInputStatus = "Escuchando..."

    }



    fun stopVoiceInput() {

        isListening = false

        voiceInputStatus = ""

    }



    // ✅ RESET Y UTILIDADES MODIFICADO

    fun resetQuiz() {
        currentQuestionIndex = 0
        userAnswer = ""
        showFeedback = false
        score = 0
        evaluationCompleted = false
        totalQuestionsAnswered = 0
        hasAnsweredCurrentQuestion = false
        answeredQuestions.clear()
        questionResponses.clear()
        _progressState.value = 0
        isValidatingWithAI = false
        voiceInputStatus = ""

        // ✅ RESET ESPECÍFICO PARA EVALUACIÓN
        showEvaluationSummary = false
        evaluationResult = null
        isGeneratingEvaluationFeedback = false

        // ✅ RESET ESPECÍFICO PARA SIMULACRO - AGREGAR ESTAS LÍNEAS
        showSimulationSummary = false
        simulationResult = null

        // Reset analytics
        questionTimes.clear()
        errorPatterns.clear()
        sessionStartTime = System.currentTimeMillis()
        questionStartTime = System.currentTimeMillis()
    }



    fun closeFeedback() {

        showFeedback = false

    }



    // ✅ FUNCIONES DE CONSULTA

    fun getCurrentQuestion(): ICFESQuestion? {

        return if (currentQuestionIndex < currentQuestions.size) {

            currentQuestions[currentQuestionIndex]

        } else null

    }



    fun getProgressStats(): Map<String, Any> {

        return sharedPrefs?.let { prefs ->

            val stats = mutableMapOf<String, Any>()



            populatedICFESModules.forEach { module ->

                val score = prefs.getInt("icfes_score_${module.id}", 0)

                val percentage = prefs.getFloat("icfes_percentage_${module.id}", 0f)

                val percentile = prefs.getInt("icfes_percentile_${module.id}", 0)



                stats[module.id] = mapOf(

                    "score" to score,

                    "percentage" to percentage,

                    "percentile" to percentile

                )

            }



            stats["overall_score"] = prefs.getInt("icfes_overall_score", 0)

            stats["completed_modules"] = prefs.getInt("icfes_completed_modules", 0)



            stats

        } ?: emptyMap()

    }



    fun getStudentAchievements(): List<Achievement> {

        return defaultAchievements.map { achievement ->

            val isUnlocked =

                sharedPrefs?.getBoolean("achievement_${achievement.id}", false) ?: false

            val unlockedAt = if (isUnlocked) {

                sharedPrefs?.getLong("achievement_${achievement.id}_time", 0)

            } else null



            achievement.copy(

                isUnlocked = isUnlocked,

                unlockedAt = unlockedAt

            )

        }

    }



    // ✅ FUNCIONES DE EXPORTACIÓN

    fun exportProgressData(): String {

        val prefs = sharedPrefs ?: return "No hay datos disponibles"



        return buildString {

            append("=== REPORTE DE PROGRESO ICFES ===\n\n")

            append(

                "Fecha: ${

                    java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date())

                }\n"

            )

            append("Estudiante: ${studentProfile?.name ?: "No especificado"}\n\n")



            append("=== PUNTAJES POR MÓDULO ===\n")

            populatedICFESModules.forEach { module ->

                val score = prefs.getInt("icfes_score_${module.id}", 0)

                val percentage = prefs.getFloat("icfes_percentage_${module.id}", 0f)

                val percentile = prefs.getInt("icfes_percentile_${module.id}", 0)



                append("${module.name}\n")

                append("   • Puntaje: $score/500\n")

                append("   • Porcentaje: ${"%.1f".format(percentage)}%\n")

                append("   • Percentil: $percentile\n\n")

            }



            val overallScore = prefs.getInt("icfes_overall_score", 0)

            val completedModules = prefs.getInt("icfes_completed_modules", 0)



            append("=== RESUMEN GENERAL ===\n")

            append("Puntaje promedio: $overallScore/500\n")

            append("Módulos completados: $completedModules/5\n")

            append("Nivel actual: ${studentProfile?.currentLevel ?: 1}\n")

            append("Experiencia: ${studentProfile?.experience ?: 0} puntos\n")



            // ✅ AGREGAR ESTADÍSTICAS DE EVALUACIÓN

            append("\n=== EVALUACIONES COMPLETADAS ===\n")

            val totalEvaluations = prefs.getInt("total_evaluations", 0)

            append("Total de evaluaciones: $totalEvaluations\n")



            populatedICFESModules.forEach { module ->

                val evalScore = prefs.getInt("eval_score_${module.id}", 0)

                val evalPercentage = prefs.getFloat("eval_percentage_${module.id}", 0f)

                val evalLevel = prefs.getString("eval_level_${module.id}", "No evaluado")



                if (evalScore > 0) {

                    append("${module.name} (Evaluación):\n")

                    append("   • Mejor puntaje: $evalScore/500\n")

                    append("   • Porcentaje: ${"%.1f".format(evalPercentage)}%\n")

                    append("   • Nivel: $evalLevel\n\n")

                }

            }

        }

    }



    // ✅ FUNCIONES ESPECÍFICAS PARA EVALUACIÓN

    fun getEvaluationStats(): Map<String, Any> {

        return sharedPrefs?.let { prefs ->

            val stats = mutableMapOf<String, Any>()



            stats["total_evaluations"] = prefs.getInt("total_evaluations", 0)



            populatedICFESModules.forEach { module ->

                val evalScore = prefs.getInt("eval_score_${module.id}", 0)

                val evalPercentage = prefs.getFloat("eval_percentage_${module.id}", 0f)

                val evalLevel = prefs.getString("eval_level_${module.id}", "No evaluado")



                stats["eval_${module.id}"] = mapOf(

                    "score" to evalScore,

                    "percentage" to evalPercentage,

                    "level" to evalLevel

                )

            }



            stats

        } ?: emptyMap()

    }



    // ✅ FUNCIÓN PARA OBTENER COMPARACIÓN PRÁCTICA VS EVALUACIÓN

    fun getPerformanceComparison(): Map<String, Map<String, Any>> {

        return sharedPrefs?.let { prefs ->

            val comparison = mutableMapOf<String, Map<String, Any>>()



            populatedICFESModules.forEach { module ->

                val practiceScore = prefs.getInt("icfes_score_${module.id}", 0)

                val practicePercentage = prefs.getFloat("icfes_percentage_${module.id}", 0f)



                val evalScore = prefs.getInt("eval_score_${module.id}", 0)

                val evalPercentage = prefs.getFloat("eval_percentage_${module.id}", 0f)



                comparison[module.id] = mapOf(

                    "practice" to mapOf(

                        "score" to practiceScore,

                        "percentage" to practicePercentage

                    ),

                    "evaluation" to mapOf(

                        "score" to evalScore,

                        "percentage" to evalPercentage

                    ),

                    "improvement" to (evalScore - practiceScore)

                )

            }



            comparison

        } ?: emptyMap()

    }



    // ✅ FUNCIÓN PARA VERIFICAR SI HAY PROGRESO EN EVALUACIONES

    fun hasEvaluationProgress(): Boolean {

        return sharedPrefs?.getInt("total_evaluations", 0) ?: 0 > 0

    }



    // ✅ FUNCIÓN PARA OBTENER LA MEJOR EVALUACIÓN

    fun getBestEvaluationResult(): Pair<String, Int>? {

        return sharedPrefs?.let { prefs ->

            var bestModule = ""

            var bestScore = 0



            populatedICFESModules.forEach { module ->

                val score = prefs.getInt("eval_score_${module.id}", 0)

                if (score > bestScore) {

                    bestScore = score

                    bestModule = module.name

                }

            }



            if (bestScore > 0) Pair(bestModule, bestScore) else null

        }

    }


    // ✅ FUNCIÓN PARA GENERAR SIMULACRO COMPLETO
    private fun generateFullSimulation(): List<ICFESQuestion> {
        val allQuestions = mutableListOf<ICFESQuestion>()

        // Obtener preguntas de cada módulo (proporcionalmente)
        populatedICFESModules.forEach { module ->
            val moduleQuestions = when (module.id) {
                "lectura_critica" -> getQuestionsFromModule(module, 35)
                "matematicas" -> getQuestionsFromModule(module, 35)
                "ciencias_naturales" -> getQuestionsFromModule(module, 35)
                "sociales_ciudadanas" -> getQuestionsFromModule(module, 35)
                "ingles" -> getQuestionsFromModule(module, 35)
                else -> emptyList()
            }
            allQuestions.addAll(moduleQuestions)
        }

        // Mezclar todas las preguntas aleatoriamente
        return allQuestions.shuffled()
    }

    // ✅ FUNCIÓN AUXILIAR PARA OBTENER PREGUNTAS DE UN MÓDULO
    private fun getQuestionsFromModule(module: ICFESModule, count: Int): List<ICFESQuestion> {
        val allModuleQuestions = module.areas.flatMap { it.questions }

        return if (allModuleQuestions.size >= count) {
            allModuleQuestions.shuffled().take(count)
        } else {
            // Repetir preguntas si es necesario
            val questions = mutableListOf<ICFESQuestion>()
            val cycles = (count / allModuleQuestions.size) + 1

            repeat(cycles) { cycle ->
                allModuleQuestions.shuffled().forEach { question ->
                    if (questions.size < count) {
                        questions.add(
                            question.copy(
                                id = "${question.id}_SIM_${cycle}",
                                tags = question.tags + "simulacro_completo"
                            )
                        )
                    }
                }
            }
            questions.take(count)
        }
    }


    // ✅ FUNCIÓN PARA CONFIGURAR SIMULACRO COMPLETO
    private fun setupFullSimulation() {
        // Tiempo total del simulacro: 4 horas y 30 minutos (como ICFES real)
        timeRemaining = 4 * 60 * 60 * 1000L + 30 * 60 * 1000L // 4.5 horas en ms

        // Generar preguntas mezcladas de todos los módulos
        currentQuestions = generateFullSimulation()

        // Configurar módulo virtual para simulacro
        currentModule = ICFESModule(
            id = "simulacro_completo",
            name = "Simulacro Completo ICFES",
            description = "Simulacro completo con preguntas de todos los módulos",
            icon = R.drawable.ic_timer,
            color = Color(0xFF9C27B0), // Morado para diferenciarlo
            totalQuestions = currentQuestions.size,
            timeLimit = 270, // 4.5 horas = 270 minutos
            areas = emptyList(),
            minimumScore = 250,
            nationalAverage = 250
        )

        // Iniciar temporizador
        startTimer()
    }

}