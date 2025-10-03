package com.charlesdev.icfes.student.duelo


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.charlesdev.icfes.student.duelo.DuelModeRoot
import com.charlesdev.icfes.ui.theme.IcfesTheme

class DuelICFESActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            IcfesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "duelo_home"
                    ) {
                        composable("duelo_home") {
                            DuelModeRoot(
                                navController = navController,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}