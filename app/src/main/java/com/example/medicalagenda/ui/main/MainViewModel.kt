@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AppointmentRepository,
    private val preferencesManager: PreferencesManager,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _customerValidation = MutableStateFlow<CustomerValidationState>(CustomerValidationState.Initial)
    val customerValidation: StateFlow<CustomerValidationState> = _customerValidation.asStateFlow()

    private val blockedTimes = mutableSetOf<String>()
    private var weekendEnabled = false

    private val _availableTimeSlots = MutableStateFlow<List<String>>(emptyList())
    val availableTimeSlots: StateFlow<List<String>> = _availableTimeSlots.asStateFlow()

    private var selectedCustomerId: Int? = null
    private var selectedCustomerName: String? = null
    var selectedTime: String? = null
        set(value) {
            field = value
            updateAppointmentData()
        }

    private val _appointmentData = MutableStateFlow<AppointmentData?>(null)
    val appointmentData: StateFlow<AppointmentData?> = _appointmentData.asStateFlow()

    private val _appointmentGroups = MutableStateFlow<List<AppointmentGroup>>(emptyList())
    val appointmentGroups: StateFlow<List<AppointmentGroup>> = _appointmentGroups.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _filterOptions = MutableStateFlow(FilterOptions())

    init {
        loadSettings()
    }

    private fun loadSettings() {
        // Cargar configuraciones guardadas
        weekendEnabled = preferencesManager.isWeekendEnabled()
        blockedTimes.addAll(preferencesManager.getBlockedTimes())
    }

    fun validateCustomer(identification: String) {
        viewModelScope.launch {
            try {
                repository.validateCustomer(identification).fold(
                    onSuccess = { response ->
                        _customerValidation.value = CustomerValidationState.Success(response.nombre)
                    },
                    onFailure = {
                        _customerValidation.value = CustomerValidationState.Error(
                            it.message ?: "Error al validar el cliente"
                        )
                    }
                )
            } catch (e: Exception) {
                _customerValidation.value = CustomerValidationState.Error(
                    e.message ?: "Error al validar el cliente"
                )
            }
        }
    }

    fun isValidDate(calendar: Calendar): Boolean {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return if (weekendEnabled) {
            true
        } else {
            dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY
        }
    }

    fun isValidTime(time: String): Boolean {
        return !blockedTimes.contains(time)
    }

    fun loadAvailableTimeSlots(date: String) {
        viewModelScope.launch {
            val allSlots = DateUtils.generateTimeSlots()
            val bookedSlots = repository.getAppointments(date)
                .map { it.init_time.substring(0, 5) }
                .toSet()
            
            _availableTimeSlots.value = allSlots.filter { time ->
                !bookedSlots.contains(time) && !blockedTimes.contains(time)
            }
        }
    }

    fun createAppointment(customerId: Int, date: String, time: String, description: String?) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val request = AppointmentRequest(
                    customer_id = customerId,
                    init_date = date,
                    init_time = time,
                    description = description,
                    title = null
                )
                repository.createAppointment(request).fold(
                    onSuccess = { appointment ->
                        loadAppointments(date)
                        _uiState.value = UiState.Success
                        // Programar recordatorio
                        notificationHelper.scheduleAppointmentReminder(appointment)
                    },
                    onFailure = {
                        _uiState.value = UiState.Error(it.message ?: "Error al crear la cita")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al crear la cita")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            preferencesManager.clearSession()
        }
    }

    private fun updateAppointmentData() {
        val customerId = selectedCustomerId
        val customerName = selectedCustomerName
        val time = selectedTime

        if (customerId != null && customerName != null && time != null) {
            val currentDate = getCurrentDate()
            _appointmentData.value = AppointmentData(
                patientName = customerName,
                formattedDateTime = "${formatDateForDisplay(parseDate(currentDate))} - $time",
                description = null,
                customerId = customerId,
                date = currentDate,
                time = time
            )
        }
    }

    fun onCustomerValidated(id: Int, name: String) {
        selectedCustomerId = id
        selectedCustomerName = name
        updateAppointmentData()
    }

    private fun parseDate(dateStr: String): Date {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr) ?: Date()
    }

    private fun formatDateForDisplay(date: Date): String {
        return SimpleDateFormat("dd MMM, yyyy", Locale("es")).format(date)
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun loadAppointments(date: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val appointments = repository.getAppointments(date)
                val groups = appointments
                    .groupBy { it.init_time.substring(0, 5) }
                    .map { (time, appointments) ->
                        AppointmentGroup(time, appointments)
                    }
                    .sortedBy { it.time }
                _appointmentGroups.value = groups
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun updateFilters(filters: FilterOptions) {
        _filterOptions.value = filters
        applyFilters()
    }

    private fun applyFilters() {
        viewModelScope.launch {
            val query = _searchQuery.value.lowercase()
            val filters = _filterOptions.value
            
            val filteredGroups = _appointmentGroups.value.map { group ->
                group.copy(
                    appointments = group.appointments.filter { appointment ->
                        val matchesSearch = appointment.customer?.name?.lowercase()
                            ?.contains(query) ?: false
                        val matchesFilter = when {
                            appointment.attendance && !filters.showAttended -> false
                            !appointment.attendance && !filters.showPending -> false
                            else -> true
                        }
                        matchesSearch && matchesFilter
                    }
                )
            }.filter { it.appointments.isNotEmpty() }

            _filteredAppointmentGroups.value = filteredGroups
        }
    }

    private val _filteredAppointmentGroups = MutableStateFlow<List<AppointmentGroup>>(emptyList())
    val filteredAppointmentGroups: StateFlow<List<AppointmentGroup>> = _filteredAppointmentGroups

    fun getCurrentFilters(): FilterOptions {
        return _filterOptions.value
    }
}

sealed class CustomerValidationState {
    object Initial : CustomerValidationState()
    data class Success(val name: String) : CustomerValidationState()
    data class Error(val message: String) : CustomerValidationState()
} 