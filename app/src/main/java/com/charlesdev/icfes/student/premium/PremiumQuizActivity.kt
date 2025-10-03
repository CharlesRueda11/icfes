
package com.charlesdev.icfes.student.premium

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.charlesdev.icfes.ui.theme.IcfesTheme

class PremiumQuizActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val moduleId = intent.getStringExtra("module_id") ?: ""
        val teacherId = intent.getStringExtra("teacher_id") ?: ""
        val sessionType = intent.getStringExtra("session_type") ?: "practica"
        val moduleName = intent.getStringExtra("module_name") ?: "Módulo"
        val teacherName = intent.getStringExtra("teacher_name") ?: "Profesor"

        setContent {
            IcfesTheme {
                PremiumQuizScreen(
                    moduleId = moduleId,
                    teacherId = teacherId,
                    sessionType = sessionType,
                    moduleName = moduleName,
                    teacherName = teacherName
                )
            }
        }
    }
}