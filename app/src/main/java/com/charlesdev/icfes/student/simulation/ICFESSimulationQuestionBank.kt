package com.charlesdev.icfes.student.simulation

import com.charlesdev.icfes.student.data.*

/**
 * ===================================
 * 📁 BANCO DE PREGUNTAS ESPECÍFICO DEL SIMULACRO COMPLETO
 * ===================================
 * Versión simplificada: 5 preguntas (1 por módulo) para pruebas
 */

class ICFESSimulationQuestionBank {

    // ✅ PREGUNTAS DE LECTURA CRÍTICA PARA SIMULACRO (4 preguntas base)
    private val simulationLecturaQuestions = listOf(
        ICFESQuestion(
            id = "SIM_LC001",
            type = ICFESQuestionType.READING_COMPREHENSION,
            context = """
            La revolución digital ha transformado radicalmente la forma en que los seres humanos interactúan, aprenden y construyen conocimiento. Las redes sociales, los motores de búsqueda y las plataformas de contenido han democratizado el acceso a la información, pero también han generado nuevos desafíos relacionados con la veracidad, la privacidad y la concentración del poder.

            En este nuevo ecosistema informativo, la capacidad de discernir entre información confiable y desinformación se ha convertido en una habilidad fundamental para la ciudadanía del siglo XXI. Los algoritmos que gobiernan estas plataformas no son neutrales; reflejan los sesgos de sus creadores y pueden amplificar divisiones sociales existentes.

            La educación debe evolucionar para preparar a las nuevas generaciones en el uso crítico de la tecnología, promoviendo tanto el aprovechamiento de sus beneficios como la comprensión de sus limitaciones y riesgos potenciales.
            """.trimIndent(),
            question = "¿Cuál es la tesis principal que desarrolla el autor en el texto?",
            options = listOf(
                "A) La tecnología digital solo tiene efectos negativos en la sociedad",
                "B) La revolución digital requiere nuevas competencias ciudadanas y educativas",
                "C) Los algoritmos deben ser eliminados de las plataformas digitales",
                "D) Las redes sociales han democratizado completamente la información"
            ),
            correctAnswer = "B",
            competency = "Pensamiento crítico",
            difficulty = Difficulty.MEDIO,
            explanation = "El autor presenta la transformación digital como un fenómeno que requiere nuevas habilidades críticas y adaptaciones educativas para aprovechar beneficios y mitigar riesgos.",
            timeEstimated = 120,
            feedback = ICFESFeedback(
                correct = "Excelente comprensión. Has identificado la tesis central del texto sobre la necesidad de adaptación educativa ante la revolución digital.",
                incorrect = mapOf(
                    "A" to "El texto presenta tanto beneficios como desafíos de la tecnología, no solo efectos negativos.",
                    "C" to "El autor no propone eliminar algoritmos, sino comprender sus sesgos.",
                    "D" to "El texto menciona democratización pero también señala problemas asociados."
                ),
                tip = "Busca la idea que conecta todos los párrafos y sintetiza la posición del autor.",
                relatedTopics = listOf("Lectura crítica", "Textos argumentativos", "Comprensión global")
            ),
            tags = listOf("tecnologia", "sociedad", "simulacro")
        ),

        ICFESQuestion(
            id = "SIM_LC002",
            type = ICFESQuestionType.READING_COMPREHENSION,
            context = """
            Los océanos absorben aproximadamente el 30% del dióxido de carbono producido por las actividades humanas, lo que ha llevado a un proceso conocido como acidificación oceánica. Este fenómeno, a menudo llamado "el otro problema del CO₂", tiene consecuencias profundas para los ecosistemas marinos.

            Cuando el CO₂ se disuelve en el agua de mar, forma ácido carbónico, reduciendo el pH del océano. Esta acidificación afecta particularmente a los organismos que construyen estructuras de carbonato de calcio, como corales, moluscos y algunos tipos de plancton. La degradación de estos organismos tiene efectos en cascada en toda la cadena alimentaria marina.

            Los arrecifes de coral, que albergan aproximadamente el 25% de todas las especies marinas, son especialmente vulnerables. Su deterioro no solo representa una pérdida de biodiversidad, sino también un impacto económico significativo para las comunidades costeras que dependen del turismo y la pesca.
            """.trimIndent(),
            question = "¿Qué relación causal establece el texto entre el CO₂ y los arrecifes de coral?",
            options = listOf(
                "A) El CO₂ directamente destruye los corales al contacto",
                "B) El CO₂ se disuelve, acidifica el océano y afecta las estructuras de carbonato",
                "C) Los corales producen más CO₂ del que pueden procesar",
                "D) El CO₂ aumenta la temperatura que derrite los corales"
            ),
            correctAnswer = "B",
            competency = "Comprensión inferencial",
            difficulty = Difficulty.MEDIO,
            explanation = "El texto explica la secuencia: CO₂ → ácido carbónico → reducción pH → afectación organismos con carbonato de calcio (como corales).",
            timeEstimated = 110,
            feedback = ICFESFeedback(
                correct = "Excelente. Has seguido correctamente la cadena causal descrita en el texto.",
                incorrect = mapOf(
                    "A" to "No es contacto directo, sino un proceso químico que cambia el pH.",
                    "C" to "Los corales no producen CO₂, son afectados por él.",
                    "D" to "El texto habla de acidificación, no de temperatura."
                ),
                tip = "Sigue paso a paso el proceso: disolución → acidificación → efecto en carbonatos.",
                relatedTopics = listOf("Relaciones causales", "Procesos científicos")
            ),
            tags = listOf("oceanos", "cambio_climatico", "simulacro")
        ),

        ICFESQuestion(
            id = "SIM_LC003",
            type = ICFESQuestionType.CRITICAL_THINKING,
            question = "¿Cuál de las siguientes afirmaciones representa un argumento válido?",
            options = listOf(
                "A) Todos los estudiantes exitosos estudian mucho; María estudia mucho, por tanto será exitosa",
                "B) Algunos políticos son corruptos; Juan es político, por tanto Juan es corrupto",
                "C) Ningún mamífero pone huevos; los murciélagos son mamíferos, por tanto no ponen huevos",
                "D) La mayoría de deportistas son altos; Pedro es alto, por tanto es deportista"
            ),
            correctAnswer = "C",
            competency = "Pensamiento crítico",
            difficulty = Difficulty.DIFICIL,
            explanation = "Esta es la única estructura lógica válida: premisa universal negativa + clasificación → conclusión necesaria.",
            timeEstimated = 100,
            feedback = ICFESFeedback(
                correct = "Excelente razonamiento lógico. Has identificado la única estructura argumentativa válida.",
                incorrect = mapOf(
                    "A" to "Confunde condición necesaria con suficiente. Estudiar mucho no garantiza éxito.",
                    "B" to "Falacia de generalización. 'Algunos' no permite conclusiones sobre casos individuales.",
                    "D" to "Afirma el consecuente. La mayoría no permite inferencias sobre casos individuales."
                ),
                tip = "Analiza la estructura lógica: ¿la conclusión se sigue necesariamente de las premisas?",
                relatedTopics = listOf("Lógica", "Argumentación", "Falacias")
            ),
            tags = listOf("logica", "argumentacion", "simulacro")
        )
    )

    // ✅ PREGUNTAS DE MATEMÁTICAS PARA SIMULACRO (3 preguntas base)
    private val simulationMathQuestions = listOf(
        ICFESQuestion(
            id = "SIM_MAT001",
            type = ICFESQuestionType.MATHEMATICAL_PROBLEM,
            question = "Si f(x) = 3x² - 2x + 1 y g(x) = x + 2, ¿cuál es el valor de (f∘g)(1)?",
            options = listOf(
                "A) 20",
                "B) 22",
                "C) 24",
                "D) 26"
            ),
            correctAnswer = "B",
            competency = "Álgebra y funciones",
            difficulty = Difficulty.MEDIO,
            explanation = "Primero g(1) = 1 + 2 = 3, luego f(3) = 3(3)² - 2(3) + 1 = 27 - 6 + 1 = 22",
            timeEstimated = 120,
            feedback = ICFESFeedback(
                correct = "Perfecto. Has aplicado correctamente la composición de funciones.",
                incorrect = mapOf(
                    "A" to "Revisa el cálculo: f(3) = 3(9) - 6 + 1 = 27 - 6 + 1 = 22",
                    "C" to "Posible error en la evaluación de f(3). Verifica paso a paso.",
                    "D" to "Revisa la composición: primero evalúa g(1), luego f con ese resultado."
                ),
                tip = "Para (f∘g)(x), primero evalúa g(x), luego usa ese resultado en f.",
                relatedTopics = listOf("Composición de funciones", "Evaluación de funciones")
            ),
            tags = listOf("funciones", "composicion", "simulacro")
        ),

        ICFESQuestion(
            id = "SIM_MAT002",
            type = ICFESQuestionType.MATHEMATICAL_PROBLEM,
            question = "Un cono tiene radio de la base 6 cm y altura 8 cm. ¿Cuál es su volumen?",
            options = listOf(
                "A) 96π cm³",
                "B) 144π cm³",
                "C) 192π cm³",
                "D) 288π cm³"
            ),
            correctAnswer = "A",
            competency = "Geometría y medición",
            difficulty = Difficulty.MEDIO,
            explanation = "V = (1/3)πr²h = (1/3)π(6)²(8) = (1/3)π(36)(8) = (1/3)π(288) = 96π cm³",
            timeEstimated = 100,
            feedback = ICFESFeedback(
                correct = "Excelente aplicación de la fórmula del volumen del cono.",
                incorrect = mapOf(
                    "B" to "Olvidaste dividir entre 3. La fórmula es V = (1/3)πr²h",
                    "C" to "Revisa la fórmula: es (1/3)πr²h, no (2/3)πr²h",
                    "D" to "Ese sería πr²h sin el factor 1/3. Recuerda la fórmula correcta."
                ),
                tip = "Volumen del cono = (1/3) × área de la base × altura",
                relatedTopics = listOf("Volumen", "Geometría 3D", "Cono")
            ),
            tags = listOf("geometria", "volumen", "simulacro")
        ),

        ICFESQuestion(
            id = "SIM_MAT003",
            type = ICFESQuestionType.MATHEMATICAL_PROBLEM,
            question = "En una distribución normal con media 100 y desviación estándar 15, ¿qué porcentaje de los datos está entre 85 y 115?",
            options = listOf(
                "A) 34%",
                "B) 68%",
                "C) 95%",
                "D) 99.7%"
            ),
            correctAnswer = "B",
            competency = "Estadística y probabilidad",
            difficulty = Difficulty.MEDIO,
            explanation = "Entre μ-σ y μ+σ (100-15=85 y 100+15=115) se encuentra aproximadamente 68% de los datos en una distribución normal.",
            timeEstimated = 90,
            feedback = ICFESFeedback(
                correct = "Correcto. Conoces bien la regla empírica de la distribución normal.",
                incorrect = mapOf(
                    "A" to "34% es solo de la media a una desviación estándar (un lado).",
                    "C" to "95% corresponde a dos desviaciones estándar (entre 70 y 130).",
                    "D" to "99.7% corresponde a tres desviaciones estándar."
                ),
                tip = "Regla 68-95-99.7: 68% dentro de 1σ, 95% dentro de 2σ, 99.7% dentro de 3σ",
                relatedTopics = listOf("Distribución normal", "Regla empírica", "Estadística")
            ),
            tags = listOf("estadistica", "distribucion_normal", "simulacro")
        )
    )

    // ✅ PREGUNTAS DE CIENCIAS NATURALES PARA SIMULACRO (3 preguntas base)
    private val simulationCienciasQuestions = listOf(
        ICFESQuestion(
            id = "SIM_CN001",
            type = ICFESQuestionType.SCIENTIFIC_ANALYSIS,
            question = "¿Cuál es la función principal del sistema linfático en el cuerpo humano?",
            options = listOf(
                "A) Transportar oxígeno a los tejidos",
                "B) Filtrar desechos metabólicos",
                "C) Defender el organismo contra infecciones",
                "D) Regular la temperatura corporal"
            ),
            correctAnswer = "C",
            competency = "Biología sistémica",
            difficulty = Difficulty.FACIL,
            explanation = "El sistema linfático es fundamental para la defensa inmunológica, produciendo y transportando linfocitos y anticuerpos.",
            timeEstimated = 60,
            feedback = ICFESFeedback(
                correct = "Correcto. El sistema linfático es clave en la respuesta inmune.",
                incorrect = mapOf(
                    "A" to "El transporte de oxígeno es función del sistema circulatorio.",
                    "B" to "El filtrado de desechos es función principal del sistema excretor.",
                    "D" to "La regulación térmica involucra principalmente piel y sistema circulatorio."
                ),
                tip = "Piensa en los ganglios linfáticos y su papel cuando hay infecciones.",
                relatedTopics = listOf("Sistema linfático", "Inmunología", "Biología humana")
            ),
            tags = listOf("biologia", "sistema_linfatico", "simulacro")
        ),

        ICFESQuestion(
            id = "SIM_CN002",
            type = ICFESQuestionType.MATHEMATICAL_PROBLEM,
            question = "Un objeto se lanza verticalmente hacia arriba con velocidad inicial de 20 m/s. ¿Cuál es su altura máxima? (g = 10 m/s²)",
            options = listOf(
                "A) 10 m",
                "B) 15 m",
                "C) 20 m",
                "D) 25 m"
            ),
            correctAnswer = "C",
            competency = "Física mecánica",
            difficulty = Difficulty.MEDIO,
            explanation = "En altura máxima v = 0. Usando v² = v₀² - 2gh: 0 = 400 - 20h, entonces h = 20 m",
            timeEstimated = 110,
            feedback = ICFESFeedback(
                correct = "Excelente aplicación de cinemática. Has usado correctamente las ecuaciones de movimiento vertical.",
                incorrect = mapOf(
                    "A" to "Revisa la ecuación: v² = v₀² - 2gh con v = 0 en altura máxima.",
                    "B" to "Verifica el cálculo: 0 = 400 - 20h → h = 400/20 = 20 m",
                    "D" to "Cuidado con los signos en la ecuación cinemática."
                ),
                tip = "En altura máxima la velocidad es cero. Usa v² = v₀² - 2gh",
                relatedTopics = listOf("Cinemática", "Movimiento vertical", "Lanzamiento vertical")
            ),
            tags = listOf("fisica", "cinematica", "simulacro")
        ),

        ICFESQuestion(
            id = "SIM_CN003",
            type = ICFESQuestionType.SCIENTIFIC_ANALYSIS,
            question = "¿Cuál es el producto principal de la fermentación alcohólica realizada por levaduras?",
            options = listOf(
                "A) Ácido láctico y agua",
                "B) Etanol y dióxido de carbono",
                "C) Metano y ácido acético",
                "D) Acetona y ácido butírico"
            ),
            correctAnswer = "B",
            competency = "Bioquímica y metabolismo",
            difficulty = Difficulty.FACIL,
            explanation = "La fermentación alcohólica convierte glucosa en etanol (alcohol etílico) y CO₂, proceso utilizado en producción de bebidas alcohólicas y pan.",
            timeEstimated = 70,
            feedback = ICFESFeedback(
                correct = "Correcto. Conoces bien el proceso de fermentación alcohólica.",
                incorrect = mapOf(
                    "A" to "Ácido láctico es producto de fermentación láctica, no alcohólica.",
                    "C" to "Metano se produce en fermentación metánica, no alcohólica.",
                    "D" to "Estos son productos de otras fermentaciones diferentes."
                ),
                tip = "Piensa en los productos de la fabricación de cerveza o vino.",
                relatedTopics = listOf("Fermentación", "Metabolismo", "Microbiología")
            ),
            tags = listOf("bioquimica", "fermentacion", "simulacro")
        )
    )

    // ✅ PREGUNTAS DE SOCIALES Y CIUDADANAS PARA SIMULACRO (3 preguntas base)
    private val simulationSocialesQuestions = listOf(
        ICFESQuestion(
            id = "SIM_SC001",
            type = ICFESQuestionType.SOCIAL_CONTEXT,
            context = """
            El proceso de independencia de Colombia se caracterizó por una serie de conflictos internos entre diferentes grupos políticos. Los centralistas, liderados principalmente por Simón Bolívar, defendían un gobierno fuerte y unificado. Los federalistas, encabezados por Francisco de Paula Santander, abogaban por mayor autonomía regional y un gobierno descentralizado.

            Estas diferencias ideológicas se intensificaron después de 1826 y llevaron a la disolución de la Gran Colombia en 1830. El conflicto entre estas dos visiones de Estado marcó profundamente la historia política colombiana del siglo XIX.
            """.trimIndent(),
            question = "¿Cuál fue la principal consecuencia política de las diferencias entre centralistas y federalistas?",
            options = listOf(
                "A) El fortalecimiento de la unidad regional",
                "B) La disolución de la Gran Colombia en 1830",
                "C) La consolidación del poder militar",
                "D) La creación de nuevas constituciones"
            ),
            correctAnswer = "B",
            competency = "Historia de Colombia",
            difficulty = Difficulty.MEDIO,
            explanation = "El texto establece claramente que las diferencias entre centralistas y federalistas llevaron a la disolución de la Gran Colombia en 1830.",
            timeEstimated = 100,
            feedback = ICFESFeedback(
                correct = "Excelente comprensión de las causas de la disolución de la Gran Colombia.",
                incorrect = mapOf(
                    "A" to "Al contrario, las diferencias debilitaron la unidad regional.",
                    "C" to "El texto no menciona consolidación del poder militar como consecuencia directa.",
                    "D" to "Aunque hubo constituciones, la consecuencia principal mencionada es la disolución."
                ),
                tip = "Busca la consecuencia directa que el texto menciona tras explicar las diferencias ideológicas.",
                relatedTopics = listOf("Historia de Colombia", "Gran Colombia", "Independencia")
            ),
            tags = listOf("historia", "gran_colombia", "simulacro")
        ),

        ICFESQuestion(
            id = "SIM_SC002",
            type = ICFESQuestionType.SOCIAL_CONTEXT,
            question = "¿Cuál es el principal objetivo de la descentralización administrativa en Colombia?",
            options = listOf(
                "A) Concentrar el poder en el gobierno nacional",
                "B) Transferir competencias y recursos a entidades territoriales",
                "C) Eliminar los departamentos y municipios",
                "D) Centralizar todos los servicios públicos"
            ),
            correctAnswer = "B",
            competency = "Organización del Estado",
            difficulty = Difficulty.FACIL,
            explanation = "La descentralización busca transferir competencias, funciones y recursos del nivel nacional a departamentos y municipios para mayor autonomía local.",
            timeEstimated = 80,
            feedback = ICFESFeedback(
                correct = "Correcto. Comprendes el concepto de descentralización administrativa.",
                incorrect = mapOf(
                    "A" to "La descentralización busca lo contrario: distribuir el poder.",
                    "C" to "La descentralización fortalece las entidades territoriales, no las elimina.",
                    "D" to "Busca descentralizar, no centralizar los servicios."
                ),
                tip = "Descentralización significa distribución del poder hacia las regiones.",
                relatedTopics = listOf("Organización del Estado", "Autonomía territorial")
            ),
            tags = listOf("descentralizacion", "estado", "simulacro")
        ),

        ICFESQuestion(
            id = "SIM_SC003",
            type = ICFESQuestionType.SOCIAL_CONTEXT,
            question = "¿Cuál es una característica principal del relieve de la región Andina colombiana?",
            options = listOf(
                "A) Presenta principalmente llanuras extensas",
                "B) Está formada por tres cordilleras que atraviesan el país",
                "C) Carece de alturas significativas",
                "D) Es una región completamente plana"
            ),
            correctAnswer = "B",
            competency = "Geografía física",
            difficulty = Difficulty.FACIL,
            explanation = "La región Andina colombiana se caracteriza por las tres cordilleras (Oriental, Central y Occidental) que son ramificaciones de la cordillera de los Andes.",
            timeEstimated = 60,
            feedback = ICFESFeedback(
                correct = "Correcto. Conoces bien la geografía física de Colombia.",
                incorrect = mapOf(
                    "A" to "Las llanuras extensas corresponden a la región de la Orinoquía.",
                    "C" to "La región Andina tiene las mayores alturas del país.",
                    "D" to "Al contrario, es la región más montañosa de Colombia."
                ),
                tip = "Piensa en las montañas que atraviesan Colombia de sur a norte.",
                relatedTopics = listOf("Geografía de Colombia", "Relieve", "Cordilleras")
            ),
            tags = listOf("geografia", "cordilleras", "simulacro")
        )
    )

    // ✅ PREGUNTAS DE INGLÉS PARA SIMULACRO (2 preguntas base)
    private val simulationEnglishQuestions = listOf(
        ICFESQuestion(
            id = "SIM_ENG001",
            type = ICFESQuestionType.ENGLISH_READING,
            context = """
            Artificial intelligence has revolutionized many industries, from healthcare to transportation. Machine learning algorithms can now diagnose diseases with remarkable accuracy, sometimes surpassing human specialists. In the automotive industry, self-driving cars are becoming increasingly sophisticated, promising to reduce traffic accidents caused by human error.

            However, these advances also raise important ethical questions. As AI systems become more autonomous, questions about accountability, privacy, and job displacement become more pressing. Society must carefully balance the benefits of AI with the need to address these challenges responsibly.
            """.trimIndent(),
            question = "According to the text, what is one advantage of AI in healthcare?",
            options = listOf(
                "A) It replaces all human doctors",
                "B) It can diagnose diseases with high accuracy",
                "C) It eliminates the need for medical training",
                "D) It makes healthcare completely free"
            ),
            correctAnswer = "B",
            competency = "Reading comprehension",
            difficulty = Difficulty.FACIL,
            explanation = "The text states that machine learning algorithms can diagnose diseases with remarkable accuracy, sometimes surpassing human specialists.",
            timeEstimated = 90,
            feedback = ICFESFeedback(
                correct = "Excellent reading comprehension. You identified the specific advantage mentioned in the text.",
                incorrect = mapOf(
                    "A" to "The text doesn't say AI replaces all doctors, but that it can surpass them in accuracy.",
                    "C" to "Medical training is not mentioned in the text.",
                    "D" to "Cost or pricing is not discussed in the passage."
                ),
                tip = "Look for the specific benefit mentioned about AI in healthcare.",
                relatedTopics = listOf("Reading comprehension", "AI vocabulary", "Healthcare")
            ),
            tags = listOf("english", "AI", "healthcare", "simulacro")
        ),

        ICFESQuestion(
            id = "SIM_ENG002",
            type = ICFESQuestionType.ENGLISH_READING,
            question = "Choose the correct option: 'I wish I _____ more time to finish the project.'",
            options = listOf(
                "A) have",
                "B) had",
                "C) will have",
                "D) would have"
            ),
            correctAnswer = "B",
            competency = "Use of English",
            difficulty = Difficulty.MEDIO,
            explanation = "After 'I wish' we use past simple to express a present unreal situation: I wish I had more time (but I don't).",
            timeEstimated = 60,
            feedback = ICFESFeedback(
                correct = "Perfect! You know how to use 'wish' for present unreal situations.",
                incorrect = mapOf(
                    "A" to "After 'I wish', we need past simple, not present simple.",
                    "C" to "Future tense doesn't work with 'I wish' for present situations.",
                    "D" to "'Would have' is used for past unreal situations, not present."
                ),
                tip = "I wish + past simple = present unreal situation",
                relatedTopics = listOf("Conditionals", "Wishes", "Unreal situations")
            ),
            tags = listOf("english", "grammar", "wishes", "simulacro")
        )
    )

    // ✅ FUNCIÓN PRINCIPAL PARA GENERAR SIMULACRO COMPLETO - AJUSTADA A 5 PREGUNTAS
    fun generateFullSimulation(): List<SimulationSession> {
        return listOf(
            createSession(
                sessionNumber = 1,
                moduleId = "lectura_critica",
                moduleName = "Lectura Crítica",
                description = "Comprensión, interpretación y evaluación de textos",
                questions = generateLecturaQuestions(5), // Cambiado de 35 a 5
                duration = 65 * 60 * 1000L, // 65 minutos
                color = 0xFF2196F3,
                icon = "📚"
            ),
            createSession(
                sessionNumber = 2,
                moduleId = "matematicas",
                moduleName = "Matemáticas",
                description = "Razonamiento cuantitativo y resolución de problemas",
                questions = generateMathQuestions(5), // Cambiado de 35 a 5
                duration = 65 * 60 * 1000L,
                color = 0xFFFF9800,
                icon = "🔢"
            ),
            createSession(
                sessionNumber = 3,
                moduleId = "ciencias_naturales",
                moduleName = "Ciencias Naturales",
                description = "Uso comprensivo del conocimiento científico",
                questions = generateCienciasQuestions(5), // Cambiado de 35 a 5
                duration = 65 * 60 * 1000L,
                color = 0xFF4CAF50,
                icon = "🧪"
            ),
            createSession(
                sessionNumber = 4,
                moduleId = "sociales_ciudadanas",
                moduleName = "Sociales y Ciudadanas",
                description = "Pensamiento social y competencias ciudadanas",
                questions = generateSocialesQuestions(5), // Cambiado de 35 a 5
                duration = 65 * 60 * 1000L,
                color = 0xFF9C27B0,
                icon = "🏛️"
            ),
            createSession(
                sessionNumber = 5,
                moduleId = "ingles",
                moduleName = "Inglés",
                description = "Comunicación en lengua inglesa",
                questions = generateEnglishQuestions(5), // Cambiado de 35 a 5
                duration = 60 * 60 * 1000L, // 60 minutos
                color = 0xFFF44336,
                icon = "🇺🇸"
            )
        )
    }

    // ✅ FUNCIÓN AUXILIAR PARA CREAR SESIONES
    private fun createSession(
        sessionNumber: Int,
        moduleId: String,
        moduleName: String,
        description: String,
        questions: List<ICFESQuestion>,
        duration: Long,
        color: Long,
        icon: String
    ): SimulationSession {
        return SimulationSession(
            sessionNumber = sessionNumber,
            moduleId = moduleId,
            moduleName = moduleName,
            description = description,
            questions = questions.shuffled(), // Mezclar preguntas
            duration = duration,
            color = color,
            icon = icon
        )
    }

    // ✅ GENERADORES DE PREGUNTAS POR MÓDULO CON IA
    private fun generateLecturaQuestions(count: Int): List<ICFESQuestion> {
        val baseQuestions = simulationLecturaQuestions
        return if (baseQuestions.size >= count) {
            baseQuestions.shuffled().take(count)
        } else {
            // Si necesitamos más preguntas, duplicar y variar IDs
            val questions = mutableListOf<ICFESQuestion>()
            val cycles = (count / baseQuestions.size) + 1

            repeat(cycles) { cycle ->
                baseQuestions.forEach { question ->
                    if (questions.size < count) {
                        questions.add(
                            question.copy(
                                id = "${question.id}_C${cycle}",
                                tags = question.tags + "cycle_$cycle"
                            )
                        )
                    }
                }
            }
            questions.shuffled().take(count)
        }
    }

    private fun generateMathQuestions(count: Int): List<ICFESQuestion> {
        val baseQuestions = simulationMathQuestions
        return expandQuestionSet(baseQuestions, count, "MAT")
    }

    private fun generateCienciasQuestions(count: Int): List<ICFESQuestion> {
        val baseQuestions = simulationCienciasQuestions
        return expandQuestionSet(baseQuestions, count, "CN")
    }

    private fun generateSocialesQuestions(count: Int): List<ICFESQuestion> {
        val baseQuestions = simulationSocialesQuestions
        return expandQuestionSet(baseQuestions, count, "SC")
    }

    private fun generateEnglishQuestions(count: Int): List<ICFESQuestion> {
        val baseQuestions = simulationEnglishQuestions
        return expandQuestionSet(baseQuestions, count, "ENG")
    }

    // ✅ FUNCIÓN AUXILIAR PARA EXPANDIR CONJUNTOS DE PREGUNTAS
    private fun expandQuestionSet(baseQuestions: List<ICFESQuestion>, count: Int, prefix: String): List<ICFESQuestion> {
        return if (baseQuestions.size >= count) {
            baseQuestions.shuffled().take(count)
        } else {
            val questions = mutableListOf<ICFESQuestion>()
            val cycles = (count / baseQuestions.size) + 1

            repeat(cycles) { cycle ->
                baseQuestions.shuffled().forEach { question ->
                    if (questions.size < count) {
                        questions.add(
                            question.copy(
                                id = "${question.id}_CYCLE${cycle}",
                                tags = question.tags + "simulacro_cycle_$cycle"
                            )
                        )
                    }
                }
            }
            questions.take(count)
        }
    }

    // ✅ FUNCIÓN PARA OBTENER ESTADÍSTICAS DEL BANCO
    fun getQuestionBankStats(): Map<String, Any> {
        return mapOf(
            "lectura_critica" to simulationLecturaQuestions.size,
            "matematicas" to simulationMathQuestions.size,
            "ciencias_naturales" to simulationCienciasQuestions.size,
            "sociales_ciudadanas" to simulationSocialesQuestions.size,
            "ingles" to simulationEnglishQuestions.size,
            "total_questions" to (
                    simulationLecturaQuestions.size +
                            simulationMathQuestions.size +
                            simulationCienciasQuestions.size +
                            simulationSocialesQuestions.size +
                            simulationEnglishQuestions.size
                    ),
            "can_generate_full_simulation" to (
                    simulationLecturaQuestions.isNotEmpty() &&
                            simulationMathQuestions.isNotEmpty() &&
                            simulationCienciasQuestions.isNotEmpty() &&
                            simulationSocialesQuestions.isNotEmpty() &&
                            simulationEnglishQuestions.isNotEmpty()
                    )
        )
    }
}