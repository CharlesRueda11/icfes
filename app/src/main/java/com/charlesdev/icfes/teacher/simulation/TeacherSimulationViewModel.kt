package com.charlesdev.icfes.teacher.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charlesdev.icfes.teacher.practice_evaluation.TeacherQuestion
import com.charlesdev.icfes.teacher.practice_evaluation.getICFESModules
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ===================================
 * 🎯 VIEWMODEL COMPLETO - GESTIÓN DE SIMULACROS PREMIUM
 * ===================================
 * Lógica de negocio para crear, gestionar y analizar simulacros personalizados
 */

class TeacherSimulationViewModel : ViewModel() {

    // ✅ ESTADOS PRINCIPALES
    private val _simulations = MutableStateFlow<List<TeacherSimulation>>(emptyList())
    val simulations: StateFlow<List<TeacherSimulation>> = _simulations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedSimulation = MutableStateFlow<TeacherSimulation?>(null)
    val selectedSimulation: StateFlow<TeacherSimulation?> = _selectedSimulation.asStateFlow()

    private val _studentResults = MutableStateFlow<List<StudentSimulationProgress>>(emptyList())
    val studentResults: StateFlow<List<StudentSimulationProgress>> = _studentResults.asStateFlow()

    private val _analytics = MutableStateFlow<TeacherAnalysisReport?>(null)
    val analytics: StateFlow<TeacherAnalysisReport?> = _analytics.asStateFlow()

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ===================================
    // 🎯 CARGA DE DATOS
    // ===================================

    /**
     * Carga todos los simulacros del profesor
     */
    // ✅ CORRECCIÓN - Método simplificado
    fun loadTeacherSimulations(teacherId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = database.reference
                    .child("TeacherSimulations")
                    .child(teacherId)
                    .get()
                    .await()

                val simList = snapshot.children.mapNotNull { child ->
                    child.getValue(TeacherSimulation::class.java)
                }.sortedByDescending { it.createdAt }

                _simulations.value = simList
                _error.value = null

            } catch (e: Exception) {
                _error.value = "Error al cargar simulacros: ${e.message}"
                _simulations.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Carga un simulacro específico con detalles completos
     */
    fun loadSimulationDetails(simulationId: String, teacherId: String) {
        viewModelScope.launch {
            try {
                val snapshot = database.reference
                    .child("TeacherSimulations")
                    .child(teacherId)
                    .child(simulationId)
                    .get()
                    .await()

                val simulation = snapshot.getValue(TeacherSimulation::class.java)
                _selectedSimulation.value = simulation

            } catch (e: Exception) {
                _error.value = "Error al cargar detalles: ${e.message}"
            }
        }
    }

    // ===================================
    // 🎯 CREACIÓN Y GESTIÓN
    // ===================================

    /**
     * Crea un nuevo simulacro personalizado
     */
    fun createSimulation(
        title: String,
        description: String,
        config: TeacherSimulationConfig,
        moduleConfigs: List<ModuleConfiguration>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val teacher = auth.currentUser
                val teacherId = teacher?.uid ?: throw IllegalStateException("Usuario no autenticado")
                val teacherName = teacher.displayName ?: "Profesor"

                // Obtener datos del profesor
                val teacherData = database.reference
                    .child("Profesores")
                    .child(teacherId)
                    .get()
                    .await()

                val institution = teacherData.child("institucion").getValue(String::class.java) ?: ""

                // Cargar preguntas para cada módulo
                val sessions = moduleConfigs.map { config ->
                    val questions = loadQuestionsForModule(
                        teacherId = teacherId,
                        moduleId = config.moduleId,
                        count = config.questionCount,
                        difficultyBias = DifficultyBias.BALANCED
                    )

                    val moduleInfo = getICFESModules().find { it.id == config.moduleId }

                    TeacherSimulationSession(
                        sessionNumber = moduleConfigs.indexOf(config) + 1,
                        moduleId = config.moduleId,
                        moduleName = moduleInfo?.name ?: config.moduleId,
                        description = moduleInfo?.description ?: "",
                        questions = questions,
                        timeLimit = config.timeLimit,
                        difficultyBias = DifficultyBias.BALANCED,
                        color = moduleInfo?.color?.value ?: 0xFF607D8B,
                        icon = moduleInfo?.emoji ?: "📚"
                    )
                }

                val simulation = TeacherSimulation(
                    teacherId = teacherId,
                    teacherName = teacherName,
                    institution = institution,
                    simulationId = "SIM_${System.currentTimeMillis()}",
                    title = title,
                    description = description,
                    config = config,
                    sessions = sessions,
                    totalQuestions = moduleConfigs.sumOf { it.questionCount },
                    totalDuration = calculateTotalDuration(moduleConfigs)
                )

                database.reference
                    .child("TeacherSimulations")
                    .child(teacherId)
                    .child(simulation.simulationId)
                    .setValue(simulation)
                    .await()

                loadTeacherSimulations(teacherId)
                _error.value = null

            } catch (e: Exception) {
                _error.value = "Error al crear simulacro: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza un simulacro existente
     */
    fun updateSimulation(simulation: TeacherSimulation) {
        viewModelScope.launch {
            try {
                database.reference
                    .child("TeacherSimulations")
                    .child(simulation.teacherId)
                    .child(simulation.simulationId)
                    .setValue(simulation.copy(lastModified = System.currentTimeMillis()))
                    .await()

                loadTeacherSimulations(simulation.teacherId)
                _error.value = null

            } catch (e: Exception) {
                _error.value = "Error al actualizar: ${e.message}"
            }
        }
    }

    /**
     * Elimina un simulacro
     */
    fun deleteSimulation(simulation: TeacherSimulation) {
        viewModelScope.launch {
            try {
                database.reference
                    .child("TeacherSimulations")
                    .child(simulation.teacherId)
                    .child(simulation.simulationId)
                    .removeValue()
                    .await()

                loadTeacherSimulations(simulation.teacherId)
                _error.value = null

            } catch (e: Exception) {
                _error.value = "Error al eliminar: ${e.message}"
            }
        }
    }

    /**
     * Activa/desactiva un simulacro
     */
    fun toggleSimulationStatus(simulation: TeacherSimulation, isActive: Boolean) {
        viewModelScope.launch {
            try {
                val updated = simulation.copy(isActive = isActive)
                updateSimulation(updated)
            } catch (e: Exception) {
                _error.value = "Error al cambiar estado: ${e.message}"
            }
        }
    }

    // ===================================
    // 🎯 CARGA DE PREGUNTAS
    // ===================================

    /**
     * Carga preguntas desde el contenido del profesor
     */
    suspend fun loadQuestionsForModule(
        teacherId: String,
        moduleId: String,
        count: Int,
        difficultyBias: DifficultyBias
    ): List<TeacherQuestion> {
        return try {
            val moduleRef = database.reference
                .child("ContenidoDocente")
                .child("profesores")
                .child(teacherId)
                .child("modulos")
                .child(moduleId)

            // Cargar preguntas de práctica y evaluación
            val practiceSnapshot = moduleRef.child("practica").get().await()
            val evaluationSnapshot = moduleRef.child("evaluacion").get().await()

            val allQuestions = mutableListOf<TeacherQuestion>()

            practiceSnapshot.children.forEach { child ->
                val question = child.getValue(TeacherQuestion::class.java)
                question?.let { allQuestions.add(it) }
            }

            evaluationSnapshot.children.forEach { child ->
                val question = child.getValue(TeacherQuestion::class.java)
                question?.let { allQuestions.add(it) }
            }

            // Aplicar filtro de dificultad
            val filteredQuestions = when (difficultyBias) {
                DifficultyBias.FACIL -> allQuestions.filter { it.difficulty == "FACIL" }
                DifficultyBias.DIFICIL -> allQuestions.filter { it.difficulty == "DIFICIL" }
                DifficultyBias.BALANCED -> allQuestions
                DifficultyBias.PERSONALIZADO -> allQuestions
            }

            // Mezclar y limitar cantidad
            filteredQuestions.shuffled().take(count)

        } catch (e: Exception) {
            emptyList()
        }
    }

    // ===================================
    // 🎯 ANÁLISIS Y REPORTES
    // ===================================

    /**
     * Carga resultados de estudiantes para un simulacro
     */
    fun loadStudentResults(simulationId: String) {
        viewModelScope.launch {
            try {
                val snapshot = database.reference
                    .child("StudentSimulationResults")
                    .orderByChild("simulationId")
                    .equalTo(simulationId)
                    .get()
                    .await()

                val results = snapshot.children.mapNotNull { child ->
                    child.getValue(StudentSimulationProgress::class.java)
                }

                _studentResults.value = results
                _error.value = null

            } catch (e: Exception) {
                _error.value = "Error al cargar resultados: ${e.message}"
            }
        }
    }

    /**
     * Genera análisis detallado del simulacro
     */
    fun generateAnalysisReport(simulationId: String, teacherId: String) {
        viewModelScope.launch {
            try {
                val simulation = _simulations.value.find { it.simulationId == simulationId }
                val results = _studentResults.value

                if (simulation != null && results.isNotEmpty()) {
                    val analysis = createAnalysisReport(simulation, results)
                    _analytics.value = analysis
                }

            } catch (e: Exception) {
                _error.value = "Error al generar análisis: ${e.message}"
            }
        }
    }

    /**
     * Exporta datos del simulacro
     */
    fun exportSimulationData(
        simulation: TeacherSimulation,
        config: ExportConfiguration,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val exportData = prepareExportData(simulation, config)
                val exportId = "EXPORT_${System.currentTimeMillis()}"

                database.reference
                    .child("TeacherSimulationExports")
                    .child(simulation.teacherId)
                    .child(exportId)
                    .setValue(exportData)
                    .await()

                onSuccess("Datos exportados exitosamente")

            } catch (e: Exception) {
                onError("Error al exportar: ${e.message}")
            }
        }
    }

    // ===================================
    // 🎯 FUNCIONES PRIVADAS AUXILIARES
    // ===================================

    private fun createSessionsFromConfig(
        moduleConfigs: List<ModuleConfiguration>
    ): List<TeacherSimulationSession> {
        return moduleConfigs.mapIndexed { index, config ->
            val moduleInfo = getICFESModules().find { it.id == config.moduleId }

            TeacherSimulationSession(
                sessionNumber = index + 1,
                moduleId = config.moduleId,
                moduleName = moduleInfo?.name ?: config.moduleId,
                description = moduleInfo?.description ?: "",
                questions = emptyList(), // Se llenarán después
                timeLimit = config.timeLimit,
                difficultyBias = DifficultyBias.BALANCED,
                focusAreas = config.focusCompetencies,
                color = moduleInfo?.color?.value ?: 0xFF607D8B,
                icon = moduleInfo?.emoji ?: "📚"
            )
        }
    }

    private fun calculateTotalDuration(moduleConfigs: List<ModuleConfiguration>): Long {
        return moduleConfigs.sumOf { it.timeLimit * 60 * 1000L } + // Tiempo de sesiones
                (moduleConfigs.size - 1) * 15 * 60 * 1000L // Breaks de 15 minutos
    }

    private fun createAnalysisReport(
        simulation: TeacherSimulation,
        results: List<StudentSimulationProgress>
    ): TeacherAnalysisReport {
        val completedResults = results.filter { it.completionStatus == CompletionStatus.COMPLETED }
        val totalStudents = completedResults.size

        if (totalStudents == 0) return TeacherAnalysisReport()

        val averageScore = completedResults.map { it.globalScore }.average()
        val bestScore = completedResults.maxOf { it.globalScore }
        val worstScore = completedResults.minOf { it.globalScore }

        // Análisis por módulo
        val moduleScores = simulation.sessions.associate { session ->
            val scores = completedResults.flatMap { result ->
                result.sessionResults.filter { it.sessionId == session.moduleId }
                    .map { it.score }
            }

            session.moduleName to (scores.average())
        }

        val mostChallengingModule = moduleScores.minByOrNull { it.value }?.key ?: ""
        val strongestModule = moduleScores.maxByOrNull { it.value }?.key ?: ""

        // Análisis de estudiantes
        val studentDetails = completedResults.map { result ->
            StudentDetail(
                studentId = result.studentId,
                studentName = result.studentName,
                score = result.globalScore,
                percentile = calculatePercentile(result.globalScore, completedResults),
                improvementAreas = identifyImprovementAreas(result),
                strengths = identifyStrengths(result)
            )
        }

        return TeacherAnalysisReport(
            reportId = "ANALYSIS_${System.currentTimeMillis()}",
            simulationId = simulation.simulationId,
            teacherId = simulation.teacherId,
            summary = ReportSummary(
                totalStudents = totalStudents,
                averageScore = averageScore,
                scoreRange = Pair(worstScore, bestScore),
                mostChallengingModule = mostChallengingModule,
                strongestModule = strongestModule
            ),
            studentDetails = studentDetails,
            recommendations = generateRecommendations(simulation, completedResults)
        )
    }

    private fun calculatePercentile(score: Int, results: List<StudentSimulationProgress>): Int {
        val sortedScores = results.map { it.globalScore }.sorted()
        val position = sortedScores.indexOf(score) + 1
        return (position.toDouble() / sortedScores.size * 100).toInt()
    }

    private fun identifyImprovementAreas(result: StudentSimulationProgress): List<String> {
        return result.sessionResults
            .filter { it.percentage < 60 }
            .map { "Reforzar ${it.moduleName}" }
            .take(3)
    }

    private fun identifyStrengths(result: StudentSimulationProgress): List<String> {
        return result.sessionResults
            .filter { it.percentage > 80 }
            .map { "Excelente en ${it.moduleName}" }
            .take(3)
    }

    private fun generateRecommendations(
        simulation: TeacherSimulation,
        results: List<StudentSimulationProgress>
    ): List<String> {
        return listOf(
            "Enfocar práctica en el módulo más desafiante",
            "Revisar preguntas con menor porcentaje de aciertos",
            "Implementar sesiones adicionales de práctica",
            "Personalizar feedback según áreas débiles",
            "Programar simulacros periódicos para seguimiento"
        )
    }

    private fun prepareExportData(
        simulation: TeacherSimulation,
        config: ExportConfiguration
    ): Map<String, Any> {
        return mapOf(
            "simulation" to simulation,
            "exportConfig" to config,
            "timestamp" to System.currentTimeMillis(),
            "format" to config.format.name
        )
    }
}