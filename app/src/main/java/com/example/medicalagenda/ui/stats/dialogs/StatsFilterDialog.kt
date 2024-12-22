class StatsFilterDialog : DialogFragment() {
    private var _binding: DialogStatsFilterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.Theme.MedicalAgenda.Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogStatsFilterBinding.inflate(inflater, container, false)
        return binding.root.apply {
            transitionName = "filter_dialog"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTransitions()
        setupUI()
        observeViewModel()
    }

    private fun setupTransitions() {
        sharedElementEnterTransition = FilterDialogTransition().createTransition()
        enterTransition = Fade()
        returnTransition = Slide(Gravity.BOTTOM)
    }

    private fun setupUI() {
        with(binding) {
            // Configurar chips para tipos de cita
            AppointmentType.values().forEach { type ->
                val chip = createFilterChip(type.name)
                chipGroupTypes.addView(chip)
            }

            // Configurar chips para estados
            AppointmentStatus.values().forEach { status ->
                val chip = createFilterChip(status.name)
                chipGroupStatuses.addView(chip)
            }

            // Configurar slider de tasa de asistencia
            rangeSliderAttendance.addOnChangeListener { slider, _, _ ->
                val values = slider.values
                tvAttendanceRange.text = getString(
                    R.string.attendance_rate_range,
                    values[0].roundToInt(),
                    values[1].roundToInt()
                )
            }

            // Configurar botones
            btnApply.setOnClickListener {
                applyFilters()
                dismiss()
            }

            btnReset.setOnClickListener {
                viewModel.resetFilters()
                dismiss()
            }
        }
    }

    private fun createFilterChip(text: String): Chip {
        return Chip(requireContext()).apply {
            this.text = text
            isCheckable = true
            isChecked = true
            setTextAppearanceResource(R.style.TextAppearance.MaterialComponents.Body2)
            setChipBackgroundColorResource(R.color.chip_background_color)
            setTextColor(ContextCompat.getColorStateList(context, R.color.chip_text_color))
            setChipStrokeColorResource(R.color.chip_stroke_color)
            chipStrokeWidth = resources.getDimension(R.dimen.chip_stroke_width)
            
            // Animación al hacer clic
            setOnCheckedChangeListener { _, isChecked ->
                animate()
                    .scaleX(if (isChecked) 1.1f else 1f)
                    .scaleY(if (isChecked) 1.1f else 1f)
                    .setDuration(100)
                    .setInterpolator(FastOutSlowInInterpolator())
                    .start()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filter.collect { filter ->
                updateUI(filter)
            }
        }
    }

    private fun updateUI(filter: StatsFilterState) {
        with(binding) {
            // Actualizar chips
            chipGroupTypes.children.forEachIndexed { index, view ->
                (view as Chip).isChecked = filter.showTypes.contains(AppointmentType.values()[index])
            }

            chipGroupStatuses.children.forEachIndexed { index, view ->
                (view as Chip).isChecked = filter.showStatuses.contains(AppointmentStatus.values()[index])
            }

            // Actualizar slider
            rangeSliderAttendance.values = listOf(
                filter.minAttendanceRate?.times(100) ?: 0f,
                filter.maxAttendanceRate?.times(100) ?: 100f
            )
        }
    }

    private fun applyFilters() {
        val selectedTypes = binding.chipGroupTypes.children
            .filterIsInstance<Chip>()
            .filter { it.isChecked }
            .mapIndexed { index, _ -> AppointmentType.values()[index] }
            .toSet()

        val selectedStatuses = binding.chipGroupStatuses.children
            .filterIsInstance<Chip>()
            .filter { it.isChecked }
            .mapIndexed { index, _ -> AppointmentStatus.values()[index] }
            .toSet()

        val attendanceRange = binding.rangeSliderAttendance.values
        
        viewModel.updateFilter { current ->
            current.copy(
                showTypes = selectedTypes,
                showStatuses = selectedStatuses,
                minAttendanceRate = attendanceRange[0] / 100f,
                maxAttendanceRate = attendanceRange[1] / 100f
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "StatsFilterDialog"
    }
} 