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
import javax.inject.Inject

data class VehicleStats(
    val vehicle: Vehicle,
    val serviceCount: Int,
    val totalCost: Long
)

data class StatsUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val allServices: List<ServiceRecord> = emptyList(),
    val totalVehicles: Int = 0,
    val totalServices: Int = 0,
    val totalCost: Long = 0,
    val serviceByType: Map<ServiceType, Int> = emptyMap(),
    val vehicleStats: List<VehicleStats> = emptyList(),
    val selectedVehicleId: Long? = null,
    val topServiceTypes: List<Pair<ServiceType, Int>> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val serviceRecordRepository: ServiceRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                vehicleRepository.getAllVehicles(),
                serviceRecordRepository.getAllServiceRecords(),
                serviceRecordRepository.getTotalCost()
            ) { vehicles, services, totalCostNullable ->
                Triple(
                    vehicles,
                    services,
                    totalCostNullable ?: 0L
                )
            }.catch { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = e.message ?: "Gagal memuat data"
                    )
                }
            }.collect { (vehicles, services, totalCost) ->
                val vehicleMap = vehicles.associateBy { it.id }

                // Calculate stats per vehicle
                val vehicleStats = vehicles.map { vehicle ->
                    val vehicleServices = services.filter { it.vehicleId == vehicle.id }
                    VehicleStats(
                        vehicle = vehicle,
                        serviceCount = vehicleServices.size,
                        totalCost = vehicleServices.sumOf { it.cost?.toLong() ?: 0L }
                    )
                }.sortedByDescending { it.totalCost }

                // Calculate service counts by type
                val serviceByType = services
                    .groupBy { it.type }
                    .mapValues { it.value.size }
                    .filter { it.value > 0 }

                // Get top 3 service types
                val topServiceTypes = serviceByType.entries
                    .sortedByDescending { it.value }
                    .take(3)

                _uiState.update {
                    it.copy(
                        vehicles = vehicles,
                        allServices = services,
                        totalVehicles = vehicles.size,
                        totalServices = services.size,
                        totalCost = totalCost,
                        serviceByType = serviceByType,
                        vehicleStats = vehicleStats,
                        topServiceTypes = topServiceTypes.map {item -> item.toPair() },
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

    fun selectVehicle(vehicleId: Long?) {
        _uiState.update { it.copy(selectedVehicleId = vehicleId) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
