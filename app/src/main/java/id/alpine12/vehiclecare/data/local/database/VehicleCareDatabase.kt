package id.alpine12.vehiclecare.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import id.alpine12.vehiclecare.data.local.dao.ReminderDao
import id.alpine12.vehiclecare.data.local.dao.ServiceRecordDao
import id.alpine12.vehiclecare.data.local.dao.VehicleDao
import id.alpine12.vehiclecare.data.local.entity.ReminderEntity
import id.alpine12.vehiclecare.data.local.entity.ServiceRecordEntity
import id.alpine12.vehiclecare.data.local.entity.VehicleEntity

@Database(
    entities = [VehicleEntity::class, ServiceRecordEntity::class, ReminderEntity::class],
    version = 2,
    exportSchema = false
)
abstract class VehicleCareDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun serviceRecordDao(): ServiceRecordDao
    abstract fun reminderDao(): ReminderDao
    companion object { const val DATABASE_NAME = "vehicle_care_db" }
}

// Migration from version 1 to 2: Add oilIntervalKm column with default value 5000
val MIGRATION_1_2 = Migration(1, 2) { database ->
    database.execSQL("ALTER TABLE vehicles ADD COLUMN oilIntervalKm INTEGER NOT NULL DEFAULT 5000")
}