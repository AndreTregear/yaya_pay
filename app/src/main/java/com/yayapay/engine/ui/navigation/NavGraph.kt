package com.yayapay.engine.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.yayapay.engine.ui.screens.dashboard.DashboardScreen
import com.yayapay.engine.ui.screens.logs.LogScreen
import com.yayapay.engine.ui.screens.settings.SettingsScreen
import com.yayapay.engine.ui.screens.setup.SetupScreen

enum class Screen(val route: String) {
    Setup("setup"),
    Dashboard("dashboard"),
    Logs("logs"),
    Settings("settings")
}

@Composable
fun YayaPayNavGraph(
    navController: NavHostController,
    isSetupComplete: Boolean
) {
    NavHost(
        navController = navController,
        startDestination = if (isSetupComplete) Screen.Dashboard.route else Screen.Setup.route
    ) {
        composable(Screen.Setup.route) {
            SetupScreen(onSetupComplete = {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Setup.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToLogs = { navController.navigate(Screen.Logs.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Logs.route) {
            LogScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
