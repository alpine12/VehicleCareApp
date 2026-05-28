package id.alpine12.vehiclecare.data.repository

import id.alpine12.vehiclecare.data.local.dao.VehicleDao
import id.alpine12.vehiclecare.data.local.mapper.toDomain
import id.alpine12.vehiclecare.data.local.mapper.toEntity
import id.alpine12.vehiclecare.domain.model.Vehicle
import id.alpine12.vehiclecare.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val vehicleDao: VehicleDao
) : VehicleRepository {

    override fun getAllVehicles(): Flow<List<Vehicle>> =
        vehicleDao.getAllVehicles().map { entities -> entities.map { it.toDomain() } }

    override fun getVehicleById(id: Long): Flow<Vehicle?> =
        vehicleDao.getVehicleById(id).map { it?.toDomain() }

    override suspend fun getVehicleByIdOnce(id: Long): Vehicle? =
        vehicleDao.getVehicleByIdOnce(id)?.toDomain()

    override suspend fun insertVehicle(vehicle: Vehicle): Long =
        vehicleDao.insertVehicle(vehicle.toEntity())


    override suspend fun updateVehicle(vehicle: Vehicle) =
        vehicleDao.updateVehicle(vehicle.toEntity())

    override suspend fun deleteVehicle(id: Long) =
        vehicleDao.deleteVehicleById(id)


    override suspend fun updateKm(vehicleId: Long, km: Int) =
        vehicleDao.updateKm(vehicleId, km, System.currentTimeMillis())
}