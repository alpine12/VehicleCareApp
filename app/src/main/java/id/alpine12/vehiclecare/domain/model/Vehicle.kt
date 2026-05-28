package id.alpine12.vehiclecare.domain.model

data class Vehicle(
    val id: Long = 0,
    val name: String,
    val type: VehicleType,
    val brand: String,
    val model: String,
    val year: Int,
    val licensePlate: String = "",
    val currentKm: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)