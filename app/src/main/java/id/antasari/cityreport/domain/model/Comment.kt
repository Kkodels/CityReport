package id.antasari.cityreport.domain.model

data class Comment(
    val id: String,
    val reportId: String,
    val userId: String,
    val userName: String,
    val isAdmin: Boolean,
    val message: String,
    val createdAt: String
)
