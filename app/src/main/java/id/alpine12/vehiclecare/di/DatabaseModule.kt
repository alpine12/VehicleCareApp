package id.alpine12.vehiclecare.di

import android.content.Context
import androidx.room.Room
import id.alpine12.vehiclecare.data.local.dao.ReminderDao
import id.alpine12.vehiclecare.data.local.dao.ServiceRecordDao
import id.alpine12.vehiclecare.data.local.dao.VehicleDao
import id.alpine12.vehiclecare.data.local.database.MIGRATION_1_2
import id.alpine12.vehiclecare.data.local.database.VehicleCareDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VehicleCareDatabase {
        return Room.databaseBuilder(
            context,
            VehicleCareDatabase::class.java,
            VehicleCareDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideVehicleDao(database: VehicleCareDatabase): VehicleDao = database.vehicleDao()

    @Provides
    @Singleton
    fun provideServiceRecordDao(database: VehicleCareDatabase): ServiceRecordDao = database.serviceRecordDao()

    @Provides
    @Singleton
    fun provideReminderDao(database: VehicleCareDatabase): ReminderDao = database.reminderDao()
}