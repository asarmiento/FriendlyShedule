@AndroidEntryPoint
class ReminderBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        val appointmentId = intent.getIntExtra(NotificationHelper.EXTRA_APPOINTMENT_ID, 0)
        val patientName = intent.getStringExtra(NotificationHelper.EXTRA_PATIENT_NAME)
        val appointmentTime = intent.getStringExtra(NotificationHelper.EXTRA_APPOINTMENT_TIME)

        if (appointmentId != 0 && appointmentTime != null) {
            notificationHelper.showAppointmentNotification(
                appointmentId,
                patientName,
                appointmentTime.substring(0, 5)
            )
        }
    }
} 