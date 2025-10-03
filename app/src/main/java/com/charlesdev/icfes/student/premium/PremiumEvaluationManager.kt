package com.charlesdev.icfes.student.premium

import androidx.compose.runtime.*
import com.charlesdev.icfes.data.Data
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.json.JSONArray

/**
 * 🎯 MANAGER ESPECÍFICO PARA EVALUACIONES PREMIUM
 * Adaptado de ICFESEvaluationManager pero con contexto del profesor
 * Optimizado para reducir costos de IA
 */
class PremiumEvaluationManager {

    // ✅ MISMO MODELO QUE ICFES
    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = Data.apikey
    )

    // ✅ ALMACENAMIENTO DE RESPUESTAS SIN IA INMEDIATA (como ICFESEvaluationManager)
    val premiumEvaluationAnswers = mutableMapOf<Int, PremiumEvaluationAnswer>()
    private var evaluationStartTime = 0L
    private var currentTeacherContext: TeacherContext? = null
    private var currentModuleInfo: PremiumModuleInfo? = null

    // ✅ ESTADO DE EVALUACIÓN PREMIUM
    var isEvaluationMode by mutableStateOf(false)
        private set
    var evaluationCompleted by mutableStateOf(false)
        private set
    var premiumEvaluationResult by mutableStateOf<PremiumEvaluationResult?>(null)
        private set
    var isGeneratingFeedback by mutableStateOf(false)
        private set

    /**
     * ✅ INICIALIZAR EVALUACIÓN PREMIUM
     */
    fun startPremiumEvaluation(
        moduleId: String,
        moduleName: String,
        teacherId: String,
        teacherName: String,
        institution: String,
        sessionType: PremiumSessionType,
        totalQuestions: Int = 20
    ) {
        isEvaluationMode = sessionType == PremiumSessionType.EVALUATION
        evaluationStartTime = System.currentTimeMillis()
        premiumEvaluationAnswers.clear()
        evaluationCompleted = false
        premiumEvaluationResult = null

        // ✅ CONFIGURAR CONTEXTO DEL PROFESOR
        currentTeacherContext = TeacherContext(
            teacherId = teacherId,
            teacherName = teacherName,
            institution = institution,
            specialization = "", // Para futuro
            teachingStyle = "personalizado",
            customInstructions = ""
        )

        // ✅ CONFIGURAR INFO DEL MÓDULO
        currentModuleInfo = PremiumModuleInfo(
            moduleId = moduleId,
            moduleName = moduleName,
            totalQuestions = totalQuestions
        )
    }

    /**
     * ✅ GUARDAR RESPUESTA SIN FEEDBACK INMEDIATO (COMO ICFESEvaluationManager)
     */
    fun submitPremiumEvaluationAnswer(
        questionIndex: Int,
        question: PremiumQuestion,
        userAnswer: String
    ) {
        val responseTime = System.currentTimeMillis() - evaluationStartTime

        premiumEvaluationAnswers[questionIndex] = PremiumEvaluationAnswer(
            questionId = question.id,
            question = question,
            userAnswer = userAnswer,
            timestamp = System.currentTimeMillis(),
            timeSpent = responseTime,
            teacherContext = currentTeacherContext!!
        )
    }

    /**
     * 🎯 UNA SOLA LLAMADA DE IA AL FINAL - OPTIMIZACIÓN DE COSTOS
     */
    suspend fun generateConsolidatedPremiumFeedback(): PremiumEvaluationResult = withContext(Dispatchers.IO) {
        isGeneratingFeedback = true

        try {
            // ✅ PREPARAR DATOS PARA UNA SOLA LLAMADA MASIVA
            val evaluationData = preparePremiumEvaluationData()

            // ✅ PROMPT OPTIMIZADO PARA ANÁLISIS MASIVO PREMIUM
            val consolidatedPrompt = createConsolidatedPremiumPrompt(evaluationData)

            // ✅ UNA SOLA LLAMADA DE IA
            val response = generativeModel.generateContent(consolidatedPrompt)
            val responseText = response.text ?: throw Exception("Respuesta vacía")

            // ✅ PARSEAR RESPUESTA CONSOLIDADA
            val result = parseConsolidatedPremiumResponse(responseText, evaluationData)

            premiumEvaluationResult = result
            evaluationCompleted = true

            result

        } catch (e: Exception) {
            // ✅ FALLBACK SIN IA (NO CUESTA NADA)
            generateFallbackPremiumEvaluation()
        } finally {
            isGeneratingFeedback = false
        }
    }

    /**
     * ✅ PREPARAR DATOS PARA ANÁLISIS MASIVO PREMIUM (AHORA DENTRO DE LA CLASE)
     */
    private fun preparePremiumEvaluationData(): PremiumEvaluationData {
        val totalQuestions = premiumEvaluationAnswers.size
        val correctAnswers = premiumEvaluationAnswers.values.count { answer ->
            answer.userAnswer.equals(answer.question.correctAnswer, ignoreCase = true)
        }

        // Agrupar por competencias
        val competencyPerformance = premiumEvaluationAnswers.values.groupBy { it.question.competency }
            .mapValues { (_, answers) ->
                val correct = answers.count { it.userAnswer.equals(it.question.correctAnswer, ignoreCase = true) }
                PremiumCompetencyScore(
                    competency = answers.first().question.competency,
                    correct = correct,
                    total = answers.size,
                    percentage = (correct.toFloat() / answers.size * 100),
                    teacherNotes = "" // Para futuro
                )
            }

        // Agrupar por dificultad
        val difficultyPerformance = premiumEvaluationAnswers.values.groupBy { it.question.difficulty }
            .mapValues { (_, answers) ->
                val correct = answers.count { it.userAnswer.equals(it.question.correctAnswer, ignoreCase = true) }
                PremiumDifficultyScore(
                    difficulty = answers.first().question.difficulty,
                    correct = correct,
                    total = answers.size,
                    percentage = (correct.toFloat() / answers.size * 100)
                )
            }

        return PremiumEvaluationData(
            moduleId = currentModuleInfo?.moduleId ?: "",
            moduleName = currentModuleInfo?.moduleName ?: "",
            teacherContext = currentTeacherContext!!,
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            totalTime = System.currentTimeMillis() - evaluationStartTime,
            competencyPerformance = competencyPerformance,
            difficultyPerformance = difficultyPerformance,
            answers = premiumEvaluationAnswers.values.toList(),
            percentage = if (totalQuestions > 0) (correctAnswers.toFloat() / totalQuestions * 100) else 0f
        )
    }

    /**
     * ✅ CREAR PROMPT PREMIUM CONSOLIDADO (DENTRO DE LA CLASE)
     */
    private fun createConsolidatedPremiumPrompt(data: PremiumEvaluationData): String {
        return """
            Eres un evaluador experto que analiza contenido ICFES PREMIUM creado por ${data.teacherContext.teacherName}.

            CONTEXTO PREMIUM:
            - Profesor: ${data.teacherContext.teacherName} de ${data.teacherContext.institution}
            - Especialización: ${data.teacherContext.specialization}
            - Contenido personalizado vs contenido estático ICFES
            - Estudiante completó evaluación con material del profesor

            MÓDULO EVALUADO: ${data.moduleName}
            PREGUNTAS TOTALES: ${data.totalQuestions}
            RESPUESTAS CORRECTAS: ${data.correctAnswers}
            PORCENTAJE: ${"%.1f".format(data.percentage)}%
            TIEMPO TOTAL: ${data.totalTime / 60000} minutos

            RENDIMIENTO POR COMPETENCIA:
            ${data.competencyPerformance.map { "${it.key}: ${it.value.correct}/${it.value.total} (${"%.1f".format(it.value.percentage)}%)" }.joinToString("\n")}

            RENDIMIENTO POR DIFICULTAD:
            ${data.difficultyPerformance.map { "${it.key}: ${it.value.correct}/${it.value.total} (${"%.1f".format(it.value.percentage)}%)" }.joinToString("\n")}

            ERRORES PRINCIPALES (solo preguntas incorrectas):
            ${data.answers.filter { !it.userAnswer.equals(it.question.correctAnswer, ignoreCase = true) }
            .take(5) // Solo los primeros 5 errores para ahorrar tokens
            .mapIndexed { index, answer ->
                "${index + 1}. ${answer.question.competency} - Respondió: ${answer.userAnswer}, Correcto: ${answer.question.correctAnswer}"
            }.joinToString("\n")}

            GENERA UNA RESPUESTA EN JSON PREMIUM CON:
            1. Fortalezas identificadas (máximo 3) - específicas del enfoque del profesor
            2. Debilidades principales (máximo 3) - considerando el material premium
            3. Recomendaciones del profesor (máximo 5) - adaptadas a su metodología
            4. Nivel alcanzado (Excelente/Muy Bueno/Bueno/Necesita Mejorar)
            5. Puntaje Premium estimado (0-100) - diferente a escala ICFES
            6. Estrategias premium (máximo 3) - específicas del profesor
            7. Análisis personalizado que compare con contenido estático ICFES

            Responde SOLO en JSON sin texto adicional:
            {
                "fortalezas": ["fortaleza1", "fortaleza2", "fortaleza3"],
                "debilidades": ["debilidad1", "debilidad2", "debilidad3"],
                "recomendacionesProfesor": ["rec1", "rec2", "rec3", "rec4", "rec5"],
                "nivel": "Muy Bueno",
                "puntajePremium": 85,
                "estrategiasPremium": ["estrategia1", "estrategia2", "estrategia3"],
                "analisisPersonalizado": "Análisis comparativo con contenido estático ICFES (máximo 150 palabras)",
                "toqueProfesor": "Comentario específico del enfoque de ${data.teacherContext.teacherName} (máximo 80 palabras)"
            }
            """.trimIndent()
    }

    /**
     * ✅ PARSEAR RESPUESTA CONSOLIDADA PREMIUM (DENTRO DE LA CLASE)
     */
    private fun parseConsolidatedPremiumResponse(
        responseText: String,
        data: PremiumEvaluationData
    ): PremiumEvaluationResult {
        try {
            val jsonText = responseText.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()

            val jsonResponse = JSONObject(jsonText)

            return PremiumEvaluationResult(
                moduleId = data.moduleId,
                moduleName = data.moduleName,
                teacherId = data.teacherContext.teacherId,
                teacherName = data.teacherContext.teacherName,
                totalQuestions = data.totalQuestions,
                correctAnswers = data.correctAnswers,
                percentage = data.percentage,
                timeSpent = data.totalTime,
                puntajePremium = jsonResponse.getInt("puntajePremium"),
                nivel = jsonResponse.getString("nivel"),
                fortalezas = parseJsonArray(jsonResponse.getJSONArray("fortalezas")),
                debilidades = parseJsonArray(jsonResponse.getJSONArray("debilidades")),
                recomendacionesProfesor = parseJsonArray(jsonResponse.getJSONArray("recomendacionesProfesor")),
                estrategiasPremium = parseJsonArray(jsonResponse.getJSONArray("estrategiasPremium")),
                analisisPersonalizado = jsonResponse.getString("analisisPersonalizado") +
                        "\n\n" + jsonResponse.optString("toqueProfesor", ""),
                competencyScores = data.competencyPerformance,
                difficultyScores = data.difficultyPerformance,
                evaluationDate = System.currentTimeMillis(),
                comparedToICFES = null // Se calculará después con datos del estudiante
            )

        } catch (e: Exception) {
            return generateFallbackPremiumEvaluation()
        }
    }

    /**
     * ✅ FALLBACK PREMIUM SIN IA (GRATIS) - DENTRO DE LA CLASE
     */
    private fun generateFallbackPremiumEvaluation(): PremiumEvaluationResult {
        val data = preparePremiumEvaluationData()

        val nivel = when {
            data.percentage >= 90 -> "Excelente"
            data.percentage >= 80 -> "Muy Bueno"
            data.percentage >= 70 -> "Bueno"
            else -> "Necesita Mejorar"
        }

        val puntajePremium = data.percentage.toInt().coerceIn(0, 100)

        return PremiumEvaluationResult(
            moduleId = data.moduleId,
            moduleName = data.moduleName,
            teacherId = data.teacherContext.teacherId,
            teacherName = data.teacherContext.teacherName,
            totalQuestions = data.totalQuestions,
            correctAnswers = data.correctAnswers,
            percentage = data.percentage,
            timeSpent = data.totalTime,
            puntajePremium = puntajePremium,
            nivel = nivel,
            fortalezas = generateBasicPremiumStrengths(data),
            debilidades = generateBasicPremiumWeaknesses(data),
            recomendacionesProfesor = generateBasicPremiumRecommendations(data),
            estrategiasPremium = generateBasicPremiumStrategies(data),
            analisisPersonalizado = "Evaluación completada con contenido premium de ${data.teacherContext.teacherName}. " +
                    "Este material está específicamente adaptado para tu institución.",
            competencyScores = data.competencyPerformance,
            difficultyScores = data.difficultyPerformance,
            evaluationDate = System.currentTimeMillis(),
            comparedToICFES = null
        )
    }

    // ✅ FUNCIONES AUXILIARES PREMIUM (DENTRO DE LA CLASE)
    private fun generateBasicPremiumStrengths(data: PremiumEvaluationData): List<String> {
        return data.competencyPerformance.filter { it.value.percentage >= 70 }
            .map { "Buen desempeño en ${it.key} con contenido de ${data.teacherContext.teacherName}" }
            .take(3)
            .ifEmpty { listOf("Completaste la evaluación premium con dedicación") }
    }

    private fun generateBasicPremiumWeaknesses(data: PremiumEvaluationData): List<String> {
        return data.competencyPerformance.filter { it.value.percentage < 60 }
            .map { "Refuerza ${it.key} con el material específico del profesor" }
            .take(3)
    }

    private fun generateBasicPremiumRecommendations(data: PremiumEvaluationData): List<String> {
        val recs = mutableListOf<String>()

        if (data.percentage < 70) {
            recs.add("Practica más con el contenido premium de ${data.teacherContext.teacherName}")
            recs.add("Revisa las explicaciones específicas de tu profesor")
        }

        recs.add("Combina el contenido premium con el material básico ICFES")
        recs.add("Consulta con ${data.teacherContext.teacherName} sobre áreas de mejora")

        return recs.take(5)
    }

    private fun generateBasicPremiumStrategies(data: PremiumEvaluationData): List<String> {
        return listOf(
            "Utiliza la metodología específica de ${data.teacherContext.teacherName}",
            "Combina contenido premium con práctica estándar ICFES",
            "Enfócate en las competencias prioritarias de tu institución"
        )
    }

    private fun parseJsonArray(jsonArray: JSONArray?): List<String> {
        if (jsonArray == null) return emptyList()
        return (0 until jsonArray.length()).map { jsonArray.getString(it) }
    }

    fun resetEvaluation() {
        isEvaluationMode = false
        evaluationCompleted = false
        premiumEvaluationResult = null
        premiumEvaluationAnswers.clear()
        isGeneratingFeedback = false
        currentTeacherContext = null
        currentModuleInfo = null
    }
}

// ===================================
// 📄 FUNCIONES DE UTILIDAD PARA INTEGRACIÓN (FUERA DE LA CLASE)
// ===================================

/**
 * ✅ FUNCIÓN PARA COMPARAR CON ICFES BÁSICO
 */
fun createPremiumICFESComparison(
    icfesScore: Int,
    premiumScore: Int,
    moduleId: String
): PremiumICFESComparison {
    val icfesNormalized = (icfesScore * 100 / 500) // Normalizar ICFES 0-500 a 0-100
    val improvement = premiumScore - icfesNormalized

    val recommendation = when {
        improvement > 20 -> "¡Excelente! El contenido premium te está ayudando mucho. Mantén el balance."
        improvement > 10 -> "Buen progreso con contenido premium. Continúa combinando ambos."
        improvement > 0 -> "Mejora leve con premium. Practica más este contenido personalizado."
        improvement > -10 -> "Rendimiento similar. Asegúrate de entender las diferencias de enfoque."
        else -> "Enfócate más en el contenido básico ICFES para este módulo."
    }

    return PremiumICFESComparison(
        icfesScore = icfesScore,
        premiumScore = premiumScore,
        improvement = improvement,
        recommendation = recommendation
    )
}

// ===================================
// 📊 DATA CLASSES AUXILIARES
// ===================================

data class PremiumEvaluationAnswer(
    val questionId: String,
    val question: PremiumQuestion,
    val userAnswer: String,
    val timestamp: Long,
    val timeSpent: Long,
    val teacherContext: TeacherContext
)

data class PremiumModuleInfo(
    val moduleId: String,
    val moduleName: String,
    val totalQuestions: Int
)