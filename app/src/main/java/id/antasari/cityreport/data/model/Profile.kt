package id.antasari.cityreport.data.model

/**
 * User profile data class matching Appwrite 'profiles' collection
 */
data class Profile(
    val userId: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val profilePhotoId: String? = null
)

/**
 * User role enumeration
 */
enum class UserRole(val value: String) {
    USER("USER"),
    ADMIN("ADMIN");
    
    companion object {
        fun fromString(value: String): UserRole {
            return values().find { it.value == value } ?: USER
        }
    }
}
