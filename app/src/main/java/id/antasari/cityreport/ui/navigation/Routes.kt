package id.antasari.cityreport.ui.navigation

object Routes {
    // Splash
    const val SPLASH = "splash"
    
    // Onboarding & Auth
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    
    // Role-based home routes
    const val HOME_USER = "home_user"
    const val HOME_ADMIN = "home_admin"

    const val REPORT_FORM = "report_form"
    const val REPORT_LIST = "report_list"
    const val MAP_PICKER = "map_picker"

    const val REPORT_DETAIL_BASE = "report_detail"
    const val REPORT_DETAIL = "report_detail/{reportId}"

    const val PROFILE = "profile"
    const val NEARBY_REPORTS = "nearby_reports"
    
    // Comments
    const val COMMENTS_BASE = "comments"
    const val COMMENTS = "comments/{reportId}/{reportTitle}"
    
    // Admin routes
    const val ADMIN_REPORT_LIST = "admin_report_list"
    const val ADMIN_REPORT_DETAIL_BASE = "admin_report_detail"
    const val ADMIN_REPORT_DETAIL = "admin_report_detail/{reportId}"
    const val ADMIN_PROFILE = "admin_profile"
}

