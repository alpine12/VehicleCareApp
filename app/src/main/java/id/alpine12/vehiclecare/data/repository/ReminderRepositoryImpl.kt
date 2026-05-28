package id.alpine12.vehiclecare.data.repository

import id.alpine12.vehiclecare.data.local.dao.ReminderDao
import id.alpine12.vehiclecare.data.local.mapper.toDomain
import id.alpine12.vehiclecare.data.local.mapper.toEntity
import id.alpine12.vehiclecare.domain.model.Reminder
import id.alpine12.vehiclecare.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override fun getAllReminders(): Flow<List<Reminder>> =
        reminderDao.getAllReminders().map { entities -> entities.map { it.toDomain() } }

    override fun getRemindersByVehicle(vehicleId: Long): Flow<List<Reminder>> =
        reminderDao.getRemindersByVehicle(vehicleId).map { entities -> entities.map { it.toDomain() } }


    override fun getActiveReminders(): Flow<List<Reminder>> =
        reminderDao.getActiveReminders().map { entities -> entities.map { it.toDomain() } }


    override fun getReminderById(id: Long): Flow<Reminder?> =
        reminderDao.getReminderById(id).map { it?.toDomain() }

    override suspend fun getReminderByIdOnce(id: Long): Reminder? =
        reminderDao.getReminderByIdOnce(id)?.toDomain()

    override suspend fun insertReminder(reminder: Reminder): Long =
        reminderDao.insertReminder(reminder.toEntity())

    override suspend fun updateReminder(reminder: Reminder) =
        reminderDao.updateReminder(reminder.toEntity())


    override suspend fun deleteReminder(id: Long) =
        reminderDao.deleteReminderById(id)

    override suspend fun markAsCompleted(id: Long) =
        reminderDao.markAsCompleted(id)
}