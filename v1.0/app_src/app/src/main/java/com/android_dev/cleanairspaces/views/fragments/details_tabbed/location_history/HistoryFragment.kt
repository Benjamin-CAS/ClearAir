package com.android_dev.cleanairspaces.views.fragments.details_tabbed.location_history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat.getFont
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.FragmentHistoryBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.utils.*
import com.bumptech.glide.Glide
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HistoryFragment : Fragment() {
    companion object {
        private val TAG = HistoryFragment::class.java.simpleName
        private const val MONTH_TAG = "m"
        private const val DAY_TAG = "d"
        private const val WEEK_TAG = "w"
    }

    @Inject
    lateinit var myLogger: MyLogger


    private var selectedTv: TextView? = null
    private lateinit var selectedParamType: ParamTypes
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LocationHistoryViewModel by viewModels()


    private val titleFont by lazy {
        getFont(requireContext(), R.font.noto_sans)
    }

    private val bodyFont by lazy {
        getFont(requireContext(), R.font.fira_sans)
    }

    private val daysChartTitle by lazy { getString(R.string.last_seven_two_hours) }
    private val weekChartTitle by lazy { getString(R.string.last_week_lbl) }
    private val monthChartTitle by lazy { getString(R.string.last_thirty_lbl) }


    private val goodColor by lazy { ContextCompat.getColor(requireContext(), R.color.aqi_good) }
    private val moderateColor by lazy {
        ContextCompat.getColor(
                requireContext(),
                R.color.aqi_moderate
        )
    }
    private val gUnhealthyColor by lazy {
        ContextCompat.getColor(
                requireContext(),
                R.color.aqi_g_unhealthy
        )
    }
    private val unhealthyColor by lazy {
        ContextCompat.getColor(
                requireContext(),
                R.color.aqi_unhealthy
        )
    }
    private val blackColor by lazy { ContextCompat.getColor(requireContext(), R.color.black) }
    private val checkIcon by lazy {
        ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_blue_check
        )
    }


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

        //initially
        selectedParamType = ParamTypes.OUT_PM
        setSelectedParamView(binding.outdoorsTv)
        styleBarChart(binding.daysChart, DAY_TAG)
        styleBarChart(binding.weekChart, WEEK_TAG)
        styleBarChart(binding.monthChart, MONTH_TAG)

        viewModel.observeWatchedLocation().observe(viewLifecycleOwner, {
            if (it != null) {
                viewModel.refreshHistoryIfNecessary(it)
                updateGeneralLocationInfo(it)
                val hasIndoorData = it.isIndoorLoc
                toggleIndoorParameters(hasIndoorData)
                refreshLocationHistory(it.actualDataTag)
            }
        })

        observeHistoryData()
    }

    private fun updateGeneralLocationInfo(locationDetailsInfo: WatchedLocationHighLights) {
        val logoURL = locationDetailsInfo.getFullLogoUrl()
        binding.apply {
            if (logoURL.isNotBlank()) {
                locationLogo.isVisible = true
                Glide.with(requireContext())
                        .load(logoURL)
                        .into(locationLogo)
            }
            locationNameTv.text = if(locationDetailsInfo.location_area.isNotBlank())
                locationDetailsInfo.location_area
            else locationDetailsInfo.name
            locationNameTv.isSelected = true
        }
    }

    private fun refreshLocationHistory(actualDataTag: String) {
        viewModel.observeHistories(actualDataTag).days.observe(
                viewLifecycleOwner,
                {
                    if (it != null)
                        viewModel.setDaysHistory(it)
                })
        viewModel.observeHistories(actualDataTag).week.observe(
                viewLifecycleOwner,
                {
                    if (it != null)
                        viewModel.setWeekHistory(it)
                })

        viewModel.observeHistories(actualDataTag).month.observe(
                viewLifecycleOwner,
                {
                    if (it != null)
                        viewModel.setMonthHistory(it)
                })

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*********** graph controls ***********/
    private fun setParametersClickListeners() {
        binding.apply {
            aqiTv.setOnClickListener {
                selectedParamType = ParamTypes.IN_PM
                setSelectedParamView(aqiTv)
                clearClickedValue("")
                refreshChartData(
                        refreshDaysHistory = true,
                        refreshWeeksHistory = true,
                        refreshMonthsHistory = true
                )
                myLogger.logThis(
                        tag = LogTags.USER_ACTION_CLICK_FEATURE,
                        from = TAG,
                        msg = "Indoor PM"
                )
            }
            tvocTv.setOnClickListener {
                selectedParamType = ParamTypes.TVOC
                setSelectedParamView(tvocTv)
                clearClickedValue("")
                refreshChartData(
                        refreshDaysHistory = true,
                        refreshWeeksHistory = true,
                        refreshMonthsHistory = true
                )
                myLogger.logThis(
                        tag = LogTags.USER_ACTION_CLICK_FEATURE,
                        from = TAG,
                        msg = "Indoor TVOC"
                )
            }
            tmpTv.setOnClickListener {
                selectedParamType = ParamTypes.TMP
                setSelectedParamView(tmpTv)
                clearClickedValue("")
                refreshChartData(
                        refreshDaysHistory = true,
                        refreshWeeksHistory = true,
                        refreshMonthsHistory = true
                )
                myLogger.logThis(
                        tag = LogTags.USER_ACTION_CLICK_FEATURE,
                        from = TAG,
                        msg = "Indoor Temperature"
                )
            }
            humidityTv.setOnClickListener {
                selectedParamType = ParamTypes.HUMIDITY
                setSelectedParamView(humidityTv)
                clearClickedValue("")
                refreshChartData(
                        refreshDaysHistory = true,
                        refreshWeeksHistory = true,
                        refreshMonthsHistory = true
                )
                myLogger.logThis(
                        tag = LogTags.USER_ACTION_CLICK_FEATURE,
                        from = TAG,
                        msg = "Indoor Humidity"
                )
            }
            co2Tv.setOnClickListener {
                selectedParamType = ParamTypes.CO2
                setSelectedParamView(co2Tv)
                clearClickedValue("")
                refreshChartData(
                        refreshDaysHistory = true,
                        refreshWeeksHistory = true,
                        refreshMonthsHistory = true
                )
                myLogger.logThis(
                        tag = LogTags.USER_ACTION_CLICK_FEATURE,
                        from = TAG,
                        msg = "Indoor Carbon Dioxide"
                )
            }
            outdoorsTv.setOnClickListener {
                selectedParamType = ParamTypes.OUT_PM
                setSelectedParamView(outdoorsTv)
                clearClickedValue("")
                refreshChartData(
                        refreshDaysHistory = true,
                        refreshWeeksHistory = true,
                        refreshMonthsHistory = true
                )
                myLogger.logThis(
                        tag = LogTags.USER_ACTION_CLICK_FEATURE,
                        from = TAG,
                        msg = "Outdoor PM"
                )
            }
        }
    }

    private fun clearPrevSelectedParamView() {
        selectedTv?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
    }

    private fun setSelectedParamView(selectedParamView: TextView) {
        clearPrevSelectedParamView()
        selectedParamView.setCompoundDrawablesWithIntrinsicBounds(null, null, checkIcon, null)
        this.selectedTv = selectedParamView
    }

    private fun toggleIndoorParameters(hasIndoorData: Boolean) {
        binding.apply {
            pmCard.isVisible = hasIndoorData
            co2Card.isVisible = hasIndoorData
            tmpCard.isVisible = hasIndoorData
            humidityCard.isVisible = hasIndoorData
            tvocCard.isVisible = hasIndoorData
        }
    }


    /***********  DATA *********/
    private var clickListenersSet = false
    private fun observeHistoryData() {
        viewModel.observeLocationDaysHistory().observe(
                viewLifecycleOwner, { list ->
            list?.let {
                viewModel.currentlyDisplayedDaysHistoryData = list
                if (!clickListenersSet) {
                    clickListenersSet = true
                    setParametersClickListeners()
                }
                refreshChartData(refreshDaysHistory = true)
            }
        }
        )

        viewModel.observeLocationWeekHistory().observe(
                viewLifecycleOwner, { list ->
            list?.let {
                viewModel.currentlyDisplayedWeekHistoryData = list
                refreshChartData(refreshWeeksHistory = true)
            }
        }
        )

        viewModel.observeLocationMonthHistory().observe(
                viewLifecycleOwner, { list ->
            list?.let {
                viewModel.currentlyDisplayedMonthHistoryData = list
                refreshChartData(refreshMonthsHistory = true)
            }
        }
        )
    }

    private fun refreshChartData(
            refreshDaysHistory: Boolean = false,
            refreshWeeksHistory: Boolean = false,
            refreshMonthsHistory: Boolean = false
    ) {

        if (refreshDaysHistory) {
            val dayData = getChartDataForParam(forDays = true)
            updateChart(
                    chart = binding.daysChart,
                    title = daysChartTitle,
                    chartData = dayData
            )
        }

        if (refreshWeeksHistory) {
            val weekData = getChartDataForParam(forWeek = true)
            updateChart(
                    chart = binding.weekChart,
                    title = weekChartTitle,
                    chartData = weekData
            )
        }


        if (refreshMonthsHistory) {
            val monthData = getChartDataForParam(forMonth = true)
            updateChart(
                    chart = binding.monthChart,
                    title = monthChartTitle,
                    chartData = monthData
            )
        }
    }

    private fun getChartDataForParam(
            forDays: Boolean = false,
            forWeek: Boolean = false,
            forMonth: Boolean = false
    ): List<Float> {

        val chartData = arrayListOf<Float>()
        val chartDates = arrayListOf<String>()
        when (selectedParamType) {
            ParamTypes.IN_PM -> {
                when {
                    forDays -> {
                        for (aData in viewModel.currentlyDisplayedDaysHistoryData)
                            aData.data.indoor_pm.let {
                                chartData.add(it)
                                chartDates.add(aData.data.dates)
                            }
                        viewModel.currentDatesForDaysChart = chartDates
                    }
                    forWeek -> {
                        for (aData in viewModel.currentlyDisplayedWeekHistoryData)
                            aData.data.indoor_pm.let {
                                chartData.add(it)
                                chartDates.add(aData.data.dates)
                            }
                        viewModel.currentDatesForWeekChart = chartDates
                    }
                    forMonth -> {
                        for (aData in viewModel.currentlyDisplayedMonthHistoryData)
                            aData.data.indoor_pm.let {
                                chartData.add(it)
                                chartDates.add(aData.data.dates)
                            }
                        viewModel.currentDatesForMonthChart = chartDates
                    }
                }
            }
            ParamTypes.TMP -> {
                when {
                    forDays -> {
                        for (aData in viewModel.currentlyDisplayedDaysHistoryData)
                            aData.data.temperature.let {
                                chartData.add(it)
                                chartDates.add(aData.data.dates)
                            }
                        viewModel.currentDatesForDaysChart = chartDates
                    }
                    forWeek -> {
                        for (aData in viewModel.currentlyDisplayedWeekHistoryData)
                            aData.data.temperature.let {
                                chartData.add(it)
                                chartDates.add(aData.data.dates)
                            }
                        viewModel.currentDatesForWeekChart = chartDates
                    }
                    forMonth -> {
                        for (aData in viewModel.currentlyDisplayedMonthHistoryData)
                            aData.data.temperature.let {
                                chartData.add(it)
                                chartDates.add(aData.data.dates)
                            }
                        viewModel.currentDatesForMonthChart = chartDates
                    }
                }
            }
            ParamTypes.TVOC -> {
                when {
                    forDays -> {
                        for (aData in viewModel.currentlyDisplayedDaysHistoryData)
                            aData.data.tvoc.let {
                                chartData.add(it)
                                chartDates.add(aData.data.dates)
                            }
                        viewModel.currentDatesForDaysChart = chartDates
                    }
                    forWeek -> {
                        for (aData in viewModel.currentlyDisplayedWeekHistoryData)
                            aData.data.tvoc.let {
                                chartData.add(it)
                                chartDates.add(aData.data.dates)
                            }
                        viewModel.currentDatesForWeekChart = chartDates
                    }
                    forMonth -> {
                        for (aData in viewModel.currentlyDisplayedMonthHistoryData)
                            aData.data.tvoc.let {
                                chartData.add(it)
                                chartDates.add(aData.data.dates)
                            }
                        viewModel.currentDatesForMonthChart = chartDates
                    }
                }
            }
            ParamTypes.HUMIDITY -> {
                when {
                    forDays -> {
                        for (aData in viewModel.currentlyDisplayedDaysHistoryData)
                            aData.data.humidity.let {
                                chartData.add(it)
                                chartDates.add(aData.data.dates)
                            }
                        viewModel.currentDatesForDaysChart = chartDates
                    }
                    forWeek -> {
                        for (aData in viewModel.currentlyDisplayedWeekHistoryData)
                            aData.data.humidity.let {
                                chartData.add(it)
                                chartDates.add(aData.data.dates)
                            }
                        viewModel.currentDatesForWeekChart = chartDates
                    }
                    forMonth -> {
                        for (aData in viewModel.currentlyDisplayedMonthHistoryData)
                            aData.data.humidity.let {
                                chartData.add(it)
                                chartDates.add(aData.data.dates)
                            }
                        viewModel.currentDatesForMonthChart = chartDates
                    }
                }
            }
            ParamTypes.CO2 -> {
                when {
                    forDays -> {
                        for (aData in viewModel.currentlyDisplayedDaysHistoryData)
                            aData.data.co2.let {
                                chartData.add(it)
                                chartDates.add(aData.data.dates)
                            }
                        viewModel.currentDatesForDaysChart = chartDates
                    }
                    forWeek -> {
                        for (aData in viewModel.currentlyDisplayedWeekHistoryData)
                            aData.data.co2.let {
                                chartData.add(it)
                                chartDates.add(aData.data.dates)
                            }
                        viewModel.currentDatesForWeekChart = chartDates
                    }
                    forMonth -> {
                        for (aData in viewModel.currentlyDisplayedMonthHistoryData)
                            aData.data.co2.let {
                                chartData.add(it)
                                chartDates.add(aData.data.dates)
                            }
                        viewModel.currentDatesForMonthChart = chartDates
                    }
                }
            }

            ParamTypes.OUT_PM -> {
                when {
                    forDays -> {
                        for (aData in viewModel.currentlyDisplayedDaysHistoryData)
                            aData.data.outdoor_pm.let {
                                chartData.add(it)
                                chartDates.add(aData.data.dates)
                            }
                        viewModel.currentDatesForDaysChart = chartDates
                    }
                    forWeek -> {
                        for (aData in viewModel.currentlyDisplayedWeekHistoryData)
                            aData.data.outdoor_pm.let {
                                chartData.add(it)
                                chartDates.add(aData.data.dates)
                            }
                        viewModel.currentDatesForWeekChart = chartDates
                    }
                    forMonth -> {
                        for (aData in viewModel.currentlyDisplayedMonthHistoryData)
                            aData.data.outdoor_pm.let {
                                chartData.add(it)
                                chartDates.add(aData.data.dates)
                            }
                        viewModel.currentDatesForMonthChart = chartDates
                    }
                }
            }
        }
        return chartData
    }

    /************** GRAPH ***********/
    private fun styleBarChart(barChart: BarChart, barChartTag: String) {
        barChart.apply {
            axisRight.isEnabled = false
            axisLeft.isEnabled = false
            tag = barChartTag


            val legendGood = LegendEntry()
            legendGood.label = getString(R.string.good_air_status_txt)
            legendGood.formColor = goodColor

            val legendModerate = LegendEntry()
            legendModerate.label = getString(R.string.moderate_air_status_txt)
            legendModerate.formColor = moderateColor

            val legendSBad = LegendEntry()
            legendSBad.label = getString(R.string.aqi_status_unhealthy_sensitive_groups_abbrev)
            legendSBad.formColor = gUnhealthyColor

            val legendBad = LegendEntry()
            legendBad.label = getString(R.string.danger_txt)
            legendBad.formColor = unhealthyColor

            legend.apply {
                typeface = bodyFont
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                yOffset = 0f
                setCustom(listOf(legendGood, legendModerate, legendSBad, legendBad))
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


            setTouchEnabled(true)
            setPinchZoom(false)
            description.isEnabled = false
            setNoDataText(getString(R.string.loading_graph_data))
            setNoDataTextTypeface(bodyFont)
            setNoDataTextColor(blackColor)
            animateX(1000, Easing.EaseInExpo)

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let { entry ->
                        displayClickedValue(entry, barChartTag)
                    }
                }

                override fun onNothingSelected() {
                    clearClickedValue(barChartTag)
                }
            })
        }
    }

    private fun displayClickedValue(entry: Entry, chartIdentifierTag: String) {
        try {
            val pos = entry.x.toInt()
            val value = entry.y
            val clickedVal: String
            val unitsTxt = when (selectedParamType) {
                ParamTypes.IN_PM,
                ParamTypes.OUT_PM -> getString(R.string.pm_units)
                ParamTypes.TMP -> getString(R.string.tmp_units)
                ParamTypes.TVOC -> getString(R.string.tvoc_units)
                ParamTypes.HUMIDITY -> getString(R.string.humid_units)
                ParamTypes.CO2 -> getString(R.string.co2_units)
            }
            when (chartIdentifierTag) {
                DAY_TAG -> {
                    clickedVal = "${viewModel.currentDatesForDaysChart[pos]} $value $unitsTxt"
                    binding.daysChartValue.text = clickedVal
                }
                WEEK_TAG -> {
                    clickedVal = "${viewModel.currentDatesForWeekChart[pos]} $value $unitsTxt"
                    binding.weekChartValue.text = clickedVal

                }
                MONTH_TAG -> {
                    clickedVal = "${viewModel.currentDatesForMonthChart[pos]} $value $unitsTxt"
                    binding.monthChartValue.text = clickedVal

                }
            }
        } catch (exc: Exception) {
            myLogger.logThis(tag = LogTags.EXCEPTION, from = "$TAG displayClickedValue()", msg = exc.message, exc = exc)

        }
    }

    private fun clearClickedValue(chartIdentifierTag: String) {
        try {
            when (chartIdentifierTag) {
                DAY_TAG -> {
                    binding.daysChartValue.text = ""
                }
                WEEK_TAG -> {
                    binding.weekChartValue.text = ""

                }
                MONTH_TAG -> {
                    binding.monthChartValue.text = ""
                }
                else -> {
                    //clear all
                    binding.daysChartValue.text = ""
                    binding.weekChartValue.text = ""
                    binding.monthChartValue.text = ""
                }
            }
        } catch (exc: Exception) {
            myLogger.logThis(tag = LogTags.EXCEPTION, from = "$TAG clearClickedValue()", msg = exc.message, exc = exc)

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

    private fun getStatusColorForValue(value: Float, paramType: ParamTypes): Int {

        val aqiIndex = viewModel.aqiIndex ?: DEFAULT_AQI_INDEX_PM25
        return when (paramType) {
            ParamTypes.IN_PM,
            ParamTypes.OUT_PM -> getAQIStatusFromPM25(
                    aqiIndex = aqiIndex,
                    pm25 = value.toDouble()
            ).aqi_color_res
            ParamTypes.TMP -> getColorResFromTmp(value.toDouble())
            ParamTypes.TVOC -> getColorResFromVoc(value.toDouble())
            ParamTypes.HUMIDITY -> getColorResFromHumid(value.toDouble())
            ParamTypes.CO2 -> getColorResFromCO2(value.toDouble())
        }

    }

    private fun updateChart(
            chart: BarChart,
            title: String,
            chartData: List<Float>
    ) {
        try {
            val entries = ArrayList<BarEntry>()
            val valColorMap = ArrayList<Int>()
            for ((index, aData) in chartData.withIndex()) {
                val entry = BarEntry(index.toFloat(), aData)
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
        } catch (exc: Exception) {
            myLogger.logThis(tag = LogTags.EXCEPTION, from = "$TAG updateChart()", msg = exc.message, exc = exc)

        }
    }
}

internal class MyBarDataSet(
        yVals: List<BarEntry?>?,
        label: String?
) : BarDataSet(yVals, label) {
    override fun getEntryIndex(e: BarEntry?): Int {
        return super.getEntryIndex(e)
    }

    override fun getColor(index: Int): Int {
        return mColors[index]
    }
}

internal enum class ParamTypes {
    IN_PM,
    TMP,
    TVOC,
    HUMIDITY,
    CO2,
    OUT_PM
}