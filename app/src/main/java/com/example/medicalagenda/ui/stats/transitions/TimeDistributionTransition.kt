class TimeDistributionTransition : TransitionInflater() {
    override fun createTransition(): Transition {
        return TransitionSet().apply {
            ordering = TransitionSet.ORDERING_TOGETHER
            
            addTransition(Fade().apply {
                duration = 200
                addTarget(R.id.tvTitle)
                addTarget(R.id.tvAppointmentCount)
                addTarget(R.id.tvTimeRange)
                addTarget(R.id.tvAttendanceRate)
            })
            
            addTransition(Slide(Gravity.BOTTOM).apply {
                duration = 300
                addTarget(R.id.btnClose)
            })
            
            addTransition(ChangeBounds().apply {
                duration = 300
            })
        }
    }
} 