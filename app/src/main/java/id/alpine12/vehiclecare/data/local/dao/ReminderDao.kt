package id.alpine12.vehiclecare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import id.alpine12.vehiclecare.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY createdAt DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>
    @Query("SELECT * FROM reminders WHERE vehicleId = :vehicleId ORDER BY createdAt DESC")
    fun getRemindersByVehicle(vehicleId: Long): Flow<List<ReminderEntity>>
    @Query("SELECT * FROM reminders WHERE isActive = 1 AND isCompleted = 0 ORDER BY targetDate ASC")
    fun getActiveReminders(): Flow<List<ReminderEntity>>
    @Query("SELECT * FROM reminders WHERE id = :id")
    fun getReminderById(id: Long): Flow<ReminderEntity?>
    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderByIdOnce(id: Long): ReminderEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long
    @Update
    suspend fun updateReminder(reminder: ReminderEntity)
    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)
    @Query("UPDATE reminders SET isCompleted = 1 WHERE id = :id")
    suspend fun markAsCompleted(id: Long)
}