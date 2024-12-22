class HourlyStatsAdapter(
    private val stats: List<Map.Entry<String, Int>>
) : RecyclerView.Adapter<HourlyStatsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHourlyStatsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(stats[position])
    }

    override fun getItemCount() = stats.size

    class ViewHolder(
        private val binding: ItemHourlyStatsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(stat: Map.Entry<String, Int>) {
            binding.tvHour.text = stat.key
            binding.tvCount.text = stat.value.toString()
            binding.progressBar.progress = stat.value
        }
    }
} 