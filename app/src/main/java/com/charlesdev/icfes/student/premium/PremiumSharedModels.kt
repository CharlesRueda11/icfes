package com.charlesdev.icfes.student.premium

// ===================================
// 🔄 DATA CLASSES COMPARTIDAS PARA AMBOS MANAGERS
// ===================================

/**
 * ✅ DATA CLASS UNIFICADA PARA RESPUESTAS PREMIUM
 * Usada tanto en PremiumAIManager como en PremiumEvaluationManager
 */
data class PremiumAnswerUnified(
    val questionId: String,
    val question: PremiumQuestion,
    val userAnswer: String,
    val timestamp: Long,
    val timeSpent: Long,
    val teacherContext: TeacherContext
)

/**
 * ✅ DATA CLASS PARA DATOS DE EVALUACIÓN PREMIUM
 * Compartida entre ambos managers
 */
data class PremiumEvaluationDataShared(
    val moduleId: String,
    val moduleName: String,
    val teacherContext: TeacherContext,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val totalTime: Long,
    val competencyPerformance: Map<String, PremiumCompetencyScore>,
    val difficultyPerformance: Map<String, PremiumDifficultyScore>,
    val answers: List<PremiumAnswerUnified>, // ✅ TIPO UNIFICADO
    val percentage: Float
)

/**
 * ✅ FUNCIONES DE EXTENSIÓN PARA CONVERSIÓN
 */

// Convertir de PremiumAnswer (AIManager) a PremiumAnswerUnified
fun PremiumAnswer.toUnified(): PremiumAnswerUnified {
    return PremiumAnswerUnified(
        questionId = this.questionId,
        question = this.question,
        userAnswer = this.userAnswer,
        timestamp = this.timestamp,
        timeSpent = this.timeSpent,
        teacherContext = this.teacherContext
    )
}

// Convertir de PremiumEvaluationAnswer (EvaluationManager) a PremiumAnswerUnified
fun PremiumEvaluationAnswer.toUnified(): PremiumAnswerUnified {
    return PremiumAnswerUnified(
        questionId = this.questionId,
        question = this.question,
        userAnswer = this.userAnswer,
        timestamp = this.timestamp,
        timeSpent = this.timeSpent,
        teacherContext = this.teacherContext
    )
}

// Convertir de PremiumEvaluationData a PremiumEvaluationDataShared
fun PremiumEvaluationData.toShared(): PremiumEvaluationDataShared {
    return PremiumEvaluationDataShared(
        moduleId = this.moduleId,
        moduleName = this.moduleName,
        teacherContext = this.teacherContext,
        totalQuestions = this.totalQuestions,
        correctAnswers = this.correctAnswers,
        totalTime = this.totalTime,
        competencyPerformance = this.competencyPerformance,
        difficultyPerformance = this.difficultyPerformance,
        answers = this.answers.map { it.toUnified() }, // ✅ CONVERSIÓN AUTOMÁTICA
        percentage = this.percentage
    )
}

/**
 * ✅ FUNCIONES AUXILIARES PARA AMBOS MANAGERS
 */
object PremiumDataUtils {

    /**
     * Crear datos de evaluación premium desde una colección de respuestas unificadas
     */
    fun createEvaluationData(
        answers: Collection<PremiumAnswerUnified>,
        moduleId: String,
        moduleName: String,
        sessionStartTime: Long,
        teacherContext: TeacherContext
    ): PremiumEvaluationDataShared {
        val totalQuestions = answers.size
        val correctAnswers = answers.count { answer ->
            answer.userAnswer.equals(answer.question.correctAnswer, ignoreCase = true)
        }

        // Agrupar por competencias
        val competencyPerformance = answers.groupBy { it.question.competency }
            .mapValues { (_, answerList) ->
                val correct = answerList.count { it.userAnswer.equals(it.question.correctAnswer, ignoreCase = true) }
                PremiumCompetencyScore(
                    competency = answerList.first().question.competency,
                    correct = correct,
                    total = answerList.size,
                    percentage = (correct.toFloat() / answerList.size * 100),
                    teacherNotes = ""
                )
            }

        // Agrupar por dificultad
        val difficultyPerformance = answers.groupBy { it.question.difficulty }
            .mapValues { (_, answerList) ->
                val correct = answerList.count { it.userAnswer.equals(it.question.correctAnswer, ignoreCase = true) }
                PremiumDifficultyScore(
                    difficulty = answerList.first().question.difficulty,
                    correct = correct,
                    total = answerList.size,
                    percentage = (correct.toFloat() / answerList.size * 100)
                )
            }

        return PremiumEvaluationDataShared(
            moduleId = moduleId,
            moduleName = moduleName,
            teacherContext = teacherContext,
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            totalTime = System.currentTimeMillis() - sessionStartTime,
            competencyPerformance = competencyPerformance,
            difficultyPerformance = difficultyPerformance,
            answers = answers.toList(),
            percentage = if (totalQuestions > 0) (correctAnswers.toFloat() / totalQuestions * 100) else 0f
        )
    }

    /**
     * Crear prompt consolidado premium (reutilizable)
     */
    fun createConsolidatedPrompt(data: PremiumEvaluationDataShared): String {
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
            .take(5)
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
     * Parsear respuesta JSON consolidada (reutilizable)
     */
    fun parseConsolidatedResponse(
        responseText: String,
        data: PremiumEvaluationDataShared
    ): PremiumEvaluationResult? {
        return try {
            val jsonText = responseText.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()

            val jsonResponse = org.json.JSONObject(jsonText)

            PremiumEvaluationResult(
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
                comparedToICFES = null
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generar evaluación fallback (reutilizable)
     */
    fun generateFallbackEvaluation(data: PremiumEvaluationDataShared): PremiumEvaluationResult {
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
            fortalezas = generateBasicStrengths(data),
            debilidades = generateBasicWeaknesses(data),
            recomendacionesProfesor = generateBasicRecommendations(data),
            estrategiasPremium = generateBasicStrategies(data),
            analisisPersonalizado = "Evaluación completada con contenido premium de ${data.teacherContext.teacherName}. " +
                    "Este material está específicamente adaptado para tu institución.",
            competencyScores = data.competencyPerformance,
            difficultyScores = data.difficultyPerformance,
            evaluationDate = System.currentTimeMillis(),
            comparedToICFES = null
        )
    }

    // Funciones auxiliares privadas
    private fun parseJsonArray(jsonArray: org.json.JSONArray?): List<String> {
        if (jsonArray == null) return emptyList()
        return (0 until jsonArray.length()).map { jsonArray.getString(it) }
    }

    private fun generateBasicStrengths(data: PremiumEvaluationDataShared): List<String> {
        return data.competencyPerformance.filter { it.value.percentage >= 70 }
            .map { "Buen desempeño en ${it.key} con contenido de ${data.teacherContext.teacherName}" }
            .take(3)
            .ifEmpty { listOf("Completaste la evaluación premium con dedicación") }
    }

    private fun generateBasicWeaknesses(data: PremiumEvaluationDataShared): List<String> {
        return data.competencyPerformance.filter { it.value.percentage < 60 }
            .map { "Refuerza ${it.key} con el material específico del profesor" }
            .take(3)
    }

    private fun generateBasicRecommendations(data: PremiumEvaluationDataShared): List<String> {
        val recs = mutableListOf<String>()

        if (data.percentage < 70) {
            recs.add("Practica más con el contenido premium de ${data.teacherContext.teacherName}")
            recs.add("Revisa las explicaciones específicas de tu profesor")
        }

        recs.add("Combina el contenido premium con el material básico ICFES")
        recs.add("Consulta con ${data.teacherContext.teacherName} sobre áreas de mejora")

        return recs.take(5)
    }

    private fun generateBasicStrategies(data: PremiumEvaluationDataShared): List<String> {
        return listOf(
            "Utiliza la metodología específica de ${data.teacherContext.teacherName}",
            "Combina contenido premium con práctica estándar ICFES",
            "Enfócate en las competencias prioritarias de tu institución"
        )
    }
}