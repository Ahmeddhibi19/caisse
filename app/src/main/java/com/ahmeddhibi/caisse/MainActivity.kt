package com.ahmeddhibi.caisse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ahmeddhibi.caisse.ui.CaisseApp
import com.ahmeddhibi.caisse.ui.theme.CaisseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CaisseTheme {
                CaisseApp()
            }
        }
    }
}
