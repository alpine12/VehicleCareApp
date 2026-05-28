package id.alpine12.vehiclecare.domain.repository

import id.alpine12.vehiclecare.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getAllReminders(): Flow<List<Reminder>>
    fun getRemindersByVehicle(vehicleId: Long): Flow<List<Reminder>>
    fun getActiveReminders(): Flow<List<Reminder>>
    fun getReminderById(id: Long): Flow<Reminder?>
    suspend fun getReminderByIdOnce(id: Long): Reminder?
    suspend fun insertReminder(reminder: Reminder): Long
    suspend fun updateReminder(reminder: Reminder)
    suspend fun deleteReminder(id: Long)
    suspend fun markAsCompleted(id: Long)
}