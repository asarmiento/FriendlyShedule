class FilterDialog(
    private val currentFilters: FilterOptions,
    private val onFiltersApplied: (FilterOptions) -> Unit
) : DialogFragment() {

    private var _binding: DialogFilterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        with(binding) {
            switchAttended.isChecked = currentFilters.showAttended
            switchPending.isChecked = currentFilters.showPending

            btnApply.setOnClickListener {
                val filters = FilterOptions(
                    showAttended = switchAttended.isChecked,
                    showPending = switchPending.isChecked
                )
                onFiltersApplied(filters)
                dismiss()
            }

            btnReset.setOnClickListener {
                onFiltersApplied(FilterOptions())
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "FilterDialog"
    }
}

data class FilterOptions(
    val showAttended: Boolean = true,
    val showPending: Boolean = true
) 