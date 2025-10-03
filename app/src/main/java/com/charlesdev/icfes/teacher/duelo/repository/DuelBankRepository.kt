package com.charlesdev.icfes.teacher.duelo.repository

import android.util.Log
import com.charlesdev.icfes.student.duelo.Difficulty
import com.charlesdev.icfes.student.duelo.DuelTopic
import com.charlesdev.icfes.student.duelo.Question
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.*

/**
 * Repositorio para gestionar el banco de preguntas del duelo ICFES
 * Estructura Firebase: DueloBancoPreguntasICFES/profesores/{profesorId}/
 */
class DuelBankRepository {

    private val database = FirebaseDatabase.getInstance()
    private val rootRef = database.getReference("DueloBancoPreguntasICFES")

    companion object {
        private const val TAG = "DuelBankRepository"
        private const val NODE_PROFESORES = "profesores"
        private const val NODE_ACTIVAS = "activas"
        private const val NODE_BORRADORES = "borradores"
        private const val NODE_METADATA = "metadata"
        private const val NODE_ASIGNACIONES = "asignaciones"
    }

    // ========== CRUD PARA PREGUNTAS ==========

    /**
     * Crear o actualizar una pregunta
     */
    suspend fun saveQuestion(
        question: Question,
        isDraft: Boolean = true,
        profesorId: String? = null
    ): Result<String> {
        return try {
            val uid = profesorId ?: getCurrentProfesorId()
            val node = if (isDraft) NODE_BORRADORES else NODE_ACTIVAS

            val questionRef = rootRef
                .child(NODE_PROFESORES)
                .child(uid)
                .child(node)
                .child(question.id)

            // Validar antes de guardar
            if (!question.isValid()) {
                Log.e(TAG, "Pregunta inválida: ${question.getDebugInfo()}")
                return Result.failure(IllegalArgumentException("Pregunta inválida"))
            }

            // ✅ CONVERTIR A MAP para evitar error con Char
            val questionMap = question.toMap(isActive = !isDraft)

            questionRef.setValue(questionMap).await()

            // Actualizar metadata
            updateMetadata(uid)

            Log.d(TAG, "Pregunta guardada: ${question.id} (${if (isDraft) "borrador" else "activa"})")
            Result.success(question.id)

        } catch (e: Exception) {
            Log.e(TAG, "Error guardando pregunta", e)
            Result.failure(e)
        }
    }

    /**
     * Publicar una pregunta (mover de borradores a activas)
     */
    suspend fun publishQuestion(questionId: String, profesorId: String? = null): Result<Unit> {
        return try {
            val uid = profesorId ?: getCurrentProfesorId()

            // 1. Leer de borradores
            val draftRef = rootRef
                .child(NODE_PROFESORES)
                .child(uid)
                .child(NODE_BORRADORES)
                .child(questionId)

            val snapshot = draftRef.get().await()
            val question = snapshot.toQuestion()
                ?: return Result.failure(IllegalStateException("Pregunta no encontrada"))

            // 2. Validar
            if (!question.isValid()) {
                return Result.failure(IllegalArgumentException("Pregunta inválida, no se puede publicar"))
            }

            // 3. Guardar en activas como Map
            val activeRef = rootRef
                .child(NODE_PROFESORES)
                .child(uid)
                .child(NODE_ACTIVAS)
                .child(questionId)

            val questionMap = question.toMap(isActive = true)

            activeRef.setValue(questionMap).await()

            // 4. Eliminar de borradores
            draftRef.removeValue().await()

            // 5. Actualizar metadata
            updateMetadata(uid)

            Log.d(TAG, "Pregunta publicada: $questionId")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Error publicando pregunta", e)
            Result.failure(e)
        }
    }

    /**
     * Mover pregunta activa a borradores
     */
    suspend fun unpublishQuestion(questionId: String, profesorId: String? = null): Result<Unit> {
        return try {
            val uid = profesorId ?: getCurrentProfesorId()

            // 1. Leer de activas
            val activeRef = rootRef
                .child(NODE_PROFESORES)
                .child(uid)
                .child(NODE_ACTIVAS)
                .child(questionId)

            val snapshot = activeRef.get().await()
            val question = snapshot.toQuestion()
                ?: return Result.failure(IllegalStateException("Pregunta no encontrada"))

            // 2. Guardar en borradores
            val draftRef = rootRef
                .child(NODE_PROFESORES)
                .child(uid)
                .child(NODE_BORRADORES)
                .child(questionId)

            val questionMap = question.toMap(isActive = false)

            draftRef.setValue(questionMap).await()

            // 3. Eliminar de activas
            activeRef.removeValue().await()

            // 4. Actualizar metadata
            updateMetadata(uid)

            Log.d(TAG, "Pregunta movida a borradores: $questionId")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Error moviendo pregunta a borradores", e)
            Result.failure(e)
        }
    }

    /**
     * Eliminar pregunta (de activas o borradores)
     */
    suspend fun deleteQuestion(
        questionId: String,
        isDraft: Boolean,
        profesorId: String? = null
    ): Result<Unit> {
        return try {
            val uid = profesorId ?: getCurrentProfesorId()
            val node = if (isDraft) NODE_BORRADORES else NODE_ACTIVAS

            rootRef
                .child(NODE_PROFESORES)
                .child(uid)
                .child(node)
                .child(questionId)
                .removeValue()
                .await()

            updateMetadata(uid)

            Log.d(TAG, "Pregunta eliminada: $questionId")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando pregunta", e)
            Result.failure(e)
        }
    }

    // ========== LECTURA DE PREGUNTAS ==========

    /**
     * Obtener preguntas activas en tiempo real
     */
    fun getActiveQuestionsFlow(profesorId: String? = null): Flow<List<Question>> = callbackFlow {
        val uid = profesorId ?: getCurrentProfesorId()

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val questions = mutableListOf<Question>()
                snapshot.children.forEach { child ->
                    child.toQuestion()?.let { question ->
                        if (question.isValid()) {
                            questions.add(question)
                        } else {
                            Log.w(TAG, "Pregunta inválida omitida: ${question.id}")
                        }
                    }
                }
                trySend(questions)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error en listener activas: ${error.message}")
            }
        }

        val ref = rootRef
            .child(NODE_PROFESORES)
            .child(uid)
            .child(NODE_ACTIVAS)

        ref.addValueEventListener(listener)

        awaitClose { ref.removeEventListener(listener) }
    }

    /**
     * Obtener borradores en tiempo real
     */
    fun getDraftQuestionsFlow(profesorId: String? = null): Flow<List<Question>> = callbackFlow {
        val uid = profesorId ?: getCurrentProfesorId()

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val questions = mutableListOf<Question>()
                snapshot.children.forEach { child ->
                    child.toQuestion()?.let { question ->
                        questions.add(question)
                    }
                }
                trySend(questions)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error en listener borradores: ${error.message}")
            }
        }

        val ref = rootRef
            .child(NODE_PROFESORES)
            .child(uid)
            .child(NODE_BORRADORES)

        ref.addValueEventListener(listener)

        awaitClose { ref.removeEventListener(listener) }
    }

    /**
     * Obtener una pregunta específica
     */
    suspend fun getQuestion(
        questionId: String,
        isDraft: Boolean,
        profesorId: String? = null
    ): Result<Question> {
        return try {
            val uid = profesorId ?: getCurrentProfesorId()
            val node = if (isDraft) NODE_BORRADORES else NODE_ACTIVAS

            val snapshot = rootRef
                .child(NODE_PROFESORES)
                .child(uid)
                .child(node)
                .child(questionId)
                .get()
                .await()

            val question = snapshot.toQuestion()
                ?: return Result.failure(IllegalStateException("Pregunta no encontrada"))

            Result.success(question)

        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo pregunta", e)
            Result.failure(e)
        }
    }

    /**
     * Obtener preguntas activas (snapshot único, para estudiantes)
     */
    suspend fun getAllActiveQuestions(profesorId: String): Result<List<Question>> {
        return try {
            val snapshot = rootRef
                .child(NODE_PROFESORES)
                .child(profesorId)
                .child(NODE_ACTIVAS)
                .get()
                .await()

            val questions = mutableListOf<Question>()
            snapshot.children.forEach { child ->
                child.toQuestion()?.let { question ->
                    if (question.isValid()) {
                        questions.add(question)
                    }
                }
            }

            Log.d(TAG, "Obtenidas ${questions.size} preguntas activas de profesor $profesorId")
            Result.success(questions)

        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo preguntas activas", e)
            Result.failure(e)
        }
    }

    // ========== ASIGNACIONES INSTITUCIÓN -> PROFESOR ==========

    /**
     * Asignar profesor a una institución
     */
    suspend fun assignProfesorToInstitution(
        institucion: String,
        profesorId: String,
        nombreProfesor: String
    ): Result<Unit> {
        return try {
            val assignment = mapOf(
                "profesorId" to profesorId,
                "nombreProfesor" to nombreProfesor,
                "activo" to true,
                "fechaAsignacion" to System.currentTimeMillis()
            )

            rootRef
                .child(NODE_ASIGNACIONES)
                .child(institucion)
                .setValue(assignment)
                .await()

            Log.d(TAG, "Profesor $nombreProfesor asignado a $institucion")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Error asignando profesor", e)
            Result.failure(e)
        }
    }

    /**
     * Obtener profesor asignado a una institución
     */
    suspend fun getProfesorByInstitution(institucion: String): Result<String?> {
        return try {
            val snapshot = rootRef
                .child(NODE_ASIGNACIONES)
                .child(institucion)
                .get()
                .await()

            val profesorId = snapshot.child("profesorId").getValue(String::class.java)
            val activo = snapshot.child("activo").getValue(Boolean::class.java) ?: false

            if (activo && profesorId != null) {
                Result.success(profesorId)
            } else {
                Result.success(null)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo profesor por institución", e)
            Result.failure(e)
        }
    }

    // ========== METADATA ==========

    /**
     * Actualizar contadores de metadata
     */
    private suspend fun updateMetadata(profesorId: String) {
        try {
            val activasCount = rootRef
                .child(NODE_PROFESORES)
                .child(profesorId)
                .child(NODE_ACTIVAS)
                .get()
                .await()
                .childrenCount

            val borradoresCount = rootRef
                .child(NODE_PROFESORES)
                .child(profesorId)
                .child(NODE_BORRADORES)
                .get()
                .await()
                .childrenCount

            val metadata = mapOf(
                "totalActivas" to activasCount,
                "totalBorradores" to borradoresCount,
                "ultimaActualizacion" to System.currentTimeMillis()
            )

            rootRef
                .child(NODE_PROFESORES)
                .child(profesorId)
                .child(NODE_METADATA)
                .setValue(metadata)
                .await()

        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando metadata", e)
        }
    }

    /**
     * Obtener metadata del profesor
     */
    suspend fun getMetadata(profesorId: String? = null): Result<Map<String, Any>> {
        return try {
            val uid = profesorId ?: getCurrentProfesorId()

            val snapshot = rootRef
                .child(NODE_PROFESORES)
                .child(uid)
                .child(NODE_METADATA)
                .get()
                .await()

            val metadata = mapOf(
                "totalActivas" to (snapshot.child("totalActivas").getValue(Long::class.java) ?: 0L),
                "totalBorradores" to (snapshot.child("totalBorradores").getValue(Long::class.java) ?: 0L),
                "ultimaActualizacion" to (snapshot.child("ultimaActualizacion").getValue(Long::class.java) ?: 0L)
            )

            Result.success(metadata)

        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo metadata", e)
            Result.failure(e)
        }
    }

    // ========== HELPERS ==========

    private fun getCurrentProfesorId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("Profesor no autenticado")
    }

    /**
     * Generar ID único para pregunta
     */
    fun generateQuestionId(): String {
        return "Q_${System.currentTimeMillis()}_${Random().nextInt(9999)}"
    }

    // ✅ HELPERS para conversión Firebase <-> Question
    private fun DataSnapshot.toQuestion(): Question? {
        return try {
            Question(
                id = child("id").getValue(String::class.java) ?: "",
                text = child("text").getValue(String::class.java) ?: "",
                optionA = child("optionA").getValue(String::class.java) ?: "",
                optionB = child("optionB").getValue(String::class.java) ?: "",
                optionC = child("optionC").getValue(String::class.java) ?: "",
                optionD = child("optionD").getValue(String::class.java) ?: "",
                correctAnswer = (child("correctAnswer").getValue(String::class.java) ?: "A")[0],
                difficulty = Difficulty.valueOf(child("difficulty").getValue(String::class.java) ?: "EASY"),
                topic = DuelTopic.valueOf(child("topic").getValue(String::class.java) ?: "OTROS"),
                hint = child("hint").getValue(String::class.java) ?: "",
                formula = child("formula").getValue(String::class.java) ?: "",
                createdAt = child("createdAt").getValue(Long::class.java) ?: 0L,
                lastModified = child("lastModified").getValue(Long::class.java) ?: System.currentTimeMillis(),
                isActive = child("isActive").getValue(Boolean::class.java) ?: false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando Question", e)
            null
        }
    }

    private fun Question.toMap(isActive: Boolean = this.isActive): Map<String, Any> {
        return mapOf(
            "id" to id,
            "text" to text,
            "optionA" to optionA,
            "optionB" to optionB,
            "optionC" to optionC,
            "optionD" to optionD,
            "correctAnswer" to correctAnswer.toString(),
            "difficulty" to difficulty.name,
            "topic" to topic.name,
            "hint" to hint,
            "formula" to formula,
            "createdAt" to createdAt,
            "lastModified" to System.currentTimeMillis(),
            "isActive" to isActive
        )
    }
}