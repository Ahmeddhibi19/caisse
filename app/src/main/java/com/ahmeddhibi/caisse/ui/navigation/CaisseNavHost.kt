package com.ahmeddhibi.caisse.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ahmeddhibi.caisse.ui.history.HistoryScreen
import com.ahmeddhibi.caisse.ui.pos.PosScreen
import kotlinx.serialization.Serializable

@Serializable
data object PosRoute

@Serializable
data object HistoryRoute

@Composable
fun CaisseNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = PosRoute, modifier = modifier) {
        composable<PosRoute> {
            PosScreen(onOpenHistory = { navController.navigate(HistoryRoute) { launchSingleTop = true } })
        }
        composable<HistoryRoute> {
            HistoryScreen(onBack = { navController.navigateUp() })
        }
    }
}
