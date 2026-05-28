package id.alpine12.vehiclecare.data.local.mapper

import id.alpine12.vehiclecare.data.local.entity.ServiceRecordEntity
import id.alpine12.vehiclecare.domain.model.ServiceRecord
import id.alpine12.vehiclecare.domain.model.ServiceType

fun ServiceRecordEntity.toDomain(): ServiceRecord = ServiceRecord(
    id = id,
    vehicleId = vehicleId,
    type = ServiceType.valueOf(type),
    date = date,
    kilometer = kilometer,
    productBrand = productBrand,
    cost = cost,
    workshop = workshop,
    notes = notes,
    createdAt = createdAt
)

fun ServiceRecord.toEntity(): ServiceRecordEntity = ServiceRecordEntity(
    id = id,
    vehicleId = vehicleId,
    type = type.name,
    date = date,
    kilometer = kilometer,
    productBrand = productBrand,
    cost = cost,
    workshop = workshop,
    notes = notes,
    createdAt = createdAt
)