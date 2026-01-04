package id.antasari.cityreport.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import id.antasari.cityreport.data.model.UserRole
import id.antasari.cityreport.data.repository.AuthRepository
import id.antasari.cityreport.ui.admin.AdminHomeScreen
import id.antasari.cityreport.ui.admin.AdminReportDetailScreen
import id.antasari.cityreport.ui.admin.AdminReportListScreen
import id.antasari.cityreport.ui.admin.AdminProfileScreen
import id.antasari.cityreport.ui.auth.NewLoginScreen
import id.antasari.cityreport.ui.auth.RegisterScreen
import id.antasari.cityreport.ui.components.BlueBottomNavigation
import id.antasari.cityreport.ui.home.NewHomeScreen
import id.antasari.cityreport.ui.nearby.NewNearbyReportsScreen
import id.antasari.cityreport.ui.onboarding.OnboardingScreen
import id.antasari.cityreport.ui.profile.NewProfileScreen
import id.antasari.cityreport.ui.report.NewReportDetailScreen
import id.antasari.cityreport.ui.report.NewReportFormScreen
import id.antasari.cityreport.ui.report.NewReportListScreen
import id.antasari.cityreport.ui.search.SearchScreen
import id.antasari.cityreport.ui.splash.SplashScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        // Splash Screen with session check
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigate = { destination ->
                    navController.navigate(destination) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

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

        composable(Routes.REGISTER) {
            RegisterScreen(
                onBackToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    // After successful registration, navigate to HOME_USER
                    navController.navigate(Routes.HOME_USER) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        // User Home with Bottom Navigation
        composable(Routes.HOME_USER) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            
            Scaffold(
                bottomBar = {
                    BlueBottomNavigation(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            if (route == Routes.HOME_USER) {
                                // Already on home, do nothing or just refresh
                                // Don't navigate to avoid issues
                            } else {
                                navController.navigate(route) {
                                    popUpTo(Routes.HOME_USER) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
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
        
        // Report List with Bottom Navigation
        composable(Routes.REPORT_LIST) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            
            Scaffold(
                bottomBar = {
                    BlueBottomNavigation(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            if (route == Routes.HOME_USER) {
                                navController.popBackStack(Routes.HOME_USER, false)
                            } else {
                                navController.navigate(route) {
                                    popUpTo(Routes.HOME_USER) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    NewReportListScreen(
                        onNavigateToDetail = { id ->
                            navController.navigate("${Routes.REPORT_DETAIL_BASE}/$id")
                        },
                        onNavigateBack = { navController.navigate(Routes.HOME_USER) }
                    )
                }
            }
        }
        
        // Report Form (from home "Buat Laporan")
        composable("report/create") {
            NewReportFormScreen(
                onNavigateBack = { navController.popBackStack() },
                onReportSubmitted = { reportId ->
                    navController.navigate("report/$reportId") {
                        popUpTo(Routes.HOME_USER) { inclusive = false }
                    }
                }
            )
        }
        
        // My Reports (from home "Laporan Saya")
        composable("reports/my") {
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
                    NewReportListScreen(
                        onNavigateToDetail = { id ->
                            navController.navigate("report/$id")
                        },
                        onNavigateBack = { navController.navigate(Routes.HOME_USER) }
                    )
                }
            }
        }
        
        // All Reports (from home "Lihat Semua")
        composable("reports/all") {
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
                    NewReportListScreen(
                        onNavigateToDetail = { id ->
                            navController.navigate("report/$id")
                        },
                        onNavigateBack = { navController.navigate(Routes.HOME_USER) }
                    )
                }
            }
        }
        
        // Report Detail (from home report cards)
        composable("report/{reportId}") { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
            NewReportDetailScreen(
                reportId = reportId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Search Screen (from home search bar)
        composable("search") {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { reportId: String ->
                    navController.navigate("report/$reportId")
                }
            )
        }
        
        // Profile (from header avatar)
        composable(Routes.PROFILE) {
            val scope = rememberCoroutineScope()
            val authRepository = remember { AuthRepository() }
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            
            Scaffold(
                bottomBar = {
                    BlueBottomNavigation(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            if (route == Routes.HOME_USER) {
                                navController.popBackStack(Routes.HOME_USER, false)
                            } else {
                                navController.navigate(route) {
                                    popUpTo(Routes.HOME_USER) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
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
        
        // Nearby Reports with Bottom Navigation
        composable(Routes.NEARBY_REPORTS) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            
            Scaffold(
                bottomBar = {
                    BlueBottomNavigation(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            if (route == Routes.HOME_USER) {
                                navController.popBackStack(Routes.HOME_USER, false)
                            } else {
                                navController.navigate(route) {
                                    popUpTo(Routes.HOME_USER) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    NewNearbyReportsScreen(
                        onNavigateToDetail = { id ->
                            navController.navigate("${Routes.REPORT_DETAIL_BASE}/$id")
                        },
                        onNavigateToCreate = {
                            navController.navigate(Routes.REPORT_FORM)
                        }
                    )
                }
            }
        }
        
        // Admin Home with Dashboard
        composable(Routes.HOME_ADMIN) {
            AdminHomeScreen(
                onNavigateToReportDetail = { id: String ->
                    navController.navigate("${Routes.ADMIN_REPORT_DETAIL_BASE}/$id")
                },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.HOME_ADMIN) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // Admin Report List
        composable(Routes.ADMIN_REPORT_LIST) {
            AdminReportListScreen(
                onNavigateToDetail = { id ->
                    navController.navigate("${Routes.ADMIN_REPORT_DETAIL_BASE}/$id")
                },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.HOME_ADMIN) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        
        // Admin Profile
        composable(Routes.ADMIN_PROFILE) {
            AdminProfileScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.HOME_ADMIN) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REPORT_FORM) {
            NewReportFormScreen(
                onNavigateBack = { navController.popBackStack() },
                onReportSubmitted = { id ->
                    navController.navigate("${Routes.REPORT_DETAIL_BASE}/$id") {
                        popUpTo(Routes.HOME_USER) { inclusive = false }
                    }
                }
            )
        }


        composable("${Routes.REPORT_DETAIL_BASE}/{reportId}") {
            NewReportDetailScreen(
                reportId = it.arguments?.getString("reportId") ?: "",
                onNavigateBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        
        // Admin Report Detail
        composable("${Routes.ADMIN_REPORT_DETAIL_BASE}/{reportId}") {
            AdminReportDetailScreen(
                reportId = it.arguments?.getString("reportId") ?: "",
                onBack = { navController.popBackStack() },
                onReportDeleted = {
                    navController.popBackStack()
                }
            )
        }
    }
}

