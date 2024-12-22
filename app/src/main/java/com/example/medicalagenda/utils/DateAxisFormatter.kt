class DateAxisFormatter(private val dates: List<String>) : ValueFormatter() {
    private val dateFormat = SimpleDateFormat("dd MMM", Locale("es"))
    private val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return try {
            val index = value.toInt()
            if (index >= 0 && index < dates.size) {
                val date = parser.parse(dates[index])
                date?.let { dateFormat.format(it) } ?: ""
            } else ""
        } catch (e: Exception) {
            ""
        }
    }
} 