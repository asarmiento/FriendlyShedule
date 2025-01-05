class ActiveFiltersView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    private val chipGroup: ChipGroup
    private var onFilterRemoved: ((String) -> Unit)? = null

    init {
        chipGroup = ChipGroup(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
            isSingleLine = true
        }
        addView(chipGroup)
    }

    fun setFilters(filters: List<FilterItem>) {
        chipGroup.removeAllViews()
        filters.forEach { filter ->
            addFilterChip(filter)
        }
    }

    private fun addFilterChip(filter: FilterItem) {
        val chip = Chip(context).apply {
            text = filter.displayText
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                onFilterRemoved?.invoke(filter.id)
                chipGroup.removeView(this)
            }
        }
        chipGroup.addView(chip)
    }

    fun setOnFilterRemovedListener(listener: (String) -> Unit) {
        onFilterRemoved = listener
    }
}

data class FilterItem(
    val id: String,
    val displayText: String
) 