package id.alpine12.vehiclecare.di

import id.alpine12.vehiclecare.data.repository.ReminderRepositoryImpl
import id.alpine12.vehiclecare.data.repository.ServiceRecordRepositoryImpl
import id.alpine12.vehiclecare.data.repository.VehicleRepositoryImpl
import id.alpine12.vehiclecare.domain.repository.ReminderRepository
import id.alpine12.vehiclecare.domain.repository.ServiceRecordRepository
import id.alpine12.vehiclecare.domain.repository.VehicleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(impl: VehicleRepositoryImpl): VehicleRepository

    @Binds
    @Singleton
    abstract fun bindServiceRecordRepository(impl: ServiceRecordRepositoryImpl): ServiceRecordRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository
}