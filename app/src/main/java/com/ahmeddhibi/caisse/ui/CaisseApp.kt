package com.ahmeddhibi.caisse.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmeddhibi.caisse.ui.enrollment.EnrollmentScreen
import com.ahmeddhibi.caisse.ui.pos.PosScreen

@Composable
fun CaisseApp(viewModel: MainViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState) {
        MainUiState.Loading -> Surface(modifier = Modifier.fillMaxSize()) {}
        MainUiState.NotEnrolled -> EnrollmentScreen()
        is MainUiState.Ready -> PosScreen()
    }
}
