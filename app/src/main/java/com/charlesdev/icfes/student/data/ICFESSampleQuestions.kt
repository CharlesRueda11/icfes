package com.charlesdev.icfes.student.data


//Comenzar practica
// ✅ PREGUNTAS DE LECTURA CRÍTICA
val lecturaComprehensionQuestions = listOf(
        ICFESQuestion(
            id = "LC001",
            type = ICFESQuestionType.READING_COMPREHENSION,
            context = """
        El concepto de desarrollo sostenible ha evolucionado significativamente desde su primera formulación en 1987 por la Comisión Brundtland. Inicialmente definido como "el desarrollo que satisface las necesidades del presente sin comprometer la capacidad de las generaciones futuras para satisfacer sus propias necesidades", este concepto ha sido objeto de múltiples interpretaciones y aplicaciones.

        En la actualidad, el desarrollo sostenible se entiende como un paradigma que integra tres dimensiones fundamentales: la económica, la social y la ambiental. Esta visión holística reconoce que el crecimiento económico, la equidad social y la protección del medio ambiente son aspectos interdependientes que deben ser considerados simultáneamente en cualquier estrategia de desarrollo.

        Sin embargo, la implementación práctica del desarrollo sostenible enfrenta numerosos desafíos. Entre estos se encuentran la dificultad para medir el progreso de manera integral, la resistencia al cambio por parte de actores económicos tradicionales, y la necesidad de coordinación entre diferentes niveles de gobierno y sectores de la sociedad.
        """.trimIndent(),
            question = "Según el texto, ¿cuál es la principal característica del concepto actual de desarrollo sostenible?",
            options = listOf(
                "A) Se centra exclusivamente en el crecimiento económico",
                "B) Integra dimensiones económicas, sociales y ambientales",
                "C) Prioriza la protección del medio ambiente sobre otros aspectos",
                "D) Mantiene la definición original de la Comisión Brundtland"
            ),
            correctAnswer = "B",
            competency = "Comprensión inferencial",
            difficulty = Difficulty.MEDIO,
            explanation = "El texto explica que actualmente el desarrollo sostenible se entiende como un paradigma holístico que integra tres dimensiones: económica, social y ambiental.",
            timeEstimated = 120,
            feedback = ICFESFeedback(
                correct = "Excelente comprensión del texto. Has identificado correctamente la característica principal del concepto actual de desarrollo sostenible.",
                incorrect = mapOf(
                    "A" to "Incorrecto. El texto señala que la visión actual va más allá del crecimiento económico.",
                    "C" to "Incorrecto. El texto enfatiza la integración de las tres dimensiones, no la priorización de una sobre otras.",
                    "D" to "Incorrecto. El texto indica que ha evolucionado desde la definición original."
                ),
                tip = "Busca en el texto la explicación sobre cómo se entiende 'en la actualidad' el desarrollo sostenible.",
                relatedTopics = listOf("Comprensión inferencial", "Análisis de textos expositivos")
            ),
            tags = listOf("desarrollo_sostenible", "texto_expositivo", "medio_ambiente")
        ),

        ICFESQuestion(
            id = "LC002",
            type = ICFESQuestionType.READING_COMPREHENSION,
            context = """
        La inteligencia artificial (IA) ha comenzado a transformar radicalmente el mundo laboral. Mientras que algunas profesiones están siendo automatizadas, otras emergen como resultado de estos avances tecnológicos. Este fenómeno plantea importantes interrogantes sobre el futuro del trabajo y la necesidad de adaptación de los trabajadores.

        Los expertos coinciden en que la clave para navegar esta transición exitosamente radica en el desarrollo de habilidades que complementen, en lugar de competir con, las capacidades de la IA. Estas incluyen el pensamiento crítico, la creatividad, la inteligencia emocional y la capacidad de trabajo en equipo.

        Sin embargo, esta transformación no está exenta de desafíos. La brecha digital, la desigualdad en el acceso a la educación y la resistencia al cambio son factores que podrían agravar las disparidades sociales existentes.
        """.trimIndent(),
            question = "¿Cuál es la actitud del autor hacia la transformación laboral causada por la IA?",
            options = listOf(
                "A) Completamente optimista sobre sus beneficios",
                "B) Totalmente pesimista sobre sus consecuencias",
                "C) Equilibrada, reconociendo tanto oportunidades como desafíos",
                "D) Indiferente hacia el impacto en los trabajadores"
            ),
            correctAnswer = "C",
            competency = "Pensamiento crítico",
            difficulty = Difficulty.DIFICIL,
            explanation = "El autor presenta una visión equilibrada: reconoce las oportunidades (nuevas profesiones, habilidades complementarias) pero también los desafíos (brecha digital, desigualdad).",
            timeEstimated = 150,
            feedback = ICFESFeedback(
                correct = "Muy bien. Has identificado correctamente la actitud equilibrada del autor hacia la transformación laboral.",
                incorrect = mapOf(
                    "A" to "Aunque menciona aspectos positivos, también aborda desafíos importantes.",
                    "B" to "El autor no es pesimista; propone soluciones y ve oportunidades.",
                    "D" to "El autor muestra clara preocupación por el impacto en los trabajadores."
                ),
                tip = "Analiza cómo el autor presenta tanto aspectos positivos como negativos del tema.",
                relatedTopics = listOf("Pensamiento crítico", "Análisis de actitudes", "Textos argumentativos")
            ),
            tags = listOf("inteligencia_artificial", "mundo_laboral", "pensamiento_critico")
        )
    )

// ✅ PREGUNTAS DE MATEMÁTICAS
val mathematicsQuestions = listOf(
    ICFESQuestion(
        id = "MAT001",
        type = ICFESQuestionType.MATHEMATICAL_PROBLEM,
        question = "Si f(x) = 2x² - 3x + 1, ¿cuál es el valor de f(3)?",
        options = listOf(
            "A) 8",
            "B) 10",
            "C) 12",
            "D) 14"
        ),
        correctAnswer = "B",
        competency = "Álgebra y funciones",
        difficulty = Difficulty.FACIL,
        explanation = "Para encontrar f(3), sustituimos x = 3 en la función: f(3) = 2(3)² - 3(3) + 1 = 2(9) - 9 + 1 = 18 - 9 + 1 = 10",
        timeEstimated = 90,
        feedback = ICFESFeedback(
            correct = "Excelente. Has aplicado correctamente la sustitución en la función cuadrática.",
            incorrect = mapOf(
                "A" to "Revisa el cálculo: 2(9) - 9 + 1 = 18 - 9 + 1 = 10",
                "C" to "Parece que olvidaste sumar el término constante +1",
                "D" to "Verifica paso a paso: f(3) = 2(3)² - 3(3) + 1"
            ),
            tip = "Sustituye x = 3 en cada término de la función y opera paso a paso.",
            relatedTopics = listOf("Funciones cuadráticas", "Sustitución algebraica", "Operaciones básicas")
        ),
        tags = listOf("funciones", "algebra", "sustitucion")
    ),

    ICFESQuestion(
        id = "MAT002",
        type = ICFESQuestionType.MATHEMATICAL_PROBLEM,
        question = "En un triángulo rectángulo, un cateto mide 3 cm y la hipotenusa mide 5 cm. ¿Cuánto mide el otro cateto?",
        options = listOf(
            "A) 2 cm",
            "B) 3 cm",
            "C) 4 cm",
            "D) 6 cm"
        ),
        correctAnswer = "C",
        competency = "Geometría y medición",
        difficulty = Difficulty.MEDIO,
        explanation = "Usando el teorema de Pitágoras: a² + b² = c², donde c = 5 y a = 3. Entonces: 3² + b² = 5², 9 + b² = 25, b² = 16, b = 4 cm",
        timeEstimated = 120,
        feedback = ICFESFeedback(
            correct = "Perfecto. Has aplicado correctamente el teorema de Pitágoras.",
            incorrect = mapOf(
                "A" to "Revisa el cálculo: 3² + b² = 5² → 9 + b² = 25 → b² = 16 → b = 4",
                "B" to "Ese es el valor del cateto conocido, no del que buscamos",
                "D" to "Verifica: si b = 6, entonces 3² + 6² = 9 + 36 = 45 ≠ 25"
            ),
            tip = "Recuerda que a² + b² = c², donde c es la hipotenusa (el lado más largo).",
            relatedTopics = listOf("Teorema de Pitágoras", "Triángulos rectángulos", "Geometría básica")
        ),
        tags = listOf("geometria", "pitagoras", "triangulos")
    ),

    ICFESQuestion(
        id = "MAT003",
        type = ICFESQuestionType.MATHEMATICAL_PROBLEM,
        question = "En una encuesta a 100 estudiantes sobre su deporte favorito, 40 prefieren fútbol, 30 prefieren baloncesto y 20 prefieren tenis. El resto no practica deportes. ¿Cuál es la probabilidad de que un estudiante elegido al azar prefiera fútbol?",
        options = listOf(
            "A) 0.3",
            "B) 0.4",
            "C) 0.5",
            "D) 0.6"
        ),
        correctAnswer = "B",
        competency = "Estadística y probabilidad",
        difficulty = Difficulty.FACIL,
        explanation = "Probabilidad = casos favorables / casos totales = 40/100 = 0.4",
        timeEstimated = 60,
        feedback = ICFESFeedback(
            correct = "Correcto. Has calculado bien la probabilidad básica.",
            incorrect = mapOf(
                "A" to "Ese sería el resultado si fueran 30 estudiantes, no 40",
                "C" to "Revisa: son 40 estudiantes de fútbol entre 100 total",
                "D" to "Verifica los datos: 40 prefieren fútbol, no 60"
            ),
            tip = "Probabilidad = (número de estudiantes que prefieren fútbol) / (total de estudiantes)",
            relatedTopics = listOf("Probabilidad básica", "Estadística descriptiva", "Proporciones")
        ),
        tags = listOf("probabilidad", "estadistica", "proporciones")
    )
)

// ✅ PREGUNTAS DE CIENCIAS NATURALES
val cienciasNaturalesQuestions = listOf(
    ICFESQuestion(
        id = "CN001",
        type = ICFESQuestionType.SCIENTIFIC_ANALYSIS,
        question = "¿Cuál es la función principal del ribosoma en la célula?",
        options = listOf(
            "A) Producir ATP para la célula",
            "B) Sintetizar proteínas",
            "C) Almacenar material genético",
            "D) Controlar el transporte de sustancias"
        ),
        correctAnswer = "B",
        competency = "Biología celular",
        difficulty = Difficulty.FACIL,
        explanation = "Los ribosomas son orgánulos especializados en la síntesis de proteínas, traduciendo la información del ARN mensajero en cadenas de aminoácidos.",
        timeEstimated = 60,
        feedback = ICFESFeedback(
            correct = "Excelente. Has identificado correctamente la función del ribosoma.",
            incorrect = mapOf(
                "A" to "La producción de ATP es función de las mitocondrias, no de los ribosomas",
                "C" to "El material genético se almacena en el núcleo (ADN) y citoplasma (ARN)",
                "D" to "El control del transporte es función de la membrana celular"
            ),
            tip = "Recuerda que los ribosomas son las 'fábricas' de proteínas de la célula.",
            relatedTopics = listOf("Biología celular", "Orgánulos", "Síntesis de proteínas")
        ),
        tags = listOf("biologia", "celula", "ribosomas", "proteinas")
    ),

    ICFESQuestion(
        id = "CN002",
        type = ICFESQuestionType.MATHEMATICAL_PROBLEM,
        question = "Un objeto cae libremente desde una altura de 20 metros. Si g = 10 m/s², ¿cuánto tiempo tarda en llegar al suelo?",
        options = listOf(
            "A) 1 segundo",
            "B) 2 segundos",
            "C) 3 segundos",
            "D) 4 segundos"
        ),
        correctAnswer = "B",
        competency = "Física mecánica",
        difficulty = Difficulty.MEDIO,
        explanation = "Usando la ecuación h = ½gt², donde h = 20m y g = 10 m/s²: 20 = ½(10)t², 20 = 5t², t² = 4, t = 2 segundos",
        timeEstimated = 120,
        feedback = ICFESFeedback(
            correct = "Perfecto. Has aplicado correctamente la ecuación de caída libre.",
            incorrect = mapOf(
                "A" to "Verifica el cálculo: h = ½gt² → 20 = 5t² → t² = 4 → t = 2",
                "C" to "Revisa: si t = 3, entonces h = ½(10)(3)² = 45 m, no 20 m",
                "D" to "Comprueba: si t = 4, entonces h = ½(10)(4)² = 80 m, no 20 m"
            ),
            tip = "Usa la ecuación h = ½gt² para caída libre y despeja t.",
            relatedTopics = listOf("Caída libre", "Cinemática", "Ecuaciones del movimiento")
        ),
        tags = listOf("fisica", "caida_libre", "cinematica")
    ),

    ICFESQuestion(
        id = "CN003",
        type = ICFESQuestionType.SCIENTIFIC_ANALYSIS,
        question = "¿Cuál es el producto principal de la reacción: 2H₂ + O₂ → ?",
        options = listOf(
            "A) H₂O₂ (peróxido de hidrógeno)",
            "B) 2H₂O (agua)",
            "C) H₂O (agua)",
            "D) 2H₂O₂ (peróxido de hidrógeno)"
        ),
        correctAnswer = "B",
        competency = "Química inorgánica",
        difficulty = Difficulty.MEDIO,
        explanation = "La reacción balanceada es: 2H₂ + O₂ → 2H₂O. Se forman 2 moléculas de agua a partir de 2 moléculas de hidrógeno y 1 de oxígeno.",
        timeEstimated = 90,
        feedback = ICFESFeedback(
            correct = "Correcto. Has balanceado bien la ecuación química.",
            incorrect = mapOf(
                "A" to "El peróxido de hidrógeno requiere más oxígeno: H₂ + O₂ → H₂O₂",
                "C" to "Falta considerar el balanceo: 2H₂ requiere 2H₂O como producto",
                "D" to "Esta reacción no produce peróxido de hidrógeno"
            ),
            tip = "Balancea la ecuación contando los átomos de cada elemento en ambos lados.",
            relatedTopics = listOf("Balanceo de ecuaciones", "Reacciones químicas", "Estequiometría")
        ),
        tags = listOf("quimica", "reacciones", "balanceo")
    )
)

// ✅ PREGUNTAS DE SOCIALES Y CIUDADANAS
val socialesQuestions = listOf(
    ICFESQuestion(
        id = "SC001",
        type = ICFESQuestionType.SOCIAL_CONTEXT,
        context = """
        La Constitución de 1991 estableció a Colombia como un Estado social de derecho, lo que implica que el Estado debe garantizar no solo las libertades individuales sino también los derechos sociales, económicos y culturales de los ciudadanos. Esta transformación marcó un cambio significativo respecto a la constitución anterior de 1886.
        """.trimIndent(),
        question = "¿Cuál es la principal diferencia entre un Estado de derecho y un Estado social de derecho?",
        options = listOf(
            "A) El Estado social de derecho elimina las libertades individuales",
            "B) El Estado social de derecho incluye la garantía de derechos sociales y económicos",
            "C) El Estado de derecho es más democrático que el Estado social de derecho",
            "D) No hay diferencias significativas entre ambos conceptos"
        ),
        correctAnswer = "B",
        competency = "Constitución y democracia",
        difficulty = Difficulty.MEDIO,
        explanation = "El Estado social de derecho no solo protege las libertades individuales sino que también obliga al Estado a garantizar derechos sociales, económicos y culturales.",
        timeEstimated = 120,
        feedback = ICFESFeedback(
            correct = "Excelente comprensión del concepto de Estado social de derecho.",
            incorrect = mapOf(
                "A" to "El Estado social de derecho mantiene las libertades individuales y añade derechos sociales",
                "C" to "Ambos son formas democráticas; la diferencia está en el alcance de los derechos",
                "D" to "Hay diferencias importantes en cuanto a los derechos que garantizan"
            ),
            tip = "Piensa en qué derechos adicionales debe garantizar un Estado 'social' de derecho.",
            relatedTopics = listOf("Constitución de 1991", "Derechos fundamentales", "Estado social")
        ),
        tags = listOf("constitucion", "estado_social", "derechos")
    ),

    ICFESQuestion(
        id = "SC002",
        type = ICFESQuestionType.SOCIAL_CONTEXT,
        question = "¿Cuál fue una de las principales consecuencias de la Revolución Industrial en el siglo XIX?",
        options = listOf(
            "A) La disminución de la población urbana",
            "B) El fortalecimiento del sistema feudal",
            "C) La aparición de la clase obrera industrial",
            "D) La reducción de la producción manufacturera"
        ),
        correctAnswer = "C",
        competency = "Historia y cultura",
        difficulty = Difficulty.FACIL,
        explanation = "La Revolución Industrial transformó las estructuras sociales, creando una nueva clase social: los obreros industriales que trabajaban en las fábricas.",
        timeEstimated = 80,
        feedback = ICFESFeedback(
            correct = "Correcto. Has identificado una consecuencia social clave de la Revolución Industrial.",
            incorrect = mapOf(
                "A" to "La Revolución Industrial aumentó la población urbana debido a la migración del campo a la ciudad",
                "B" to "La Revolución Industrial debilitó el feudalismo, no lo fortaleció",
                "D" to "Al contrario, la producción manufacturera aumentó significativamente"
            ),
            tip = "Piensa en cómo cambió la estructura social con la creación de fábricas.",
            relatedTopics = listOf("Revolución Industrial", "Clases sociales", "Historia económica")
        ),
        tags = listOf("revolucion_industrial", "clase_obrera", "historia")
    )
)

// ✅ PREGUNTAS DE INGLÉS
val englishQuestions = listOf(
    ICFESQuestion(
        id = "ENG001",
        type = ICFESQuestionType.ENGLISH_READING,
        context = """
        Climate change is one of the most pressing challenges facing humanity today. Rising global temperatures, melting ice caps, and extreme weather events are clear signs that our planet is warming at an unprecedented rate. Scientists agree that human activities, particularly the burning of fossil fuels, are the primary cause of this crisis.

        However, there is hope. Renewable energy technologies such as solar and wind power are becoming more affordable and efficient. Many countries are investing heavily in clean energy infrastructure and implementing policies to reduce carbon emissions. Individual actions, such as using public transportation and reducing energy consumption, also play a crucial role in addressing this global challenge.
        """.trimIndent(),
        question = "According to the text, what is the primary cause of climate change?",
        options = listOf(
            "A) Natural weather patterns",
            "B) Solar radiation variations",
            "C) Human activities, especially burning fossil fuels",
            "D) Volcanic eruptions"
        ),
        correctAnswer = "C",
        competency = "Reading comprehension",
        difficulty = Difficulty.FACIL,
        explanation = "The text clearly states that 'scientists agree that human activities, particularly the burning of fossil fuels, are the primary cause of this crisis.'",
        timeEstimated = 90,
        feedback = ICFESFeedback(
            correct = "Excellent! You correctly identified the main cause mentioned in the text.",
            incorrect = mapOf(
                "A" to "The text doesn't mention natural weather patterns as the primary cause",
                "B" to "Solar radiation variations are not mentioned in the text",
                "D" to "Volcanic eruptions are not discussed in this passage"
            ),
            tip = "Look for the phrase 'primary cause' or similar expressions in the text.",
            relatedTopics = listOf("Reading comprehension", "Climate change vocabulary", "Cause and effect")
        ),
        tags = listOf("english", "reading", "climate_change")
    ),

    ICFESQuestion(
        id = "ENG002",
        type = ICFESQuestionType.ENGLISH_READING,
        question = "Choose the correct option to complete the sentence: 'If I _____ more time, I would learn to play the guitar.'",
        options = listOf(
            "A) have",
            "B) had",
            "C) will have",
            "D) would have"
        ),
        correctAnswer = "B",
        competency = "Use of English",
        difficulty = Difficulty.MEDIO,
        explanation = "This is a second conditional sentence (hypothetical situation). The structure is: If + past simple, would + infinitive.",
        timeEstimated = 60,
        feedback = ICFESFeedback(
            correct = "Perfect! You correctly identified the second conditional structure.",
            incorrect = mapOf(
                "A" to "This would be first conditional: If I have time, I will learn...",
                "C" to "This doesn't fit the conditional structure with 'would'",
                "D" to "This would create a third conditional, which doesn't fit the context"
            ),
            tip = "Remember: Second conditional = If + past simple, would + infinitive (for hypothetical situations).",
            relatedTopics = listOf("Conditional sentences", "Grammar", "Hypothetical situations")
        ),
        tags = listOf("english", "grammar", "conditionals")
    )
)

// ✅ FUNCIÓN PARA ASIGNAR PREGUNTAS A ÁREAS
fun populateModulesWithQuestions(): List<ICFESModule> {
    return icfesModules.map { module ->
        when (module.id) {
            "lectura_critica" -> module.copy(
                areas = module.areas.map { area ->
                    when (area.id) {
                        "comprension_literal" -> area.copy(questions = lecturaComprehensionQuestions.take(1))
                        "comprension_inferencial" -> area.copy(questions = lecturaComprehensionQuestions.drop(1).take(1))
                        "pensamiento_critico" -> area.copy(questions = lecturaComprehensionQuestions.take(1))
                        else -> area
                    }
                }
            )
            "matematicas" -> module.copy(
                areas = module.areas.map { area ->
                    when (area.id) {
                        "algebra_funciones" -> area.copy(questions = mathematicsQuestions.take(1))
                        "geometria_medicion" -> area.copy(questions = mathematicsQuestions.drop(1).take(1))
                        "estadistica_probabilidad" -> area.copy(questions = mathematicsQuestions.drop(2))
                        else -> area
                    }
                }
            )
            "ciencias_naturales" -> module.copy(
                areas = module.areas.map { area ->
                    when (area.id) {
                        "biologia" -> area.copy(questions = cienciasNaturalesQuestions.take(1))
                        "fisica" -> area.copy(questions = cienciasNaturalesQuestions.drop(1).take(1))
                        "quimica" -> area.copy(questions = cienciasNaturalesQuestions.drop(2))
                        else -> area
                    }
                }
            )
            "sociales_ciudadanas" -> module.copy(
                areas = module.areas.map { area ->
                    when (area.id) {
                        "historia_cultura" -> area.copy(questions = socialesQuestions.take(1))
                        "geografia_ambiente" -> area.copy(questions = socialesQuestions.drop(1).take(1))
                        "constitucion_democracia" -> area.copy(questions = socialesQuestions.drop(1))
                        else -> area
                    }
                }
            )
            "ingles" -> module.copy(
                areas = module.areas.map { area ->
                    when (area.id) {
                        "reading_comprehension" -> area.copy(questions = englishQuestions.take(1))
                        "use_of_english" -> area.copy(questions = englishQuestions.drop(1))
                        else -> area
                    }
                }
            )
            else -> module
        }
    }
}

// ✅ MÓDULOS POPULADOS CON PREGUNTAS
val populatedICFESModules = populateModulesWithQuestions()