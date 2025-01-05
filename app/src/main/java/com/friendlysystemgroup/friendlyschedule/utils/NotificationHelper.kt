class NotificationHelper @Inject constructor(
    private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de citas",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Canal para recordatorios de citas médicas"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleAppointmentReminder(appointment: Appointment) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra(EXTRA_APPOINTMENT_ID, appointment.id)
            putExtra(EXTRA_PATIENT_NAME, appointment.customer?.name)
            putExtra(EXTRA_APPOINTMENT_TIME, appointment.init_time)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointment.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            time = parseDateTime(appointment.init_date, appointment.init_time)
            add(Calendar.MINUTE, -30) // Notificar 30 minutos antes
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun showAppointmentNotification(appointmentId: Int, patientName: String?, time: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Recordatorio de cita")
            .setContentText("Cita con $patientName a las $time")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(appointmentId, notification)
    }

    private fun parseDateTime(date: String, time: String): Date {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .parse("$date $time") ?: Date()
    }

    companion object {
        const val CHANNEL_ID = "appointment_reminders"
        const val EXTRA_APPOINTMENT_ID = "appointment_id"
        const val EXTRA_PATIENT_NAME = "patient_name"
        const val EXTRA_APPOINTMENT_TIME = "appointment_time"
    }
} 