package com.charlesdev.icfes.student.utils



import com.google.ai.client.generativeai.GenerativeModel
import com.charlesdev.icfes.data.Data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class InstitutionSuggestion(
    val fullName: String,
    val municipality: String,
    val department: String,
    val sector: String, // "Público" o "Privado"
    val level: String, // "Básica y Media", "Media Técnica", etc.
    val confidence: Float
)

class InstitutionHelper {

    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = Data.apikey
    )

    /**
     * Busca y normaliza información de instituciones educativas colombianas
     */
    suspend fun searchInstitution(partialName: String): List<InstitutionSuggestion> = withContext(Dispatchers.IO) {
        try {
            if (partialName.length < 3) return@withContext emptyList()

            val prompt = createInstitutionSearchPrompt(partialName)
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text ?: return@withContext emptyList()

            parseInstitutionResponse(responseText)

        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Valida y completa información específica de una institución
     */
    suspend fun validateAndCompleteInstitution(
        institutionName: String,
        municipalityHint: String? = null
    ): InstitutionSuggestion? = withContext(Dispatchers.IO) {
        try {
            val prompt = createValidationPrompt(institutionName, municipalityHint)
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text ?: return@withContext null

            parseValidationResponse(responseText)

        } catch (e: Exception) {
            null
        }
    }

    /**
     * Búsqueda híbrida: primero IA, luego base de datos local
     */
    suspend fun hybridSearch(query: String): List<InstitutionSuggestion> = withContext(Dispatchers.IO) {
        // Primero intentar con IA
        val aiResults = try {
            if (query.length >= 3) {
                searchInstitution(query)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }

        // Si la IA devuelve resultados, usarlos
        if (aiResults.isNotEmpty()) {
            return@withContext aiResults
        }

        // Fallback a base de datos local
        LocalInstitutionsData.searchInstitutionFallback(query)
    }

    /**
     * Validación híbrida que combina IA y datos locales
     */
    suspend fun hybridValidation(
        institutionName: String,
        municipalityHint: String? = null
    ): InstitutionSuggestion? = withContext(Dispatchers.IO) {
        // Primero verificar en base de datos local
        val localResult = LocalInstitutionsData.validateInstitution(institutionName)
        if (localResult != null) {
            return@withContext localResult
        }

        // Si no está en local, intentar con IA
        try {
            validateAndCompleteInstitution(institutionName, municipalityHint)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Función auxiliar para búsqueda sin conexión (fallback mejorado)
     */
    fun getCommonInstitutions(query: String): List<InstitutionSuggestion> {
        return LocalInstitutionsData.searchInstitutionFallback(query)
    }

    private fun createInstitutionSearchPrompt(partialName: String): String {
        return """
        Eres un experto en el sistema educativo colombiano. Un estudiante está buscando su institución educativa.

        TEXTO PARCIAL INGRESADO: "$partialName"

        Tu tarea es sugerir hasta 5 instituciones educativas reales de Colombia que coincidan con este texto parcial.

        INSTRUCCIONES:
        - Solo sugiere instituciones REALES que existan en Colombia
        - Incluye instituciones públicas y privadas
        - Prioriza instituciones de educación media (grados 10° y 11°)
        - Si el texto es muy general, sugiere las más reconocidas
        - Incluye información completa: nombre oficial, municipio, departamento

        CONOCIMIENTO CONTEXTUAL DE COLOMBIA:
        - Departamentos principales: Antioquia, Cundinamarca, Valle del Cauca, Santander, Atlántico, etc.
        - Ciudades principales: Bogotá, Medellín, Cali, Barranquilla, Cartagena, Bucaramanga, etc.
        - Instituciones reconocidas como: Gimnasio Moderno, Colegio San Carlos, INEM, etc.

        Responde SOLO en formato JSON válido:
        {
            "sugerencias": [
                {
                    "nombre_completo": "Nombre oficial completo de la institución",
                    "municipio": "Municipio donde está ubicada",
                    "departamento": "Departamento de Colombia",
                    "sector": "Público o Privado",
                    "nivel": "Básica y Media / Media Técnica / etc.",
                    "confianza": 0.95
                }
            ]
        }
        """.trimIndent()
    }

    private fun createValidationPrompt(institutionName: String, municipalityHint: String?): String {
        return """
        Eres un experto en instituciones educativas de Colombia. Necesito validar y completar información.

        INSTITUCIÓN PROPORCIONADA: "$institutionName"
        ${if (municipalityHint != null) "PISTA DE UBICACIÓN: \"$municipalityHint\"" else ""}

        Tu tarea es:
        1. Identificar si esta institución existe realmente en Colombia
        2. Proporcionar el nombre oficial completo correcto
        3. Completar la información de ubicación exacta

        CRITERIOS DE VALIDACIÓN:
        - La institución debe existir realmente
        - Debe ofrecer educación media (grados 10° y 11°)
        - Proporciona el nombre oficial registrado ante el MEN (Ministerio de Educación Nacional)

        Responde SOLO en formato JSON válido:
        {
            "existe": true/false,
            "nombre_oficial": "Nombre oficial completo según MEN",
            "municipio": "Municipio exacto",
            "departamento": "Departamento de Colombia",
            "sector": "Público/Privado",
            "nivel": "Tipo de educación que ofrece",
            "codigo_dane": "Código DANE si lo conoces (opcional)",
            "confianza": 0.98
        }
        """.trimIndent()
    }

    private fun parseInstitutionResponse(responseText: String): List<InstitutionSuggestion> {
        return try {
            val cleanJson = responseText.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()

            val jsonObject = JSONObject(cleanJson)
            val suggestions = jsonObject.getJSONArray("sugerencias")

            val result = mutableListOf<InstitutionSuggestion>()

            for (i in 0 until suggestions.length()) {
                val suggestion = suggestions.getJSONObject(i)
                result.add(
                    InstitutionSuggestion(
                        fullName = suggestion.getString("nombre_completo"),
                        municipality = suggestion.getString("municipio"),
                        department = suggestion.getString("departamento"),
                        sector = suggestion.getString("sector"),
                        level = suggestion.getString("nivel"),
                        confidence = suggestion.getDouble("confianza").toFloat()
                    )
                )
            }

            result.sortedByDescending { it.confidence }

        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseValidationResponse(responseText: String): InstitutionSuggestion? {
        return try {
            val cleanJson = responseText.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()

            val jsonObject = JSONObject(cleanJson)

            if (!jsonObject.getBoolean("existe")) {
                return null
            }

            InstitutionSuggestion(
                fullName = jsonObject.getString("nombre_oficial"),
                municipality = jsonObject.getString("municipio"),
                department = jsonObject.getString("departamento"),
                sector = jsonObject.getString("sector"),
                level = jsonObject.getString("nivel"),
                confidence = jsonObject.getDouble("confianza").toFloat()
            )

        } catch (e: Exception) {
            null
        }
    }
}

// Objeto para importar en otros archivos
object LocalInstitutionsData {
    // Se implementará en el siguiente archivo
    fun searchInstitutionFallback(query: String): List<InstitutionSuggestion> = emptyList()
    fun validateInstitution(name: String): InstitutionSuggestion? = null
}