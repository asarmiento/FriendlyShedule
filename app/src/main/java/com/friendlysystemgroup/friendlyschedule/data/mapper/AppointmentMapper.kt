fun AppointmentEntity.toDomain(): Appointment {
    return Appointment(
        id = id,
        customer_id = customerId,
        init_date = initDate,
        init_time = initTime,
        end_date = null,
        end_time = null,
        title = null,
        description = description,
        attendance = attendance,
        absent = absent,
        customer = null
    )
}

fun Appointment.toEntity(): AppointmentEntity {
    return AppointmentEntity(
        id = id,
        customerId = customer_id,
        initDate = init_date,
        initTime = init_time,
        description = description,
        attendance = attendance,
        absent = absent
    )
} 