package com.charlesdev.icfes.student.evaluation

// ===================================
// 📁 ICFESEvaluationQuestionBank.kt
// ===================================



import com.charlesdev.icfes.student.data.*

/**
 * Banco de preguntas específico para evaluaciones
 * Diferente del banco de práctica para evitar memorización
 */
class ICFESEvaluationQuestionBank {

    // ✅ PREGUNTAS DE EVALUACIÓN - LECTURA CRÍTICA
    private val evaluationLecturaQuestions = listOf(
        ICFESQuestion(
            id = "EVAL_LC001",
            type = ICFESQuestionType.READING_COMPREHENSION,
            context = """
            La inteligencia emocional se define como la capacidad de reconocer, comprender y gestionar nuestras propias emociones, así como de percibir y responder adecuadamente a las emociones de otros. Esta habilidad, que durante mucho tiempo fue subestimada en comparación con la inteligencia cognitiva tradicional, ha demostrado ser fundamental para el éxito personal y profesional.

            Investigaciones recientes sugieren que las personas con alta inteligencia emocional tienden a tener mejores relaciones interpersonales, mayor capacidad de liderazgo y mejor salud mental. Además, en el ámbito laboral, se ha observado que los empleados con estas características suelen adaptarse mejor a los cambios y mostrar mayor resilencia ante situaciones de estrés.

            Sin embargo, desarrollar la inteligencia emocional requiere práctica consciente y reflexión constante. No es una habilidad que se adquiera de forma natural, sino que debe cultivarse a través de ejercicios específicos como la meditación, la autorreflexión y la práctica de la empatía.
            """.trimIndent(),
            question = "Según el texto, ¿cuál es la característica más importante de la inteligencia emocional en el ámbito laboral?",
            options = listOf(
                "A) Mejora las capacidades cognitivas tradicionales",
                "B) Facilita la adaptación al cambio y la resiliencia",
                "C) Permite obtener mejores salarios",
                "D) Garantiza ascensos rápidos en la empresa"
            ),
            correctAnswer = "B",
            competency = "Comprensión inferencial",
            difficulty = Difficulty.MEDIO,
            explanation = "El texto específicamente menciona que en el ámbito laboral, las personas con inteligencia emocional 'suelen adaptarse mejor a los cambios y mostrar mayor resilencia ante situaciones de estrés'.",
            timeEstimated = 120,
            feedback = ICFESFeedback(
                correct = "Excelente. Has identificado correctamente la característica clave mencionada en el texto.",
                incorrect = mapOf(
                    "A" to "El texto no dice que mejore las capacidades cognitivas, sino que es diferente a ellas",
                    "C" to "Los salarios no se mencionan en el texto",
                    "D" to "Los ascensos no se mencionan específicamente"
                ),
                tip = "Busca la información específica sobre el ámbito laboral en el segundo párrafo.",
                relatedTopics = listOf("Comprensión inferencial", "Lectura de textos informativos")
            ),
            tags = listOf("inteligencia_emocional", "texto_informativo", "evaluacion")
        ),

        ICFESQuestion(
            id = "EVAL_LC002",
            type = ICFESQuestionType.READING_COMPREHENSION,
            context = """
            El fenómeno del cambio climático representa uno de los desafíos más complejos que enfrenta la humanidad en el siglo XXI. Las evidencias científicas indican que las actividades humanas, particularmente la emisión de gases de efecto invernadero, han alterado significativamente los patrones climáticos globales.

            Los efectos de este cambio se manifiestan de diversas formas: el aumento del nivel del mar, la intensificación de fenómenos meteorológicos extremos, y cambios en los ecosistemas que afectan tanto a la biodiversidad como a las actividades humanas. Estas transformaciones no solo tienen implicaciones ambientales, sino también económicas, sociales y políticas.

            La respuesta a este desafío requiere una acción coordinada a nivel global, que incluya tanto la mitigación de las causas como la adaptación a los efectos ya inevitables. Sin embargo, la implementación de estas medidas enfrenta obstáculos significativos, incluyendo intereses económicos contrapuestos y diferencias en las capacidades de respuesta entre países desarrollados y en desarrollo.
            """.trimIndent(),
            question = "¿Cuál es la tesis principal que defiende el autor del texto?",
            options = listOf(
                "A) El cambio climático es un problema exclusivamente ambiental",
                "B) Solo los países desarrollados pueden combatir el cambio climático",
                "C) El cambio climático es un desafío multidimensional que requiere acción global coordinada",
                "D) Los efectos del cambio climático son todavía reversibles"
            ),
            correctAnswer = "C",
            competency = "Pensamiento crítico",
            difficulty = Difficulty.DIFICIL,
            explanation = "La tesis principal se desarrolla a lo largo del texto: el cambio climático es complejo, tiene múltiples efectos (ambientales, económicos, sociales, políticos) y requiere acción global coordinada.",
            timeEstimated = 150,
            feedback = ICFESFeedback(
                correct = "Perfecto. Has identificado la tesis principal que se desarrolla a lo largo de todo el texto.",
                incorrect = mapOf(
                    "A" to "El texto claramente menciona implicaciones económicas, sociales y políticas, no solo ambientales",
                    "B" to "El texto menciona diferencias entre países pero no dice que solo los desarrollados puedan actuar",
                    "D" to "El texto habla de 'efectos ya inevitables', sugiriendo que no todo es reversible"
                ),
                tip = "Identifica la idea central que conecta todos los párrafos del texto.",
                relatedTopics = listOf("Pensamiento crítico", "Identificación de tesis", "Textos argumentativos")
            ),
            tags = listOf("cambio_climatico", "texto_argumentativo", "evaluacion")
        )
    )

    // ✅ PREGUNTAS DE EVALUACIÓN - MATEMÁTICAS
    private val evaluationMathQuestions = listOf(
        ICFESQuestion(
            id = "EVAL_MAT001",
            type = ICFESQuestionType.MATHEMATICAL_PROBLEM,
            question = "Si log₂(x) = 3, ¿cuál es el valor de x?",
            options = listOf(
                "A) 6",
                "B) 8",
                "C) 9",
                "D) 12"
            ),
            correctAnswer = "B",
            competency = "Álgebra y funciones",
            difficulty = Difficulty.MEDIO,
            explanation = "Si log₂(x) = 3, entonces 2³ = x, por lo tanto x = 8.",
            timeEstimated = 90,
            feedback = ICFESFeedback(
                correct = "Correcto. Has aplicado bien la definición de logaritmo.",
                incorrect = mapOf(
                    "A" to "Recuerda que log₂(x) = 3 significa 2³ = x, no 2 × 3 = x",
                    "C" to "Eso sería 3², no 2³",
                    "D" to "Verifica: 2³ = 2 × 2 × 2 = 8, no 12"
                ),
                tip = "Usa la definición: si log_a(x) = b, entonces a^b = x",
                relatedTopics = listOf("Logaritmos", "Exponentes", "Ecuaciones logarítmicas")
            ),
            tags = listOf("logaritmos", "algebra", "evaluacion")
        ),

        ICFESQuestion(
            id = "EVAL_MAT002",
            type = ICFESQuestionType.MATHEMATICAL_PROBLEM,
            question = "En una progresión aritmética, el primer término es 5 y la diferencia común es 3. ¿Cuál es el décimo término?",
            options = listOf(
                "A) 32",
                "B) 35",
                "C) 38",
                "D) 41"
            ),
            correctAnswer = "A",
            competency = "Álgebra y funciones",
            difficulty = Difficulty.MEDIO,
            explanation = "En una progresión aritmética: aₙ = a₁ + (n-1)d. Entonces a₁₀ = 5 + (10-1)×3 = 5 + 27 = 32",
            timeEstimated = 100,
            feedback = ICFESFeedback(
                correct = "Excelente. Has aplicado correctamente la fórmula de progresiones aritméticas.",
                incorrect = mapOf(
                    "B" to "Parece que calculaste a₁₁ en lugar de a₁₀",
                    "C" to "Revisa el cálculo: (n-1) = (10-1) = 9, no 11",
                    "D" to "Verifica la fórmula: aₙ = a₁ + (n-1)d"
                ),
                tip = "Usa la fórmula aₙ = a₁ + (n-1)d donde n es la posición del término.",
                relatedTopics = listOf("Progresiones aritméticas", "Secuencias", "Álgebra")
            ),
            tags = listOf("progresiones", "secuencias", "evaluacion")
        )
    )

    // ✅ PREGUNTAS DE EVALUACIÓN - CIENCIAS NATURALES
    private val evaluationCienciasQuestions = listOf(
        ICFESQuestion(
            id = "EVAL_CN001",
            type = ICFESQuestionType.SCIENTIFIC_ANALYSIS,
            question = "¿Cuál es la función principal de las mitocondrias en las células eucariotas?",
            options = listOf(
                "A) Sintetizar proteínas",
                "B) Producir ATP mediante respiración celular",
                "C) Almacenar información genética",
                "D) Transportar sustancias"
            ),
            correctAnswer = "B",
            competency = "Biología celular",
            difficulty = Difficulty.FACIL,
            explanation = "Las mitocondrias son los orgánulos responsables de la producción de ATP (energía) mediante el proceso de respiración celular.",
            timeEstimated = 60,
            feedback = ICFESFeedback(
                correct = "Correcto. Las mitocondrias son las 'centrales energéticas' de la célula.",
                incorrect = mapOf(
                    "A" to "La síntesis de proteínas es función de los ribosomas",
                    "C" to "El ADN se almacena principalmente en el núcleo",
                    "D" to "El transporte es función del retículo endoplasmático y aparato de Golgi"
                ),
                tip = "Recuerda que las mitocondrias son conocidas como las 'centrales energéticas' de la célula.",
                relatedTopics = listOf("Mitocondrias", "Respiración celular", "Biología celular")
            ),
            tags = listOf("mitocondrias", "celula", "evaluacion")
        ),

        ICFESQuestion(
            id = "EVAL_CN002",
            type = ICFESQuestionType.MATHEMATICAL_PROBLEM,
            question = "Un móvil viaja a velocidad constante de 15 m/s. ¿Qué distancia recorre en 8 segundos?",
            options = listOf(
                "A) 120 m",
                "B) 100 m",
                "C) 90 m",
                "D) 150 m"
            ),
            correctAnswer = "A",
            competency = "Física mecánica",
            difficulty = Difficulty.FACIL,
            explanation = "Usando la fórmula d = v × t: d = 15 m/s × 8 s = 120 m",
            timeEstimated = 60,
            feedback = ICFESFeedback(
                correct = "Perfecto. Has aplicado correctamente la fórmula de movimiento rectilíneo uniforme.",
                incorrect = mapOf(
                    "B" to "Verifica el cálculo: 15 × 8 = 120, no 100",
                    "C" to "Revisa: 15 × 8 = 120 m",
                    "D" to "Cuidado con la operación: 15 × 8 = 120, no 150"
                ),
                tip = "Para movimiento rectilíneo uniforme: distancia = velocidad × tiempo",
                relatedTopics = listOf("Movimiento rectilíneo uniforme", "Cinemática", "Física básica")
            ),
            tags = listOf("cinematica", "movimiento", "evaluacion")
        )
    )

    // ✅ PREGUNTAS DE EVALUACIÓN - SOCIALES Y CIUDADANAS
    private val evaluationSocialesQuestions = listOf(
        ICFESQuestion(
            id = "EVAL_SC001",
            type = ICFESQuestionType.SOCIAL_CONTEXT,
            context = """
            La participación ciudadana en Colombia se ha fortalecido a partir de la Constitución de 1991, que estableció diversos mecanismos de participación democrática como el referendo, el plebiscito, la consulta popular y la iniciativa popular legislativa. Estos instrumentos buscan ampliar la democracia más allá del simple ejercicio del voto cada cuatro años.
            """.trimIndent(),
            question = "¿Cuál es el propósito principal de los mecanismos de participación ciudadana establecidos en la Constitución de 1991?",
            options = listOf(
                "A) Reemplazar completamente el sistema electoral tradicional",
                "B) Ampliar la democracia más allá del voto periódico",
                "C) Reducir el poder del Congreso de la República",
                "D) Eliminar los partidos políticos tradicionales"
            ),
            correctAnswer = "B",
            competency = "Constitución y democracia",
            difficulty = Difficulty.MEDIO,
            explanation = "El texto claramente establece que estos mecanismos 'buscan ampliar la democracia más allá del simple ejercicio del voto cada cuatro años'.",
            timeEstimated = 100,
            feedback = ICFESFeedback(
                correct = "Excelente comprensión de los mecanismos de participación ciudadana.",
                incorrect = mapOf(
                    "A" to "No buscan reemplazar el sistema electoral, sino complementarlo",
                    "C" to "No se menciona reducir el poder del Congreso",
                    "D" to "No se refieren a eliminar partidos políticos"
                ),
                tip = "Enfócate en la frase clave sobre 'ampliar la democracia más allá del voto'.",
                relatedTopics = listOf("Participación ciudadana", "Constitución de 1991", "Democracia")
            ),
            tags = listOf("participacion_ciudadana", "constitucion", "evaluacion")
        ),

        ICFESQuestion(
            id = "EVAL_SC002",
            type = ICFESQuestionType.SOCIAL_CONTEXT,
            question = "¿Cuál fue una de las principales transformaciones sociales durante la Revolución Industrial en Europa?",
            options = listOf(
                "A) El fortalecimiento del sistema feudal",
                "B) La migración masiva del campo a la ciudad",
                "C) La disminución de la población europea",
                "D) El retorno a la economía agrícola tradicional"
            ),
            correctAnswer = "B",
            competency = "Historia y cultura",
            difficulty = Difficulty.FACIL,
            explanation = "La Revolución Industrial generó una migración masiva de población rural hacia las ciudades industriales en busca de trabajo en las fábricas.",
            timeEstimated = 80,
            feedback = ICFESFeedback(
                correct = "Correcto. La urbanización fue una transformación clave de la Revolución Industrial.",
                incorrect = mapOf(
                    "A" to "La Revolución Industrial debilitó el feudalismo, no lo fortaleció",
                    "C" to "La población europea creció durante este período",
                    "D" to "Al contrario, se desarrolló la industria sobre la agricultura"
                ),
                tip = "Piensa en cómo las fábricas cambiaron el patrón de asentamiento de la población.",
                relatedTopics = listOf("Revolución Industrial", "Urbanización", "Historia europea")
            ),
            tags = listOf("revolucion_industrial", "urbanizacion", "evaluacion")
        )
    )

    // ✅ PREGUNTAS DE EVALUACIÓN - INGLÉS
    private val evaluationEnglishQuestions = listOf(
        ICFESQuestion(
            id = "EVAL_ENG001",
            type = ICFESQuestionType.ENGLISH_READING,
            context = """
            Renewable energy sources have become increasingly important in the fight against climate change. Solar and wind power, in particular, have experienced dramatic cost reductions over the past decade, making them competitive with traditional fossil fuels. Many countries are now setting ambitious targets to transition to cleaner energy systems.

            However, the transition to renewable energy faces several challenges. Energy storage remains expensive, and the intermittent nature of solar and wind power requires backup systems. Additionally, existing infrastructure needs significant upgrades to accommodate these new energy sources.
            """.trimIndent(),
            question = "According to the text, what is the main challenge mentioned for renewable energy?",
            options = listOf(
                "A) High costs compared to fossil fuels",
                "B) Lack of government support",
                "C) Intermittent nature and storage issues",
                "D) Environmental concerns"
            ),
            correctAnswer = "C",
            competency = "Reading comprehension",
            difficulty = Difficulty.MEDIO,
            explanation = "The text specifically mentions that 'Energy storage remains expensive, and the intermittent nature of solar and wind power requires backup systems.'",
            timeEstimated = 100,
            feedback = ICFESFeedback(
                correct = "Excellent! You correctly identified the main challenges mentioned in the text.",
                incorrect = mapOf(
                    "A" to "The text says they have become competitive with fossil fuels",
                    "B" to "Government support is not mentioned as a challenge",
                    "D" to "Environmental concerns are not mentioned as challenges for renewables"
                ),
                tip = "Look for the paragraph that discusses challenges and obstacles.",
                relatedTopics = listOf("Reading comprehension", "Renewable energy vocabulary", "Text analysis")
            ),
            tags = listOf("english", "renewable_energy", "evaluacion")
        ),

        ICFESQuestion(
            id = "EVAL_ENG002",
            type = ICFESQuestionType.ENGLISH_READING,
            question = "Choose the correct form: 'By the time you arrive, we _____ dinner.'",
            options = listOf(
                "A) will finish",
                "B) will have finished",
                "C) finish",
                "D) are finishing"
            ),
            correctAnswer = "B",
            competency = "Use of English",
            difficulty = Difficulty.MEDIO,
            explanation = "This is a future perfect tense: 'By the time' + present simple, + future perfect. The action of finishing dinner will be completed before the arrival.",
            timeEstimated = 60,
            feedback = ICFESFeedback(
                correct = "Perfect! You correctly used the future perfect tense.",
                incorrect = mapOf(
                    "A" to "Simple future doesn't show completion before the arrival",
                    "C" to "Present simple doesn't fit the future time reference",
                    "D" to "Present continuous doesn't show future completion"
                ),
                tip = "Use future perfect (will have + past participle) for actions completed before a future time.",
                relatedTopics = listOf("Future perfect tense", "Time expressions", "Grammar")
            ),
            tags = listOf("english", "grammar", "future_perfect", "evaluacion")
        )
    )

    /**
     * ✅ MÉTODO PRINCIPAL PARA OBTENER PREGUNTAS DE EVALUACIÓN
     */
    fun getEvaluationQuestions(moduleId: String, totalQuestions: Int = 20): List<ICFESQuestion> {
        val moduleQuestions = when (moduleId) {
            "lectura_critica" -> evaluationLecturaQuestions +
                    generateAdditionalLecturaQuestions(totalQuestions - evaluationLecturaQuestions.size)
            "matematicas" -> evaluationMathQuestions +
                    generateAdditionalMathQuestions(totalQuestions - evaluationMathQuestions.size)
            "ciencias_naturales" -> evaluationCienciasQuestions +
                    generateAdditionalCienciasQuestions(totalQuestions - evaluationCienciasQuestions.size)
            "sociales_ciudadanas" -> evaluationSocialesQuestions +
                    generateAdditionalSocialesQuestions(totalQuestions - evaluationSocialesQuestions.size)
            "ingles" -> evaluationEnglishQuestions +
                    generateAdditionalEnglishQuestions(totalQuestions - evaluationEnglishQuestions.size)
            else -> emptyList()
        }

        // ✅ MEZCLAR Y TOMAR SOLO LAS NECESARIAS
        return moduleQuestions.shuffled().take(totalQuestions)
    }

    /**
     * ✅ GENERADORES DE PREGUNTAS ADICIONALES (SI SE NECESITAN MÁS)
     */
    private fun generateAdditionalLecturaQuestions(needed: Int): List<ICFESQuestion> {
        if (needed <= 0) return emptyList()

        // Aquí puedes agregar más preguntas o reutilizar con variaciones
        return evaluationLecturaQuestions.shuffled().take(needed)
    }

    private fun generateAdditionalMathQuestions(needed: Int): List<ICFESQuestion> {
        if (needed <= 0) return emptyList()
        return evaluationMathQuestions.shuffled().take(needed)
    }

    private fun generateAdditionalCienciasQuestions(needed: Int): List<ICFESQuestion> {
        if (needed <= 0) return emptyList()
        return evaluationCienciasQuestions.shuffled().take(needed)
    }

    private fun generateAdditionalSocialesQuestions(needed: Int): List<ICFESQuestion> {
        if (needed <= 0) return emptyList()
        return evaluationSocialesQuestions.shuffled().take(needed)
    }

    private fun generateAdditionalEnglishQuestions(needed: Int): List<ICFESQuestion> {
        if (needed <= 0) return emptyList()
        return evaluationEnglishQuestions.shuffled().take(needed)
    }

    /**
     * ✅ OBTENER PREGUNTAS POR DIFICULTAD (PARA EVALUACIONES ADAPTATIVAS)
     */
    fun getQuestionsByDifficulty(moduleId: String, difficulty: Difficulty, count: Int): List<ICFESQuestion> {
        return getEvaluationQuestions(moduleId, 50)
            .filter { it.difficulty == difficulty }
            .shuffled()
            .take(count)
    }

    /**
     * ✅ OBTENER PREGUNTAS POR COMPETENCIA
     */
    fun getQuestionsByCompetency(moduleId: String, competency: String, count: Int): List<ICFESQuestion> {
        return getEvaluationQuestions(moduleId, 50)
            .filter { it.competency.contains(competency, ignoreCase = true) }
            .shuffled()
            .take(count)
    }

    /**
     * ✅ VERIFICAR DISPONIBILIDAD DE PREGUNTAS
     */
    fun getAvailableQuestionsCount(moduleId: String): Int {
        return when (moduleId) {
            "lectura_critica" -> evaluationLecturaQuestions.size
            "matematicas" -> evaluationMathQuestions.size
            "ciencias_naturales" -> evaluationCienciasQuestions.size
            "sociales_ciudadanas" -> evaluationSocialesQuestions.size
            "ingles" -> evaluationEnglishQuestions.size
            else -> 0
        }
    }
}