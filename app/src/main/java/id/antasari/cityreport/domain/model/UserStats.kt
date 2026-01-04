package id.antasari.cityreport.domain.model

data class UserStats(
    val totalReports: Int = 0,
    val resolvedCount: Int = 0,
    val totalVotes: Int = 0,
    val lastReportDate: String? = null
) {
    val resolvedPercentage: Int
        get() = if (totalReports > 0) {
            (resolvedCount.toFloat() / totalReports * 100).toInt()
        } else 0
    
    val averageVotes: Int
        get() = if (totalReports > 0) {
            totalVotes / totalReports
        } else 0
}
