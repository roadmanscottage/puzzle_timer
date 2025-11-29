package com.puzzletimer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.puzzletimer.app.navigation.PuzzleTimerNavHost
import com.puzzletimer.app.ui.theme.PuzzleTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PuzzleTimerApp()
        }
    }
}

@Composable
private fun PuzzleTimerApp() {
    val navController = rememberNavController()

    PuzzleTimerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PuzzleTimerNavHost(navController = navController)
        }
    }
}
