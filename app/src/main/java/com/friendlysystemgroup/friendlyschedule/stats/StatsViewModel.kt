@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsDao: StatsDao,
    private val statsFilterManager: StatsFilterManager
) : ViewModel() {

    val filter = statsFilterManager.currentFilter

    fun updateFilter(update: (StatsFilterState) -> StatsFilterState) {
        statsFilterManager.updateFilter(update)
    }

    fun resetFilters() {
        statsFilterManager.resetFilters()
    }

    private val _stats = MutableStateFlow<AppointmentStats?>(null)
    val stats: StateFlow<AppointmentStats?> = _stats.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val currentFilter = filter.value
            val (startDate, endDate) = when {
                currentFilter.customDateRange != null -> {
                    currentFilter.customDateRange.let { (start, end) ->
                        Pair(
                            start.format(DateTimeFormatter.ISO_DATE),
                            end.format(DateTimeFormatter.ISO_DATE)
                        )
                    }
                }
                else -> calculateDateRange(currentFilter.dateRange)
            }

            val stats = statsDao.getAppointmentStats(startDate, endDate)
            
            // Aplicar filtros
            val filteredStats = stats.copy(
                total = stats.total,
                attended = if (currentFilter.showStatuses.contains(AppointmentStatus.ATTENDED)) 
                    stats.attended else 0,
                pending = if (currentFilter.showStatuses.contains(AppointmentStatus.PENDING)) 
                    stats.pending else 0,
                absent = if (currentFilter.showStatuses.contains(AppointmentStatus.ABSENT)) 
                    stats.absent else 0
            )

            _stats.value = filteredStats
        }
    }

    private fun calculateDateRange(range: DateRange): Pair<String, String> {
        val endDate = LocalDate.now()
        val startDate = when (range) {
            DateRange.WEEK -> endDate.minusWeeks(1)
            DateRange.MONTH -> endDate.minusMonths(1)
            DateRange.YEAR -> endDate.minusYears(1)
        }
        return Pair(
            startDate.format(DateTimeFormatter.ISO_DATE),
            endDate.format(DateTimeFormatter.ISO_DATE)
        )
    }
}

enum class DateRange {
    WEEK, MONTH, YEAR
} 