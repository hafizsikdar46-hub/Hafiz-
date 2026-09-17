package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CurrentDetectorScreen
import com.example.ui.theme.CurrentDetectorTheme
import com.example.viewmodel.CurrentDetectorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CurrentDetectorTheme {
                val viewModel: CurrentDetectorViewModel = viewModel()
                CurrentDetectorScreen(viewModel = viewModel)
            }
        }
    }
}
