package com.charlesdev.icfes.student.logout



/**
 * Data class para manejar la información de instituciones educativas
 */
data class InstitutionData(
    val name: String = "",
    val municipality: String = "",
    val department: String = "",
    val sector: String = "", // "Público" o "Privado"
    val level: String = "", // "Básica y Media", "Media Técnica", etc.
    val isValidated: Boolean = false
)