package id.alpine12.vehiclecare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.alpine12.vehiclecare.domain.model.Reminder
import id.alpine12.vehiclecare.domain.model.Vehicle
import id.alpine12.vehiclecare.domain.repository.ReminderRepository
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

data class ReminderWithVehicle(
    val reminder: Reminder,
    val vehicle: Vehicle?
)

data class ReminderUiState(
    val pendingReminders: List<ReminderWithVehicle> = emptyList(),
    val completedReminders: List<ReminderWithVehicle> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderUiState())
    val uiState: StateFlow<ReminderUiState> = _uiState.asStateFlow()

    init {
        loadReminders()
    }

    private fun loadReminders() {
        viewModelScope.launch {
            combine(
                reminderRepository.getAllReminders(),
                vehicleRepository.getAllVehicles()
            ) { reminders, vehicles ->
                val vehicleMap = vehicles.associateBy { it.id }

                val pending = mutableListOf<ReminderWithVehicle>()
                val completed = mutableListOf<ReminderWithVehicle>()

                reminders.forEach { reminder ->
                    val item = ReminderWithVehicle(reminder, vehicleMap[reminder.vehicleId])
                    if (reminder.isCompleted) {
                        completed.add(item)
                    } else {
                        pending.add(item)
                    }
                }

                // Sort by target date (closest first) for pending
                val sortedPending = pending.sortedBy { it.reminder.targetDate ?: Long.MAX_VALUE }

                ReminderUiState(
                    pendingReminders = sortedPending,
                    completedReminders = completed.sortedByDescending { it.reminder.createdAt },
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = null
                )
            }.catch { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = e.message ?: "Gagal memuat reminder"
                    )
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadReminders()
    }

    fun markAsCompleted(reminderId: Long) {
        viewModelScope.launch {
            try {
                reminderRepository.markAsCompleted(reminderId)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal mengubah status: ${e.message}") }
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

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
