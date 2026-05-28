package id.alpine12.vehiclecare.data.repository

import id.alpine12.vehiclecare.data.local.dao.ServiceRecordDao
import id.alpine12.vehiclecare.data.local.mapper.toDomain
import id.alpine12.vehiclecare.data.local.mapper.toEntity
import id.alpine12.vehiclecare.domain.model.ServiceRecord
import id.alpine12.vehiclecare.domain.repository.ServiceRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRecordRepositoryImpl @Inject constructor(
    private val serviceRecordDao: ServiceRecordDao
) : ServiceRecordRepository {

    override fun getAllServiceRecords(): Flow<List<ServiceRecord>> =
        serviceRecordDao.getAllServiceRecords().map { entities -> entities.map { it.toDomain() } }


    override fun getServiceRecordsByVehicle(vehicleId: Long): Flow<List<ServiceRecord>> =
        serviceRecordDao.getServiceRecordsByVehicle(vehicleId).map { entities -> entities.map { it.toDomain() } }

    override fun getServiceRecordById(id: Long): Flow<ServiceRecord?> =
        serviceRecordDao.getServiceRecordById(id).map { it?.toDomain() }

    override suspend fun getServiceRecordByIdOnce(id: Long): ServiceRecord? =
        serviceRecordDao.getServiceRecordByIdOnce(id)?.toDomain()

    override suspend fun insertServiceRecord(serviceRecord: ServiceRecord): Long =
        serviceRecordDao.insertServiceRecord(serviceRecord.toEntity())

    override suspend fun updateServiceRecord(serviceRecord: ServiceRecord) =
        serviceRecordDao.updateServiceRecord(serviceRecord.toEntity())

    override suspend fun deleteServiceRecord(id: Long) =
        serviceRecordDao.deleteServiceRecordById(id)

    override suspend fun getLatestServiceRecord(vehicleId: Long): ServiceRecord? =
        serviceRecordDao.getLatestServiceRecord(vehicleId)?.toDomain()

    override fun getTotalCost(): Flow<Long?> = serviceRecordDao.getTotalCost()
}