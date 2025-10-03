package com.charlesdev.icfes.student.evaluation
// ===================================
// 📁 ICFESEvaluationManager.kt
// ===================================


import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import com.charlesdev.icfes.data.Data
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.json.JSONArray
import com.charlesdev.icfes.student.data.*

/**
 * Maneja específicamente las evaluaciones (simulacros, exámenes con tiempo)
 * Optimizado para reducir costos de IA
 */
class ICFESEvaluationManager {

    // ✅ BANCO DE PREGUNTAS DIFERENCIADO
    private val evaluationQuestionBank = ICFESEvaluationQuestionBank()

    // ✅ ALMACENAMIENTO DE RESPUESTAS SIN IA INMEDIATA
    val evaluationAnswers = mutableMapOf<Int, EvaluationAnswer>()
    private var evaluationStartTime = 0L
    private var evaluationModule: ICFESModule? = null

    // ✅ ESTADO DE EVALUACIÓN
    var isEvaluationMode by mutableStateOf(false)
        private set
    var evaluationCompleted by mutableStateOf(false)
        private set
    var evaluationFeedback by mutableStateOf<ICFESEvaluationResult?>(null)
        private set
    var isGeneratingFeedback by mutableStateOf(false)
        private set

    /**
     * Inicializar evaluación con preguntas específicas
     */
    fun startEvaluation(
        moduleId: String,
        sessionType: SessionType,
        totalQuestions: Int = 20
    ): List<ICFESQuestion> {
        isEvaluationMode = true
        evaluationStartTime = System.currentTimeMillis()
        evaluationModule = populatedICFESModules.find { it.id == moduleId }
        evaluationAnswers.clear()
        evaluationCompleted = false
        evaluationFeedback = null

        // ✅ OBTENER PREGUNTAS ESPECÍFICAS PARA EVALUACIÓN
        return evaluationQuestionBank.getEvaluationQuestions(moduleId, totalQuestions)
    }

    /**
     * Guardar respuesta sin feedback inmediato (CLAVE para reducir costos)
     */
    fun submitEvaluationAnswer(
        questionIndex: Int,
        question: ICFESQuestion,
        userAnswer: String
    ) {
        val responseTime = System.currentTimeMillis() - evaluationStartTime

        evaluationAnswers[questionIndex] = EvaluationAnswer(
            questionId = question.id,
            question = question,
            userAnswer = userAnswer,
            timestamp = System.currentTimeMillis(),
            timeSpent = responseTime
        )
    }

    /**
     * ✅ UNA SOLA LLAMADA DE IA AL FINAL - OPTIMIZACIÓN DE COSTOS
     */
    suspend fun generateConsolidatedFeedback(): ICFESEvaluationResult = withContext(Dispatchers.IO) {
        isGeneratingFeedback = true

        try {
            // ✅ PREPARAR DATOS PARA UNA SOLA LLAMADA MASIVA
            val evaluationData = prepareEvaluationData()

            // ✅ PROMPT OPTIMIZADO PARA ANÁLISIS MASIVO
            val consolidatedPrompt = createConsolidatedPrompt(evaluationData)

            // ✅ UNA SOLA LLAMADA DE IA
            val generativeModel = GenerativeModel(
                modelName = "gemini-2.0-flash",
                apiKey = Data.apikey
            )

            val response = generativeModel.generateContent(consolidatedPrompt)
            val responseText = response.text ?: throw Exception("Respuesta vacía")

            // ✅ PARSEAR RESPUESTA CONSOLIDADA
            val result = parseConsolidatedResponse(responseText, evaluationData)

            evaluationFeedback = result
            evaluationCompleted = true

            result

        } catch (e: Exception) {
            // ✅ FALLBACK SIN IA (NO CUESTA NADA)
            generateFallbackEvaluation()
        } finally {
            isGeneratingFeedback = false
        }
    }

    /**
     * ✅ PREPARAR DATOS PARA ANÁLISIS MASIVO
     */
    private fun prepareEvaluationData(): EvaluationData {
        val totalQuestions = evaluationAnswers.size
        val correctAnswers = evaluationAnswers.values.count { answer ->
            answer.userAnswer.equals(answer.question.correctAnswer, ignoreCase = true)
        }

        // Agrupar por competencias
        val competencyPerformance = evaluationAnswers.values.groupBy { it.question.competency }
            .mapValues { (_, answers) ->
                val correct = answers.count { it.userAnswer.equals(it.question.correctAnswer, ignoreCase = true) }
                CompetencyScore(correct, answers.size, (correct.toFloat() / answers.size * 100))
            }

        // Agrupar por dificultad
        val difficultyPerformance = evaluationAnswers.values.groupBy { it.question.difficulty }
            .mapValues { (_, answers) ->
                val correct = answers.count { it.userAnswer.equals(it.question.correctAnswer, ignoreCase = true) }
                DifficultyScore(correct, answers.size, (correct.toFloat() / answers.size * 100))
            }

        return EvaluationData(
            moduleId = evaluationModule?.id ?: "",
            moduleName = evaluationModule?.name ?: "",
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            totalTime = System.currentTimeMillis() - evaluationStartTime,
            competencyPerformance = competencyPerformance,
            difficultyPerformance = difficultyPerformance,
            answers = evaluationAnswers.values.toList(),
            percentage = if (totalQuestions > 0) (correctAnswers.toFloat() / totalQuestions * 100) else 0f
        )
    }

    /**
     * ✅ PROMPT OPTIMIZADO PARA ANÁLISIS MASIVO (MENOS TOKENS)
     */
    private fun createConsolidatedPrompt(data: EvaluationData): String {
        return """
        Eres un experto evaluador del examen ICFES. Analiza esta evaluación completa y genera feedback educativo consolidado.

        MÓDULO: ${data.moduleName}
        PREGUNTAS TOTALES: ${data.totalQuestions}
        RESPUESTAS CORRECTAS: ${data.correctAnswers}
        PORCENTAJE: ${"%.1f".format(data.percentage)}%
        TIEMPO TOTAL: ${data.totalTime / 60000} minutos

        RENDIMIENTO POR COMPETENCIA:
        ${data.competencyPerformance.map { "${it.key}: ${it.value.correct}/${it.value.total} (${"%.1f".format(it.value.percentage)}%)" }.joinToString("\n")}

        RENDIMIENTO POR DIFICULTAD:
        ${data.difficultyPerformance.map { "${it.key.displayName}: ${it.value.correct}/${it.value.total} (${"%.1f".format(it.value.percentage)}%)" }.joinToString("\n")}

        ERRORES PRINCIPALES (solo preguntas incorrectas):
        ${data.answers.filter { !it.userAnswer.equals(it.question.correctAnswer, ignoreCase = true) }
            .take(5) // Solo los primeros 5 errores para ahorrar tokens
            .mapIndexed { index, answer ->
                "${index + 1}. ${answer.question.competency} - Respondió: ${answer.userAnswer}, Correcto: ${answer.question.correctAnswer}"
            }.joinToString("\n")}

        GENERA UNA RESPUESTA EN JSON CON:
        1. Fortalezas identificadas (máximo 3)
        2. Debilidades principales (máximo 3)  
        3. Recomendaciones específicas (máximo 5)
        4. Nivel alcanzado (Bajo/Medio/Alto)
        5. Puntaje ICFES estimado (0-500)
        6. Estrategias de mejora (máximo 3)

        Responde SOLO en JSON sin texto adicional:
        {
            "fortalezas": ["fortaleza1", "fortaleza2", "fortaleza3"],
            "debilidades": ["debilidad1", "debilidad2", "debilidad3"],
            "recomendaciones": ["rec1", "rec2", "rec3", "rec4", "rec5"],
            "nivel": "Medio",
            "puntajeICFES": 280,
            "estrategias": ["estrategia1", "estrategia2", "estrategia3"],
            "analisisGeneral": "Análisis breve del rendimiento general (máximo 100 palabras)"
        }
        """.trimIndent()
    }

    /**
     * ✅ PARSEAR RESPUESTA CONSOLIDADA
     */
    private fun parseConsolidatedResponse(responseText: String, data: EvaluationData): ICFESEvaluationResult {
        try {
            val jsonText = responseText.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()

            val jsonResponse = JSONObject(jsonText)

            return ICFESEvaluationResult(
                moduleId = data.moduleId,
                moduleName = data.moduleName,
                totalQuestions = data.totalQuestions,
                correctAnswers = data.correctAnswers,
                percentage = data.percentage,
                timeSpent = data.totalTime,
                puntajeICFES = jsonResponse.getInt("puntajeICFES"),
                nivel = jsonResponse.getString("nivel"),
                fortalezas = parseJsonArray(jsonResponse.getJSONArray("fortalezas")),
                debilidades = parseJsonArray(jsonResponse.getJSONArray("debilidades")),
                recomendaciones = parseJsonArray(jsonResponse.getJSONArray("recomendaciones")),
                estrategias = parseJsonArray(jsonResponse.getJSONArray("estrategias")),
                analisisGeneral = jsonResponse.getString("analisisGeneral"),
                competencyScores = data.competencyPerformance,
                difficultyScores = data.difficultyPerformance,
                evaluationDate = System.currentTimeMillis()
            )

        } catch (e: Exception) {
            return generateFallbackEvaluation()
        }
    }

    private fun parseJsonArray(jsonArray: JSONArray): List<String> {
        return (0 until jsonArray.length()).map { jsonArray.getString(it) }
    }

    /**
     * ✅ FALLBACK SIN IA (GRATIS)
     */
    private fun generateFallbackEvaluation(): ICFESEvaluationResult {
        val data = prepareEvaluationData()

        val nivel = when {
            data.percentage >= 80 -> "Alto"
            data.percentage >= 60 -> "Medio"
            else -> "Bajo"
        }

        val puntajeICFES = (data.percentage * 5).toInt().coerceIn(0, 500)

        return ICFESEvaluationResult(
            moduleId = data.moduleId,
            moduleName = data.moduleName,
            totalQuestions = data.totalQuestions,
            correctAnswers = data.correctAnswers,
            percentage = data.percentage,
            timeSpent = data.totalTime,
            puntajeICFES = puntajeICFES,
            nivel = nivel,
            fortalezas = generateBasicStrengths(data),
            debilidades = generateBasicWeaknesses(data),
            recomendaciones = generateBasicRecommendations(data),
            estrategias = generateBasicStrategies(data),
            analisisGeneral = "Evaluación completada. Revisa las recomendaciones para mejorar tu rendimiento.",
            competencyScores = data.competencyPerformance,
            difficultyScores = data.difficultyPerformance,
            evaluationDate = System.currentTimeMillis()
        )
    }

    private fun generateBasicStrengths(data: EvaluationData): List<String> {
        return data.competencyPerformance.filter { it.value.percentage >= 70 }
            .map { "Buen desempeño en ${it.key}" }
            .take(3)
            .ifEmpty { listOf("Completaste la evaluación con dedicación") }
    }

    private fun generateBasicWeaknesses(data: EvaluationData): List<String> {
        return data.competencyPerformance.filter { it.value.percentage < 60 }
            .map { "Necesitas reforzar ${it.key}" }
            .take(3)
    }

    private fun generateBasicRecommendations(data: EvaluationData): List<String> {
        val recs = mutableListOf<String>()

        if (data.percentage < 70) {
            recs.add("Practica más preguntas de este módulo")
            recs.add("Revisa los conceptos fundamentales")
        }

        data.difficultyPerformance[Difficulty.FACIL]?.let { easy ->
            if (easy.percentage < 80) {
                recs.add("Fortalece los conceptos básicos")
            }
        }

        if (recs.isEmpty()) {
            recs.add("Continúa practicando para mantener tu nivel")
        }

        return recs.take(5)
    }

    private fun generateBasicStrategies(data: EvaluationData): List<String> {
        return listOf(
            "Administra mejor el tiempo durante la evaluación",
            "Lee cuidadosamente cada pregunta antes de responder",
            "Practica con simulacros cronometrados"
        )
    }

    fun resetEvaluation() {
        isEvaluationMode = false
        evaluationCompleted = false
        evaluationFeedback = null
        evaluationAnswers.clear()
        isGeneratingFeedback = false
    }
}

// ===================================
// 📁 ICFESEvaluationModels.kt
// ===================================

data class EvaluationAnswer(
    val questionId: String,
    val question: ICFESQuestion,
    val userAnswer: String,
    val timestamp: Long,
    val timeSpent: Long
)

data class EvaluationData(
    val moduleId: String,
    val moduleName: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val totalTime: Long,
    val competencyPerformance: Map<String, CompetencyScore>,
    val difficultyPerformance: Map<Difficulty, DifficultyScore>,
    val answers: List<EvaluationAnswer>,
    val percentage: Float
)

data class CompetencyScore(
    val correct: Int,
    val total: Int,
    val percentage: Float
)

data class DifficultyScore(
    val correct: Int,
    val total: Int,
    val percentage: Float
)

data class ICFESEvaluationResult(
    val moduleId: String,
    val moduleName: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val percentage: Float,
    val timeSpent: Long,
    val puntajeICFES: Int,
    val nivel: String,
    val fortalezas: List<String>,
    val debilidades: List<String>,
    val recomendaciones: List<String>,
    val estrategias: List<String>,
    val analisisGeneral: String,
    val competencyScores: Map<String, CompetencyScore>,
    val difficultyScores: Map<Difficulty, DifficultyScore>,
    val evaluationDate: Long
)
