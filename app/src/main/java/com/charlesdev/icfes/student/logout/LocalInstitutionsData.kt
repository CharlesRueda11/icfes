package com.charlesdev.icfes.student.logout



import com.charlesdev.icfes.student.utils.InstitutionSuggestion

/**
 * Base de datos local de instituciones educativas colombianas
 * Para usar como respaldo cuando no hay conexión a internet o falla la IA
 */
object LocalInstitutionsData {

    private val institutions = listOf(
        // SANTANDER (Enfoque especial en tu región)
        InstitutionSuggestion("Instituto Técnico Santo Tomás", "Zapatoca", "Santander", "Público", "Media Técnica", 1.0f),
        InstitutionSuggestion("Colegio Provincial San José", "Bucaramanga", "Santander", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Universidad Industrial de Santander (UIS)", "Bucaramanga", "Santander", "Público", "Superior", 1.0f),
        InstitutionSuggestion("Colegio Santander", "Bucaramanga", "Santander", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("INEM Custodio García Rovira", "Bucaramanga", "Santander", "Público", "Media Técnica", 1.0f),
        InstitutionSuggestion("Colegio Americano", "Bucaramanga", "Santander", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Instituto Técnico Salesiano Eloy Valenzuela", "Bucaramanga", "Santander", "Privado", "Media Técnica", 1.0f),
        InstitutionSuggestion("Normal Superior María Auxiliadora", "Copacabana", "Santander", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Colegio Cooperativo San Antonio de Padua", "Bucaramanga", "Santander", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Instituto Técnico Mercedes Abrego", "Cúcuta", "Norte de Santander", "Público", "Media Técnica", 1.0f),

        // BOGOTÁ - CUNDINAMARCA
        InstitutionSuggestion("Colegio San Carlos", "Bogotá", "Cundinamarca", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Gimnasio Moderno", "Bogotá", "Cundinamarca", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Colegio Mayor de San Bartolomé", "Bogotá", "Cundinamarca", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Liceo Femenino Mercedes Nariño", "Bogotá", "Cundinamarca", "Público", "Básica y Media", 1.0f),
        InstitutionSuggestion("Colegio San Patricio", "Bogotá", "Cundinamarca", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Instituto Técnico Central La Salle", "Bogotá", "Cundinamarca", "Privado", "Media Técnica", 1.0f),
        InstitutionSuggestion("Colegio Rochester", "Bogotá", "Cundinamarca", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("IED Francisco José de Caldas", "Bogotá", "Cundinamarca", "Público", "Básica y Media", 1.0f),
        InstitutionSuggestion("Colegio Anglo Colombiano", "Bogotá", "Cundinamarca", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Colegio Nueva Granada", "Bogotá", "Cundinamarca", "Privado", "Básica y Media", 1.0f),

        // ANTIOQUIA
        InstitutionSuggestion("INEM José Félix de Restrepo", "Medellín", "Antioquia", "Público", "Media Técnica", 1.0f),
        InstitutionSuggestion("Instituto Técnico Industrial Pascual Bravo", "Medellín", "Antioquia", "Público", "Media Técnica", 1.0f),
        InstitutionSuggestion("Colegio San José de Las Vegas", "Medellín", "Antioquia", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Universidad de Antioquia", "Medellín", "Antioquia", "Público", "Superior", 1.0f),
        InstitutionSuggestion("Colegio Calasanz", "Medellín", "Antioquia", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Colegio Marymount", "Medellín", "Antioquia", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Colegio La Salle Envigado", "Envigado", "Antioquia", "Privado", "Básica y Media", 1.0f),

        // VALLE DEL CAUCA
        InstitutionSuggestion("Colegio Santa Librada", "Cali", "Valle del Cauca", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Instituto Técnico Industrial Antonio José Camacho", "Cali", "Valle del Cauca", "Público", "Media Técnica", 1.0f),
        InstitutionSuggestion("Colegio Bolivar", "Cali", "Valle del Cauca", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Universidad del Valle", "Cali", "Valle del Cauca", "Público", "Superior", 1.0f),
        InstitutionSuggestion("Colegio Bennett", "Cali", "Valle del Cauca", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Colegio Jefferson", "Cali", "Valle del Cauca", "Privado", "Básica y Media", 1.0f),

        // ATLÁNTICO
        InstitutionSuggestion("Colegio San José", "Barranquilla", "Atlántico", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Instituto Técnico Industrial", "Barranquilla", "Atlántico", "Público", "Media Técnica", 1.0f),
        InstitutionSuggestion("Colegio Americano", "Barranquilla", "Atlántico", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Universidad del Norte", "Barranquilla", "Atlántico", "Privado", "Superior", 1.0f),
        InstitutionSuggestion("Colegio Karl C. Parrish", "Barranquilla", "Atlántico", "Privado", "Básica y Media", 1.0f),

        // BOLÍVAR
        InstitutionSuggestion("Colegio Naval Almirante Colón", "Cartagena", "Bolívar", "Público", "Básica y Media", 1.0f),
        InstitutionSuggestion("Universidad de Cartagena", "Cartagena", "Bolívar", "Público", "Superior", 1.0f),
        InstitutionSuggestion("Colegio La Consolata", "Cartagena", "Bolívar", "Privado", "Básica y Media", 1.0f),

        // HUILA
        InstitutionSuggestion("Colegio Nacional José Eusebio Caro", "Neiva", "Huila", "Público", "Básica y Media", 1.0f),
        InstitutionSuggestion("Universidad Surcolombiana", "Neiva", "Huila", "Público", "Superior", 1.0f),

        // TOLIMA
        InstitutionSuggestion("Colegio San Simón", "Ibagué", "Tolima", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Universidad del Tolima", "Ibagué", "Tolima", "Público", "Superior", 1.0f),

        // CALDAS
        InstitutionSuggestion("Colegio de la UPB", "Manizales", "Caldas", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Universidad de Caldas", "Manizales", "Caldas", "Público", "Superior", 1.0f),

        // NARIÑO
        InstitutionSuggestion("Colegio San Felipe Neri", "Pasto", "Nariño", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Universidad de Nariño", "Pasto", "Nariño", "Público", "Superior", 1.0f),

        // RISARALDA
        InstitutionSuggestion("Colegio Deogracias Cardona", "Pereira", "Risaralda", "Público", "Básica y Media", 1.0f),
        InstitutionSuggestion("Universidad Tecnológica de Pereira", "Pereira", "Risaralda", "Público", "Superior", 1.0f),

        // QUINDIO
        InstitutionSuggestion("Colegio La Salle", "Armenia", "Quindío", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Universidad del Quindío", "Armenia", "Quindío", "Público", "Superior", 1.0f),

        // NORTE DE SANTANDER
        InstitutionSuggestion("Colegio Diocesano Juan XXIII", "Cúcuta", "Norte de Santander", "Privado", "Básica y Media", 1.0f),
        InstitutionSuggestion("Universidad Francisco de Paula Santander", "Cúcuta", "Norte de Santander", "Público", "Superior", 1.0f),

        // CAUCA
        InstitutionSuggestion("Universidad del Cauca", "Popayán", "Cauca", "Público", "Superior", 1.0f),
        InstitutionSuggestion("Colegio Mayor del Cauca", "Popayán", "Cauca", "Público", "Básica y Media", 1.0f),

        // BOYACÁ
        InstitutionSuggestion("Universidad Pedagógica y Tecnológica de Colombia", "Tunja", "Boyacá", "Público", "Superior", 1.0f),
        InstitutionSuggestion("Colegio de Boyacá", "Tunja", "Boyacá", "Público", "Básica y Media", 1.0f)
    )

    /**
     * Busca instituciones por nombre parcial
     */
    fun searchByName(query: String): List<InstitutionSuggestion> {
        if (query.length < 2) return emptyList()

        return institutions.filter { institution ->
            institution.fullName.contains(query, ignoreCase = true) ||
                    institution.municipality.contains(query, ignoreCase = true) ||
                    institution.department.contains(query, ignoreCase = true)
        }.sortedByDescending { institution ->
            // Dar mayor prioridad a coincidencias exactas en el nombre
            when {
                institution.fullName.startsWith(query, ignoreCase = true) -> 1.0f
                institution.fullName.contains(query, ignoreCase = true) -> 0.8f
                institution.municipality.contains(query, ignoreCase = true) -> 0.6f
                else -> 0.4f
            }
        }.take(5)
    }

    /**
     * Busca por municipio o departamento
     */
    fun searchByLocation(municipality: String?, department: String?): List<InstitutionSuggestion> {
        return institutions.filter { institution ->
            (municipality.isNullOrEmpty() || institution.municipality.equals(municipality, ignoreCase = true)) &&
                    (department.isNullOrEmpty() || institution.department.equals(department, ignoreCase = true))
        }
    }

    /**
     * Obtiene instituciones por sector (Público/Privado)
     */
    fun getInstitutionsBySector(sector: String): List<InstitutionSuggestion> {
        return institutions.filter { it.sector.equals(sector, ignoreCase = true) }
    }

    /**
     * Obtiene todas las instituciones de un departamento específico
     */
    fun getInstitutionsByDepartment(department: String): List<InstitutionSuggestion> {
        return institutions.filter { it.department.equals(department, ignoreCase = true) }
    }

    /**
     * Obtiene lista de departamentos únicos
     */
    fun getDepartments(): List<String> {
        return institutions.map { it.department }.distinct().sorted()
    }

    /**
     * Obtiene municipios de un departamento específico
     */
    fun getMunicipalitiesByDepartment(department: String): List<String> {
        return institutions
            .filter { it.department.equals(department, ignoreCase = true) }
            .map { it.municipality }
            .distinct()
            .sorted()
    }

    /**
     * Valida si una institución existe en la base de datos local
     */
    fun validateInstitution(name: String): InstitutionSuggestion? {
        return institutions.find {
            it.fullName.equals(name, ignoreCase = true) ||
                    // Búsqueda más flexible
                    it.fullName.contains(name, ignoreCase = true) && name.length > 5
        }
    }

    /**
     * Obtiene instituciones más populares (las primeras 10)
     */
    fun getPopularInstitutions(): List<InstitutionSuggestion> {
        return institutions.take(10)
    }

    /**
     * Función híbrida para InstitutionHelper como fallback
     */
    fun searchInstitutionFallback(query: String): List<InstitutionSuggestion> {
        val results = searchByName(query)

        // Si no hay resultados exactos, buscar por palabras clave
        if (results.isEmpty()) {
            val keywords = query.split(" ").filter { it.length > 2 }
            val keywordResults = mutableSetOf<InstitutionSuggestion>()

            keywords.forEach { keyword ->
                keywordResults.addAll(searchByName(keyword))
            }

            return keywordResults.toList().take(3)
        }

        return results
    }

    /**
     * Obtiene instituciones de Santander (para tu región específica)
     */
    fun getSantanderInstitutions(): List<InstitutionSuggestion> {
        return institutions.filter {
            it.department.equals("Santander", ignoreCase = true) ||
                    it.department.equals("Norte de Santander", ignoreCase = true)
        }
    }

    /**
     * Búsqueda inteligente que combina múltiples criterios
     */
    fun smartSearch(query: String): List<InstitutionSuggestion> {
        val directMatches = searchByName(query)
        if (directMatches.isNotEmpty()) return directMatches

        // Intentar búsqueda por palabras clave comunes
        val keywords = mapOf(
            "santo tomas" to "Instituto Técnico Santo Tomás",
            "inem" to "INEM",
            "salle" to "La Salle",
            "san carlos" to "Colegio San Carlos",
            "gimnasio moderno" to "Gimnasio Moderno",
            "americano" to "Americano",
            "salesiano" to "Salesiano"
        )

        keywords.forEach { (keyword, fullName) ->
            if (query.contains(keyword, ignoreCase = true)) {
                val matches = institutions.filter {
                    it.fullName.contains(fullName, ignoreCase = true)
                }
                if (matches.isNotEmpty()) return matches
            }
        }

        return emptyList()
    }
}