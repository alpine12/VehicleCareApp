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

data class HomeUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadVehicles()
    }

    fun loadVehicles() {
        viewModelScope.launch {
            vehicleRepository.getAllVehicles()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = e.message ?: "Gagal memuat data"
                        )
                    }
                }
                .collect { vehicles ->
                    _uiState.update {
                        it.copy(
                            vehicles = vehicles,
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
        loadVehicles()
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
