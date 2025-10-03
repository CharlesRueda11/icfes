package com.charlesdev.icfes.student.premium



import androidx.compose.runtime.*
import com.charlesdev.icfes.data.Data
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.json.JSONArray

/**
 * 🎯 MANAGER ESPECÍFICO PARA IA PREMIUM
 * Optimizado para contenido personalizado por profesor
 */
class PremiumAIManager {

    // ✅ MISMO MODELO QUE ICFES
    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = Data.apikey
    )

    // ✅ ALMACENAMIENTO DE RESPUESTAS SIN IA INMEDIATA (como ICFESEvaluationManager)
    val premiumAnswers = mutableMapOf<Int, PremiumAnswer>()
    private var sessionStartTime = 0L
    private var currentTeacherContext: TeacherContext? = null

    // ✅ ESTADO DE EVALUACIÓN PREMIUM
    var isEvaluationMode by mutableStateOf(false)
        private set
    var evaluationCompleted by mutableStateOf(false)
        private set
    var premiumFeedback by mutableStateOf<PremiumEvaluationResult?>(null)
        private set
    var isGeneratingFeedback by mutableStateOf(false)
        private set

    /**
     * ✅ INICIALIZAR SESIÓN PREMIUM
     */
    fun startPremiumSession(
        teacherId: String,
        teacherName: String,
        institution: String,
        sessionType: PremiumSessionType,
        specialization: String = ""
    ) {
        isEvaluationMode = sessionType == PremiumSessionType.EVALUATION
        sessionStartTime = System.currentTimeMillis()
        premiumAnswers.clear()
        evaluationCompleted = false
        premiumFeedback = null

        // ✅ CONFIGURAR CONTEXTO DEL PROFESOR
        currentTeacherContext = TeacherContext(
            teacherId = teacherId,
            teacherName = teacherName,
            institution = institution,
            specialization = specialization,
            teachingStyle = "personalizado", // Para futuro
            customInstructions = "" // Para futuro
        )
    }

    /**
     * ✅ GUARDAR RESPUESTA SIN FEEDBACK INMEDIATO (COMO ICFESEvaluationManager)
     */
    fun submitPremiumAnswer(
        questionIndex: Int,
        question: PremiumQuestion,
        userAnswer: String
    ) {
        val responseTime = System.currentTimeMillis() - sessionStartTime

        premiumAnswers[questionIndex] = PremiumAnswer(
            questionId = question.id,
            question = question,
            userAnswer = userAnswer,
            timestamp = System.currentTimeMillis(),
            timeSpent = responseTime,
            teacherContext = currentTeacherContext!!
        )
    }

    /**
     * 🎯 FEEDBACK INMEDIATO PARA PRÁCTICA (PERSONALIZADO POR PROFESOR)
     */
    suspend fun generateImmediateFeedback(
        question: PremiumQuestion,
        userAnswer: String,
        isCorrect: Boolean
    ): PremiumFeedback = withContext(Dispatchers.IO) {
        try {
            val promptTemplate = createPremiumFeedbackPrompt(
                question = question,
                userAnswer = userAnswer,
                isCorrect = isCorrect,
                teacherContext = currentTeacherContext!!
            )

            val response = generativeModel.generateContent(promptTemplate)
            val responseText = response.text ?: throw Exception("Respuesta vacía")

            val jsonText = responseText.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()

            val jsonResponse = JSONObject(jsonText)

            PremiumFeedback(
                questionId = question.id,
                isCorrect = jsonResponse.getBoolean("es_correcto"),
                title = jsonResponse.optString("titulo", if (isCorrect) "¡Correcto!" else "Incorrecto"),
                message = jsonResponse.getString("feedback"),
                tip = jsonResponse.optString("consejo", ""),
                teacherPersonalization = jsonResponse.optString("toque_profesor", ""),
                relatedTopics = parseJsonArray(jsonResponse.optJSONArray("temas_relacionados") ?: JSONArray()),
                generatedBy = "AI",
                confidence = 0.85f
            )

        } catch (e: Exception) {
            // ✅ FALLBACK SIN IA (NO CUESTA NADA)
            createPremiumFallback(question, userAnswer, isCorrect)
        }
    }

    /**
     * 🎯 UNA SOLA LLAMADA DE IA AL FINAL PARA EVALUACIONES (OPTIMIZACIÓN DE COSTOS)
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

            premiumFeedback = result
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
     * ✅ PROMPT ESPECÍFICO PARA FEEDBACK PREMIUM INMEDIATO
     */
    private fun createPremiumFeedbackPrompt(
        question: PremiumQuestion,
        userAnswer: String,
        isCorrect: Boolean,
        teacherContext: TeacherContext
    ): String {
        return """
        Eres un asistente especializado en el contenido ICFES creado por el profesor ${teacherContext.teacherName} de ${teacherContext.institution}.

        CONTEXTO ESPECIAL:
        - Esta pregunta fue creada específicamente por ${teacherContext.teacherName}
        - Es contenido PREMIUM personalizado para estudiantes de ${teacherContext.institution}
        - Especialización del profesor: ${teacherContext.specialization}
        - Debes incorporar el enfoque pedagógico personalizado del profesor

        PREGUNTA PREMIUM:
        ${question.question}

        ${if (!question.context.isNullOrEmpty()) "TEXTO BASE:\n${question.context}\n" else ""}

        OPCIONES:
        ${question.options.joinToString("\n")}

        RESPUESTA DEL ESTUDIANTE: ${userAnswer}
        RESPUESTA CORRECTA: ${question.correctAnswer}
        RESULTADO: ${if (isCorrect) "CORRECTO" else "INCORRECTO"}

        EXPLICACIÓN ORIGINAL DEL PROFESOR:
        ${question.explanation ?: "Sin explicación específica"}

        INSTRUCCIONES PREMIUM:
        - Proporciona feedback que COMBINE la explicación del profesor con análisis IA
        - Si es CORRECTO: Refuerza el enfoque del profesor y conecta con su metodología
        - Si es INCORRECTO: Usa la explicación del profesor como base y amplía con estrategias
        - Incluye un "toque personal" que refleje el estilo del profesor ${teacherContext.teacherName}
        - Menciona que es contenido premium de ${teacherContext.institution}
        - Máximo 120 palabras en feedback principal
        - NO uses emoticones en tu respuesta

        Responde en JSON:
        {
            "es_correcto": ${isCorrect},
            "titulo": "Título motivador apropiado sin emoticones",
            "feedback": "Feedback que incorpora la explicación del profesor sin emoticones",
            "consejo": "Estrategia específica del enfoque del profesor sin emoticones",
            "toque_profesor": "Comentario que refleje el estilo pedagógico del profesor",
            "temas_relacionados": ["tema1", "tema2", "tema3"]
        }
        """.trimIndent()
    }

    /**
     * ✅ PROMPT OPTIMIZADO PARA ANÁLISIS MASIVO PREMIUM (MENOS TOKENS)
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


    private fun preparePremiumEvaluationData(): PremiumEvaluationData {
        // ✅ USAR LA FUNCIÓN COMPARTIDA
        val sharedData = PremiumDataUtils.createEvaluationData(
            answers = premiumAnswers.values.map { it.toUnified() }, // Convertir a tipo unificado
            moduleId = if (premiumAnswers.isNotEmpty())
                premiumAnswers.values.first().question.id.split("_").firstOrNull() ?: "unknown"
            else "unknown",
            moduleName = "Módulo Premium", // Se actualizará desde el ViewModel
            sessionStartTime = sessionStartTime,
            teacherContext = currentTeacherContext!!
        )

        // ✅ CONVERTIR DE VUELTA AL FORMATO ESPERADO POR ESTA CLASE
        return PremiumEvaluationData(
            moduleId = sharedData.moduleId,
            moduleName = sharedData.moduleName,
            teacherContext = sharedData.teacherContext,
            totalQuestions = sharedData.totalQuestions,
            correctAnswers = sharedData.correctAnswers,
            totalTime = sharedData.totalTime,
            competencyPerformance = sharedData.competencyPerformance,
            difficultyPerformance = sharedData.difficultyPerformance,
            // ✅ CONVERTIR DE VUELTA A PremiumEvaluationAnswer
            answers = sharedData.answers.map { unified ->
                PremiumEvaluationAnswer(
                    questionId = unified.questionId,
                    question = unified.question,
                    userAnswer = unified.userAnswer,
                    timestamp = unified.timestamp,
                    timeSpent = unified.timeSpent,
                    teacherContext = unified.teacherContext
                )
            },
            percentage = sharedData.percentage
        )
    }

    /**
     * ✅ PARSEAR RESPUESTA CONSOLIDADA PREMIUM
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
     * ✅ FALLBACK PREMIUM SIN IA (GRATIS)
     */
    private fun createPremiumFallback(
        question: PremiumQuestion,
        userAnswer: String,
        isCorrect: Boolean
    ): PremiumFeedback {
        return if (isCorrect) {
            PremiumFeedback(
                questionId = question.id,
                isCorrect = true,
                title = "Correcto",
                message = "Excelente trabajo con el contenido premium. ${question.explanation ?: ""}",
                tip = "Continúa practicando con el material de ${currentTeacherContext?.teacherName}.",
                teacherPersonalization = "Este contenido fue diseñado específicamente por tu profesor.",
                relatedTopics = listOf(question.competency),
                generatedBy = "TEMPLATE"
            )
        } else {
            PremiumFeedback(
                questionId = question.id,
                isCorrect = false,
                title = "Incorrecto",
                message = "La respuesta correcta es ${question.correctAnswer}. ${question.explanation ?: ""}",
                tip = "Revisa el material específico de ${currentTeacherContext?.teacherName} sobre este tema.",
                teacherPersonalization = "Tu profesor ha incluido explicaciones detalladas para este tipo de preguntas.",
                relatedTopics = listOf(question.competency),
                generatedBy = "TEMPLATE"
            )
        }
    }

    /**
     * ✅ FALLBACK PREMIUM PARA EVALUACIÓN
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

    // ✅ FUNCIONES AUXILIARES PREMIUM
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

    fun resetPremiumSession() {
        isEvaluationMode = false
        evaluationCompleted = false
        premiumFeedback = null
        premiumAnswers.clear()
        isGeneratingFeedback = false
        currentTeacherContext = null
    }
}

// ✅ DATA CLASSES AUXILIARES PARA PREMIUM AI
data class PremiumAnswer(
    val questionId: String,
    val question: PremiumQuestion,
    val userAnswer: String,
    val timestamp: Long,
    val timeSpent: Long,
    val teacherContext: TeacherContext
)

data class PremiumEvaluationData(
    val moduleId: String,
    val moduleName: String,
    val teacherContext: TeacherContext,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val totalTime: Long,
    val competencyPerformance: Map<String, PremiumCompetencyScore>,
    val difficultyPerformance: Map<String, PremiumDifficultyScore>,
    val answers: List<PremiumEvaluationAnswer>,
    val percentage: Float
)