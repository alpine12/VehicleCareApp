package id.alpine12.vehiclecare.domain.repository

import id.alpine12.vehiclecare.domain.model.ServiceRecord
import kotlinx.coroutines.flow.Flow

interface ServiceRecordRepository {
    fun getAllServiceRecords(): Flow<List<ServiceRecord>>
    fun getServiceRecordsByVehicle(vehicleId: Long): Flow<List<ServiceRecord>>
    fun getServiceRecordById(id: Long): Flow<ServiceRecord?>
    suspend fun getServiceRecordByIdOnce(id: Long): ServiceRecord?
    suspend fun insertServiceRecord(serviceRecord: ServiceRecord): Long
    suspend fun updateServiceRecord(serviceRecord: ServiceRecord)
    suspend fun deleteServiceRecord(id: Long)
    suspend fun getLatestServiceRecord(vehicleId: Long): ServiceRecord?
    fun getTotalCost(): Flow<Long?>
}