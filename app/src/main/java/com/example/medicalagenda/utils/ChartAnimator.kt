class ChartAnimator {
    companion object {
        fun animatePieChart(pieChart: PieChart) {
            pieChart.animateY(1400, Easing.EaseInOutQuad)
        }

        fun animateLineChart(lineChart: LineChart) {
            lineChart.animateX(1400, Easing.EaseInOutQuad)
        }

        fun animateBarChart(barChart: BarChart) {
            barChart.animateY(1400, Easing.EaseInOutQuad)
        }
    }
} 