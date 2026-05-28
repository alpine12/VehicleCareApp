package id.alpine12.vehiclecare.domain.repository

import id.alpine12.vehiclecare.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun getAllVehicles(): Flow<List<Vehicle>>
    fun getVehicleById(id: Long): Flow<Vehicle?>
    suspend fun getVehicleByIdOnce(id: Long): Vehicle?
    suspend fun insertVehicle(vehicle: Vehicle): Long
    suspend fun updateVehicle(vehicle: Vehicle)
    suspend fun deleteVehicle(id: Long)
    suspend fun updateKm(vehicleId: Long, km: Int)
}