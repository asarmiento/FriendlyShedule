class TimeDistributionAnimator(private val view: TimeDistributionView) {
    private var animator: ValueAnimator? = null
    private var currentProgress = 0f
    private var targetDistribution: TimeDistribution? = null
    private var currentDistribution: TimeDistribution = TimeDistribution(0, 0, 0)

    fun animateToDistribution(target: TimeDistribution) {
        animator?.cancel()
        targetDistribution = target
        
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000
            interpolator = FastOutSlowInInterpolator()
            
            addUpdateListener { animation ->
                currentProgress = animation.animatedValue as Float
                updateCurrentDistribution()
                view.setData(currentDistribution)
            }
            
            start()
        }
    }

    private fun updateCurrentDistribution() {
        targetDistribution?.let { target ->
            currentDistribution = TimeDistribution(
                morning = interpolateValue(0, target.morning),
                afternoon = interpolateValue(0, target.afternoon),
                evening = interpolateValue(0, target.evening)
            )
        }
    }

    private fun interpolateValue(start: Int, end: Int): Int {
        return (start + (end - start) * currentProgress).roundToInt()
    }

    fun cancel() {
        animator?.cancel()
    }
} 