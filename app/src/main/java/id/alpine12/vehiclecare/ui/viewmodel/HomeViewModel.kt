package id.alpine12.vehiclecare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.alpine12.vehiclecare.domain.model.ServiceRecord
import id.alpine12.vehiclecare.domain.model.ServiceType
import id.alpine12.vehiclecare.domain.model.Vehicle
import id.alpine12.vehiclecare.domain.repository.ServiceRecordRepository
import id.alpine12.vehiclecare.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val totalServicesThisMonth: Int = 0,
    val totalCostThisMonth: Long = 0,
    val lastServiceKmMap: Map<Long, Long> = emptyMap(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val serviceRecordRepository: ServiceRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)

            combine(
                vehicleRepository.getAllVehicles(),
                serviceRecordRepository.getAllServiceRecords()
            ) { vehicles: List<Vehicle>, allServices: List<ServiceRecord> ->
                // Calculate services this month
                val servicesThisMonth = allServices.filter { service ->
                    val serviceCalendar = Calendar.getInstance().apply { timeInMillis = service.date }
                    serviceCalendar.get(Calendar.MONTH) == currentMonth &&
                            serviceCalendar.get(Calendar.YEAR) == currentYear
                }

                // Calculate total cost this month
                val costThisMonth = servicesThisMonth.sumOf { it.cost?.toLong() ?: 0L }

                // Get last service km for each vehicle (oli service)
                val lastServiceKmMap = mutableMapOf<Long, Long>()
                vehicles.forEach { vehicle ->
                    val vehicleServices = allServices
                        .filter { it.vehicleId == vehicle.id }
                        .sortedByDescending { it.date }

                    // Find last oli service
                    val lastOliService = vehicleServices.firstOrNull { it.type == ServiceType.GANTI_OLI }
                    if (lastOliService != null) {
                        lastServiceKmMap[vehicle.id] = lastOliService.kilometer.toLong()
                    }
                }

                Triple(
                    vehicles,
                    servicesThisMonth.size to costThisMonth,
                    lastServiceKmMap
                )
            }.catch { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = e.message ?: "Gagal memuat data"
                    )
                }
            }.collect { (vehicles, stats, lastServiceKmMap) ->
                _uiState.update {
                    it.copy(
                        vehicles = vehicles,
                        totalServicesThisMonth = stats.first,
                        totalCostThisMonth = stats.second,
                        lastServiceKmMap = lastServiceKmMap,
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadData()
    }

    fun deleteVehicle(vehicleId: Long) {
        viewModelScope.launch {
            try {
                vehicleRepository.deleteVehicle(vehicleId)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal menghapus: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
