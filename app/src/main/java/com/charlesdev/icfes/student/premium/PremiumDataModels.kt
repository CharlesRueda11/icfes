package com.charlesdev.icfes.student.premium



// ===================================
// 🎯 MODELOS PARA RESPUESTAS PREMIUM
// ===================================

data class PremiumQuestionResponse(
    val questionId: String,
    val userAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val timeSpent: Long,
    val difficulty: String,
    val competency: String,
    val teacherId: String,
    val teacherName: String,
    val moduleId: String,
    val sessionType: String, // "practica" o "evaluacion"
    val timestamp: Long = System.currentTimeMillis()
)

// ===================================
// 📈 MODELOS PARA EVALUACIONES PREMIUM
// ===================================

data class PremiumEvaluationResult(
    val moduleId: String,
    val moduleName: String,
    val teacherId: String,
    val teacherName: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val percentage: Float,
    val timeSpent: Long,
    val puntajePremium: Int, // Escala 0-100 (diferente a ICFES 0-500)
    val nivel: String, // "Excelente", "Muy Bueno", "Bueno", "Necesita Mejorar"
    val fortalezas: List<String>,
    val debilidades: List<String>,
    val recomendacionesProfesor: List<String>, // Específicas del profesor
    val estrategiasPremium: List<String>,
    val analisisPersonalizado: String, // Análisis del profesor o IA
    val competencyScores: Map<String, PremiumCompetencyScore>,
    val difficultyScores: Map<String, PremiumDifficultyScore>,
    val evaluationDate: Long = System.currentTimeMillis(),
    // NUEVO: Comparación con contenido básico ICFES
    val comparedToICFES: PremiumICFESComparison?
)

data class PremiumCompetencyScore(
    val competency: String,
    val correct: Int,
    val total: Int,
    val percentage: Float,
    val teacherNotes: String = "" // Notas específicas del profesor
)

data class PremiumDifficultyScore(
    val difficulty: String,
    val correct: Int,
    val total: Int,
    val percentage: Float
)

data class PremiumICFESComparison(
    val icfesScore: Int, // Puntaje en contenido básico ICFES
    val premiumScore: Int, // Puntaje en contenido premium
    val improvement: Int, // Diferencia positiva/negativa
    val recommendation: String // "Mantén balance", "Enfócate más en premium", etc.
)

// ===================================
// 🎯 MODELOS PARA SIMULACROS PREMIUM
// ===================================

data class PremiumSimulationResult(
    val totalQuestions: Int,
    val correctAnswers: Int,
    val percentage: Float,
    val globalScore: Int, // Escala 0-100 premium
    val moduleScores: Map<String, PremiumModuleSimulationScore>,
    val timeSpent: Long,
    val nivel: String,
    val completedAt: Long,
    val teacherId: String,
    val teacherName: String,
    // NUEVO: Comparación con simulacro ICFES básico
    val icfesComparison: SimulationComparison?
)

data class PremiumModuleSimulationScore(
    val moduleId: String,
    val moduleName: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val percentage: Float,
    val puntajePremium: Int, // 0-100
    val teacherFeedback: String = "" // Feedback específico del profesor para este módulo
)

data class SimulationComparison(
    val icfesGlobalScore: Int, // Puntaje simulacro básico
    val premiumGlobalScore: Int, // Puntaje simulacro premium
    val overallImprovement: Int,
    val moduleImprovements: Map<String, Int> // Mejoras por módulo
)

// ===================================
// 💡 MODELOS PARA FEEDBACK PREMIUM
// ===================================

data class PremiumFeedback(
    val questionId: String,
    val isCorrect: Boolean,
    val title: String,
    val message: String,
    val tip: String,
    val teacherPersonalization: String, // Toque personal del profesor
    val relatedTopics: List<String>,
    val generatedBy: String, // "TEACHER", "AI", "TEMPLATE"
    val confidence: Float = 1.0f, // Para futuro análisis de calidad
    val timestamp: Long = System.currentTimeMillis()
)

data class PremiumAIAnalysis(
    val analysisId: String,
    val studentResponses: List<PremiumQuestionResponse>,
    val teacherContext: TeacherContext,
    val aiRecommendations: List<String>,
    val personalizedStrategies: List<String>,
    val nextSteps: List<String>,
    val confidenceScore: Float,
    val generatedAt: Long = System.currentTimeMillis()
)

data class TeacherContext(
    val teacherId: String,
    val teacherName: String,
    val institution: String,
    val teachingStyle: String = "", // Para personalización futura
    val specialization: String = "",
    val customInstructions: String = "" // Instrucciones específicas del profesor para IA
)

// ===================================
// 📊 MODELOS PARA PROGRESO PREMIUM
// ===================================

data class PremiumStudentProgress(
    val studentId: String,
    val teacherId: String,
    val teacherName: String,
    val institution: String,
    val moduleProgress: Map<String, PremiumModuleProgress>,
    val overallStats: PremiumOverallStats,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class PremiumModuleProgress(
    val moduleId: String,
    val moduleName: String,
    val practiceScore: Int, // Mejor puntaje en práctica (0-100)
    val evaluationScore: Int, // Mejor puntaje en evaluación (0-100)
    val practiceAttempts: Int,
    val evaluationAttempts: Int,
    val totalTimeSpent: Long,
    val strengthAreas: List<String>,
    val improvementAreas: List<String>,
    val teacherNotes: String = "",
    val lastActivity: Long,
    val isUnlocked: Boolean = true, // Para futuro sistema de prerrequisitos
    val nextRecommendation: String = ""
)

data class PremiumOverallStats(
    val totalQuestionsAnswered: Int,
    val averagePremiumScore: Float,
    val totalStudyTime: Long,
    val streak: Int, // Días consecutivos usando contenido premium
    val achievements: List<PremiumAchievement>,
    val level: Int, // Nivel específico con el profesor
    val experience: Int,
    val favoriteTeacher: String = "", // Si tiene múltiples profesores
    val preferredDifficulty: String = "MEDIO"
)

data class PremiumAchievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val unlockedAt: Long,
    val teacherId: String, // Profesor que "otorgó" el logro
    val rarity: String = "COMMON" // COMMON, RARE, EPIC, LEGENDARY
)

// ===================================
// 🎓 MODELOS PARA ANÁLISIS DEL PROFESOR
// ===================================

data class TeacherAnalytics(
    val teacherId: String,
    val teacherName: String,
    val institution: String,
    val studentCount: Int,
    val moduleAnalytics: Map<String, ModuleAnalytics>,
    val overallPerformance: TeacherOverallPerformance,
    val generatedAt: Long = System.currentTimeMillis()
)

data class ModuleAnalytics(
    val moduleId: String,
    val moduleName: String,
    val totalQuestions: Int, // Preguntas creadas por el profesor
    val avgStudentScore: Float,
    val completionRate: Float, // % de estudiantes que completan
    val difficultyDistribution: Map<String, Int>, // FACIL, MEDIO, DIFICIL
    val mostMissedQuestions: List<String>, // IDs de preguntas más falladas
    val topPerformingStudents: List<String>,
    val studentsNeedingHelp: List<String>
)

data class TeacherOverallPerformance(
    val contentQuality: Float, // Basado en performance de estudiantes
    val engagementRate: Float, // Cuánto usan los estudiantes el contenido
    val improvementRate: Float, // Cuánto mejoran vs contenido básico
    val recommendation: String // Sugerencia para el profesor
)

// ===================================
// 🔄 MODELOS PARA SINCRONIZACIÓN
// ===================================

data class PremiumSyncData(
    val lastSyncTimestamp: Long,
    val pendingUploads: List<PremiumQuestionResponse>,
    val cacheVersion: Int,
    val teacherContentVersion: Map<String, Int>, // Por módulo
    val syncStatus: String = "SYNCED" // SYNCED, PENDING, ERROR
)

// ===================================
// ⚙️ MODELOS DE CONFIGURACIÓN PREMIUM
// ===================================

data class PremiumSettings(
    val studentId: String,
    val preferredTeachers: List<String>, // Si hay múltiples
    val autoSyncEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val feedbackLevel: String = "DETAILED", // BASIC, DETAILED, ADVANCED
    val aiPersonalization: Boolean = true,
    val shareProgressWithTeacher: Boolean = true,
    val preferredStudyTime: String = "AFTERNOON", // Para recomendaciones
    val offlineMode: Boolean = false
)

// ===================================
// 🔍 MODELOS PARA BÚSQUEDA Y FILTROS
// ===================================

data class PremiumSearchFilter(
    val teacherId: String? = null,
    val moduleId: String? = null,
    val difficulty: String? = null,
    val competency: String? = null,
    val sessionType: String? = null,
    val dateRange: Pair<Long, Long>? = null,
    val scoreRange: Pair<Int, Int>? = null,
    val onlyIncorrect: Boolean = false
)

data class PremiumSearchResult(
    val questions: List<PremiumQuestion>,
    val responses: List<PremiumQuestionResponse>,
    val analytics: SearchAnalytics,
    val totalFound: Int,
    val searchTime: Long
)

data class SearchAnalytics(
    val avgScore: Float,
    val mostCommonMistakes: List<String>,
    val timeDistribution: Map<String, Int>,
    val difficultyBreakdown: Map<String, Int>
)

// ===================================
// 📱 MODELOS PARA INTEGRACIÓN CON UI
// ===================================

data class PremiumUIState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentTeacher: TeacherContext? = null,
    val availableModules: List<PremiumModuleData> = emptyList(),
    val syncStatus: PremiumSyncData? = null,
    val notifications: List<PremiumNotification> = emptyList()
)

data class PremiumNotification(
    val id: String,
    val type: String, // "NEW_CONTENT", "TEACHER_FEEDBACK", "ACHIEVEMENT", "REMINDER"
    val title: String,
    val message: String,
    val teacherId: String? = null,
    val actionRequired: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

// ===================================
// 🚀 EXTENSION FUNCTIONS PARA CONVERSIÓN
// ===================================

// Convertir de PremiumQuestion a formato compatible con ICFES
fun PremiumQuestion.toICFESFormat(): Map<String, Any> {
    return mapOf(
        "id" to id,
        "question" to question,
        "context" to (context ?: ""),
        "options" to options,
        "correctAnswer" to correctAnswer,
        "competency" to competency,
        "difficulty" to difficulty,
        "timeEstimated" to timeEstimated,
        "explanation" to (explanation ?: ""),
        "tags" to listOf("premium", "teacher_content"),
        "source" to "PREMIUM"
    )
}

// Convertir PremiumEvaluationResult a formato para SharedPreferences
fun PremiumEvaluationResult.toPreferencesMap(prefix: String = "premium"): Map<String, Any> {
    return mapOf(
        "${prefix}_score_${moduleId}" to puntajePremium,
        "${prefix}_percentage_${moduleId}" to percentage,
        "${prefix}_level_${moduleId}" to nivel,
        "${prefix}_teacher_${moduleId}" to teacherId,
        "${prefix}_timestamp_${moduleId}" to evaluationDate,
        "${prefix}_correct_${moduleId}" to correctAnswers,
        "${prefix}_total_${moduleId}" to totalQuestions
    )
}

// Crear comparación automática con datos ICFES
fun createICFESComparison(
    icfesScore: Int,
    premiumScore: Int,
    moduleId: String
): PremiumICFESComparison {
    val improvement = premiumScore - (icfesScore * 100 / 500) // Normalizar ICFES a escala 0-100

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