package jAADD

import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.colors.XChartSeriesColors
import org.knowm.xchart.style.markers.SeriesMarkers

class AADDStream (name: String) {
    val name: String = name
    var timeUnit: String = ""
    var sampleUnit: String = ""

    val samples:  ArrayList<AADD> = ArrayList()
    val times:    ArrayList<Double> = ArrayList()

    fun add(sample: AADD, t: Double) {
        samples.add(sample)
        times.add(t)
    }

    fun display() {
        val chart = XYChartBuilder().width(800).height(600).title(name).xAxisTitle("t").yAxisTitle(name).build()
        // Customize Chart
        // chart.styler.defaultSeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Scatter
        // chart.styler.defaultSeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Line;
        // chart.styler.isChartTitleVisible = true
        chart.styler.isLegendVisible = true
        // chart.styler.setAxisTitlesVisible(true)
        chart.styler.xAxisDecimalPattern = "0.0"

        // Series
        val minD: MutableList<Double> = ArrayList()
        val maxD: MutableList<Double> = ArrayList()

        for (aadd in samples) {
            val r = aadd.getRange()
            maxD.add(r.max)
            minD.add(r.min)
        }
        // val series = chart.addSeries(name, xData, yData, errorBars)
        val maxG = chart.addSeries(name+".max", times, maxD)
        val minG = chart.addSeries(name+".min", times, minD)

        maxG.marker = SeriesMarkers.NONE
        maxG.lineColor = XChartSeriesColors.BLUE

        minG.marker = SeriesMarkers.NONE
        minG.lineColor = XChartSeriesColors.BLUE

        SwingWrapper(chart).displayChart()
    }
}