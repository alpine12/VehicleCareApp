package id.alpine12.vehiclecare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.alpine12.vehiclecare.domain.model.Vehicle
import id.alpine12.vehiclecare.domain.model.VehicleType
import id.alpine12.vehiclecare.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VehicleFormState(
    val name: String = "",
    val type: VehicleType = VehicleType.MOTOR,
    val brand: String = "",
    val model: String = "",
    val year: String = "",
    val licensePlate: String = "",
    val currentKm: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class VehicleFormViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(VehicleFormState())
    val state: StateFlow<VehicleFormState> = _state.asStateFlow()

    fun loadVehicle(vehicleId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val v = vehicleRepository.getVehicleByIdOnce(vehicleId)
            _state.update {
                if (v != null) {
                    it.copy(
                        name = v.name,
                        type = v.type,
                        brand = v.brand,
                        model = v.model,
                        year = v.year.toString(),
                        licensePlate = v.licensePlate,
                        currentKm = v.currentKm.toString(),
                        isLoading = false
                    )
                } else {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    fun updateName(n: String) {
        _state.update { it.copy(name = n) }
    }

    fun updateType(t: VehicleType) {
        _state.update { it.copy(type = t) }
    }

    fun updateBrand(b: String) {
        _state.update { it.copy(brand = b) }
    }

    fun updateModel(m: String) {
        _state.update { it.copy(model = m) }
    }

    fun updateYear(y: String) {
        _state.update { it.copy(year = y) }
    }

    fun updateLicensePlate(lp: String) {
        _state.update { it.copy(licensePlate = lp) }
    }

    fun updateCurrentKm(km: String) {
        _state.update { it.copy(currentKm = km) }
    }

    fun saveVehicle(vehicleId: Long? = null) {
        val s = _state.value
        if (s.name.isBlank() || s.brand.isBlank() || s.model.isBlank() || s.year.isBlank() || s.currentKm.isBlank()) {
            _state.update { it.copy(errorMessage = "Mohon isi semua field yang wajib") }
            return
        }
        val year = s.year.toIntOrNull()
        val km = s.currentKm.toIntOrNull()
        if (year == null || km == null) {
            _state.update { it.copy(errorMessage = "Format tahun atau km tidak valid") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val v = Vehicle(
                    id = vehicleId ?: 0,
                    name = s.name,
                    type = s.type,
                    brand = s.brand,
                    model = s.model,
                    year = year,
                    licensePlate = s.licensePlate,
                    currentKm = km
                )
                if (vehicleId != null) {
                    vehicleRepository.updateVehicle(v)
                } else {
                    vehicleRepository.insertVehicle(v)
                }
                _state.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "Gagal: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
