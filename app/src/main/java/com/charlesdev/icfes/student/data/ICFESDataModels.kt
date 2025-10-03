package com.charlesdev.icfes.student.data



import androidx.compose.ui.graphics.Color

import com.charlesdev.icfes.R



// ✅ MODELO BASE - Adaptado para tu estructura actual

data class ICFESModule(

    val id: String,

    val name: String,

    val description: String,

    val icon: Int,

    val color: Color,

    val totalQuestions: Int,

    val timeLimit: Int, // en minutos

    val areas: List<ICFESArea>,

    val minimumScore: Int = 0, // Puntaje mínimo para aprobar

    val nationalAverage: Int = 250 // Promedio nacional

)



data class ICFESArea(

    val id: String,

    val name: String,

    val description: String,

    val competencies: List<String>,

    val questions: List<ICFESQuestion>,

    val weight: Double = 1.0, // Peso en el puntaje final

    val difficultyDistribution: DifficultyDistribution = DifficultyDistribution()

)



data class DifficultyDistribution(

    val easy: Float = 0.3f,    // 30% fácil

    val medium: Float = 0.5f,  // 50% medio

    val hard: Float = 0.2f     // 20% difícil

)



// ✅ PREGUNTA ICFES - Extendiendo tu Question actual

data class ICFESQuestion(

    val id: String,

    val type: ICFESQuestionType,

    val question: String,

    val context: String? = null, // Texto base para lectura crítica

    val image: String? = null, // URL o resource para gráficos

    val options: List<String>,

    val correctAnswer: String,

    val competency: String,

    val difficulty: Difficulty,

    val explanation: String,

    val timeEstimated: Int, // segundos estimados

    val feedback: ICFESFeedback,

    val tags: List<String> = emptyList(), // Para filtros y búsqueda

    val source: String = "Milton Ochoa" // Fuente del contenido

)



// ✅ TIPOS DE PREGUNTAS ICFES

enum class ICFESQuestionType {

    MULTIPLE_CHOICE,           // Selección múltiple básica

    READING_COMPREHENSION,     // Comprensión lectora (con texto base)

    MATHEMATICAL_PROBLEM,      // Problema matemático

    SCIENTIFIC_ANALYSIS,       // Análisis científico

    SOCIAL_CONTEXT,           // Contexto social/histórico

    ENGLISH_READING,          // Lectura en inglés

    CRITICAL_THINKING         // Pensamiento crítico

}



// ✅ NIVELES DE DIFICULTAD

enum class Difficulty(val displayName: String, val points: Int) {

    FACIL("Fácil", 1),

    MEDIO("Medio", 2),

    DIFICIL("Difícil", 3)

}



// ✅ FEEDBACK ICFES - Adaptado de tu Feedback actual

data class ICFESFeedback(

    val correct: String,

    val incorrect: Map<String, String> = emptyMap(),

    val incorrectGeneral: String = "",

    val tip: String,

    val relatedTopics: List<String> = emptyList(),

    val studyRecommendations: List<String> = emptyList()

)



// ✅ SISTEMA DE PUNTAJE ICFES

data class ICFESScore(

    val moduleId: String,

    val globalScore: Int, // 0-500

    val rawScore: Int, // Respuestas correctas

    val totalQuestions: Int,

    val percentage: Float,

    val percentile: Int, // Percentil nacional

    val areaScores: Map<String, AreaScore>,

    val timeSpent: Long, // milisegundos

    val efficiency: Number, // Puntos por minuto

    val strengths: List<String>,

    val weaknesses: List<String>,

    val recommendations: List<String>

)



data class AreaScore(

    val areaId: String,

    val score: Int,

    val total: Int,

    val percentage: Float,

    val timeSpent: Long,

    val difficulty: Map<Difficulty, Int> // Correctas por dificultad

)



// ✅ PERFIL DE ESTUDIANTE - Para gamificación (compatible con tu sistema actual)

data class StudentProfile(

    val id: String,

    val name: String,

    val email: String,

    val institution: String = "",

    val grade: String = "", // 11°, Universitario, etc.

    val targetScore: Int = 300,

    val currentLevel: Int = 1,

    val experience: Int = 0,

    val achievements: List<Achievement> = emptyList(),

    val studyStreak: Int = 0,

    val totalStudyTime: Long = 0,

    val createdAt: Long = System.currentTimeMillis(),

    val lastActivity: Long = System.currentTimeMillis()

)



// ✅ SISTEMA DE LOGROS

data class Achievement(

    val id: String,

    val name: String,

    val description: String,

    val icon: Int,

    val category: AchievementCategory,

    val requirement: AchievementRequirement,

    val reward: AchievementReward,

    val isUnlocked: Boolean = false,

    val progress: Float = 0f,

    val unlockedAt: Long? = null

)



enum class AchievementCategory {

    FIRST_STEPS,    // Primeros pasos

    MASTERY,        // Dominio de temas

    CONSISTENCY,    // Consistencia

    SPEED,          // Velocidad

    SOCIAL,         // Aspectos sociales

    SPECIAL         // Logros especiales

}



data class AchievementRequirement(

    val type: RequirementType,

    val value: Int,

    val moduleId: String? = null

)


// ✅ DATA CLASSES PARA SIMULACRO COMPLETO
data class ICFESSimulationResult(
    val totalQuestions: Int,
    val correctAnswers: Int,
    val percentage: Float,
    val globalScore: Int,
    val moduleScores: Map<String, ModuleSimulationScore>,
    val timeSpent: Long,
    val nivel: String,
    val completedAt: Long
)

data class ModuleSimulationScore(
    val moduleId: String,
    val moduleName: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val percentage: Float,
    val puntajeICFES: Int
)



enum class RequirementType {

    COMPLETE_QUIZZES,      // Completar X quizzes

    SCORE_PERCENTAGE,      // Obtener X% en un módulo

    STUDY_STREAK,          // Estudiar X días consecutivos

    TOTAL_STUDY_TIME,      // Estudiar X horas total

    SPEED_RECORD,          // Responder X preguntas en Y tiempo

    PERFECT_SCORE          // Obtener puntaje perfecto

}



data class AchievementReward(

    val experience: Int,

    val badge: String,

    val specialContent: String? = null

)



// ✅ ESTADÍSTICAS DE SESIÓN

data class StudySession(

    val id: String,

    val studentId: String,

    val moduleId: String,

    val startTime: Long,

    val endTime: Long,

    val questionsAnswered: Int,

    val correctAnswers: Int,

    val score: ICFESScore,

    val sessionType: SessionType,

    val completed: Boolean = false

)



enum class SessionType {

    PRACTICE,       // Práctica libre

    TIMED_QUIZ,     // Quiz con tiempo

    SIMULATION,     // Simulacro completo

    REVIEW,         // Repaso de errores

    CHALLENGE       // Desafío/competencia

}



// ✅ CONFIGURACIÓN DE MÓDULOS ICFES

val icfesModules = listOf(

    ICFESModule(

        id = "lectura_critica",

        name = "Lectura Crítica",

        description = "Comprensión, interpretación y evaluación de textos continuos y discontinuos",

        icon = R.drawable.ic_book, // Necesitarás agregar estos iconos

        color = Color(0xFF2196F3), // Azul

        totalQuestions = 35,

        timeLimit = 65,

        nationalAverage = 252,

        areas = listOf(

            ICFESArea(

                id = "comprension_literal",

                name = "Comprensión Literal",

                description = "Identificar información explícita en el texto",

                competencies = listOf(

                    "Identificar información local",

                    "Reconocer estructura semántica",

                    "Identificar propósito comunicativo"

                ),

                questions = emptyList(), // Se llenarán después

                weight = 1.0

            ),

            ICFESArea(

                id = "comprension_inferencial",

                name = "Comprensión Inferencial",

                description = "Realizar inferencias y deducciones",

                competencies = listOf(

                    "Inferir información implícita",

                    "Establecer relaciones intertextuales",

                    "Reconocer estrategias discursivas"

                ),

                questions = emptyList(),

                weight = 1.0

            ),

            ICFESArea(

                id = "pensamiento_critico",

                name = "Pensamiento Crítico",

                description = "Reflexionar y evaluar el contenido del texto",

                competencies = listOf(

                    "Evaluar validez de argumentos",

                    "Asumir posición crítica",

                    "Proponer interpretaciones alternativas"

                ),

                questions = emptyList(),

                weight = 1.0

            )

        )

    ),



    ICFESModule(

        id = "matematicas",

        name = "Matemáticas",

        description = "Razonamiento cuantitativo y resolución de problemas matemáticos",

        icon = R.drawable.ic_calculator, // Necesitarás agregar estos iconos

        color = Color(0xFFFF9800), // Naranja

        totalQuestions = 35,

        timeLimit = 65,

        nationalAverage = 248,

        areas = listOf(

            ICFESArea(

                id = "algebra_funciones",

                name = "Álgebra y Funciones",

                description = "Manejo algebraico y análisis de funciones",

                competencies = listOf(

                    "Resolver ecuaciones y sistemas",

                    "Analizar funciones y gráficas",

                    "Modelar situaciones algebraicamente"

                ),

                questions = emptyList(),

                weight = 1.0

            ),

            ICFESArea(

                id = "geometria_medicion",

                name = "Geometría y Medición",

                description = "Conceptos geométricos y medición",

                competencies = listOf(

                    "Calcular áreas y volúmenes",

                    "Aplicar teoremas geométricos",

                    "Resolver problemas métricos"

                ),

                questions = emptyList(),

                weight = 1.0

            ),

            ICFESArea(

                id = "estadistica_probabilidad",

                name = "Estadística y Probabilidad",

                description = "Análisis de datos y probabilidad",

                competencies = listOf(

                    "Interpretar datos estadísticos",

                    "Calcular probabilidades",

                    "Analizar tendencias"

                ),

                questions = emptyList(),

                weight = 1.0

            )

        )

    ),



    ICFESModule(

        id = "ciencias_naturales",

        name = "Ciencias Naturales",

        description = "Uso comprensivo del conocimiento científico",

        icon = R.drawable.ic_science, // Necesitarás agregar estos iconos

        color = Color(0xFF4CAF50), // Verde

        totalQuestions = 35,

        timeLimit = 65,

        nationalAverage = 245,

        areas = listOf(

            ICFESArea(

                id = "biologia",

                name = "Biología",

                description = "Sistemas biológicos y procesos vitales",

                competencies = listOf(

                    "Analizar procesos biológicos",

                    "Interpretar fenómenos naturales",

                    "Evaluar impacto ambiental"

                ),

                questions = emptyList(),

                weight = 1.0

            ),

            ICFESArea(

                id = "fisica",

                name = "Física",

                description = "Fenómenos físicos y principios fundamentales",

                competencies = listOf(

                    "Aplicar principios físicos",

                    "Interpretar gráficas y datos",

                    "Resolver problemas cuantitativos"

                ),

                questions = emptyList(),

                weight = 1.0

            ),

            ICFESArea(

                id = "quimica",

                name = "Química",

                description = "Transformaciones químicas y propiedades",

                competencies = listOf(

                    "Explicar transformaciones químicas",

                    "Balancear ecuaciones",

                    "Interpretar estructura molecular"

                ),

                questions = emptyList(),

                weight = 1.0

            )

        )

    ),



    ICFESModule(

        id = "sociales_ciudadanas",

        name = "Sociales y Ciudadanas",

        description = "Pensamiento social y competencias ciudadanas",

        icon = R.drawable.ic_people, // Necesitarás agregar estos iconos

        color = Color(0xFF9C27B0), // Morado

        totalQuestions = 35,

        timeLimit = 65,

        nationalAverage = 251,

        areas = listOf(

            ICFESArea(

                id = "historia_cultura",

                name = "Historia y Cultura",

                description = "Procesos históricos y manifestaciones culturales",

                competencies = listOf(

                    "Analizar procesos históricos",

                    "Interpretar fuentes históricas",

                    "Evaluar cambios y continuidades"

                ),

                questions = emptyList(),

                weight = 1.0

            ),

            ICFESArea(

                id = "geografia_ambiente",

                name = "Geografía y Ambiente",

                description = "Espacio geográfico y problemáticas ambientales",

                competencies = listOf(

                    "Interpretar mapas y gráficos",

                    "Analizar problemáticas territoriales",

                    "Evaluar impacto ambiental"

                ),

                questions = emptyList(),

                weight = 1.0

            ),

            ICFESArea(

                id = "constitucion_democracia",

                name = "Constitución y Democracia",

                description = "Principios constitucionales y participación democrática",

                competencies = listOf(

                    "Conocer derechos y deberes",

                    "Analizar instituciones democráticas",

                    "Evaluar participación ciudadana"

                ),

                questions = emptyList(),

                weight = 1.0

            )

        )

    ),



    ICFESModule(

        id = "ingles",

        name = "Inglés",

        description = "Comunicación en lengua inglesa",

        icon = R.drawable.ic_language, // Necesitarás agregar estos iconos

        color = Color(0xFFF44336), // Rojo

        totalQuestions = 35,

        timeLimit = 60,

        nationalAverage = 249,

        areas = listOf(

            ICFESArea(

                id = "reading_comprehension",

                name = "Reading Comprehension",

                description = "Comprensión de textos en inglés",

                competencies = listOf(

                    "Understand main ideas",

                    "Identify specific information",

                    "Infer meaning from context"

                ),

                questions = emptyList(),

                weight = 1.0

            ),

            ICFESArea(

                id = "use_of_english",

                name = "Use of English",

                description = "Uso del inglés en contexto",

                competencies = listOf(

                    "Grammar and vocabulary",

                    "Pragmatic competence",

                    "Functional language"

                ),

                questions = emptyList(),

                weight = 1.0

            )

        )

    )

)



// ✅ LOGROS PREDEFINIDOS

val defaultAchievements = listOf(

    Achievement(

        id = "first_quiz",

        name = "Primer Paso",

        description = "Completa tu primera evaluación",

        icon = R.drawable.ic_quiz, // Necesitarás agregar estos iconos

        category = AchievementCategory.FIRST_STEPS,

        requirement = AchievementRequirement(RequirementType.COMPLETE_QUIZZES, 1),

        reward = AchievementReward(experience = 50, badge = "🎯")

    ),

    Achievement(

        id = "reading_master",

        name = "Maestro de Lectura",

        description = "Obtén 85% o más en Lectura Crítica",

        icon = R.drawable.ic_book,

        category = AchievementCategory.MASTERY,

        requirement = AchievementRequirement(RequirementType.SCORE_PERCENTAGE, 85, "lectura_critica"),

        reward = AchievementReward(experience = 200, badge = "📚")

    ),

    Achievement(

        id = "math_genius",

        name = "Genio Matemático",

        description = "Obtén 90% o más en Matemáticas",

        icon = R.drawable.ic_calculator,

        category = AchievementCategory.MASTERY,

        requirement = AchievementRequirement(RequirementType.SCORE_PERCENTAGE, 90, "matematicas"),

        reward = AchievementReward(experience = 250, badge = "🔢")

    ),

    Achievement(

        id = "consistent_learner",

        name = "Estudiante Consistente",

        description = "Estudia 7 días consecutivos",

        icon = R.drawable.ic_calendar,

        category = AchievementCategory.CONSISTENCY,

        requirement = AchievementRequirement(RequirementType.STUDY_STREAK, 7),

        reward = AchievementReward(experience = 300, badge = "🔥")

    ),

    Achievement(

        id = "speed_demon",

        name = "Demonio de Velocidad",

        description = "Responde 20 preguntas en menos de 10 minutos",

        icon = R.drawable.ic_timer,

        category = AchievementCategory.SPEED,

        requirement = AchievementRequirement(RequirementType.SPEED_RECORD, 20),

        reward = AchievementReward(experience = 150, badge = "⚡")

    ),

    Achievement(

        id = "perfect_score",

        name = "Puntaje Perfecto",

        description = "Obtén 100% en cualquier módulo",

        icon = R.drawable.ic_star,

        category = AchievementCategory.SPECIAL,

        requirement = AchievementRequirement(RequirementType.PERFECT_SCORE, 100),

        reward = AchievementReward(experience = 500, badge = "🌟", specialContent = "Acceso a contenido premium")

    )

)