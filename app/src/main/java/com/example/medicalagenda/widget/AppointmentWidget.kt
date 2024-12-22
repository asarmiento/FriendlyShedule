@AndroidEntryPoint
class AppointmentWidget : AppWidgetProvider() {

    @Inject
    lateinit var repository: AppointmentRepository

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val appointments = repository.getAppointments(getCurrentDate())
                .filter { !it.attendance }
                .take(3)

            appWidgetIds.forEach { appWidgetId ->
                updateAppWidget(context, appWidgetManager, appWidgetId, appointments)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        appointments: List<Appointment>
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_appointments)

        views.removeAllViews(R.id.llAppointments)
        appointments.forEach { appointment ->
            val appointmentView = RemoteViews(context.packageName, R.layout.item_widget_appointment)
            appointmentView.setTextViewText(
                R.id.tvTime,
                appointment.init_time.substring(0, 5)
            )
            appointmentView.setTextViewText(
                R.id.tvPatientName,
                appointment.customer?.name ?: "Sin nombre"
            )
            views.addView(R.id.llAppointments, appointmentView)
        }

        if (appointments.isEmpty()) {
            views.setViewVisibility(R.id.tvNoAppointments, View.VISIBLE)
            views.setViewVisibility(R.id.llAppointments, View.GONE)
        } else {
            views.setViewVisibility(R.id.tvNoAppointments, View.GONE)
            views.setViewVisibility(R.id.llAppointments, View.VISIBLE)
        }

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widgetContainer, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
} 