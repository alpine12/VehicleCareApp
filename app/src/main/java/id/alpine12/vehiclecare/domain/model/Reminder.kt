package id.alpine12.vehiclecare.domain.model

data class Reminder(
    val id: Long = 0,
    val vehicleId: Long,
    val title: String,
    val description: String = "",
    val targetDate: Long? = null,
    val targetKm: Int? = null,
    val isActive: Boolean = true,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)