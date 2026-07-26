package id.alpine12.vehiclecare.data.local.mapper

import id.alpine12.vehiclecare.data.local.entity.VehicleEntity
import id.alpine12.vehiclecare.domain.model.Vehicle
import id.alpine12.vehiclecare.domain.model.VehicleType

fun VehicleEntity.toDomain(): Vehicle = Vehicle(
    id = id,
    name = name,
    type = VehicleType.valueOf(type),
    brand = brand,
    model = model,
    year = year,
    licensePlate = licensePlate,
    currentKm = currentKm,
    oilIntervalKm = oilIntervalKm,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Vehicle.toEntity(): VehicleEntity = VehicleEntity(
    id = id,
    name = name,
    type = type.name,
    brand = brand,
    model = model,
    year = year,
    licensePlate = licensePlate,
    currentKm = currentKm,
    oilIntervalKm = oilIntervalKm,
    createdAt = createdAt,
    updatedAt = updatedAt
)
