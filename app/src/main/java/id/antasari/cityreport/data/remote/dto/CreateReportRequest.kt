package id.antasari.cityreport.data.remote.dto

data class CreateReportRequest(
    val title: String,
    val description: String,
    val category: String,   // sesuaikan dengan Postman (bisa "categoryId" kalau di backend begitu)
    val urgency: String,    // contoh: "LOW", "MEDIUM", "HIGH"
    val latitude: Double,
    val longitude: Double
)
