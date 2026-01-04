package id.antasari.cityreport.data.remote

data class ReportRequest(
    val title: String,
    val description: String,
    val category: String?,
    val latitude: Double?,
    val longitude: Double?
)
