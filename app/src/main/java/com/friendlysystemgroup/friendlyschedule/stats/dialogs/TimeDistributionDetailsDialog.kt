class TimeDistributionDetailsDialog(
    private val timeOfDay: TimeOfDay,
    private val stats: TimeDistributionDetails
) : DialogFragment() {

    private var _binding: DialogTimeDistributionDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.Theme_MedicalAgenda_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTimeDistributionDetailsBinding.inflate(inflater, container, false)
        return binding.root.apply {
            transitionName = "time_distribution_dialog"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTransitions()
        setupUI()
    }

    private fun setupTransitions() {
        sharedElementEnterTransition = TimeDistributionTransition().createTransition()
        enterTransition = Fade()
        returnTransition = Slide(Gravity.BOTTOM)
    }

    private fun setupUI() {
        with(binding) {
            tvTitle.text = when (timeOfDay) {
                TimeOfDay.MORNING -> "Citas en la mañana"
                TimeOfDay.AFTERNOON -> "Citas en la tarde"
                TimeOfDay.EVENING -> "Citas en la noche"
            }

            tvAppointmentCount.text = "${stats.appointmentCount} citas"
            tvTimeRange.text = stats.timeRange
            tvAttendanceRate.text = "Tasa de asistencia: ${(stats.attendanceRate * 100).roundToInt()}%"
            tvAttendanceRate.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (stats.attendanceRate >= 0.7f) R.color.success else R.color.error
                )
            )

            btnClose.setOnClickListener {
                dismissWithAnimation()
            }
        }
    }

    private fun dismissWithAnimation() {
        binding.root.animate()
            .alpha(0f)
            .translationY(100f)
            .setDuration(200)
            .withEndAction { dismiss() }
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "TimeDistributionDetailsDialog"
    }
}

data class TimeDistributionDetails(
    val appointmentCount: Int,
    val timeRange: String,
    val attendanceRate: Float
) 