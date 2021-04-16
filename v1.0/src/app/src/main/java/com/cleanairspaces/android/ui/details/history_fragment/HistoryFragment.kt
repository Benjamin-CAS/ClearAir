package com.cleanairspaces.android.ui.details.history_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.FragmentHistoryBinding
import com.cleanairspaces.android.ui.details.LocationDetailsViewModel
import com.cleanairspaces.android.utils.*
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.truncate


@AndroidEntryPoint
class HistoryFragment : Fragment() {

    companion object {
        private val TAG = HistoryFragment::class.java.simpleName
    }

    private lateinit var selectedParamType: ParamTypes
    private var graphParamSelected: TextView? = null
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LocationDetailsViewModel by activityViewModels()

    private val openSansLight by lazy {
        ResourcesCompat.getFont(requireContext(), R.font.open_sans_condensed_light)
    }

    private val openSans by lazy {
        ResourcesCompat.getFont(requireContext(), R.font.open_sans)
    }


    private val greenColor by lazy { ContextCompat.getColor(requireContext(), R.color.green) }
    private val yellowColor by lazy { ContextCompat.getColor(requireContext(), R.color.yellow) }
    private val redColor by lazy { ContextCompat.getColor(requireContext(), R.color.red) }
    private val blackColor by lazy { ContextCompat.getColor(requireContext(), R.color.black) }
    private val checkIcon by lazy { ContextCompat.getDrawable(requireContext(), R.drawable.ic_blue_check) }

    private val daysChartTitle by lazy { getString(R.string.last_seven_two_hours) }
    private val weekChartTitle by lazy { getString(R.string.last_week_lbl) }
    private val monthChartTitle by lazy { getString(R.string.last_thirty_lbl) }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedParamType = ParamTypes.IN_PM //by default
        setParametersClickListeners()
        setSelectedParamView(binding.aqiTv)
        styleBarChart(binding.daysChart)
        styleBarChart(binding.weekChart)
        styleBarChart(binding.monthChart)

        viewModel.observeLocationDetails().observe(viewLifecycleOwner, Observer {
                val showIndoor = (it.indoorPmValue != UNSET_PARAM_VAL)
                toggleIndoorParameters(show = showIndoor)
                 try {

                     observeHistory()
                 }catch (e : java.lang.Exception) {
                     MyLogger.logThis(
                             TAG, "exc", e.message, e
                     )
                 }
        })

    }

    private fun observeHistory(){
           viewModel.observeLocationDaysHistory().observe(
                viewLifecycleOwner, { list ->
            list?.let {
                viewModel.currentlyDisplayedDaysHistoryData = list
                val chartData = getChartDataForParam(forDays = true)
                updateChart(
                        chart = binding.daysChart,
                        title = daysChartTitle,
                        chartData = chartData
                )
            }
        }
        )

        viewModel.observeLocationWeekHistory().observe(
                viewLifecycleOwner, { list ->
            list?.let {
                viewModel.currentlyDisplayedWeekHistoryData = list
                val chartData = getChartDataForParam(forWeek = true)
                updateChart(
                        chart = binding.weekChart,
                        title = weekChartTitle,
                        chartData = chartData
                )
            }
        }
        )

        viewModel.observeLocationMonthHistory().observe(
                viewLifecycleOwner, { list ->
            list?.let {
                viewModel.currentlyDisplayedMonthHistoryData = list
                val chartData = getChartDataForParam(forMonth = true)
                updateChart(
                        chart = binding.monthChart,
                        title = monthChartTitle,
                        chartData = chartData
                )
            }
        }
        )
    }

    private fun refreshAllHistoryWithNewParams(){

        //day
        val dayData = getChartDataForParam(forDays= true)
        updateChart(
                chart = binding.daysChart,
                title = daysChartTitle,
                chartData = dayData
        )

        //week
        val weekData = getChartDataForParam(forWeek = true)
        updateChart(
                chart = binding.weekChart,
                title = weekChartTitle,
                chartData = weekData
        )


        //month
        val monthData = getChartDataForParam(forMonth = true)
        updateChart(
                chart = binding.monthChart,
              title = monthChartTitle,
            chartData = monthData
        )

        MyLogger.logThis(
                TAG, "refreshing histories", "called"
        )
    }

    private fun getChartDataForParam(
            forDays : Boolean = false,
            forWeek : Boolean = false,
            forMonth  :Boolean = false): List<Double> {

        val data  = ArrayList<Double>()
        when(selectedParamType){


            ParamTypes.IN_PM -> {
                when{
                    forDays -> {
                        for (aData in viewModel.currentlyDisplayedDaysHistoryData)
                            aData.avg_reading?.let {
                                data.add(truncate(it))
                            }
                    }
                    forWeek -> {
                        for (aData in viewModel.currentlyDisplayedWeekHistoryData)
                            aData.avg_reading?.let {
                                data.add(truncate(it))
                            }
                    }
                    forMonth -> {
                        for (aData in viewModel.currentlyDisplayedMonthHistoryData)
                            aData.avg_reading?.let {
                                data.add(truncate(it))
                            }
                    }
                }
            }
            ParamTypes.TMP -> {
                when{
                    forDays -> {
                        for (aData in viewModel.currentlyDisplayedDaysHistoryData)
                            aData.avg_temperature?.let {
                                data.add(truncate(it))
                            }
                    }
                    forWeek -> {
                        for (aData in viewModel.currentlyDisplayedWeekHistoryData)
                            aData.avg_temperature?.let {
                                data.add(truncate(it))
                            }
                    }
                    forMonth -> {
                        for (aData in viewModel.currentlyDisplayedMonthHistoryData)
                            aData.avg_temperature?.let {
                                data.add(truncate(it))
                            }
                    }
                }
            }
            ParamTypes.TVOC -> {
                when{
                    forDays -> {
                        for (aData in viewModel.currentlyDisplayedDaysHistoryData)
                            aData.avg_tvoc?.let {
                                data.add(truncate(it.toDouble()))
                            }
                    }
                    forWeek -> {
                        for (aData in viewModel.currentlyDisplayedWeekHistoryData)
                            aData.avg_tvoc?.let {
                                data.add(truncate(it.toDouble()))
                            }
                    }
                    forMonth -> {
                        for (aData in viewModel.currentlyDisplayedMonthHistoryData)
                            aData.avg_tvoc?.let {
                                data.add(truncate(it.toDouble()))
                            }
                    }
                }
            }
            ParamTypes.HUMIDITY -> {
                when{
                    forDays -> {
                        for (aData in viewModel.currentlyDisplayedDaysHistoryData)
                            aData.avg_humidity?.let {
                                data.add(truncate(it))
                            }
                    }
                    forWeek -> {
                        for (aData in viewModel.currentlyDisplayedWeekHistoryData)
                            aData.avg_humidity?.let {
                                data.add(truncate(it))
                            }
                    }
                    forMonth -> {
                        for (aData in viewModel.currentlyDisplayedMonthHistoryData)
                            aData.avg_humidity?.let {
                                data.add(truncate(it))
                            }
                    }
                }
            }
            ParamTypes.CO2 -> {
                when{
                    forDays -> {
                        for (aData in viewModel.currentlyDisplayedDaysHistoryData)
                            aData.avg_co2?.let {
                                data.add(truncate(it))
                            }
                    }
                    forWeek -> {
                        for (aData in viewModel.currentlyDisplayedWeekHistoryData)
                            aData.avg_co2?.let {
                                data.add(truncate(it))
                            }
                    }
                    forMonth -> {
                        for (aData in viewModel.currentlyDisplayedMonthHistoryData)
                            aData.avg_co2?.let {
                                data.add(truncate(it))
                            }
                    }
                }
            }

            ParamTypes.OUT_PM -> {
                when{
                    forDays -> {
                        for (aData in viewModel.currentlyDisplayedDaysHistoryData)
                            aData.reading_comp?.let {
                                data.add(truncate(it))
                            }
                    }
                    forWeek -> {
                        for (aData in viewModel.currentlyDisplayedWeekHistoryData)
                            aData.reading_comp?.let {
                                data.add(truncate(it))
                            }
                    }
                    forMonth -> {
                        for (aData in viewModel.currentlyDisplayedMonthHistoryData)
                            aData.reading_comp?.let {
                                data.add(truncate(it))
                            }
                    }
                }
            }
        }
        return data
    }

    /*********** graph controls ***********/
    private fun setParametersClickListeners(){
        binding.apply {
            aqiTv.setOnClickListener {
                selectedParamType = ParamTypes.IN_PM
                setSelectedParamView(aqiTv)
                refreshAllHistoryWithNewParams()
            }
            tvocTv.setOnClickListener {
                selectedParamType = ParamTypes.TVOC
                setSelectedParamView(tvocTv)
                refreshAllHistoryWithNewParams()
            }
            tmpTv.setOnClickListener {
                selectedParamType = ParamTypes.TMP
                setSelectedParamView(tmpTv)
                refreshAllHistoryWithNewParams()
            }
            humidityTv.setOnClickListener {
                selectedParamType = ParamTypes.HUMIDITY
                setSelectedParamView(humidityTv)
                refreshAllHistoryWithNewParams()
            }
            co2Tv.setOnClickListener {
                selectedParamType = ParamTypes.CO2
                setSelectedParamView(co2Tv)
                refreshAllHistoryWithNewParams()
            }
            outdoorsTv.setOnClickListener {
                selectedParamType = ParamTypes.IN_PM
                setSelectedParamView(outdoorsTv)
                refreshAllHistoryWithNewParams()
            }
        }
    }
    private fun clearSelectedParamView(){
            graphParamSelected?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
    }

    private fun setSelectedParamView(selectedTv: TextView) {
        clearSelectedParamView()
        selectedTv.setCompoundDrawablesWithIntrinsicBounds(null, null, checkIcon, null)
        graphParamSelected = selectedTv
    }

    private fun toggleIndoorParameters(show : Boolean){
        binding.apply {
            pmCard.isVisible = show
            tmpCard.isVisible = show
            co2Card.isVisible = show
            humidityCard.isVisible = show
            tvocCard.isVisible = show
        }
    }



    /********** bar chart **********/
    private fun styleBarChart(barChart: BarChart) {
        barChart.apply {
            axisRight.isEnabled = false
            axisLeft.isEnabled = false


            val legendGood = LegendEntry()
            legendGood.label = getString(R.string.good_air_status_txt)
            legendGood.formColor = greenColor

            val legendModerate = LegendEntry()
            legendModerate.label = getString(R.string.moderate_air_status_txt)
            legendModerate.formColor = yellowColor

            val legendBad = LegendEntry()
            legendBad.label = getString(R.string.danger_txt)
            legendBad.formColor = redColor

            legend.apply {
                typeface = openSans
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                yOffset = 0f
                setCustom(listOf(legendGood, legendModerate, legendBad))
            }

            xAxis.apply {
                labelRotationAngle = 0f
                setDrawGridLines(false)
                position = XAxis.XAxisPosition.BOTTOM
                typeface = openSansLight
                textSize = 12f
                axisMinimum = 1f
                xAxis.setDrawLabels(false)

            }

            setNoDataTextTypeface(openSansLight)


            setTouchEnabled(false)
            setPinchZoom(false)
            description.isEnabled = false
            setNoDataText(getString(R.string.loading_graph_data))
            setNoDataTextTypeface(openSans)
            setNoDataTextColor(blackColor)
            animateX(1000, Easing.EaseInExpo)
        }
    }

    private fun styleDataSet(barDataSet: BarDataSet, valColorMap: ArrayList<Int>) {
        barDataSet.apply {
            //color of the bar
            colors = valColorMap
            //Setting the size of the legend box
            formSize = 10f
            //showing the value of the bar at the top, default true if not set
            setDrawValues(false)
            //setting the text size of the value of the bar
            valueTextSize = 12f
            valueTextColor = blackColor
            valueTypeface = openSansLight

        }
    }

    private fun getStatusColorForValue(value : Double,  paramType : ParamTypes) : Int {
        val aqiIndex = viewModel.getNonObservableDetails().aqiIndex
        return when(paramType){
            ParamTypes.IN_PM,
            ParamTypes.OUT_PM -> getStatusColorForPm(ctx = requireContext(), aqiIndex = aqiIndex, pmValue = value)
            ParamTypes.TMP -> AQI.getColorResFromTmp(value)
            ParamTypes.TVOC -> AQI.getColorResFromVoc(value)
            ParamTypes.HUMIDITY -> AQI.getColorResFromHumid(value)
            ParamTypes.CO2 -> AQI.getColorResFromCO2(value)
        }

    }

    private fun updateChart(chart: BarChart,
                            title : String,
                            chartData: List<Double>,
                            ) {
        try {
            val entries = ArrayList<BarEntry>()
            val valColorMap = ArrayList<Int>()
            for ((i, aData) in chartData.withIndex()) {
                val entry = BarEntry(i.toFloat(), aData.toFloat())
                entries.add(entry)
                val color = getStatusColorForValue(aData, selectedParamType)
                valColorMap.add(ContextCompat.getColor(requireContext(), color))
            }
            val daysHistoryDataSet = MyBarDataSet(entries, title)
            styleDataSet(daysHistoryDataSet, valColorMap)

            chart.apply {
                invalidate()
                data = BarData(daysHistoryDataSet)
                notifyDataSetChanged()
            }
            MyLogger.logThis(
                    TAG, "update chart $selectedParamType", "called"
            )
        } catch (e: Exception) {
            MyLogger.logThis(
                    TAG, "updateChart()",
                    "failed -- new data ${chartData.size} in size ${e.message}",
                    e
            )
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

internal class MyBarDataSet(yVals: List<BarEntry?>?,
                            label: String?) : BarDataSet(yVals, label) {
    override fun getEntryIndex(e: BarEntry?): Int {
        return super.getEntryIndex(e)
    }

    override fun getColor(index: Int): Int {
        MyLogger.logThis("coloring bar", "called", "$index")
        return mColors[index]
    }
}


internal enum class ParamTypes{
        IN_PM,
        TMP,
        TVOC,
        HUMIDITY,
        CO2,
        OUT_PM
}