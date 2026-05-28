package id.alpine12.vehiclecare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.alpine12.vehiclecare.domain.model.Reminder
import id.alpine12.vehiclecare.domain.repository.ReminderRepository
import id.alpine12.vehiclecare.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderFormState(
    val vehicleId: Long = 0,
    val vehicleName: String = "",
    val title: String = "",
    val description: String = "",
    val targetDate: Long? = null,
    val targetKm: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ReminderFormViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReminderFormState())
    val state: StateFlow<ReminderFormState> = _state.asStateFlow()

    fun initialize(vehicleId: Long) {
        viewModelScope.launch {
            val vehicle = vehicleRepository.getVehicleByIdOnce(vehicleId)
            _state.update {
                it.copy(
                    vehicleId = vehicleId,
                    vehicleName = vehicle?.name ?: "",
                    targetDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
                )
            }
        }
    }

    fun updateTitle(title: String) {
        _state.update { it.copy(title = title) }
    }

    fun updateDescription(description: String) {
        _state.update { it.copy(description = description) }
    }

    fun updateTargetDate(date: Long?) {
        _state.update { it.copy(targetDate = date) }
    }

    fun updateTargetKm(km: String) {
        _state.update { it.copy(targetKm = km) }
    }

    fun saveReminder() {
        val currentState = _state.value
        if (currentState.title.isBlank()) {
            _state.update { it.copy(errorMessage = "Judul reminder harus diisi") }
            return
        }
        if (currentState.targetDate == null && currentState.targetKm.isBlank()) {
            _state.update { it.copy(errorMessage = "Isi minimal tanggal ATAU kilometer target") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val reminder = Reminder(
                    vehicleId = currentState.vehicleId,
                    title = currentState.title,
                    description = currentState.description,
                    targetDate = currentState.targetDate,
                    targetKm = currentState.targetKm.toIntOrNull()
                )
                reminderRepository.insertReminder(reminder)
                _state.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "Gagal menyimpan: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
