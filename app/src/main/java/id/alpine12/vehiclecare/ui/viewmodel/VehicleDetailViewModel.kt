package id.alpine12.vehiclecare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.alpine12.vehiclecare.domain.model.Reminder
import id.alpine12.vehiclecare.domain.model.ServiceRecord
import id.alpine12.vehiclecare.domain.model.ServiceType
import id.alpine12.vehiclecare.domain.model.Vehicle
import id.alpine12.vehiclecare.domain.repository.ReminderRepository
import id.alpine12.vehiclecare.domain.repository.ServiceRecordRepository
import id.alpine12.vehiclecare.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VehicleDetailUiState(
    val vehicle: Vehicle? = null,
    val serviceRecords: List<ServiceRecord> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val lastOilServiceKm: Long? = null,
    val lastOilServiceDate: Long? = null,
    val totalServiceCount: Int = 0,
    val totalServiceCost: Long = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val showAllServices: Boolean = false
)

@HiltViewModel
class VehicleDetailViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val serviceRecordRepository: ServiceRecordRepository,
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _vehicleId = MutableStateFlow<Long?>(null)
    private val _uiState = MutableStateFlow(VehicleDetailUiState())
    val uiState: StateFlow<VehicleDetailUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadVehicle(vehicleId: Long) {
        _vehicleId.value = vehicleId
        viewModelScope.launch {
            combine(
                vehicleRepository.getVehicleById(vehicleId)
                    .catch { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = e.message ?: "Gagal memuat data kendaraan"
                            )
                        }
                    },
                serviceRecordRepository.getServiceRecordsByVehicle(vehicleId)
                    .catch { /* ignore service errors */ },
                reminderRepository.getRemindersByVehicle(vehicleId)
                    .catch { /* ignore reminder errors */ }
            ) { vehicle, services, reminders ->
                // Find last oil service
                val oilServices = services.filter { it.type == ServiceType.GANTI_OLI }
                val lastOilService = oilServices.maxByOrNull { it.date }

                // Calculate total stats
                val totalCost = services.sumOf { it.cost?.toLong() ?: 0L }

                _uiState.update {
                    it.copy(
                        vehicle = vehicle,
                        serviceRecords = services,
                        reminders = reminders,
                        lastOilServiceKm = lastOilService?.kilometer?.toLong(),
                        lastOilServiceDate = lastOilService?.date,
                        totalServiceCount = services.size,
                        totalServiceCost = totalCost,
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    fun toggleShowAllServices() {
        _uiState.update { it.copy(showAllServices = !it.showAllServices) }
    }

    fun deleteVehicle(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                _vehicleId.value?.let { id ->
                    vehicleRepository.deleteVehicle(id)
                    onComplete()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal menghapus: ${e.message}") }
            }
        }
    }

    fun deleteServiceRecord(serviceId: Long) {
        viewModelScope.launch {
            try {
                serviceRecordRepository.deleteServiceRecord(serviceId)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal menghapus: ${e.message}") }
            }
        }
    }

    fun deleteReminder(reminderId: Long) {
        viewModelScope.launch {
            try {
                reminderRepository.deleteReminder(reminderId)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal menghapus: ${e.message}") }
            }
        }
    }

    fun markReminderCompleted(reminderId: Long) {
        viewModelScope.launch {
            try {
                reminderRepository.markAsCompleted(reminderId)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal mengubah status: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
