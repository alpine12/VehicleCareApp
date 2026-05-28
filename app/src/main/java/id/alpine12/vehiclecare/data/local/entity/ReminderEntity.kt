package id.alpine12.vehiclecare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [ForeignKey(entity = VehicleEntity::class, parentColumns = ["id"], childColumns = ["vehicleId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("vehicleId")]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleId: Long,
    val title: String,
    val description: String,
    val targetDate: Long?,
    val targetKm: Int?,
    val isActive: Boolean,
    val isCompleted: Boolean,
    val createdAt: Long
)