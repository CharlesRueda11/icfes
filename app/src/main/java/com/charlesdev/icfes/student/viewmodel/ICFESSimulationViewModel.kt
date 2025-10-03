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
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.ai.client.generativeai.GenerativeModel
import org.json.JSONObject
import org.json.JSONArray
import com.charlesdev.icfes.data.Data
import com.charlesdev.icfes.student.data.*
import com.charlesdev.icfes.student.simulation.CompetencyResult
import com.charlesdev.icfes.student.simulation.DifficultyResult
import com.charlesdev.icfes.student.simulation.ICFESNationalComparison
import com.charlesdev.icfes.student.simulation.ICFESSimulation
import com.charlesdev.icfes.student.simulation.ICFESSimulationCompleteResult
import com.charlesdev.icfes.student.simulation.ICFESSimulationQuestionBank
import com.charlesdev.icfes.student.simulation.QuestionAnalysis
import com.charlesdev.icfes.student.simulation.SimulationAnalysis
import com.charlesdev.icfes.student.simulation.SimulationAnswer
import com.charlesdev.icfes.student.simulation.SimulationInstructions
import com.charlesdev.icfes.student.simulation.SimulationPhase
import com.charlesdev.icfes.student.simulation.SimulationSession
import com.charlesdev.icfes.student.simulation.SimulationSessionResult
import com.charlesdev.icfes.student.simulation.SimulationState
import com.charlesdev.icfes.student.simulation.StudyPriority
import com.charlesdev.icfes.student.simulation.StudyRecommendation
import com.charlesdev.icfes.student.simulation.TimeManagementAnalysis
import java.util.*
import kotlin.math.roundToInt

/**
 * ===================================
 * 📁 VIEWMODEL ESPECÍFICO DEL SIMULACRO COMPLETO ICFES
 * ===================================
 * Maneja toda la lógica del simulacro real: 5 sesiones + breaks + análisis final
 */

class ICFESSimulationViewModel : ViewModel() {

    // ✅ BANCO DE PREGUNTAS
    private val questionBank = ICFESSimulationQuestionBank()

    // ✅ MODELO DE IA
    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = Data.apikey
    )

    // ✅ PERSISTENCIA
    private var sharedPrefs: SharedPreferences? = null

    // ✅ ESTADO PRINCIPAL DEL SIMULACRO
    var simulationState by mutableStateOf(SimulationState())
        private set

    var currentSimulation by mutableStateOf<ICFESSimulation?>(null)
        private set

    // ✅ ESTADO DE LA UI
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var showInstructions by mutableStateOf(true)
        private set

    var showBreakScreen by mutableStateOf(false)
        private set

    var showFinalResults by mutableStateOf(false)
        private set

    var finalResults by mutableStateOf<ICFESSimulationCompleteResult?>(null)
        private set

    var isGeneratingResults by mutableStateOf(false)
        private set

    // ✅ ESTADO DE LA SESIÓN ACTUAL
    var currentQuestion by mutableStateOf<ICFESQuestion?>(null)
        private set

    var currentQuestionIndex by mutableStateOf(0)
        private set

    var userAnswer by mutableStateOf("")

    var hasAnsweredCurrentQuestion by mutableStateOf(false)
        private set

    // ✅ STATEFLOWS PARA TIMERS
    private val _sessionTimeRemaining = MutableStateFlow(0L)
    val sessionTimeRemaining: StateFlow<Long> = _sessionTimeRemaining.asStateFlow()

    private val _breakTimeRemaining = MutableStateFlow(0L)
    val breakTimeRemaining: StateFlow<Long> = _breakTimeRemaining.asStateFlow()

    private val _totalTimeSpent = MutableStateFlow(0L)
    val totalTimeSpent: StateFlow<Long> = _totalTimeSpent.asStateFlow()

    // ✅ CONTROL DE TIMERS
    private var isSessionTimerActive = false
    private var isBreakTimerActive = false
    private var simulationStartTime = 0L

    // ✅ INICIALIZACIÓN
    fun initializeSimulation(context: Context) {
        initializePreferences(context)
        loadSavedSimulation()

        if (currentSimulation == null) {
            createNewSimulation()
        }

        updateCurrentQuestion()
    }

    private fun initializePreferences(context: Context) {
        if (sharedPrefs == null) {
            sharedPrefs = context.getSharedPreferences("ICFESSimulationPrefs", Context.MODE_PRIVATE)
        }
    }

    // ✅ CREAR NUEVO SIMULACRO
    private fun createNewSimulation() {
        try {
            isLoading = true
            val sessions = questionBank.generateFullSimulation()
            val totalDuration = sessions.sumOf { it.duration } + (4 * 15 * 60 * 1000L) // 4 breaks de 15 min

            currentSimulation = ICFESSimulation(
                id = "SIM_${System.currentTimeMillis()}",
                sessions = sessions,
                totalDuration = totalDuration,
                instructions = SimulationInstructions.getDefaultInstructions(),
                startTime = 0L
            )

            simulationState = SimulationState(
                currentSession = 0,
                currentQuestion = 0,
                timeRemaining = sessions[0].duration,
                phase = SimulationPhase.INSTRUCTIONS
            )

            // Guardar simulacro creado
            saveSimulationProgress()

        } catch (e: Exception) {
            errorMessage = "Error al crear simulacro: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // ✅ CARGAR SIMULACRO GUARDADO
    private fun loadSavedSimulation() {
        sharedPrefs?.let { prefs ->
            val simulationId = prefs.getString("current_simulation_id", null)
            if (simulationId != null) {
                // Cargar datos del simulacro guardado
                val canResume = prefs.getBoolean("can_resume_simulation", false)
                if (canResume) {
                    // Implementar lógica de carga aquí si es necesario
                    // Por ahora, crear nuevo simulacro
                }
            }
        }
    }

    // ✅ COMENZAR SIMULACRO
    fun startSimulation() {
        currentSimulation?.let { simulation ->
            showInstructions = false
            simulationStartTime = System.currentTimeMillis()

            simulationState = simulationState.copy(
                phase = SimulationPhase.SESSION,
                timeRemaining = simulation.sessions[0].duration
            )

            // Actualizar simulación con tiempo de inicio
            currentSimulation = simulation.copy(
                startTime = simulationStartTime,
                sessions = simulation.sessions.mapIndexed { index, session ->
                    if (index == 0) {
                        session.copy(startTime = simulationStartTime)
                    } else session
                }
            )

            startSessionTimer()
            updateCurrentQuestion()
            saveSimulationProgress()
        }
    }

    // ✅ MANEJO DE RESPUESTAS
    fun selectAnswer(answer: String) {
        if (!hasAnsweredCurrentQuestion) {
            userAnswer = answer
        }
    }

    fun submitAnswer() {
        currentQuestion?.let { question ->
            currentSimulation?.let { simulation ->
                if (hasAnsweredCurrentQuestion) return

                val sessionId = simulation.sessions[simulationState.currentSession].moduleId
                val timeSpent = System.currentTimeMillis() - simulationState.questionStartTime

                // Guardar respuesta
                val answer = SimulationAnswer(
                    questionId = question.id,
                    questionIndex = currentQuestionIndex,
                    sessionId = sessionId,
                    userAnswer = userAnswer,
                    timeSpent = timeSpent,
                    timestamp = System.currentTimeMillis()
                )

                // Actualizar estado
                val sessionAnswers = simulationState.answers.getOrPut(sessionId) { mutableMapOf() }
                sessionAnswers[currentQuestionIndex] = answer

                hasAnsweredCurrentQuestion = true
                saveSimulationProgress()
            }
        }
    }

    // ✅ NAVEGACIÓN ENTRE PREGUNTAS
    fun nextQuestion() {
        currentSimulation?.let { simulation ->
            val currentSession = simulation.sessions[simulationState.currentSession]

            if (currentQuestionIndex < currentSession.questions.size - 1) {
                // Siguiente pregunta en la misma sesión
                currentQuestionIndex++
                userAnswer = ""
                hasAnsweredCurrentQuestion = false
                simulationState = simulationState.copy(
                    currentQuestion = currentQuestionIndex,
                    questionStartTime = System.currentTimeMillis()
                )
                updateCurrentQuestion()
            } else {
                // Fin de sesión
                finishCurrentSession()
            }
            saveSimulationProgress()
        }
    }

    fun previousQuestion() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--
            userAnswer = ""

            // Verificar si ya había respondido esta pregunta
            currentSimulation?.let { simulation ->
                val sessionId = simulation.sessions[simulationState.currentSession].moduleId
                val sessionAnswers = simulationState.answers[sessionId]
                val previousAnswer = sessionAnswers?.get(currentQuestionIndex)

                if (previousAnswer != null) {
                    userAnswer = previousAnswer.userAnswer
                    hasAnsweredCurrentQuestion = true
                } else {
                    hasAnsweredCurrentQuestion = false
                }
            }

            simulationState = simulationState.copy(
                currentQuestion = currentQuestionIndex,
                questionStartTime = System.currentTimeMillis()
            )
            updateCurrentQuestion()
            saveSimulationProgress()
        }
    }

    // ✅ FINALIZAR SESIÓN ACTUAL
    private fun finishCurrentSession() {
        stopSessionTimer()

        currentSimulation?.let { simulation ->
            val updatedSessions = simulation.sessions.mapIndexed { index, session ->
                if (index == simulationState.currentSession) {
                    session.copy(
                        isCompleted = true,
                        endTime = System.currentTimeMillis()
                    )
                } else session
            }

            currentSimulation = simulation.copy(sessions = updatedSessions)

            if (simulationState.currentSession < simulation.sessions.size - 1) {
                // Hay más sesiones - iniciar break
                startBreak()
            } else {
                // Última sesión - finalizar simulacro
                finishSimulation()
            }
        }
    }

    // ✅ INICIAR BREAK
    private fun startBreak() {
        simulationState = simulationState.copy(
            phase = SimulationPhase.BREAK,
            isInBreak = true,
            breakTimeRemaining = 15 * 60 * 1000L // 15 minutos
        )

        showBreakScreen = true
        startBreakTimer()
        saveSimulationProgress()
    }

    // ✅ FINALIZAR BREAK Y COMENZAR SIGUIENTE SESIÓN
    fun finishBreak() {
        stopBreakTimer()
        showBreakScreen = false

        currentSimulation?.let { simulation ->
            val nextSessionIndex = simulationState.currentSession + 1

            if (nextSessionIndex < simulation.sessions.size) {
                // Configurar siguiente sesión
                simulationState = simulationState.copy(
                    currentSession = nextSessionIndex,
                    currentQuestion = 0,
                    phase = SimulationPhase.SESSION,
                    isInBreak = false,
                    timeRemaining = simulation.sessions[nextSessionIndex].duration,
                    questionStartTime = System.currentTimeMillis()
                )

                currentQuestionIndex = 0
                userAnswer = ""
                hasAnsweredCurrentQuestion = false

                // Actualizar sesión con tiempo de inicio
                val updatedSessions = simulation.sessions.mapIndexed { index, session ->
                    if (index == nextSessionIndex) {
                        session.copy(startTime = System.currentTimeMillis())
                    } else session
                }
                currentSimulation = simulation.copy(sessions = updatedSessions)

                startSessionTimer()
                updateCurrentQuestion()
                saveSimulationProgress()
            }
        }
    }

    // ✅ FINALIZAR SIMULACRO COMPLETO
    private fun finishSimulation() {
        stopSessionTimer()

        simulationState = simulationState.copy(
            phase = SimulationPhase.COMPLETED
        )

        currentSimulation?.let { simulation ->
            currentSimulation = simulation.copy(
                isCompleted = true,
                currentSessionIndex = simulation.sessions.size
            )
        }

        // Generar resultados finales
        generateFinalResults()
        saveSimulationProgress()
    }

    // ✅ GENERAR RESULTADOS FINALES CON IA
    private fun generateFinalResults() {
        isGeneratingResults = true

        viewModelScope.launch {
            try {
                val results = withContext(Dispatchers.IO) {
                    calculateSimulationResults()
                }

                finalResults = results
                showFinalResults = true

            } catch (e: Exception) {
                errorMessage = "Error generando resultados: ${e.message}"
                // Generar resultados básicos como fallback
                finalResults = generateBasicResults()
                showFinalResults = true
            } finally {
                isGeneratingResults = false
            }
        }
    }

    // ✅ CALCULAR RESULTADOS CON IA
    private suspend fun calculateSimulationResults(): ICFESSimulationCompleteResult {
        val simulation = currentSimulation ?: throw Exception("No hay simulacro activo")

        // Calcular puntajes por sesión
        val sessionResults = mutableListOf<SimulationSessionResult>()
        var totalCorrect = 0
        var totalQuestions = 0

        simulation.sessions.forEachIndexed { sessionIndex, session ->
            val sessionAnswers = simulationState.answers[session.moduleId] ?: emptyMap()
            val correctCount = session.questions.indices.count { questionIndex ->
                val answer = sessionAnswers[questionIndex]
                answer?.userAnswer?.equals(session.questions[questionIndex].correctAnswer, ignoreCase = true) == true
            }

            totalCorrect += correctCount
            totalQuestions += session.questions.size

            val percentage = if (session.questions.isNotEmpty()) {
                (correctCount.toFloat() / session.questions.size * 100)
            } else 0f

            val icfesScore = (percentage * 5).toInt().coerceIn(0, 500)
            val timeSpent = if (session.endTime > 0) session.endTime - session.startTime else 0L

            sessionResults.add(
                SimulationSessionResult(
                    sessionNumber = sessionIndex + 1,
                    moduleId = session.moduleId,
                    moduleName = session.moduleName,
                    totalQuestions = session.questions.size,
                    correctAnswers = correctCount,
                    percentage = percentage,
                    icfesScore = icfesScore,
                    timeSpent = timeSpent,
                    timeRemaining = maxOf(0L, session.duration - timeSpent),
                    level = when {
                        percentage >= 80 -> "Superior"
                        percentage >= 70 -> "Alto"
                        percentage >= 60 -> "Medio"
                        else -> "Bajo"
                    },
                    competencyBreakdown = calculateCompetencyBreakdown(session, sessionAnswers),
                    difficultyBreakdown = calculateDifficultyBreakdown(session, sessionAnswers),
                    efficiency = if (timeSpent > 0) correctCount.toFloat() / (timeSpent / 60000f) else 0f,
                    questionAnalysis = generateQuestionAnalysis(session, sessionAnswers)
                )
            )
        }

        // Calcular puntaje global
        val globalPercentage = if (totalQuestions > 0) (totalCorrect.toFloat() / totalQuestions * 100) else 0f
        val globalScore = (globalPercentage * 5).toInt().coerceIn(0, 500)

        // Generar análisis con IA
        val detailedAnalysis = generateAIAnalysis(sessionResults, globalScore, globalPercentage)

        return ICFESSimulationCompleteResult(
            simulationId = simulation.id,
            completedAt = System.currentTimeMillis(),
            totalTimeSpent = System.currentTimeMillis() - simulationStartTime,
            sessionResults = sessionResults,
            globalScore = globalScore,
            globalPercentage = globalPercentage,
            globalLevel = when {
                globalPercentage >= 80 -> "Superior"
                globalPercentage >= 70 -> "Alto"
                globalPercentage >= 60 -> "Medio"
                else -> "Bajo"
            },
            nationalPercentile = calculatePercentile(globalScore),
            comparison = generateNationalComparison(globalScore),
            detailedAnalysis = detailedAnalysis,
            recommendations = detailedAnalysis.studyPlan.map { it.toString() },
            certificateEligible = globalScore >= 300
        )
    }

    // ✅ ANÁLISIS CON IA
    private suspend fun generateAIAnalysis(
        sessionResults: List<SimulationSessionResult>,
        globalScore: Int,
        globalPercentage: Float
    ): SimulationAnalysis {
        return try {
            val prompt = createAnalysisPrompt(sessionResults, globalScore, globalPercentage)
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text ?: throw Exception("Respuesta vacía")

            parseAIAnalysisResponse(responseText)
        } catch (e: Exception) {
            generateBasicAnalysis(sessionResults, globalScore, globalPercentage)
        }
    }

    // ✅ PROMPT PARA ANÁLISIS CON IA
    private fun createAnalysisPrompt(
        sessionResults: List<SimulationSessionResult>,
        globalScore: Int,
        globalPercentage: Float
    ): String {
        return """
        Eres un experto analista del examen ICFES Saber 11 de Colombia. Analiza este simulacro completo y genera un reporte detallado.

        PUNTAJE GLOBAL: $globalScore/500 (${"%.1f".format(globalPercentage)}%)

        RESULTADOS POR MÓDULO:
        ${sessionResults.joinToString("\n") {
            "${it.moduleName}: ${it.correctAnswers}/${it.totalQuestions} (${it.icfesScore}/500 - ${"%.1f".format(it.percentage)}%)"
        }}

        ANÁLISIS REQUERIDO:
        1. Fortalezas principales (máximo 4)
        2. Debilidades críticas (máximo 4)
        3. Áreas de mejora prioritarias (máximo 5)
        4. Recomendaciones estratégicas (máximo 5)
        5. Plan de estudio personalizado (máximo 5 elementos)
        6. Análisis de manejo del tiempo
        7. Mensaje motivacional personalizado

        Responde SOLO en JSON sin texto adicional:
        {
            "strengths": ["fortaleza1", "fortaleza2", "fortaleza3", "fortaleza4"],
            "weaknesses": ["debilidad1", "debilidad2", "debilidad3", "debilidad4"],
            "improvementAreas": ["area1", "area2", "area3", "area4", "area5"],
            "strategicRecommendations": ["rec1", "rec2", "rec3", "rec4", "rec5"],
            "studyPlan": [
                {
                    "moduleId": "lectura_critica",
                    "priority": "HIGH",
                    "suggestedHours": 10,
                    "specificTopics": ["topic1", "topic2"],
                    "practiceType": "Evaluación cronometrada"
                }
            ],
            "timeManagement": {
                "efficiency": 0.8,
                "level": "Bueno",
                "recommendations": ["rec1", "rec2", "rec3"]
            },
            "motivationalMessage": "Mensaje personalizado motivador (máximo 150 palabras)"
        }
        """.trimIndent()
    }

    // ✅ PARSEAR RESPUESTA DE IA
    private fun parseAIAnalysisResponse(responseText: String): SimulationAnalysis {
        val jsonText = responseText.trim()
            .removePrefix("```json")
            .removeSuffix("```")
            .trim()

        val jsonResponse = JSONObject(jsonText)

        return SimulationAnalysis(
            strengths = parseJsonArray(jsonResponse.getJSONArray("strengths")),
            weaknesses = parseJsonArray(jsonResponse.getJSONArray("weaknesses")),
            improvementAreas = parseJsonArray(jsonResponse.getJSONArray("improvementAreas")),
            strategicRecommendations = parseJsonArray(jsonResponse.getJSONArray("strategicRecommendations")),
            studyPlan = parseStudyPlan(jsonResponse.getJSONArray("studyPlan")),
            timeManagement = parseTimeManagement(jsonResponse.getJSONObject("timeManagement")),
            motivationalMessage = jsonResponse.getString("motivationalMessage")
        )
    }

    private fun parseJsonArray(jsonArray: JSONArray): List<String> {
        return (0 until jsonArray.length()).map { jsonArray.getString(it) }
    }

    private fun parseStudyPlan(jsonArray: JSONArray): List<StudyRecommendation> {
        return (0 until jsonArray.length()).map { index ->
            val item = jsonArray.getJSONObject(index)
            StudyRecommendation(
                moduleId = item.getString("moduleId"),
                moduleName = getModuleName(item.getString("moduleId")),
                priority = StudyPriority.valueOf(item.getString("priority")),
                suggestedHours = item.getInt("suggestedHours"),
                specificTopics = parseJsonArray(item.getJSONArray("specificTopics")),
                resources = emptyList(), // Se puede expandir
                practiceType = item.getString("practiceType")
            )
        }
    }

    private fun parseTimeManagement(jsonObject: JSONObject): TimeManagementAnalysis {
        return TimeManagementAnalysis(
            totalTimeUsed = System.currentTimeMillis() - simulationStartTime,
            totalTimeAvailable = 4 * 60 * 60 * 1000L + 30 * 60 * 1000L, // 4.5 horas
            efficiency = jsonObject.getDouble("efficiency").toFloat(),
            sessionTimeBreakdown = emptyMap(), // Se puede expandir
            questionsPetMinute = 0f, // Se puede calcular
            timeManagementLevel = jsonObject.getString("level"),
            recommendations = parseJsonArray(jsonObject.getJSONArray("recommendations"))
        )
    }

    // ✅ FUNCIONES AUXILIARES DE CÁLCULO
    private fun calculateCompetencyBreakdown(
        session: SimulationSession,
        answers: Map<Int, SimulationAnswer>
    ): Map<String, CompetencyResult> {
        return session.questions.groupBy { it.competency }.mapValues { (competency, questions) ->
            val correctCount = questions.indices.count { index ->
                val answer = answers[session.questions.indexOf(questions[index])]
                answer?.userAnswer?.equals(questions[index].correctAnswer, ignoreCase = true) == true
            }

            CompetencyResult(
                competencyName = competency,
                totalQuestions = questions.size,
                correctAnswers = correctCount,
                percentage = if (questions.isNotEmpty()) (correctCount.toFloat() / questions.size * 100) else 0f,
                averageTime = 0L, // Se puede calcular si es necesario
                performance = when {
                    correctCount.toFloat() / questions.size >= 0.8 -> "Excelente"
                    correctCount.toFloat() / questions.size >= 0.6 -> "Bueno"
                    correctCount.toFloat() / questions.size >= 0.4 -> "Regular"
                    else -> "Necesita mejora"
                }
            )
        }
    }

    private fun calculateDifficultyBreakdown(
        session: SimulationSession,
        answers: Map<Int, SimulationAnswer>
    ): Map<Difficulty, DifficultyResult> {
        return session.questions.groupBy { it.difficulty }.mapValues { (difficulty, questions) ->
            val correctCount = questions.indices.count { index ->
                val answer = answers[session.questions.indexOf(questions[index])]
                answer?.userAnswer?.equals(questions[index].correctAnswer, ignoreCase = true) == true
            }

            DifficultyResult(
                difficulty = difficulty,
                totalQuestions = questions.size,
                correctAnswers = correctCount,
                percentage = if (questions.isNotEmpty()) (correctCount.toFloat() / questions.size * 100) else 0f,
                averageTime = 0L
            )
        }
    }

    private fun generateQuestionAnalysis(
        session: SimulationSession,
        answers: Map<Int, SimulationAnswer>
    ): List<QuestionAnalysis> {
        return session.questions.mapIndexed { index, question ->
            val answer = answers[index]
            val isCorrect = answer?.userAnswer?.equals(question.correctAnswer, ignoreCase = true) == true

            QuestionAnalysis(
                questionId = question.id,
                questionNumber = index + 1,
                isCorrect = isCorrect,
                userAnswer = answer?.userAnswer ?: "",
                correctAnswer = question.correctAnswer,
                timeSpent = answer?.timeSpent ?: 0L,
                difficulty = question.difficulty,
                competency = question.competency,
                topic = question.tags.firstOrNull() ?: "",
                wasChanged = answer?.wasChanged ?: false,
                nationalCorrectRate = generateNationalCorrectRate(question.difficulty)
            )
        }
    }

    // ✅ FUNCIONES DE UTILIDAD
    private fun calculatePercentile(score: Int): Int {
        return when {
            score >= 450 -> 95
            score >= 400 -> 90
            score >= 350 -> 80
            score >= 300 -> 60
            score >= 250 -> 40
            score >= 200 -> 20
            else -> 10
        }
    }

    private fun generateNationalComparison(score: Int): ICFESNationalComparison {
        val nationalAverage = 250
        val percentile = calculatePercentile(score)

        return ICFESNationalComparison(
            nationalAverage = nationalAverage,
            percentilePosition = percentile,
            studentsAbove = 100 - percentile,
            studentsBelow = percentile,
            regionAverage = 255, // Santander suele estar ligeramente arriba
            institutionTypeAverage = 260, // Promedio por tipo de institución
            comparison = when {
                score > nationalAverage + 50 -> "Muy superior al promedio nacional"
                score > nationalAverage -> "Superior al promedio nacional"
                score > nationalAverage - 30 -> "Cerca del promedio nacional"
                else -> "Por debajo del promedio nacional"
            }
        )
    }

    private fun generateNationalCorrectRate(difficulty: Difficulty): Float {
        return when (difficulty) {
            Difficulty.FACIL -> 0.75f
            Difficulty.MEDIO -> 0.55f
            Difficulty.DIFICIL -> 0.35f
        }
    }

    private fun getModuleName(moduleId: String): String {
        return when (moduleId) {
            "lectura_critica" -> "Lectura Crítica"
            "matematicas" -> "Matemáticas"
            "ciencias_naturales" -> "Ciencias Naturales"
            "sociales_ciudadanas" -> "Sociales y Ciudadanas"
            "ingles" -> "Inglés"
            else -> moduleId
        }
    }

    // ✅ ANÁLISIS BÁSICO (FALLBACK)
    private fun generateBasicAnalysis(
        sessionResults: List<SimulationSessionResult>,
        globalScore: Int,
        globalPercentage: Float
    ): SimulationAnalysis {
        val strengths = mutableListOf<String>()
        val weaknesses = mutableListOf<String>()

        sessionResults.forEach { result ->
            if (result.percentage >= 70) {
                strengths.add("Buen desempeño en ${result.moduleName}")
            } else if (result.percentage < 50) {
                weaknesses.add("Necesitas reforzar ${result.moduleName}")
            }
        }

        return SimulationAnalysis(
            strengths = strengths.ifEmpty { listOf("Completaste el simulacro completo") },
            weaknesses = weaknesses.ifEmpty { listOf("Continúa practicando para mejorar") },
            improvementAreas = listOf("Práctica constante", "Manejo del tiempo", "Técnicas de examen"),
            strategicRecommendations = listOf("Estudia los temas más débiles", "Practica con cronómetro"),
            studyPlan = generateBasicStudyPlan(),
            timeManagement = TimeManagementAnalysis(
                totalTimeUsed = System.currentTimeMillis() - simulationStartTime,
                totalTimeAvailable = 4 * 60 * 60 * 1000L + 30 * 60 * 1000L,
                efficiency = 0.7f,
                sessionTimeBreakdown = emptyMap(),
                questionsPetMinute = 1.2f,
                timeManagementLevel = "Regular",
                recommendations = listOf("Administra mejor el tiempo", "Practica velocidad de respuesta")
            ),
            motivationalMessage = "¡Felicidades por completar tu simulacro! Cada práctica te acerca más a tu meta."
        )
    }

    private fun generateBasicStudyPlan(): List<StudyRecommendation> {
        return listOf(
            StudyRecommendation(
                moduleId = "matematicas",
                moduleName = "Matemáticas",
                priority = StudyPriority.HIGH,
                suggestedHours = 8,
                specificTopics = listOf("Álgebra", "Geometría"),
                resources = listOf("Guías ICFES", "Videos educativos"),
                practiceType = "Práctica cronometrada"
            )
        )
    }

    private fun generateBasicResults(): ICFESSimulationCompleteResult {
        val simulation = currentSimulation ?: return createEmptyResult()

        return ICFESSimulationCompleteResult(
            simulationId = simulation.id,
            completedAt = System.currentTimeMillis(),
            totalTimeSpent = System.currentTimeMillis() - simulationStartTime,
            sessionResults = emptyList(),
            globalScore = 250,
            globalPercentage = 50f,
            globalLevel = "Medio",
            nationalPercentile = 50,
            comparison = generateNationalComparison(250),
            detailedAnalysis = generateBasicAnalysis(emptyList(), 250, 50f),
            recommendations = listOf("Continúa practicando"),
            certificateEligible = false
        )
    }

    private fun createEmptyResult(): ICFESSimulationCompleteResult {
        return ICFESSimulationCompleteResult(
            simulationId = "ERROR",
            completedAt = System.currentTimeMillis(),
            totalTimeSpent = 0L,
            sessionResults = emptyList(),
            globalScore = 0,
            globalPercentage = 0f,
            globalLevel = "Sin evaluar",
            nationalPercentile = 0,
            comparison = ICFESNationalComparison(0, 0, 0, 0, 0, 0, ""),
            detailedAnalysis = SimulationAnalysis(
                emptyList(), emptyList(), emptyList(), emptyList(),
                emptyList(), TimeManagementAnalysis(0, 0, 0f, emptyMap(), 0f, "", emptyList()),
                "Error en el análisis"
            ),
            recommendations = emptyList(),
            certificateEligible = false
        )
    }

    // ✅ MANEJO DE TIMERS
    private fun startSessionTimer() {
        isSessionTimerActive = true
        viewModelScope.launch {
            while (isSessionTimerActive && simulationState.timeRemaining > 0) {
                delay(1000)
                simulationState = simulationState.copy(
                    timeRemaining = maxOf(0L, simulationState.timeRemaining - 1000)
                )
                _sessionTimeRemaining.value = simulationState.timeRemaining
                _totalTimeSpent.value = System.currentTimeMillis() - simulationStartTime
            }

            if (simulationState.timeRemaining <= 0) {
                // Tiempo agotado - auto enviar
                finishCurrentSession()
            }
        }
    }

    private fun stopSessionTimer() {
        isSessionTimerActive = false
    }

    private fun startBreakTimer() {
        isBreakTimerActive = true
        viewModelScope.launch {
            while (isBreakTimerActive && simulationState.breakTimeRemaining > 0) {
                delay(1000)
                simulationState = simulationState.copy(
                    breakTimeRemaining = maxOf(0L, simulationState.breakTimeRemaining - 1000)
                )
                _breakTimeRemaining.value = simulationState.breakTimeRemaining
            }

            if (simulationState.breakTimeRemaining <= 0) {
                // Break terminado automáticamente
                finishBreak()
            }
        }
    }

    private fun stopBreakTimer() {
        isBreakTimerActive = false
    }

    // ✅ ACTUALIZACIÓN DE PREGUNTA ACTUAL
    private fun updateCurrentQuestion() {
        currentSimulation?.let { simulation ->
            val currentSession = simulation.sessions[simulationState.currentSession]
            if (currentQuestionIndex < currentSession.questions.size) {
                currentQuestion = currentSession.questions[currentQuestionIndex]

                // Verificar si ya respondió esta pregunta
                val sessionAnswers = simulationState.answers[currentSession.moduleId]
                val existingAnswer = sessionAnswers?.get(currentQuestionIndex)

                if (existingAnswer != null) {
                    userAnswer = existingAnswer.userAnswer
                    hasAnsweredCurrentQuestion = true
                } else {
                    userAnswer = ""
                    hasAnsweredCurrentQuestion = false
                }
            }
        }
    }

    // ✅ PERSISTENCIA
    private fun saveSimulationProgress() {
        sharedPrefs?.edit()?.apply {
            currentSimulation?.let { simulation ->
                putString("current_simulation_id", simulation.id)
                putBoolean("can_resume_simulation", !simulation.isCompleted)
                putLong("simulation_start_time", simulationStartTime)
                putInt("current_session", simulationState.currentSession)
                putInt("current_question", simulationState.currentQuestion)
                // Guardar más detalles según sea necesario
            }
            apply()
        }
    }

    // ✅ FUNCIONES DE CONTROL
    fun pauseSimulation() {
        if (simulationState.isInBreak) {
            stopBreakTimer()
            simulationState = simulationState.copy(phase = SimulationPhase.PAUSED)
        }
    }

    fun resumeSimulation() {
        if (simulationState.phase == SimulationPhase.PAUSED) {
            if (simulationState.isInBreak) {
                simulationState = simulationState.copy(phase = SimulationPhase.BREAK)
                startBreakTimer()
            } else {
                simulationState = simulationState.copy(phase = SimulationPhase.SESSION)
                startSessionTimer()
            }
        }
    }

    fun exitSimulation() {
        stopSessionTimer()
        stopBreakTimer()
        // No borrar progreso - permitir reanudar después
    }

    fun resetSimulation() {
        stopSessionTimer()
        stopBreakTimer()

        // Limpiar preferencias
        sharedPrefs?.edit()?.clear()?.apply()

        // Resetear estado
        currentSimulation = null
        simulationState = SimulationState()
        showInstructions = true
        showBreakScreen = false
        showFinalResults = false
        finalResults = null
        isGeneratingResults = false
        currentQuestion = null
        currentQuestionIndex = 0
        userAnswer = ""
        hasAnsweredCurrentQuestion = false
        errorMessage = null
    }

    // ✅ FUNCIONES DE CONSULTA
    fun getCurrentSession(): SimulationSession? {
        return currentSimulation?.sessions?.getOrNull(simulationState.currentSession)
    }

    fun getProgress(): Float {
        currentSimulation?.let { simulation ->
            val totalQuestions = simulation.sessions.sumOf { it.questions.size }
            val answeredQuestions = simulationState.answers.values.sumOf { it.size }
            return if (totalQuestions > 0) answeredQuestions.toFloat() / totalQuestions else 0f
        }
        return 0f
    }

    fun getSessionProgress(): Float {
        getCurrentSession()?.let { session ->
            val sessionAnswers = simulationState.answers[session.moduleId]?.size ?: 0
            return if (session.questions.isNotEmpty()) sessionAnswers.toFloat() / session.questions.size else 0f
        }
        return 0f
    }

    fun canGoToNextQuestion(): Boolean {
        return hasAnsweredCurrentQuestion
    }

    fun canGoToPreviousQuestion(): Boolean {
        return currentQuestionIndex > 0
    }

    fun getTimeDisplay(timeMs: Long): String {
        val hours = timeMs / (1000 * 60 * 60)
        val minutes = (timeMs % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (timeMs % (1000 * 60)) / 1000

        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopSessionTimer()
        stopBreakTimer()
    }


}