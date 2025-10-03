package com.charlesdev.icfes.student.duelo

import android.util.Log
import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlin.collections.joinToString
import kotlin.collections.take
import kotlin.let
import kotlin.text.isNotBlank
import kotlin.text.isNotEmpty
import kotlin.text.split
import kotlin.text.startsWith
import kotlin.text.take
import kotlin.text.uppercase

class DuelController(scope: CoroutineScope) {
    val service: GameService = FirebaseGameService(scope)
    var myPlayer by mutableStateOf<Player?>(null)
    var mySide by mutableStateOf<TeamSide?>(null)
    var myMatchFlow: StateFlow<Match?>? = null

    private val questionsLoader = DynamicQuestionsLoader()

    // ✅ EXACTAMENTE IGUAL QUE EN HOMESCREEN
    private val database = FirebaseDatabase.getInstance().getReference("Users")

    init {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            // ✅ MANTENER EXACTAMENTE IGUAL: La lógica original de inicialización
            myPlayer = Player(
                id = user.uid,
                name = user.displayName ?: user.email ?: "Usuario",
                email = user.email ?: "",
                profileImageUrl = "", // ✅ Inicialmente vacío
                // MANTENER EXACTAMENTE IGUAL: Todos los campos del avance individual
                currentQuestionIndex = 0,
                timeLeftOnCurrentQuestion = 25,
                individualAnswers = emptyList(),
                individualScore = 0,
                hasFinished = false,
                finishedAt = 0
            )
            Log.d("DuelController", "Player initialized: ${myPlayer?.name}")

            // ✅ USAR EXACTAMENTE LA MISMA LÓGICA QUE HOMESCREEN
            loadProfileImageLikeHomeScreen(user.uid)

        } ?: Log.w("DuelController", "No authenticated user found")
    }

    // ✅ EXACTAMENTE LA MISMA LÓGICA QUE EN HOMESCREEN
    private fun loadProfileImageLikeHomeScreen(userId: String) {
        database.child(userId).get().addOnSuccessListener { snapshot ->
            // ✅ MISMA LÍNEA QUE EN HOMESCREEN
            val imageUrl = snapshot.child("profileImage").value?.toString() ?: ""

            Log.d("DuelController", "Loaded imageUrl from DB: '$imageUrl'")

            // ✅ SOLO actualizar la foto, mantener todo lo demás igual
            myPlayer?.let { currentPlayer ->
                myPlayer = currentPlayer.copy(profileImageUrl = imageUrl)
                Log.d("DuelController", "Updated player photo: ${imageUrl.isNotEmpty()}")
            }
        }.addOnFailureListener { exception ->
            Log.w("DuelController", "Failed to load profile image", exception)
        }
    }

    // ✅ MANTENER EXACTAMENTE IGUAL: Todas las funciones existentes
    fun bind(code: String) {
        myMatchFlow = service.observe(code)
    }

    fun cleanup(code: String) {
        if (service is FirebaseGameService) {
            service.cleanup(code)
        }
        questionsLoader.clearCache()
    }

    fun getQuestionsLoader(): DynamicQuestionsLoader = questionsLoader

    // ✅ MANTENER EXACTAMENTE IGUAL: Helpers del avance individual
    fun getMyCurrentPlayer(): Player? {
        val match = myMatchFlow?.value ?: return null
        return match.findPlayer(myPlayer?.id ?: "")
    }

    fun haveIFinished(): Boolean {
        return getMyCurrentPlayer()?.hasFinished == true
    }

    fun getMyCurrentScore(): Int {
        return getMyCurrentPlayer()?.individualScore ?: 0
    }

    fun getMyCurrentQuestionIndex(): Int {
        return getMyCurrentPlayer()?.currentQuestionIndex ?: 0
    }

    fun getMyTimeLeft(): Int {
        return getMyCurrentPlayer()?.timeLeftOnCurrentQuestion ?: 25
    }

    // ✅ HELPERS PARA AVATAR (sin tocar lógica de juego)
    fun getMyProfileImageUrl(): String {
        return myPlayer?.profileImageUrl ?: ""
    }

    fun hasProfileImage(): Boolean {
        val url = getMyProfileImageUrl()
        return url.isNotEmpty() && url.startsWith("http")
    }

    fun getMyInitials(): String {
        val name = myPlayer?.name ?: "Usuario"
        return if (name.isNotBlank()) {
            name.split(" ")
                .take(2)
                .joinToString("") { it.take(1).uppercase() }
        } else {
            "U"
        }
    }

    // ✅ NUEVO: Función para refrescar manualmente (si es necesario)
    fun refreshProfileImage() {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            loadProfileImageLikeHomeScreen(user.uid)
        }
    }
}