object StatsAnimator {
    fun animateProgress(progressBar: ProgressBar, targetProgress: Int) {
        val animator = ObjectAnimator.ofInt(progressBar, "progress", 0, targetProgress)
        animator.duration = 1000
        animator.interpolator = FastOutSlowInInterpolator()
        animator.start()
    }

    fun animateNumber(textView: TextView, targetNumber: Int) {
        val animator = ValueAnimator.ofInt(0, targetNumber)
        animator.duration = 1000
        animator.interpolator = FastOutSlowInInterpolator()
        animator.addUpdateListener { animation ->
            textView.text = animation.animatedValue.toString()
        }
        animator.start()
    }

    fun animatePercentage(textView: TextView, targetPercentage: Float) {
        val animator = ValueAnimator.ofFloat(0f, targetPercentage)
        animator.duration = 1000
        animator.interpolator = FastOutSlowInInterpolator()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            textView.text = "${(value * 100).roundToInt()}%"
        }
        animator.start()
    }

    fun animateRecyclerView(recyclerView: RecyclerView) {
        recyclerView.alpha = 0f
        recyclerView.translationY = 100f
        recyclerView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()
    }
} 