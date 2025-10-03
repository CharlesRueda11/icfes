package com.charlesdev.icfes.student.duelo

import java.util.*

enum class Difficulty { EASY, MEDIUM, HARD }

enum class DuelTopic {
    // MATEMÁTICAS
    ALGEBRA, GEOMETRIA, ESTADISTICA, CALCULO, TRIGONOMETRIA, MATEMATICAS_GENERAL,

    // CIENCIAS NATURALES - FÍSICA
    MECANICA, TERMODINAMICA, ELECTRICIDAD, ONDAS, FISICA_MODERNA, FISICA_GENERAL,

    // CIENCIAS NATURALES - QUÍMICA
    ATOMICA, ENLACE_QUIMICO, ESTEQUIOMETRIA, SOLUCIONES, ORGANICA, QUIMICA_GENERAL,

    // CIENCIAS NATURALES - BIOLOGÍA
    CELULAR, GENETICA, EVOLUCION, ECOLOGIA, ANATOMIA, BIOLOGIA_GENERAL,

    // CIENCIAS SOCIALES
    HISTORIA_COLOMBIA, HISTORIA_UNIVERSAL, GEOGRAFIA, CONSTITUCION, ECONOMIA,
    FILOSOFIA, CIVICA, SOCIALES_GENERAL,

    // LECTURA CRÍTICA
    COMPRENSION_LECTORA, ARGUMENTACION, INTERPRETACION, INFERENCIA,
    PROPOSITO_COMUNICATIVO, LECTURA_GENERAL,

    // INGLÉS
    GRAMMAR, VOCABULARY, READING_COMPREHENSION, LISTENING, WRITING, INGLES_GENERAL,

    // GENERAL
    OTROS
}

// ✅ NUEVO: Mismo sistema que Resistencia de Materiales
data class Question(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctAnswer: Char = 'A', // ✅ A, B, C, o D
    val difficulty: Difficulty = Difficulty.EASY,
    val hint: String = "",
    val formula: String = "",
    val topic: DuelTopic = DuelTopic.OTROS,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
) {
    constructor() : this("", "", "", "", "", "", 'A', Difficulty.EASY, "", "", DuelTopic.OTROS, "", 0, 0, true)

    // ✅ Función para obtener todas las opciones como lista (compatibilidad)
    fun getOptions(): List<String> = listOf(optionA, optionB, optionC, optionD)

    // ✅ Función para obtener el índice numérico de la respuesta correcta (compatibilidad)
    fun getCorrectIndex(): Int = when(correctAnswer.uppercaseChar()) {
        'A' -> 0
        'B' -> 1
        'C' -> 2
        'D' -> 3
        else -> 0 // Fallback a A si hay error
    }

    // ✅ Función para obtener la respuesta correcta como texto
    fun getCorrectAnswerText(): String = when(correctAnswer.uppercaseChar()) {
        'A' -> optionA
        'B' -> optionB
        'C' -> optionC
        'D' -> optionD
        else -> optionA
    }

    // ✅ Función para validar si una letra seleccionada es correcta
    fun isCorrectAnswer(selectedLetter: Char): Boolean {
        return selectedLetter.uppercaseChar() == correctAnswer.uppercaseChar()
    }

    // ✅ Función para validar si un índice seleccionado es correcto (compatibilidad)
    fun isCorrectAnswer(selectedIndex: Int): Boolean {
        return selectedIndex == getCorrectIndex()
    }

    // ✅ Función para validar que la pregunta esté bien formada
    fun isValid(): Boolean {
        return text.isNotBlank() &&
                optionA.isNotBlank() &&
                optionB.isNotBlank() &&
                optionC.isNotBlank() &&
                optionD.isNotBlank() &&
                correctAnswer.uppercaseChar() in listOf('A', 'B', 'C', 'D')
    }

    // ✅ Función para obtener información de debugging
    fun getDebugInfo(): String {
        return """
            Pregunta: $text
            A) $optionA
            B) $optionB  
            C) $optionC
            D) $optionD
            Respuesta correcta: $correctAnswer) ${getCorrectAnswerText()}
            Válida: ${isValid()}
        """.trimIndent()
    }

    // ✅ NUEVO: Conversión desde formato antiguo (options + correctIndex)
    companion object {
        fun fromLegacyFormat(
            text: String,
            options: List<String>,
            correctIndex: Int,
            difficulty: Difficulty = Difficulty.EASY,
            hint: String = "",
            formula: String = "",
            topic: DuelTopic = DuelTopic.OTROS
        ): Question {
            require(options.size == 4) { "Debe haber exactamente 4 opciones" }
            require(correctIndex in 0..3) { "correctIndex debe estar entre 0 y 3" }

            return Question(
                text = text,
                optionA = options[0],
                optionB = options[1],
                optionC = options[2],
                optionD = options[3],
                correctAnswer = when(correctIndex) {
                    0 -> 'A'
                    1 -> 'B'
                    2 -> 'C'
                    3 -> 'D'
                    else -> 'A'
                },
                difficulty = difficulty,
                hint = hint,
                formula = formula,
                topic = topic
            )
        }
    }
}

// ✅ MANTENER IGUAL: Respuesta individual de un jugador
data class PlayerAnswer(
    val questionIndex: Int = 0,
    val selectedOption: Int = -1,
    val isCorrect: Boolean = false,
    val timeWhenAnswered: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this(0, -1, false, 0, 0L)
}

// ✅ MANTENER IGUAL: Player con progreso individual
data class Player(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val currentQuestionIndex: Int = 0,
    val timeLeftOnCurrentQuestion: Int = 25,
    val individualAnswers: List<PlayerAnswer> = emptyList(),
    val individualScore: Int = 0,
    val hasFinished: Boolean = false,
    val finishedAt: Long = 0
) {
    constructor() : this("", "", "", "", 0, 25, emptyList(), 0, false, 0)

    fun getProgress(): Float = currentQuestionIndex / 20f
    fun getCorrectAnswers(): Int = individualAnswers.count { it.isCorrect }
    fun hasAnsweredCurrentQuestion(): Boolean =
        individualAnswers.any { it.questionIndex == currentQuestionIndex }

    fun getInitials(): String {
        return if (name.isNotBlank()) {
            name.split(" ")
                .take(2)
                .joinToString("") { it.take(1).uppercase() }
        } else {
            "?"
        }
    }

    fun hasProfileImage(): Boolean = profileImageUrl.isNotEmpty()
}

enum class TeamSide { A, B }

data class PowerUps(
    val fiftyFifty: Boolean = true,
    val hint: Boolean = true,
    val swap: Boolean = true,
    val extraTime: Boolean = true,
    val formula: Boolean = true,
    val reverse: Boolean = true,
    val used: Int = 0,
    val maxPerTeam: Int = 3
) {
    constructor() : this(true, true, true, true, true, true, 0, 3)
}

data class Team(
    val name: String = "",
    val players: List<Player> = emptyList(),
    val powerUps: PowerUps = PowerUps()
) {
    constructor() : this("", emptyList(), PowerUps())

    fun getTotalScore(): Int = players.sumOf { it.individualScore }
    fun getAverageScore(): Double = if (players.isNotEmpty()) getTotalScore().toDouble() / players.size else 0.0
    fun getFinishedPlayersCount(): Int = players.count { it.hasFinished }
    fun hasAllPlayersFinished(): Boolean = players.isNotEmpty() && players.all { it.hasFinished }
    fun getAverageProgress(): Float = if (players.isNotEmpty()) players.map { it.getProgress() }.average().toFloat() else 0f
}

sealed class PowerUpType {
    object FiftyFifty : PowerUpType()
    object Hint : PowerUpType()
    object Swap : PowerUpType()
    object ExtraTime : PowerUpType()
    object Formula : PowerUpType()
    object Reverse : PowerUpType()
}

data class Match(
    val code: String = "",
    val pin: String? = null,
    val hostId: String = "",
    val started: Boolean = false,
    val teamA: Team = Team(),
    val teamB: Team = Team(),
    val log: List<String> = emptyList(),
    val createdAt: Long = 0,
    val finished: Boolean = false,
    val winner: String = "",
    val finalScoreA: Int = 0,
    val finalScoreB: Int = 0,
    val finishedAt: Long = 0
) {
    constructor() : this("", null, "", false, Team(), Team(), emptyList(), 0, false, "", 0, 0, 0)

    fun getTotalPlayers(): Int = teamA.players.size + teamB.players.size
    fun getAllPlayers(): List<Player> = teamA.players + teamB.players
    fun getFinishedPlayers(): List<Player> = getAllPlayers().filter { it.hasFinished }
    fun hasAllPlayersFinished(): Boolean = getTotalPlayers() > 0 && getAllPlayers().all { it.hasFinished }

    fun findPlayer(playerId: String): Player? = getAllPlayers().find { it.id == playerId }
    fun getPlayerTeamSide(playerId: String): TeamSide? = when {
        teamA.players.any { it.id == playerId } -> TeamSide.A
        teamB.players.any { it.id == playerId } -> TeamSide.B
        else -> null
    }

    fun getOverallProgress(): Float {
        val allPlayers = getAllPlayers()
        return if (allPlayers.isNotEmpty()) {
            allPlayers.map { it.getProgress() }.average().toFloat()
        } else 0f
    }

    fun calculateWinner(): String {
        val scoreA = teamA.getTotalScore()
        val scoreB = teamB.getTotalScore()
        return when {
            scoreA > scoreB -> "A"
            scoreB > scoreA -> "B"
            else -> "EMPATE"
        }
    }
}