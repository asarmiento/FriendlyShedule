@Singleton
class StatsFilterManager @Inject constructor() {
    private val _currentFilter = MutableStateFlow(StatsFilterState())
    val currentFilter: StateFlow<StatsFilterState> = _currentFilter.asStateFlow()

    fun updateFilter(update: (StatsFilterState) -> StatsFilterState) {
        _currentFilter.value = update(_currentFilter.value)
    }

    fun resetFilters() {
        _currentFilter.value = StatsFilterState()
    }
}

data class StatsFilterState(
    val dateRange: DateRange = DateRange.WEEK,
    val customDateRange: Pair<LocalDate, LocalDate>? = null,
    val timeRange: Pair<LocalTime, LocalTime>? = null,
    val showTypes: Set<AppointmentType> = AppointmentType.values().toSet(),
    val showStatuses: Set<AppointmentStatus> = AppointmentStatus.values().toSet(),
    val minAttendanceRate: Float? = null,
    val maxAttendanceRate: Float? = null,
    val sortBy: StatsSortOption = StatsSortOption.DATE_DESC
)

enum class AppointmentType {
    REGULAR, EMERGENCY, FOLLOW_UP
}

enum class AppointmentStatus {
    PENDING, ATTENDED, ABSENT
}

enum class StatsSortOption {
    DATE_ASC, DATE_DESC,
    ATTENDANCE_RATE_ASC, ATTENDANCE_RATE_DESC,
    COUNT_ASC, COUNT_DESC
} 