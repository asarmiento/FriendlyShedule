data class Appointment(
    val id: Int,
    val customer_id: Int,
    val init_date: String,
    val init_time: String,
    val end_date: String?,
    val end_time: String?,
    val title: String?,
    val description: String?,
    val attendance: Boolean,
    val absent: Boolean,
    val customer: Customer?
) 