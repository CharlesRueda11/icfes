package com.charlesdev.icfes.student.simulation

import com.charlesdev.icfes.student.data.ICFESQuestion
import com.charlesdev.icfes.student.data.Difficulty

/**
 * ===================================
 * 📁 MODELOS ESPECÍFICOS DEL SIMULACRO COMPLETO ICFES
 * ===================================
 * Datos y estructuras exclusivas para la experiencia del simulacro completo
 */

// ✅ ESTRUCTURA COMPLETA DEL SIMULACRO ICFES
data class ICFESSimulation(
    val id: String,
    val sessions: List<SimulationSession>,
    val totalDuration: Long, // 4.5 horas en milisegundos
    val breakDuration: Long = 15 * 60 * 1000L, // 15 minutos
    val instructions: SimulationInstructions,
    val startTime: Long = 0L,
    val currentSessionIndex: Int = 0,
    val isCompleted: Boolean = false,
    val isPaused: Boolean = false
)

// ✅ SESIÓN INDIVIDUAL DEL SIMULACRO (cada módulo)
data class SimulationSession(
    val sessionNumber: Int,
    val moduleId: String,
    val moduleName: String,
    val description: String,
    val questions: List<ICFESQuestion>,
    val duration: Long, // duración en milisegundos
    val color: Long, // Color del módulo en formato Long
    val icon: String, // emoji o descripción del ícono
    val isCompleted: Boolean = false,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val answers: Map<Int, String> = emptyMap(), // índice pregunta -> respuesta
    val timeSpentPerQuestion: Map<Int, Long> = emptyMap() // tiempo por pregunta
)

// ✅ INSTRUCCIONES DEL SIMULACRO
data class SimulationInstructions(
    val generalInstructions: List<String>,
    val timingInstructions: List<String>,
    val technicalInstructions: List<String>,
    val behaviorInstructions: List<String>
) {
    companion object {
        fun getDefaultInstructions() = SimulationInstructions(
            generalInstructions = listOf(
                "Este simulacro replica exactamente la estructura del examen ICFES Saber 11",
                "Consta de 5 sesiones con 35 preguntas cada una",
                "Tendrás breaks de 15 minutos entre cada sesión",
                "El tiempo total es de 4 horas y 30 minutos",
                "No podrás regresar a sesiones anteriores una vez completadas"
            ),
            timingInstructions = listOf(
                "Cada sesión tiene un tiempo límite específico",
                "Lectura Crítica: 65 minutos",
                "Matemáticas: 65 minutos",
                "Ciencias Naturales: 65 minutos",
                "Sociales y Ciudadanas: 65 minutos",
                "Inglés: 60 minutos"
            ),
            technicalInstructions = listOf(
                "Asegúrate de tener conexión estable a internet",
                "Mantén tu dispositivo cargado o conectado",
                "No uses otras aplicaciones durante el simulacro",
                "Si hay problemas técnicos, contacta soporte inmediatamente"
            ),
            behaviorInstructions = listOf(
                "Busca un lugar silencioso y sin distracciones",
                "Ten agua y snacks preparados para los breaks",
                "No consultes material de estudio durante el simulacro",
                "Simula las condiciones reales del examen ICFES"
            )
        )
    }
}

// ✅ ESTADO ACTUAL DEL SIMULACRO
data class SimulationState(
    val currentSession: Int = 0,
    val currentQuestion: Int = 0,
    val timeRemaining: Long = 0L,
    val isInBreak: Boolean = false,
    val breakTimeRemaining: Long = 0L,
    val phase: SimulationPhase = SimulationPhase.INSTRUCTIONS,
    val answers: MutableMap<String, MutableMap<Int, SimulationAnswer>> = mutableMapOf(),
    val sessionStartTimes: MutableMap<Int, Long> = mutableMapOf(),
    val questionStartTime: Long = 0L
)

// ✅ FASES DEL SIMULACRO
enum class SimulationPhase {
    INSTRUCTIONS,    // Mostrando instrucciones
    SESSION,         // En una sesión activa
    BREAK,          // En break entre sesiones
    REVIEW,         // Revisando respuestas antes de continuar
    COMPLETED,      // Simulacro completado
    PAUSED          // Pausado (solo en breaks)
}

// ✅ RESPUESTA EN EL SIMULACRO
data class SimulationAnswer(
    val questionId: String,
    val questionIndex: Int,
    val sessionId: String,
    val userAnswer: String,
    val timeSpent: Long,
    val timestamp: Long,
    val wasChanged: Boolean = false, // Si cambió la respuesta
    val changeCount: Int = 0 // Cuántas veces cambió
)

// ✅ RESULTADO FINAL DEL SIMULACRO
data class ICFESSimulationCompleteResult(
    val simulationId: String,
    val completedAt: Long,
    val totalTimeSpent: Long,
    val sessionResults: List<SimulationSessionResult>,
    val globalScore: Int, // Puntaje global ICFES (0-500)
    val globalPercentage: Float,
    val globalLevel: String, // Superior, Alto, Medio, Bajo
    val nationalPercentile: Int, // Percentil estimado
    val comparison: ICFESNationalComparison,
    val detailedAnalysis: SimulationAnalysis,
    val recommendations: List<String>,
    val certificateEligible: Boolean // Si puede generar certificado
)

// ✅ RESULTADO POR SESIÓN
data class SimulationSessionResult(
    val sessionNumber: Int,
    val moduleId: String,
    val moduleName: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val percentage: Float,
    val icfesScore: Int, // Puntaje ICFES para este módulo
    val timeSpent: Long,
    val timeRemaining: Long,
    val level: String,
    val competencyBreakdown: Map<String, CompetencyResult>,
    val difficultyBreakdown: Map<Difficulty, DifficultyResult>,
    val efficiency: Float, // preguntas correctas por minuto
    val questionAnalysis: List<QuestionAnalysis>
)

// ✅ ANÁLISIS POR COMPETENCIA
data class CompetencyResult(
    val competencyName: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val percentage: Float,
    val averageTime: Long,
    val performance: String // Excelente, Bueno, Regular, Necesita mejora
)

// ✅ ANÁLISIS POR DIFICULTAD
data class DifficultyResult(
    val difficulty: Difficulty,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val percentage: Float,
    val averageTime: Long
)

// ✅ ANÁLISIS INDIVIDUAL DE PREGUNTA
data class QuestionAnalysis(
    val questionId: String,
    val questionNumber: Int,
    val isCorrect: Boolean,
    val userAnswer: String,
    val correctAnswer: String,
    val timeSpent: Long,
    val difficulty: Difficulty,
    val competency: String,
    val topic: String,
    val wasChanged: Boolean,
    val nationalCorrectRate: Float // % de estudiantes que la responden bien
)

// ✅ COMPARACIÓN NACIONAL
data class ICFESNationalComparison(
    val nationalAverage: Int,
    val percentilePosition: Int,
    val studentsAbove: Int, // Estimado de estudiantes por encima
    val studentsBelow: Int, // Estimado de estudiantes por debajo
    val regionAverage: Int, // Promedio regional (Santander)
    val institutionTypeAverage: Int, // Promedio por tipo de institución
    val comparison: String // "Superior al promedio nacional", etc.
)

// ✅ ANÁLISIS DETALLADO DEL SIMULACRO
data class SimulationAnalysis(
    val strengths: List<String>,
    val weaknesses: List<String>,
    val improvementAreas: List<String>,
    val strategicRecommendations: List<String>,
    val studyPlan: List<StudyRecommendation>,
    val timeManagement: TimeManagementAnalysis,
    val motivationalMessage: String
)

// ✅ RECOMENDACIÓN DE ESTUDIO
data class StudyRecommendation(
    val moduleId: String,
    val moduleName: String,
    val priority: StudyPriority,
    val suggestedHours: Int,
    val specificTopics: List<String>,
    val resources: List<String>,
    val practiceType: String // "Práctica básica", "Evaluación cronometrada", etc.
)

// ✅ PRIORIDAD DE ESTUDIO
enum class StudyPriority(val displayName: String, val color: Long) {
    URGENT("Urgente", 0xFFF44336),
    HIGH("Alta", 0xFFFF9800),
    MEDIUM("Media", 0xFF2196F3),
    LOW("Baja", 0xFF4CAF50)
}

// ✅ ANÁLISIS DE MANEJO DEL TIEMPO
data class TimeManagementAnalysis(
    val totalTimeUsed: Long,
    val totalTimeAvailable: Long,
    val efficiency: Float, // 0.0 - 1.0
    val sessionTimeBreakdown: Map<String, Long>,
    val questionsPetMinute: Float,
    val timeManagementLevel: String, // Excelente, Bueno, Regular, Necesita mejora
    val recommendations: List<String>
)

// ✅ PROGRESO DEL SIMULACRO (para guardar y reanudar)
data class SimulationProgress(
    val simulationId: String,
    val currentState: SimulationState,
    val simulation: ICFESSimulation,
    val lastSaved: Long,
    val canResume: Boolean,
    val remainingTime: Long
)

// ✅ CONFIGURACIÓN DEL SIMULACRO
data class SimulationConfig(
    val allowPause: Boolean = false, // Solo en breaks
    val showTimer: Boolean = true,
    val playAlerts: Boolean = true,
    val autoSubmit: Boolean = true, // Auto-enviar al acabar tiempo
    val shuffleQuestions: Boolean = true,
    val showProgress: Boolean = true,
    val allowReview: Boolean = false, // Revisión antes de finalizar sesión
    val strictMode: Boolean = true // Modo estricto como ICFES real
)

// ✅ ESTADÍSTICAS HISTÓRICAS DE SIMULACROS
data class SimulationHistory(
    val totalSimulations: Int,
    val averageScore: Float,
    val bestScore: Int,
    val lastScore: Int,
    val improvement: Float, // Mejora desde el primer simulacro
    val completionRate: Float, // % de simulacros terminados
    val averageTimePerSession: Map<String, Long>,
    val strengthsEvolution: List<String>, // Competencias que han mejorado
    val consistentWeaknesses: List<String> // Competencias que siguen débiles
)

// ✅ EVENTO DEL SIMULACRO (para analytics)
data class SimulationEvent(
    val eventId: String,
    val simulationId: String,
    val timestamp: Long,
    val eventType: SimulationEventType,
    val sessionNumber: Int?,
    val questionNumber: Int?,
    val data: Map<String, Any> = emptyMap()
)

// ✅ TIPOS DE EVENTOS
enum class SimulationEventType {
    SIMULATION_STARTED,
    SESSION_STARTED,
    SESSION_COMPLETED,
    BREAK_STARTED,
    BREAK_ENDED,
    QUESTION_ANSWERED,
    QUESTION_CHANGED,
    TIME_WARNING, // Cuando quedan 5 minutos
    AUTO_SUBMIT,
    SIMULATION_COMPLETED,
    SIMULATION_PAUSED,
    SIMULATION_RESUMED
}