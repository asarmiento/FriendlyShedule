class DateRangeDialog(
    private val currentRange: Pair<LocalDate, LocalDate>?,
    private val onRangeSelected: (Pair<LocalDate, LocalDate>) -> Unit
) : DialogFragment() {

    private var _binding: DialogDateRangeBinding? = null
    private val binding get() = _binding!!
    
    private var startDate: LocalDate = LocalDate.now().minusWeeks(1)
    private var endDate: LocalDate = LocalDate.now()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDateRangeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        currentRange?.let {
            startDate = it.first
            endDate = it.second
        }

        updateDateTexts()

        binding.etStartDate.setOnClickListener { showDatePicker(true) }
        binding.etEndDate.setOnClickListener { showDatePicker(false) }

        binding.btnApply.setOnClickListener {
            if (endDate.isBefore(startDate)) {
                showError("La fecha final debe ser posterior a la fecha inicial")
                return@setOnClickListener
            }
            onRangeSelected(Pair(startDate, endDate))
            dismiss()
        }

        binding.btnReset.setOnClickListener {
            startDate = LocalDate.now().minusWeeks(1)
            endDate = LocalDate.now()
            updateDateTexts()
        }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val date = if (isStartDate) startDate else endDate
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val selectedDate = LocalDate.of(year, month + 1, day)
                if (isStartDate) {
                    startDate = selectedDate
                } else {
                    endDate = selectedDate
                }
                updateDateTexts()
            },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth
        ).show()
    }

    private fun updateDateTexts() {
        val formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale("es"))
        binding.etStartDate.setText(startDate.format(formatter))
        binding.etEndDate.setText(endDate.format(formatter))
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DateRangeDialog"
    }
} 