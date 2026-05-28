package id.alpine12.vehiclecare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "service_records",
    foreignKeys = [ForeignKey(entity = VehicleEntity::class, parentColumns = ["id"], childColumns = ["vehicleId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("vehicleId")]
)
data class ServiceRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleId: Long,
    val type: String,
    val date: Long,
    val kilometer: Int,
    val productBrand: String,
    val cost: Int?,
    val workshop: String,
    val notes: String,
    val createdAt: Long
)