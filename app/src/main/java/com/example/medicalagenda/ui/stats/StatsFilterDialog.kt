class StatsFilterDialog(
    private val currentFilter: StatsFilter,
    private val onFilterApplied: (StatsFilter) -> Unit
) : DialogFragment() {

    private var _binding: DialogStatsFilterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogStatsFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        with(binding) {
            switchAttended.isChecked = currentFilter.showAttended
            switchPending.isChecked = currentFilter.showPending
            switchAbsent.isChecked = currentFilter.showAbsent

            etMinTime.setText(currentFilter.minTime)
            etMaxTime.setText(currentFilter.maxTime)

            etMinTime.setOnClickListener { showTimePicker(true) }
            etMaxTime.setOnClickListener { showTimePicker(false) }

            btnApply.setOnClickListener {
                val newFilter = currentFilter.copy(
                    showAttended = switchAttended.isChecked,
                    showPending = switchPending.isChecked,
                    showAbsent = switchAbsent.isChecked,
                    minTime = etMinTime.text?.toString(),
                    maxTime = etMaxTime.text?.toString()
                )
                onFilterApplied(newFilter)
                dismiss()
            }

            btnReset.setOnClickListener {
                onFilterApplied(StatsFilter())
                dismiss()
            }
        }
    }

    private fun showTimePicker(isMinTime: Boolean) {
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                val time = String.format("%02d:%02d", hour, minute)
                if (isMinTime) {
                    binding.etMinTime.setText(time)
                } else {
                    binding.etMaxTime.setText(time)
                }
            },
            0, 0, true
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "StatsFilterDialog"
    }
} 