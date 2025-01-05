@AndroidEntryPoint
class StatsWidget : AppWidgetProvider() {

    @Inject
    lateinit var statsDao: StatsDao

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val today = LocalDate.now()
            val startDate = today.minusWeeks(1)
            
            val stats = statsDao.getAppointmentStats(
                startDate.format(DateTimeFormatter.ISO_DATE),
                today.format(DateTimeFormatter.ISO_DATE)
            )

            appWidgetIds.forEach { appWidgetId ->
                updateWidget(context, appWidgetManager, appWidgetId, stats)
            }
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        stats: AppointmentStatsEntity
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_stats)
        
        val attendanceRate = if (stats.total > 0) {
            (stats.attended.toFloat() / stats.total * 100).roundToInt()
        } else 0

        views.setTextViewText(R.id.tvTotalAppointments, "Total: ${stats.total}")
        views.setTextViewText(R.id.tvAttendanceRate, "Asistencia: $attendanceRate%")
        
        val intent = Intent(context, StatsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widgetContainer, pendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
} 