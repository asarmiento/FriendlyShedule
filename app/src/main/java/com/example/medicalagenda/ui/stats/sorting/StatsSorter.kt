@Singleton
class StatsSorter @Inject constructor() {
    fun sortStats(stats: AppointmentStats, option: StatsSortOption): AppointmentStats {
        return when (option) {
            StatsSortOption.DATE_ASC -> stats.copy(
                appointmentsByDay = stats.appointmentsByDay.toSortedMap()
            )
            StatsSortOption.DATE_DESC -> stats.copy(
                appointmentsByDay = stats.appointmentsByDay.toSortedMap(reverseOrder())
            )
            StatsSortOption.ATTENDANCE_RATE_ASC -> stats.copy(
                appointmentsByDay = stats.appointmentsByDay.entries
                    .sortedBy { calculateAttendanceRate(it.key, stats) }
                    .associate { it.key to it.value }
            )
            StatsSortOption.ATTENDANCE_RATE_DESC -> stats.copy(
                appointmentsByDay = stats.appointmentsByDay.entries
                    .sortedByDescending { calculateAttendanceRate(it.key, stats) }
                    .associate { it.key to it.value }
            )
            StatsSortOption.COUNT_ASC -> stats.copy(
                appointmentsByDay = stats.appointmentsByDay.entries
                    .sortedBy { it.value }
                    .associate { it.key to it.value }
            )
            StatsSortOption.COUNT_DESC -> stats.copy(
                appointmentsByDay = stats.appointmentsByDay.entries
                    .sortedByDescending { it.value }
                    .associate { it.key to it.value }
            )
        }
    }

    private fun calculateAttendanceRate(date: String, stats: AppointmentStats): Float {
        val total = stats.appointmentsByDay[date] ?: return 0f
        val attended = stats.attendedAppointments
        return if (total > 0) attended.toFloat() / total else 0f
    }
} 