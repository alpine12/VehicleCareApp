package id.alpine12.vehiclecare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import id.alpine12.vehiclecare.data.local.entity.ServiceRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceRecordDao {
    @Query("SELECT * FROM service_records ORDER BY date DESC")
    fun getAllServiceRecords(): Flow<List<ServiceRecordEntity>>
    @Query("SELECT * FROM service_records WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun getServiceRecordsByVehicle(vehicleId: Long): Flow<List<ServiceRecordEntity>>
    @Query("SELECT * FROM service_records WHERE id = :id")
    fun getServiceRecordById(id: Long): Flow<ServiceRecordEntity?>
    @Query("SELECT * FROM service_records WHERE id = :id")
    suspend fun getServiceRecordByIdOnce(id: Long): ServiceRecordEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceRecord(serviceRecord: ServiceRecordEntity): Long
    @Update
    suspend fun updateServiceRecord(serviceRecord: ServiceRecordEntity)
    @Query("DELETE FROM service_records WHERE id = :id")
    suspend fun deleteServiceRecordById(id: Long)
    @Query("SELECT * FROM service_records WHERE vehicleId = :vehicleId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestServiceRecord(vehicleId: Long): ServiceRecordEntity?
    @Query("SELECT SUM(cost) FROM service_records WHERE cost IS NOT NULL")
    fun getTotalCost(): Flow<Long?>
}