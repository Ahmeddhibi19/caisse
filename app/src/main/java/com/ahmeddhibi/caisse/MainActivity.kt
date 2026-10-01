package com.ahmeddhibi.caisse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ahmeddhibi.caisse.domain.printing.PrintQueue
import com.ahmeddhibi.caisse.ui.CaisseApp
import com.ahmeddhibi.caisse.ui.theme.CaisseTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var printQueue: PrintQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Started here rather than in Application: a background WorkManager start must not print.
        printQueue.start()
        setContent {
            CaisseTheme {
                CaisseApp()
            }
        }
    }
}
