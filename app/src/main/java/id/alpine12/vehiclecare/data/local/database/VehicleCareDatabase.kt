package id.alpine12.vehiclecare.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import id.alpine12.vehiclecare.data.local.dao.ReminderDao
import id.alpine12.vehiclecare.data.local.dao.ServiceRecordDao
import id.alpine12.vehiclecare.data.local.dao.VehicleDao
import id.alpine12.vehiclecare.data.local.entity.ReminderEntity
import id.alpine12.vehiclecare.data.local.entity.ServiceRecordEntity
import id.alpine12.vehiclecare.data.local.entity.VehicleEntity

@Database(
    entities = [VehicleEntity::class, ServiceRecordEntity::class, ReminderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VehicleCareDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun serviceRecordDao(): ServiceRecordDao
    abstract fun reminderDao(): ReminderDao
    companion object { const val DATABASE_NAME = "vehicle_care_db" }
}