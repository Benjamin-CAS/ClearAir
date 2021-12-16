package com.android_dev.cleanairspaces.views.fragments.monitor_details

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.FragmentMonitorHistoryBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.MonitorDetails
import com.android_dev.cleanairspaces.utils.*
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MonitorHistoryFragment : Fragment() {

    companion object {
        private val TAG = MonitorHistoryFragment::class.java.simpleName
        private const val MONTH_TAG = "m"
        private const val DAY_TAG = "d"
        private const val WEEK_TAG = "w"
    }

    @Inject
    lateinit var myLogger: MyLogger


    private var selectedTv: TextView? = null
    private lateinit var selectedParamType: ParamTypes
    private var _binding: FragmentMonitorHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MonitorHistoryViewModel by viewModels()


    private val titleFont by lazy {
        ResourcesCompat.getFont(requireContext(), R.font.noto_sans)
    }

    private val bodyFont by lazy {
        ResourcesCompat.getFont(requireContext(), R.font.fira_sans)
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
        _binding = FragmentMonitorHistoryBinding.inflate(inflater, container, false)

        return binding.root
    }

    val args: MonitorHistoryFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backMonitorBtn.setOnClickListener {
            findNavController().navigate(R.id.monitorsFragment)
        }
        //initially
        selectedParamType = ParamTypes.IN_OUT_PM
        toggleCombinedCharts(showCombined = true)
        setSelectedParamView(binding.aqiTv)
        styleCombinedChart(binding.daysCombinedChart, DAY_TAG)
        styleCombinedChart(binding.weekChartCombined, WEEK_TAG)
        styleCombinedChart(binding.monthChartCombined, MONTH_TAG)
        styleBarChart(binding.daysChart, DAY_TAG)
        styleBarChart(binding.weekChart, WEEK_TAG)
        styleBarChart(binding.monthChart, MONTH_TAG)

        val monitorDetails = args.monitorDetails.monitorDetails
        viewModel.aqiIndex = args.monitorDetails.aqiIndex
        viewModel.refreshHistoryIfNecessary(monitorDetails)
        updateGeneralLocationInfo(monitorDetails)
        viewModel.hasIndoorData =
            monitorDetails.indoor_pm_25 != null && monitorDetails.indoor_pm_25 > 0
        toggleIndoorParameters()
        refreshLocationHistory(monitorDetails.actualDataTag)
        observeHistoryData()
    }

    private fun updateGeneralLocationInfo(monitorDetails: MonitorDetails) {
        binding.apply {
            locationLogo.isVisible = false
            locationNameTv.text = monitorDetails.outdoor_name_en ?: monitorDetails.indoor_name_en
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
    private fun toggleCombinedCharts(showCombined: Boolean) {
        binding.apply {
            daysChart.isVisible = !showCombined
            daysCombinedChart.isVisible = showCombined
            weekChart.isVisible = !showCombined
            weekChartCombined.isVisible = showCombined
            monthChart.isVisible = !showCombined
            monthChartCombined.isVisible = showCombined
        }
    }

    private fun setParametersClickListeners() {
        binding.apply {
            aqiTv.setOnClickListener {
                selectedParamType = ParamTypes.IN_OUT_PM
                toggleCombinedCharts(showCombined = true)
                setSelectedParamView(aqiTv)
                clearClickedValue("")
                refreshChartData(
                    refreshDaysHistory = true,
                    refreshWeeksHistory = true,
                    refreshMonthsHistory = true
                )
                lifecycleScope.launch(Dispatchers.IO) {
                    myLogger.logThis(
                        tag = LogTags.USER_ACTION_CLICK_FEATURE,
                        from = TAG,
                        msg = "AIR QUALITY"
                    )
                }
            }
            tvocTv.setOnClickListener {
                selectedParamType = ParamTypes.TVOC
                toggleCombinedCharts(showCombined = false)
                setSelectedParamView(tvocTv)
                clearClickedValue("")
                refreshChartData(
                    refreshDaysHistory = true,
                    refreshWeeksHistory = true,
                    refreshMonthsHistory = true
                )
                lifecycleScope.launch(Dispatchers.IO) {
                    myLogger.logThis(
                        tag = LogTags.USER_ACTION_CLICK_FEATURE,
                        from = TAG,
                        msg = "Indoor TVOC"
                    )
                }
            }
            tmpTv.setOnClickListener {
                selectedParamType = ParamTypes.TMP
                toggleCombinedCharts(showCombined = false)
                setSelectedParamView(tmpTv)
                clearClickedValue("")
                refreshChartData(
                    refreshDaysHistory = true,
                    refreshWeeksHistory = true,
                    refreshMonthsHistory = true
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    myLogger.logThis(
                        tag = LogTags.USER_ACTION_CLICK_FEATURE,
                        from = TAG,
                        msg = "Indoor Temperature"
                    )
                }
            }
            humidityTv.setOnClickListener {
                selectedParamType = ParamTypes.HUMIDITY
                toggleCombinedCharts(showCombined = false)
                setSelectedParamView(humidityTv)
                clearClickedValue("")
                refreshChartData(
                    refreshDaysHistory = true,
                    refreshWeeksHistory = true,
                    refreshMonthsHistory = true
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    myLogger.logThis(
                        tag = LogTags.USER_ACTION_CLICK_FEATURE,
                        from = TAG,
                        msg = "Indoor Humidity"
                    )
                }
            }
            co2Tv.setOnClickListener {
                selectedParamType = ParamTypes.CO2
                toggleCombinedCharts(showCombined = false)
                setSelectedParamView(co2Tv)
                clearClickedValue("")
                refreshChartData(
                    refreshDaysHistory = true,
                    refreshWeeksHistory = true,
                    refreshMonthsHistory = true
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    myLogger.logThis(
                        tag = LogTags.USER_ACTION_CLICK_FEATURE,
                        from = TAG,
                        msg = "Indoor Carbon Dioxide"
                    )
                }
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

    private fun toggleIndoorParameters() {
        binding.apply {
            co2Card.isVisible = viewModel.hasIndoorData
            tmpCard.isVisible = viewModel.hasIndoorData
            humidityCard.isVisible = viewModel.hasIndoorData
            tvocCard.isVisible = viewModel.hasIndoorData
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
        if (selectedParamType == ParamTypes.IN_OUT_PM) {
            if (refreshDaysHistory) {
                val dayData = getChartDataForParam(forDays = true)
                updateCombinedChart(
                    chart = binding.daysCombinedChart,
                    title = daysChartTitle,
                    indoorChartData = dayData
                )
            }
            if (refreshWeeksHistory) {
                val weekData = getChartDataForParam(forWeek = true)
                updateCombinedChart(
                    chart = binding.weekChartCombined,
                    title = weekChartTitle,
                    indoorChartData = weekData
                )
            }
            if (refreshMonthsHistory) {
                val monthData = getChartDataForParam(forMonth = true)
                updateCombinedChart(
                    chart = binding.monthChartCombined,
                    title = monthChartTitle,
                    indoorChartData = monthData
                )
            }
        } else {
            if (refreshDaysHistory) {
                val dayData = getChartDataForParam(forDays = true)
                updateBarChart(
                    chart = binding.daysChart,
                    title = daysChartTitle,
                    chartData = dayData
                )
            }
            if (refreshWeeksHistory) {
                val weekData = getChartDataForParam(forWeek = true)
                updateBarChart(
                    chart = binding.weekChart,
                    title = weekChartTitle,
                    chartData = weekData
                )
            }
            if (refreshMonthsHistory) {
                val monthData = getChartDataForParam(forMonth = true)
                updateBarChart(
                    chart = binding.monthChart,
                    title = monthChartTitle,
                    chartData = monthData
                )
            }
        }
    }

    /******** outdoor data ***************/
    private val outChartData = arrayListOf<Float>()

    private fun getChartDataForParam(
        forDays: Boolean = false,
        forWeek: Boolean = false,
        forMonth: Boolean = false
    ): List<Float> {

        val chartData = arrayListOf<Float>()
        val chartDates = arrayListOf<String>()
        val outChartDates = arrayListOf<String>()

        //outdoor data
        outChartData.clear()
        outChartDates.clear()

        when (selectedParamType) {
            ParamTypes.IN_OUT_PM -> {
                when {
                    forDays -> {
                        for (aData in viewModel.currentlyDisplayedDaysHistoryData) {
                            if (viewModel.hasIndoorData) {
                                aData.data.indoor_pm.let {
                                    chartData.add(it)
                                    chartDates.add(aData.data.dates)
                                }
                            }
                            aData.data.outdoor_pm.let {
                                outChartData.add(it)
                                outChartDates.add(aData.data.dates)
                            }
                        }
                        viewModel.currentDatesForDaysChart = chartDates
                        viewModel.currentOutdoorDatesForDaysChart = outChartDates
                    }
                    forWeek -> {
                        for (aData in viewModel.currentlyDisplayedWeekHistoryData) {
                            if (viewModel.hasIndoorData) {
                                aData.data.indoor_pm.let {
                                    chartData.add(it)
                                    chartDates.add(aData.data.dates)
                                }
                            }
                            aData.data.outdoor_pm.let {
                                outChartData.add(it)
                                outChartDates.add(aData.data.dates)
                            }
                        }
                        viewModel.currentDatesForWeekChart = chartDates
                        viewModel.currentOutdoorDatesForWeekChart = outChartDates
                    }
                    forMonth -> {
                        for (aData in viewModel.currentlyDisplayedMonthHistoryData) {
                            if (viewModel.hasIndoorData) {
                                aData.data.indoor_pm.let {
                                    chartData.add(it)
                                    chartDates.add(aData.data.dates)
                                }
                            }
                            aData.data.outdoor_pm.let {
                                outChartData.add(it)
                                outChartDates.add(aData.data.dates)
                            }
                        }
                        viewModel.currentDatesForMonthChart = chartDates
                        viewModel.currentOutdoorDatesForMonthChart = outChartDates
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

        }
        return chartData
    }

    /************** GRAPH ***********/
    private fun styleBarChart(barChart: BarChart, barChartTag: String) {
        barChart.apply {
            axisRight.isEnabled = false
            axisLeft.isEnabled = true
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
                axisMinimum = 0f
                xAxis.setDrawLabels(false)

            }

            setNoDataTextTypeface(titleFont)


            setTouchEnabled(true)
            setPinchZoom(false)
            isDoubleTapToZoomEnabled = false
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

    private fun styleCombinedChart(combinedChart: CombinedChart, combinedChartTag: String) {
        combinedChart.apply {
            axisRight.isEnabled = false
            axisLeft.isEnabled = true
            tag = combinedChartTag


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
                axisMinimum = 0f
                xAxis.setDrawLabels(false)

            }


            setNoDataTextTypeface(titleFont)


            setTouchEnabled(true)
            setPinchZoom(false)
            isDoubleTapToZoomEnabled = false
            description.isEnabled = false
            setNoDataText(getString(R.string.loading_graph_data))
            setNoDataTextTypeface(bodyFont)
            setNoDataTextColor(blackColor)
            animateX(1000, Easing.EaseInExpo)

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {

                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let { entry ->
                        val isOutDoorData = h?.dataIndex != 1
                        displayClickedValue(entry, combinedChartTag, isOutDoorData = isOutDoorData)
                    }
                }

                override fun onNothingSelected() {
                    clearClickedValue(combinedChartTag)
                }
            })

        }
    }

    private fun displayClickedValue(
        entry: Entry,
        chartIdentifierTag: String,
        isOutDoorData: Boolean = false
    ) {
        try {
            val pos = entry.x.toInt() - 1
            val value = entry.y
            var clickedVal: String = ""
            val unitsTxt = when (selectedParamType) {
                ParamTypes.IN_OUT_PM -> getString(R.string.pm_units)
                ParamTypes.TMP -> getString(R.string.tmp_units)
                ParamTypes.TVOC -> getString(R.string.tvoc_units)
                ParamTypes.HUMIDITY -> getString(R.string.humid_units)
                ParamTypes.CO2 -> getString(R.string.co2_units)
            }
            val prefixTxt = when (selectedParamType) {
                ParamTypes.IN_OUT_PM -> if (isOutDoorData) getString(R.string.outdoor_txt)
                else getString(R.string.indoor_txt)
                ParamTypes.TMP -> getString(R.string.tmp_lbl)
                ParamTypes.TVOC -> getString(R.string.tvoc_lbl)
                ParamTypes.HUMIDITY -> getString(R.string.humidity_lbl)
                ParamTypes.CO2 -> getString(R.string.co_lbl)
            }
            when (chartIdentifierTag) {
                DAY_TAG -> {
                    clickedVal = if (isOutDoorData)
                        "$prefixTxt ${viewModel.currentOutdoorDatesForDaysChart[pos]} $value $unitsTxt"
                    else
                        "$prefixTxt ${viewModel.currentDatesForDaysChart[pos]} $value $unitsTxt"
                    binding.daysChartValue.text = clickedVal
                }
                WEEK_TAG -> {
                    clickedVal = if (isOutDoorData)
                        "$prefixTxt ${viewModel.currentOutdoorDatesForWeekChart[pos]} $value $unitsTxt"
                    else
                        "$prefixTxt ${viewModel.currentDatesForWeekChart[pos]} $value $unitsTxt"
                    binding.weekChartValue.text = clickedVal

                }
                MONTH_TAG -> {
                    clickedVal = if (isOutDoorData)
                        "$prefixTxt ${viewModel.currentOutdoorDatesForMonthChart[pos]} $value $unitsTxt"
                    else
                        "$prefixTxt ${viewModel.currentDatesForMonthChart[pos]} $value $unitsTxt"
                    binding.monthChartValue.text = clickedVal

                }
            }
        } catch (exc: Exception) {
            lifecycleScope.launch(Dispatchers.IO) {
                myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG displayClickedValue()",
                    msg = exc.message,
                    exc = exc
                )
            }
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

            lifecycleScope.launch(Dispatchers.IO) {
                myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG clearClickedValue()",
                    msg = exc.message,
                    exc = exc
                )
            }
        }
    }

    private fun styleBarDataSet(barDataSet: BarDataSet, valColorMap: ArrayList<Int>) {
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

    private fun styleLineDataSet(lineDataSet: LineDataSet, valColorMap: ArrayList<Int>) {
        lineDataSet.apply {
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


            setDrawCircleHole(false)

            lineWidth = 1f
            circleRadius = 2.5f


        }
    }

    private fun getStatusColorForValue(value: Float, paramType: ParamTypes): Int {

        val aqiIndex = viewModel.aqiIndex ?: DEFAULT_AQI_INDEX_PM25
        return when (paramType) {
            ParamTypes.IN_OUT_PM -> getAQIStatusFromPM25(
                aqiIndex = aqiIndex,
                pm25 = value.toDouble()
            ).aqi_color_res
            ParamTypes.TMP -> getColorResFromTmp(value.toDouble())
            ParamTypes.TVOC -> getColorResFromVoc(value.toDouble())
            ParamTypes.HUMIDITY -> getColorResFromHumid(value.toDouble())
            ParamTypes.CO2 -> getColorResFromCO2(value.toDouble())
        }

    }

    private fun getMaximumY(
        chartData: List<Float>
    ): Float {
        return when (selectedParamType) {
            ParamTypes.IN_OUT_PM -> {
                var defaultMax = 35f
                for (data in chartData) {
                    if (defaultMax < data) {
                        defaultMax = data
                    }
                }
                if (defaultMax > 35)
                    defaultMax *= 1.1f
                defaultMax
            }
            ParamTypes.TMP -> {
                var defaultMax = 45f
                for (data in chartData) {
                    if (defaultMax < data) {
                        defaultMax = data
                    }
                }
                if (defaultMax > 45)
                    defaultMax *= 1.1f
                defaultMax
            }
            ParamTypes.TVOC -> {
                var defaultMax = 0.7f
                for (data in chartData) {
                    if (defaultMax < data) {
                        defaultMax = data
                    }
                }
                if (defaultMax > 0.7)
                    defaultMax *= 1.1f
                defaultMax
            }
            ParamTypes.HUMIDITY -> {
                var defaultMax = 70f
                for (data in chartData) {
                    if (defaultMax < data) {
                        defaultMax = data
                    }
                }
                if (defaultMax > 70)
                    defaultMax *= 1.1f
                defaultMax
            }
            ParamTypes.CO2 -> {
                var defaultMax = 1000f
                for (data in chartData) {
                    if (defaultMax < data) {
                        defaultMax = data
                    }
                }
                if (defaultMax > 1000)
                    defaultMax *= 1.1f
                defaultMax
            }
        }
    }

    private fun updateBarChart(
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
            styleBarDataSet(daysHistoryDataSet, valColorMap)

            chart.apply {
                axisLeft.axisMaximum = getMaximumY(chartData)
                invalidate()
                data = BarData(daysHistoryDataSet)
                notifyDataSetChanged()
            }
        } catch (exc: Exception) {

            lifecycleScope.launch(Dispatchers.IO) {
                myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG updateChart()",
                    msg = exc.message,
                    exc = exc
                )
            }
        }
    }

    private fun updateCombinedChart(
        chart: CombinedChart,
        title: String,
        indoorChartData: List<Float>
    ) {
        try {

            //Outdoor data --- line data
            val mLineData = LineData()
            val outDoorEntries = ArrayList<Entry>()
            val outdoorValColorMap = ArrayList<Int>()

            for ((index, aData) in outChartData.withIndex()) {
                val entry = Entry(index.toFloat(), aData)
                outDoorEntries.add(entry)
                val color = getStatusColorForValue(aData, ParamTypes.IN_OUT_PM)
                outdoorValColorMap.add(ContextCompat.getColor(requireContext(), color))
            }

            val outDoorDataLbl = getString(R.string.outdoors_air_quality_txt)
            val set = MyLineDataSet(outDoorEntries, outDoorDataLbl)
            styleLineDataSet(set, outdoorValColorMap)
            mLineData.addDataSet(set)

            //Indoor data --- bar data
            val entries = ArrayList<BarEntry>()
            val valColorMap = ArrayList<Int>()
            for ((index, aData) in indoorChartData.withIndex()) {
                val entry = BarEntry(index.toFloat(), aData)
                entries.add(entry)
                val color = getStatusColorForValue(aData, selectedParamType)
                valColorMap.add(ContextCompat.getColor(requireContext(), color))
            }
            val mBarDataset = MyBarDataSet(entries, title)
            styleBarDataSet(mBarDataset, valColorMap)

            val combinedData = CombinedData()
            combinedData.setData(BarData(mBarDataset))
            combinedData.setData(mLineData)

            val combinedChartData = ArrayList<Float>()
            combinedChartData.addAll(outChartData)
            combinedChartData.addAll(indoorChartData)
            chart.apply {
                invalidate()
                axisLeft.axisMaximum = getMaximumY(combinedChartData)
                data = combinedData
                notifyDataSetChanged()
            }
        } catch (exc: Exception) {

            lifecycleScope.launch(Dispatchers.IO) {
                myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG updateChart()",
                    msg = exc.message,
                    exc = exc
                )
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

    internal class MyLineDataSet(
        yVals: List<Entry?>?,
        label: String?
    ) : LineDataSet(yVals, label) {

        override fun getCircleColor(index: Int): Int {
            return mColors[index]
        }

        override fun getEntryIndex(e: Entry?): Int {
            return super.getEntryIndex(e)
        }

        override fun getColor(index: Int): Int {
            return Color.rgb(128, 128, 128)
        }

    }
}

internal enum class ParamTypes {
    IN_OUT_PM,
    TMP,
    TVOC,
    HUMIDITY,
    CO2
}