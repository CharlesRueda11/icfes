package com.charlesdev.icfes.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import com.charlesdev.icfes.student.logout.StudentRegistroActivity
import com.charlesdev.icfes.teacher.logout.TeacherRegistroActivity
import com.charlesdev.icfes.ui.theme.IcfesTheme

class UserTypeSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IcfesTheme {
                UserTypeSelectionScreen()
            }
        }
    }
}

data class UserType(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val gradient: List<Color>,
    val borderColor: Color
)

@Composable
fun UserTypeSelectionScreen() {
    val context = LocalContext.current

    val userTypes = remember {
        listOf(
            UserType(
                title = "Estudiante",
                subtitle = "Preparación ICFES Saber 11",
                description = "Accede a cursos, evaluaciones y herramientas de estudio personalizado para aprobar el ICFES",
                icon = Icons.Default.School,
                gradient = listOf(Color(0xFF1E88E5), Color(0xFF42A5F5)),
                borderColor = Color(0xFF1E88E5)
            ),
            UserType(
                title = "Profesor",
                subtitle = "Gestiona grupos y estudiantes",
                description = "Crea grupos, asigna tareas y monitorea el progreso de tus estudiantes en tiempo real",
                icon = Icons.Default.Psychology,
                gradient = listOf(Color(0xFF7B1FA2), Color(0xFFAB47BC)),
                borderColor = Color(0xFF7B1FA2)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color(0xFFE3F2FD)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo/Branding principal
            Text(
                text = "ICFES",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E88E5),
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "APP",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Preparación SABER 11 ",
                fontSize = 16.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Título de selección
            Text(
                text = "Selecciona tu perfil",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "¿Eres estudiante o profesor?",
                fontSize = 16.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tarjetas de tipo de usuario
            userTypes.forEach { userType ->
                UserTypeCard(
                    userType = userType,
                    onClick = {
                        when (userType.title) {
                            "Estudiante" -> {
                                context.startActivity(Intent(context, StudentRegistroActivity::class.java))
                            }
                            "Profesor" -> {
                                context.startActivity(Intent(context, TeacherRegistroActivity::class.java))
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Información adicional
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF0F9FF)
                ),
                border = BorderStroke(1.dp, Color(0xFF1E88E5).copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ambos perfiles tienen acceso a todo el contenido educativo de preparación ICFES",
                        fontSize = 13.sp,
                        color = Color(0xFF1E88E5),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun UserTypeCard(
    userType: UserType,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .scale(scale)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(2.dp, userType.borderColor.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Fondo degradado sutil
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = userType.gradient.map { it.copy(alpha = 0.08f) }
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono con fondo circular
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(
                            Brush.radialGradient(
                                colors = userType.gradient.map { it.copy(alpha = 0.15f) }
                            ),
                            RoundedCornerShape(35.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = userType.icon,
                        contentDescription = userType.title,
                        tint = userType.borderColor,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Contenido de texto
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = userType.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50)
                    )

                    Text(
                        text = userType.subtitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = userType.borderColor,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Text(
                        text = userType.description,
                        fontSize = 12.sp,
                        color = Color(0xFF7F8C8D),
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Flecha de navegación
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Seleccionar ${userType.title}",
                    tint = userType.borderColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    // Reset de la animación
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}