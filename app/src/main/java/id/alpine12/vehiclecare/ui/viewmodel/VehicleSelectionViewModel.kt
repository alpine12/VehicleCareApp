package id.alpine12.vehiclecare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.alpine12.vehiclecare.domain.model.Vehicle
import id.alpine12.vehiclecare.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VehicleSelectionUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class VehicleSelectionViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehicleSelectionUiState())
    val uiState: StateFlow<VehicleSelectionUiState> = _uiState.asStateFlow()

    init {
        loadVehicles()
    }

    private fun loadVehicles() {
        viewModelScope.launch {
            vehicleRepository.getAllVehicles()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Gagal memuat kendaraan"
                        )
                    }
                }
                .collect { vehicles ->
                    _uiState.update {
                        it.copy(
                            vehicles = vehicles,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadVehicles()
    }
}
