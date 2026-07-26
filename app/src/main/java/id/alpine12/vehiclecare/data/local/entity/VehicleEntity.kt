package id.alpine12.vehiclecare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String,
    val brand: String,
    val model: String,
    val year: Int,
    val licensePlate: String,
    val currentKm: Int,
    val oilIntervalKm: Int = 5000,
    val createdAt: Long,
    val updatedAt: Long
)