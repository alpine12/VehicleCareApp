package id.alpine12.vehiclecare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import id.alpine12.vehiclecare.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY updatedAt DESC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>
    @Query("SELECT * FROM vehicles WHERE id = :id")
    fun getVehicleById(id: Long): Flow<VehicleEntity?>
    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getVehicleByIdOnce(id: Long): VehicleEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity): Long
    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)
    @Query("DELETE FROM vehicles WHERE id = :id")
    suspend fun deleteVehicleById(id: Long)
    @Query("UPDATE vehicles SET currentKm = :km, updatedAt = :updatedAt WHERE id = :vehicleId AND currentKm < :km")
    suspend fun updateKm(vehicleId: Long, km: Int, updatedAt: Long)
}