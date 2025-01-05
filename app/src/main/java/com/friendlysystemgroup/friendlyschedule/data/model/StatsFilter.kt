data class StatsFilter(
    val dateRange: DateRange = DateRange.WEEK,
    val showAttended: Boolean = true,
    val showPending: Boolean = true,
    val showAbsent: Boolean = true,
    val minTime: String? = null,
    val maxTime: String? = null,
    val customDateRange: Pair<LocalDate, LocalDate>? = null
) 