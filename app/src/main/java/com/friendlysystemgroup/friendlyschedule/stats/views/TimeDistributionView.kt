class TimeDistributionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var timeDistribution: TimeDistribution? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    private val morningColor = Color.parseColor("#FFC107") // Amarillo
    private val afternoonColor = Color.parseColor("#2196F3") // Azul
    private val eveningColor = Color.parseColor("#4CAF50") // Verde

    private val animator = TimeDistributionAnimator(this)

    private var selectedBar: TimeOfDay? = null
    private var onBarSelectedListener: ((TimeOfDay) -> Unit)? = null
    private var onBarClickListener: ((TimeOfDay, TimeDistributionDetails) -> Unit)? = null
    
    private val gestureDetector = TimeDistributionGestureDetector(this) { timeOfDay ->
        selectedBar = timeOfDay
        invalidate()
        
        timeDistribution?.let { distribution ->
            val details = when (timeOfDay) {
                TimeOfDay.MORNING -> TimeDistributionDetails(
                    appointmentCount = distribution.morning,
                    timeRange = "7:00 - 12:00",
                    attendanceRate = 0.85f // Este valor debería venir de los datos reales
                )
                TimeOfDay.AFTERNOON -> TimeDistributionDetails(
                    appointmentCount = distribution.afternoon,
                    timeRange = "12:00 - 17:00",
                    attendanceRate = 0.75f
                )
                TimeOfDay.EVENING -> TimeDistributionDetails(
                    appointmentCount = distribution.evening,
                    timeRange = "17:00 - 21:00",
                    attendanceRate = 0.65f
                )
            }
            onBarClickListener?.invoke(timeOfDay, details)
        }
    }

    init {
        textPaint.apply {
            textSize = context.resources.getDimension(R.dimen.text_size_small)
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
        }
    }

    fun setData(distribution: TimeDistribution, animate: Boolean = true) {
        if (animate) {
            animator.animateToDistribution(distribution)
        } else {
            timeDistribution = distribution
            invalidate()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        timeDistribution?.let { drawDistribution(canvas, it) }
    }

    private fun drawDistribution(canvas: Canvas, distribution: TimeDistribution) {
        val total = distribution.morning + distribution.afternoon + distribution.evening
        if (total == 0) return

        val width = width.toFloat()
        val height = height.toFloat()
        val barHeight = height * 0.6f
        val barTop = (height - barHeight) / 2
        val barBottom = barTop + barHeight

        // Dibujar barras
        var left = width * 0.1f
        val barWidth = width * 0.25f
        val spacing = width * 0.05f

        // Mañana
        paint.color = morningColor
        val morningHeight = (distribution.morning.toFloat() / total) * barHeight
        rect.set(left, barBottom - morningHeight, left + barWidth, barBottom)
        canvas.drawRoundRect(rect, 8f, 8f, paint)
        canvas.drawText("Mañana", left + barWidth/2, barBottom + 40f, textPaint)
        canvas.drawText("${distribution.morning}", left + barWidth/2, barBottom + 70f, textPaint)

        // Tarde
        left += barWidth + spacing
        paint.color = afternoonColor
        val afternoonHeight = (distribution.afternoon.toFloat() / total) * barHeight
        rect.set(left, barBottom - afternoonHeight, left + barWidth, barBottom)
        canvas.drawRoundRect(rect, 8f, 8f, paint)
        canvas.drawText("Tarde", left + barWidth/2, barBottom + 40f, textPaint)
        canvas.drawText("${distribution.afternoon}", left + barWidth/2, barBottom + 70f, textPaint)

        // Noche
        left += barWidth + spacing
        paint.color = eveningColor
        val eveningHeight = (distribution.evening.toFloat() / total) * barHeight
        rect.set(left, barBottom - eveningHeight, left + barWidth, barBottom)
        canvas.drawRoundRect(rect, 8f, 8f, paint)
        canvas.drawText("Noche", left + barWidth/2, barBottom + 40f, textPaint)
        canvas.drawText("${distribution.evening}", left + barWidth/2, barBottom + 70f, textPaint)

        // Agregar efecto de selección
        selectedBar?.let { selected ->
            paint.color = Color.parseColor("#80000000")
            when (selected) {
                TimeOfDay.MORNING -> rect.set(left, barBottom - morningHeight, left + barWidth, barBottom)
                TimeOfDay.AFTERNOON -> rect.set(left + barWidth + spacing, barBottom - afternoonHeight, left + 2 * barWidth + spacing, barBottom)
                TimeOfDay.EVENING -> rect.set(left + 2 * barWidth + 2 * spacing, barBottom - eveningHeight, left + 3 * barWidth + 2 * spacing, barBottom)
            }
            canvas.drawRoundRect(rect, 8f, 8f, paint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = context.resources.getDimensionPixelSize(R.dimen.time_distribution_width)
        val desiredHeight = context.resources.getDimensionPixelSize(R.dimen.time_distribution_height)

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    fun setOnBarSelectedListener(listener: (TimeOfDay) -> Unit) {
        onBarSelectedListener = listener
    }

    fun getTimeOfDayAt(x: Float, y: Float): TimeOfDay? {
        val width = width.toFloat()
        val barWidth = width * 0.25f
        val spacing = width * 0.05f
        val startX = width * 0.1f

        return when {
            x in startX..(startX + barWidth) -> TimeOfDay.MORNING
            x in (startX + barWidth + spacing)..(startX + 2 * barWidth + spacing) -> TimeOfDay.AFTERNOON
            x in (startX + 2 * barWidth + 2 * spacing)..(startX + 3 * barWidth + 2 * spacing) -> TimeOfDay.EVENING
            else -> null
        }
    }

    fun setOnBarClickListener(listener: (TimeOfDay, TimeDistributionDetails) -> Unit) {
        onBarClickListener = listener
    }
} 