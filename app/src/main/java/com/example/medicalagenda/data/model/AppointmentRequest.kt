data class AppointmentRequest(
    val customer_id: Int,
    val init_date: String,
    val init_time: String,
    val description: String?,
    val title: String?
) 