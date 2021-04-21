package com.android_dev.cleanairspaces.ui.details.fragments.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.FragmentHistoryBinding
import com.android_dev.cleanairspaces.ui.details.LocationDetailsViewModel
import com.android_dev.cleanairspaces.utils.*
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import kotlin.math.truncate

@AndroidEntryPoint
class HistoryFragment : Fragment() {
    companion object {
        private val TAG = HistoryFragment::class.java.simpleName
    }

    private lateinit var selectedParamType: ParamTypes
    private lateinit var selectedParamView: TextView
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LocationDetailsViewModel by activityViewModels()


    private val titleFont by lazy {
        getFont(requireContext(), R.font.noto_sans)
    }

    private val bodyFont by lazy {
        getFont(requireContext(), R.font.fira_sans)
    }

    private val daysChartTitle by lazy { getString(R.string.last_seven_two_hours) }
    private val weekChartTitle by lazy { getString(R.string.last_week_lbl) }
    private val monthChartTitle by lazy { getString(R.string.last_thirty_lbl) }


    private val greenColor by lazy { ContextCompat.getColor(requireContext(), R.color.green) }
    private val yellowColor by lazy { ContextCompat.getColor(requireContext(), R.color.yellow) }
    private val redColor by lazy { ContextCompat.getColor(requireContext(), R.color.red) }
    private val blackColor by lazy { ContextCompat.getColor(requireContext(), R.color.black) }
    private val checkIcon by lazy { ContextCompat.getDrawable(requireContext(), R.drawable.ic_blue_check) }


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

        selectedParamType = ParamTypes.OUT_PM
        setSelectedParamView(binding.outdoorsTv)
        setParametersClickListeners()
        styleBarChart(binding.daysChart)
        styleBarChart(binding.weekChart)
        styleBarChart(binding.monthChart)

        viewModel.observeAQIIndex().observe(viewLifecycleOwner, Observer {
            viewModel.currentlyUsedAqi = it
            refreshWatchedLocationDetails()
        })

        viewModel.observeWatchedLocation().observe(viewLifecycleOwner, Observer {
            refreshWatchedLocationDetails()
        })
    }

    private fun refreshWatchedLocationDetails() {
        binding.progressCircular.isVisible = true
        val location = viewModel.currentlyDisplayedLocationHighLights
        val hasIndoorData =  location.isIndoorLoc
        toggleIndoorParameters(hasIndoorData)
        refreshAllHistoryWithNewParams()
        binding.progressCircular.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        selectedParamView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
    }

    private fun setSelectedParamView(selectedTv: TextView) {
        clearSelectedParamView()
        selectedTv.setCompoundDrawablesWithIntrinsicBounds(null, null, checkIcon, null)
        selectedParamView = selectedTv
    }

    private fun toggleIndoorParameters(hasIndoorData : Boolean){
        binding.apply {
            pmCard.isVisible = hasIndoorData
            co2Card.isVisible = hasIndoorData
            tmpCard.isVisible = hasIndoorData
            humidityCard.isVisible = hasIndoorData
            tvocCard.isVisible = hasIndoorData
        }
    }


    /***********  DATA *********/
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

    /************** GRAPH ***********/
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
                typeface = bodyFont
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                yOffset = 0f
                setCustom(listOf(legendGood, legendModerate, legendBad))
            }

            xAxis.apply {
                labelRotationAngle = 0f
                setDrawGridLines(false)
                position = XAxis.XAxisPosition.BOTTOM
                typeface = titleFont
                textSize = 12f
                axisMinimum = 1f
                xAxis.setDrawLabels(false)

            }

            setNoDataTextTypeface(titleFont)


            setTouchEnabled(false)
            setPinchZoom(false)
            description.isEnabled = false
            setNoDataText(getString(R.string.loading_graph_data))
            setNoDataTextTypeface(bodyFont)
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
            valueTypeface = titleFont

        }
    }

    private fun getStatusColorForValue(value : Double,  paramType : ParamTypes) : Int {
        val aqiIndex = viewModel.currentlyUsedAqi?: DEFAULT_AQI_INDEX_PM25
        return when(paramType){
            ParamTypes.IN_PM,
            ParamTypes.OUT_PM ->  getAQIStatusFromPM25(aqiIndex = aqiIndex, pm25 = value).aqi_color_res
            ParamTypes.TMP -> getColorResFromTmp(value)
            ParamTypes.TVOC -> getColorResFromVoc(value)
            ParamTypes.HUMIDITY -> getColorResFromHumid(value)
            ParamTypes.CO2 -> getColorResFromCO2(value)
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

}

internal class MyBarDataSet(yVals: List<BarEntry?>?,
                            label: String?) : BarDataSet(yVals, label) {
    override fun getEntryIndex(e: BarEntry?): Int {
        return super.getEntryIndex(e)
    }

    override fun getColor(index: Int): Int {
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