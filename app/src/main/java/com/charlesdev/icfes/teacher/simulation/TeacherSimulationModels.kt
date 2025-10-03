package com.charlesdev.icfes.teacher.simulation



import com.charlesdev.icfes.teacher.practice_evaluation.TeacherQuestion
import com.google.firebase.database.IgnoreExtraProperties

/**
 * ===================================
 * 🎯 MODELOS DE DATOS - GESTIÓN DE SIMULACROS PREMIUM
 * ===================================
 * Estructuras para que el profesor cree y gestione simulacros personalizados
 */

// ✅ CONFIGURACIÓN PRINCIPAL DEL PROFESOR
@IgnoreExtraProperties
data class TeacherSimulation(
    val teacherId: String = "",
    val teacherName: String = "",
    val institution: String = "",
    val simulationId: String = "",
    val title: String = "Simulacro Premium ICFES",
    val description: String = "Contenido personalizado para estudiantes",
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val totalQuestions: Int = 175,
    val totalDuration: Long = (4.5 * 60 * 60 * 1000).toLong(), // 4.5 horas en milisegundos
    val config: TeacherSimulationConfig = TeacherSimulationConfig(),
    val sessions: List<TeacherSimulationSession> = emptyList(),
    val analytics: SimulationAnalytics = SimulationAnalytics()
)

// ✅ CONFIGURACIÓN ESPECÍFICA DEL PROFESOR
@IgnoreExtraProperties
data class TeacherSimulationConfig(
    val allowPauseInBreaks: Boolean = true,
    val autoSubmitEnabled: Boolean = true,
    val showDetailedAnalytics: Boolean = true,
    val includeTeacherFeedback: Boolean = true,
    val customInstructions: String = "",
    val timeWarnings: List<Int> = listOf(10, 5, 1), // minutos
    val enableTimer: Boolean = true,
    val shuffleQuestions: Boolean = true,
    val showProgressBar: Boolean = true,
    val strictMode: Boolean = true,
    val certificateEnabled: Boolean = true,
    val minimumPracticeScore: Int = 50 // Para desbloquear evaluación
)

// ✅ SESIÓN PERSONALIZADA POR PROFESOR
@IgnoreExtraProperties
data class TeacherSimulationSession(
    val sessionNumber: Int = 0,
    val moduleId: String = "",
    val moduleName: String = "",
    val description: String = "",
    val questions: List<TeacherQuestion> = emptyList(),
    val timeLimit: Int = 65, // minutos
    val difficultyBias: DifficultyBias = DifficultyBias.BALANCED,
    val focusAreas: List<String> = emptyList(),
    val customInstructions: String = "",
    val color: Comparable<*> = 0xFF2196F3, // Color en formato Long
    val icon: String = "📚",
    val isCompleted: Boolean = false
)

// ✅ SESGO DE DIFICULTAD PERSONALIZABLE
enum class DifficultyBias(val displayName: String, val description: String) {
    FACIL("Más fácil", "Enfocado en fortalecer bases"),
    BALANCED("Balanceado", "Distribución estándar"),
    DIFICIL("Más desafiante", "Preparación para alto rendimiento"),
    PERSONALIZADO("Personalizado", "Basado en análisis previo")
}

// ✅ ANÁLISIS DE RENDIMIENTO
@IgnoreExtraProperties
data class SimulationAnalytics(
    val totalStudentsAttempted: Int = 0,
    val totalStudentsCompleted: Int = 0,
    val averageGlobalScore: Double = 0.0,
    val bestScore: Int = 0,
    val completionRate: Double = 0.0,
    val averageTimePerSession: Map<String, Long> = emptyMap(),
    val commonMistakes: List<String> = emptyList(),
    val improvementAreas: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

// ✅ CONFIGURACIÓN AVANZADA POR MÓDULO
@IgnoreExtraProperties
data class ModuleConfiguration(
    val moduleId: String = "",
    val moduleName: String = "",
    val questionCount: Int = 35,
    val timeLimit: Int = 65,
    val difficultyDistribution: Map<String, Int> = mapOf(
        "FACIL" to 12,
        "MEDIO" to 18,
        "DIFICIL" to 5
    ),
    val focusCompetencies: List<String> = emptyList(),
    val excludedTopics: List<String> = emptyList()
)

// ✅ PROGRESO DEL ESTUDIANTE EN SIMULACRO
@IgnoreExtraProperties
data class StudentSimulationProgress(
    val studentId: String = "",
    val studentName: String = "",
    val teacherId: String = "",
    val simulationId: String = "",
    val attemptNumber: Int = 1,
    val startTime: Long = 0,
    val endTime: Long = 0,
    val globalScore: Int = 0,
    val globalPercentage: Double = 0.0,
    val sessionResults: List<StudentSessionResult> = emptyList(),
    val timeSpent: Long = 0,
    val completionStatus: CompletionStatus = CompletionStatus.IN_PROGRESS,
    val teacherNotes: String = ""
)

// ✅ RESULTADO POR SESIÓN
@IgnoreExtraProperties
data class StudentSessionResult(
    val sessionId: String = "",
    val moduleName: String = "",
    val score: Int = 0,
    val percentage: Double = 0.0,
    val timeSpent: Long = 0,
    val correctAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val competencyBreakdown: Map<String, Double> = emptyMap()
)

// ✅ ESTADO DE COMPLETADO
enum class CompletionStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    ABANDONED,
    TIME_EXPIRED
}

// ✅ REPORTE DE ANÁLISIS PARA EL PROFESOR
@IgnoreExtraProperties
data class TeacherAnalysisReport(
    val reportId: String = "",
    val simulationId: String = "",
    val teacherId: String = "",
    val generatedAt: Long = System.currentTimeMillis(),
    val summary: ReportSummary = ReportSummary(),
    val studentDetails: List<StudentDetail> = emptyList(),
    val recommendations: List<String> = emptyList()
)

@IgnoreExtraProperties
data class ReportSummary(
    val totalStudents: Int = 0,
    val averageScore: Double = 0.0,
    val scoreRange: Pair<Int, Int> = Pair(0, 0),
    val mostChallengingModule: String = "",
    val strongestModule: String = ""
)

@IgnoreExtraProperties
data class StudentDetail(
    val studentId: String = "",
    val studentName: String = "",
    val score: Int = 0,
    val percentile: Int = 0,
    val improvementAreas: List<String> = emptyList(),
    val strengths: List<String> = emptyList()
)

// ✅ CONFIGURACIÓN DE EXPORTACIÓN
@IgnoreExtraProperties
data class ExportConfiguration(
    val includeStudentNames: Boolean = true,
    val includeDetailedAnalytics: Boolean = true,
    val format: ExportFormat = ExportFormat.PDF,
    val includeComparativeCharts: Boolean = true
)

enum class ExportFormat {
    PDF,
    EXCEL,
    JSON,
    CSV
}

// ✅ FUNCIÓN AUXILIAR PARA CALCULAR TIEMPO TOTAL
fun calculateTotalTime(questions: Int, timePerQuestion: Double): Long {
    return (questions * timePerQuestion * 60 * 1000).toLong()
}

// ✅ FUNCIÓN PARA VALIDAR CONFIGURACIÓN
fun TeacherSimulation.validate(): ValidationResult {
    return ValidationResult(
        isValid = sessions.isNotEmpty() && totalQuestions > 0,
        errors = mutableListOf<String>().apply {
            if (sessions.isEmpty()) add("Debe incluir al menos una sesión")
            if (totalQuestions <= 0) add("Debe incluir preguntas")
            if (totalDuration <= 0) add("Duración inválida")
        }
    )
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)