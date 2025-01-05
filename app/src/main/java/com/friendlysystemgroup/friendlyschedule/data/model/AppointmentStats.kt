data class AppointmentStats(
    val totalAppointments: Int,
    val attendedAppointments: Int,
    val pendingAppointments: Int,
    val absentAppointments: Int,
    val appointmentsByDay: Map<String, Int>,
    val appointmentsByHour: Map<String, Int>
) {
    val attendanceRate: Float
        get() = if (totalAppointments > 0) {
            attendedAppointments.toFloat() / totalAppointments
        } else 0f

    val absentRate: Float
        get() = if (totalAppointments > 0) {
            absentAppointments.toFloat() / totalAppointments
        } else 0f
} 