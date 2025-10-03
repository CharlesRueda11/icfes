package com.charlesdev.icfes.student

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ✅ TARJETA DE ACCESO AL ICFES LABORATORIO (VERSIÓN BÁSICA)
@Composable
fun ICFESAdvancedCard(
    context: Context = LocalContext.current
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
        ),
        border = BorderStroke(2.dp, Color(0xFF1E88E5).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Default.Science,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color(0xFF1E88E5)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "🧪 ICFES Laboratorio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E88E5)
                    )
                    Text(
                        "Versión de entrenamiento gratuita",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ✅ Badge de "versión limitada"
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF9800).copy(alpha = 0.15f)
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    "⚡ Contenido básico • Ideal para empezar",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
            }

            // Características del laboratorio
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                FeatureItem(
                    icon = "📖",
                    text = "10-15 preguntas por módulo"
                )
                FeatureItem(
                    icon = "🎯",
                    text = "Introducción a competencias ICFES"
                )
                FeatureItem(
                    icon = "⏱️",
                    text = "Simulacros básicos cronometrados"
                )
                FeatureItem(
                    icon = "🔓",
                    text = "¿Quieres más? Pide a tu profesor contenido completo"
                )
            }

            Button(
                onClick = {
                    val intent = Intent(context, StudentICFESActivity::class.java)
                    intent.putExtra("mode", "laboratorio") // Diferenciarlo del modo completo
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E88E5)
                )
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Empezar Entrenamiento",
                    fontWeight = FontWeight.Bold
                )
            }

            // ✅ NUEVO: Hint sobre el contenido del profesor
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "💡 Tip: Si tu profesor crea contenido personalizado, aparecerá aquí automáticamente",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun FeatureItem(icon: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}