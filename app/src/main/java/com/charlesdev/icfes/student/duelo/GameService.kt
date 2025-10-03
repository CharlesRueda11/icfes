package com.charlesdev.icfes.student.duelo

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

interface GameService {
    suspend fun createMatch(teamName: String, pin: String?, host: Player): Match
    suspend fun joinMatch(code: String, pin: String?, player: Player, side: TeamSide)
    fun observe(code: String): StateFlow<Match?>
    suspend fun startGame(code: String)
    suspend fun submitAnswer(code: String, optionIndex: Int): Boolean // Compatibilidad
    suspend fun submitAnswerByLetter(code: String, selectedLetter: Char): Boolean // ✅ NUEVO método principal
    suspend fun forcePlayerNextQuestion(code: String, playerId: String)
}

class FirebaseGameService(private val scope: CoroutineScope) : GameService {
    private val database = FirebaseDatabase.getInstance()
    private val matchesRef = database.getReference("duel_matches")
    private val flows = mutableMapOf<String, MutableStateFlow<Match?>>()
    private val listeners = mutableMapOf<String, ValueEventListener>()

    private val questionsLoader = DynamicQuestionsLoader()

    companion object {
        private const val TAG = "FirebaseGameService"
        private const val TOTAL_QUESTIONS = 20
        private const val TIME_PER_QUESTION = 25
    }

    // ✅ MANTENER IGUAL: createMatch, joinMatch, observe, startGame
    override suspend fun createMatch(teamName: String, pin: String?, host: Player): Match {
        return try {
            val code = generateCode()
            questionsLoader.clearCache()

            val hostPlayer = Player(
                id = host.id,
                name = host.name,
                email = host.email,
                profileImageUrl = host.profileImageUrl,
                currentQuestionIndex = 0,
                timeLeftOnCurrentQuestion = TIME_PER_QUESTION,
                individualAnswers = emptyList(),
                individualScore = 0,
                hasFinished = false,
                finishedAt = 0
            )

            val teamA = Team(
                name = teamName,
                players = listOf(hostPlayer),
                powerUps = PowerUps()
            )

            val teamB = Team(
                name = "Equipo B",
                players = emptyList(),
                powerUps = PowerUps()
            )

            val match = Match(
                code = code,
                pin = pin,
                hostId = host.id,
                started = false,
                teamA = teamA,
                teamB = teamB,
                log = emptyList(),
                createdAt = System.currentTimeMillis()
            )

            Log.d(TAG, "Creating match with code: $code")

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                throw IllegalStateException("Usuario no autenticado")
            }

            matchesRef.child(code).setValue(match).await()
            Log.d(TAG, "Match created successfully")
            match
        } catch (e: Exception) {
            Log.e(TAG, "Error creating match", e)
            throw e
        }
    }

    override suspend fun joinMatch(code: String, pin: String?, player: Player, side: TeamSide) {
        try {
            Log.d(TAG, "Joining match: $code as team $side")

            val matchRef = matchesRef.child(code)
            val snapshot = matchRef.get().await()

            if (!snapshot.exists()) {
                Log.e(TAG, "Match $code does not exist")
                throw IllegalStateException("Sala no existe")
            }

            val match = snapshot.getValue(Match::class.java)
                ?: throw IllegalStateException("Partida inválida")

            if (match.pin?.isNotBlank() == true && match.pin != pin) {
                Log.e(TAG, "PIN incorrect for match $code")
                throw IllegalArgumentException("PIN incorrecto")
            }

            val newPlayer = Player(
                id = player.id,
                name = player.name,
                email = player.email,
                profileImageUrl = player.profileImageUrl,
                currentQuestionIndex = 0,
                timeLeftOnCurrentQuestion = TIME_PER_QUESTION,
                individualAnswers = emptyList(),
                individualScore = 0,
                hasFinished = false,
                finishedAt = 0
            )

            val updates = mutableMapOf<String, Any>()
            val logMessage = "${player.name} se unió al equipo $side"
            val newLog = match.log.toMutableList().apply {
                add(logMessage)
                if (size > 80) removeFirst()
            }

            when (side) {
                TeamSide.A -> {
                    if (match.teamA.players.none { it.id == player.id }) {
                        val playersList = match.teamA.players.toMutableList()
                        playersList.add(newPlayer)
                        updates["teamA/players"] = playersList
                    }
                }
                TeamSide.B -> {
                    if (match.teamB.players.none { it.id == player.id }) {
                        val playersList = match.teamB.players.toMutableList()
                        playersList.add(newPlayer)
                        updates["teamB/players"] = playersList
                    }
                }
            }

            updates["log"] = newLog
            matchRef.updateChildren(updates).await()
            Log.d(TAG, "Successfully joined match $code")
        } catch (e: Exception) {
            Log.e(TAG, "Error joining match $code", e)
            throw e
        }
    }

    override fun observe(code: String): StateFlow<Match?> {
        return flows.getOrPut(code) {
            val flow = MutableStateFlow<Match?>(null)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val match = snapshot.getValue(Match::class.java)
                        Log.d(TAG, "Match updated: $code, started: ${match?.started}")
                        flow.value = match
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deserializing match $code", e)
                        flow.value = null
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Database error observing match $code: ${error.message}")
                }
            }
            listeners[code] = listener
            matchesRef.child(code).addValueEventListener(listener)
            flow
        }
    }

    override suspend fun startGame(code: String) {
        try {
            Log.d(TAG, "Starting game: $code")

            val questionsResult = questionsLoader.loadQuestionsForMatch()
            if (questionsResult.isFailure) {
                Log.w(TAG, "Failed to load questions, using local questions")
            }

            val snapshot = matchesRef.child(code).get().await()
            val match = snapshot.getValue(Match::class.java) ?: return

            val updatedTeamA = match.teamA.copy(
                players = match.teamA.players.map { player ->
                    player.copy(
                        currentQuestionIndex = 0,
                        timeLeftOnCurrentQuestion = TIME_PER_QUESTION,
                        individualAnswers = emptyList(),
                        individualScore = 0,
                        hasFinished = false
                    )
                }
            )

            val updatedTeamB = match.teamB.copy(
                players = match.teamB.players.map { player ->
                    player.copy(
                        currentQuestionIndex = 0,
                        timeLeftOnCurrentQuestion = TIME_PER_QUESTION,
                        individualAnswers = emptyList(),
                        individualScore = 0,
                        hasFinished = false
                    )
                }
            )

            val newLog = match.log.toMutableList().apply {
                add("🎯 DUELO ICFES INICIADO - Todos comienzan con pregunta 1/20")
                add("⏰ Cada jugador tiene su timer individual de 25s por pregunta")
                if (size > 80) removeFirst()
            }

            val updates = mapOf(
                "started" to true,
                "teamA" to updatedTeamA,
                "teamB" to updatedTeamB,
                "log" to newLog
            )

            matchesRef.child(code).updateChildren(updates).await()
            Log.d(TAG, "Game started successfully: $code")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting game $code", e)
            throw e
        }
    }

    // ✅ NUEVO MÉTODO PRINCIPAL: submitAnswerByLetter
    override suspend fun submitAnswerByLetter(code: String, selectedLetter: Char): Boolean {
        return try {
            Log.d(TAG, "=== SUBMIT ANSWER BY LETTER ===")
            Log.d(TAG, "Match: $code, Selected letter: $selectedLetter")

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Log.e(TAG, "User not authenticated")
                return false
            }
            val playerId = currentUser.uid

            val snapshot = matchesRef.child(code).get().await()
            val match = snapshot.getValue(Match::class.java) ?: return false

            val player = match.findPlayer(playerId) ?: return false
            val teamSide = match.getPlayerTeamSide(playerId) ?: return false

            if (player.hasFinished || player.hasAnsweredCurrentQuestion()) {
                Log.d(TAG, "Player $playerId has already finished or answered current question")
                return false
            }

            val currentQuestionIndex = player.currentQuestionIndex

            // ✅ OBTENER LA PREGUNTA DEL NUEVO SISTEMA
            val question = getQuestionByIndex(currentQuestionIndex)
            if (question == null) {
                Log.e(TAG, "No question found for index $currentQuestionIndex")
                return false
            }

            Log.d(TAG, "=== QUESTION VALIDATION ===")
            Log.d(TAG, "Question: ${question.text}")
            Log.d(TAG, "A) ${question.optionA}")
            Log.d(TAG, "B) ${question.optionB}")
            Log.d(TAG, "C) ${question.optionC}")
            Log.d(TAG, "D) ${question.optionD}")
            Log.d(TAG, "Correct answer: ${question.correctAnswer}) ${question.getCorrectAnswerText()}")
            Log.d(TAG, "Player selected: $selectedLetter")

            // ✅ VALIDAR QUE LA PREGUNTA ESTÉ BIEN FORMADA
            if (!question.isValid()) {
                Log.e(TAG, "❌ Invalid question structure!")
                Log.e(TAG, question.getDebugInfo())
                return false
            }

            // ✅ VALIDAR QUE LA LETRA SELECCIONADA SEA VÁLIDA
            val normalizedLetter = selectedLetter.uppercaseChar()
            if (normalizedLetter !in listOf('A', 'B', 'C', 'D')) {
                Log.e(TAG, "❌ Invalid letter selected: $selectedLetter")
                return false
            }

            // ✅ DETERMINAR SI LA RESPUESTA ES CORRECTA
            val isCorrect = question.isCorrectAnswer(normalizedLetter)
            val pointsEarned = if (isCorrect) 10 else 0

            Log.d(TAG, "=== ANSWER RESULT ===")
            Log.d(TAG, "Selected: $normalizedLetter) ${getSelectedAnswerText(question, normalizedLetter)}")
            Log.d(TAG, "Expected: ${question.correctAnswer}) ${question.getCorrectAnswerText()}")
            Log.d(TAG, "Is correct: $isCorrect")
            Log.d(TAG, "Points earned: $pointsEarned")

            // ✅ CREAR RESPUESTA DEL JUGADOR
            val playerAnswer = PlayerAnswer(
                questionIndex = currentQuestionIndex,
                selectedOption = question.getCorrectIndex(), // Para compatibilidad
                isCorrect = isCorrect,
                timeWhenAnswered = player.timeLeftOnCurrentQuestion,
                timestamp = System.currentTimeMillis()
            )

            // ✅ ACTUALIZAR JUGADOR
            val newAnswers = player.individualAnswers + playerAnswer
            val newScore = player.individualScore + pointsEarned
            val nextQuestionIndex = currentQuestionIndex + 1
            val hasCompletedAllQuestions = nextQuestionIndex >= TOTAL_QUESTIONS

            val updatedPlayer = player.copy(
                individualAnswers = newAnswers,
                individualScore = newScore,
                currentQuestionIndex = if (hasCompletedAllQuestions) currentQuestionIndex else nextQuestionIndex,
                timeLeftOnCurrentQuestion = if (hasCompletedAllQuestions) 0 else TIME_PER_QUESTION,
                hasFinished = hasCompletedAllQuestions,
                finishedAt = if (hasCompletedAllQuestions) System.currentTimeMillis() else 0
            )

            // ✅ ACTUALIZAR FIREBASE
            val updates = mutableMapOf<String, Any>()
            val teamPath = if (teamSide == TeamSide.A) "teamA" else "teamB"
            val team = if (teamSide == TeamSide.A) match.teamA else match.teamB

            val updatedPlayers = team.players.map { p ->
                if (p.id == playerId) updatedPlayer else p
            }
            updates["$teamPath/players"] = updatedPlayers

            // ✅ LOG MEJORADO CON INFORMACIÓN DE LA RESPUESTA
            val selectedText = getSelectedAnswerText(question, normalizedLetter)
            val logMessage = if (hasCompletedAllQuestions) {
                "🏁 ${player.name} TERMINÓ todas las preguntas! Puntuación final: $newScore"
            } else {
                "${player.name}: ${if (isCorrect) "✅" else "❌"} $normalizedLetter) ${selectedText.take(20)}... (+$pointsEarned pts) [${nextQuestionIndex}/20]"
            }

            val newLog = match.log.toMutableList().apply {
                add(logMessage)
                if (size > 80) removeFirst()
            }
            updates["log"] = newLog

            // ✅ VERIFICAR SI TODOS TERMINARON
            val allPlayersUpdated = (if (teamSide == TeamSide.A) updatedPlayers + match.teamB.players else match.teamA.players + updatedPlayers)
            val allFinished = allPlayersUpdated.all { it.hasFinished }

            if (allFinished) {
                finishGame(match, allPlayersUpdated, updates, newLog)
            }

            matchesRef.child(code).updateChildren(updates).await()

            Log.d(TAG, "=== SUBMIT SUCCESS ===")
            Log.d(TAG, "Answer submitted successfully: ${player.name}, correct=$isCorrect, points=$pointsEarned")
            Log.d(TAG, "========================")

            return isCorrect

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error submitting answer", e)
            return false
        }
    }

    // ✅ MANTENER COMPATIBILIDAD CON EL MÉTODO ANTERIOR
    override suspend fun submitAnswer(code: String, optionIndex: Int): Boolean {
        val letter = when (optionIndex) {
            0 -> 'A'
            1 -> 'B'
            2 -> 'C'
            3 -> 'D'
            else -> {
                Log.e(TAG, "Invalid option index: $optionIndex")
                return false
            }
        }
        return submitAnswerByLetter(code, letter)
    }

    // ✅ FUNCIÓN AUXILIAR PARA OBTENER PREGUNTA POR ÍNDICE
    private fun getQuestionByIndex(index: Int): Question? {
        val question = questionsLoader.getQuestionByIndex(index)
            ?: SampleQuestionsICFES.list.getOrNull(index)

        if (question != null && !question.isValid()) {
            Log.e(TAG, "Question at index $index is invalid: ${question.getDebugInfo()}")
            return null
        }

        return question
    }

    // ✅ FUNCIÓN AUXILIAR PARA OBTENER TEXTO DE LA RESPUESTA SELECCIONADA
    private fun getSelectedAnswerText(question: Question, letter: Char): String {
        return when (letter.uppercaseChar()) {
            'A' -> question.optionA
            'B' -> question.optionB
            'C' -> question.optionC
            'D' -> question.optionD
            else -> ""
        }
    }

    // ✅ FUNCIÓN AUXILIAR PARA FINALIZAR EL JUEGO
    private fun finishGame(
        match: Match,
        allPlayersUpdated: List<Player>,
        updates: MutableMap<String, Any>,
        newLog: MutableList<String>
    ) {
        Log.d(TAG, "All players finished, ending game")
        val teamAScore = allPlayersUpdated.filter { match.teamA.players.any { ta -> ta.id == it.id } }.sumOf { it.individualScore }
        val teamBScore = allPlayersUpdated.filter { match.teamB.players.any { tb -> tb.id == it.id } }.sumOf { it.individualScore }

        val winner = when {
            teamAScore > teamBScore -> "A"
            teamBScore > teamAScore -> "B"
            else -> "EMPATE"
        }

        val finalLog = newLog.apply {
            add("🎯 TODOS HAN TERMINADO!")
            add("🏆 RESULTADO: Equipo A: $teamAScore pts, Equipo B: $teamBScore pts")
            add(when (winner) {
                "A" -> "👑 GANADOR: EQUIPO A"
                "B" -> "👑 GANADOR: EQUIPO B"
                else -> "🤝 EMPATE"
            })
        }

        updates.putAll(mapOf(
            "finished" to true,
            "winner" to winner,
            "finalScoreA" to teamAScore,
            "finalScoreB" to teamBScore,
            "finishedAt" to System.currentTimeMillis(),
            "log" to finalLog
        ))
    }

    // ✅ MANTENER IGUAL: forcePlayerNextQuestion
    override suspend fun forcePlayerNextQuestion(code: String, playerId: String) {
        try {
            Log.d(TAG, "Force advancing player $playerId due to timeout: $code")

            val snapshot = matchesRef.child(code).get().await()
            val match = snapshot.getValue(Match::class.java) ?: return

            val player = match.findPlayer(playerId) ?: return
            val teamSide = match.getPlayerTeamSide(playerId) ?: return

            if (player.hasFinished || player.hasAnsweredCurrentQuestion()) {
                return
            }

            val nextQuestionIndex = player.currentQuestionIndex + 1
            val hasCompletedAllQuestions = nextQuestionIndex >= TOTAL_QUESTIONS

            val updatedPlayer = player.copy(
                currentQuestionIndex = if (hasCompletedAllQuestions) player.currentQuestionIndex else nextQuestionIndex,
                timeLeftOnCurrentQuestion = if (hasCompletedAllQuestions) 0 else TIME_PER_QUESTION,
                hasFinished = hasCompletedAllQuestions,
                finishedAt = if (hasCompletedAllQuestions) System.currentTimeMillis() else 0
            )

            val teamPath = if (teamSide == TeamSide.A) "teamA" else "teamB"
            val team = if (teamSide == TeamSide.A) match.teamA else match.teamB

            val updatedPlayers = team.players.map { p ->
                if (p.id == playerId) updatedPlayer else p
            }

            val logMessage = if (hasCompletedAllQuestions) {
                "⏰ ${player.name} se quedó sin tiempo y terminó con ${player.individualScore} puntos"
            } else {
                "⏰ ${player.name} se quedó sin tiempo en pregunta ${player.currentQuestionIndex + 1}"
            }

            val newLog = match.log.toMutableList().apply {
                add(logMessage)
                if (size > 80) removeFirst()
            }

            val updates = mapOf(
                "$teamPath/players" to updatedPlayers,
                "log" to newLog
            )

            matchesRef.child(code).updateChildren(updates).await()
            Log.d(TAG, "Player $playerId advanced due to timeout")

        } catch (e: Exception) {
            Log.e(TAG, "Error forcing player next question", e)
        }
    }

    private fun generateCode(length: Int = 6): String {
        val alphabet = (('A'..'Z') + ('0'..'9')).joinToString("")
        return (1..length).map { alphabet[Random.nextInt(alphabet.length)] }.joinToString("")
    }

    fun cleanup(code: String) {
        listeners[code]?.let { listener ->
            matchesRef.child(code).removeEventListener(listener)
            listeners.remove(code)
        }
        flows.remove(code)
    }
}