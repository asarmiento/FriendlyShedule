import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val repository: AppointmentRepository
) : ViewModel() {

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadAppointments(date: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = repository.getAppointments(date)
                _appointments.value = result
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun markAsAttended(appointmentId: Int, attended: Boolean) {
        viewModelScope.launch {
            try {
                repository.updateAppointmentStatus(appointmentId, attended)
                // Actualizar la lista local
                val updatedList = _appointments.value.map { appointment ->
                    if (appointment.id == appointmentId) {
                        appointment.copy(attendance = attended)
                    } else {
                        appointment
                    }
                }
                _appointments.value = updatedList
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al actualizar la cita")
            }
        }
    }
}

sealed class UiState {
    object Loading : UiState()
    object Success : UiState()
    data class Error(val message: String) : UiState()
} 