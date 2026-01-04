package id.antasari.cityreport.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")

    data object Home : Screen("id/antasari/cityreport/ui/screens/home")
    data object Reports : Screen("reports")
    data object Profile : Screen("id/antasari/cityreport/ui/screens/profile")
    data object CreateReport : Screen("create_report")
    data object Nearby : Screen("nearby")

    // Admin
    data object AdminDashboard : Screen("admin_dashboard")
}
