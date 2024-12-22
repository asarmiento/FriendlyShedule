class FilterDialogTransition : TransitionInflater() {
    override fun createTransition(): Transition {
        return TransitionSet().apply {
            ordering = TransitionSet.ORDERING_TOGETHER
            
            addTransition(Slide(Gravity.TOP).apply {
                duration = 300
                addTarget(R.id.chipGroupTypes)
                addTarget(R.id.chipGroupStatuses)
            })
            
            addTransition(Fade().apply {
                duration = 200
                addTarget(R.id.tvAttendanceRange)
                addTarget(R.id.rangeSliderAttendance)
            })
            
            addTransition(Slide(Gravity.BOTTOM).apply {
                duration = 300
                addTarget(R.id.btnReset)
                addTarget(R.id.btnApply)
            })
        }
    }
} 