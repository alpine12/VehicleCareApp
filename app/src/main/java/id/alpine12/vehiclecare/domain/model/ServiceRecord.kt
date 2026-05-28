package id.alpine12.vehiclecare.domain.model

data class ServiceRecord(
    val id: Long = 0,
    val vehicleId: Long,
    val type: ServiceType,
    val date: Long,
    val kilometer: Int,
    val productBrand: String = "",
    val cost: Int? = null,
    val workshop: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)