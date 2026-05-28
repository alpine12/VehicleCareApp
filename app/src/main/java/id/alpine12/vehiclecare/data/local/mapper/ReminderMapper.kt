package id.alpine12.vehiclecare.data.local.mapper

import id.alpine12.vehiclecare.data.local.entity.ReminderEntity
import id.alpine12.vehiclecare.domain.model.Reminder

fun ReminderEntity.toDomain(): Reminder = Reminder(
    id = id,
    vehicleId = vehicleId,
    title = title,
    description = description,
    targetDate = targetDate,
    targetKm = targetKm,
    isActive = isActive,
    isCompleted = isCompleted,
    createdAt = createdAt
)

fun Reminder.toEntity(): ReminderEntity = ReminderEntity(
    id = id,
    vehicleId = vehicleId,
    title = title,
    description = description,
    targetDate = targetDate,
    targetKm = targetKm,
    isActive = isActive,
    isCompleted = isCompleted,
    createdAt = createdAt
)