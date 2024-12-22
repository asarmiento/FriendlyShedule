class TimeDistributionGestureDetector(
    private val view: TimeDistributionView,
    private val onBarSelected: (TimeOfDay) -> Unit
) : GestureDetector.SimpleOnGestureListener() {

    private val gestureDetector = GestureDetector(view.context, this)

    fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        val timeOfDay = view.getTimeOfDayAt(e.x, e.y)
        timeOfDay?.let { onBarSelected(it) }
        return true
    }
}

enum class TimeOfDay {
    MORNING, AFTERNOON, EVENING
} 