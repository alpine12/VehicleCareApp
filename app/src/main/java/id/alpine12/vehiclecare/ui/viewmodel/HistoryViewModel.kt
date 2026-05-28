package id.alpine12.vehiclecare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.alpine12.vehiclecare.domain.model.ServiceRecord
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

data class ServiceWithVehicle(
    val service: ServiceRecord,
    val vehicle: Vehicle?
)

enum class SortOrder(val displayName: String) {
    DATE_DESC("Terbaru"),
    DATE_ASC("Terlama"),
    COST_DESC("Termahal"),
    COST_ASC("Termurah")
}

data class HistoryUiState(
    val allServices: List<ServiceWithVehicle> = emptyList(),
    val filteredServices: List<ServiceWithVehicle> = emptyList(),
    val vehicles: List<Vehicle> = emptyList(),
    val selectedVehicleId: Long? = null,
    val sortOrder: SortOrder = SortOrder.DATE_DESC,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val serviceRecordRepository: ServiceRecordRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                serviceRecordRepository.getAllServiceRecords(),
                vehicleRepository.getAllVehicles()
            ) { services, vehicles ->
                val vehicleMap = vehicles.associateBy { it.id }
                val serviceList = services.map { service ->
                    ServiceWithVehicle(service, vehicleMap[service.vehicleId])
                }

                Triple(
                    serviceList,
                    vehicles,
                    serviceList
                )
            }.catch { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = e.message ?: "Gagal memuat data"
                    )
                }
            }.collect { (allServices, vehicles, _) ->
                _uiState.update {
                    val sortedAll = applySorting(allServices, it.sortOrder)
                    val filtered = applyFilter(sortedAll, it.selectedVehicleId)
                    it.copy(
                        allServices = allServices,
                        vehicles = vehicles,
                        filteredServices = filtered,
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
        _uiState.update { state ->
            val sorted = applySorting(state.allServices, state.sortOrder)
            val filtered = applyFilter(sorted, vehicleId)
            state.copy(
                selectedVehicleId = vehicleId,
                filteredServices = filtered
            )
        }
    }

    fun setSortOrder(sortOrder: SortOrder) {
        _uiState.update { state ->
            val sorted = applySorting(state.allServices, sortOrder)
            val filtered = applyFilter(sorted, state.selectedVehicleId)
            state.copy(
                sortOrder = sortOrder,
                filteredServices = filtered
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun applySorting(services: List<ServiceWithVehicle>, sortOrder: SortOrder): List<ServiceWithVehicle> {
        return when (sortOrder) {
            SortOrder.DATE_DESC -> services.sortedByDescending { it.service.date }
            SortOrder.DATE_ASC -> services.sortedBy { it.service.date }
            SortOrder.COST_DESC -> services.sortedByDescending { it.service.cost ?: 0 }
            SortOrder.COST_ASC -> services.sortedBy { it.service.cost ?: 0 }
        }
    }

    private fun applyFilter(services: List<ServiceWithVehicle>, vehicleId: Long?): List<ServiceWithVehicle> {
        return if (vehicleId == null) {
            services
        } else {
            services.filter { it.service.vehicleId == vehicleId }
        }
    }
}
