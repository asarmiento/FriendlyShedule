class TrendAdapter(
    private val trends: List<Trend>
) : RecyclerView.Adapter<TrendAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTrendBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(trends[position])
    }

    override fun getItemCount() = trends.size

    class ViewHolder(
        private val binding: ItemTrendBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(trend: Trend) {
            binding.tvTrendTitle.text = trend.title
            binding.tvTrendDescription.text = trend.description
            
            binding.ivTrendIcon.setImageResource(
                when (trend.type) {
                    TrendType.POSITIVE -> R.drawable.ic_trend_up
                    TrendType.NEGATIVE -> R.drawable.ic_trend_down
                    TrendType.NEUTRAL -> R.drawable.ic_trend_neutral
                }
            )

            itemView.setOnClickListener {
                // Animar el card al hacer clic
                itemView.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction {
                        itemView.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                    }
                    .start()
            }
        }
    }
} 