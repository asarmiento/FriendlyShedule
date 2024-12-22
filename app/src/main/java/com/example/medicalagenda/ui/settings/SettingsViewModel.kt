@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _blockedTimes = MutableStateFlow<Set<String>>(emptySet())
    val blockedTimes: StateFlow<Set<String>> = _blockedTimes.asStateFlow()

    private val _weekendEnabled = MutableStateFlow(false)
    val weekendEnabled: StateFlow<Boolean> = _weekendEnabled.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _weekendEnabled.value = preferencesManager.isWeekendEnabled()
        _blockedTimes.value = preferencesManager.getBlockedTimes()
    }

    fun toggleWeekendEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setWeekendEnabled(enabled)
            _weekendEnabled.value = enabled
        }
    }

    fun addBlockedTime(time: String) {
        viewModelScope.launch {
            val currentTimes = _blockedTimes.value.toMutableSet()
            currentTimes.add(time)
            preferencesManager.setBlockedTimes(currentTimes)
            _blockedTimes.value = currentTimes
        }
    }

    fun removeBlockedTime(time: String) {
        viewModelScope.launch {
            val currentTimes = _blockedTimes.value.toMutableSet()
            currentTimes.remove(time)
            preferencesManager.setBlockedTimes(currentTimes)
            _blockedTimes.value = currentTimes
        }
    }
} 