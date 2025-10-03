package com.charlesdev.icfes.student.duelo


object SampleQuestionsICFES {
    val list = listOf(
        // MATEMÁTICAS - ÁLGEBRA


                // MATEMÁTICAS - ÁLGEBRA
                Question(
                    text = "Si 2x + 3 = 11, ¿cuál es el valor de x?",
                    optionA = "x = 3",
                    optionB = "x = 4",
                    optionC = "x = 5",
                    optionD = "x = 6",
                    correctAnswer = 'B', // ✅ 2x=8 → x=4
                    difficulty = Difficulty.EASY,
                    topic = DuelTopic.ALGEBRA,
                    hint = "Despeja x paso a paso.",
                    formula = "2x + 3 = 11 → 2x = 8 → x = 4"
                ),
/*
                Question(
                    text = "¿Cuál es la solución de la ecuación x² - 5x + 6 = 0?",
                    optionA = "x = 1, x = 6",
                    optionB = "x = 2, x = 3",
                    optionC = "x = -2, x = -3",
                    optionD = "x = 1, x = -6",
                    correctAnswer = 'B', // ✅ soluciones 2 y 3
                    difficulty = Difficulty.MEDIUM,
                    topic = DuelTopic.ALGEBRA,
                    hint = "Factoriza o usa la fórmula cuadrática.",
                    formula = "(x-2)(x-3) = 0"
                ),

                // MATEMÁTICAS - GEOMETRÍA
                Question(
                    text = "El área de un triángulo con base 8 cm y altura 5 cm es:",
                    optionA = "20 cm²",
                    optionB = "40 cm²",
                    optionC = "13 cm²",
                    optionD = "25 cm²",
                    correctAnswer = 'A', // ✅ A=(8*5)/2=20
                    difficulty = Difficulty.EASY,
                    topic = DuelTopic.GEOMETRIA,
                    hint = "Área = (base × altura) / 2",
                    formula = "A = (b × h) / 2"
                ),

                Question(
                    text = "La circunferencia de un círculo con radio 3 cm es:",
                    optionA = "6π cm",
                    optionB = "9π cm",
                    optionC = "18π cm",
                    optionD = "3π cm",
                    correctAnswer = 'A', // ✅ C=2πr=6π
                    difficulty = Difficulty.MEDIUM,
                    topic = DuelTopic.GEOMETRIA,
                    hint = "C = 2πr",
                    formula = "C = 2π × 3 = 6π"
                ),



        // ESTADÍSTICA
        Question(
            text = "La media aritmética de los números 2, 4, 6, 8, 10 es:",
            optionA = "5",
            optionB = "6",
            optionC = "7",
            optionD = "8",
            correctAnswer = 'B', // ✅ media = 6
            difficulty = Difficulty.EASY,
            topic = DuelTopic.ESTADISTICA,
            hint = "Suma todos y divide entre la cantidad.",
            formula = "(2+4+6+8+10)/5 = 30/5 = 6"
        ),

        // --- FÍSICA ---
        Question(
            text = "Un objeto cae libremente desde 20 m de altura. ¿Cuánto tiempo tarda en tocar el suelo? (g = 10 m/s²)",
            optionA = "1 s",
            optionB = "2 s",
            optionC = "3 s",
            optionD = "4 s",
            correctAnswer = 'B', // ✅ t=√(2h/g)=2
            difficulty = Difficulty.MEDIUM,
            topic = DuelTopic.MECANICA,
            hint = "Usa h = ½gt²",
            formula = "20 = ½(10)t² → t = 2 s"
        ),
        Question(
            text = "La velocidad de un objeto en movimiento rectilíneo uniforme que recorre 60 m en 20 s es:",
            optionA = "2 m/s",
            optionB = "3 m/s",
            optionC = "4 m/s",
            optionD = "5 m/s",
            correctAnswer = 'B', // ✅ 3 m/s
            difficulty = Difficulty.EASY,
            topic = DuelTopic.MECANICA,
            hint = "v = d/t",
            formula = "v = 60/20 = 3"
        ),

        // --- QUÍMICA ---
        Question(
            text = "¿Cuántos protones tiene un átomo de carbono (C)?",
            optionA = "4",
            optionB = "6",
            optionC = "12",
            optionD = "14",
            correctAnswer = 'B', // ✅ Z=6
            difficulty = Difficulty.EASY,
            topic = DuelTopic.ATOMICA,
            hint = "El número atómico indica los protones.",
            formula = "Z = 6"
        ),
        Question(
            text = "En la reacción 2H₂ + O₂ → 2H₂O, ¿cuántas moléculas de agua se forman a partir de 4 moléculas de H₂?",
            optionA = "2",
            optionB = "4",
            optionC = "6",
            optionD = "8",
            correctAnswer = 'B', // ✅ 4 H₂ → 4 H₂O
            difficulty = Difficulty.MEDIUM,
            topic = DuelTopic.ESTEQUIOMETRIA,
            hint = "Usa la proporción estequiométrica.",
            formula = "2H₂ : 2H₂O → 4H₂ : 4H₂O"
        ),

        // --- BIOLOGÍA ---
        Question(
            text = "¿Cuál es la función principal de los ribosomas?",
            optionA = "Respiración celular",
            optionB = "Síntesis de proteínas",
            optionC = "División celular",
            optionD = "Digestión celular",
            correctAnswer = 'B', // ✅ síntesis de proteínas
            difficulty = Difficulty.EASY,
            topic = DuelTopic.CELULAR,
            hint = "Relacionado con la traducción del ARN."
        ),
        Question(
            text = "¿En qué organelo se lleva a cabo la fotosíntesis?",
            optionA = "Mitocondria",
            optionB = "Núcleo",
            optionC = "Cloroplasto",
            optionD = "Retículo endoplásmico",
            correctAnswer = 'C', // ✅ Cloroplasto
            difficulty = Difficulty.EASY,
            topic = DuelTopic.CELULAR,
            hint = "Contiene clorofila."
        ),

        // --- HISTORIA COLOMBIA ---
        Question(
            text = "¿En qué año se firmó la independencia de Colombia?",
            optionA = "1810",
            optionB = "1819",
            optionC = "1821",
            optionD = "1830",
            correctAnswer = 'A',
            difficulty = Difficulty.EASY,
            topic = DuelTopic.HISTORIA_COLOMBIA,
            hint = "Grito de independencia."
        ),
        Question(
            text = "¿Quién fue el primer presidente de la Gran Colombia?",
            optionA = "Antonio Nariño",
            optionB = "Simón Bolívar",
            optionC = "Francisco de Paula Santander",
            optionD = "José María Córdova",
            correctAnswer = 'B', // ✅ Bolívar
            difficulty = Difficulty.MEDIUM,
            topic = DuelTopic.HISTORIA_COLOMBIA,
            hint = "El Libertador."
        ),

        // --- GEOGRAFÍA ---
        Question(
            text = "¿Cuál es la capital de Brasil?",
            optionA = "São Paulo",
            optionB = "Río de Janeiro",
            optionC = "Brasilia",
            optionD = "Salvador",
            correctAnswer = 'C', // ✅ Brasilia
            difficulty = Difficulty.EASY,
            topic = DuelTopic.GEOGRAFIA,
            hint = "Ciudad planificada en el centro."
        ),

        // --- LECTURA CRÍTICA ---
        Question(
            text = "En el texto: 'El cambio climático afecta los ecosistemas globalmente', ¿cuál es la idea principal?",
            optionA = "Los ecosistemas están cambiando",
            optionB = "El clima es variable",
            optionC = "El cambio climático tiene impacto mundial en ecosistemas",
            optionD = "Los ecosistemas son globales",
            correctAnswer = 'C', // ✅ impacto mundial
            difficulty = Difficulty.MEDIUM,
            topic = DuelTopic.COMPRENSION_LECTORA,
            hint = "Identifica el sujeto y la acción."
        ),

        // --- INGLÉS ---
        Question(
            text = "Choose the correct sentence:",
            optionA = "She don't like pizza",
            optionB = "She doesn't likes pizza",
            optionC = "She doesn't like pizza",
            optionD = "She not like pizza",
            correctAnswer = 'C', // ✅ "She doesn't like pizza"
            difficulty = Difficulty.EASY,
            topic = DuelTopic.GRAMMAR,
            hint = "Third person singular negative."
        ),
        Question(
            text = "What is the past tense of 'go'?",
            optionA = "goed",
            optionB = "went",
            optionC = "gone",
            optionD = "going",
            correctAnswer = 'B', // ✅ went
            difficulty = Difficulty.EASY,
            topic = DuelTopic.GRAMMAR,
            hint = "Irregular verb."
        ),
        Question(
            text = "What does 'environment' mean?",
            optionA = "Medio ambiente",
            optionB = "Entretenimiento",
            optionC = "Desenvolvimiento",
            optionD = "Envolvimiento",
            correctAnswer = 'A', // ✅ Medio ambiente
            difficulty = Difficulty.EASY,
            topic = DuelTopic.VOCABULARY,
            hint = "Related to nature and surroundings."
        ),

        // --- FILOSOFÍA ---
        Question(
            text = "¿Quién escribió 'El Príncipe'?",
            optionA = "Platón",
            optionB = "Aristóteles",
            optionC = "Nicolás Maquiavelo",
            optionD = "Santo Tomás de Aquino",
            correctAnswer = 'C', // ✅ Maquiavelo
            difficulty = Difficulty.MEDIUM,
            topic = DuelTopic.FILOSOFIA,
            hint = "Filósofo renacentista italiano."
        ),

        // --- TRIGONOMETRÍA ---
        Question(
            text = "¿Cuál es el valor de sen(30°)?",
            optionA = "1/2",
            optionB = "√2/2",
            optionC = "√3/2",
            optionD = "1",
            correctAnswer = 'A', // ✅ 1/2
            difficulty = Difficulty.MEDIUM,
            topic = DuelTopic.TRIGONOMETRIA,
            hint = "Valor especial de seno.",
            formula = "sen(30°) = 1/2"
        ),

        // --- ECONOMÍA ---
        Question(
            text = "¿Qué es la inflación?",
            optionA = "Disminución generalizada de precios",
            optionB = "Aumento generalizado de precios",
            optionC = "Estabilidad de precios",
            optionD = "Variación del tipo de cambio",
            correctAnswer = 'B', // ✅ aumento de precios
            difficulty = Difficulty.EASY,
            topic = DuelTopic.ECONOMIA,
            hint = "Pérdida del poder adquisitivo."
        ),

        // --- CONSTITUCIÓN ---
        Question(
            text = "¿En qué año se promulgó la actual Constitución de Colombia?",
            optionA = "1886",
            optionB = "1991",
            optionC = "1993",
            optionD = "2001",
            correctAnswer = 'B', // ✅ 1991
            difficulty = Difficulty.EASY,
            topic = DuelTopic.CONSTITUCION,
            hint = "Constitución de la apertura económica."
        )
*/
    )
}