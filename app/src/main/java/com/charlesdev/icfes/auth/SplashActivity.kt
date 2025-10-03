package com.charlesdev.icfes.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.charlesdev.icfes.ui.theme.IcfesTheme
import com.charlesdev.icfes.student.StudentMainActivity
import com.charlesdev.icfes.teacher.TeacherMainActivity
import com.charlesdev.icfes.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : ComponentActivity() {

    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            IcfesTheme {
                UISplashScreen()
            }
        }

        // Verificar autenticación después de mostrar el splash por 3 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserAuthenticationAndRedirect()
        }, 3000)
    }

    private fun checkUserAuthenticationAndRedirect() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // Usuario autenticado, verificar su tipo
            checkUserTypeAndRedirect(currentUser.uid)
        } else {
            // No hay sesión activa, ir a selección de tipo de usuario
            redirectToUserTypeSelection()
        }
    }

    private fun checkUserTypeAndRedirect(uid: String) {
        val database = FirebaseDatabase.getInstance()
        val usuariosRef = database.getReference("Usuarios").child(uid)

        usuariosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val tipoUsuario = dataSnapshot.child("tipoUsuario").getValue(String::class.java)
                    val activo = dataSnapshot.child("activo").getValue(Boolean::class.java) ?: false

                    Log.d(TAG, "Usuario encontrado - Tipo: $tipoUsuario, Activo: $activo")

                    if (activo) {
                        when (tipoUsuario) {
                            "estudiante" -> {
                                redirectToStudentMain()
                            }
                            "profesor" -> {
                                redirectToTeacherMain()
                            }
                            else -> {
                                Log.w(TAG, "Tipo de usuario desconocido: $tipoUsuario")
                                redirectToUserTypeSelection()
                            }
                        }
                    } else {
                        Log.w(TAG, "Usuario inactivo")
                        FirebaseAuth.getInstance().signOut()
                        redirectToUserTypeSelection()
                    }
                } else {
                    Log.w(TAG, "Usuario no encontrado en la base de datos")
                    FirebaseAuth.getInstance().signOut()
                    redirectToUserTypeSelection()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error al consultar la base de datos", databaseError.toException())
                redirectToUserTypeSelection()
            }
        })
    }

    private fun redirectToStudentMain() {
        Log.d(TAG, "Redirigiendo a MainActivity de Estudiante")
        try {
            val intent = Intent(this, StudentMainActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error al redirigir a MainActivity de Estudiante", e)
            redirectToUserTypeSelection()
        }
    }

    private fun redirectToTeacherMain() {
        Log.d(TAG, "Redirigiendo a MainActivity de Profesor")
        try {
            val intent = Intent(this, TeacherMainActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error al redirigir a MainActivity de Profesor", e)
            redirectToUserTypeSelection()
        }
    }

    private fun redirectToUserTypeSelection() {
        Log.d(TAG, "Redirigiendo a UserTypeSelectionActivity")
        val intent = Intent(this, UserTypeSelectionActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@Composable
fun UISplashScreen() {
    // Animaciones
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = EaseInOut
        ),
        label = "alpha"
    )

    val scaleAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = EaseInOut
        ),
        label = "scale"
    )

    // Iniciar animación automáticamente
    LaunchedEffect(key1 = true) {
        startAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2E7D32), // Verde UIS principal
                        Color(0xFF4CAF50)  // Verde UIS claro
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo UIS con animación
            Image(
                painter = painterResource(id = R.drawable.logo_uis), // Asegúrate de tener este logo en drawable
                contentDescription = "Logo UIS",
                modifier = Modifier
                    .size(120.dp)
                    .scale(scaleAnimation)
                    .alpha(alphaAnimation)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Texto institucional
            Text(
                text = "Universidad Industrial\nde Santander",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alphaAnimation)
            )

            Spacer(modifier = Modifier.height(8.dp))

           /* Text(
                text = "Facultad de Ingenierías",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alphaAnimation)
            )*/

            Spacer(modifier = Modifier.height(16.dp))

            // Cambié "Resistencia de Materiales" por "Preparación SABER 11" para mantener coherencia con ICFES
            Text(
                text = "Preparación SABER 11",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alphaAnimation)
            )
        }

        // Texto de carga en la parte inferior
        Text(
            text = "Cargando...",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(alphaAnimation)
        )
    }
}