package id.antasari.cityreport.ui.navigation

/**
 * Integration wrapper for new UI screens
 * This allows gradual migration to new design while keeping old screens functional
 */

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import id.antasari.cityreport.data.model.UserRole
import id.antasari.cityreport.ui.auth.NewLoginScreen
import id.antasari.cityreport.ui.components.BlueBottomNavigation
import id.antasari.cityreport.ui.home.NewHomeScreen
import id.antasari.cityreport.ui.profile.NewProfileScreen
import id.antasari.cityreport.ui.report.NewReportDetailScreen
import id.antasari.cityreport.ui.report.CommentsScreen
import id.antasari.cityreport.ui.splash.SplashScreen as NewSplashScreen

/**
 * New UI Navigation Host - Ready to integrate
 * 
 * To use: Replace AppNavHost with NewAppNavHost in MainActivity
 */
@Composable
fun NewUINavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        // Splash Screen - NEW DESIGN
        composable(Routes.SPLASH) {
            NewSplashScreen(
                onNavigate = { destination: String ->
                    navController.navigate(destination) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        
        // Login Screen - NEW DESIGN
        composable(Routes.LOGIN) {
            NewLoginScreen(
                onLoginSuccess = { role ->
                    val destination = when (role) {
                        UserRole.ADMIN -> Routes.HOME_ADMIN
                        UserRole.USER -> Routes.HOME_USER
                    }
                    navController.navigate(destination) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        
        // User Home - NEW DESIGN with Bottom Nav
        composable(Routes.HOME_USER) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            
            Scaffold(
                bottomBar = {
                    BlueBottomNavigation(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(Routes.HOME_USER) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    NewHomeScreen(
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }
            }
        }
        
        // Profile - NEW DESIGN
        composable(Routes.PROFILE) {
            Scaffold(
                bottomBar = {
                    BlueBottomNavigation(
                        currentRoute = Routes.PROFILE,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(Routes.HOME_USER) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    NewProfileScreen(
                        onNavigate = { route -> navController.navigate(route) },
                        onLogout = {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
        
        // Report Detail - NEW DESIGN
        composable("${Routes.REPORT_DETAIL}/{reportId}") { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
            NewReportDetailScreen(
                reportId = reportId,
                onNavigateBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        
        // Comments Screen
        composable(Routes.COMMENTS) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
            val reportTitle = backStackEntry.arguments?.getString("reportTitle") ?: ""
            CommentsScreen(
                reportId = reportId,
                reportTitle = java.net.URLDecoder.decode(reportTitle, "UTF-8"),
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Keep existing screens for now (can be migrated gradually)
        // Add other routes as needed...
    }
}
