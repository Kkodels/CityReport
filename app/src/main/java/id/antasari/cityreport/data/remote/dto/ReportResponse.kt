package id.antasari.cityreport.data.remote.dto

data class ReportResponse(
    val id: Long,
    val title: String,
    val description: String,
    val category: String,
    val urgency: String,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val createdAt: String
)
