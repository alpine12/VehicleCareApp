package id.alpine12.vehiclecare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.alpine12.vehiclecare.domain.model.ServiceRecord
import id.alpine12.vehiclecare.domain.model.ServiceType
import id.alpine12.vehiclecare.domain.repository.ServiceRecordRepository
import id.alpine12.vehiclecare.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ServiceFormState(
    val vehicleId: Long = 0,
    val vehicleName: String = "",
    val type: ServiceType = ServiceType.GANTI_OLI,
    val date: Long = System.currentTimeMillis(),
    val kilometer: String = "",
    val productBrand: String = "",
    val cost: String = "",
    val workshop: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ServiceFormViewModel @Inject constructor(
    private val serviceRecordRepository: ServiceRecordRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ServiceFormState())
    val state: StateFlow<ServiceFormState> = _state.asStateFlow()

    fun initialize(vehicleId: Long) {
        viewModelScope.launch {
            val v = vehicleRepository.getVehicleByIdOnce(vehicleId)
            _state.update { it.copy(
                vehicleId = vehicleId,
                vehicleName = v?.name ?: "",
                kilometer = v?.currentKm?.toString() ?: "",
                date = System.currentTimeMillis()
            ) }
        }
    }

    fun updateType(t: ServiceType) = _state.update { it.copy(type = t) }
    fun updateDate(d: Long) = _state.update { it.copy(date = d) }
    fun updateKilometer(km: String) = _state.update { it.copy(kilometer = km) }
    fun updateProductBrand(b: String) = _state.update { it.copy(productBrand = b) }
    fun updateCost(c: String) = _state.update { it.copy(cost = c) }
    fun updateWorkshop(w: String) = _state.update { it.copy(workshop = w) }
    fun updateNotes(n: String) = _state.update { it.copy(notes = n) }

    fun saveService() {
        val s = _state.value
        if (s.kilometer.isBlank()) {
            _state.update { it.copy(errorMessage = "Kilometer harus diisi") }
            return
        }

        val km = s.kilometer.toIntOrNull()
        if (km == null) {
            _state.update { it.copy(errorMessage = "Format km tidak valid") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val record = ServiceRecord(
                    vehicleId = s.vehicleId,
                    type = s.type,
                    date = s.date,
                    kilometer = km,
                    productBrand = s.productBrand,
                    cost = s.cost.toIntOrNull() ?: 0,
                    workshop = s.workshop,
                    notes = s.notes
                )
                serviceRecordRepository.insertServiceRecord(record)
                vehicleRepository.updateKm(s.vehicleId, km)
                _state.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "Gagal: ${e.message}") }
            }
        }
    }

    fun clearError() = _state.update { it.copy(errorMessage = null) }
}